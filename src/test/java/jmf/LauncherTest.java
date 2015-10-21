package jmf;

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
 * @author Jan Strauß
 */
public class LauncherTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(LauncherTest.class);

	private class Module extends AbstractModule {

		public Module() {
			super(new ModuleUniqueId(UnsignedInteger.fromIntBits(13), UnsignedLong.fromLongBits(233)), UnsignedInteger.fromIntBits(13), "KEK", null);
		}

		@Override
		public boolean enable() {
			LOGGER.info("enable");

			getFramework().subscribe(new MessageType("".getBytes()), (msg, sender) -> {
				System.out.println("received sub: " + new String(msg.getType().getMatch()) + " | " + new String(msg.getData()) + " | from: " + sender.toString());
			});

			return true;
		}

		@Override
		public void disable() {
			LOGGER.info("disable");
		}
		
		public void doPub() {
			getFramework().publish(new Message(new MessageType("kek".getBytes()), "wolo".getBytes()));
		}
		
		public void doReq() {
			try {
				getFramework().sendRequest(getUniqueId(), new Message(new MessageType("kek".getBytes()), "ayyy".getBytes())).get();
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
			while (!Thread.interrupted()) {
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
		Module module = new Module();

		IFrameworkController fc = Launcher.createInstance(module, true, true, false, false);

		Thread.sleep(1000);

		Assert.assertTrue(module.isEnabled());

		module.doPub();

		module.doReq();

		//module.doPubLoop();

		fc.requestStopInstance();
		fc.joinExecution();
	}
}