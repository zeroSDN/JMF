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
 * Interface to ZmfCore for peerStateChange events from peer discovery
 * Created on 8/1/15.
 * @author Tobias Korb
 */
public interface IPeerDiscoveryCore {
	void peerStateChange(final ModuleHandleInternal module, final ModuleLifecycleState newState,
						 final ModuleLifecycleState lastState);
}
