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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jmf.messaging.IMessagingService;

/**
 * Represents an incoming reply.
 * Offers Java Future functions to wait on incoming reply.
 * Created on 7/25/15.
 * @author Jan Strauß
 */
public class InReply {

	private final long requestId;
	private final Future<Message> future;
	private final IMessagingService zmqService;

	public InReply(final long requestId, final Future<Message> future, final IMessagingService zmqService) {
		this.requestId = requestId;
		this.future = future;
		this.zmqService = zmqService;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		zmqService.cancelRequest(requestId, false);
		future.cancel(false);
	}

    /**
     * Waits until receiving reply (future.get)
     * @return Received Reply
     * @throws ExecutionException
     * @throws InterruptedException
     */
	public Message get() throws ExecutionException, InterruptedException {
		return future.get();
	}

    /**
     * Waits until receiving reply with timeut
     * @param timeout Specifies timeout
     * @param unit Unit if timeout time
     * @return Received Reply
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
	public Message get(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException {
		return future.get(timeout, unit);
	}

    /**
     * Returns future.isDone value
     */
	public boolean isDone() {
		return future.isDone();
	}

    /**
     * Cancels ZMQ request and request future
     */
	public void cancelRequest() {
		zmqService.cancelRequest(requestId, true);
		future.cancel(false);
	}

}
