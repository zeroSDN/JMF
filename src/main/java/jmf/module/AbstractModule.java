package jmf.module;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.primitives.UnsignedInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmf.data.*;

/**
 * Abstract base class of a JMF module.
 * Defines methods to override and offers access to the underlying JMF instance.
 * Every module must have an ID (type+instance), a version, a name and optionally dependencies.
 * The base class offers a logger and JMF access via IFrameworkAccess.
 * Created on 7/25/15.
 * Modified on 08/06/15
 * @author Jonas Grunert
 * @author Tobias Korb
 * @author Jan Strauß
 */
public abstract class AbstractModule {

    /** Unique identifier of this module */
	private final ModuleUniqueId uniqueId;
    /** Version of this module */
	private final UnsignedInteger version;
    /** Name of this module */
	private final String name;
    /** Dependencies needed by this module */
	private final Collection<ModuleDependency> dependencies;

    /** Common Logger for event logging */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractModule.class);
    /** @return Common Logger for event logging */
	public Logger getLogger() {
		return LOGGER;
	}

    /** Enabled state of this module */
	private AtomicReference<ModuleEnabledState> moduleState = new AtomicReference<>(ModuleEnabledState.DISABLED);

    /** Enabled state of a module */
    enum ModuleEnabledState {
        /** Module is disabled */
        DISABLED,
        /** Module currently enabling */
        ENABLING,
        /** Module is enabled */
        ENABLED,
        /** Module is currently disabling */
        DISABLING
    }

    /** Interface to access JMF functions */
    private IFrameworkAccess frameworkAccess;

    /**
     * Internal Mutex to lock object by JMF
     * ONLY USE FROM CORE, NOT FROM MODULE
     */
	private final Object internalMutex = new Object();


    /**
     * Base class constructor for modules. Sets given parameters and allows ZMF to internally access their values.
     * @param uniqueId Unique identifier of this module
     * @param version Version of this module
     * @param name Name of this module
     * @param dependencies Dependencies needed by this module
     */
	public AbstractModule(ModuleUniqueId uniqueId, UnsignedInteger version, String name, Collection<ModuleDependency> dependencies) {
        if(version.intValue() > 65535) {
            throw new IllegalArgumentException("Illegal version value > 65535, only ushort values allowed");
        }
		this.uniqueId = uniqueId;
		this.version = version;
		this.name = name;
		this.dependencies = dependencies;
	}


    /** @return Unique identifier of this module */
	public final ModuleUniqueId getUniqueId() {
		return uniqueId;
	}

    /** @return Name of this module */
	public final String getModuleName() {
		return name;
	}

    /** @return Version of this module */
	public final UnsignedInteger getVersion() {
		return version;
	}

    /** @return Dependencies needed by this module */
	public final Collection<ModuleDependency> getDependencies() {
		return dependencies;
	}


    /** @return Interface to access JMF functions */
	public final IFrameworkAccess getFramework() {
		return frameworkAccess;
	}


    /** @return If true module is currently Enabled */
	public final boolean isEnabled() {
		return moduleState.get().equals(ModuleEnabledState.ENABLED);
	}


	/**
	 * ONLY CALL FROM CORE, NOT FROM MODULE
	 */
	public Object INTERNAL_getInternalMutex() {
		return internalMutex;
	}

    /**
     * Enable method to override by module class.
     * Enables module eg. connecting, starting threads
     * ONLY CALL FROM CORE, NOT FROM MODULE
     * @return True if module enable succeeded
     */
	public abstract boolean enable();

    /**
     * Disable method to override by module class.
     * Disables module eg. disconnecting, stopping threads
     * ONLY CALL FROM CORE, NOT FROM MODULE
     */
	public abstract void disable();

	/**
	 * ONLY CALL FROM CORE, NOT FROM MODULE
     * Initiates module enable
	 */
	public final boolean INTERNAL_internalEnable(IFrameworkAccess access) {
		if (!moduleState.compareAndSet(ModuleEnabledState.DISABLED, ModuleEnabledState.ENABLING)) {
			LOGGER.warn("Tried to enable module twice - ignoring request");
			return true;
		}

		frameworkAccess = access;

		LOGGER.info("Enabling module " + getNameInstanceString());

		if (enable()) {
			LOGGER.info("Enabled module " + getNameInstanceString());
            moduleState.set(ModuleEnabledState.ENABLED);
			return true;
		} else {
			LOGGER.error("Failed to enable " + getNameInstanceString());
            moduleState.set(ModuleEnabledState.DISABLED);
			return false;
		}
	}

    /**
     * ONLY CALL FROM CORE, NOT FROM MODULE
     * Initiates module disable
     */
	public final void INTERNAL_internalDisable() {
        if (!moduleState.compareAndSet(ModuleEnabledState.ENABLED, ModuleEnabledState.DISABLING)) {
			LOGGER.warn("Tried to disable module that is not enabled - ignoring request");
			return;
		}

		LOGGER.info("Disabling module " + getNameInstanceString());
		disable();
        LOGGER.info("Disabled module " + getNameInstanceString());

        moduleState.set(ModuleEnabledState.DISABLED);

        frameworkAccess = null;
	}

    /**
     * Handler to handle incoming requests
     * ONLY CALL FROM CORE, NOT FROM MODULE
     * @param message Incoming request message
     * @param sender Sender of the request
     * @return Outgoing reply to reply on request
     */
	public OutReply handleRequest(final Message message, final ModuleUniqueId sender) {
		return null;
	}

    /**
     * Handler to handle incoming requests
     * ONLY CALL FROM CORE, NOT FROM MODULE
     * @param changedModule Handle of the module whichs state changed
     * @param newState New state of the module
     * @param lastState Last state of the module before change
     */
	public void handleModuleStateChange(final ModuleHandle changedModule,
										final ModuleLifecycleState newState,
										final ModuleLifecycleState lastState) {
	}


    /**
     * @return String with name and instance ID [name]:[instanceId] eg. ModulX:1
     */
	public final String getNameInstanceString() {
		return name + ":" + uniqueId.getInstanceId().toString();
	}

    /**
     * @return String with name, type and instance ID [name]([typeId]):[instanceId] eg. ModulX(2):1
     */
	public final String getNameTypeInstanceString() {
		return getModuleName() + "(" + uniqueId.getTypeId().toString() + "):" +
				uniqueId.getInstanceId().toString();
	}

    /**
     * @return String with name, type ID, instance ID and version [name]([typeId]):[instanceId]_v[version] eg. ModulX(2):1_v1
     */
	public final String getNameTypeInstanceVersionString() {
		return getNameInstanceString() + "_v" + version.toString();
	}
}
