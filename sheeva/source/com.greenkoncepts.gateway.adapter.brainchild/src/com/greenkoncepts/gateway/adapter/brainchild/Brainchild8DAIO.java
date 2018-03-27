package com.greenkoncepts.gateway.adapter.brainchild;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.expression.GKEP;
import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class Brainchild8DAIO extends BrainchildDevice {
	private static int MBREG_DATA_START_0 = 03;
	private static int MBREG_DATA_START_1 = 100;
	private static int MBREG_DATA_START_2 = 120;
	private static int MBREG_DATA_NUM_0 = 13;
	private static int MBREG_DATA_NUM_1 = 10;
	private static int MBREG_DATA_NUM_2 = 4;

	private static int CHANNEL_NUM = 8;

	private long[] inputCounter = new long[CHANNEL_NUM];
	private short[] posInput = { 0, 1, 2, 3, 5, 7, 9, 11 };

	private long[] cfData0 = new long[CHANNEL_NUM];
	private int[] cfData1 = new int[MBREG_DATA_NUM_1];
	private int[] cfData2 = new int[MBREG_DATA_NUM_2];

	private String names[] = new String[CHANNEL_NUM];
	private String units[] = new String[CHANNEL_NUM];
	private String formula[] = new String[CHANNEL_NUM];
	private double[] calcInputCounter = new double[CHANNEL_NUM];
	
	private GKEP parser;

	public Brainchild8DAIO(int addr, String category) {
		super(category, addr);
		
		parser = new GKEP();
		parser.setAllowUndeclared(true);

		for (int ch = 0; ch < CHANNEL_NUM; ch++) {
			calcInputCounter[ch] = 0;
		}
	}

	@Override
	public void setDeviceAttributes(List<Map<String, String>> attr) {
		for (int ch = 0; ch < 8; ch++) {
			names[ch] = attr.get(ch).get("name");
			formula[ch] = attr.get(ch).get("formula");
			units[ch] = attr.get(ch).get("unit");
			if ((names[ch] == null) || (names[ch].equals(""))) {
				mLogger.warn("[DAIO 8] Device " + modbusid + " Channel " + ch
						+ " is not defined, IGNORE this channel");
			}

		}
	}

	@Override
	public String getDeviceData() {
		byte[] data = modbus.readInputRegisters(modbusid, MBREG_DATA_START_0, MBREG_DATA_NUM_0);
		if (decodingData(0, data, DATA_MODE)) {
			calculateDecodedData();
		}
		return createDataSendToServer(data);
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		device_config.clear();
		// First group Register
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START_0, MBREG_DATA_NUM_0);
		if (decodingData(0, data, CONFIG_MODE)) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START_1, MBREG_DATA_NUM_1);
			if (decodingData(1, data, CONFIG_MODE)) {
				data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START_2, MBREG_DATA_NUM_2);
				if (decodingData(2, data, CONFIG_MODE)) {
					for (int i = 0; i < CHANNEL_NUM; i++) {
						device_config.put(40001 + MBREG_DATA_START_0 + posInput[i], String.valueOf(cfData0[i]));
					}

					for (int i = 0; i < MBREG_DATA_NUM_1; i++) {
						device_config.put(40001 + MBREG_DATA_START_1 + i, String.valueOf(cfData1[i]));
					}

					for (int i = 0; i < MBREG_DATA_NUM_2; i++) {
						device_config.put(40001 + MBREG_DATA_START_2 + i, String.valueOf(cfData2[i]));
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

			List<Integer> cfData0Regs = new ArrayList<Integer>();
			List<Integer> cfData1Regs = new ArrayList<Integer>();
			List<Integer> cfData2Regs = new ArrayList<Integer>();

			for (Integer n : config.keySet()) {
				if (n < (40001 + MBREG_DATA_START_1)) {
					int idx = 4 + (n - 40009) / 2;
					cfData0[idx] = Long.parseLong(config.get(n));
					cfData0Regs.add(n);
				} else if (n < (40001 + MBREG_DATA_START_2)) {
					cfData1[n - 40001 - MBREG_DATA_START_1] = Integer.parseInt(config.get(n));
					cfData1Regs.add(n);
				} else {
					cfData1[n - 40001 - MBREG_DATA_START_2] = Integer.parseInt(config.get(n));
					cfData2Regs.add(n);
				}

			}
			if (!cfData0Regs.isEmpty()) {
				data = new byte[16];
				for (int i = 4; i < 8; i++) {
					byte[] temp = ModbusUtil.uintToRegisters(cfData0[i]);
					data[4 * (i-4)] = temp[0];
					data[4 * (i-4) + 1] = temp[1];
					data[4 * (i-4) + 2] = temp[2];
					data[4 * (i-4) + 3] = temp[3];
				}
				results = modbus.writeMultipleRegisters(modbusid, 8, data);
				if (results != null) {
					registers.addAll(cfData0Regs);
				}
			}

			if (!cfData1Regs.isEmpty()) {
				data = new byte[MBREG_DATA_NUM_1 * 2];
				for (int i = 0; i < MBREG_DATA_NUM_1; i++) {
					byte[] temp = ModbusUtil.unsignedShortToRegister(cfData1[i]);
					data[2 * i] = temp[0];
					data[2 * i + 1] = temp[1];
				}
				results = modbus.writeMultipleRegisters(modbusid, MBREG_DATA_START_1, data);
				if (results != null) {
					registers.addAll(cfData1Regs);
				}
			}

			if (!cfData2Regs.isEmpty()) {
				data = new byte[MBREG_DATA_NUM_2 * 2];
				for (int i = 0; i < MBREG_DATA_NUM_2; i++) {
					byte[] temp = ModbusUtil.unsignedShortToRegister(cfData2[i]);
					data[2 * i] = temp[0];
					data[2 * i + 1] = temp[1];
				}
				results = modbus.writeMultipleRegisters(modbusid, MBREG_DATA_START_2, data);
				if (results != null) {
					registers.addAll(cfData2Regs);
				}
			}

		}
		return registers;
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			byte[] data = modbus.readInputRegisters(modbusid, MBREG_DATA_START_0, MBREG_DATA_NUM_0);
			if (decodingData(0, data, DATA_MODE)) {
				calculateDecodedData();
			}
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			for (int i = 0; i < CHANNEL_NUM; i++) {
				if ((names[i] == null) || (names[i].equals(""))) {
					continue;
				}
				Map<String, String> item = new Hashtable<String, String>();
				item.put("data_" + i, vformat.format(inputCounter[i]));
				item.put("data_user_" + i, vformat.format(calcInputCounter[i]));
				item.put("name_" + i, names[i]);
				item.put("unit_" + i, units[i]);
				real_time_data.add(item);
			}
		}
		return real_time_data;
	}

	private String createDataSendToServer(byte[] dataBinary) {

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
			data.append(";" + names[ch] + "=" + vformat.format(calcInputCounter[ch]) + "," + units[ch]);
		}
		return data.toString();

	}

	private boolean decodingData(int idx, byte[] data, int mode) {
		if (data == null) {
			errorCount++;
			return false;
		}

		errorCount = 0;
		if (mode == DATA_MODE) {
			for (int i = 0; i < CHANNEL_NUM; i++) {
				if (i < 4) {
					inputCounter[i] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posInput[i]);
				} else {
					inputCounter[i] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * posInput[i]);
				}
			}
			return true;
		}

		if (mode == CONFIG_MODE) {
			if (idx == 0) {
				for (int i = 0; i < CHANNEL_NUM; i++) {
					if (i < 4) {
						cfData0[i] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posInput[i]);
					} else {
						cfData0[i] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * posInput[i]);
					}
				}
			} else if (idx == 1) {
				for (int i = 0; i < MBREG_DATA_NUM_1; i++) {
					cfData1[i] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * i);
				}
			} else if (idx == 2) {
				for (int i = 0; i < MBREG_DATA_NUM_2; i++) {
					cfData2[i] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * i);
				}
			}
			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		for (int ch = 0; ch < CHANNEL_NUM; ch++) {
			if ((names[ch] == null) || (names[ch].equals(""))) {
				continue;
			}
			if ((formula[ch] == null) || (formula[ch].equals(""))) {
				calcInputCounter[ch] = inputCounter[ch];
			} else {
				parser.parseExpression(formula[ch]);
				// parser.getSymbolTable();
				if (parser.hasError()) {
					mLogger.warn("Wrong format of formula at channel " + ch);
					calcInputCounter[ch] = inputCounter[ch];
				} else {
					parser.addVariable("ch" + ch, inputCounter[ch]);
					calcInputCounter[ch] = parser.getValue();
				}
			}
		}
	}

	@Override
	public String getDeviceStateData() {
		// TODO Auto-generated method stub
		return null;
	}

}
