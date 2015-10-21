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

package jmf;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import jmf.data.*;
import jmf.module.AbstractModule;
import jmf.module.IFrameworkController;

/**
 * TODO Descrive
 * Created on 8/5/15
 *
 * @author Jan Strauß
 */
public class LauncherDuoTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(LauncherDuoTest.class);

	private class Module extends AbstractModule {

		private final boolean doReq;
		private final ModuleUniqueId target;

		public Module(final int typeId, final int instanceId, final Collection<ModuleDependency> deps, final boolean doReq, final ModuleUniqueId target) {
			super(new ModuleUniqueId(UnsignedInteger.fromIntBits(typeId), UnsignedLong.fromLongBits(instanceId)), UnsignedInteger.fromIntBits(1), "KEK", deps);
			this.doReq = doReq;
			this.target = target;
		}

		@Override
		public boolean enable() {
			LOGGER.info("enable");

			getFramework().subscribe(new MessageType("".getBytes()), (msg, sender) -> {
				System.out.println("received sub: " + new String(msg.getType().getMatch()) + " | " + new String(msg.getData()) + " | from: " + sender.toString());
			});

			if (doReq) {
				doReq(target);
			}

			return true;
		}

		@Override
		public void disable() {
			LOGGER.info("disable");
		}
		
		public void doPub() {
			getFramework().publish(new Message(new MessageType("kek".getBytes()), "wolo".getBytes()));
		}
		
		public void doReq(final ModuleUniqueId target) {
			try {
				getFramework().sendRequest(target, new Message(new MessageType("kek".getBytes()), "ayyy".getBytes())).get();
				LOGGER.info("request-reply done");
			} catch (ExecutionException | InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		public OutReply handleRequest(final Message message, final ModuleUniqueId sender) {
			return OutReply.createImmediateReply(message);
		}
		
		public void doPubLoop() {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}

				getFramework().publish(new Message("ayyyyyy".getBytes(), "LMAO".getBytes()));

				final ModuleHandle handle = getFramework().getPeerRegistry().getAnyPeerWithType(UnsignedInteger.valueOf(45), true);

				if (handle != null) {
					try {
						final Message request = new Message("wasd".getBytes(), "qqq".getBytes());
						final Message reply = getFramework().sendRequest(handle.getUniqueId(), request).get();
						System.out.println("received reply! " + request.equals(reply));
					} catch (final ExecutionException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	@Test
	public void testCreateInstance() throws Exception {
		final Module moduleA = new Module(13, 1336, null, false, null);
		final Module moduleB = new Module(12, 1, Arrays.asList(new ModuleDependency(UnsignedInteger.valueOf(13), UnsignedInteger.valueOf(1))), true, moduleA.getUniqueId());

		final IFrameworkController fcA = Launcher.createInstance(moduleA, true, true, false, false, false, Optional.<String>empty());
		final IFrameworkController fcB = Launcher.createInstance(moduleB, true, true, false, false, false, Optional.<String>empty());

		Thread.sleep(2000);

		Assert.assertTrue(moduleA.isEnabled());
		Assert.assertTrue(moduleB.isEnabled());

		moduleA.doPub();
		moduleB.doPub();

		moduleA.doReq(moduleB.getUniqueId());
		moduleB.doReq(moduleA.getUniqueId());

		Thread.sleep(1000);

		fcA.requestStopInstance();
		fcB.requestStopInstance();

		fcA.joinExecution();
		fcB.joinExecution();
	}
}