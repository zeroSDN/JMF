package jmf.messaging;

import jmf.config.IConfigurationProvider;
import jmf.data.*;
import jmf.messaging.implementation.ExternalRequestIdentity;

/**
 * This class provides the interface definition for the ZMQ-module of ZMF. The IZmfMessagingCoreInterface class provides the logical counterpart.
 * The Implementation
 * Created on 8/2/15.
 * @author Tobias Korb
 * @author Jan Strauß
 */
public interface IMessagingService {

	boolean start(IMessagingCore core, ModuleHandleInternal selfHandle, IConfigurationProvider config);

	void stop();

	void peerJoin(ModuleHandleInternal module);

	void peerLeave(ModuleHandleInternal module);

	void subscribe(MessageType topic);

	void unsubscribe(MessageType topic);

	void publish(Message msg);

	InReply sendRequest(ModuleUniqueId target, Message msg);

	void sendReply(ExternalRequestIdentity id, Message msg);

	void cancelRequest(long requestID, boolean manual);

	void onDisable();

}
