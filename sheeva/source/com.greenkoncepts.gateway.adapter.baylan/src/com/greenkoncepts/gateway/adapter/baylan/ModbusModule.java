package com.greenkoncepts.gateway.adapter.baylan;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class ModbusModule extends BaylanDevice {
	public static float SCALAR_VOLUME = 0.1f;
	public static int MBREG_DATA_START = 0x0002;
	public static int MBREG_DATA_NUM = 0x0C;
	public static int MBREG_CONFIG_START = 0x0002;
	public static int MBREG_CONFIG_NUM = 13;
	public static int posLowData = 0;
	public static int posHighData = 1;
	public static int posSlaveID = 2;
	public static int posInterInfo = 3;
	public static int posSecond = 4;
	public static int posMinute = 5;
	public static int posHour = 6;
	public static int posDay = 7;
	public static int posMonth = 8;
	public static int posYear = 9;
	public static int posMaxFlowRate = 10;
	public static int posBatteryStatus = 11;
	public static int posVersion = 12;

	private int[] cfData = new int[MBREG_CONFIG_NUM];

	private float waterVolumeReading = 0.0f;
	private float waterVolume = 0.0f;
	private float prevWaterVolumeReading = 0.0f;
	private int batteryLevel = 0;
	private float batteryLevelPercentage = 0.0f;

	public ModbusModule(int address, String category) {
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
			item.put("waterVolumeReading", vformat.format(waterVolumeReading));
			item.put("batteryLevel", vformat.format(batteryLevelPercentage));
			real_time_data.add(item);
		}
		return real_time_data;
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START, MBREG_CONFIG_NUM);
		device_config.clear();
		if (decodingData(0, data, CONFIG_MODE)) {
			byte[] kwL1lowReg = ModbusUtil.unsignedShortToRegister(cfData[posLowData]);
			byte[] kwL1highReg = ModbusUtil.unsignedShortToRegister(cfData[posHighData]);
			float result = ModbusUtil.ieee754RegistersToFloat(kwL1highReg, kwL1lowReg) * SCALAR_VOLUME;
			device_config.put(MBREG_CONFIG_START + posLowData, String.valueOf(result));
			device_config.put(MBREG_CONFIG_START + posSlaveID, String.valueOf(cfData[posSlaveID]));
			device_config.put(MBREG_CONFIG_START + posInterInfo, String.valueOf(cfData[posInterInfo]));
			device_config.put(MBREG_CONFIG_START + posSecond, String.valueOf(cfData[posSecond]));
			device_config.put(MBREG_CONFIG_START + posMinute, String.valueOf(cfData[posMinute]));
			device_config.put(MBREG_CONFIG_START + posHour, String.valueOf(cfData[posHour]));
			device_config.put(MBREG_CONFIG_START + posDay, String.valueOf(cfData[posDay]));
			device_config.put(MBREG_CONFIG_START + posMonth, String.valueOf(cfData[posMonth]));
			device_config.put(MBREG_CONFIG_START + posYear, String.valueOf(cfData[posYear]));
			device_config.put(MBREG_CONFIG_START + posMaxFlowRate, String.valueOf(cfData[posMaxFlowRate]));
			device_config.put(MBREG_CONFIG_START + posBatteryStatus, String.valueOf(cfData[posBatteryStatus]));
			device_config.put(MBREG_CONFIG_START + posVersion, String.valueOf(cfData[posVersion]));
		}
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		if ((config != null) && (!config.isEmpty())) {
			byte[] results = null;
			byte[] data;
			for (Integer n : config.keySet()) {
				if (n ==  MBREG_CONFIG_START ) {
					float consumption = Float.parseFloat(config.get(n));
					consumption = consumption / SCALAR_VOLUME;
					data = new byte[4];
					byte tmp[] = ModbusUtil.floatToRegisters(consumption);
					data[0] = tmp[2];
					data[1] = tmp[3];
					data[2] = tmp[0];
					data[3] = tmp[1];
				} else {
					cfData[n - MBREG_CONFIG_START] = Integer.parseInt(config.get(n));
					int val = Integer.parseInt(config.get(n));
					data = ModbusUtil.unsignedShortToRegister(val);
				}
				results = modbus.writeMultipleRegisters(modbusid, n, data);
				if (results != null) {
					registers.add(n);
				}
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
			waterVolumeReading = ModbusUtil.ieee754RegistersToFloatLowFirst(data, OFFSET_DATA) * SCALAR_VOLUME;
			batteryLevel = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * 11);
			batteryLevelPercentage = ((float)batteryLevel / (float) 255) * 100; 
			return true;
		}
		if (mode == CONFIG_MODE) {
			cfData[posLowData] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posLowData);
			cfData[posHighData] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posHighData);
			cfData[posSlaveID] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posSlaveID);
			cfData[posInterInfo] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posInterInfo);
			cfData[posSecond] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posSecond);
			cfData[posMinute] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posMinute);
			cfData[posHour] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posHour);
			cfData[posDay] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posDay);
			cfData[posMonth] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posMonth);
			cfData[posYear] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posYear);
			cfData[posMaxFlowRate] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posMaxFlowRate);
			cfData[posBatteryStatus] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posBatteryStatus);
			cfData[posVersion] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posVersion);
			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		if ((prevWaterVolumeReading == 0) || (prevWaterVolumeReading > waterVolumeReading)) {
			waterVolume = 0.0f;
		} else {
			waterVolume = waterVolumeReading - prevWaterVolumeReading;
		}
		prevWaterVolumeReading = waterVolumeReading;
	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		data.append("|DEVICEID=" + getId() + "-0-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Water Volume Reading=" + vformat.format(waterVolumeReading) + ",cu m");
		data.append(";Water Volume=" + vformat.format(waterVolume) + ",cu m");
		data.append(";Battery Level=" + vformat.format(batteryLevelPercentage) + ",%");
		return data.toString();
	}

}
