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
