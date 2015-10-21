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

import org.junit.Test;
import org.zeromq.ZFrame;

import junit.framework.Assert;

/**
 * TODO Descrive
 * Created on 8/6/15.
 * @author Jan Strauß
 */
public class ByteUtilsTest {
	
	@Test
	public void testLong() throws Exception {

		final long id = 9635324;

		final ZFrame frame = ByteUtils.convertLongToFrame(id);

		final long parsed = ByteUtils.convertFrameToLong(frame);

		final ZFrame frame2 = ByteUtils.convertLongToFrame(parsed);

		Assert.assertEquals(id, parsed);
		Assert.assertEquals(frame, frame2);
	}
}