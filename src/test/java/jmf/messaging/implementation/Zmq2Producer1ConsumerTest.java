package jmf.messaging.implementation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import jmf.data.InReply;
import jmf.data.Message;
import jmf.data.ModuleHandleInternal;
import jmf.data.ModuleUniqueId;
import jmf.messaging.IMessagingCore;
import jmf.messaging.IMessagingService;

/**
 * test zmq implementation with two producers and one consumer
 * Created on 8/2/15.
 * @author Jan Strauß
 */
public class Zmq2Producer1ConsumerTest {
	
	private AtomicBoolean alive = new AtomicBoolean(false);

	int cnt = 0;

	private class DummyCore implements IMessagingCore {
		private final IMessagingService service;
		
		public DummyCore(final IMessagingService service) {
			this.service = service;
		}
		
		@Override
		public void onSubMsgReceived(final Message message, final ModuleUniqueId sender) {
			//System.out.println("received sub on topic: " + message.getType());
		}
		
		@Override
		public void onRequestMsgReceived(final ExternalRequestIdentity id, final Message message, final ModuleUniqueId sender) {
			service.sendReply(id, message);
			cnt++;
		}
	}
	
	@Test
	public void testZmq() throws InterruptedException, ExecutionException, IOException {
		final ZmqMessagingService cnsmr = new ZmqMessagingService();
		final ZmqMessagingService prod1 = new ZmqMessagingService();
		final ZmqMessagingService prod2 = new ZmqMessagingService();
		
		final ModuleHandleInternal cnsmrHandle = new ModuleHandleInternal(new ModuleUniqueId(UnsignedInteger.fromIntBits(41), UnsignedLong.fromLongBits(1337)), UnsignedInteger.fromIntBits(5), "ZmqMessagingServiceTest CON", true);
		final ModuleHandleInternal prod1Handle = new ModuleHandleInternal(new ModuleUniqueId(UnsignedInteger.fromIntBits(41), UnsignedLong.fromLongBits(1338)), UnsignedInteger.fromIntBits(5), "ZmqMessagingServiceTest PRO1", true);
		final ModuleHandleInternal prod2Handle = new ModuleHandleInternal(new ModuleUniqueId(UnsignedInteger.fromIntBits(41), UnsignedLong.fromLongBits(1339)), UnsignedInteger.fromIntBits(5), "ZmqMessagingServiceTest PRO2", true);
		
		cnsmr.start(new DummyCore(cnsmr), cnsmrHandle, null);
		prod1.start(new DummyCore(prod1), prod1Handle, null);
		prod2.start(new DummyCore(prod2), prod2Handle, null);
		
		cnsmr.peerJoin(cnsmrHandle);
		cnsmr.peerJoin(prod1Handle);
		cnsmr.peerJoin(prod2Handle);

		prod1.peerJoin(cnsmrHandle);
		prod1.peerJoin(prod1Handle);
		prod1.peerJoin(prod2Handle);

		prod2.peerJoin(cnsmrHandle);
		prod2.peerJoin(prod1Handle);
		prod2.peerJoin(prod2Handle);

		Thread.sleep(1000);
		System.out.println("after startup");

		alive.set(true);

		Thread t1 = new Thread(() -> {
			testRun(prod1, cnsmrHandle, prod1Handle);
		}, "T1");
		Thread t2 = new Thread(() -> {
			testRun(prod2, cnsmrHandle, prod2Handle);
		}, "T2");

		t1.start();
		t2.start();

		Thread.sleep(5000);
		alive.set(false);
		System.out.println("after run");

		t1.join();
		t2.join();

		Thread.sleep(2000);
		System.out.println("after join");

		cnsmr.stop();
		prod1.stop();
		prod2.stop();
		System.out.println("after end");
	}

	private void testRun(final ZmqMessagingService prod, final ModuleHandleInternal cnsmrHandle, final ModuleHandleInternal prodHandle) {
		try {
			final List<InReply> replies = new ArrayList<>();
			while (alive.get()) {
				final InReply inReply = prod.sendRequest(cnsmrHandle.getUniqueId(), new Message("kek".getBytes(), "ayyy".getBytes()));
				//System.out.println(prodHandle.getName() + " send req");
				replies.add(inReply);
			}
			System.out.println(prodHandle.getName() + " done send loop");
			while (true) {
				final long rcv = replies.stream().filter(InReply::isDone).count();
				System.out.println(replies.size() + " | " + rcv + " | " + cnt);
				Thread.sleep(1000);
				if (rcv == replies.size()) {
					break;
				}
			}
			System.out.println(prodHandle.getName() + " done receive loop for " + replies.size());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}