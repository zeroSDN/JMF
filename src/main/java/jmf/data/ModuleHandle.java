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
