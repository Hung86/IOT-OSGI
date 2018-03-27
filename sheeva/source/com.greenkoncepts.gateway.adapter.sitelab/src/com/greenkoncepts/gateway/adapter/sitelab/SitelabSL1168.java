package com.greenkoncepts.gateway.adapter.sitelab;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class SitelabSL1168 extends SitelabDevice {
	private static final int MBREG_CONFIG1_START = 0x001D;
	private static final int MBREG_CONFIG1_NUM = 0x03;
	private static final int offErrorCode = 0;

	private static final int MBREG_CONFIG2_START = 0x003b;
	private static final int MBREG_CONFIG2_NUM = 0x05;
	private static final int offVelocityUnit = 0;
	private static final int offFlowrateUnit = 2;
	private static final int offTotalUnit = 4;

	private static final int MBREG_DATA_START = 0x0000;
	private static final int MBREG_DATA_NUM = 0x11;
	private static final int offFlowPerSecond = 0;
	private static final int offVelocity = 6;
	private static final int offNetTotal = 14;
	private static final int offNetTotalExp = 16;
	private float velocity = 0.0f;
	private float flow_rate_s = 0.0f;
	private float volume_reading = 0.0f;
	private long net_total = 0;
	private long net_total_exp = 0;
	private float pre_volume_reading = 0.0f;
	private float volume = 0.0f;
	private String velocityUnit = "";
	private String flowUnit = "";
	private String totalUnit = "";
	private String errorCode = "";

	public SitelabSL1168(int address, String category) {
		super(category, address);
	}
	@Override
	public String getDeviceData() {
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
		if (decodingData(0, data, DATA_MODE)) {
			calculateDecodedData();
		}
		return createDataSendToServer();
	}
	
	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
			decodingData(0, data, DATA_MODE);
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			item.put("Velocity", Float.toString(velocity));
			item.put("Volumetric_s", Float.toString(flow_rate_s));
			item.put("Volume_Reading", Float.toString(volume_reading));
			real_time_data.add(item);
		}
		return real_time_data;
	}
	
	@Override
	public Map<Integer, String> getDeviceConfig() {
		device_config.clear();
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG1_START, MBREG_CONFIG1_NUM);
		if (decodingData(0, data, CONFIG_MODE)) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG2_START, MBREG_CONFIG2_NUM);
			if (decodingData(1, data, CONFIG_MODE)) {
				device_config.put(40001 + MBREG_CONFIG1_START + offErrorCode, errorCode);
				device_config.put(40001 + MBREG_CONFIG2_START + offVelocityUnit, velocityUnit);
				device_config.put(40001 + MBREG_CONFIG2_START + offFlowrateUnit, flowUnit);
				device_config.put(40001 + MBREG_CONFIG2_START + offTotalUnit, totalUnit);
			}
		}
		return device_config;
	}
	
	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		return registers;
	}
	
	private boolean decodingData(int idx, byte[] data, int mode) {
		if (data == null) {
			errorCount++;
			return false;
		}

		errorCount = 0;

		if (mode == DATA_MODE) {
			flow_rate_s = ModbusUtil.ieee754RegistersToFloatLowFirst(data, ModbusUtil.MB_RESP_DATA_POS + 2 * offFlowPerSecond);
			velocity = ModbusUtil.ieee754RegistersToFloatLowFirst(data, ModbusUtil.MB_RESP_DATA_POS + 2 * offVelocity);
			net_total = ModbusUtil.registersMEToInt(data, ModbusUtil.MB_RESP_DATA_POS + 2 * offNetTotal);
			net_total_exp = ModbusUtil.registerBEToShort(data, ModbusUtil.MB_RESP_DATA_POS + 2 * offNetTotalExp);
			volume_reading = ((float) net_total) * ((float) Math.pow(10, net_total_exp));
			return true;
		}

		if (mode == CONFIG_MODE) {
			if (idx == 0) {
				errorCode = ModbusUtil.registerASCIIToString(data, ModbusUtil.MB_RESP_DATA_POS + 2 * offErrorCode, 6);
			} else if (idx == 1) {
				velocityUnit = ModbusUtil.registerASCIIToString(data, ModbusUtil.MB_RESP_DATA_POS + 2 * offVelocityUnit, 4);
				flowUnit = ModbusUtil.registerASCIIToString(data, ModbusUtil.MB_RESP_DATA_POS + 2 * offFlowrateUnit, 2);
				totalUnit = ModbusUtil.registerASCIIToString(data, ModbusUtil.MB_RESP_DATA_POS + 2 * offTotalUnit, 2);
			}
			return true;
		}

		return true;
	}
	
	private void calculateDecodedData() {
		if ((pre_volume_reading == 0) || (pre_volume_reading > volume_reading)) {
			volume = 0.0f;
		} else {
			volume = volume_reading - pre_volume_reading;
		}

		pre_volume_reading = volume_reading;
	}
	
	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		data.append("|DEVICEID=" + getId() + "-" + 0 + "-" + 0);
		data.append(";TIMESTAMP=" + System.currentTimeMillis());
		data.append(";Water Velocity=" + velocity + "," + velocityUnit);
		data.append(";Flow Rate=" + flow_rate_s + "," + flowUnit + "/s");
		data.append(";Water Volume Reading=" + volume_reading / 1000.0 + ",cu m");
		data.append(";Water Volume=" + volume / 1000.0 + ",cu m");
		return data.toString();
	}

}
