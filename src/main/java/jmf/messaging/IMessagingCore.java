package jmf.messaging;

import jmf.data.Message;
import jmf.data.ModuleUniqueId;
import jmf.messaging.implementation.ExternalRequestIdentity;

/**
 * Counterpart of the IZmfMessagingService interface. The implementation of the IZmfMessagingService will deliver incoming events to the ZMF core via this interface.
 * Additionally this class provides access to the logger to be used by the IZmfMessagingService implementation.
 * Created on 8/2/15.
 * @author Tobias Korb
 * @author Jonas Grunert
 * @author Jan Strauß
 */
public interface IMessagingCore {

	void onSubMsgReceived(Message message, ModuleUniqueId sender);

	void onRequestMsgReceived(ExternalRequestIdentity id, Message message, ModuleUniqueId sender);
}
