package jmf.data;

import java.util.function.BiConsumer;

import jmf.messaging.ISubscriptionHandler;

/**
 * Subscription handle representing a specific subscription.
 * Can be used to unsubscribe from a subscription
 * Created on 7/25/15.
 * Modified on 08/06/15
 * @author Tobias Korb
 * @author Jonas Grunert
 */
public class SubscriptionHandle {

	private final int subId;
	private final ISubscriptionHandler unsubscribeHandler;
	private final MessageType subTopic;
	private final BiConsumer<Message, ModuleUniqueId> callback;

	public SubscriptionHandle(final ISubscriptionHandler unsubscribeHandler, final MessageType topic, final BiConsumer<Message, ModuleUniqueId> callback) {
		this.subId = unsubscribeHandler.getSubId();
		this.unsubscribeHandler = unsubscribeHandler;
		this.subTopic = topic;
		this.callback = callback;
	}

    /**
     * Unsubscribe from this subscription
     * @throws Exception
     */
	public void unsubscribe() throws Exception {
		unsubscribeHandler.unsubscribe(this);
	}

    /**
     * @return Topic of this subscription
     */
	public MessageType getTopic() {
		return subTopic;
	}

    /**
     * @return Subscription handler callback
     */
	public BiConsumer<Message, ModuleUniqueId> getCallback() {
		return callback;
	}

    /**
     * @return ID of this subscription
     */
	public int getSubId() {
		return subId;
	}
}
