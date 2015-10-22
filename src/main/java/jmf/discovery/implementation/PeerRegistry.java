package jmf.discovery.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.UnsignedInteger;

import jmf.data.ModuleHandle;
import jmf.data.ModuleLifecycleState;
import jmf.data.ModuleUniqueId;
import jmf.discovery.IPeerRegistry;

/**
 * The PeerRegistry keeps track of every module in the network. It tracks their state and
 * is able to provide information about all those modules.
 * Implements IPeerRegistry to offer access to peer data.
 * Created on 7/30/15.
 * Modified on 08/06/15
 * @author Jonas Grunert
 * @author Tobias Korb
 */
public class PeerRegistry implements IPeerRegistry {

	/** Lock on internal datastructure */
	public final Object peerDatastructuresLock = new Object();

	/// Map of all registered modules
	private ConcurrentMap<ModuleUniqueId, ModuleHandle> allPeers = new ConcurrentHashMap<>();
	/// Map of all registered modules, grouped by type
	private ConcurrentMap<Short, List<ModuleHandle>> allPeersByType = new ConcurrentHashMap<>();

	/// Map of all active registered modules
	private ConcurrentMap<ModuleUniqueId, ModuleHandle> allActivePeers = new ConcurrentHashMap<>();
	/// Map of all active registered modules, grouped by type
	private ConcurrentMap<Short, List<ModuleHandle>> allActivePeersByType = new ConcurrentHashMap<>();

	private ConcurrentMap<ModuleUniqueId, ModuleLifecycleState> peerStates = new ConcurrentHashMap<>();
	private ConcurrentMap<ModuleUniqueId, byte[]> peerAdditionalStates = new ConcurrentHashMap<>();

	private final static Logger logger = LoggerFactory.getLogger(PeerRegistry.class);

	/**
	 * Constructor
	 */
	public PeerRegistry() {
	}

	private void addModuleInternal(final ModuleHandle toAdd, final ConcurrentMap<ModuleUniqueId, ModuleHandle> peersMap, final ConcurrentMap<Short, List<ModuleHandle>> peersByTypeMap) {
		// Add to modules
		peersMap.put(toAdd.getUniqueId(), toAdd);

		// Add to modules by type
		if (peersByTypeMap.containsKey(toAdd.getUniqueId().getTypeId())) {
			// Add to existing set
			peersByTypeMap.get(toAdd.getUniqueId().getTypeId()).add(toAdd);
		} else {
			// Create new set
			final List<ModuleHandle> newTypeList = new ArrayList<>();
			newTypeList.add(toAdd);
			peersByTypeMap.put(toAdd.getUniqueId().getTypeId(), newTypeList);
		}
	}

	public synchronized void addModule(final ModuleHandle toAdd, ModuleLifecycleState state, byte[] additionalState) {
		synchronized (peerDatastructuresLock) {
			if (!allPeers.containsKey(toAdd.getUniqueId())) {
				// Add to modules
				addModuleInternal(toAdd, allPeers, allPeersByType);
				peerStates.put(toAdd.getUniqueId(), state);
				peerAdditionalStates.put(toAdd.getUniqueId(), additionalState);
				// Add to active modules if active
				if (state == ModuleLifecycleState.Active) {
					addModuleInternal(toAdd, allActivePeers, allActivePeersByType);
				}
			} else {
				// Module already in registry
				logger.error("Tried to add a module twice to the registry: " +
                        toAdd.getUniqueId().getTypeIdUnsigned().toString() + ":" +
                        toAdd.getUniqueId().getInstanceIdUnsigned().toString());
			}
		}
	}

	private void removeModuleInternal(final ModuleUniqueId toRemoveId, final ConcurrentMap<ModuleUniqueId, ModuleHandle> peersMap, final ConcurrentMap<Short, List<ModuleHandle>> peersByTypeMap) {
		if (peersMap.containsKey(toRemoveId)) {
			// Erase from allModules
			peersMap.remove(toRemoveId);

			// Erase from allModulesByType
			final List<ModuleHandle> typeList = peersByTypeMap.get(toRemoveId.getTypeId());
			if (typeList.size() == 1) {
				// If all assumptions are correct this module is the last module of this type
				peersByTypeMap.remove(toRemoveId.getTypeId());
			} else {
				// Remove module from type list
				int toRemoveIndex = 0;
				for (int i = 0; i < typeList.size(); i++) {
					if (typeList.get(i).getUniqueId().equals(toRemoveId)) {
						toRemoveIndex = i;
						break;
					}
				}
				typeList.remove(toRemoveIndex);
			}
		}
	}

