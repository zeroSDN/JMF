package jmf.messaging.implementation;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;
import org.zeromq.ZMsg;

import jmf.config.IConfigurationProvider;
import jmf.data.*;
import jmf.messaging.IMessagingCore;
import jmf.messaging.IMessagingService;
import jmf.util.ByteUtils;
import jmf.util.NetworkUtil;
import jmf.util.Pair;

/**
 * Implementation of the IMessagingService using ZMQ.
 * Created on 8/2/15.
 * @author Jan Strauß
 */
public class ZmqMessagingService implements IMessagingService {

	private static final String NOTIFY_ADDRESS = "inproc://jmf_zmq_notify";
	/**
	 * address to bind the pub and rep sockets on
	 */
	private static final String ENDPOINT_STRING = "tcp://*";

	private static final byte MESSAGE_TYPE_REQUEST = 0;
	private static final byte MESSAGE_TYPE_REPLY = 1;
	private static final byte MESSAGE_TYPE_HELLO = 2;

	private static final Logger LOGGER_MAIN = LoggerFactory.getLogger(ZmqMessagingService.class.getName() + " MAIN");
	private static final Logger LOGGER_POLLER = LoggerFactory.getLogger(ZmqMessagingService.class.getName() + " LOOP");

	private static final String INTERFACE_CONFIG_KEY = "ZMF_NETWORK_INTERFACE_NAME";

	private enum NotifyType {
		SUBSCRIPTION_CHANGE,
		MEMBERSHIP_CHANCE,
		SHUTDOWN,
	}

	private IMessagingCore core;
	private ModuleHandleInternal selfHandle;
	private Thread pollerThread;

	private final AtomicBoolean alive = new AtomicBoolean(false);

	private ZMQ.Context context;

	private ZMQ.Socket socketPub;
	private ZMQ.Socket socketSub;
	private ZMQ.Socket socketRep;
	private ZMQ.Socket socketPush;
	private ZMQ.Socket socketPull;

	private final Object lockPubSocket = new Object();
	private final Object lockReqSockets = new Object();

	private ZMQ.Poller poller;

	private final ConcurrentLinkedQueue<Pair<MessageType, Boolean>> queueSubscriptionChanges = new ConcurrentLinkedQueue<>();
	private final ConcurrentLinkedQueue<Pair<String, Boolean>> queueMembershipChanges = new ConcurrentLinkedQueue<>();

	private final ConcurrentMap<Long, WeakReference<CompletableFuture<Message>>> outstandingRequests = new ConcurrentHashMap<>();
	private final ConcurrentMap<ExternalRequestIdentity, ModuleUniqueId> outstandingReplies = new ConcurrentHashMap<>();
	private final ConcurrentMap<ModuleUniqueId, ZMQ.Socket> socketsReq = new ConcurrentHashMap<>();

	private final AtomicLong nextRequestID = new AtomicLong(0);

	private IConfigurationProvider config;

    private long ZMF_ZMQ_ZMQ_RCVBUF = 0;
    private long ZMF_ZMQ_ZMQ_RCVHWM = 100000;
    private long ZMF_ZMQ_ZMQ_SNDBUF = 0;
    private long ZMF_ZMQ_ZMQ_SNDHWM = 100000;


