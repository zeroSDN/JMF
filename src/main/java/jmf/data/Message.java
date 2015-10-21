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

import java.util.Arrays;
import java.util.Objects;

/**
 * A message to send or received, a pair of message type and data
 * Created on 7/25/15.
 * @author Jan Strauße
 */
public class Message {

	private final MessageType type;
	private final byte[] data;

	public Message(final MessageType type, final byte[] data) {
		this.type = type;
		this.data = data;
	}

	public Message(final byte[] type, final byte[] data) {
		this.type = new MessageType(type);
		this.data = data;
	}

	public MessageType getType() {
		return type;
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Message message = (Message) o;
		return Objects.equals(type, message.type) && Arrays.equals(data, message.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, data);
	}
}
