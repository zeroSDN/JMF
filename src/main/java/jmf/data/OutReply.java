/*
 * Copyright 2015 ZSDN Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jmf.data;

import jmf.messaging.IMessagingService;
import jmf.messaging.implementation.ExternalRequestIdentity;

/**
 * Represents an outgoing reply message.
 * Can be NO_REPLY (no reply will be sent),
 * IMMEDIATE_REPLY (reply now) or
 * FUTURE_REPLY (allows replying in the future)
 * Improvment idea: Factory + different classes
 * Created on 7/25/15.
 * @author Tobias Korb
 * @author Jan Strauß
 */
public class OutReply {
	private final Object serviceLock = new Object();
	
	public Message getReplyImmediate() {
		return replyImmediate;
	}

    /**
     * Type of the reply
     */
	public enum ReplyType {
        /** Send reply immediately */
		IMMEDIATE_REPLY,
        /** Send reply in the future */
		FUTURE_REPLY,
        /** Send no reply */
		NO_REPLY
	}

    /**
     * State indicating if reply is finished or open
     */
	private enum State {
		OPEN, FINISHED
	}

    /// Type of how to reply
	private final ReplyType type;
	private Message replyImmediate;
    /// State indicating if reply is finished or open
	private State state;
	private ExternalRequestIdentity requestId;
	private IMessagingService service;

    /**
     * Private constructor, use create methods to construct instance
     */
	private OutReply(final Message replyImmediate) {
		this.replyImmediate = replyImmediate;
		type = ReplyType.IMMEDIATE_REPLY;
		state = State.FINISHED;
	}

    /**
     * Private constructor, use create methods to construct instance
     */
	private OutReply(final ReplyType type, final State state) {
		this.type = type;
		this.state = state;
	}

    /**
     * Creates and returns a NoReply reply
     */
	public static OutReply createNoReply() {
		return new OutReply(ReplyType.NO_REPLY, State.FINISHED);
	}

    /**
     * Creates and returns a Immediate reply
     */
	public static OutReply createImmediateReply(final Message immediate) {
		return new OutReply(immediate);
	}

    /**
     * Creates and returns a Future reply
     */
	public static OutReply createFutureReply() {
		return new OutReply(ReplyType.FUTURE_REPLY, State.OPEN);
	}


	public void injectFutureInfo(final IMessagingService service, final ExternalRequestIdentity requestId) {
		synchronized (serviceLock) {
			this.service = service;
			this.requestId = requestId;

			serviceLock.notify();
		}
	}

	public ReplyType getType() {
		return type;
	}

    /**
     * Used to send a future reply
     * @param message Reply message
     */
	public void sendFutureReply(final Message message) {
		if (state == State.FINISHED) {
			return;
		}

		synchronized (serviceLock) {
			try {
				while (service == null) {
					serviceLock.wait();
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}

		service.sendReply(requestId, message);
		state = State.FINISHED;
	}

}
