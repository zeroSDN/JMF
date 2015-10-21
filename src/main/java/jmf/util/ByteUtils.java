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

package jmf.util;

import java.nio.ByteBuffer;

import org.zeromq.ZFrame;

/**
 * Utility class for conversions between datatypes and ZFrame
 * Created on 7/25/15.
 * @author Jan Strauß
 */
public class ByteUtils {

	private static final int CAPACITY_LONG = Long.BYTES;

	public static long convertFrameToLong(final ZFrame frame) {
		ByteBuffer BUFFER_LONG = ByteBuffer.allocate(CAPACITY_LONG);
		BUFFER_LONG.clear();
		BUFFER_LONG.put(frame.getData(), 0, CAPACITY_LONG);
		BUFFER_LONG.flip();
		return BUFFER_LONG.getLong();
	}

	public static ZFrame convertLongToFrame(final long x) {
		ByteBuffer BUFFER_LONG = ByteBuffer.allocate(CAPACITY_LONG);
		BUFFER_LONG.clear();
		BUFFER_LONG.putLong(0, x);
		return new ZFrame(BUFFER_LONG.array());
	}
}