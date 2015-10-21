package jmf.messaging.implementation;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import jmf.data.*;
import jmf.messaging.IMessagingCore;
import jmf.messaging.IMessagingService;

/**
 * TODO Descrive
 * Created on 8/2/15.
 * @author Jan Strauß
 */
public class ZmqMessagingServiceTest {
	
	//private static final int MSG_COUNT = 1_000_000;
    private static final int MSG_COUNT = 1000;
	private int rcvCounter;
	
	private class DummyCore implements IMessagingCore {
		private final IMessagingService service;

		public DummyCore(final IMessagingService service) {
			this.service = service;
		}

		@Override
		public void onSubMsgReceived(final Message message, final ModuleUniqueId sender) {
			rcvCounter++;
			//System.out.println("received sub on topic: " + message.getType());
		}

		@Override
		public void onRequestMsgReceived(final ExternalRequestIdentity id, final Message message, final ModuleUniqueId sender) {
			service.sendReply(id, message);
		}
	}

	@Test
	public void testZmq() throws InterruptedException, ExecutionException, IOException {
		final ZmqMessagingService service = new ZmqMessagingService();

		final ModuleHandleInternal selfHandle = new ModuleHandleInternal(new ModuleUniqueId(UnsignedInteger.fromIntBits(41), UnsignedLong.fromLongBits(1337)), UnsignedInteger.fromIntBits(5), "ZmqMessagingServiceTest", true);
		service.start(new DummyCore(service), selfHandle, null);

		service.subscribe(new MessageType("kek".getBytes()));

		service.peerJoin(selfHandle);

		Thread.sleep(10000);

		doPerfRun(service);
		doPerfRun(service);
		doPerfRun(service);
		doPerfRun(service);
		doPerfRun(service);
		doPerfRun(service);

		final InReply q = service.sendRequest(selfHandle.getUniqueId(), new Message(new MessageType("ayy".getBytes()), "lmao".getBytes()));

		System.out.println(Arrays.toString(q.get().getData()));

		service.stop();
	}

	private void doPerfRun(final ZmqMessagingService service) throws InterruptedException, ExecutionException {
		final Message message = new Message(new MessageType("kek".getBytes()), "wololo".getBytes());
		rcvCounter = 0;

		final long start = System.currentTimeMillis();

		for (int i = 0; i < MSG_COUNT; i++) {
			service.publish(message);
		}

		final long end_send = System.currentTimeMillis();

		while (rcvCounter < MSG_COUNT) {
			Thread.sleep(1);
		}

		final long end_recv = System.currentTimeMillis();

		final long dur_send = end_send - start;
		final long dur_recv = end_recv - start;
		System.out.println(dur_send + " | " + dur_recv + " " + (MSG_COUNT / dur_recv));
	}
	
}