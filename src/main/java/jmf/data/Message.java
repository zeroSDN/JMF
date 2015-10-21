package jmf.data;

import java.util.Arrays;
import java.util.Objects;

/**
 * A message to send or received, a pair of message type and data
 * Created on 7/25/15.
 * @author Jan Strauße
 */
public class Message {

	private final MessageType type;
	private final byte[] data;

	public Message(final MessageType type, final byte[] data) {
		this.type = type;
		this.data = data;
	}

	public Message(final byte[] type, final byte[] data) {
		this.type = new MessageType(type);
		this.data = data;
	}

	public MessageType getType() {
		return type;
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Message message = (Message) o;
		return Objects.equals(type, message.type) && Arrays.equals(data, message.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, data);
	}
}