	@Override
	public boolean start(final IMessagingCore core, final ModuleHandleInternal selfHandle, final IConfigurationProvider config) {
		if (alive.get()) {
			LOGGER_MAIN.warn("start called but alive flag already set, aborting start");
			return false;
		}

		this.core = core;
		this.selfHandle = selfHandle;
		this.config = config;


        OptionalLong cfgTmp;

        if(config != null && (cfgTmp = config.getAsLong("ZMF_ZMQ_ZMQ_RCVBUF")).isPresent()) {
            ZMF_ZMQ_ZMQ_RCVBUF = cfgTmp.getAsLong();
        }
        LOGGER_MAIN.debug("ZMF_ZMQ_ZMQ_RCVBUF configuration: " + ZMF_ZMQ_ZMQ_RCVBUF);

        if(config != null && (cfgTmp = config.getAsLong("ZMF_ZMQ_ZMQ_RCVHWM")).isPresent()) {
            ZMF_ZMQ_ZMQ_RCVHWM = cfgTmp.getAsLong();
        }
        LOGGER_MAIN.debug("ZMF_ZMQ_ZMQ_RCVHWM configuration: " + ZMF_ZMQ_ZMQ_RCVHWM);

        if(config != null && (cfgTmp = config.getAsLong("ZMF_ZMQ_ZMQ_SNDBUF")).isPresent()) {
            ZMF_ZMQ_ZMQ_SNDBUF = cfgTmp.getAsLong();
        }
        LOGGER_MAIN.debug("ZMF_ZMQ_ZMQ_SNDBUF configuration: " + ZMF_ZMQ_ZMQ_SNDBUF);

        if(config != null && (cfgTmp = config.getAsLong("ZMF_ZMQ_ZMQ_SNDHWM")).isPresent()) {
            ZMF_ZMQ_ZMQ_SNDHWM = cfgTmp.getAsLong();
        }
        LOGGER_MAIN.debug("ZMF_ZMQ_ZMQ_SNDHWM configuration: " + ZMF_ZMQ_ZMQ_SNDHWM);


		context = ZMQ.context(1);

		socketPub = context.socket(ZMQ.PUB);
		socketSub = context.socket(ZMQ.SUB);
		socketRep = context.socket(ZMQ.ROUTER);
		socketPush = context.socket(ZMQ.PUSH);
		socketPull = context.socket(ZMQ.PULL);

		socketPub.setSndHWM(ZMF_ZMQ_ZMQ_SNDHWM);
		socketRep.setRcvHWM(ZMF_ZMQ_ZMQ_RCVHWM);
		socketSub.setRcvHWM(ZMF_ZMQ_ZMQ_RCVHWM);

        socketPub.setSendBufferSize(ZMF_ZMQ_ZMQ_SNDBUF);
        socketRep.setReceiveBufferSize(ZMF_ZMQ_ZMQ_RCVBUF);
        socketSub.setReceiveBufferSize(ZMF_ZMQ_ZMQ_RCVBUF);

		socketPub.setLinger(0);
		socketSub.setLinger(0);
		socketRep.setLinger(0);
		socketPull.setLinger(0);
		socketPush.setLinger(0);


		socketPush.bind(NOTIFY_ADDRESS);
		socketPull.connect(NOTIFY_ADDRESS);

		try {
			final int portPub = socketPub.bindToRandomPort(ENDPOINT_STRING);
			selfHandle.selfSetPubPort(portPub);

			final int portRep = socketRep.bindToRandomPort(ENDPOINT_STRING);
			selfHandle.selfSetRepPort(portRep);

		} catch (final ZMQException e) {
			LOGGER_MAIN.error("failed to start ZMQ Messaging Service", e);
			closeSockets();
			context.term();
			return false;
		}

		alive.set(true);

		poller = new ZMQ.Poller(3);
		poller.register(socketPull, ZMQ.Poller.POLLIN);
		poller.register(socketSub, ZMQ.Poller.POLLIN);
		poller.register(socketRep, ZMQ.Poller.POLLIN);

		pollerThread = new Thread(this::pollerLoop, "ZMQMsgSrvc Poller");
		pollerThread.start();

		LOGGER_MAIN.info("started ZMQ Messaging Service:");
		LOGGER_MAIN.info("PUB: " + selfHandle.getZmqPubAddr());
		LOGGER_MAIN.info("REP: " + selfHandle.getZmqRepAddr());


		return true;
	}

	@Override
	public void stop() {
		if (!alive.compareAndSet(true, false)) {
			LOGGER_MAIN.warn("stop called but alive flag not set, aborting stop");
			return;
		}

		notifyPoller(NotifyType.SHUTDOWN);
		try {
			pollerThread.join();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}

		closeSockets();
		context.term();

		outstandingRequests.clear();
		outstandingReplies.clear();
		socketsReq.clear();

		queueMembershipChanges.clear();
		queueSubscriptionChanges.clear();

		LOGGER_MAIN.info("stopped ZMQ Messaging Service");

	}

