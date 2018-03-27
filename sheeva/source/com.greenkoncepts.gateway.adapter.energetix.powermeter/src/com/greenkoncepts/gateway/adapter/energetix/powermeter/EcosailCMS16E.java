package com.greenkoncepts.gateway.adapter.energetix.powermeter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class EcosailCMS16E extends EcosailDevice {

	public static int MBREG_DATA_START = 100;
	public static int MBREG_DATA_NUM = 32;
	public static int MBREG_CONFIG_START = 501;
	public static int MBREG_CONFIG_NUM = 3;

	private final int posSlaveID = 0;
	private final int posAddress = 1;
	private final int posBaudrate = 2;
	private int[] cfData = new int[MBREG_CONFIG_NUM];

	final static int CHANNEL_NUM = 16;

	private long preReading[] = new long[CHANNEL_NUM]; // Wh
	private long reading[] = new long[CHANNEL_NUM]; // Wh
	private int energy[] = new int[CHANNEL_NUM]; // Wh

	public EcosailCMS16E(int addr, String category) {
		super(category, addr);
		for (int i = 0; i < CHANNEL_NUM; i++) {
			preReading[i] = 0;
			reading[i] = 0;
			energy[i] = 0;
		}
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
			for (int i = 0; i < CHANNEL_NUM; i++) {
				Map<String, String> item = new Hashtable<String, String>();
				item.put("kWh_" + i, vformat.format(reading[i] / 1000.0));
				real_time_data.add(item);
			}
		}
		return real_time_data;
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START, MBREG_CONFIG_NUM);
		device_config.clear();
		if (decodingData(0, data, CONFIG_MODE)) {
			device_config.put(MBREG_CONFIG_START + posSlaveID, String.valueOf(cfData[posSlaveID]));
			device_config.put(MBREG_CONFIG_START + posAddress, String.valueOf(cfData[posAddress]));
			device_config.put(MBREG_CONFIG_START + posBaudrate, String.valueOf(cfData[posBaudrate]));
		}
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		if ((config != null) && (!config.isEmpty())) {
			for (Integer n : config.keySet()) {
				cfData[n - MBREG_CONFIG_START] = Integer.parseInt(config.get(n));
			}
			byte[] data = new byte[cfData.length * 2];
			for (int i = 0; i < cfData.length; i++) {
				byte[] temp = ModbusUtil.unsignedShortToRegister(cfData[i]);
				data[2 * i] = temp[0];
				data[2 * i + 1] = temp[1];
			}
			byte[] results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG_START, data);
			if (results != null) {
				registers.addAll(config.keySet());
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
				reading[i] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 4 * i);
			}
			return true;
		}
		if (mode == CONFIG_MODE) {
			cfData[posSlaveID] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posSlaveID);
			cfData[posAddress] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posAddress);
			cfData[posBaudrate] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posBaudrate);
			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		for (int i = 0; i < CHANNEL_NUM; i++) {
			if ((preReading[i] == 0) || (preReading[i] > reading[i])) {
				energy[i] = 0;
			} else {
				energy[i] = (int) (reading[i] - preReading[i]);
			}
			preReading[i] = reading[i];
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
			data.append(";Active Energy Reading=" + (reading[ch] / 1000.0) + ",kWh");
			data.append(";Active Energy=" + energy[ch] + ",Wh");
		}
		return data.toString();
	}

}
