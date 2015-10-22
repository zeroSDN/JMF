package jmf.config;

import java.util.Optional;

import org.junit.Test;

import jmf.config.implementation.ConfigurationProviderImplementation;
import junit.framework.Assert;


/**
 * TODO Describe
 * Created on 8/2/15.
 * @author Jan Strauß
 */
public class ConfigurationProviderImplementationTest {

	private final ConfigurationProviderImplementation subject;

	public ConfigurationProviderImplementationTest() {
		this.subject = new ConfigurationProviderImplementation(Optional.of("src/test/resources/testConfig.config"));
	}

	@Test
	public void testGetAsString() throws Exception {
		Assert.assertEquals(subject.getAsString("stringValue").get(), "dankMemes");
	}

	@Test
	public void testGetAsBoolean() throws Exception {
		Assert.assertEquals(subject.getAsBoolean("boolValue").get(), Boolean.TRUE);
	}

	@Test
	public void testGetAsLong() throws Exception {
		Assert.assertEquals(subject.getAsLong("intValue").getAsLong(), 1337);
	}

	@Test
	public void testGetAsDouble() throws Exception {
		Assert.assertEquals(subject.getAsDouble("doubleValue").getAsDouble(), 4.20);
	}
}