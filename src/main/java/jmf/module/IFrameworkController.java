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

package jmf.module;

import jmf.data.ModuleUniqueId;

/**
 * Interface to control a ZMF instance.
 * Created on 7/25/15.
 * @author Tobias Korb
 * @author Jonas Grunert
 * @author Jan Strauß
 */
public interface IFrameworkController {
	/**
	 * Requests trying to enable the module controlled by this instance controller as soon as possible.
	 * Module will be enabled if all preconditions are satisfied (dependencies satisfied, module id unique)
	 */
	void requestEnableModule();

	/**
	 * Requests disabling the module controlled by this instance controller as soon as possible.
	 */
	void requestDisableModule();

	/**
	 * Requests to stop the instance as soon as possible.
	 * Will not will block until the instance is shut down.
	 */
	void requestStopInstance();

	/**
	 * Requests the remote instance to be enabled. Will wait for the given timeout (ms) and return true only if a reply was
	 * received for the request and if the received reply was positive
	 */
	boolean requestEnableRemoteInstance(ModuleUniqueId id, long timeout);

	/**
	 * Requests the remote instance to be disabled. Will wait for the given timeout (ms) and return true only if a reply was
	 * received for the request and if the received reply was positive
	 */
	boolean requestDisableRemoteInstance(ModuleUniqueId id, long timeout);

	/**
	 * Requests the remote instance to stop. Will wait for the given timeout (ms) and return true only if a reply was
	 * received for the request and if the received reply was positive
	 */
	boolean requestStopRemoteInstance(ModuleUniqueId id, long timeout);

	/**
	 * Stopps the instance immediately
	 * Will not will block until the instance is shut down.
	 */
	void stopInstance();

	/**
	 * Joins the execution of the ZMF instance and the module, will block until the instance is shut down.
	 */
	void joinExecution();

	/**
	 * Returns the controlled module
	 */
	AbstractModule getModule();

	/**
	 * Returns if zmf instance is started and no shutdown in progress
	 */
	boolean isStarted();

	/**
	 * Returns if instance not started or shutdown finished and no starting in progress
	 */
	boolean isStopped();
}
