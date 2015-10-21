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

package jmf.data;

import java.util.function.BiConsumer;

import jmf.messaging.ISubscriptionHandler;

/**
 * Subscription handle representing a specific subscription.
 * Can be used to unsubscribe from a subscription
 * Created on 7/25/15.
 * Modified on 08/06/15
 * @author Tobias Korb
 * @author Jonas Grunert
 */
public class SubscriptionHandle {

	private final int subId;
	private final ISubscriptionHandler unsubscribeHandler;
	private final MessageType subTopic;
	private final BiConsumer<Message, ModuleUniqueId> callback;

	public SubscriptionHandle(final ISubscriptionHandler unsubscribeHandler, final MessageType topic, final BiConsumer<Message, ModuleUniqueId> callback) {
		this.subId = unsubscribeHandler.getSubId();
		this.unsubscribeHandler = unsubscribeHandler;
		this.subTopic = topic;
		this.callback = callback;
	}

    /**
     * Unsubscribe from this subscription
     * @throws Exception
     */
	public void unsubscribe() throws Exception {
		unsubscribeHandler.unsubscribe(this);
	}

    /**
     * @return Topic of this subscription
     */
	public MessageType getTopic() {
		return subTopic;
	}

    /**
     * @return Subscription handler callback
     */
	public BiConsumer<Message, ModuleUniqueId> getCallback() {
		return callback;
	}

    /**
     * @return ID of this subscription
     */
	public int getSubId() {
		return subId;
	}
}
