package jmf.core;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmf.config.IConfigurationProvider;
import jmf.data.*;
import jmf.discovery.IPeerDiscoveryCore;
import jmf.discovery.IPeerDiscoveryService;
import jmf.discovery.IPeerRegistry;
import jmf.messaging.IMessagingService;
import jmf.module.AbstractModule;
import jmf.module.IFrameworkAccess;
import jmf.module.IFrameworkController;

/**
 * Manages the ZMF execution and the module lifecycle.
 * Connects the module to execute, messaging and peer discovery.
 * Initiates module enabling, disabling or instance stopping.
 * Needs a module reference, a peer discovery service and a messaging service.
 * Also manages module dependency management.
 * Via the interface IZmfInstanceAccess it allows modules to interact with ZMF.
 * Via the interface IZmfInstanceController it allows external control of the ZMF.
 * created on 7/30/15.
 * @author Jonas Grunert
 * @author Tobias Korb
 */
public class Core implements IFrameworkAccess, IPeerDiscoveryCore, IFrameworkController {

	/** broadcastFrequency Peer discovery Frequency to send out state broadcasts (in milliseconds between broadcasts) */
	private static final int PEER_DISCOVERY_BROADCAST_FREQ = 1000;
	/** udpPort Peer discovery UDP port for state broadcast communication */
	static short PEER_DISCOVERY_UDP_PORT = 4213;
	/** Interval to check state and if all dependencies satisfied (in ms) */
	static int STATE_CHECK_INTERVAL = 100;

    /// Configuration provider providing module configuration
	IConfigurationProvider configProvider;

    /// Logger instance
    private static final Logger LOGGER = LoggerFactory.getLogger(Core.class);

	/** Module to be operated by this instance */
	AbstractModule selfModule;
	/** Module state handle of this instances module */
	ModuleHandleInternal selfModuleHandle;

	/** Service to discover other modules */
	IPeerDiscoveryService peerDiscoveryService;
	/** Service operating the messaging message bus */
	ModuleEventDispatcher eventDispatcher;

	boolean exitWhenEnableFail = false;

    // Control flags
	AtomicBoolean isStarted;
	AtomicBoolean isStopped;
	AtomicBoolean moduleActive;
	AtomicBoolean stopRequested;
	AtomicBoolean disableRequested;
	AtomicBoolean enableRequested;

	/// Indicates if zmfInstanceThread initialized and not disposed
	AtomicBoolean zmfInstanceThreadInitialized;
	Thread zmfInstanceThread;

	private final Object lockPeerChange = new Object();


    /**
     * Constructor of the Core class, initializing flags and basic variables
     * @param configProvider Configuration service providing configuration values
     * @param module Module to be managed by this core
     * @param peerDiscoveryService Peer discovery service detecting other peers on the ZMF bus
     * @param zmqSrv ZMQ messaging service for communicating over the ZMF/ZMQ bus
     */
	public Core(final IConfigurationProvider configProvider, final AbstractModule module, final IPeerDiscoveryService peerDiscoveryService, final IMessagingService zmqSrv) {

		this.configProvider = configProvider;
        this.selfModule = module;
		this.peerDiscoveryService = peerDiscoveryService;
		isStarted = new AtomicBoolean(false);
		isStopped = new AtomicBoolean(true);
		moduleActive = new AtomicBoolean(false);
		stopRequested = new AtomicBoolean(false);
		disableRequested = new AtomicBoolean(false);
		enableRequested = new AtomicBoolean(false);
		zmfInstanceThreadInitialized = new AtomicBoolean(false);
		selfModuleHandle = new ModuleHandleInternal(selfModule.getUniqueId(), selfModule.getVersion(), module.getModuleName(), true);
		eventDispatcher = new ModuleEventDispatcher(zmqSrv, configProvider, this);
	}

	public void close() {
		LOGGER.trace("Close: started closing Instance");
		// Stop instance if not already stopped or stopping
		if (isStarted.get() && !stopRequested.get()) {
			stopInstance();
		}

		// Join and dispose thread if not disposed
		disposeThread();
		LOGGER.trace("Close: Instance closed");
	}

