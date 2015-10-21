package jmf.discovery;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import jmf.data.ModuleHandleInternal;
import jmf.data.ModuleLifecycleState;
import jmf.data.ModuleUniqueId;
import jmf.discovery.implementation.PeerRegistry;

/**
 * TODO Describe
 * Created on 8/6/15.
 * @author Jonas Grunert
 */
public class PeerRegistryTest {

	//private final ConfigurationProviderImplementation subject;

	public PeerRegistryTest() {
		//this.subject = new ConfigurationProviderImplementation(Optional.of("src/test/resources/testConfig.config"));
	}

	ModuleUniqueId id10 = new ModuleUniqueId(UnsignedInteger.fromIntBits(1), UnsignedLong.fromLongBits(0));
	ModuleUniqueId id11 = new ModuleUniqueId(UnsignedInteger.fromIntBits(1), UnsignedLong.fromLongBits(1));
	ModuleUniqueId id12 = new ModuleUniqueId(UnsignedInteger.fromIntBits(1), UnsignedLong.fromLongBits(2));
	ModuleUniqueId id20 = new ModuleUniqueId(UnsignedInteger.fromIntBits(2), UnsignedLong.fromLongBits(0));
	ModuleUniqueId id21 = new ModuleUniqueId(UnsignedInteger.fromIntBits(2), UnsignedLong.fromLongBits(1));
	ModuleUniqueId id30 = new ModuleUniqueId(UnsignedInteger.fromIntBits(3), UnsignedLong.fromLongBits(0));