	@Override
	public void peerJoin(final ModuleHandleInternal module) {
		checkAlive();

		internalConnect(module.getUniqueId(), module.getZmqRepAddr(), module.getZmqPubAddr());

		LOGGER_MAIN.trace("peer join: " + module.getUniqueId().toString());
	}

	private void internalConnect(final ModuleUniqueId identity, final String repAddr, final String pubAddr) {

		synchronized (lockReqSockets) {
			if (!socketsReq.containsKey(identity)) {
				final ZMQ.Socket socket = context.socket(ZMQ.DEALER);
				socket.setSndHWM(ZMF_ZMQ_ZMQ_SNDHWM);
                socket.setSendBufferSize(ZMF_ZMQ_ZMQ_SNDBUF);
				socket.setLinger(0);
				socket.connect(repAddr);

				String ip = getLocalIp();

				final String selfRepAddr = "tcp://" + ip + ":" + selfHandle.getSelfRepPort();
				final String selfPubAddr = "tcp://" + ip + ":" + selfHandle.getSelfPubPort();

				final ZMsg hello = new ZMsg();
				hello.add(new byte[]{MESSAGE_TYPE_HELLO});
				hello.add(selfHandle.getUniqueId().getSenderProtoBytes());
				hello.add(selfRepAddr);
				hello.add(selfPubAddr);
				hello.send(socket);

				socketsReq.put(identity, socket);

				queueMembershipChanges.add(Pair.of(pubAddr, true));
				notifyPoller(NotifyType.MEMBERSHIP_CHANCE);

			} else {
				LOGGER_MAIN.warn("tried to add peer already known: " + identity);
			}
		}
	}

	private String getLocalIp() {
		String ip = null;
		if (config != null) {
			final Optional<String> interfaceName = config.getAsString(INTERFACE_CONFIG_KEY);
			if (interfaceName.isPresent()) {
				ip = NetworkUtil.getLocalIp(interfaceName.get(), false);
			}
		}

		if (ip == null) {
			ip = NetworkUtil.getLocalIp(false);
		}
		return ip;
	}

	@Override
	public void peerLeave(final ModuleHandleInternal module) {
		checkAlive();

		final ModuleUniqueId key = module.getUniqueId();
		synchronized (lockReqSockets) {
			if (socketsReq.containsKey(key)) {
				socketsReq.remove(key).close();
			}
		}

		queueMembershipChanges.add(Pair.of(module.getZmqPubAddr(), false));
		notifyPoller(NotifyType.MEMBERSHIP_CHANCE);
	}

	@Override
	public void subscribe(final MessageType topic) {
		checkAlive();
		queueSubscriptionChanges.add(Pair.of(topic, true));
		notifyPoller(NotifyType.SUBSCRIPTION_CHANGE);
	}

	@Override
	public void unsubscribe(final MessageType topic) {
		checkAlive();
		queueSubscriptionChanges.add(Pair.of(topic, false));
		notifyPoller(NotifyType.SUBSCRIPTION_CHANGE);
	}

	@Override
	public void publish(final Message msg) {
		checkAlive();

		final ZMsg zMsg = new ZMsg();
		zMsg.add(msg.getType().getMatch());
		zMsg.add(selfHandle.getUniqueId().getSenderProtoBytes());
		zMsg.add(msg.getData());

		synchronized (lockPubSocket) {
			zMsg.send(socketPub);
		}
		LOGGER_MAIN.trace("send event");
	}

