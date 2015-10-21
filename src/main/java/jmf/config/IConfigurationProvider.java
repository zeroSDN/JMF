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

package jmf.config;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;

/**
 * This interface specifies the configuration access (read only) methods exposed to the module.
 * created on 7/7/15.
 * @author Jan Strauß
 */
public interface IConfigurationProvider {

    /**
     * Gets the value of the config entry identified by key as string.
     * Returns value if succesful, returns empty if no config loaded, no property with name or parsing failed
     *
     * @param key the key of the value to retrieve
     * @return Value the value was found, OptionalX.empty(); otherwise
     */
	Optional<String> getAsString(String key);

    /**
     * Gets the value of the config entry identified by key as bool.
     * Returns value if succesful, returns empty if no config loaded, no property with name or parsing failed
     *
     * @param key the key of the value to retrieve
     * @return Value the value was found, OptionalX.empty(); otherwise
     */
	Optional<Boolean> getAsBoolean(String key);

    /**
     * Gets the value of the config entry identified by key as integer.
     * Returns value if succesful, returns empty if no config loaded, no property with name or parsing failed
     *
     * @param key the key of the value to retrieve
     * @return Value the value was found, OptionalX.empty(); otherwise
     */
	OptionalLong getAsLong(String key);

    /**
     * Gets the value of the config entry identified by key as double.
     * Returns value if succesful, returns empty if no config loaded, no property with name or parsing failed
     *
     * @param key the key of the value to retrieve
     * @return Value the value was found, OptionalX.empty(); otherwise
     */
	OptionalDouble getAsDouble(String key);
}