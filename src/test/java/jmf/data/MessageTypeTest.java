package jmf.data;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test message type contains method
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