	void checkRegistryEmpty(PeerRegistry peerRegistry, boolean onlyActivePeers) {
		Assert.assertTrue(peerRegistry.INTERNAL_getAllPeers(onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.INTERNAL_getAllPeersByType(onlyActivePeers).size() == 0);
		// Contains no type of peers
		Assert.assertTrue(!peerRegistry.containsPeerWithType(UnsignedInteger.fromIntBits(1), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithType(UnsignedInteger.fromIntBits(2), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithType(UnsignedInteger.fromIntBits(3), onlyActivePeers));
		Assert.assertTrue(peerRegistry.getPeersWithType(UnsignedInteger.fromIntBits(1), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getPeersWithType(UnsignedInteger.fromIntBits(2), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getPeersWithType(UnsignedInteger.fromIntBits(3), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getAnyPeerWithType(UnsignedInteger.fromIntBits(1), onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithType(UnsignedInteger.fromIntBits(2), onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithType(UnsignedInteger.fromIntBits(2), onlyActivePeers) == null);
		// No types, not versions
		Assert.assertTrue(!peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(0), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(1), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(0), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(1), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(0), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(1), onlyActivePeers));
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(0), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(1), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(0), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(1), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(0), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(1), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(0), onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(1), onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(0), onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(1), onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(0), onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(1), onlyActivePeers) == null);
		// Contains no peers
		Assert.assertTrue(!peerRegistry.containsPeerWithId(id10, onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithId(id11, onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithId(id12, onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithId(id20, onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithId(id21, onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithId(id30, onlyActivePeers));
		Assert.assertTrue(peerRegistry.getPeerWithId(id10, onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id11, onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id12, onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id20, onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id21, onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id30, onlyActivePeers) == null);
	}

	void checkRegistry_10(PeerRegistry peerRegistry, boolean onlyActivePeers) {
		Assert.assertTrue(peerRegistry.INTERNAL_getAllPeers(onlyActivePeers).size() == 1);
		Assert.assertTrue(peerRegistry.INTERNAL_getAllPeersByType(onlyActivePeers).size() == 1);
		// Contains no type of peers
		Assert.assertTrue(peerRegistry.containsPeerWithType(UnsignedInteger.fromIntBits(1), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithType(UnsignedInteger.fromIntBits(2), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithType(UnsignedInteger.fromIntBits(3), onlyActivePeers));
		Assert.assertTrue(peerRegistry.getPeersWithType(UnsignedInteger.fromIntBits(1), onlyActivePeers).size() == 1);
		Assert.assertTrue(peerRegistry.getPeersWithType(UnsignedInteger.fromIntBits(2), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getPeersWithType(UnsignedInteger.fromIntBits(3), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getAnyPeerWithType(UnsignedInteger.fromIntBits(1), onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithType(UnsignedInteger.fromIntBits(2), onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithType(UnsignedInteger.fromIntBits(3), onlyActivePeers) == null);
		// No types, not versions
		Assert.assertTrue(peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(0), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(1), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(0), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(1), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(0), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(1), onlyActivePeers));
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(0), onlyActivePeers).size() == 1);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(1), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(0), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(1), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(0), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(1), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(0), onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(0), onlyActivePeers).getUniqueId().equals(id10));
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(1), onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(0), onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(1), onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(0), onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(1), onlyActivePeers) == null);
		// Contains no peers
		Assert.assertTrue(peerRegistry.containsPeerWithId(id10, onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithId(id11, onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithId(id12, onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithId(id20, onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithId(id21, onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithId(id30, onlyActivePeers));
		Assert.assertTrue(peerRegistry.getPeerWithId(id10, onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id10, onlyActivePeers).getUniqueId().equals(id10));
		Assert.assertTrue(peerRegistry.getPeerWithId(id11, onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id12, onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id20, onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id21, onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id30, onlyActivePeers) == null);
	}

	void checkRegistry_10_21(PeerRegistry peerRegistry, boolean onlyActivePeers) {
		Assert.assertTrue(peerRegistry.INTERNAL_getAllPeers(onlyActivePeers).size() == 2);
		Assert.assertTrue(peerRegistry.INTERNAL_getAllPeersByType(onlyActivePeers).size() == 2);
		// Contains no type of peers
		Assert.assertTrue(peerRegistry.containsPeerWithType(UnsignedInteger.fromIntBits(1), onlyActivePeers));
		Assert.assertTrue(peerRegistry.containsPeerWithType(UnsignedInteger.fromIntBits(2), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithType(UnsignedInteger.fromIntBits(3), onlyActivePeers));
		Assert.assertTrue(peerRegistry.getPeersWithType(UnsignedInteger.fromIntBits(1), onlyActivePeers).size() == 1);
		Assert.assertTrue(peerRegistry.getPeersWithType(UnsignedInteger.fromIntBits(2), onlyActivePeers).size() == 1);
		Assert.assertTrue(peerRegistry.getPeersWithType(UnsignedInteger.fromIntBits(3), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getAnyPeerWithType(UnsignedInteger.fromIntBits(1), onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithType(UnsignedInteger.fromIntBits(2), onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithType(UnsignedInteger.fromIntBits(3), onlyActivePeers) == null);
		// No types, not versions
		Assert.assertTrue(peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(0), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(1), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(0), onlyActivePeers));
		Assert.assertTrue(peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(1), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(0), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(1), onlyActivePeers));
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(0), onlyActivePeers).size() == 1);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(1), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(0), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(1), onlyActivePeers).size() == 1);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(0), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(1), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(0), onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(0), onlyActivePeers).getUniqueId().equals(id10));
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(1), onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(0), onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(1), onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(1), onlyActivePeers).getUniqueId().equals(id21));
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(0), onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(1), onlyActivePeers) == null);
		// Contains no peers
		Assert.assertTrue(peerRegistry.containsPeerWithId(id10, onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithId(id11, onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithId(id12, onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithId(id20, onlyActivePeers));
		Assert.assertTrue(peerRegistry.containsPeerWithId(id21, onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithId(id30, onlyActivePeers));
		Assert.assertTrue(peerRegistry.getPeerWithId(id10, onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id10, onlyActivePeers).getUniqueId().equals(id10));
		Assert.assertTrue(peerRegistry.getPeerWithId(id11, onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id12, onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id20, onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id21, onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id21, onlyActivePeers).getUniqueId().equals(id21));
		Assert.assertTrue(peerRegistry.getPeerWithId(id30, onlyActivePeers) == null);
	}

	void checkRegistry_10_11_12_21_22(PeerRegistry peerRegistry, boolean onlyActivePeers) {
		Assert.assertTrue(peerRegistry.INTERNAL_getAllPeers(onlyActivePeers).size() == 5);
		Assert.assertTrue(peerRegistry.INTERNAL_getAllPeersByType(onlyActivePeers).size() == 2);
		// Contains no type of peers
		Assert.assertTrue(peerRegistry.containsPeerWithType(UnsignedInteger.fromIntBits(1), onlyActivePeers));
		Assert.assertTrue(peerRegistry.containsPeerWithType(UnsignedInteger.fromIntBits(2), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithType(UnsignedInteger.fromIntBits(3), onlyActivePeers));
		Assert.assertTrue(peerRegistry.getPeersWithType(UnsignedInteger.fromIntBits(1), onlyActivePeers).size() == 3);
		Assert.assertTrue(peerRegistry.getPeersWithType(UnsignedInteger.fromIntBits(2), onlyActivePeers).size() == 2);
		Assert.assertTrue(peerRegistry.getPeersWithType(UnsignedInteger.fromIntBits(3), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getAnyPeerWithType(UnsignedInteger.fromIntBits(1), onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithType(UnsignedInteger.fromIntBits(2), onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithType(UnsignedInteger.fromIntBits(3), onlyActivePeers) == null);
		// No types, not versions
		Assert.assertTrue(peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(0), onlyActivePeers));
		Assert.assertTrue(peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(1), onlyActivePeers));
		Assert.assertTrue(peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(0), onlyActivePeers));
		Assert.assertTrue(peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(1), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(0), onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(1), onlyActivePeers));
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(0), onlyActivePeers).size() == 1);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(1), onlyActivePeers).size() == 2);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(0), onlyActivePeers).size() == 1);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(1), onlyActivePeers).size() == 1);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(0), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getPeersWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(1), onlyActivePeers).size() == 0);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(0), onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(0), onlyActivePeers).getUniqueId().equals(id10));
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(1), UnsignedInteger.fromIntBits(1), onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(0), onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(1), onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(2), UnsignedInteger.fromIntBits(1), onlyActivePeers).getUniqueId().equals(id21));
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(0), onlyActivePeers) == null);
		Assert.assertTrue(peerRegistry.getAnyPeerWithTypeVersion(UnsignedInteger.fromIntBits(3), UnsignedInteger.fromIntBits(1), onlyActivePeers) == null);
		// Contains no peers
		Assert.assertTrue(peerRegistry.containsPeerWithId(id10, onlyActivePeers));
		Assert.assertTrue(peerRegistry.containsPeerWithId(id11, onlyActivePeers));
		Assert.assertTrue(peerRegistry.containsPeerWithId(id12, onlyActivePeers));
		Assert.assertTrue(peerRegistry.containsPeerWithId(id20, onlyActivePeers));
		Assert.assertTrue(peerRegistry.containsPeerWithId(id21, onlyActivePeers));
		Assert.assertTrue(!peerRegistry.containsPeerWithId(id30, onlyActivePeers));
		Assert.assertTrue(peerRegistry.getPeerWithId(id10, onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id10, onlyActivePeers).getUniqueId().equals(id10));
		Assert.assertTrue(peerRegistry.getPeerWithId(id11, onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id12, onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id20, onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id21, onlyActivePeers) != null);
		Assert.assertTrue(peerRegistry.getPeerWithId(id21, onlyActivePeers).getUniqueId().equals(id21));
		Assert.assertTrue(peerRegistry.getPeerWithId(id30, onlyActivePeers) == null);
	}

	@Test
	public void testRegistry() {
		PeerRegistry peerRegistry = new PeerRegistry();

		// Creates adds and removes modules from peer registry

		ModuleHandleInternal testHandle10 = new ModuleHandleInternal(id10, UnsignedInteger.fromIntBits(0), "testHandle10", true);
		ModuleHandleInternal testHandle11 = new ModuleHandleInternal(id11, UnsignedInteger.fromIntBits(1), "testHandle11", true);
		ModuleHandleInternal testHandle12 = new ModuleHandleInternal(id12, UnsignedInteger.fromIntBits(1), "testHandle12", true);
		ModuleHandleInternal testHandle20 = new ModuleHandleInternal(id20, UnsignedInteger.fromIntBits(0), "testHandle20", true);
		ModuleHandleInternal testHandle21 = new ModuleHandleInternal(id21, UnsignedInteger.fromIntBits(1), "testHandle21", true);

		// Registry must be empty at the beginning
		checkRegistryEmpty(peerRegistry, true);
		checkRegistryEmpty(peerRegistry, false);

		// Add the module 1:0 which is inactive
		peerRegistry.addModule(testHandle10, ModuleLifecycleState.Inactive, new byte[0]);
		checkRegistryEmpty(peerRegistry, true);
		checkRegistry_10(peerRegistry, false);
		Assert.assertTrue(peerRegistry.getPeerState(testHandle10) == ModuleLifecycleState.Inactive);

		// Enable 1:0
		peerRegistry.INTERNAL_updatePeerState(testHandle10, ModuleLifecycleState.Active);
		checkRegistry_10(peerRegistry, true);
		checkRegistry_10(peerRegistry, false);
		Assert.assertTrue(peerRegistry.getPeerState(testHandle10) == ModuleLifecycleState.Active);

		// Disable 1:0
		peerRegistry.INTERNAL_updatePeerState(testHandle10, ModuleLifecycleState.Inactive);
		checkRegistryEmpty(peerRegistry, true);
		checkRegistry_10(peerRegistry, false);
		Assert.assertTrue(peerRegistry.getPeerState(testHandle10) == ModuleLifecycleState.Inactive);

		// Enable 1:0
		peerRegistry.INTERNAL_updatePeerState(testHandle10, ModuleLifecycleState.Active);
		checkRegistry_10(peerRegistry, true);
		checkRegistry_10(peerRegistry, false);
		Assert.assertTrue(peerRegistry.getPeerState(testHandle10) == ModuleLifecycleState.Active);


		// Add active module 2:1
		peerRegistry.addModule(testHandle21, ModuleLifecycleState.Active, new byte[0]);
		checkRegistry_10_21(peerRegistry, true);
		checkRegistry_10_21(peerRegistry, false);
		Assert.assertTrue(peerRegistry.getPeerState(testHandle10) == ModuleLifecycleState.Active);
		Assert.assertTrue(peerRegistry.getPeerState(testHandle21) == ModuleLifecycleState.Active);

		// Disable 2:1
		peerRegistry.INTERNAL_updatePeerState(testHandle21, ModuleLifecycleState.Inactive);
		checkRegistry_10(peerRegistry, true);
		checkRegistry_10_21(peerRegistry, false);
		Assert.assertTrue(peerRegistry.getPeerState(testHandle10) == ModuleLifecycleState.Active);
		Assert.assertTrue(peerRegistry.getPeerState(testHandle21) == ModuleLifecycleState.Inactive);

		// Enable module 2:1
		peerRegistry.INTERNAL_updatePeerState(testHandle21, ModuleLifecycleState.Active);
		checkRegistry_10_21(peerRegistry, true);
		checkRegistry_10_21(peerRegistry, false);
		Assert.assertTrue(peerRegistry.getPeerState(testHandle10) == ModuleLifecycleState.Active);
		Assert.assertTrue(peerRegistry.getPeerState(testHandle21) == ModuleLifecycleState.Active);

		// Remove the module 2:1
		peerRegistry.removeModule(id21);
		checkRegistry_10(peerRegistry, true);
		checkRegistry_10(peerRegistry, false);

		// Add again module 2:1
		peerRegistry.addModule(testHandle21, ModuleLifecycleState.Active, new byte[0]);
		checkRegistry_10_21(peerRegistry, true);
		checkRegistry_10_21(peerRegistry, false);


		// Add all except 30
		peerRegistry.addModule(testHandle11, ModuleLifecycleState.Active, new byte[0]);
		peerRegistry.addModule(testHandle12, ModuleLifecycleState.Active, new byte[0]);
		peerRegistry.addModule(testHandle20, ModuleLifecycleState.Active, new byte[0]);
		// Check
		checkRegistry_10_11_12_21_22(peerRegistry, true);
		checkRegistry_10_11_12_21_22(peerRegistry, false);


		// Clear registry
		peerRegistry.clearRegistry();
		// Registry must be empty after clear
		checkRegistryEmpty(peerRegistry, true);
		checkRegistryEmpty(peerRegistry, false);
	}
}