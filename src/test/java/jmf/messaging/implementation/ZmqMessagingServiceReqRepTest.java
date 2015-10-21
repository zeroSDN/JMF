/*
 * Copyright 2015 ZSDN Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jmf.messaging.implementation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
public class ZmqMessagingServiceReqRepTest {
	
	private static final int MSG_COUNT = 100000;

	private int cnt = 0;

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
		final ZmqMessagingService serviceSender = new ZmqMessagingService();
		//final ZmqMessagingService serviceReceiver = new ZmqMessagingService();

		final ModuleHandleInternal senderSelfHandle = new ModuleHandleInternal(new ModuleUniqueId(UnsignedInteger.fromIntBits(41), UnsignedLong.fromLongBits(1337)), UnsignedInteger.fromIntBits(5), "ZmqMessagingServiceTest SENDER", true);
		//final ModuleHandleInternal receiverSelfHandle = new ModuleHandleInternal(new ModuleUniqueId(UnsignedInteger.fromIntBits(41), UnsignedLong.fromLongBits(1338)), UnsignedInteger.fromIntBits(5), "ZmqMessagingServiceTest RECEIVER", true);

		serviceSender.start(new DummyCore(serviceSender), senderSelfHandle, null);
		//serviceReceiver.start(new DummyCore(serviceReceiver), receiverSelfHandle);

		serviceSender.peerJoin(senderSelfHandle);
		//serviceSender.peerJoin(receiverSelfHandle);

		//serviceReceiver.peerJoin(senderSelfHandle);
		//serviceReceiver.peerJoin(receiverSelfHandle);

		//serviceSender.sendRequest(receiverSelfHandle.getUniqueId(), new Message("qq".getBytes(), "wweqeqwe".getBytes())).get();
		//serviceReceiver.sendRequest(senderSelfHandle.getUniqueId(), new Message("qq".getBytes(), "wweqeqwe".getBytes())).get();

		doPerfRun(senderSelfHandle.getUniqueId(), serviceSender);

		serviceSender.stop();
		//serviceReceiver.stop();
	}

	private void doPerfRun(ModuleUniqueId target, final ZmqMessagingService service) throws InterruptedException, ExecutionException {
		final Message message = new Message(new MessageType("kek".getBytes()), "wololo".getBytes());

		System.out.println("begin test");

		final long start = System.currentTimeMillis();

		final List<InReply> requests = new ArrayList<>(MSG_COUNT);
		for (int i = 0; i < MSG_COUNT; i++) {
			requests.add(service.sendRequest(target, message));
		}

		final long end_send = System.currentTimeMillis();

		while (true) {
			final long rcv = requests.stream().filter(InReply::isDone).count();
			System.out.println(cnt + " | " + rcv);
			Thread.sleep(1);
			if (rcv == MSG_COUNT) {
				break;
			}
		}

		final long end_recv = System.currentTimeMillis();

		final long dur_send = end_send - start;
		final long dur_recv = end_recv - start;
		System.out.println(dur_send + " | " + dur_recv + " " + (MSG_COUNT / dur_recv));
	}
	
}