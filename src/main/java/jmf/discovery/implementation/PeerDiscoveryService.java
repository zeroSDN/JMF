package jmf.discovery.implementation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.UnsignedInteger;
import com.google.protobuf.ByteString;

import jmf.data.ModuleHandle;
import jmf.data.ModuleHandleInternal;
import jmf.data.ModuleLifecycleState;
import jmf.data.ModuleUniqueId;
import jmf.discovery.IPeerDiscoveryCore;
import jmf.discovery.IPeerDiscoveryService;
import jmf.discovery.IPeerRegistry;
import jmf.proto.FrameworkProto;

/**
 * Service class to recognize other modules and there states in the network
 * The PeerDiscoveryService Class regularly sends out udp-multicasts to the network and receives those of other module instances.
 * In doing so it recognizes other modules with their current lifecycleState and adds them to the PeerRegistryClass, where all known module instances are listed and updated.
 * At the same time the service lets all other instances know of the current state of its own module so that they can update their registries in the same way.
 * The PeerDiscoveryService is a main component of the ZMF-framework and thus started and stopped with the ZMF-instance.
 * Created 8/1/15
 *
 * @author Matthias Blohm
 * @author Jonas Grunert
 */
public class PeerDiscoveryService implements IPeerDiscoveryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerDiscoveryService.class);

	/**
	 * Reference to the ZMF-Core to which the service belongs
	 */
	private IPeerDiscoveryCore core;

	/**
	 * Reference to the own ModuleHandle of this ZMF-instance
	 */
	private ModuleHandleInternal selfHandle;

	/**
	 * Contains the current lifecycle-state of the own module
	 */
	private ModuleLifecycleState selfState;

	/**
	 * Contains the current additional state of the own module
	 */

	private byte[] selfAdditionalState;

	/**
	 * This is where all peer modules are registered
	 */
	private final PeerRegistry peerRegistry = new PeerRegistry();

	/**
	 * MULTICAST_ADDRESS contains the ip address that is uses for the multicast group to which all peer discovery services subscribe
	 */
	private static final String MULTICAST_ADDRESS = "239.255.255.250";

	/**
	 * Multicast-Address used for sending out heartbeats to the multicast group
	 */
	private InetAddress multicastAddress;

	/**
	 * Socket for sending and receiving UDP-multicasts
	 */
	private MulticastSocket udpMulticastSocket;

	/**
	 * Portnumber used for multicasts
	 */
	private short udpPort;

	/** Factor to determine peerTimeout which is (multicastFrequency * timeoutFactor) */
	private static final int PEER_TIMEOUT_FACTOR = 4;

	/** Frequency to send state multicasts (in ms.) */
	private int multicast_Frequency;

	/** Time without a received state multicast to mark a peer as dead (in ms.) */
	private int peerTimeout;

	/** Indicates whether the PeerDiscoveryService is currently running */
	private final AtomicBoolean isStarted = new AtomicBoolean(false);

	/** Indicates whether a stop of the service has been requested which is beeing executed now */
	private final AtomicBoolean stopRequested = new AtomicBoolean(false);

	/** Indicates whether the sending Thread has been initialized already and is currently running */
	private final AtomicBoolean sendThreadInitialized = new AtomicBoolean(false);

	/** Indicates whether the receiving Thread has been initialized already and is currently running */
	private final AtomicBoolean receiveThreadInitialized = new AtomicBoolean(false);

	/** Threads for sending and receiving multicasts */
	private Thread sendMulticastThread;
	private Thread receiveMulticastsThread;

	/** Object for locking access to operations dependend on multicasts */
	private final Object lockMulticast = new Object();

	/** multicastIdentifier gets a random number that helps this instance to recognize its own broadcasts coming in */
	private final int multicastIdentifier;
	private boolean disableEqualModuleInterconnect;

	/**
	 * Constructor initializes the flags with false and creates random Number for multicastIdentifier
	 */
	public PeerDiscoveryService() {
		isStarted.set(false);
		stopRequested.set(false);
		receiveThreadInitialized.set(false);
		sendThreadInitialized.set(false);

		final Random rd = new Random();
		multicastIdentifier = rd.nextInt();
	}

	/**
	 * Starts the peer discovery service: State multicasting, peer discovery and module registry
	 *
	 * @param core
	 * 		Reference to the ZMF core
	 * @param selfHandle
	 * 		Module handle for the module of this instance. Contains information about module and its state
	 * @param broadcastFrequency
	 * 		Frequency to send out state multicasts (in milliseconds between multicasts)
	 * @param udpPort
	 * 		UDP port for state multicast communication
	 * @param peerDiscoveryWait
	 * 		If true the service will wait until all active modules discovered before it returns from the starting process
	 * @param disableEqualModuleInterconnect
	 * @return Returns true if start successful, false otherwise
	 */
	@Override
	public boolean start(final IPeerDiscoveryCore core, final ModuleHandleInternal selfHandle, final int broadcastFrequency, final short udpPort, final boolean peerDiscoveryWait, final boolean disableEqualModuleInterconnect) {
		
		if (isStarted.get()) {
			LOGGER.warn("start: Already stared - cancel start");
			return false;
		}
		
		try {
			this.core = core;
			this.multicast_Frequency = broadcastFrequency;
			this.peerTimeout = broadcastFrequency * PEER_TIMEOUT_FACTOR;
			this.udpPort = udpPort;
			this.selfHandle = selfHandle;
			this.selfState = ModuleLifecycleState.Dead;
			this.selfAdditionalState = new byte[0];
			this.disableEqualModuleInterconnect = disableEqualModuleInterconnect;
			
			final InetSocketAddress ownAddress = new InetSocketAddress(this.udpPort);
			
			this.multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
			
			this.udpMulticastSocket = new MulticastSocket(ownAddress);
			this.udpMulticastSocket.setReuseAddress(true);
			this.udpMulticastSocket.joinGroup(this.multicastAddress);
			
			this.stopRequested.set(false);
			
			this.sendMulticastThread = new Thread(this::sendMulticastLoop, "peerDiscovery Sender");
			this.sendMulticastThread.start();
			this.sendThreadInitialized.set(true);

			this.receiveMulticastsThread = new Thread(this::receiveMulticastsLoop, "peerDiscovery Receiver");
			this.receiveMulticastsThread.start();
			this.sendThreadInitialized.set(true);
			
			this.isStarted.set(true);


			if (peerDiscoveryWait) {
				LOGGER.info("will wait to discover peers");
				Thread.sleep(2 * multicast_Frequency);
				LOGGER.info("wait complete");
			}

			LOGGER.info("PeerDiscoveryService started");
			return true;
			
		} catch (final Exception e) {
			LOGGER.error("start: ERROR due to Exception: ", e);
			return false;
		}
	}

	/**
	 * Stops the peer discovery service
	 */
	@Override
	public void stop() {
		if (!isStarted.get()) {
			LOGGER.warn("stop: Not started - cancel stop");
			return;
		}
		
		stopRequested.set(true);

		try {
			this.udpMulticastSocket.leaveGroup(this.multicastAddress);
		} catch (final IOException e) {
			LOGGER.warn("stop: IOException while leaving multicast-group: ", e);
		}
		this.udpMulticastSocket.close();
		disposeThreads();
		
		this.isStarted.set(false);
		LOGGER.info("PeerDiscoveryService stopped");
	}

	/**
	 * Called when there is a change in the lifecycle-state of the own module, updates the selfState_ variable
	 *
	 * @param state
	 * 		The new lifecycle-state that the own module has now
	 */
	public void updateSelfState(final ModuleLifecycleState state) {
		synchronized (lockMulticast) {
			this.selfState = state;
		}
	}

	/**
	 * Called when there is a change in the additional state of own module, updates the selfAdditionalState_ variable
	 *
	 * @param additionalState
	 * 		The new additional state that the own module should be set to
	 */
	public void updateSelfAdditionalState(final byte[] additionalState) {
		synchronized (lockMulticast) {
			this.selfAdditionalState = additionalState;
		}
	}

	/**
	 * sends out a udp-multicast to the network with the current state of the own module
	 */
	@Override
	public void sendStateMulticast() {
		if (!this.isStarted.get() || this.stopRequested.get()) {
			return;
		}
		synchronized (lockMulticast) {
			try {
				final FrameworkProto.StateBroadcast.Builder multicastBuilder = FrameworkProto.StateBroadcast.newBuilder();
				multicastBuilder.setZmqPubPort(this.selfHandle.getSelfPubPort());
				multicastBuilder.setZmqRepPort(this.selfHandle.getSelfRepPort());
				multicastBuilder.setVersion(this.selfHandle.getVersion().shortValue());
				multicastBuilder.setLifecycleState(this.selfState.ordinal());
				multicastBuilder.setAdditionalStateInfos(ByteString.copyFrom(selfAdditionalState));
				multicastBuilder.setMulticastIdentifier(multicastIdentifier);
				multicastBuilder.setSenderName(selfHandle.getName());

				final FrameworkProto.SenderId senderId = selfHandle.getUniqueId().getSenderProto();
				multicastBuilder.setSenderId(senderId);

				final byte[] msg = multicastBuilder.build().toByteArray();

				final DatagramPacket packet = new DatagramPacket(msg, msg.length, this.multicastAddress, this.udpPort);
				this.udpMulticastSocket.send(packet);

			} catch (final Exception e) {
				LOGGER.error("sendMulticast failed due to Exception: ", e);
			}
		}
	}

	/**
	 * Returns the current peer registry with all actual know modules
	 *
	 * @return Peer registry where all known peers are registered
	 */
	@Override
	public IPeerRegistry getPeerRegistry() {
		return peerRegistry;
	}

	/**
	 * Performed by the receiveMulticastThread this loop frequently sends out multicasts as long as the service is running
	 */
	private void sendMulticastLoop() {
		LOGGER.info("sendMulticastLoop started");
		while (!this.stopRequested.get()) {

			sendStateMulticast();
			updatePeerTimeouts();

			try {
				Thread.sleep(multicast_Frequency);
			} catch (final InterruptedException e) {
				if (!this.stopRequested.get()) {
					LOGGER.error("sendMulticastLoop interrupted: ", e);
				}
			}

		}
		LOGGER.info("sendMulticastLoop finished");
	}

	/**
	 * Performed by the sendMulticastThread this loop continuously receives multicasts from the network as long as the service is running
	 */
	private void receiveMulticastsLoop() {
		LOGGER.info("receiveMulticastLoop started");
		while (!this.stopRequested.get()) {
			try {
				final byte[] buffer = new byte[1024];

				final InetAddress senderAddress;
				final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				this.udpMulticastSocket.receive(packet);
				senderAddress = packet.getAddress();

				final byte[] cut = new byte[packet.getLength()];
				System.arraycopy(buffer, 0, cut, 0, packet.getLength());
				final FrameworkProto.StateBroadcast stateMulticast = FrameworkProto.StateBroadcast.parseFrom(cut);

				processIncomingMulticast(stateMulticast, senderAddress.getHostAddress());

			} catch (final Exception e) {
				if (!stopRequested.get()) {
					LOGGER.warn("receiveMulticastLoop Exception: ", e);
				}
			}
		}
		LOGGER.info("receiveMulticastsLoop finished");
	}

	/**
	 * processes an incoming multicast-message and updates PeerRegistry if necessary
	 *
	 * @param stateMulticast
	 * 		the incoming message parsed back to protobuffer-class
	 * @param senderIp
	 * 		ip the sender's ip address
	 */
	private void processIncomingMulticast(final FrameworkProto.StateBroadcast stateMulticast, final String senderIp) {
		if (!this.isStarted.get() || this.stopRequested.get()) {
			return;
		}

		final ModuleUniqueId peerId = new ModuleUniqueId(stateMulticast.getSenderId());

		
		if (peerId.equals(this.selfHandle.getUniqueId()) && stateMulticast.getMulticastIdentifier() == this.multicastIdentifier) {
			return;
		}

		// check if disableEqualModuleInterconnect_ is set and if this is the case, discard any msg from peers with same module id
		if (this.disableEqualModuleInterconnect && peerId.getTypeId().intValue() == this.selfHandle.getUniqueId().getTypeId().intValue()) {
			LOGGER.debug("ignoring peer with same module type");
			return;
		}
		
		final ModuleLifecycleState peerState;


		peerState = ModuleLifecycleState.values()[stateMulticast.getLifecycleState()];


		if (peerState == ModuleLifecycleState.Dead) {
			final ModuleHandleInternal peerHandle = (ModuleHandleInternal) this.peerRegistry.getPeerWithId(peerId, false);
			if (peerHandle != null) {
				onPeerDead(peerHandle);
			}
		} else {
			final byte[] additionalStateInfo;
			if (stateMulticast.hasAdditionalStateInfos()) {
				additionalStateInfo = stateMulticast.getAdditionalStateInfos().toByteArray();
			} else {
				additionalStateInfo = "".getBytes();
			}
			ModuleHandleInternal peerHandle = (ModuleHandleInternal) this.peerRegistry.getPeerWithId(peerId, false);
			if (peerHandle == null) {
				final String pubAddr = "tcp://" + senderIp + ":" + String.valueOf(stateMulticast.getZmqPubPort());
				final String repAddr = "tcp://" + senderIp + ":" + String.valueOf(stateMulticast.getZmqRepPort());
				
				peerHandle = new ModuleHandleInternal(peerId, UnsignedInteger.fromIntBits(stateMulticast.getVersion()), stateMulticast.getSenderName(), pubAddr, repAddr, false);
				onPeerNew(peerHandle, peerState, additionalStateInfo);
			} else {
				peerHandle.resetPeerTimeout();

				peerRegistry.INTERNAL_updatePeerAdditionalState(peerHandle, additionalStateInfo);
				final ModuleLifecycleState oldState = peerRegistry.getPeerState(peerHandle);
				if (oldState != peerState) {
					LOGGER.debug("Peer state changed: " + peerHandle.getUniqueId().toString() + " to " + peerState.toString());
					peerRegistry.INTERNAL_updatePeerState(peerHandle, peerState);
					this.core.peerStateChange(peerHandle, peerState, oldState);
				}

			}
		}
	}

	/**
	 * updates the timeout-values of the registered modules in peerRegistry_
	 */
	private void updatePeerTimeouts() {
		final List<ModuleHandleInternal> timedOutModules = new ArrayList<>();
		synchronized (peerRegistry.peerDatastructuresLock) {
			final Map<ModuleUniqueId, ModuleHandle> allPeers = this.peerRegistry.INTERNAL_getAllPeers(false);

			for (final Entry<ModuleUniqueId, ModuleHandle> peer : allPeers.entrySet()) {
				final ModuleHandleInternal peerHandle = ((ModuleHandleInternal) peer.getValue());
				peerHandle.incrementPeerTimeout(this.multicast_Frequency);
				if (peerHandle.getPeerTimeout() > this.peerTimeout) {
					timedOutModules.add(peerHandle);
				}
			}
		}
		
		for (final ModuleHandleInternal timedOutPeer : timedOutModules) {
			onPeerDead(timedOutPeer);
		}
	}

	/**
	 * Called when a new peer recognized, updates peerRegistry_ and notifies core
	 *
	 * @param newPeer
	 * 		the ModuleHandle of a new peer that was recognized through multicast
	 * @param peerState
	 * 		the current lifecycle-state of the new peer that was recognized
	 * @param peerAdditionalState
	 * 		the current additional state of the new peer that was recognized
	 */
	private void onPeerNew(final ModuleHandleInternal newPeer, final ModuleLifecycleState peerState, final byte[] peerAdditionalState) {
		if (!this.isStarted.get() || this.stopRequested.get()) {
			return;
		}
		LOGGER.debug("New Peer registered: " + newPeer.getUniqueId().toString());
		this.peerRegistry.addModule(newPeer, peerState, peerAdditionalState);
		this.core.peerStateChange(newPeer, peerState, ModuleLifecycleState.Dead);
	}

	/**
	 * Called when a peer timed out or received dead multicast, updates PeerRegistry and notifies core
	 *
	 * @param deadPeer
	 * 		the ModuleHandle of a peer that was recognized as dead
	 */
	private void onPeerDead(final ModuleHandleInternal deadPeer) {
		if (!this.isStarted.get() || this.stopRequested.get()) {
			return;
		}
		LOGGER.debug("Peer dead: " + deadPeer.getUniqueId().toString());
		final ModuleLifecycleState lastState = peerRegistry.getPeerState(deadPeer);
		this.peerRegistry.removeModule(deadPeer.getUniqueId());
		this.core.peerStateChange(deadPeer, ModuleLifecycleState.Dead, lastState);
	}

	/**
	 * Makes sure that all threads are stopped correctly
	 */
	private void disposeThreads() {
		if (this.sendThreadInitialized.get() || this.receiveThreadInitialized.get()) {
			joinThreads();
			this.sendThreadInitialized.set(false);
			this.receiveThreadInitialized.set(false);
		}
		
	}

	/**
	 * Waits for both receiving and sending thread to terminate their execution if not done so far
	 */
	private void joinThreads() {
		if (this.sendMulticastThread.isAlive()) {
			LOGGER.trace("Waiting for sendMulticastThread to terminate");
			try {
				this.sendMulticastThread.join();
				LOGGER.trace("sendMulticastThread terminated");
			} catch (final Exception e) {
				LOGGER.error("Failed to join sendMulticastThread ", e);
			}
		}
		if (this.receiveMulticastsThread.isAlive()) {
			LOGGER.trace("Waiting for receiveMulticastThread to terminate");
			try {
				this.receiveMulticastsThread.join();
				LOGGER.trace("receiveMulticastThread terminated");
			} catch (final Exception e) {
				LOGGER.error("Failed to join receiveMulticastsThread ", e);
			}
		}
	}
}
