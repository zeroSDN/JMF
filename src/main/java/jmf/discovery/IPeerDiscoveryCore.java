package jmf.discovery;

import jmf.data.ModuleHandleInternal;
import jmf.data.ModuleLifecycleState;

/**
 * Interface to ZmfCore for peerStateChange events from peer discovery
 * Created on 8/1/15.
 * @author Tobias Korb
 */
public interface IPeerDiscoveryCore {
	void peerStateChange(final ModuleHandleInternal module, final ModuleLifecycleState newState,
						 final ModuleLifecycleState lastState);
}
