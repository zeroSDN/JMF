package jmf.data;

import com.google.common.primitives.UnsignedInteger;

/**
 * Specifies a module dependency, consists of dependency module type and dependency module version.
 * Created on 7/25/15.
 * Modified on 08/06/15
 * @author Jonas Grunert
 * @author Jan Strauß
 */
public class ModuleDependency {

	private final UnsignedInteger moduleTypeId;
	private final UnsignedInteger moduleVersion;

	public ModuleDependency(final UnsignedInteger moduleTypeId, final UnsignedInteger moduleVersion) {
		if(moduleTypeId.intValue() > 65535) {
			throw new IllegalArgumentException("Illegal moduleTypeId value > 65535, only ushort values allowed");
		}
		if(moduleVersion.intValue() > 65535) {
			throw new IllegalArgumentException("Illegal version moduleVersion > 65535, only ushort values allowed");
		}
		this.moduleTypeId = moduleTypeId;
		this.moduleVersion = moduleVersion;
	}

    /**
     * Returns dependency type
     * @return Type of the needed dependency
     */
	public UnsignedInteger getModuleTypeId() {
		return moduleTypeId;
	}

    /**
     * Returns dependency version
     * @return Version of the needed dependency
     */
	public UnsignedInteger getModuleVersion() {
		return moduleVersion;
	}
}
