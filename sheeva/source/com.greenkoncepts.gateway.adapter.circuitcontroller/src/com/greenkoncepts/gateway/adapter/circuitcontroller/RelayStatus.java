package com.greenkoncepts.gateway.adapter.circuitcontroller;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.FuncUtil;

public class RelayStatus extends CircuitControllerDevice {
	/*
	 * register address 01 02 03
	 * baudrate : 9600
	 * stopbit : 1
	 */
	public static final int MBREG_DATA_START = 0;
	public static final int MBREG_DATA_NUM = 3;
	public static final int MBREG_CONFIG_START = 0;
	public static final int MBREG_CONFIG_NUM = 3;
	
	public static final int CHANNEL = 3;
	
	private int reg1 = -1;
	private int reg2 = -1;
	private int reg3 = -1;
	
	private int cf_reg1 = -1;
	private int cf_reg2 = -1;
	private int cf_reg3 = -1;

	public RelayStatus(int id, String cat) {
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
			device_config.put(0, String.valueOf(cf_reg1));
			device_config.put(1, String.valueOf(cf_reg2));
			device_config.put(2, String.valueOf(cf_reg3));
		}
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> regs = new ArrayList<Integer>();
		for (Integer idx : config.keySet()) {
			int val = Integer.valueOf(config.get(idx));
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
			reg1 = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*0);
			reg2 = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*1);
			reg3 = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*2);
			return true;
		}

		if (mode == CONFIG_MODE) {
			cf_reg1 = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*0);
			cf_reg2 = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*1);
			cf_reg3 = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*2);
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
		sb.append("|DEVICEID=" + getId() + "-0-0");
		sb.append(";TIMESTAMP=" + timestamp);
		sb.append(";Relay Status=" + reg1 + ",None");

		sb.append("|DEVICEID=" + getId() + "-1-0");
		sb.append(";TIMESTAMP=" + timestamp);
		sb.append(";Relay Status=" + reg2 + ",None");
		
		sb.append("|DEVICEID=" + getId() + "-2-0");
		sb.append(";TIMESTAMP=" + timestamp);
		sb.append(";Relay Status=" + reg3 + ",None");
		
		return sb.toString();
	}

}
