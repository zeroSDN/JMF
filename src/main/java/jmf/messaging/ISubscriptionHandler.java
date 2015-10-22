package jmf.messaging;

import jmf.data.SubscriptionHandle;

/**
 * Interface offering possibility to unsubscribe from a subscription
 * Created on 8/2/15.
 * @author Tobias Korb
 * @author Jan Strauß
 */
public interface ISubscriptionHandler {
    /**
     * Unsubscribes from the given handle
     * @param handle Handle to unsubscribe
     * @throws Exception Exception if fails
     */
	void unsubscribe(SubscriptionHandle handle) throws Exception;

    /**
     * @return ID of the corresponding subscription
     */
	int getSubId();
}
