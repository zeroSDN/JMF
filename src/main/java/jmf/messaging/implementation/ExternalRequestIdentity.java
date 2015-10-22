package jmf.messaging.implementation;

import java.util.Objects;

import jmf.data.ModuleUniqueId;

/**
 * Data structure representing ID of an externally requesting peer
 * Created on 8/2/15.
 * @author Jan Strauß
 */
public class ExternalRequestIdentity {

	public final ModuleUniqueId senderId;
	public final long messageId;

	public ExternalRequestIdentity(final ModuleUniqueId senderId, final long messageId) {
		this.senderId = senderId;
		this.messageId = messageId;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ExternalRequestIdentity that = (ExternalRequestIdentity) o;
		return Objects.equals(messageId, that.messageId) && Objects.equals(senderId, that.senderId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(senderId, messageId);
	}
}
