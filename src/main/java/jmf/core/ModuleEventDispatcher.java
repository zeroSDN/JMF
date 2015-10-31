package jmf.core;

import java.util.Arrays;
import java.util.OptionalLong;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import jmf.discovery.IPeerDiscoveryCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmf.config.IConfigurationProvider;
import jmf.data.*;
import jmf.messaging.IMessagingCore;
import jmf.messaging.IMessagingService;
import jmf.messaging.ISubscriptionHandler;
import jmf.messaging.implementation.ExternalRequestIdentity;
import jmf.module.AbstractModule;
import jmf.module.IFrameworkController;

/**
 * Event dispatching class receiving ZMQ messages (events, requests) and PeerStateChanges.
 * The events are queued in a Poco notification queue and are delivered by one processing thread.
 * This guarantees singlethreaded calls to module implementations
 * @author Jonas Grunert
 * @author Jan Strauß
 * @author Tobias Korb
 * created on 7/13/15.
 */
public class ModuleEventDispatcher implements ISubscriptionHandler, IMessagingCore, IPeerDiscoveryCore {
	
	private static abstract class ModuleEventNotification {
		public enum Type {EVENT, REQUEST, PeerStateChange}
		
		private final Type type;
		
		public ModuleEventNotification(final Type type) {
			this.type = type;
		}
	}

    /**
     * Internal base class for queued notifications for received messages
     */
	private static class MessageEventNotification extends ModuleEventNotification {
		private final Message message;
		private final ModuleUniqueId sender;
		private final ExternalRequestIdentity id;

		public MessageEventNotification(final Type type, final Message message, final ModuleUniqueId sender, final ExternalRequestIdentity id) {
			super(type);
			this.message = message;
			this.sender = sender;
			this.id = id;
		}
	}

    /**
     * Internal class for queued notifications for peer state changes
     */
	private static class PeerStatechangeEventNotification extends ModuleEventNotification {

		private final ModuleHandleInternal module;
		private final ModuleLifecycleState newState;
		private final ModuleLifecycleState lastState;

		public PeerStatechangeEventNotification(final ModuleHandleInternal module, final ModuleLifecycleState newState, final ModuleLifecycleState lastState) {
			super(Type.PeerStateChange);
			this.module = module;
			this.newState = newState;
			this.lastState = lastState;
		}
	}

	/** Module to be operated by this instance */
	private AbstractModule selfModule;

	/** All subscriptions of the module operated by this instance */
	private final ConcurrentMap<Integer, SubscriptionHandle> moduleSubscriptionHandlers = new ConcurrentHashMap<>();

    /** BlockingQueue queueing messages and notifications to be delivered */
	private final BlockingQueue<ModuleEventNotification> deliveryQueue = new LinkedBlockingQueue<>();
	
	/** Service operating the messaging message bus */
	private final IMessagingService msgService;
	
	private final AtomicBoolean alive = new AtomicBoolean(false);
	
	private Thread thread;
	
	private final AtomicInteger subCounter = new AtomicInteger(0);

	private final IFrameworkController core;
	private final IConfigurationProvider config;

	private final static Logger LOGGER = LoggerFactory.getLogger(ModuleEventDispatcher.class);

	private final static MessageType SYSTEM_REQUEST = new MessageType(new byte[]{0x03, -1});
	private final static byte SYSTEM_REQUEST_ENABLE = 0x03;
	private final static byte SYSTEM_REQUEST_DISABLE = 0x01;
	private final static byte SYSTEM_REQUEST_STOP = 0x02;

    private long ZMF_INMSG_BUFFER_SIZE = 100000;
    private long ZMF_INMSG_BUFFER_MODE = ZMF_INMSG_BUFFER_MODE_BLOCK;
    private static final long ZMF_INMSG_BUFFER_MODE_BLOCK = 0;
    private static final long ZMF_INMSG_BUFFER_MODE_DROP = 1;
    private static final long ZMF_INMSG_BUFFER_MODE_BLOCK_WAITMS = 10;



    public ModuleEventDispatcher(final IMessagingService iMessagingService, final IConfigurationProvider config, IFrameworkController core) {
		this.config = config;
		this.core = core;
		msgService = iMessagingService;
		subCounter.set(0);
	}
	
