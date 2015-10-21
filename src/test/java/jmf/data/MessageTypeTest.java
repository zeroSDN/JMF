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

import org.junit.Assert;
import org.junit.Test;

/**
 * TODO Describe
 * Created on 8/5/15.
 * @author Jan Strauß
 */
public class MessageTypeTest {
	
	@Test
	public void testContainsTopic() throws Exception {


		MessageType full = new MessageType("".getBytes());
		MessageType a = new MessageType("a".getBytes());
		MessageType aa = new MessageType("aa".getBytes());
		MessageType aaa = new MessageType("aaa".getBytes());
		MessageType aab = new MessageType("aab".getBytes());
		MessageType aabb = new MessageType("aabb".getBytes());


		Assert.assertTrue(full.containsTopic(full));
		Assert.assertTrue(full.containsTopic(a));

		Assert.assertTrue(a.containsTopic(a));
		Assert.assertTrue(a.containsTopic(aa));
		Assert.assertTrue(a.containsTopic(aaa));
		Assert.assertTrue(a.containsTopic(aab));
		Assert.assertTrue(a.containsTopic(aabb));

		Assert.assertFalse(aa.containsTopic(a));
		Assert.assertFalse(aab.containsTopic(aaa));
		Assert.assertFalse(aaa.containsTopic(aab));

	}
}