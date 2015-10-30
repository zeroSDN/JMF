package jmf.util;

import org.junit.Assert;
import org.junit.Test;
import org.zeromq.ZFrame;

/**
 * Test byte utils class
 * Created on 8/6/15.
 *
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