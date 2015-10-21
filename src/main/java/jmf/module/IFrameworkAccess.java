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

package jmf.module;

import java.util.function.BiConsumer;

import jmf.config.IConfigurationProvider;
import jmf.data.*;
import jmf.discovery.IPeerRegistry;

/**
 * Interfase to access a ZMF instance from a module
 * Created on 7/25/15.
 * @author Tobias Korb
 * @author Jonas Grunert
 * @author Jan Strauß
 */
public interface IFrameworkAccess {

    /**
     * Returns the configuration provider interface which allows to access configuration values
     * @return Interface to configuration provider
     */
	IConfigurationProvider getConfigurationProvider();

    /**
     * @return The public interface of the PeerRegistry which offers information about all known peers
     */
	IPeerRegistry getPeerRegistry();


    /**
     * Sends a request to a given node and returns a future_ to await the response.
     */
	InReply sendRequest(ModuleUniqueId target, Message msg);

    /**
     * Starts a subscription to the given topic. Received subscription events will be sent to the given handler callback.
     */
	SubscriptionHandle subscribe(MessageType topic, BiConsumer<Message, ModuleUniqueId> handler);

    /**
     * Publishes an event to the messaging message bus.
     */
	void publish(Message msg);


    /**
     * Called when the additional state of a module was changed.
     * Will not trigger automatically trigger a state broadcast.
     */
	void onModuleAdditionalStateChanged(byte[] additionalState);

    /**
     * Triggers a state broadcast for the module of this instance.
     */
	void forceStateBroadcast();


    /**
     * Requests to initiate disabling of the module managed by this ZMF core
     */
	void requestDisableModule();

    /**
     * Requests to initiate stopping this ZMF core and its module
     */
	void requestStopInstance();


    /**
     * Requests the remote instance to be enabled. Will wait for the given timeout (ms) and return true only if a reply was
     * received for the request and if the received reply was positive
     */
	boolean requestEnableRemoteInstance(ModuleUniqueId id, long timeout);

    /**
     * Requests the remote instance to be disabled. Will wait for the given timeout (ms) and return true only if a reply was
     * received for the request and if the received reply was positive
     */
	boolean requestDisableRemoteInstance(ModuleUniqueId id, long timeout);

    /**
     * Requests the remote instance to stop. Will wait for the given timeout (ms) and return true only if a reply was
     * received for the request and if the received reply was positive
     */
	boolean requestStopRemoteInstance(ModuleUniqueId id, long timeout);
}
