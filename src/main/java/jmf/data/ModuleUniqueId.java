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
	private final UnsignedInteger typeId;
    /** ID describing the module instance */
	private final UnsignedLong instanceId;

	private final FrameworkProto.SenderId senderProto;
	private final byte[] senderProtoBytes;

	/**
     * Initializing constructor
	 * @param typeId
	 * 		Unsigned Short value vor type ID
	 * @param instanceId
	 * 		Unsigned Long value for instance ID
	 */
	public ModuleUniqueId(final UnsignedInteger typeId, final UnsignedLong instanceId) {
		if (typeId.intValue() > 65535) {
			throw new IllegalArgumentException("Illegal typeId value > 65535, only ushort values allowed");
		}
		this.typeId = typeId;
		this.instanceId = instanceId;
		this.senderProto = FrameworkProto.SenderId.newBuilder().setTypeId(typeId.shortValue()).setInstanceId(instanceId.longValue()).build();
		this.senderProtoBytes = senderProto.toByteArray();
	}
	
	public ModuleUniqueId(final byte[] senderProtoRaw) {
		try {
			senderProto = FrameworkProto.SenderId.parseFrom(senderProtoRaw);
			senderProtoBytes = senderProto.toByteArray();
			typeId = UnsignedInteger.valueOf(senderProto.getTypeId());
			instanceId = UnsignedLong.valueOf(senderProto.getInstanceId());
		} catch (final InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
	}

	public ModuleUniqueId(final FrameworkProto.SenderId senderId) {
		senderProto = senderId;
		senderProtoBytes = senderId.toByteArray();
		typeId = UnsignedInteger.valueOf(senderId.getTypeId());
		instanceId = UnsignedLong.valueOf(senderId.getInstanceId());
	}
	/**
	 * Constructor for ModuleuniqueId from a string
	 * (Added by Maksim, redo/remove if neccessary)
	 * @param data in typeId:instanceId format
	 */
	public ModuleUniqueId(String data){
		String[] parts = data.split(":");
		this.typeId = UnsignedInteger.valueOf(parts[0]);
		this.instanceId = UnsignedLong.valueOf(parts[1]);
		this.senderProto = FrameworkProto.SenderId.newBuilder().setTypeId(typeId.shortValue()).setInstanceId(instanceId.longValue()).build();
		this.senderProtoBytes = senderProto.toByteArray();
			
	}

    /**
     * @return ID describing the module type
     */
	public UnsignedInteger getTypeId() {
		return typeId;
	}

    /**
     * @return ID describing the module instance
     */
	public UnsignedLong getInstanceId() {
		return instanceId;
	}

	public byte[] getSenderProtoBytes() {
		return senderProtoBytes;
	}

	public FrameworkProto.SenderId getSenderProto() {
		return senderProto;
	}

	@Override
	public String toString() {
		return typeId.toString() + ":" + instanceId.toString();
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
		return typeId.equals(that.typeId) && instanceId.equals(that.instanceId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(typeId, instanceId);
	}

}
