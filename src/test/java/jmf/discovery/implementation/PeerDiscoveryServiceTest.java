package jmf.discovery.implementation;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import jmf.data.ModuleHandleInternal;
import jmf.data.ModuleLifecycleState;
import jmf.data.ModuleUniqueId;
import jmf.discovery.IPeerDiscoveryCore;
import jmf.discovery.IPeerDiscoveryService;

/**
 * @author Matthias Blohm
 * @brief Unit test to check the functionality of the PeerDiscoveryService-class in the Java-version of the ZMF
 * @details This short unit test starts some JMF-instances with their PeerDiscoveryServices and checks if they recognize each other correctly.
 * Also the detection of state changes to 'Inactive' or 'Dead' and the correct retrieval of name, version and additional state is tested.
 * More detailed tests concerning the correct transfer of information between instances can be found in the classes of the interoperability-test between Java and C++.
 * Warning:  there must be no other modules running in the network at this time, otherwise the test will fail.
 * @date created on 8/9/15.
 */
public class PeerDiscoveryServiceTest {
	/**
	 * @author Matthias Blohm
	 * @brief Dummy-core only for purpose of testing PeerDiscoveryService in JMF
	 * @details This class simulates a normal JMF core but has only the functions implemented that are necessary fot testing the PeerDiscoveyService
	 * @date 8/9/15.
	 */
	private class DummyCore implements IPeerDiscoveryCore {
		/// PeerDiscoveryService that is used by the DummyCore
		private final IPeerDiscoveryService service;

		/**
		 * Constructor fills the DummyCore with the given PeerDiscoveryService
		 *
		 * @param service
		 * 		The instance of the PeerDiscoveryService that is used by the DummyCore
		 */
		public DummyCore(final IPeerDiscoveryService service) {
			this.service = service;
		}

		/**
		 * Method needs to be implemented but is empty because it is not needed for this test
		 *
		 * @param module
		 * 		The moduleHandle of the module that has changed its stare
		 * @param newState
		 * 		The new lifecycle state the module has now
		 * @param lastState
		 * 		The old lifecycle state the module had before
		 */
		@Override
		public void peerStateChange(final ModuleHandleInternal module, final ModuleLifecycleState newState, final ModuleLifecycleState lastState) {

		}
	}

