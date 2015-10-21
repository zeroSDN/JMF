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

package dummy;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import jmf.Launcher;
import jmf.data.Message;
import jmf.data.MessageType;
import jmf.data.ModuleUniqueId;
import jmf.data.OutReply;
import jmf.module.AbstractModule;
import jmf.module.IFrameworkController;

/**
 * Created on 9/8/15.
 *
 * @author Jan Strauß
 */
public class Dummy {

	private static final int SENDER = 222;
	private static final int RECEIVER = 223;

	private static final Logger LOGGER = LoggerFactory.getLogger(Dummy.class);

	private static class Module extends AbstractModule {

		private final AtomicInteger subCnt = new AtomicInteger(0);
		private final boolean doSub;

		public Module(final int type) {
			super(new ModuleUniqueId(UnsignedInteger.fromIntBits(type), UnsignedLong.fromLongBits(233)), UnsignedInteger.fromIntBits(13), "KEK", null);
			doSub = type == RECEIVER;
		}

		@Override
		public boolean enable() {
			LOGGER.info("enable");

			if (doSub) {
				getFramework().subscribe(new MessageType("".getBytes()), (msg, sender) -> {

					subCnt.incrementAndGet();

				});

			}


			return true;
		}

		@Override
		public void disable() {
			LOGGER.info("disable");
		}

		@Override
		public OutReply handleRequest(final Message message, final ModuleUniqueId sender) {
			return OutReply.createImmediateReply(message);
		}

		public void doPubLoop() {
			long start = System.currentTimeMillis();
			long cnt = 0;
			while (!Thread.interrupted()) {

				getFramework().publish(new Message("ayyyyyy".getBytes(), "LMAO".getBytes()));
				cnt++;
				long curr = System.currentTimeMillis();
				
				if (curr - start > 1000) {
					System.out.println("send: " + cnt);
					cnt = 0;
					start = curr;
				}
			}
		}
		
		public void doRecvLoop() {
			while (!Thread.interrupted()) {

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				System.out.println("received: " + subCnt.get());

			}
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("no dummy type given, either start as sender or receiver");
			return;
		}
		
		if ("receiver".equals(args[0])) {
			actAsReceiver();
		}

		if ("sender".equals(args[0])) {
			actAsSender();
		}
	}

	private static void actAsReceiver() throws InterruptedException {
		Module module = new Module(RECEIVER);

		IFrameworkController fc = Launcher.createInstance(module, true, true, false, false, false, Optional.<String>empty());

		while (!module.isEnabled()) {
			Thread.sleep(100);
		}

		module.doRecvLoop();

		fc.joinExecution();
	}

	private static void actAsSender() throws InterruptedException {
		Module module = new Module(SENDER);

		IFrameworkController fc = Launcher.createInstance(module, true, true, false, false, false, Optional.<String>empty());

		while (!module.isEnabled()) {
			Thread.sleep(100);
		}

		module.doPubLoop();

		fc.joinExecution();
	}
}
