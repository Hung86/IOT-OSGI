package com.greenkoncepts.gateway.adapter.brainchild;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class Brainchild8AII extends BrainchildDevice {
	public static int MBREG_DATA_START = 1;
	public static int MBREG_DATA_NUM = 8;
	public static int MBREG_CONFIG_START = 120;
	public static int MBREG_CONFIG_NUM = 4;

	public static int CHANNEL_NUM = 8;
	final static int LOW_LIMIT = 0;
	final static int HIGH_LIMIT = 4095;

	private int[] cfData = new int[MBREG_CONFIG_NUM];

	private long readings[] = new long[CHANNEL_NUM];
	private String names[]= new String[CHANNEL_NUM];
	private float mins[] = new float[CHANNEL_NUM];
	private float maxs[] = new float[CHANNEL_NUM];
	// private int mode;
	private String units[] = new String[CHANNEL_NUM];

	public Brainchild8AII(int addr, String category) {
		super(category, addr);
		for (int i = 0; i < CHANNEL_NUM; i++) {
			readings[i] = -1;
		}
	}

	@Override
	public void setDeviceAttributes(List<Map<String, String>> attr) {
		for (int ch = 0; ch < 8 && ch < attr.size(); ch++) {
			names[ch] = attr.get(ch).get("name");
			units[ch] = attr.get(ch).get("unit");
			if ((names[ch] == null) || (names[ch].equals(""))) {
				mLogger.warn("[AI 8] Device " + modbusid + " Channel " + ch + " is not defined, IGNORE this channel");
				continue;
			}

			try {
				mins[ch] = Float.parseFloat(attr.get(ch).get("min"));
			} catch (NumberFormatException e) {
				mins[ch] = 0;
				mLogger.error("[AI 8] Device " + modbusid + " Channel " + ch + " , min value is not correct,use 0");
			}

			try {
				maxs[ch] = Float.parseFloat(attr.get(ch).get("max"));
			} catch (NumberFormatException e) {
				maxs[ch] = 0;
				mLogger.error("[AI 8] Device " + modbusid + " Channel " + ch + " , max value is not correct,use 0");
			}

			if (mins[ch] > maxs[ch]) {
				mLogger.error("[AI 8] Device " + modbusid + " Channel " + ch
						+ " , max value is less than min value,swap them");
				float tmp;
				tmp = mins[ch];
				mins[ch] = maxs[ch];
				maxs[ch] = tmp;
			} else if (mins[ch] == maxs[ch]) {
				mLogger.error("[AI 8] Device " + modbusid + " Channel " + ch
						+ " , max value equals min value, it leads the result becomes 0");
			}
		}
	}
	
	
	@Override
	public String getDeviceData() {
		byte[] data = modbus.readInputRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
		if (decodingData(0, data, DATA_MODE)) {
			calculateDecodedData();
		}
		return createDataSendToServer();
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			byte[] data = modbus.readInputRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
			decodingData(0, data, DATA_MODE);
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			for (int i = 0; i < CHANNEL_NUM; i++) {
				if ((names[i] == null) || (names[i].equals(""))) {
					continue;
				}
				Map<String, String> item = new Hashtable<String, String>();
				item.put("data_" + i, vformat.format((readings[i] * (maxs[i] - mins[i]) / HIGH_LIMIT)));
				item.put("name_" + i, names[i]);
				item.put("unit_" + i, units[i]);
				real_time_data.add(item);
			}
		}
		return real_time_data;
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		device_config.clear();
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START, MBREG_CONFIG_NUM);
		if (decodingData(0, data, CONFIG_MODE)) {

		}
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		if ((config != null) && (!config.isEmpty())) {
			byte[] data;
			byte[] results = null;

			List<Integer> cfDataRegs = new ArrayList<Integer>();
			for (Integer n : config.keySet()) {
				cfData[n - 4001 - MBREG_CONFIG_START] = Integer.parseInt(config.get(n));
				cfDataRegs.add(n);
			}

			if (!cfDataRegs.isEmpty()) {
				data = new byte[MBREG_CONFIG_START * 2];
				for (int i = 0; i < MBREG_CONFIG_START; i++) {
					byte[] temp = ModbusUtil.unsignedShortToRegister(cfData[i]);
					data[2 * i] = temp[0];
					data[2 * i + 1] = temp[1];
				}
				results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG_START, data);
				if (results != null) {
					registers.addAll(cfDataRegs);
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
			for (int i = 0; i < CHANNEL_NUM; i++) {
				if ((names[i] == null) || (names[i].equals(""))) {
					continue;
				}
				readings[i] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * i);
			}
			return true;
		}

		if (mode == CONFIG_MODE) {

			for (int i = 0; i < MBREG_CONFIG_NUM; i++) {
				cfData[i] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * i);
			}

			return true;
		}

		return true;
	}

	private void calculateDecodedData() {

	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		StringBuffer data = new StringBuffer();
		timestamp = System.currentTimeMillis();
		for (int ch = 0; ch < CHANNEL_NUM; ch++) {
			if ((names[ch] == null) || (names[ch].equals(""))) {
				continue;
			}
			data.append("|DEVICEID=" + getId() + "-" + ch + "-0");
			data.append(";TIMESTAMP=" + timestamp);
			data.append(";" + names[ch] + "=" + vformat.format(readings[ch] * (maxs[ch] - mins[ch]) / HIGH_LIMIT) + "," + units[ch]);
		}
		return data.toString();
	}

	@Override
	public String getDeviceStateData() {
		// TODO Auto-generated method stub
		return null;
	}
}