    /**
     * Starts this ZMF core
     * @param moduleAutoEnable If true tries to enable the module as soon as possible
     * @param exitWhenEnableFail If true will exit if module enabling fails
     * @param peerDiscoveryWait If true the peer discovery will wait until all active modules discovered
     * @param disableEqualModuleInterconnect
	 * @return True if starting successful
     */
	public boolean startInstance(final boolean moduleAutoEnable, final boolean exitWhenEnableFail, final boolean peerDiscoveryWait, final boolean disableEqualModuleInterconnect) {
		if (isStarted.get()) {
			LOGGER.trace("Trying to start JmfCore but it's allready started");
			return false;
		}


		LOGGER.trace("Trying to start JmfCore");

		isStopped.set(false);

		// First start ZMQ service so that it will not miss any host discovered notification
		if (!eventDispatcher.start(selfModule, selfModuleHandle, configProvider)) {
			LOGGER.error("Trying to start JmfCore: Failed to start event dispatcher - canceling Start");
			return false;
		}

		LOGGER.trace("Trying to start JmfCore - ZMQ Service started");
		// Now start peer discovery service
		if (!peerDiscoveryService.start(this, selfModuleHandle, PEER_DISCOVERY_BROADCAST_FREQ, PEER_DISCOVERY_UDP_PORT, peerDiscoveryWait, disableEqualModuleInterconnect)) {
			LOGGER.error("Trying to start JmfCore: Failed to start PeerDeiscovery Service - canceling Start");
			eventDispatcher.stop();
			return false;
		}
		LOGGER.trace("Trying to start JmfCore - Peer Discovery Service started");
		// Update and broadcast module state: Inactive now
		peerDiscoveryService.updateSelfState(ModuleLifecycleState.Inactive);
		peerDiscoveryService.sendStateMulticast();

		this.exitWhenEnableFail = exitWhenEnableFail;
		enableRequested.set(moduleAutoEnable);

		// Start zmfCheckThread
		LOGGER.trace("Trying to start JmfCore - Starting module state check thread");
		stopRequested.set(false);

		zmfInstanceThread = new Thread(this::zmfInstanceLoop, "instance thread");
		zmfInstanceThread.start();
		zmfInstanceThreadInitialized.set(true);

		// Start finished
		isStarted.set(true);
		LOGGER.trace("Trying to start JmfCore - JmfCore successfull started");
		return true;
	}

	public void stopInstance() {

		if (!isStarted.get() && enableRequested.get()) {
			enableRequested.set(false);
			return;
		}

		if (!isStarted.get()) {
			LOGGER.error("Stop ZMF Core: Not running - canceling Stop");
			return;
		}
		LOGGER.trace("Stop ZMF Core");
		stopRequested.set(true);

		synchronized (lockPeerChange) {
			lockPeerChange.notifyAll();
		}

		LOGGER.trace("Stop ZMF Core: Wait for zmfInstanceThread to terminate");
		try {
			zmfInstanceThread.join();
		} catch (final InterruptedException e) {
			LOGGER.error("Stop ZMF Core: zmfInstanceThread Termination was interrupted");
		}
	}

	public void requestEnableModule() {
		if (moduleActive.get()) {
			LOGGER.error("RequestEnableModule: Module already enabled - ignoring request");
			return;
		}
		disableRequested.set(false);
		enableRequested.set(true);
		synchronized (lockPeerChange) {
			lockPeerChange.notifyAll();
		}
		LOGGER.trace("RequestEnableModule : Finished Request");
	}

	public void joinExecution() {
		if (!zmfInstanceThreadInitialized.get()) {
			LOGGER.error("JoinExecution: Module thread not started - nothing to join");
			return;
		}
		try {
			LOGGER.trace("JoinExecution: joining zmfInstanceThread now");
			zmfInstanceThread.join();
		} catch (final Exception e) {
			LOGGER.error("JoinExecution: Failed to join zmfInstanceThread");
		}
		LOGGER.trace("JoinExecution: zmfInstanceThread joined");
	}

	private void disposeThread() {
		LOGGER.trace("DisposeThread: Start disposing");
		if (zmfInstanceThreadInitialized.get()) {
			LOGGER.trace("DisposeThread: Instance Stopping in process - waiting");
			// If instance stopping in progress: Wait for instance to finish before destroying it
			this.joinExecution();
		}
		LOGGER.trace("DisposeThread: Thread Disposed");
	}

