package com.greenkoncepts.gateway.adapter.energetix.sensor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class MbsensorLux extends MbsensorDevice {

	private DecimalFormat vformat = new DecimalFormat("#####0.0000");
	static public int MBREG_CONFIG_START = 0x0001;
	static public int MBREG_CONFIG_NUM = 0x01;
	private short cfModbusId = 0;

	static public int MBREG_DATA_START = 0x07;
	static public int MBREG_DATA_NUM = 0x01;

	static public int CHANNEL_NUM = 1;

	private int value[] = new int[CHANNEL_NUM]; //

	public MbsensorLux(int addr, String category) {
		super(category, addr);
		for (int i = 0; i < CHANNEL_NUM; i++) {
			value[i] = -100;
		}
	}

	@Override
	public String getDeviceData() {
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
		decodingData(0, data, DATA_MODE);
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
			for (int i = 0; i < CHANNEL_NUM; i++) {
				Map<String, String> item = new Hashtable<String, String>();
				item.put("lux_" + i, vformat.format(value[i]));
				real_time_data.add(item);
			}
		}
		return real_time_data;
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		byte[] data = modbus.readHoldingRegisters(0, MBREG_CONFIG_START, MBREG_CONFIG_NUM);
		if (decodingData(0, data, CONFIG_MODE)) {
			device_config.put(MBREG_CONFIG_START, String.valueOf(cfModbusId));
		} else {
			device_config.clear();
		}
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		if (config != null) {
			short value = Short.parseShort(config.get(MBREG_CONFIG_START));
			byte[] data = ModbusUtil.shortToRegister(value);
			byte[] results = modbus.writeMultipleRegisters(0, MBREG_CONFIG_START, data);
			if (results != null) {
				registers.add(MBREG_CONFIG_START);
			}
		}

		return registers;
	}

	private boolean decodingData(int idx, byte[] data, int mode) {
		if (data == null) {
			errorCount++;
			return false;
		}
		errorCount = 0;
		if (mode == DATA_MODE) {
			for (int i = 0; i < CHANNEL_NUM; i++) {
				value[i] = ModbusUtil.registerToShort(data, OFFSET_DATA + 4 * i);
			}
			return true;
		}
		if (mode == CONFIG_MODE) {
			cfModbusId = (short) ModbusUtil.registerToShort(data, OFFSET_DATA + 0);
			return true;
		}

		return true;
	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		StringBuffer data = new StringBuffer();
		timestamp = System.currentTimeMillis();

		for (int ch = 0; ch < CHANNEL_NUM; ch++) {
			data.append("|DEVICEID=" + getId() + "-" + ch + "-0");
			data.append(";TIMESTAMP=" + System.currentTimeMillis());
			data.append(";Lux=" + value[ch] + ",lux");
		}
		return data.toString();
	}

}
