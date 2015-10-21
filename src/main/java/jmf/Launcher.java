package jmf;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;

import jmf.config.IConfigurationProvider;
import jmf.config.implementation.ConfigurationProviderImplementation;
import jmf.core.Core;
import jmf.discovery.IPeerDiscoveryService;
import jmf.discovery.implementation.PeerDiscoveryService;
import jmf.messaging.IMessagingService;
import jmf.messaging.implementation.ZmqMessagingService;
import jmf.module.AbstractModule;
import jmf.module.IFrameworkController;

/**
 * Launcher class to create and start new ZMF instances
 * Created on 8/5/15.
 *
 * @author Jan Strauß
 * @author Janas Grunert
 * @author Andre Kutzleb
 */
public class Launcher {

	/**
	 * Starting options when creating and starting a new ZMF instance
	 */
	public enum StartingOption {
		/**
		 * NO auto enable, will NOT try to enable the module as soon as possible
		 */
		NO_AUTO_ENABLE,
		/**
		 * Will NOT exit if module enabling fails
		 */
		NO_EXIT_WHEN_ENABLE_FAILED,
		/**
		 * Peer discovery will NOT wait until all active modules discovered
		 */
		NO_PEER_DISCOVERY_WAIT,

		/**
		 * This instance will not connect to modules with the same type id
		 */
		NO_EQUAL_MODULE_INTERCONNECT
	}

	/**
	 * Creates and starts a new ZMF instance and uses default configuration file.
	 *
	 * @param module
	 * 		the module to start
	 * @param startingOptions
	 * 		Starting options for ZMF
	 * @return the framework controller for this framework instance if successful
	 * @throws RuntimeException
	 * 		if fails to create instance
	 */
	public static IFrameworkController createInstance(final AbstractModule module, final StartingOption... startingOptions) {
		return createInstance(module, Optional.<String>empty(), startingOptions);
	}

	/**
	 * Creates and starts  a new ZMF instance
	 *
	 * @param module
	 * 		the module to start
	 * @param configPath
	 * 		Path to the user defined config file
	 * @param startingOptions
	 * 		Starting options for ZMF
	 * @return the framework controller for this framework instance if successful
	 * @throws RuntimeException
	 * 		if fails to create instance
	 */
	public static IFrameworkController createInstance(final AbstractModule module, final Optional<String> configPath, final StartingOption... startingOptions) {
		final EnumSet<StartingOption> asSet = EnumSet.noneOf(StartingOption.class);
		asSet.addAll(Arrays.asList(startingOptions));
		
		if (asSet.size() != startingOptions.length) {
			throw new IllegalArgumentException("Duplicate starting options");
		}

		final boolean moduleAutoEnable = !asSet.contains(StartingOption.NO_AUTO_ENABLE);
		final boolean peerDiscoveryWait = !asSet.contains(StartingOption.NO_PEER_DISCOVERY_WAIT);
		final boolean exitWhenEnableFail = !asSet.contains(StartingOption.NO_EXIT_WHEN_ENABLE_FAILED);
		final boolean disableEqualModuleInterconnect = !asSet.contains(StartingOption.NO_EQUAL_MODULE_INTERCONNECT);

		//noinspection deprecation
		return createInstance(module, true, moduleAutoEnable, exitWhenEnableFail, peerDiscoveryWait, disableEqualModuleInterconnect, configPath);
	}

	/**
	 * Creates and starts  a new ZMF instance and uses default configuration file.
	 *
	 * @param module
	 * 		the module to start
	 * @param trackModuleStates
	 * 		deprecated - will be ignored
	 * @param moduleAutoEnable
	 * 		If true tries to enable the module as soon as possible
	 * @param exitWhenEnableFail
	 * 		If true will exit if module enabling fails
	 * @param peerDiscoveryWait
	 * 		If true the peer discovery will wait until all active modules discovered
	 * @return the framework controller for this framework instance if successful
	 * @throws RuntimeException
	 * 		if fails to create instance
	 * @deprecated use createInstance with StartingOptions
	 */
	@Deprecated
	public static IFrameworkController createInstance(final AbstractModule module, final boolean trackModuleStates, final boolean moduleAutoEnable, final boolean exitWhenEnableFail, final boolean peerDiscoveryWait) {
		return createInstance(module, trackModuleStates, moduleAutoEnable, exitWhenEnableFail, peerDiscoveryWait, false, Optional.<String>empty());
	}

	/**
	 * Creates and starts  a new ZMF instance
	 *
	 * @param module
	 * 		the module to start
	 * @param trackModuleStates
	 * 		deprecated - will be ignored
	 * @param moduleAutoEnable
	 * 		If true tries to enable the module as soon as possible
	 * @param exitWhenEnableFail
	 * 		If true will exit if module enabling fails
	 * @param peerDiscoveryWait
	 * 		If true the peer discovery will wait until all active modules discovered
	 * @param configPath
	 * 		Path to the user defined config file
	 * @return the framework controller for this framework instance if successful
	 * @throws RuntimeException
	 * 		if fails to create instance
	 * @deprecated use createInstance with StartingOptions
	 */
	@Deprecated
	public static IFrameworkController createInstance(final AbstractModule module, final boolean trackModuleStates, final boolean moduleAutoEnable, final boolean exitWhenEnableFail, final boolean peerDiscoveryWait, final boolean disableEqualModuleInterconnect, final Optional<String> configPath) {

		final IConfigurationProvider cfg = new ConfigurationProviderImplementation(configPath);
		final IPeerDiscoveryService peerDisc = new PeerDiscoveryService();
		final IMessagingService zmqServ = new ZmqMessagingService();
		
		final Core core = new Core(cfg, module, peerDisc, zmqServ);
		
		if (!core.startInstance(moduleAutoEnable, exitWhenEnableFail, peerDiscoveryWait, disableEqualModuleInterconnect)) {
			throw new RuntimeException("failed to start instance");
		}
		
		return core;
	}
}