	private void zmfInstanceLoop() {
		LOGGER.trace("zmfInstanceLoop: Loop started");
		// Loop for checking and updating state of the module
		while (!stopRequested.get()) {
			if (moduleActive.get()) {
				// Module active
				if (disableRequested.get()) {
					// Disable module
					disableRequested.set(false);
					disableModule();
				} else {
					// Check dependencies
					if (!checkDependenciesSatisfied()) {
						// Disable when dependencies not satisfied anymore
						disableModule();
						// But re-enable as soon as possible
						enableRequested.set(true);
					} else if (!moduleIsUnique()) {
						LOGGER.error("Module not unique - other module with same ID found. Stopping instance");
						stopRequested.set(true);
					}
				}
			} else {
				// Module inactive
				if (enableRequested.get() && checkDependenciesSatisfied() && moduleIsUnique()) {
					// Try to enable module
					enableRequested.set(false);
					LOGGER.info("Initiating enable module");
					final boolean enableSuccess = enableModule(false);
					if (!enableSuccess && exitWhenEnableFail) {
						// Exit when exitWhenEnableFail and enable failed
						stopRequested.set(true);
					}
				}
			}

			// Sleep for defined interval

			synchronized (lockPeerChange) {
				try {
					lockPeerChange.wait();
				} catch (InterruptedException e) {
					LOGGER.warn("", e);
				}
			}
		}
		LOGGER.trace("zmfInstanceLoop: Loop finished");
		onInstanceStopped();
	}

	private void onInstanceStopped() {
		if (!isStarted.get()) {
			LOGGER.error("OnInstanceStopped: Instance not started or already stopped");
			return;
		}

		// Disable module if still enabled - we are done now
		if (moduleActive.get()) {
			LOGGER.trace("OnInstanceStopped: module still enabled - disable module");
			disableModule();
		}

		isStarted.set(false);

		// Update state and send broadcast
		peerDiscoveryService.updateSelfState(ModuleLifecycleState.Dead);
		peerDiscoveryService.sendStateMulticast();

		// Stop services
		peerDiscoveryService.stop();
		eventDispatcher.stop();

		isStopped.set(true);

		LOGGER.trace("OnInstanceStopped: Instance stopped");
	}

	/**
	 * Tries to enable the module controlled by this instance controller
	 *
	 * @param checkDependencies
	 * 		Only enables module if all dependencies satisfied
	 */
	private boolean enableModule(final boolean checkDependencies) {
		if (moduleActive.get()) {
			LOGGER.error("EnableModule: Module already started - cancel enable");
			return false;
		}

		if (checkDependencies && !checkDependenciesSatisfied()) {
			LOGGER.error("EnableModule: Dependencies not satisfied - cancel enable");
			return false;
		}

		eventDispatcher.onEnable();

		final boolean enable;
		synchronized (selfModule.INTERNAL_getInternalMutex()) {
			enable = selfModule.INTERNAL_internalEnable(this);
		}
		if (enable) {
			moduleActive.set(true);
			// Update state and send broadcast
			peerDiscoveryService.updateSelfState(ModuleLifecycleState.Active);
			peerDiscoveryService.sendStateMulticast();
			return true;
		} else {
			LOGGER.error("EnableModule: Failed to enable module - cancel enable");
			eventDispatcher.onDisable();
		}

		return false;
	}

	private void disableModule() {
		if (!moduleActive.get()) {
			LOGGER.error("DisableModule: Module already stopped - cancel disable");
			return;
		}

		eventDispatcher.onDisable();
		synchronized (selfModule.INTERNAL_getInternalMutex()) {
			selfModule.INTERNAL_internalDisable();
		}
		moduleActive.set(false);
		// Update state and send broadcast
		peerDiscoveryService.updateSelfState(ModuleLifecycleState.Inactive);
		peerDiscoveryService.sendStateMulticast();
		LOGGER.trace("DisableModule: Module disabled");
	}

