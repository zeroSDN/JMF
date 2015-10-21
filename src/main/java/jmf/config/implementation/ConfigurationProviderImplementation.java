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

package jmf.config.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmf.config.IConfigurationProvider;

/**
 * Implementation of the IConfigurationProvider interface.
 * created on 8/2/15.
 * @author Jan Strauß
 */
public class ConfigurationProviderImplementation implements IConfigurationProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationProviderImplementation.class);

	private Path pathToFile;
	private final Properties configValues = new Properties();
    private boolean configLoaded = false;

	public ConfigurationProviderImplementation(final Optional<String> configPath) {
		try {
			if (configPath.isPresent()) {
				pathToFile = Paths.get(".").resolve(configPath.get());
			} else {
				pathToFile = Paths.get(System.getProperty("user.home"), ".jmf", "jmf_module.config");
			}
			loadConfig();
		} catch (final IOException e) {
			LOGGER.error("failed to load config file: " + e.getClass().getName() + " - " + e.getMessage());
		}
	}

	public void loadConfig() throws IOException {
		LOGGER.info("loading config values from: " + pathToFile);
		final InputStream stream = Files.newInputStream(pathToFile);
		configValues.load(stream);
		stream.close();
        configLoaded = true;
	}

	@Override
	public Optional<String> getAsString(final String key) {
        if(!configLoaded) {
            return Optional.empty();
        }

		final String value = configValues.getProperty(key);

		if (value == null) {
			return Optional.empty();
		}

		return Optional.of(value);
	}

	@Override
	public Optional<Boolean> getAsBoolean(final String key) {
        if(!configLoaded) {
            return Optional.empty();
        }

		final String strValue = configValues.getProperty(key);

		if (strValue == null) {
			return Optional.empty();
		}

		final Boolean value = Boolean.valueOf(strValue);

		return Optional.of(value);
	}

	@Override
	public OptionalLong getAsLong(final String key) {
        if(!configLoaded) {
            return OptionalLong.empty();
        }

		final String strValue = configValues.getProperty(key);

		if (strValue == null) {
			return OptionalLong.empty();
		}

		try {
			final long value = Long.parseLong(strValue);
			return OptionalLong.of(value);
		} catch (final NumberFormatException e) {
			LOGGER.error("failed to convert value of key " + key + " (" + strValue + ") to long", e);
			return OptionalLong.empty();
		}

	}

	@Override
	public OptionalDouble getAsDouble(final String key) {
        if(!configLoaded) {
            return OptionalDouble.empty();
        }

		final String strValue = configValues.getProperty(key);

		if (strValue == null) {
			return OptionalDouble.empty();
		}

		try {
			final double value = Double.parseDouble(strValue);
			return OptionalDouble.of(value);
		} catch (final NumberFormatException e) {
			LOGGER.error("failed to convert value of key " + key + " (" + strValue + ") to double", e);
			return OptionalDouble.empty();
		}
	}
}
