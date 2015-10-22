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