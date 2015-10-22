package jmf.discovery;

import jmf.data.ModuleHandleInternal;
import jmf.data.ModuleLifecycleState;

/**
 * Interface to access the PeerDiscoveryService -
 * Service class to recognize other modules and there states in the network.
 * PeerStates can be accessed via PeerRegistry (getPeerRegistry)
 * Created on 8/1/15.
 * @author Tobias Korb
 */
public interface IPeerDiscoveryService {

	boolean start(IPeerDiscoveryCore core, ModuleHandleInternal selfHandle, int broadcastFrequency, short udpPort, final boolean peerDiscoveryWait, final boolean disableEqualModuleInterconnect);

	void stop();

	void sendStateMulticast();

	void updateSelfState(ModuleLifecycleState state);

	void updateSelfAdditionalState(byte[] additionalState);

	IPeerRegistry getPeerRegistry();
}
