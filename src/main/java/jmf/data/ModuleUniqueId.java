package jmf.data;

import java.util.Objects;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.google.protobuf.InvalidProtocolBufferException;

import jmf.proto.FrameworkProto;

/**
 * ID information of a module in the ZMF system.
 * Consists of Type ID (UInt16) and Instance ID (UInt64)
 * Created on 7/25/15.
 * Modified on 08/06/15
 * @author Jonas Grunert
 * @author Tobias Korb
 */
public class ModuleUniqueId {

    /** ID describing the module type */
	private final short typeId;
    /** ID describing the module instance */
	private final long instanceId;

	private final FrameworkProto.SenderId senderProto;
	private final byte[] senderProtoBytes;

	/**
     * Initializing constructor
	 * @param typeId
	 * 		Unsigned Short value vor type ID, is interpreted as unsigned 16bit value
	 * @param instanceId
	 * 		Unsigned Long value for instance ID
	 */
	public ModuleUniqueId(final UnsignedInteger typeId, final UnsignedLong instanceId) {
		if (typeId.intValue() > 65535) {
			throw new IllegalArgumentException("Illegal typeId value > 65535, only ushort values allowed");
		}
		this.typeId = typeId.shortValue();
		this.instanceId = instanceId.longValue();
		this.senderProto = FrameworkProto.SenderId.newBuilder().setTypeId(typeId.shortValue()).setInstanceId(instanceId.longValue()).build();
		this.senderProtoBytes = senderProto.toByteArray();
	}

    /**
     * Initializing constructor
     * @param typeId
     * 		Unsigned Short value vor type ID, is interpreted as unsigned 16bit value
     * @param instanceId
     * 		Unsigned Long value for instance ID
     */
    public ModuleUniqueId(final short typeId, final long instanceId) {
        this.typeId = typeId;
        this.instanceId = instanceId;
        this.senderProto = FrameworkProto.SenderId.newBuilder().setTypeId(Short.toUnsignedInt(typeId)).setInstanceId(instanceId).build();
        this.senderProtoBytes = senderProto.toByteArray();
    }
	
	public ModuleUniqueId(final byte[] senderProtoRaw) {
		try {
			senderProto = FrameworkProto.SenderId.parseFrom(senderProtoRaw);
			senderProtoBytes = senderProto.toByteArray();
			typeId = (short)senderProto.getTypeId();
			instanceId = senderProto.getInstanceId();
		} catch (final InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
	}

	public ModuleUniqueId(final FrameworkProto.SenderId senderId) {
		senderProto = senderId;
		senderProtoBytes = senderId.toByteArray();
		typeId = (short)senderId.getTypeId();
		instanceId = senderId.getInstanceId();
	}
	/**
	 * Constructor for ModuleuniqueId from a string
	 * (Added by Maksim, redo/remove if neccessary)
	 * @param data in typeId:instanceId format
	 */
	public ModuleUniqueId(String data){
		String[] parts = data.split(":");
		this.typeId = UnsignedInteger.valueOf(parts[0]).shortValue();
		this.instanceId = UnsignedLong.valueOf(parts[1]).longValue();
		this.senderProto = FrameworkProto.SenderId.newBuilder().setTypeId(typeId).setInstanceId(instanceId).build();
		this.senderProtoBytes = senderProto.toByteArray();
			
	}


    /**
     * @return ID describing the module type
     */
	public short getTypeId() {
		return typeId;
	}
    /**
     * @return ID describing the module type as UnsignedInt
     */
    public UnsignedInteger getTypeIdUnsigned() {
        return UnsignedInteger.fromIntBits(typeId);
    }

    /**
     * @return ID describing the module instance
     */
	public long getInstanceId() {
		return instanceId;
	}
    /**
     * @return ID describing the module instance as UnsignedLong
     */
    public UnsignedLong getInstanceIdUnsigned() {
        return UnsignedLong.fromLongBits(instanceId);
    }


	public byte[] getSenderProtoBytes() {
		return senderProtoBytes;
	}

	public FrameworkProto.SenderId getSenderProto() {
		return senderProto;
	}

	@Override
	public String toString() {
		return Integer.toUnsignedString(Short.toUnsignedInt(typeId)) + ":" + Long.toUnsignedString(instanceId);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ModuleUniqueId that = (ModuleUniqueId) o;
		return typeId == that.typeId && instanceId  == that.instanceId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(typeId, instanceId);
	}

}
