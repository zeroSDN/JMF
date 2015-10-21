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

import jmf.data.SubscriptionHandle;

/**
 * Interface offering possibility to unsubscribe from a subscription
 * Created on 8/2/15.
 * @author Tobias Korb
 * @author Jan Strauß
 */
public interface ISubscriptionHandler {
    /**
     * Unsubscribes from the given handle
     * @param handle Handle to unsubscribe
     * @throws Exception Exception if fails
     */
	void unsubscribe(SubscriptionHandle handle) throws Exception;

    /**
     * @return ID of the corresponding subscription
     */
	int getSubId();
}