	/**
	 * This method starts the JmfMessaging system
	 *
	 * @param selfModule
	 * 		the module this system will take care of
	 * @param selfModuleHandle
	 * 		the statehandle of this systems module
	 * @return return weather the start was successfull or not
	 */
	public boolean start(final AbstractModule selfModule, final ModuleHandleInternal selfModuleHandle, final IConfigurationProvider config) {
		LOGGER.trace("Start: Starting JmfMessaging");
		this.selfModule = selfModule;
		LOGGER.trace("Start: Start MessageService");


        OptionalLong cfgTmp;

        if(config != null && (cfgTmp = config.getAsLong("ZMF_INMSG_BUFFER_SIZE")).isPresent()) {
            ZMF_INMSG_BUFFER_SIZE = cfgTmp.getAsLong();
        }
        LOGGER.debug("ZMF_INMSG_BUFFER_SIZE configuration: " + ZMF_INMSG_BUFFER_SIZE);

        if(config != null && (cfgTmp = config.getAsLong("ZMF_INMSG_BUFFER_MODE")).isPresent()) {
            ZMF_INMSG_BUFFER_MODE = cfgTmp.getAsLong();
        }
        LOGGER.debug("ZMF_INMSG_BUFFER_MODE configuration: " + ZMF_INMSG_BUFFER_MODE);


		final boolean start = msgService.start(this, selfModuleHandle, config);
		if (start) {
			msgService.peerJoin(selfModuleHandle);
		}
		return start;
	}
	
	/**
	 * called when JmfMessaging is getting stoped because the module doesnt need it anymore
	 */
	public void stop() {
		LOGGER.trace("Stop: Shut down JmfMessaging");
		onDisable();
		msgService.stop();
		LOGGER.trace("Stop: JmfMessaging stopped");

	}
	
	/**
	 * Called when disabling a module - clears all subscriptions
	 */
	public void onDisable() {
		if (!alive.get()) {
			LOGGER.error("onDisable: module not running - ignore command");
			return;
		}
		LOGGER.trace("onDisable: Start Disabling");
		alive.set(false);
		LOGGER.trace("onDisable: wait for thread to terminate");
		try {
			thread.interrupt();
			thread.join();
		} catch (final InterruptedException e) {
			LOGGER.trace("onDisable: Thread interrupted");
		}
		LOGGER.trace("onDisable: Thread terminated");
		LOGGER.trace("onDisable: Start unsubscribing");
		
		for (final SubscriptionHandle handler : moduleSubscriptionHandlers.values()) {
			msgService.unsubscribe(handler.getTopic());
		}
		moduleSubscriptionHandlers.clear();
		
		
		LOGGER.trace("onDisable: Unsubscribed");
		msgService.onDisable();
		LOGGER.trace("onDisable: Disable finished");
	}
	
	/**
	 * Is called when the selfmodule is getting enabled. Starts the
	 * deliveryloop to be able to handle messages
	 */
	public void onEnable() {
		if (alive.get()) {
			LOGGER.error("onEnable: module allready alive - ignore command");
			return;
		}
		LOGGER.trace("onEnable: Start Enabling - start loop");
		alive.set(true);
		
		thread = new Thread(ModuleEventDispatcher.this::deliveryLoop, "Messaging Delivery Loop Thread");
		thread.start();
		
		LOGGER.trace("onEnable: Enabling finished - loop running");
		
	}
	
	// --------------------- From Core  --------------------- //

	/**
	 * @param topic
	 * 		the topic on which the module wants to subscribe
	 * @param handler
	 * 		Function beeing called by incoming events
	 */
	public SubscriptionHandle subscribe(final MessageType topic, final BiConsumer<Message, ModuleUniqueId> handler) {
		checkAlive();
		final SubscriptionHandle subHandle = new SubscriptionHandle(this, topic, handler);
		moduleSubscriptionHandlers.put(subHandle.getSubId(), subHandle);
		
		msgService.subscribe(topic);
		LOGGER.trace("Subscribe: Subscribed for Topic: " + topic.toString());
		return subHandle;
	}
	
	/**
	 * Publishes a message
	 *
	 * @param msg
	 * 		The message that will be published
	 */
	public void publish(final Message msg) {
		msgService.publish(msg);
	}
	
