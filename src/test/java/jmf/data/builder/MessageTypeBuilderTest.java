package jmf.data.builder;

import java.nio.BufferOverflowException;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test message type builder
 * Created on 8/6/15.
 *
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