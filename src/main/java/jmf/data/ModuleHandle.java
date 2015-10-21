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

import com.google.common.primitives.UnsignedInteger;

/**
 * Interface to ID information of a module in the ZMF system.
 * Used to access known peers in the system.
 * Created on 7/25/15.
 * Modified on 08/06/15
 * @author Tobias Korb
 * @author Jonas Grunert
 */
public interface ModuleHandle {
	
	ModuleUniqueId getUniqueId();
	
	UnsignedInteger getVersion();

	String getName();
}
