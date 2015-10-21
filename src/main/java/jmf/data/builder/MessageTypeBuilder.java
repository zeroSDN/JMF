package jmf.data.builder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import jmf.data.MessageType;

/**
 * Builder for MessageType, can be reused after a build() call.
 * The append methods will throw BufferOverflowException if there is not enough space left for the given type.
 * Created on 8/5/15.
 * @author Jan Strauß
 */
public class MessageTypeBuilder {

	private final ByteBuffer buffer;

	/**
	 * Default constructor.
	 */
	public MessageTypeBuilder() {
		buffer = ByteBuffer.allocate(MessageType.MESSAGE_ID_BYTES);
		buffer.order(ByteOrder.BIG_ENDIAN);
	}

	/**
	 * Appends 8 bit (1 byte) to this builders buffer
	 *
	 * @param toAppend
	 * 		the byte to append
	 * @return this
	 */
	public MessageTypeBuilder appendMatch8(final byte toAppend) {
		buffer.put(toAppend);
		return this;
	}

	/**
	 * Appends 16 bit (2 byte) to this builders buffer
	 *
	 * @param toAppend
	 * 		the short to append
	 * @return this
	 */
	public MessageTypeBuilder appendMatch16(final short toAppend) {
		buffer.putShort(toAppend);
		return this;
	}

	/**
	 * Appends 32 bit (4 byte) to this builders buffer
	 *
	 * @param toAppend
	 * 		the int to append
	 * @return this
	 */
	public MessageTypeBuilder appendMatch32(final int toAppend) {
		buffer.putInt(toAppend);
		return this;
	}

	/**
	 * Appends 64 bit (8 byte) to this builders buffer
	 *
	 * @param toAppend
	 * 		the long to append
	 * @return this
	 */
	public MessageTypeBuilder appendMatch64(final long toAppend) {
		buffer.putLong(toAppend);
		return this;
	}

	/**
	 * This will construct a MessageType from the data appended to this builder and subsequently reset the builder so it can be reused.
	 *
	 * @return the message type build from this builders buffer
	 */
	public MessageType build() {
		buffer.flip();
		final MessageType messageType = new MessageType(Arrays.copyOfRange(buffer.array(), 0, buffer.limit()));
		buffer.clear();
		return messageType;
	}

	/**
	 * @return the number of bytes free
	 */
	public int remaining() {
		return buffer.remaining();
	}
}
