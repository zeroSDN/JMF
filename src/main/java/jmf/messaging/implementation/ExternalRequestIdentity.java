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

package jmf.messaging.implementation;

import java.util.Objects;

import jmf.data.ModuleUniqueId;

/**
 * Data structure representing ID of an externally requesting peer
 * Created on 8/2/15.
 * @author Jan Strauß
 */
public class ExternalRequestIdentity {

	public final ModuleUniqueId senderId;
	public final long messageId;

	public ExternalRequestIdentity(final ModuleUniqueId senderId, final long messageId) {
		this.senderId = senderId;
		this.messageId = messageId;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ExternalRequestIdentity that = (ExternalRequestIdentity) o;
		return Objects.equals(messageId, that.messageId) && Objects.equals(senderId, that.senderId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(senderId, messageId);
	}
}