	@Override
	public InReply sendRequest(final ModuleUniqueId target, final Message msg) {
		checkAlive();

		final long id = nextRequestID.incrementAndGet();

		final CompletableFuture<Message> future = new CompletableFuture<>();
		final WeakReference<CompletableFuture<Message>> ref = new WeakReference<>(future);
		outstandingRequests.put(id, ref);

		final ZMsg request = new ZMsg();

		request.add(new byte[]{MESSAGE_TYPE_REQUEST});
		request.add(ByteUtils.convertLongToFrame(id));
		request.add(selfHandle.getUniqueId().getSenderProtoBytes());
		request.add(msg.getType().getMatch());
		request.add(msg.getData());

		synchronized (lockReqSockets) {
			if (socketsReq.containsKey(target)) {
				request.send(socketsReq.get(target));

			} else {
				future.completeExceptionally(new RuntimeException("target unknown: " + target));
				outstandingRequests.remove(id);
			}
		}

		return new InReply(id, future, this);
	}

	@Override
	public void sendReply(final ExternalRequestIdentity id, final Message msg) {
		checkAlive();

		final ModuleUniqueId addr;

		if (outstandingReplies.containsKey(id)) {
			addr = outstandingReplies.remove(id);
		} else {
			return;
		}

		final ZMsg reply = new ZMsg();

		reply.add(new byte[]{MESSAGE_TYPE_REPLY});
		reply.add(ByteUtils.convertLongToFrame(id.messageId));
		reply.add(msg.getType().getMatch());
		reply.add(msg.getData());

		synchronized (lockReqSockets) {
			if (socketsReq.containsKey(addr)) {
				reply.send(socketsReq.get(addr));

			} else {
				LOGGER_MAIN.error("unknown target: " + addr);
			}
		}
	}

	@Override
	public void cancelRequest(final long requestID, final boolean manual) {
		outstandingRequests.remove(requestID);

		if (manual) {
			LOGGER_MAIN.trace("canceled request with id=" + requestID);
		}
	}

	@Override
	public void onDisable() {
		checkAlive();
		outstandingRequests.clear();
		outstandingReplies.clear();
	}

	/**
	 * Poller thread method: apply membership changes from queue
	 */

	private void applyMembershipChange() {

		final Pair<String, Boolean> change = queueMembershipChanges.poll();
		if (change == null) {
			return;
		}

		try {
			if (change.second) {
				socketSub.connect(change.first);
			} else {
				socketSub.disconnect(change.first);
			}
		} catch (final ZMQException e) {
			LOGGER_POLLER.error("failed to apply membership change: ", e);
		}
		LOGGER_POLLER.trace("applied membership change");
	}

	/**
	 * Poller thread method: apply subscription changes from queue
	 */
	private void applySubscriptionChange() {
		final Pair<MessageType, Boolean> change = queueSubscriptionChanges.poll();
		if (change == null) {
			return;
		}

		try {
			if (change.second) {
				socketSub.subscribe(change.first.getMatch());
			} else {
				socketSub.unsubscribe(change.first.getMatch());
			}
		} catch (final ZMQException e) {
			LOGGER_POLLER.error("failed to apply subscription change: ", e);
			return;
		}
		LOGGER_POLLER.trace("applied sub change");
	}

	/**
	 * Poller thread method: handle input on sub socket
	 */
	private void handleSubIn() {
		while (true) {
			final ZMsg msg = ZMsg.recvMsg(socketSub, ZMQ.DONTWAIT);

			if (msg == null) {
				break;
			}

			final ZFrame[] frames = msg.toArray(new ZFrame[msg.size()]);


			final ModuleUniqueId moduleUniqueId = new ModuleUniqueId(frames[1].getData());
			final Message message = new Message(new MessageType(frames[0].getData()), frames[2].getData());

			core.onSubMsgReceived(message, moduleUniqueId);

		}
	}

	/**
	 * Poller thread method: handle input on notify socket
	 */
	private void handleNotify() {
		final byte[] note = socketPull.recv();
		final NotifyType notifyType = NotifyType.values()[note[0]];

		switch (notifyType) {
			case SUBSCRIPTION_CHANGE:
				applySubscriptionChange();
				break;
			case MEMBERSHIP_CHANCE:
				applyMembershipChange();
				break;
			case SHUTDOWN:
				LOGGER_POLLER.info("received shutdown");
				break;
		}
	}

