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