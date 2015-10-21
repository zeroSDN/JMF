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
import java.util.StringJoiner;

/**
 * Data structure discribing the type of a message.
 * Consists of a string of match up to 32 bytes describing the message.
 * Is used for message filtering in ZMQ.
 * Can be bulded with the MessageTypeBuilder.
 * created on 7/25/15.
 * @author Tobias Korb
 * @author Jan Strauß
 * @author Andre Kutzleb
 */
public class MessageType {

	public static final byte MESSAGE_ID_BYTES = 32;

	private final byte[] match;

	/**
	 * Constructs a MessageType from a byte[] match.
	 *
	 * @param match
	 * 		the match to construct this MessageType from
	 * @see jmf.data.builder.MessageTypeBuilder
	 */
	public MessageType(final byte[] match) {
		if (match.length > MESSAGE_ID_BYTES) {
			throw new IllegalArgumentException("given length (" + match.length + ") exceeds max allowed length of " + MESSAGE_ID_BYTES + " bytes");
		}
		this.match = match;
	}

	/**
	 * Returns the raw inner byte array of this match.
	 *
	 * @return the raw inner byte array
	 */
	public byte[] getMatch() {
		return match;
	}

	/**
	 * Returns a String representation of this MessageType in the form of  "x1.x2.x3.xn|n" where x1 .. xn are the bytes of this match and n is the number of bytes
	 *
	 * @return a string containing the match in decimals split by a dot followed by the bytearray length
	 */
	@Override
	public String toString() {
		final StringJoiner joiner = new StringJoiner(".");
		for (final byte byteValue : match) {
			joiner.add(String.valueOf(Byte.toUnsignedInt(byteValue)));
		}
		return joiner.toString() + "|" + match.length;
	}

	/**
	 * Changes the prefix of this messageType with the given MessageType.
	 * For Example if this MessageType contains "AABBCC" and the given MessageType consists of "QQE", this MessageType will end up containing "QQEBCC".
	 *
	 * @param other
	 * 		the new prefix
	 * @throws IllegalArgumentException
	 * 		if the given MessageType is longer than this MessageType
	 */
	public void overridePrefixWith(final MessageType other) {
		if (other.match.length > match.length) {
			throw new IllegalArgumentException("can't overwrite prefix: " + other.match.length + " > " + match.length);
		}
		System.arraycopy(other.match, 0, match, 0, other.match.length);
	}

	/**
	 * Checks if this message type contains the other message type.
	 * A Contains B if and only if:
	 * A.length smaller equals B.length and all indices in A and B for the full range of A are the same
	 *
	 * @param other
	 * 		the other message type to check if it is contained in this messageType.
	 * @return true if this message type "contains" the given message type, false otherwise
	 */
	public boolean containsTopic(final MessageType other) {

		if (match.length > other.match.length) {
			return false;
		}

		for (int i = 0; i < match.length; i++) {
			if (match[i] != other.match[i]) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final MessageType that = (MessageType) o;
		return Arrays.equals(match, that.match);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(match);
	}
}
