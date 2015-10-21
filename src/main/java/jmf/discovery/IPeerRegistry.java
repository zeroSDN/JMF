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

package jmf.discovery;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.google.common.primitives.UnsignedInteger;
import jmf.data.ModuleHandle;
import jmf.data.ModuleLifecycleState;
import jmf.data.ModuleUniqueId;

/**
 * Data structure for the PeerRegistry which represents all known other peers in the system.
 * Base class for PeerRegistryInternal.
 * Created on 8/1/15.
 * @author Tobias Korb
 * @author Jonas Grunert
 */
public interface IPeerRegistry {

	/**
	 * Contract: Thread safe
	 * Performance: No data structures created, fast
	 * @param onlyActivePeers Checks only active peers
	 * @return True if there is a module with the given unique ID
	 */
	boolean containsPeerWithId(ModuleUniqueId id, boolean onlyActivePeers);


	/**
	 * Contract: Thread safe and returns shared_ptr on module handle
	 * Performance: No data structures created, fast
	 * @param onlyActivePeers Returns only an active peer
	 * @return A shared_ptr(ModuleHandle) with the given ID or shared_ptr(nullptr) if there is none
	 */
	ModuleHandle getPeerWithId(ModuleUniqueId id,
												   boolean onlyActivePeers);


	/**
	 * Contract: Thread safe and returns copied list, not concurrently modified later
	 * Performance: Copied list of all module handles, moderate
	 * @param onlyActivePeers Returns only all active peers
	 * @return A list of all modules with the given type, an empty list if there are none
	 */
	List<ModuleHandle> getPeersWithType(UnsignedInteger type,
															boolean onlyActivePeers);

	/**
	 * Contract: Thread safe
	 * Performance: No new data structure created, fast
	 * @param onlyActivePeers Checks only all active peers
	 * @return True if there is at least one module of the given type in the registry
	 */
	boolean containsPeerWithType(UnsignedInteger type, boolean onlyActivePeers);

	/**
	 * Contract: Thread safe and returns copied list, not concurrently modified later
	 * Performance: Copied list of all module handles, moderate
	 * @param onlyActivePeers Returns only all active peers
	 * @return A list of all modules with the given type, an empty set if there are none
	 */
	ModuleHandle getAnyPeerWithType(UnsignedInteger type,
														boolean onlyActivePeers);


	/**
	 * Contract: Thread safe and returns newly created list, not concurrently modified later
	 * Performance: New list of all module handle created, slow
	 * @param onlyActivePeers Returns only all active peers
	 * @return A list of all modules with the given type and version, an empty set if there are none
	 */
	List<ModuleHandle> getPeersWithTypeVersion(UnsignedInteger type,
																   UnsignedInteger version,
																   boolean onlyActivePeers);

	/**
	 * Contract: Thread safe
	 * Performance: No new data structure created, fast
	 * @param onlyActivePeers only checks all active peers
	 * @return True if there is at least one module of the given type with the given version in the registry
	 */
	boolean containsPeerWithTypeVersion(UnsignedInteger type, UnsignedInteger version, boolean onlyActivePeers);

	/**
	 * Contract: Thread safe and returns shared_ptr on module handle (copy)
	 * Performance: No new data structure created, fast
	 * @param onlyActivePeers only checks all active peers
	 * @return Returns one module if at least one module of the given type with the given version in the registry.
	 * Otherwise Null
	 */
	ModuleHandle getAnyPeerWithTypeVersion(UnsignedInteger type, UnsignedInteger version,
															   boolean onlyActivePeers);


    /**
     * @return Current state of a peer if peer in registry, ModuleLifecycleState.Dead otherwise
     */
    ModuleLifecycleState getPeerState(ModuleHandle peerHandle);

    /**
     * @return Current additional state of a peer in registry, NULL otherwise
     */
    byte[] getPeerAdditionalState(ModuleHandle peerHandle);




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
    ConcurrentMap<Short, List<ModuleHandle>> INTERNAL_getAllPeersByType(final boolean onlyActivePeers);




    /**
     * Returns a copy of the internal data structure with all peers, sorted by ID
     * Contract: Threadsafe, copied data structure.
     * Performance: Copies map, slow
     *
     * @param onlyActivePeers
     * 		Returns only all active peers
     * @return Reference to map with all peers by Id
     */
    ConcurrentMap<ModuleUniqueId, ModuleHandle> getAllPeersCopy(final boolean onlyActivePeers);
}