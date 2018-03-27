package com.greenkoncepts.gateway.adapter.energetix.sensor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class Mbsensor1TH extends MbsensorDevice {
	private DecimalFormat vformat = new DecimalFormat("#####0.0000");
	static public int CHANNEL_NUM = 1;

	static public int MBREG_CONFIG_START = 0x0001;
	static public int MBREG_CONFIG_NUM = 0x01;
	private short cfModbusId = 0;

	static public int MBREG_DATA_START = 0x05;
	static public int MBREG_DATA_NUM = 0x02;

	private int temperature[] = new int[CHANNEL_NUM];
	private int humidity[] = new int[CHANNEL_NUM];

	private int decodedTemperature[] = new int[CHANNEL_NUM];
	private int decodedHumidity[] = new int[CHANNEL_NUM];

	public Mbsensor1TH(int addr, String category) {
		super(category, addr);
		for (int i = 0; i < CHANNEL_NUM; i++) {
			temperature[i] = 0;
			humidity[i] = 0;
		}
	}

	@Override
	public String getDeviceData() {
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
		if (decodingData(0, data, DATA_MODE)) {
			calculateDecodedData();
		}
		if (!validData) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
			if (decodingData(0, data, DATA_MODE)) {
				calculateDecodedData();
			}
		}
		return createDataSendToServer();
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
			if (decodingData(0, data, DATA_MODE)) {
				for (int i = 0; i < CHANNEL_NUM; i++) {
					temperature[i] = decodedTemperature[i];
					humidity[i] = decodedHumidity[i];
				}
			}
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			for (int i = 0; i < CHANNEL_NUM; i++) {
				Map<String, String> item = new Hashtable<String, String>();
				item.put("temperature_" + i, vformat.format(temperature[i] / 10.0));
				item.put("humidity_" + i, vformat.format(humidity[i] / 10.0));
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
				decodedTemperature[i] = ModbusUtil.registerToShort(data, OFFSET_DATA + 4 * i);
				decodedHumidity[i] = ModbusUtil.registerToShort(data, OFFSET_DATA + 4 * i + 2);
			}
			return true;
		}
		if (mode == CONFIG_MODE) {
			cfModbusId = (short) ModbusUtil.registerToShort(data, OFFSET_DATA + 0);
			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		if (validData) {
			for (int i = 0; i < CHANNEL_NUM; i++) {
				if (decodedTemperature[i] >= INVALID_TEMP) {
					temperature[i] = 0;
					humidity[i] = 0;
					continue;
				}
				humidity[i] = decodedHumidity[i];
				if ((Math.abs(decodedTemperature[i] - temperature[i]) < (temperature[i] / 2))
						|| ((temperature[i] == 0) && (decodedTemperature[i] < 400))) {
					temperature[i] = decodedTemperature[i];
				} else {
					temperature[i] = -999;
					if (validData) {
						validData = false;// need to get data again
					}
				}
			}
		} else {
			validData = true;
			pikeCount++;
			for (int i = 0; i < CHANNEL_NUM; i++) {
				if (decodedTemperature[i] >= INVALID_TEMP) {
					temperature[i] = 0;
					humidity[i] = 0;
					continue;
				}
				if (temperature[i] == -999) {
					temperature[i] = decodedTemperature[i];
				}
			}
			mLogger.error("Device " + modbusid + " has pike count " + pikeCount);
		}
	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		for (int ch = 0; ch < CHANNEL_NUM; ch++) {
			data.append("|DEVICEID=" + getId() + "-" + ch + "-0");
			data.append(";TIMESTAMP=" + timestamp);
			data.append(";Temperature=" + (temperature[ch] / 10.0) + ",C");
			data.append(";Humidity=" + humidity[ch] / 10.0 + ",%");
		}
		return data.toString();
	}

}