	public synchronized void removeModule(final ModuleUniqueId toRemoveId) {
		synchronized (peerDatastructuresLock) {
			if (allPeers.containsKey(toRemoveId)) {
				// Remove from all data structures
				removeModuleInternal(toRemoveId, allPeers, allPeersByType);
				peerStates.remove(toRemoveId);
				peerAdditionalStates.remove(toRemoveId);
				removeModuleInternal(toRemoveId, allActivePeers, allActivePeersByType);
			} else {
				// Module not in registry
				logger.error("Tried to remove a module not in the registry: " + toRemoveId.getTypeIdUnsigned().toString() + ":" +
						toRemoveId.getInstanceIdUnsigned().toString());
			}
		}
	}

	public synchronized void clearRegistry() {
		synchronized (peerDatastructuresLock) {
			allPeers.clear();
			allActivePeers.clear();
			allPeersByType.clear();
			allActivePeersByType.clear();
            peerStates.clear();
            peerAdditionalStates.clear();
		}
	}

	/**
	 * Contract: Thread safe
	 * Performance: No data structures created, fast
	 *
	 * @param onlyActivePeers
	 * 		Checks only active peers
	 * @return True if there is a module with the given unique ID
	 */
	public synchronized boolean containsPeerWithId(final ModuleUniqueId id, final boolean onlyActivePeers) {
		final ConcurrentMap<ModuleUniqueId, ModuleHandle> allPeersTmp = onlyActivePeers ? allActivePeers : allPeers;
		return allPeersTmp.containsKey(id) && (!onlyActivePeers || peerStates.get(id) == ModuleLifecycleState.Active);
	}

