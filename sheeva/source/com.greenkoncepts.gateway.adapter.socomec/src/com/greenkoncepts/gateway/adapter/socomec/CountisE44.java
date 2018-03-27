package com.greenkoncepts.gateway.adapter.socomec;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class CountisE44 extends SocomecDevice {

	public static int MBREG_CONFIG_START = 57344;
	public static int MBREG_CONFIG_NUM = 12;

	public static int MBREG_DATA0_START = 50770;
	public static int MBREG_DATA0_NUM = 4;

	public static int MBREG_DATA1_START = 50514;
	public static int MBREG_DATA1_NUM = 54;

	// for data chunk 0
	public static int OFFSET_kWh_System = 0;
	public static int OFFSET_kVARh_System = 2;
	// for data chunk 1
	public static int OFFSET_V_L1_L2 = 0;//
	public static int OFFSET_V_L2_L3 = 2;
	public static int OFFSET_V_L1_L3 = 4;
	public static int OFFSET_V_L1 = 6;
	public static int OFFSET_V_L2 = 8;
	public static int OFFSET_V_L3 = 10;
	public static int OFFSET_Hz_System = 12;
	public static int OFFSET_A_L1 = 14;
	public static int OFFSET_A_L2 = 16;
	public static int OFFSET_A_L3 = 18;
	public static int OFFSET_A_Neutral = 20;
	public static int OFFSET_kW_System = 22;
	public static int OFFSET_kVAR_System = 24;
	public static int OFFSET_kVA_System = 26;
	public static int OFFSET_PF_System = 28;
	public static int OFFSET_kW_L1 = 30;
	public static int OFFSET_kW_L2 = 32;
	public static int OFFSET_kW_L3 = 34;
	public static int OFFSET_kVAR_L1 = 36;
	public static int OFFSET_kVAR_L2 = 38;
	public static int OFFSET_kVAR_L3 = 40;
	public static int OFFSET_kVA_L1 = 42;
	public static int OFFSET_kVA_L2 = 44;
	public static int OFFSET_kVA_L3 = 46;
	public static int OFFSET_PF_L1 = 48;
	public static int OFFSET_PF_L2 = 50;
	public static int OFFSET_PF_L3 = 52;

	private int cfData[] = new int[MBREG_CONFIG_NUM];

	// private double Scalar_Energy = 0.001;
	private double Scalar_Power = 0.01;
	private double Scalar_A = 0.001;
	private double Scalar_V = 0.01;
	private double Scalar_PF = 0.001;
	private double Scalar_Hz = 0.01;

	private long prev_kWh_System = 0;
	private long kWh_System = 0;
	private long kWh_System_diff = 0;
	private long prev_kVARh_System = 0;
	private long kVARh_System = 0;
	private long kVARh_System_diff = 0;
	private long kW_L1 = 0;
	private long kW_L2 = 0;
	private long kW_L3 = 0;
	private long kW_System = 0;
	private long kVAR_L1 = 0;
	private long kVAR_L2 = 0;
	private long kVAR_L3 = 0;
	private long kVAR_System = 0;
	private long kVA_L1 = 0;
	private long kVA_L2 = 0;
	private long kVA_L3 = 0;
	private long kVA_System = 0;
	private int PF_L1 = 0;
	private int PF_L2 = 0;
	private int PF_L3 = 0;
	private int V_L1 = 0;
	private int V_L2 = 0;
	private int V_L3 = 0;
	private int A_L1 = 0;
	private int A_L2 = 0;
	private int A_L3 = 0;
	private int A_SYS = 0;
	public int V_L1_L2 = 0;
	public int V_L2_L3 = 0;
	public int V_L1_L3 = 0;//
	private int Hz_System = 0;
	private int PF_System = 0;//

	// previous data
	double kWh_System_prev = 0;
	double Reverse_kWh_System_prev = 0;
	double kVARh_System_prev = 0;
	boolean isNotFirstGet = false;
	int MULTIPLICAND = 100;
	int E_LIMIT = 500;// .5kw

	public CountisE44(int addr, String category) {
		super(category, addr);
	}

	@Override
	public String getDeviceData() {
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA0_START, MBREG_DATA0_NUM);
		if (decodingData(0, data, DATA_MODE)) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_DATA1_START, MBREG_DATA1_NUM);
			if (decodingData(1, data, DATA_MODE)) {
				calculateDecodedData();
			}
		}
		return createDataSendToServer();
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA0_START, MBREG_DATA0_NUM);
			if (decodingData(0, data, DATA_MODE)) {
				data = modbus.readHoldingRegisters(modbusid, MBREG_DATA1_START, MBREG_DATA1_NUM);
				decodingData(1, data, DATA_MODE);
			}
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			item.put("kWh_System", vformat.format(kWh_System));
			item.put("kVARh_System", vformat.format(kVARh_System));
			item.put("V_L1_L2", vformat.format(Math.abs(V_L1_L2 * Scalar_V)));
			item.put("V_L2_L3", vformat.format(Math.abs(V_L2_L3 * Scalar_V)));
			item.put("V_L1_L3", vformat.format(Math.abs(V_L1_L3 * Scalar_V)));
			item.put("V_L1", vformat.format(V_L1 * Scalar_V));
			item.put("V_L2", vformat.format(V_L2 * Scalar_V));
			item.put("V_L3", vformat.format(V_L3 * Scalar_V));
			item.put("A_L1", vformat.format(Math.abs(A_L1 * Scalar_A)));
			item.put("A_L2", vformat.format(Math.abs(A_L2 * Scalar_A)));
			item.put("A_L3", vformat.format(Math.abs(A_L3 * Scalar_A)));
			item.put("A_System", vformat.format(A_SYS * Scalar_A));
			item.put("kW_L1", vformat.format(Math.abs(kW_L1 * Scalar_Power)));
			item.put("kW_L2", vformat.format(Math.abs(kW_L2 * Scalar_Power)));
			item.put("kW_L3", vformat.format(Math.abs(kW_L3 * Scalar_Power)));
			item.put("kW_System", vformat.format(Math.abs(kW_System * Scalar_Power)));
			item.put("kVAR_L1", vformat.format(Math.abs(kVAR_L1 * Scalar_Power)));
			item.put("kVAR_L2", vformat.format(Math.abs(kVAR_L2 * Scalar_Power)));
			item.put("kVAR_L3", vformat.format(Math.abs(kVAR_L3 * Scalar_Power)));
			item.put("kVAR_System", vformat.format(Math.abs(kVAR_System * Scalar_Power)));
			item.put("kVA_L1", vformat.format(kVA_L1 * Scalar_Power));
			item.put("kVA_L2", vformat.format(kVA_L2 * Scalar_Power));
			item.put("kVA_L3", vformat.format(kVA_L3 * Scalar_Power));
			item.put("kVA_System", vformat.format(kVA_System * Scalar_Power));
			item.put("PF_L1", vformat.format(Math.abs(PF_L1 * Scalar_PF)));
			item.put("PF_L2", vformat.format(Math.abs(PF_L2 * Scalar_PF)));
			item.put("PF_L3", vformat.format(Math.abs(PF_L3 * Scalar_PF)));
			item.put("Hz_System", vformat.format(Hz_System * Scalar_Hz));
			item.put("PF_System", vformat.format(Math.abs(PF_System * Scalar_PF)));
			real_time_data.add(item);
		}
		return real_time_data;
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		device_config.clear();
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START, MBREG_CONFIG_NUM);
		if (decodingData(0, data, CONFIG_MODE)) {
			for (int i = 0; i < MBREG_CONFIG_NUM; i++) {
				if (i == 8) {
					byte[] temp = new byte[4];
					byte[] temp1 = ModbusUtil.unsignedShortToRegister(cfData[i]);
					byte[] temp2 = ModbusUtil.unsignedShortToRegister(cfData[i + 1]);
					temp[0] = temp1[0];
					temp[1] = temp1[1];
					temp[2] = temp2[0];
					temp[3] = temp2[1];
					long val = ModbusUtil.registersBEToLong(temp);
					device_config.put(MBREG_CONFIG_START + i, String.valueOf(val));
					i++;
				} else {
					device_config.put(MBREG_CONFIG_START + i, String.valueOf(cfData[i]));
				}
			}

		}
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		if ((config != null) && (!config.isEmpty())) {
			List<Integer> cfConfigRegs = new ArrayList<Integer>();
			List<Integer> resetReg = new ArrayList<Integer>();
			byte[] results = null;
			for (Integer n : config.keySet()) {
				if (n == 0xE200) {
					resetReg.add(n);
					break;
				}
				if (n == 0xE008) {
					byte[] data = ModbusUtil.uintToRegisters(Integer.parseInt(config.get(n)));
					int idx = n - MBREG_CONFIG_START;
					cfData[idx] = ModbusUtil.registerBEToShort(data, 0);
					cfData[idx + 1] = ModbusUtil.registerBEToShort(data, 2);
				} else {
					cfData[n - MBREG_CONFIG_START] = Integer.parseInt(config.get(n));
				}
				cfConfigRegs.add(n);
			}

//			if (!cfConfigRegs.isEmpty()) {
//				for (Integer reg : cfConfigRegs) {
//					byte[] data = ModbusUtil.unsignedShortToRegister(cfData[reg - MBREG_CONFIG_START]);
//					results = modbus.writeMultipleRegisters(modbusid, reg, data);
//					if (results != null) {
//						registers.add(reg);
//					}
//				}
//	
//			}
			
			if (!cfConfigRegs.isEmpty()) {
				byte[] data = new byte[cfData.length * 2];
				for (int i = 0; i < cfData.length; i++) {
					byte[] temp = ModbusUtil.unsignedShortToRegister(cfData[i]);
					data[2 * i] = temp[0];
					data[2 * i + 1] = temp[1];
				}
				results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG_START, data);
				if (results != null) {
					registers.addAll(cfConfigRegs);
				}
			}
			
			if (!resetReg.isEmpty()) {
				int val = Integer.parseInt(config.get(resetReg.get(0)));
				byte[] data = ModbusUtil.unsignedShortToRegister(val);
				results = modbus.writeSingleRegister(modbusid, resetReg.get(0), data);
				if (results != null) {
					registers.addAll(resetReg);
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
			if (idx == 0) {

				kWh_System = ModbusUtil.registersBEToLongAsUint(data, OFFSET_DATA + 2 * OFFSET_kWh_System);
				kVARh_System = ModbusUtil.registersBEToLongAsUint(data, OFFSET_DATA + 2 * OFFSET_kVARh_System);

			} else if (idx == 1) {

				V_L1_L2 = ModbusUtil.registersBEToSInt(data, OFFSET_DATA + 2 * OFFSET_V_L1_L2);
				V_L2_L3 = ModbusUtil.registersBEToSInt(data, OFFSET_DATA + 2 * OFFSET_V_L2_L3);
				V_L1_L3 = ModbusUtil.registersBEToSInt(data, OFFSET_DATA + 2 * OFFSET_V_L1_L3);
				V_L1 = ModbusUtil.registersBEToSInt(data, OFFSET_DATA + 2 * OFFSET_V_L1);
				V_L2 = ModbusUtil.registersBEToSInt(data, OFFSET_DATA + 2 * OFFSET_V_L2);
				V_L3 = ModbusUtil.registersBEToSInt(data, OFFSET_DATA + 2 * OFFSET_V_L3);
				A_L1 = ModbusUtil.registersBEToSInt(data, OFFSET_DATA + 2 * OFFSET_A_L1);
				A_L2 = ModbusUtil.registersBEToSInt(data, OFFSET_DATA + 2 * OFFSET_A_L2);
				A_L3 = ModbusUtil.registersBEToSInt(data, OFFSET_DATA + 2 * OFFSET_A_L3);
				A_SYS = Math.abs(A_L1) + Math.abs(A_L2)  + Math.abs(A_L3) ;
				
				kW_L1 = ModbusUtil.registersBEToLongAsUint(data, OFFSET_DATA + 2 * OFFSET_kW_L1);
				kW_L2 = ModbusUtil.registersBEToLongAsUint(data, OFFSET_DATA + 2 * OFFSET_kW_L2);
				kW_L3 = ModbusUtil.registersBEToLongAsUint(data, OFFSET_DATA + 2 * OFFSET_kW_L3);
				kW_System = ModbusUtil.registersBEToLongAsUint(data, OFFSET_DATA + 2 * OFFSET_kW_System);
				kVAR_L1 = ModbusUtil.registersBEToLongAsUint(data, OFFSET_DATA + 2 * OFFSET_kVAR_L1);
				kVAR_L2 = ModbusUtil.registersBEToLongAsUint(data, OFFSET_DATA + 2 * OFFSET_kVAR_L2);
				kVAR_L3 = ModbusUtil.registersBEToLongAsUint(data, OFFSET_DATA + 2 * OFFSET_kVAR_L3);
				kVAR_System = ModbusUtil.registersBEToLongAsUint(data, OFFSET_DATA + 2 * OFFSET_kVAR_System);

				kVA_L1 = ModbusUtil.registersBEToLongAsUint(data, OFFSET_DATA + 2 * OFFSET_kVA_L1);
				kVA_L2 = ModbusUtil.registersBEToLongAsUint(data, OFFSET_DATA + 2 * OFFSET_kVA_L2);
				kVA_L3 = ModbusUtil.registersBEToLongAsUint(data, OFFSET_DATA + 2 * OFFSET_kVA_L3);
				kVA_System = ModbusUtil.registersBEToLongAsUint(data, OFFSET_DATA + 2 * OFFSET_kVA_System);

				PF_L1 = ModbusUtil.registersBEToSInt(data, OFFSET_DATA + 2 * OFFSET_PF_L1);
				PF_L2 = ModbusUtil.registersBEToSInt(data, OFFSET_DATA + 2 * OFFSET_PF_L2);
				PF_L3 = ModbusUtil.registersBEToSInt(data, OFFSET_DATA + 2 * OFFSET_PF_L3);
				Hz_System = ModbusUtil.registersBEToSInt(data, OFFSET_DATA + 2 * OFFSET_Hz_System);
				PF_System = ModbusUtil.registersBEToSInt(data, OFFSET_DATA + 2 * OFFSET_PF_System);
			}
			return true;
		}

		if (mode == CONFIG_MODE) {
			for (int i = 0; i < MBREG_CONFIG_NUM; i++) {
				cfData[i] = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * i);
			}
			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		if (kWh_System >= prev_kWh_System) {
			kWh_System_diff = kWh_System - prev_kWh_System;
		} else {
			if ((kWh_System < 100) && ((MAX_INT - prev_kWh_System) < 100)) {
				kWh_System_diff = MAX_INT - prev_kWh_System + kWh_System;
			} else
			{
				kWh_System_diff = 0;
			}
		}
		prev_kWh_System = kWh_System;

		if (kVARh_System >= prev_kVARh_System) {
			kVARh_System_diff = kVARh_System - prev_kVARh_System;
		} else {
			if ((kVARh_System < 100) && ((MAX_INT - prev_kVARh_System) < 100)) {
				kVARh_System_diff = MAX_INT - prev_kVARh_System + kVARh_System;
			} else
			{
				kVARh_System_diff = 0;
			}
		}
		prev_kVARh_System = kVARh_System;
	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuilder data = new StringBuilder();
		// System
		data.append("|DEVICEID=" + getId() + "-0-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + vformat.format(kWh_System) + ",kWh");
		data.append(";Reactive Energy Reading=" + vformat.format(kVARh_System) + ",kVARh");
		data.append(";Active Energy=" + vformat.format(kWh_System_diff * 1000.0) + ",Wh");
		data.append(";Reactive Energy=" + vformat.format(kVARh_System_diff * 1000.0) + ",VARh");
		data.append(";Active Power=" + vformat.format(Math.abs(kW_System * Scalar_Power)) + ",kW");
		data.append(";Reactive Power=" + vformat.format(Math.abs(kVAR_System * Scalar_Power)) + ",kVAR");
		data.append(";Power Factor=" + vformat.format(Math.abs(PF_System * Scalar_PF)) + ",None");
		data.append(";Apparent Power=" + vformat.format(kVA_System * Scalar_Power) + ",kVA");
		data.append(";Voltage L1-L2=" + vformat.format(Math.abs(V_L1_L2 * Scalar_V)) + ",V");
		data.append(";Voltage L2-L3=" + vformat.format(Math.abs(V_L2_L3 * Scalar_V)) + ",V");
		data.append(";Voltage L1-L3=" + vformat.format(Math.abs(V_L1_L3 * Scalar_V)) + ",V");
		data.append(";Current=" + vformat.format(A_SYS * Scalar_A) + ",A");
		// Phase L1
		data.append("|DEVICEID=" + getId() + "-0-1");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(Math.abs(kW_L1 * Scalar_Power)) + ",kW");
		data.append(";Reactive Power=" + vformat.format(Math.abs(kVAR_L1 * Scalar_Power)) + ",kVAR");
		data.append(";Power Factor=" + vformat.format(Math.abs(PF_L1 * Scalar_PF)) + ",None");
		data.append(";Apparent Power=" + vformat.format(kVA_L1 * Scalar_Power) + ",kVA");
		data.append(";Voltage=" + vformat.format(V_L1 * Scalar_V) + ",V");
		data.append(";Current=" + vformat.format(Math.abs(A_L1 * Scalar_A)) + ",A");
		// Phase L2
		data.append("|DEVICEID=" + getId() + "-0-2");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(Math.abs(kW_L2 * Scalar_Power)) + ",kW");
		data.append(";Reactive Power=" + vformat.format(Math.abs(kVAR_L2 * Scalar_Power)) + ",kVAR");
		data.append(";Power Factor=" + vformat.format(Math.abs(PF_L2 * Scalar_PF)) + ",None");
		data.append(";Apparent Power=" + vformat.format(kVA_L2 * Scalar_Power) + ",kVA");
		data.append(";Voltage=" + vformat.format(V_L2 * Scalar_V) + ",V");
		data.append(";Current=" + vformat.format(Math.abs(A_L2 * Scalar_A)) + ",A");
		// Phase L3
		data.append("|DEVICEID=" + getId() + "-0-3");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(Math.abs(kW_L3 * Scalar_Power)) + ",kW");
		data.append(";Reactive Power=" + vformat.format(Math.abs(kVAR_L3 * Scalar_Power)) + ",kVAR");
		data.append(";Power Factor=" + vformat.format(Math.abs(PF_L3 * Scalar_PF)) + ",None");
		data.append(";Apparent Power=" + vformat.format(kVA_L3 * Scalar_Power) + ",kVA");
		data.append(";Voltage=" + vformat.format(V_L3 * Scalar_V) + ",V");
		data.append(";Current=" + vformat.format(Math.abs(A_L3 * Scalar_A)) + ",A");
		return data.toString();

	}
}
