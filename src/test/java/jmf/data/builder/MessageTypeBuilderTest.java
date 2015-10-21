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

package jmf.data.builder;

import java.nio.BufferOverflowException;
import java.util.Random;

import org.junit.Test;

import junit.framework.Assert;

/**
 * TODO Describe
 * Created on 8/6/15.
 * @author Jan Strauß
 */
public class MessageTypeBuilderTest {

	@Test(expected = BufferOverflowException.class)
	public void testMessageTypeBuilder() {
		final Random rnd = new Random(1337);

		final MessageTypeBuilder builderEmpty = new MessageTypeBuilder();
		System.out.println(builderEmpty.build());

		Assert.assertTrue(true);

		final MessageTypeBuilder builder = new MessageTypeBuilder();
		builder.appendMatch8((byte) 0x00);
		builder.appendMatch8((byte) 0xFF);
		builder.appendMatch16((short) 0x0F0F);
		builder.appendMatch32(0xAFFEAFFE);
		builder.appendMatch64(0xDEADBEEFFACEB00CL);

		System.out.println(builder.build());

		Assert.assertTrue(true);

		builder.appendMatch64(rnd.nextLong());
		builder.appendMatch64(rnd.nextLong());
		builder.appendMatch64(rnd.nextLong());
		builder.appendMatch64(rnd.nextLong());

		System.out.println(builder.build());

		Assert.assertTrue(true);

		builder.appendMatch64(rnd.nextLong());
		builder.appendMatch64(rnd.nextLong());
		builder.appendMatch64(rnd.nextLong());
		builder.appendMatch64(rnd.nextLong());

		Assert.assertTrue(true);

		builder.appendMatch32(rnd.nextInt());

		Assert.fail();

	}
	
}