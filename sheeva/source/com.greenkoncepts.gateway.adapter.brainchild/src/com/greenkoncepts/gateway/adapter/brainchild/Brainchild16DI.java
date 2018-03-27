package com.greenkoncepts.gateway.adapter.brainchild;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class Brainchild16DI extends BrainchildDevice {

	public static int MBREG_DATA_START = 2;
	public static int MBREG_DATA_NUM = 32;

	public static int MBREG_CONFIG1_START = 2;
	public static int MBREG_CONFIG1_NUM = 32;
	public static int MBREG_CONFIG2_START = 100;
	public static int MBREG_CONFIG2_NUM = 3;
	public static int MBREG_CONFIG3_START = 120;
	public static int MBREG_CONFIG3_NUM = 4;

	public static int CHANNEL_NUM = 16;

	private long[] cfCounter = new long[CHANNEL_NUM];
	private int[] cfData1 = new int[MBREG_CONFIG2_NUM];
	private int[] cfData2 = new int[MBREG_CONFIG3_NUM];
	
	private long counter_reading[] = new long[CHANNEL_NUM];
	private long prev_counter_reading[] = new long[CHANNEL_NUM];
	private long counter[] = new long[CHANNEL_NUM];
	private String names[] = new String[CHANNEL_NUM];
	private float ratios[] = new float[CHANNEL_NUM];
	private String units[] = new String[CHANNEL_NUM];
	private int extra_ratio[] = new int[CHANNEL_NUM];

	public Brainchild16DI(int addr, String category) {
		super(category, addr);
		for (int i = 0; i < CHANNEL_NUM; i++) {
			counter_reading[i] = 0;
			prev_counter_reading[i] = 0;
			counter[i] = 0;
		}

	}

	@Override
	public void setDeviceAttributes(List<Map<String, String>> attr) {
		for (int ch = 0; ch < CHANNEL_NUM && ch < attr.size(); ch++) {
			names[ch] = attr.get(ch).get("name");
			units[ch] = attr.get(ch).get("unit");
			if ((names[ch] == null) || (names[ch].equals(""))) {
				mLogger.warn("[DI 16] Device " + modbusid + " Channel " + ch
						+ " is not defined, IGNORE this channel");
				continue;
			}
			if ("cu m".equals(units[ch])) {
				extra_ratio[ch] = 1000;
			} else {
				// for liter unit
				extra_ratio[ch] = 1;
			}
			try {
				ratios[ch] = Float.parseFloat(attr.get(ch).get("ratio"));
			} catch (NumberFormatException e) {
				ratios[ch] = 1f;
				mLogger.error("[DI 16] Device " + modbusid + " Channel " + ch + " uses default ratio = 1");
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
				item.put("data_" + i, vformat.format(counter_reading[i] * ratios[i] / extra_ratio[i]));
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
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG1_START, MBREG_CONFIG1_NUM);
		if (decodingData(0, data, CONFIG_MODE)) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG2_START, MBREG_CONFIG2_NUM);
			if (decodingData(1, data, CONFIG_MODE)) {
				data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG3_START, MBREG_CONFIG3_NUM);
				if (decodingData(2, data, CONFIG_MODE)) {
					for (int i = 0; i < CHANNEL_NUM; i++) {
						device_config.put(40001 + MBREG_CONFIG1_START + 2*i, String.valueOf(cfCounter[i]));
					}

					for (int i = 0; i < MBREG_CONFIG2_NUM; i++) {
						device_config.put(40001 + MBREG_CONFIG2_START + i, String.valueOf(cfData1[i]));
					}

					for (int i = 0; i < MBREG_CONFIG3_NUM; i++) {
						device_config.put(40001 + MBREG_CONFIG3_START + i, String.valueOf(cfData2[i]));
					}
				}

			}
		}
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		if ((config != null) && (!config.isEmpty())) {
			byte[] data;
			byte[] results = null;

			List<Integer> cfCounterRegs = new ArrayList<Integer>();
			List<Integer> cfData1Regs = new ArrayList<Integer>();
			List<Integer> cfData2Regs = new ArrayList<Integer>();
			
			for (Integer n : config.keySet()) {
				if (n < (40001 + MBREG_CONFIG1_START + MBREG_CONFIG1_NUM)) {
					int idx = (n - 40001 - MBREG_CONFIG1_START)/2;
					cfCounter[idx] = Long.parseLong(config.get(n));
					cfCounterRegs.add(n);
				} else if (n < (40001 + MBREG_CONFIG2_START + MBREG_CONFIG2_NUM)) {
					cfData1[n - 40001 - MBREG_CONFIG2_START] = Integer.parseInt(config.get(n));
					cfData1Regs.add(n);
				} else {
					cfData1[n - 40001 - MBREG_CONFIG3_START] = Integer.parseInt(config.get(n));
					cfData2Regs.add(n);
				}

			}
			if (!cfCounterRegs.isEmpty()) {
				data = new byte[CHANNEL_NUM * 4];
				for (int i = 0; i < CHANNEL_NUM; i++) {
					byte[] temp = ModbusUtil.uintToRegisters(cfCounter[i]);
					data[4 * i] = temp[0];
					data[4 * i + 1] = temp[1];
					data[4 * i + 2] = temp[2];
					data[4 * i + 3] = temp[3];
				}
				results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG1_START, data);
				if (results != null) {
					registers.addAll(cfCounterRegs);
				}
			}

			if (!cfData1Regs.isEmpty()) {
				data = new byte[MBREG_CONFIG2_NUM * 2];
				for (int i = 0; i < MBREG_CONFIG2_NUM; i++) {
					byte[] temp = ModbusUtil.unsignedShortToRegister(cfData1[i]);
					data[2 * i] = temp[0];
					data[2 * i + 1] = temp[1];
				}
				results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG2_START, data);
				if (results != null) {
					registers.addAll(cfData1Regs);
				}
			}

			if (!cfData2Regs.isEmpty()) {
				data = new byte[MBREG_CONFIG3_NUM * 2];
				for (int i = 0; i < MBREG_CONFIG3_NUM; i++) {
					byte[] temp = ModbusUtil.unsignedShortToRegister(cfData2[i]);
					data[2 * i] = temp[0];
					data[2 * i + 1] = temp[1];
				}
				results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG2_START, data);
				if (results != null) {
					registers.addAll(cfData2Regs);
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
				counter_reading[i] = ModbusUtil.registersBEToInt(data, OFFSET_DATA + 4 * i);
			}
			return true;
		}

		if (mode == CONFIG_MODE) {
			if (idx == 0) {
				for (int i = 0; i < CHANNEL_NUM; i++) {
					cfCounter[i] = ModbusUtil.registersBEToInt(data, OFFSET_DATA + 4 * i);
				}
			} else if (idx == 1) {
				for (int i = 0; i < MBREG_CONFIG2_NUM; i++) {
					cfData1[i] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * i);
				}
			} else if (idx == 2) {
				for (int i = 0; i < MBREG_CONFIG3_NUM; i++) {
					cfData2[i] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * i);
				}
			}
			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		for (int i = 0; i < CHANNEL_NUM; i++) {
			if ((names[i] == null) || (names[i].equals(""))) {
				continue;
			}
			if ((prev_counter_reading[i] == 0) || (prev_counter_reading[i] > counter_reading[i])) {
				counter[i] = 0;
			} else {
				counter[i] = (int) (counter_reading[i] - prev_counter_reading[i]);
			}
			prev_counter_reading[i] = counter_reading[i];
		}
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
			data.append(";" + names[ch] + " Reading=" + vformat.format(counter_reading[ch] * ratios[ch] / extra_ratio[ch]) + "," + units[ch]);
			data.append(";" + names[ch] + "=" + vformat.format(counter[ch] * ratios[ch] / extra_ratio[ch]) + "," + units[ch]);
			data.append(";" + "Flow Rate" + "=" + vformat.format((counter[ch] * ratios[ch]) / 60) + ",l/s");
			// data.append(";\n");
		}
		return data.toString();
	}

	@Override
	public String getDeviceStateData() {
		// TODO Auto-generated method stub
		return null;
	}
}