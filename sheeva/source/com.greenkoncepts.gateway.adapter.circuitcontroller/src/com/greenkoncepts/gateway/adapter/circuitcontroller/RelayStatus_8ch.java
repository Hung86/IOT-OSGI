package com.greenkoncepts.gateway.adapter.circuitcontroller;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.FuncUtil;

public class RelayStatus_8ch extends CircuitControllerDevice {
	/*
	 * register address 01 02 03
	 * baudrate : 9600
	 * stopbit : 1
	 */
	public static int OPEN_STATE = 0x100;
	public static int CLOSE_STATE = 0x200;
	public static int TOGGLE_STATE = 0x300;
	public static int LATCH_STATE = 0x400;
	public static int MOMENTARY_STATE = 0x500;
	
	public static final int MBREG_DATA_START = 1;
	public static final int MBREG_DATA_NUM = 8;
	public static final int MBREG_CONFIG_START = 1;
	public static final int MBREG_CONFIG_NUM = 8;
	
	public static final int CHANNEL = 8;
	
	private int reg1 = -1;
	private int reg2 = -1;
	private int reg3 = -1;
	private int reg4 = -1;
	private int reg5 = -1;
	private int reg6 = -1;
	private int reg7 = -1;
	private int reg8 = -1;
	
	private int cf_reg1 = -1;
	private int cf_reg2 = -1;
	private int cf_reg3 = -1;
	private int cf_reg4 = -1;
	private int cf_reg5 = -1;
	private int cf_reg6 = -1;
	private int cf_reg7 = -1;
	private int cf_reg8 = -1;

	public RelayStatus_8ch(int id, String cat) {
		super(id, cat);
	}

	@Override
	public String getDeviceData() {
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
		decodingData(0, data, DATA_MODE);
		return createDataSendToServer();
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		device_config.clear();
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
		if (decodingData(0, data, CONFIG_MODE)) {
			device_config.put(1, String.valueOf(cf_reg1));
			device_config.put(2, String.valueOf(cf_reg2));
			device_config.put(3, String.valueOf(cf_reg3));
			device_config.put(4, String.valueOf(cf_reg4));
			device_config.put(5, String.valueOf(cf_reg5));
			device_config.put(6, String.valueOf(cf_reg6));
			device_config.put(7, String.valueOf(cf_reg7));
			device_config.put(8, String.valueOf(cf_reg8));
		}
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> regs = new ArrayList<Integer>();
		for (Integer idx : config.keySet()) {
			int val = CLOSE_STATE;
			if ("1".equals(config.get(idx))) {
				val = OPEN_STATE;
			} else if ("0".equals(config.get(idx))) {
				val = CLOSE_STATE;
			}
			byte[] data = FuncUtil.RegisterBigEndian.unsignedShortToRegister(val);
			if (modbus.writeSingleRegister(modbusid, idx, data) != null) {
				regs.add(idx);
			}
		}
		return regs;
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx,
			boolean isRefesh) {
		if (isRefesh) {
			byte[] data = modbus.readInputRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
			decodingData(0, data, DATA_MODE);
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			item.put("reg_1", String.valueOf(reg1));
			item.put("reg_2", String.valueOf(reg2));
			item.put("reg_3", String.valueOf(reg3));
			item.put("reg_4", String.valueOf(reg4));
			item.put("reg_5", String.valueOf(reg5));
			item.put("reg_6", String.valueOf(reg6));
			item.put("reg_7", String.valueOf(reg7));
			item.put("reg_8", String.valueOf(reg8));
			real_time_data.add(item);
		}
		return real_time_data;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean setNodeValue(Object data) {
		Map<Integer, String> config = (Map<Integer, String>) data;
		if (!setDeviceConfig(config).isEmpty()) {
			return true;
		}
		return false;
	}
	
	private boolean decodingData(int idx, byte[] data, int mode) {
		if (data == null) {
			errorCount++;
			return false;
		}

		errorCount = 0;

		if (mode == DATA_MODE) {
			reg1 = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*0);
			reg2 = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*1);
			reg3 = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*2);
			reg4 = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*3);
			reg5 = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*4);
			reg6 = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*5);
			reg7 = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*6);
			reg8 = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*7);
			return true;
		}

		if (mode == CONFIG_MODE) {
			cf_reg1 = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*0);
			cf_reg2 = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*1);
			cf_reg3 = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*2);
			cf_reg4 = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*3);
			cf_reg5 = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*4);
			cf_reg6 = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*5);
			cf_reg7 = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*6);
			cf_reg8 = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*7);
			return true;
		}

		return false;
	}
	
	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		sb.append("|DEVICEID=" + getId() + "-1-0");
		sb.append(";TIMESTAMP=" + timestamp);
		sb.append(";Relay Status=" + reg1 + ",None");

		sb.append("|DEVICEID=" + getId() + "-2-0");
		sb.append(";TIMESTAMP=" + timestamp);
		sb.append(";Relay Status=" + reg2 + ",None");
		
		sb.append("|DEVICEID=" + getId() + "-3-0");
		sb.append(";TIMESTAMP=" + timestamp);
		sb.append(";Relay Status=" + reg3 + ",None");
		
		sb.append("|DEVICEID=" + getId() + "-4-0");
		sb.append(";TIMESTAMP=" + timestamp);
		sb.append(";Relay Status=" + reg4 + ",None");
		
		sb.append("|DEVICEID=" + getId() + "-5-0");
		sb.append(";TIMESTAMP=" + timestamp);
		sb.append(";Relay Status=" + reg5 + ",None");
		
		sb.append("|DEVICEID=" + getId() + "-6-0");
		sb.append(";TIMESTAMP=" + timestamp);
		sb.append(";Relay Status=" + reg6 + ",None");
		
		sb.append("|DEVICEID=" + getId() + "-7-0");
		sb.append(";TIMESTAMP=" + timestamp);
		sb.append(";Relay Status=" + reg7 + ",None");
		
		sb.append("|DEVICEID=" + getId() + "-8-0");
		sb.append(";TIMESTAMP=" + timestamp);
		sb.append(";Relay Status=" + reg8 + ",None");
		
		return sb.toString();
	}

}