	/**
	 * Contract: Thread safe and returns shared_ptr on module handle
	 * Performance: No data structures created, fast
	 *
	 * @param onlyActivePeers
	 * 		Returns only an active peer
	 * @return A shared_ptr(ModuleHandle) with the given ID or shared_ptr(nullptr) if there is none
	 */
	public synchronized ModuleHandle getPeerWithId(final ModuleUniqueId id, final boolean onlyActivePeers) {
		
		final ConcurrentMap<ModuleUniqueId, ModuleHandle> allPeersTmp = onlyActivePeers ? allActivePeers : allPeers;
		if (allPeersTmp.containsKey(id)) {
			final ModuleHandle peer = allPeersTmp.get(id);
			if (!onlyActivePeers || peerStates.get(id) == ModuleLifecycleState.Active) {
				return peer;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Contract: Thread safe and returns copied list, not concurrently modified later
	 * Performance: Copied list of all module handles, moderate
	 *
	 * @param onlyActivePeers
	 * 		Returns only all active peers
	 * @return A list of all modules with the given type, an empty list if there are none
	 */
	public synchronized List<ModuleHandle> getPeersWithType(final short type, final boolean onlyActivePeers) {

		final ConcurrentMap<Short, List<ModuleHandle>> allPeersTmp = onlyActivePeers ? allActivePeersByType : allPeersByType;
		if (allPeersTmp.containsKey(type)) {
			return new ArrayList<>(allPeersTmp.get(type));
		} else {
			return new ArrayList<>();
		}
	}

	/**
	 * Contract: Thread safe
	 * Performance: No new data structure created, fast
	 *
	 * @param onlyActivePeers
	 * 		Checks only all active peers
	 * @return True if there is at least one module of the given type in the registry
	 */
	public synchronized boolean containsPeerWithType(final short type, final boolean onlyActivePeers) {
		
		if (onlyActivePeers) {
			return allActivePeersByType.containsKey(type);
		} else {
			return allPeersByType.containsKey(type);
		}
	}

	/**
	 * Contract: Thread safe and returns copied list, not concurrently modified later
	 * Performance: Copied list of all module handles, moderate
	 *
	 * @param onlyActivePeers
	 * 		Returns only all active peers
	 * @return A list of all modules with the given type, an empty set if there are none
	 */
	public synchronized ModuleHandle getAnyPeerWithType(final short type, final boolean onlyActivePeers) {
		
		final ConcurrentMap<Short, List<ModuleHandle>> allPeersTmp = onlyActivePeers ? allActivePeersByType : allPeersByType;
		if (allPeersTmp.containsKey(type)) {
			final List<ModuleHandle> typePeers = allPeersTmp.get(type);
			for (final ModuleHandle peer : typePeers) {
				return peer;
			}
		}
		return null;
	}

	/**
	 * Contract: Thread safe and returns newly created list, not concurrently modified later
	 * Performance: New list of all module handle created, slow
	 *
	 * @param onlyActivePeers
	 * 		Returns only all active peers
	 * @return A list of all modules with the given type and version, an empty set if there are none
	 */
	public synchronized List<ModuleHandle> getPeersWithTypeVersion(final short type, final short version, final boolean onlyActivePeers) {
		
		final ConcurrentMap<Short, List<ModuleHandle>> allPeersTmp = onlyActivePeers ? allActivePeersByType : allPeersByType;
		if (allPeersTmp.containsKey(type)) {
			final List<ModuleHandle> typePeers = allPeersTmp.get(type);
			final List<ModuleHandle> typeVersionPeers = new ArrayList<>();
			for (final ModuleHandle peer : typePeers) {
				if (peer.getVersion() == version) {
					typeVersionPeers.add(peer);
				}
			}
			return typeVersionPeers;
		} else {
			return new ArrayList<>();
		}
	}

	/**
	 * Contract: Thread safe
	 * Performance: No new data structure created, fast
	 *
	 * @param onlyActivePeers
	 * 		only checks all active peers
	 * @return True if there is at least one module of the given type with the given version in the registry
	 */
	public synchronized boolean containsPeerWithTypeVersion(final short type, final short version, final boolean onlyActivePeers) {
		
		final ConcurrentMap<Short, List<ModuleHandle>> allPeersTmp = onlyActivePeers ? allActivePeersByType : allPeersByType;
		if (allPeersTmp.containsKey(type)) {
			final List<ModuleHandle> typePeers = allPeersTmp.get(type);
			for (final ModuleHandle peer : typePeers) {
				if (peer.getVersion() == version) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Contract: Thread safe and returns shared_ptr on module handle (copy)
	 * Performance: No new data structure created, fast
	 *
	 * @param onlyActivePeers
	 * 		only checks all active peers
	 * @return Returns one module if at least one module of the given type with the given version in the registry.
	 * Otherwise Null
	 */
	public synchronized ModuleHandle getAnyPeerWithTypeVersion(final short type, final short version, final boolean onlyActivePeers) {
		
		final ConcurrentMap<Short, List<ModuleHandle>> allPeersTmp = onlyActivePeers ? allActivePeersByType : allPeersByType;
		if (allPeersTmp.containsKey(type)) {
			final List<ModuleHandle> typePeers = allPeersTmp.get(type);
			for (final ModuleHandle peer : typePeers) {
				if (peer.getVersion() == version) {
					return peer;
				}
			}
		}
		return null;
	}

	public synchronized void INTERNAL_updatePeerState(ModuleHandle moduleHandle, ModuleLifecycleState state) {
		if (peerStates.containsKey(moduleHandle.getUniqueId())) {
			if (peerStates.get(moduleHandle.getUniqueId()) != state) {
				if (state == ModuleLifecycleState.Active) {
					// Module active now
					if (!allActivePeers.containsKey(moduleHandle.getUniqueId())) {
						addModuleInternal(allPeers.get(moduleHandle.getUniqueId()), allActivePeers, allActivePeersByType);
					}
				} else {
					// Module not active now
					if (allActivePeers.containsKey(moduleHandle.getUniqueId())) {
						removeModuleInternal(moduleHandle.getUniqueId(), allActivePeers, allActivePeersByType);
					}
				}
				peerStates.put(moduleHandle.getUniqueId(), state);
			}
		} else {
			// Module not in registry
			logger.error("Tried to update a module state not in the registry: " + moduleHandle.getUniqueId().toString() + ":" +
					moduleHandle.getUniqueId().toString());
		}
	}

	public synchronized void INTERNAL_updatePeerAdditionalState(ModuleHandle moduleUniqueId, byte[] additionalState) {
		if (peerAdditionalStates.containsKey(moduleUniqueId.getUniqueId())) {
			peerAdditionalStates.put(moduleUniqueId.getUniqueId(), additionalState);
		} else {
			// Module not in registry
			logger.error("Tried to update a additional module state not in the registry: " + moduleUniqueId.toString() + ":" +
					moduleUniqueId.toString());
		}
	}

	/**
     * Contract: Thread safe and returns value of current state
     * Performance: No new data structure created, fast
	 * @return Current state of a peer if peer in registry, ModuleLifecycleState.Dead otherwise
	 */
	public synchronized ModuleLifecycleState getPeerState(ModuleHandle peerHandle) {
		if (peerStates.containsKey(peerHandle.getUniqueId())) {
			return peerStates.get(peerHandle.getUniqueId());
		} else {
			return ModuleLifecycleState.Dead;
		}
	}

	/**
     * Contract: Thread safe and returns array of current additional state
     * (Array object will not be changed but internally replaced when new state received)
     * Performance: No new data structure created, fast
	 * @return Current additional state of a peer in registry, NULL otherwise
	 */
	public synchronized byte[] getPeerAdditionalState(ModuleHandle peerHandle) {
		if (peerAdditionalStates.containsKey(peerHandle.getUniqueId())) {
			return peerAdditionalStates.get(peerHandle.getUniqueId());
		} else {
			return null;
		}
	}

	/**
	 * Warning: No guarantee that data structure not concurrently modified
	 * Returns the internal data structure with all peers, sorted by ID
	 * Contract: No guarantee that data structure not changed concurrently. Copy recommended.
	 * Performance: Direct return of map, very fast
	 *
	 * @param onlyActivePeers
	 * 		Returns only all active peers
	 * @return Reference to map with all peers by Id
	 */
	public synchronized ConcurrentMap<ModuleUniqueId, ModuleHandle> INTERNAL_getAllPeers(final boolean onlyActivePeers) {
		if (onlyActivePeers) {
			return allActivePeers;

		} else {
			return allPeers;
		}
	}

	/**
	 * Warning: No guarantee that data structure not concurrently modified
	 * Returns the internal data structure with all peers, sorted by Type
	 * Contract: No guarantee that data structure not changed concurrently. Copy recommended.
	 * Performance: Direct return of map, very fast
	 *
	 * @param onlyActivePeers
	 * 		Returns only all active peers
	 * @return Reference to map with all peers by Tape
	 */
	public synchronized ConcurrentMap<Short, List<ModuleHandle>> INTERNAL_getAllPeersByType(final boolean onlyActivePeers) {
		if (onlyActivePeers) {
			return allActivePeersByType;

		} else {
			return allPeersByType;
		}
	}




    /**
     * Returns a copy of the internal data structure with all peers, sorted by ID
     * Contract: Threadsafe, copied data structure.
     * Performance: Copies map, slow
     *
     * @param onlyActivePeers
     * 		Returns only all active peers
     * @return Reference to map with all peers by Id
     */
    public synchronized ConcurrentMap<ModuleUniqueId, ModuleHandle> getAllPeersCopy(final boolean onlyActivePeers) {
        if (onlyActivePeers) {
            return new ConcurrentHashMap<>(allActivePeers);

        } else {
            return new ConcurrentHashMap<>(allPeers);
        }
    }

    /**
     * Returns a copy the internal data structure with all peers, sorted by Type
     * Contract: Threadsafe, copied data structure.
     * Performance: Copies map, slow
     *
     * @param onlyActivePeers
     * 		Returns only all active peers
     * @return Reference to map with all peers by Tape
     */
    public synchronized ConcurrentMap<Short, List<ModuleHandle>> getAllPeersByTypeCopy(final boolean onlyActivePeers) {
        if (onlyActivePeers) {
            return new ConcurrentHashMap<>(allActivePeersByType);

        } else {
            return new ConcurrentHashMap<>(allPeersByType);
        }
    }

	
	public synchronized int getAllPeerCount() {
		return allPeers.size();
	}
	
	public synchronized int getActivePeerCount() {
		return allActivePeers.size();
	}
}
