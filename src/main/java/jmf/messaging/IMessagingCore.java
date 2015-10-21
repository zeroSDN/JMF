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

import jmf.data.Message;
import jmf.data.ModuleUniqueId;
import jmf.messaging.implementation.ExternalRequestIdentity;

/**
 * Counterpart of the IZmfMessagingService interface. The implementation of the IZmfMessagingService will deliver incoming events to the ZMF core via this interface.
 * Additionally this class provides access to the logger to be used by the IZmfMessagingService implementation.
 * Created on 8/2/15.
 * @author Tobias Korb
 * @author Jonas Grunert
 * @author Jan Strauß
 */
public interface IMessagingCore {

	void onSubMsgReceived(Message message, ModuleUniqueId sender);

	void onRequestMsgReceived(ExternalRequestIdentity id, Message message, ModuleUniqueId sender);
}