	/**
	 * @param target
	 * 		the module the request will be send to
	 * @param msg
	 * 		the message that will be send
	 * @return A reply containing a future to receive a response
	 */
	public InReply sendRequest(final ModuleUniqueId target, final Message msg) {
		checkAlive();
		return msgService.sendRequest(target, msg);
	}

	/**
	 * Called by JmfCore to let this instance know a new module has joined
	 *
	 * @param module
	 * 		the module that has joined the network
	 */
	public void onPeerChange(final ModuleHandleInternal module, final ModuleLifecycleState newState, final ModuleLifecycleState lastState) {

		// Notify ZMQ service
		if (lastState == ModuleLifecycleState.Dead) {
			msgService.peerJoin(module);
		} else if (newState == ModuleLifecycleState.Dead) {
			msgService.peerLeave(module);
		}

        // Dont block or drop peer state changes (like incoming messages)

		deliveryQueue.add(new PeerStatechangeEventNotification(module, newState, lastState));
	}

	/**
	 * Called from ZMQ when a Message with a topic this module subscribed to arrives
	 *
	 * @param message
	 * 		the incoming message
	 * @param sender
	 * 		the unique id of the module that sent the message
	 */
	@Override
	public void onSubMsgReceived(final Message message, final ModuleUniqueId sender) {

        if (!alive.get()) {
            return;
        }

        // Prevent buffer overflow, block or drop
        if (!checkWaitMessageQueueBufferInsert()) {
            // Return if dropping message necessary
            return;
        }

        // Queue message to event queue
        deliveryQueue.add(new MessageEventNotification(ModuleEventNotification.Type.EVENT, message, sender, null));
	}
	
	/**
	 * Called from ZMQ when a request is received
	 *
	 * @param id
	 * 		request id
	 * @param message
	 * 		the message of the request
	 * @param sender
	 * 		unique id of the module that sent the request
	 */
	@Override
	public void onRequestMsgReceived(final ExternalRequestIdentity id, final Message message, final ModuleUniqueId sender) {
		if (SYSTEM_REQUEST.containsTopic(message.getType())) {
			handleSystemMessage(id, message);
			LOGGER.info("handled request as system message");
			return;
		}

		if (!alive.get()) {
			return;
		}

        // Prevent buffer overflow, block or drop
        if (!checkWaitMessageQueueBufferInsert()) {
            // Return if dropping message necessary
            return;
        }

        // Queue message to event queue
        deliveryQueue.add(new MessageEventNotification(ModuleEventNotification.Type.REQUEST, message, sender, id));
	}

	private void handleSystemMessage(final ExternalRequestIdentity id, final Message message) {
		if (SYSTEM_REQUEST_ENABLE == message.getData()[0]) {
			msgService.sendReply(id, new Message(new byte[]{0x04, -1}, new byte[]{0x03}));
			core.requestEnableModule();

		} else if (SYSTEM_REQUEST_DISABLE == message.getData()[0]) {
			msgService.sendReply(id, new Message(new byte[]{0x04, -1}, new byte[]{0x01}));
			core.requestDisableModule();

		} else if (SYSTEM_REQUEST_STOP == message.getData()[0]) {
			msgService.sendReply(id, new Message(new byte[]{0x04, -1}, new byte[]{0x02}));
			core.requestStopInstance();

		} else {
			msgService.sendReply(id, new Message(new byte[]{0x04, -1}, "unknown system message".getBytes()));
			LOGGER.error("received unknown system message: " + Arrays.toString(message.getData()));
		}
	}


    boolean checkWaitMessageQueueBufferInsert() {

        if (ZMF_INMSG_BUFFER_SIZE > 0 && deliveryQueue.size() >= ZMF_INMSG_BUFFER_SIZE) {
            // Buffer full
            if (ZMF_INMSG_BUFFER_MODE == ZMF_INMSG_BUFFER_MODE_BLOCK) {
                // Block and wait
                while (alive.get() && deliveryQueue.size() >= ZMF_INMSG_BUFFER_SIZE) {
                    try {
                        Thread.sleep(ZMF_INMSG_BUFFER_MODE_BLOCK_WAITMS);
                    } catch (InterruptedException e) {
                        LOGGER.warn("checkWaitMessageQueueBufferInsert interrupted");
                        return false;
                    }
                }
                return alive.get();
            } else if (ZMF_INMSG_BUFFER_MODE == ZMF_INMSG_BUFFER_MODE_DROP) {
                // Drop packet
                return false;
            }
        }
        return true;
    }

	
	/**
	 * Remove a Subscription
	 *
	 * @param handle
	 * 		the subscriptionhandle containing the subscription ID of the subscription that will be removed
	 * @throws Exception
	 */
	@Override
	public void unsubscribe(final SubscriptionHandle handle) throws Exception {
		checkAlive();
		moduleSubscriptionHandlers.remove(handle.getSubId());
		msgService.unsubscribe(handle.getTopic());
		LOGGER.trace("Unsubscribe: Unsubscribed from Topic: " + handle.getTopic().toString());
	}
	
