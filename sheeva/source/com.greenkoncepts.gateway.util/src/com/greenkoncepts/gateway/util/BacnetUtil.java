package com.greenkoncepts.gateway.util;


public class BacnetUtil {

	public static int count;
	
	// request device property list
	public static final byte[] constructRequest1(int instanceId) {
		byte[] packet = new byte[17];
		packet[0] = (byte)0x81;
		packet[1] = (byte)0x0A;
		packet[2] = (byte)0x00;
		packet[3] = (byte)0x11;
		packet[4] = (byte)0x01;
		packet[5] = (byte)0x04;
		packet[6] = (byte)0x02;
		packet[7] = (byte)0x75;
		packet[8] = (byte)count;
		packet[9] = (byte)0x0C;
		packet[10] = (byte)0x0C;
		packet[11] = (byte)0x02;
		packet[12] = (byte)0x00;
		packet[13] = (byte)(instanceId/0x100);
		packet[14] = (byte)(instanceId%0x100);
		packet[15] = (byte)0x19;
		packet[16] = (byte)0x4C;
		return packet;
	}
	
	// request object name property
	public static final byte[] constructRequestObjectName(byte[] oid) {
		byte[] packet = new byte[17];
		packet[0] = (byte)0x81;
		packet[1] = (byte)0x0A;
		packet[2] = (byte)0x00;
		packet[3] = (byte)0x11;
		packet[4] = (byte)0x01;
		packet[5] = (byte)0x04;
		packet[6] = (byte)0x02;
		packet[7] = (byte)0x75;
		packet[8] = (byte)count;
		packet[9] = (byte)0x0C;
		packet[10] = (byte)0x0C;
		System.arraycopy(oid, 0, packet, 11, 4);
		packet[15] = (byte)0x19;
		packet[16] = (byte)0x4D;
		return packet;
	}
	
	// request present value property
	public static final byte[] constructRequestPresentValue(byte[] oid) {
		byte[] packet = new byte[17];
		packet[0] = (byte)0x81;
		packet[1] = (byte)0x0A;
		packet[2] = (byte)0x00;
		packet[3] = (byte)0x11;
		packet[4] = (byte)0x01;
		packet[5] = (byte)0x04;
		packet[6] = (byte)0x02;
		packet[7] = (byte)0x75;
		packet[8] = (byte)count;
		packet[9] = (byte)0x0C;
		packet[10] = (byte)0x0C;
		System.arraycopy(oid, 0, packet, 11, 4);
		packet[15] = (byte)0x19;
		packet[16] = (byte)0x55;
		return packet;
	}

	// request units property
	public static final byte[] constructRequestUnits(byte[] oid) {
		byte[] packet = new byte[17];
		packet[0] = (byte)0x81;
		packet[1] = (byte)0x0A;
		packet[2] = (byte)0x00;
		packet[3] = (byte)0x11;
		packet[4] = (byte)0x01;
		packet[5] = (byte)0x04;
		packet[6] = (byte)0x02;
		packet[7] = (byte)0x75;
		packet[8] = (byte)count;
		packet[9] = (byte)0x0C;
		packet[10] = (byte)0x0C;
		System.arraycopy(oid, 0, packet, 11, 4);
		packet[15] = (byte)0x19;
		packet[16] = (byte)0x75;
		return packet;
	}
	
	public static final int getFirstHeader(byte[] packet) {
		return packet[6];
	}
	
	public static final int getInvokeId(byte[] packet) {
		switch (getFirstHeader(packet)) {
		// Request
		case 0x02:
			return packet[8];
		// Response
		case 0x30:
			return packet[7];
		}
		return -1;
	}

	public static final int getObjectId(byte[] packet) {
		switch (getFirstHeader(packet)) {
		case 0x02:
			return ModbusUtil.registersBEToInt(packet, 11);
		case 0x30:
			return ModbusUtil.registersBEToInt(packet, 10);
		}
		return -1;
	}
	
	public static final int getInstanceId(byte[] packet) {
		return (getObjectId(packet) & 0x3fffff);
	}

	public static final int getObjectType(byte[] packet) {
		return ((getObjectId(packet) >> 22) & 0x3ff);
	}

	public static final int getPropertyId(byte[] packet) {
		switch (getFirstHeader(packet)) {
		case 0x02:
			return packet[16];
		case 0x30:
			return packet[15];
		}
		return -1;
	}
	
	public static final int getPropertyValuePointer(byte[] packet) {
		switch (getFirstHeader(packet)) {
		case 0x30:
			return 16;
		}
		return -1;
	}

}