	/**
	 * Checks if all required module dependencies are present
	 */
	private boolean checkDependenciesSatisfied() {
		LOGGER.trace("CheckDependenciesSatisfied: Start Checking");
		final Collection<ModuleDependency> moduleDependencies = selfModule.getDependencies();
		if (moduleDependencies == null) {
			return true;
		}
		for (final ModuleDependency moduleDependency : moduleDependencies) {
			if (!peerDiscoveryService.getPeerRegistry().containsPeerWithType(moduleDependency.getModuleTypeId(), true)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return true if there exists no other equal module in the PeerDiscovery
	 */
	private boolean moduleIsUnique() {
		LOGGER.trace("ModuleIsUnique: Start Checking");
		final ModuleUniqueId uniqueId = selfModule.getUniqueId();
		return !peerDiscoveryService.getPeerRegistry().containsPeerWithId(uniqueId, false);
	}

	@Override
	public IConfigurationProvider getConfigurationProvider() {
		return configProvider;
	}

	public IPeerRegistry getPeerRegistry() {
		return peerDiscoveryService.getPeerRegistry();
	}

	/**
	 * Tells the MessageService to send a message to a specific module
	 *
	 * @param msg
	 * 		The message sent in this request
	 * @param target
	 * 		the module to which the request will be send to
	 * @return The InReply which contains a future to access the real reply sometime
	 */
	@Override
	public InReply sendRequest(final ModuleUniqueId target, final Message msg) {
		return eventDispatcher.sendRequest(target, msg);
	}

	/**
	 * Tells the MessageService to send a message to a specific module
	 *
	 * @param topic
	 * 		The topic to subscribe to
	 * @param handler
	 * 		callback handler for received events
	 */
	@Override
	public SubscriptionHandle subscribe(final MessageType topic, final BiConsumer<Message, ModuleUniqueId> handler) {
		return eventDispatcher.subscribe(topic, handler);
	}

	/**
	 * Tells the MessageService to send a message to a specific module
	 *
	 * @param msg
	 * 		The message that will be published
	 */
	@Override
	public void publish(final Message msg) {
		eventDispatcher.publish(msg);
	}

	/**
	 * Called when the additional state of a module was changed.
	 * Will not trigger automatically trigger a state broadcast.
	 *
	 * @param additionalState
	 * 		the changed additionalstates
	 */
	@Override
	public void onModuleAdditionalStateChanged(final byte[] additionalState) {
		peerDiscoveryService.updateSelfAdditionalState(additionalState);
	}

	/**
	 * Triggers a state broadcast for the module of this instance.
	 */
	@Override
	public void forceStateBroadcast() {
		peerDiscoveryService.sendStateMulticast();
	}

	/**
	 * Module disable is requested
	 */
	@Override
	public void requestDisableModule() {
		if (!moduleActive.get() && !enableRequested.get()) {
			LOGGER.error("RequestDisableModule: Module not enabled - ignoring request");
			return;
		}
		enableRequested.set(false);
		disableRequested.set(true);
		synchronized (lockPeerChange) {
			lockPeerChange.notifyAll();
		}
		LOGGER.trace("RequestDisableModule: Finished Request");
	}

	/**
	 * Instance stop is requested
	 */
	@Override
	public void requestStopInstance() {
		if (!isStarted.get() && !enableRequested.get()) {
			LOGGER.error("requestStopInstance: Instance not started - ignoring request");
			return;
		}
		enableRequested.set(false);
		stopRequested.set(true);
		synchronized (lockPeerChange) {
			lockPeerChange.notifyAll();
		}
	}

	@Override
	public boolean requestEnableRemoteInstance(final ModuleUniqueId id, final long timeout) {
		InReply reply = eventDispatcher.sendRequest(id, new Message(new byte[]{0x03, -1}, new byte[]{0x03}));

		Message content;
		try {
			content = reply.get(timeout, TimeUnit.SECONDS);
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
			return false;
		}
		return content.getData()[0] == 0x03;
	}

	@Override
	public boolean requestDisableRemoteInstance(final ModuleUniqueId id, final long timeout) {
		InReply reply = eventDispatcher.sendRequest(id, new Message(new byte[]{0x03, -1}, new byte[]{0x01}));

		Message content;
		try {
			content = reply.get(timeout, TimeUnit.SECONDS);
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
			return false;
		}
		return content.getData()[0] == 0x01;
	}

	@Override
	public boolean requestStopRemoteInstance(final ModuleUniqueId id, final long timeout) {
		InReply reply = eventDispatcher.sendRequest(id, new Message(new byte[]{0x03, -1}, new byte[]{0x02}));

		Message content;
		try {
			content = reply.get(timeout, TimeUnit.SECONDS);
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			e.printStackTrace();
			return false;
		}
		return content.getData()[0] == 0x02;
	}

	/**
	 * Called when there are state changes of a module
	 *
	 * @param lastState
	 * 		the know state of the module
	 * @param module
	 * 		the module that has changed
	 */
	@Override
	public void peerStateChange(final ModuleHandleInternal module, final ModuleLifecycleState newState, final ModuleLifecycleState lastState) {
		synchronized (lockPeerChange) {
			lockPeerChange.notifyAll();
		}

		eventDispatcher.onPeerStateChange(module, newState, lastState);
	}

	@Override
	public AbstractModule getModule() {
		return selfModule;
	}

	@Override
	public boolean isStarted() {
		return isStarted.get();
	}

	@Override
	public boolean isStopped() {
		return isStopped.get();
	}
}
