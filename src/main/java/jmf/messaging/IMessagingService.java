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

package jmf.messaging;

import jmf.config.IConfigurationProvider;
import jmf.data.*;
import jmf.messaging.implementation.ExternalRequestIdentity;

/**
 * This class provides the interface definition for the ZMQ-module of ZMF. The IZmfMessagingCoreInterface class provides the logical counterpart.
 * The Implementation
 * Created on 8/2/15.
 * @author Tobias Korb
 * @author Jan Strauß
 */
public interface IMessagingService {

	boolean start(IMessagingCore core, ModuleHandleInternal selfHandle, IConfigurationProvider config);

	void stop();

	void peerJoin(ModuleHandleInternal module);

	void peerLeave(ModuleHandleInternal module);

	void subscribe(MessageType topic);

	void unsubscribe(MessageType topic);

	void publish(Message msg);

	InReply sendRequest(ModuleUniqueId target, Message msg);

	void sendReply(ExternalRequestIdentity id, Message msg);

	void cancelRequest(long requestID, boolean manual);

	void onDisable();

}
