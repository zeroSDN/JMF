package jmf.data;

import com.google.common.primitives.UnsignedInteger;

/**
 * Internal representation of ID information of a module in the ZMF system.
 * Used to store known peers in the system and to offer public access it via ModuleHandle interface.
 * Created on 7/25/15.
 * Modified on 08/07/15
 * @author Tobias Korb
 * @author Jonas Grunert
 * @author Jan Strauß
 */
public class ModuleHandleInternal implements ModuleHandle {

	/** Unique identifier of the module (type+instance id) */
	private final ModuleUniqueId uniqueId;
	/** Version of this module */
	private final short version;
	/** Name of this module (human readable */
	private final String name;
	/** local ZMQ socket port */
	private int self_ZmqPubPort;
	/** local ZMQ socket port */
	private int self_ZmqRepPort;

	/** ZMQ publish socket address for this module */
	private String zmqPubAddr;
	/** ZMQ reply socket address for this module */
	private String zmqRepAddr;

	private int peerTimeout = 0;

	private final boolean self;

	public ModuleHandleInternal(final ModuleUniqueId moduleId, final short version, final String name, final boolean self) {
		this.uniqueId = moduleId;
		this.name = name;
		this.version = version;
		this.self = self;
	}

    public ModuleHandleInternal(final ModuleUniqueId moduleId, final UnsignedInteger version, final String name, final boolean self) {
        if (version.intValue() > 65535) {
            throw new IllegalArgumentException("Illegal version value > 65535, only ushort values allowed");
        }
        this.uniqueId = moduleId;
        this.name = name;
        this.version = version.shortValue();
        this.self = self;
    }

	public ModuleHandleInternal(final ModuleUniqueId moduleId, final short version, final String name, final String zmqPubAddr, final String zmqRepAddr, final boolean self) {
		uniqueId = moduleId;
		this.name = name;
		this.version = version;
		this.zmqPubAddr = zmqPubAddr;
		this.zmqRepAddr = zmqRepAddr;
		this.self = self;
	}

	public void selfSetPubPort(final int port) {
		if (!self) {
			throw new IllegalStateException("trying to set PubPort on non self moduleHandle");
		}
		self_ZmqPubPort = port;
		zmqPubAddr = "tcp://127.0.0.1:" + port;
	}

	public void selfSetRepPort(final int port) {
		if (!self) {
			throw new IllegalStateException("trying to set RepPort on non self moduleHandle");
		}
		self_ZmqRepPort = port;
		zmqRepAddr = "tcp://127.0.0.1:" + port;
	}
	
	public ModuleUniqueId getUniqueId() {
		return uniqueId;
	}

	public short getVersion() {
		return version;
	}

    public UnsignedInteger getVersionUnsigned() {
        return UnsignedInteger.fromIntBits(Short.toUnsignedInt(version));
    }

	public String getName() {
		return name;
	}

	public int getPeerTimeout() {
		return peerTimeout;
	}
	
	public void incrementPeerTimeout(final int multicast_frequency_) {
		this.peerTimeout += multicast_frequency_;
	}
	
	public String getZmqPubAddr() {
		return zmqPubAddr;
	}

	public String getZmqRepAddr() {
		return zmqRepAddr;
	}
	
	public int getSelfPubPort() {
		return self_ZmqPubPort;
	}
	
	public int getSelfRepPort() {
		return self_ZmqRepPort;
	}
	
	public void resetPeerTimeout() {
		this.peerTimeout = 0;
	}
}