	/**
	 * Start the test of the JMF-PeerDiscoveryService
	 *
	 * @throws InterruptedException
	 * 		if thread sleeps are interrupted
	 */
	@Test
	public void testDiscovery() throws InterruptedException {


		// first create services and modules
		final PeerDiscoveryService service = new PeerDiscoveryService();
		final PeerDiscoveryService service2 = new PeerDiscoveryService();

		final ModuleUniqueId id1 = new ModuleUniqueId(UnsignedInteger.fromIntBits(42), UnsignedLong.fromLongBits(1310));
		final ModuleUniqueId id2 = new ModuleUniqueId(UnsignedInteger.fromIntBits(43), UnsignedLong.fromLongBits(1412));


		final ModuleHandleInternal selfHandle = new ModuleHandleInternal(id1, UnsignedInteger.fromIntBits(5), "DiscoveryServiceTest 1", true);
		final ModuleHandleInternal selfHandle2 = new ModuleHandleInternal(id2, UnsignedInteger.fromIntBits(5), "DiscoveryServiceTest 2", true);


		// create additional states
		byte[] add = new byte[3];
		add[0] = 'a';
		add[1] = 'b';
		add[2] = 'c';

		byte[] add2 = new byte[3];
		add[0] = 'd';
		add[1] = 'e';
		add[2] = 'f';


		int broadcastFrequency = 1000;
		short udpPort = 4213;

		// start the services
		service.start(new DummyCore(service), selfHandle, broadcastFrequency, udpPort, true, false);
		service2.start(new DummyCore(service2), selfHandle2, broadcastFrequency, udpPort, true, false);

		// set the state of the modules to active manually (dummy core cannot do this)
		service.updateSelfState(ModuleLifecycleState.Active);
		service2.updateSelfState(ModuleLifecycleState.Active);

		// set additional state of service 1
		service.updateSelfAdditionalState(add);


		Thread.sleep(3000);

		// check if the modules recognized each other
		PeerRegistry reg1 = (PeerRegistry) service.getPeerRegistry();
		PeerRegistry reg2 = (PeerRegistry) service2.getPeerRegistry();

		Assert.assertTrue(reg1.INTERNAL_getAllPeers(true).size() == 1);
		Assert.assertTrue(reg2.INTERNAL_getAllPeers(true).size() == 1);

		Assert.assertTrue(reg1.containsPeerWithId(id2, true));
		Assert.assertTrue(reg2.containsPeerWithId(id1, true));

		byte[] emptyArray = new byte[0];

		// make sure one instance has already additional state, the other one not
		Assert.assertTrue(Arrays.equals(reg2.getPeerAdditionalState(reg2.getPeerWithId(id1, true)), add));
		Assert.assertTrue(Arrays.equals(reg1.getPeerAdditionalState(reg1.getPeerWithId(id2, true)), emptyArray));

		// set the second additional state
		service2.updateSelfAdditionalState(add2);


		//Deactivate one Module
		service.updateSelfState(ModuleLifecycleState.Inactive);
		Thread.sleep(2000);


		// check if the reactions are right
		Assert.assertTrue(reg1.INTERNAL_getAllPeers(true).size() == 1);
		Assert.assertTrue(reg2.INTERNAL_getAllPeers(true).size() == 0);
		Assert.assertTrue(reg2.INTERNAL_getAllPeers(false).size() == 1);

		Assert.assertTrue(reg1.containsPeerWithId(id2, true));
		Assert.assertTrue(reg2.containsPeerWithId(id1, false));
		Assert.assertTrue(!reg2.containsPeerWithId(id1, true));

		// check the second additional state
		Assert.assertTrue(Arrays.equals(reg1.getPeerAdditionalState(reg1.getPeerWithId(id2, true)), add2));

		//reactivate Module
		service.updateSelfState(ModuleLifecycleState.Active);

		Thread.sleep(2000);

		// check if reactivation was recognized
		Assert.assertTrue(reg1.INTERNAL_getAllPeers(true).size() == 1);
		Assert.assertTrue(reg2.INTERNAL_getAllPeers(true).size() == 1);

		Assert.assertTrue(reg1.containsPeerWithId(id2, true));
		Assert.assertTrue(reg2.containsPeerWithId(id1, true));


		// now set one module to dead
		service.updateSelfState(ModuleLifecycleState.Dead);

		Thread.sleep(2000);

		Assert.assertTrue(reg1.INTERNAL_getAllPeers(true).size() == 1);
		Assert.assertTrue(reg2.INTERNAL_getAllPeers(true).size() == 0);

		Assert.assertTrue(reg1.containsPeerWithId(id2, true));


		// stop service1

		service.stop();

		// start again
		service.start(new DummyCore(service), selfHandle, broadcastFrequency, udpPort, false, false);

		// set to inactive
		service.updateSelfState(ModuleLifecycleState.Inactive);

		Thread.sleep(2000);

		// check reactions
		Assert.assertTrue(reg1.INTERNAL_getAllPeers(true).size() == 1);
		Assert.assertTrue(reg2.INTERNAL_getAllPeers(false).size() == 1);

		Assert.assertTrue(reg1.containsPeerWithId(id2, true));
		Assert.assertTrue(!reg2.containsPeerWithId(id1, true));
		Assert.assertTrue(reg2.containsPeerWithId(id1, false));

		// set to active again
		service.updateSelfState(ModuleLifecycleState.Active);
		Thread.sleep(2000);

		// simulate Timeout
		service2.stop();

		Thread.sleep(5000);

		// make sure the dead module was removed from reigstry
		Assert.assertTrue(reg1.INTERNAL_getAllPeers(true).size() == 0);

	}

}