	/**
	 * Get a new subscription id
	 *
	 * @return the new id generated by an atomic integer to make it a unique id
	 */
	@Override
	public int getSubId() {
		return subCounter.incrementAndGet();
	}
	
	/**
	 * The loop that runs in a thread that will keep track of every incoming event
	 */
	private void deliveryLoop() {
		LOGGER.trace("deliveryLoop: Enter Loop");
		while (alive.get()) {
			try {
				final ModuleEventNotification messageNotification = deliveryQueue.take();
				
				switch (messageNotification.type) {

					case PeerStateChange:
						handlePeerStatechange((PeerStatechangeEventNotification) messageNotification);
						break;

					case EVENT:
						handleEvent((MessageEventNotification) messageNotification);
						break;

					case REQUEST:
						handleRequest((MessageEventNotification) messageNotification);
						break;
				}
			} catch (final InterruptedException e) {
				if (alive.get()) {
					LOGGER.error("interrupted but alive", e);
				} else {
					break;
				}
			}
		}
		LOGGER.trace("deliveryLoop: Leaving Loop");
	}

	private void handleRequest(final MessageEventNotification messageNotification) {
		final OutReply reply;

		synchronized (selfModule.INTERNAL_getInternalMutex()) {
			if (selfModule.isEnabled()) {
                try {
                    reply = selfModule.handleRequest(messageNotification.message, messageNotification.sender);
                }
                catch (Exception exc) {
                    LOGGER.error("Exception when calling handleRequest while DELIVERY_REQUEST", exc);
                    return;
                }
			} else {
				LOGGER.warn("Cannot deliver handleRequest: Module not enabled");
				return;
			}
		}

		if (reply == null) {
			return;
		}

		switch (reply.getType()) {
			case IMMEDIATE_REPLY:
				msgService.sendReply(messageNotification.id, reply.getReplyImmediate());
				break;

			case FUTURE_REPLY:
				reply.injectFutureInfo(msgService, messageNotification.id);
				break;

			case NO_REPLY:
				break;
		}
	}

	private void handleEvent(final MessageEventNotification messageNotification) {
		for (final SubscriptionHandle handle : moduleSubscriptionHandlers.values()) {
			if (handle.getTopic().containsTopic(messageNotification.message.getType())) {
				synchronized (selfModule.INTERNAL_getInternalMutex()) {
					if (selfModule.isEnabled()) {
                        try {
                            handle.getCallback().accept(messageNotification.message, messageNotification.sender);
                        }
                        catch (Exception exc) {
                            LOGGER.error("Exception when calling subscription callback while DELIVERY_EVENT", exc);
                        }
					} else {
						LOGGER.warn("Cannot deliver handleEvent: Module not enabled");
						return;
					}
				}
			}
		}
	}

	private void handlePeerStatechange(final PeerStatechangeEventNotification changeNotification) {
		// Notify module (if enabled)
		synchronized (selfModule.INTERNAL_getInternalMutex()) {
			if (selfModule.isEnabled()) {
                try {
                    selfModule.handleModuleStateChange(changeNotification.module, changeNotification.newState, changeNotification.lastState);
                }
                catch (Exception exc) {
                    LOGGER.error("Exception when calling module while DELIVERY_STATE", exc);
                }
			} else {
				LOGGER.warn("Cannot deliver handlePeerStatechange: Module not enabled");
				return;
			}
		}
	}
	
	/**
	 * Method to check if this module is alive or not
	 */
	private void checkAlive() {
		if (!alive.get()) {
			LOGGER.trace("checkAlive: Module not alive");
			throw new RuntimeException("JmfMessaging is not started or has been stopped");
		}
	}
}