	/**
	 * Poller thread method: handle input on rep socket
	 */
	private void handleRepIn() {
		while (true) {
			final ZMsg message = ZMsg.recvMsg(socketRep, ZMQ.DONTWAIT);

			if (message == null) {
				break;
			}

			final ZFrame[] frames = message.toArray(new ZFrame[message.size()]);

			final byte type = frames[1].getData()[0];

			switch (type) {
				case MESSAGE_TYPE_REQUEST:
					handleRequestReceived(frames);
					break;

				case MESSAGE_TYPE_REPLY:
					handleReplyReceived(frames);
					break;

				case MESSAGE_TYPE_HELLO:
					handleHelloReceived(frames);
					break;

				default:
					LOGGER_POLLER.warn("received unknown message type: " + type);
					break;
			}
		}
	}

	void handleRequestReceived(final ZFrame[] frames) {

		final ModuleUniqueId moduleUniqueId = new ModuleUniqueId(frames[3].getData());
		final long messageId = ByteUtils.convertFrameToLong(frames[2]);
		final Message message = new Message(new MessageType(frames[4].getData()), frames[5].getData());

		final ExternalRequestIdentity identity = new ExternalRequestIdentity(moduleUniqueId, messageId);

		outstandingReplies.put(identity, moduleUniqueId);

		core.onRequestMsgReceived(identity, message, moduleUniqueId);

	}

	void handleReplyReceived(final ZFrame[] frames) {
		final long id = ByteUtils.convertFrameToLong(frames[2]);

		final WeakReference<CompletableFuture<Message>> futureWeakReference = outstandingRequests.remove(id);
		
		if (futureWeakReference == null) {
			System.out.println("no future found for id " + id);
			return;
		}
		
		final CompletableFuture<Message> future = futureWeakReference.get();

		if (future == null) {
			System.out.println("weak ref is null " + id);
			return;
		}

		future.complete(new Message(new MessageType(frames[3].getData()), frames[4].getData()));
	}

	void handleHelloReceived(final ZFrame[] frames) {


		final ModuleUniqueId identity = new ModuleUniqueId(frames[2].getData());
		final String repAddr = frames[3].toString();
		final String pubAddr = frames[4].toString();

		internalConnect(identity, repAddr, pubAddr);

		LOGGER_POLLER.trace("handled hello msg");

	}

	/**
	 * util method to check if the alive flag is true
	 */
	private void checkAlive() {
		if (!alive.get()) {
			throw new RuntimeException("ZmqMessagingService is not started or has been stopped");
		}
	}

	/**
	 * Poller thread method: main loop for the poller thread
	 */
	private void pollerLoop() {
		LOGGER_POLLER.info("starting poller loop");

		while (alive.get()) {
			poller.poll();

			if (poller.pollin(0)) {
				handleNotify();
			} else if (poller.pollin(1)) {
				handleSubIn();
			} else if (poller.pollin(2)) {
				handleRepIn();
			}
		}

		LOGGER_POLLER.info("leaving poller loop");
	}

	/**
	 * util method to close all zmq sockets
	 */
	private void closeSockets() {
		socketPub.close();
		socketSub.close();
		socketRep.close();
		socketPush.close();
		socketPull.close();

		synchronized (lockReqSockets) {
			for (final ZMQ.Socket socket : socketsReq.values()) {
				socket.close();
			}
		}
	}

	/**
	 * util method to send a notify msg to the poller thread
	 */
	private void notifyPoller(final NotifyType type) {
		Objects.requireNonNull(type);

		final byte[] note = new byte[1];

		note[0] = (byte) type.ordinal();

		socketPush.send(note);
	}

	/**
	 * util/debug method to print a zmq message
	 */
	private void printMessage(final ZMsg msg) {
		System.out.println("=== MSG BEGIN");
		for (final ZFrame frame : msg) {
			System.out.println(frame.toString());
		}
		System.out.println("=== MSG END");
	}

}
