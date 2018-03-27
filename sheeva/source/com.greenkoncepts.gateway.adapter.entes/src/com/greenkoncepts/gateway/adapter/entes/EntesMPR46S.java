package com.greenkoncepts.gateway.adapter.entes;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class EntesMPR46S extends EntesDevice {

	public static int MBREG_DATA1_START = 0x0000;
	public static int MBREG_DATA1_NUM = 0x0052;
	public static int MBREG_DATA2_START = 0x00c8;
	public static int MBREG_DATA2_NUM = 0x0064;
	public static int MBREG_DATA3_START = 0x04a2;
	public static int MBREG_DATA3_NUM = 0x000a;

	public static int MBREG_SETUP1_START = 0x4268;// 0x4268 -> 0x4275
	public static int MBREG_SETUP1_NUM = 14;
	public static int MBREG_SETUP2_START = 0x42E4;// 0x42E4 -> 0x42F5
	public static int MBREG_SETUP2_NUM = 18;
	public static int MBREG_SETUP3_START = 0x1770;// 0x1770 -> 0x1781
	public static int MBREG_SETUP3_NUM = 18;
	public static int MBREG_REGISTER_RESET = 0x36B0;


	public static double Scalar_Energy = 0.001;
	public static double Scalar_Power = 0.001;
	public static double Scalar_A = 0.001;
	public static double Scalar_V = 0.1;
	public static double Scalar_PF = 0.001;
	public static double Scalar_Hz = 0.01;

	private int[] cfSetup1 = new int[MBREG_SETUP1_NUM];
	private int[] cfSetup2 = new int[MBREG_SETUP2_NUM];
	private int[] cfSetup3 = new int[MBREG_SETUP3_NUM];

	private long kWh_System = 0;
	private long kWh_L1 = 0;
	private long kWh_L2 = 0;
	private long kWh_L3 = 0;
	private long kVARh_System = 0;
	private long kVARh_L1 = 0;
	private long kVARh_L2 = 0;
	private long kVARh_L3 = 0;
	private long kVAh_System = 0;
	private long kVAh_L1 = 0;
	private long kVAh_L2 = 0;
	private long kVAh_L3 = 0;
	private float kW_L1 = 0f;
	private float kW_L2 = 0f;
	private float kW_L3 = 0f;
	private float kVAR_L1 = 0f;
	private float kVAR_L2 = 0f;
	private float kVAR_L3 = 0f;
	private float kVA_L1 = 0f;
	private float kVA_L2 = 0f;
	private float kVA_L3 = 0f;
	private int PF_L1 = 0;
	private int PF_L2 = 0;
	private int PF_L3 = 0;
	private int PF_System = 0;
	private int V_L1 = 0;
	private int V_L2 = 0;
	private int V_L3 = 0;
	private int V_L1_L2 = 0;
	private int V_L2_L3 = 0;
	private int V_L1_L3 = 0;
	private int A_L1 = 0;
	private int A_L2 = 0;
	private int A_L3 = 0;
	private int A_Neutral = 0;
	private int Hz_System = 0;
	private float kW_System = 0f;
	private float kVAR_System = 0f;
	private float kVA_System = 0f;

	private long diff_kWh_System = 0;
	private long prev_kWh_System = 0;
	private long diff_kWh_L1 = 0;
	private long prev_kWh_L1 = 0;
	private long diff_kWh_L2 = 0;
	private long prev_kWh_L2 = 0;
	private long diff_kWh_L3 = 0;
	private long prev_kWh_L3 = 0;
	private long diff_kVAh_L1 = 0;
	private long prev_kVAh_L1 = 0;
	private long diff_kVAh_L2 = 0;
	private long prev_kVAh_L2 = 0;
	private long diff_kVAh_L3 = 0;
	private long prev_kVAh_L3 = 0;
	private long diff_kVAh_System = 0;
	private long prev_kVAh_System = 0;
	private long diff_kVARh_L1 = 0;
	private long prev_kVARh_L1 = 0;
	private long diff_kVARh_L2 = 0;
	private long prev_kVARh_L2 = 0;
	private long diff_kVARh_L3 = 0;
	private long prev_kVARh_L3 = 0;
	private long diff_kVARh_System = 0;
	private long prev_kVARh_System = 0;

	private float kW_Demand_L1 = 0f;
	private float kW_Demand_L2 = 0f;
	private float kW_Demand_L3 = 0f;
	private float kW_Demand_System = 0f;

	public EntesMPR46S(int addr, String category) {
		super(category, addr);

	}

	@Override
	public String getDeviceData() {
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA1_START, MBREG_DATA1_NUM);
		if (decodingData(0, data, DATA_MODE)) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_DATA2_START, MBREG_DATA2_NUM);
			if (decodingData(1, data, DATA_MODE)) {
				data = modbus.readHoldingRegisters(modbusid, MBREG_DATA3_START, MBREG_DATA3_NUM);
				if (decodingData(2, data, DATA_MODE)) {
					calculateDecodedData();
				}
			}
		}
		return createDataSendToServer();
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA1_START, MBREG_DATA1_NUM);
			if (decodingData(0, data, DATA_MODE)) {
				data = modbus.readHoldingRegisters(modbusid, MBREG_DATA2_START, MBREG_DATA2_NUM);
				if (decodingData(1, data, DATA_MODE)) {
					data = modbus.readHoldingRegisters(modbusid, MBREG_DATA3_START, MBREG_DATA3_NUM);
					decodingData(2, data, DATA_MODE);
				}
			}
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			// System
			item.put("kWh_System", vformat.format(kWh_System * Scalar_Energy));
			item.put("kVARh_System", vformat.format(kVARh_System * Scalar_Energy));
			item.put("kVAh_System", vformat.format(kVAh_System * Scalar_Energy));
			item.put("A_Neutral", vformat.format(A_Neutral * Scalar_A));
			item.put("Hz_System", vformat.format(Hz_System * Scalar_Hz));
			item.put("kW_System", vformat.format(kW_System * Scalar_Power));
			item.put("kVAR_System", vformat.format(kVAR_System * Scalar_Power));
			item.put("kVA_System", vformat.format(kVA_System * Scalar_Power));
			item.put("PF_System", vformat.format(PF_System * Scalar_PF));
			item.put("V_L1_L2", vformat.format(V_L1_L2 * Scalar_V));
			item.put("V_L2_L3", vformat.format(V_L2_L3 * Scalar_V));
			item.put("V_L1_L3", vformat.format(V_L1_L3 * Scalar_V));
			item.put("kW_Demand_System", vformat.format(kW_Demand_System * Scalar_Power));
			item.put("V_System", vformat.format(V_L1 * Scalar_V));

			// Phase L1
			item.put("kWh_L1", vformat.format((kWh_L1) * Scalar_Energy));
			item.put("kVARh_L1", vformat.format((kVARh_L1) * Scalar_Energy));
			item.put("kVAh_L1", vformat.format(kVAh_L1 * Scalar_Energy));
			item.put("kW_L1", vformat.format(kW_L1 * Scalar_Power));
			item.put("kVAR_L1", vformat.format(kVAR_L1 * Scalar_Power));
			item.put("kVA_L1", vformat.format(kVA_L1 * Scalar_Power));
			item.put("PF_L1", vformat.format(PF_L1 * Scalar_PF));
			item.put("V_L1", vformat.format(V_L1 * Scalar_V));
			item.put("A_L1", vformat.format(A_L1 * Scalar_A));
			item.put("kW_Demand_L1", vformat.format(kW_Demand_L1 * Scalar_Power));

			// Phase L2
			item.put("kWh_L2", vformat.format(kWh_L2 * Scalar_Energy));
			item.put("kVARh_L2", vformat.format(kVARh_L2 * Scalar_Energy));
			item.put("kVAh_L2", vformat.format(kVAh_L2 * Scalar_Energy));
			item.put("kW_L2", vformat.format(kW_L2 * Scalar_Power));
			item.put("kVAR_L2", vformat.format(kVAR_L2 * Scalar_Power));
			item.put("kVA_L2", vformat.format(kVA_L2 * Scalar_Power));
			item.put("PF_L2", vformat.format(PF_L2 * Scalar_PF));
			item.put("V_L2", vformat.format(V_L2 * Scalar_V));
			item.put("A_L2", vformat.format(A_L2 * Scalar_A));
			item.put("kW_Demand_L2", vformat.format(kW_Demand_L2 * Scalar_Power));

			// Phase L3
			item.put("kWh_L3", "" + vformat.format((kWh_L3) * Scalar_Energy));
			item.put("kVARh_L3", "" + vformat.format(kVARh_L3 * Scalar_Energy));
			item.put("kVAh_L3", "" + vformat.format(kVAh_L3 * Scalar_Energy));
			item.put("kW_L3", "" + vformat.format(kW_L3 * Scalar_Power));
			item.put("kVAR_L3", "" + vformat.format(kVAR_L3 * Scalar_Power));
			item.put("kVA_L3", "" + vformat.format(kVA_L3 * Scalar_Power));
			item.put("PF_L3", "" + vformat.format(PF_L3 * Scalar_PF));
			item.put("V_L3", "" + vformat.format(V_L3 * Scalar_V));
			item.put("A_L3", "" + vformat.format(A_L3 * Scalar_A));
			item.put("kW_Demand_L3", vformat.format(kW_Demand_L3 * Scalar_Power));
			real_time_data.add(item);
		}
		return real_time_data;
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		device_config.clear();
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_SETUP1_START, MBREG_SETUP1_NUM);
		if (decodingData(0, data, CONFIG_MODE)) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_SETUP2_START, MBREG_SETUP2_NUM);
			if (decodingData(1, data, CONFIG_MODE)) {
				data = modbus.readHoldingRegisters(modbusid, MBREG_SETUP3_START, MBREG_SETUP3_NUM);
				if (decodingData(2, data, CONFIG_MODE)) {
					for (int i = 0; i < MBREG_SETUP1_NUM ; i++) {
						if ((i == 5) || (i==11)){
							byte[] temp = new byte[4];
							byte[] temp1 = ModbusUtil.unsignedShortToRegister(cfSetup1[i]);
							byte[] temp2 = ModbusUtil.unsignedShortToRegister(cfSetup1[i+1]);
							temp[0] = temp1[0];
							temp[1] = temp1[1];
							temp[2] = temp2[0];
							temp[3] = temp2[1];
							long val = ModbusUtil.registersBEToLong(temp);
							device_config.put(MBREG_SETUP1_START + i, String.valueOf(val));
							i++;
						} else  {
							device_config.put(MBREG_SETUP1_START + i, String.valueOf(cfSetup1[i]));
						}
					}
					for (int j = 0; j < MBREG_SETUP2_NUM ; j++) {
						device_config.put(MBREG_SETUP2_START + j, String.valueOf(cfSetup2[j]));
					}
					for (int k = 0; k < MBREG_SETUP3_NUM ; k++) {
						device_config.put(MBREG_SETUP3_START + k, String.valueOf(cfSetup3[k]));
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
			List<Integer> cfSetup1Regs = new ArrayList<Integer>();
			List<Integer> cfSetup2Regs = new ArrayList<Integer>();
			List<Integer> cfSetup3Regs = new ArrayList<Integer>();
			byte[] results = null;
			for (Integer n : config.keySet()) {
				if (n == MBREG_REGISTER_RESET) {
					int val = Integer.parseInt(config.get(n));
					byte[] data = ModbusUtil.unsignedShortToRegister(val);
					results = modbus.writeSingleRegister(modbusid, n, data);
					if (results != null) {
						registers.add(n);
					}
					break;
				}
				if ((n >= MBREG_SETUP1_START) && (n < MBREG_SETUP2_START)) {
					if ((n == 17005) || (n == 17011)) {
						byte[] data = ModbusUtil.uintToRegisters(Integer.parseInt(config.get(n)));
						int idx = n - MBREG_SETUP1_START;
						cfSetup1[idx] = ModbusUtil.registerBEToShort(data, 0);
						cfSetup1[idx + 1] = ModbusUtil.registerBEToShort(data, 2);
					} else {
						cfSetup1[n - MBREG_SETUP1_START] = Integer.parseInt(config.get(n));
					}
					cfSetup1Regs.add(n);
				} else if (n >= MBREG_SETUP2_START) {
					cfSetup2[n - MBREG_SETUP2_START] = Integer.parseInt(config.get(n));
					cfSetup2Regs.add(n);
				} else {
					cfSetup3[n - MBREG_SETUP3_START] = Integer.parseInt(config.get(n));
					cfSetup3Regs.add(n);
				}
			}

			if (!cfSetup1Regs.isEmpty()) {
				byte[] data = new byte[cfSetup1.length * 2];
				for (int i = 0; i < cfSetup1.length; i++) {
					byte[] temp = ModbusUtil.unsignedShortToRegister(cfSetup1[i]);
					data[2 * i] = temp[0];
					data[2 * i + 1] = temp[1];
				}
				results = modbus.writeMultipleRegisters(modbusid, MBREG_SETUP1_START, data);
				if (results != null) {
					registers.addAll(cfSetup1Regs);
				}
			}

			if (!cfSetup2Regs.isEmpty()) {
				byte[] data = new byte[cfSetup2.length * 2];
				for (int i = 0; i < cfSetup2.length; i++) {
					byte[] temp = ModbusUtil.unsignedShortToRegister(cfSetup2[i]);
					data[2 * i] = temp[0];
					data[2 * i + 1] = temp[1];
				}
				results = modbus.writeMultipleRegisters(modbusid, MBREG_SETUP2_START, data);
				if (results != null) {
					registers.addAll(cfSetup2Regs);
				}
			}

			if (!cfSetup3Regs.isEmpty()) {
			
				for (Integer reg : cfSetup3Regs) {
					byte[] data = ModbusUtil.unsignedShortToRegister(cfSetup3[reg-MBREG_SETUP3_START]);
					results = modbus.writeSingleRegister(modbusid, reg, data);
					if (results != null) {
						registers.add(reg);
					}
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
				V_L1 = ModbusUtil.registersBEToInt(data, OFFSET_DATA + 2 * 0);
				V_L2 = ModbusUtil.registersBEToInt(data, OFFSET_DATA + 2 * 2);
				V_L3 = ModbusUtil.registersBEToInt(data, OFFSET_DATA + 2 * 4);
				// padding
				V_L1_L2 = ModbusUtil.registersBEToInt(data, OFFSET_DATA + 2 * 8);
				V_L2_L3 = ModbusUtil.registersBEToInt(data, OFFSET_DATA + 2 * 10);
				V_L1_L3 = ModbusUtil.registersBEToInt(data, OFFSET_DATA + 2 * 12);
				A_L1 = ModbusUtil.registersBEToInt(data, OFFSET_DATA + 2 * 14);
				A_L2 = ModbusUtil.registersBEToInt(data, OFFSET_DATA + 2 * 16);
				A_L3 = ModbusUtil.registersBEToInt(data, OFFSET_DATA + 2 * 18);
				// padding
				A_Neutral = ModbusUtil.registersBEToInt(data, OFFSET_DATA + 2 * 22);
				Hz_System = ModbusUtil.registersBEToInt(data, OFFSET_DATA + 2 * 24);

				kW_L1 = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2 * 26);
				kW_L2 = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2 * 28);
				kW_L3 = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2 * 30);
				kW_System = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2 * 38);
				
				kVAR_L1 = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2 * 40);
				kVAR_L2 = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2 * 42);
				kVAR_L3 = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2 * 44);
				kVAR_System = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2 * 56);
				
				kVA_L1 = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2 * 58);
				kVA_L2 = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2 * 60);
				kVA_L3 = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2 * 62);
				kVA_System = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2 * 70);
				
				PF_L1 = ModbusUtil.registersBEToSInt(data, OFFSET_DATA + 2 * 72);
				PF_L2 = ModbusUtil.registersBEToSInt(data, OFFSET_DATA + 2 * 74);
				PF_L3 = ModbusUtil.registersBEToSInt(data, OFFSET_DATA + 2 * 76);
				PF_System = ModbusUtil.registersBEToSInt(data, OFFSET_DATA + 2 * 80);				
			} else if (idx == 1) {
				kWh_L1 = ModbusUtil.registersToLong(data, OFFSET_DATA + 2 * 0);
				kWh_L2 = ModbusUtil.registersToLong(data, OFFSET_DATA + 2 * 4);
				kWh_L3 = ModbusUtil.registersToLong(data, OFFSET_DATA + 2 * 8);
				// padding
				kWh_System = ModbusUtil.registersToLong(data, OFFSET_DATA + 2 * 16);

				// padding
				kVAh_L1 = ModbusUtil.registersToLong(data, OFFSET_DATA + 2 * 40);
				kVAh_L2 = ModbusUtil.registersToLong(data, OFFSET_DATA + 2 * 44);
				kVAh_L3 = ModbusUtil.registersToLong(data, OFFSET_DATA + 2 * 48);
				// padding
				kVAh_System = ModbusUtil.registersToLong(data, OFFSET_DATA + 2 * 56);
				// padding
				long kVARh_L1Q1 = ModbusUtil.registersToLong(data, OFFSET_DATA + 2 * 80);
				//long kVARh_L1Q2 = ModbusUtil.registersToLong(data, OFFSET_DATA + 2 * 100);
				kVARh_L1 = kVARh_L1Q1;// + kVARh_L1Q2;

				long kVARh_L2Q1 = ModbusUtil.registersToLong(data, OFFSET_DATA + 2 * 84);
				//long kVARh_L2Q2 = ModbusUtil.registersToLong(data, OFFSET_DATA + 2 * 104);
				kVARh_L2 = kVARh_L2Q1;// + kVARh_L2Q2;

				long kVARh_L3Q1 = ModbusUtil.registersToLong(data, OFFSET_DATA + 2 * 88);
				//long kVARh_L3Q2 = ModbusUtil.registersToLong(data, OFFSET_DATA + 2 * 108);
				kVARh_L3 = kVARh_L3Q1;// + kVARh_L3Q2;
				// padding
				long kVARh_SystemQ1 = ModbusUtil.registersToLong(data, OFFSET_DATA + 2 * 96);
				//long kVARh_SystemQ2 = ModbusUtil.registersToLong(data, OFFSET_DATA + 2 * 116);
				kVARh_System = kVARh_SystemQ1;// + kVARh_SystemQ2;
			} else if (idx == 2) {
				kW_Demand_L1 = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2 * 0);
				kW_Demand_L2 = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2 * 2);
				kW_Demand_L3 = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2 * 4);
				kW_Demand_System = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2 * 8);
			}
			return true;
		}

		if (mode == CONFIG_MODE) {
			if (idx == 0) {
				for (int i = 0; i < MBREG_SETUP1_NUM; i++) {
					cfSetup1[i] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * i);
				}
			} else if (idx == 1) {
				for (int i = 0; i < MBREG_SETUP2_NUM; i++) {
					cfSetup2[i] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * i);
				}
			} else if (idx == 2) {
				for (int i = 0; i < MBREG_SETUP3_NUM; i++) {
					cfSetup3[i] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * i);
				}
			}
			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		if ((prev_kWh_System == 0) || (prev_kWh_System > kWh_System)) {
			diff_kWh_System = 0;
		} else {
			diff_kWh_System = kWh_System - prev_kWh_System;
		}
		prev_kWh_System = kWh_System;

		if ((prev_kVARh_System == 0) || (prev_kVARh_System > kVARh_System)) {
			diff_kVARh_System = 0;
		} else {
			diff_kVARh_System = kVARh_System - prev_kVARh_System;
		}
		prev_kVARh_System = kVARh_System;

		if ((prev_kVAh_System == 0) || (prev_kVAh_System > kVAh_System)) {
			diff_kVAh_System = 0;
		} else {
			diff_kVAh_System = kVAh_System - prev_kVAh_System;
		}
		prev_kVAh_System = kVAh_System;

		if ((prev_kWh_L1 == 0) || (prev_kWh_L1 > kWh_L1)) {
			diff_kWh_L1 = 0;
		} else {
			diff_kWh_L1 = kWh_L1 - prev_kWh_L1;
		}
		prev_kWh_L1 = kWh_L1;

		if ((prev_kVARh_L1 == 0) || (prev_kVARh_L1 > kVARh_L1)) {
			diff_kVARh_L1 = 0;
		} else {
			diff_kVARh_L1 = kVARh_L1 - prev_kVARh_L1;
		}
		prev_kVARh_L1 = kVARh_L1;

		if ((prev_kVAh_L1 == 0) || (prev_kVAh_L1 > kVAh_L1)) {
			diff_kVAh_L1 = 0;
		} else {
			diff_kVAh_L1 = kVAh_L1 - prev_kVAh_L1;
		}
		prev_kVAh_L1 = kVAh_L1;

		if ((prev_kWh_L2 == 0) || (prev_kWh_L2 > kWh_L2)) {
			diff_kWh_L2 = 0;
		} else {
			diff_kWh_L2 = kWh_L2 - prev_kWh_L2;
		}
		prev_kWh_L2 = kWh_L2;

		if ((prev_kVARh_L2 == 0) || (prev_kVARh_L2 > kVARh_L2)) {
			diff_kVARh_L2 = 0;
		} else {
			diff_kVARh_L2 = kVARh_L2 - prev_kVARh_L2;
		}
		prev_kVARh_L2 = kVARh_L2;

		if ((prev_kVAh_L2 == 0) || (prev_kVAh_L2 > kVAh_L2)) {
			diff_kVAh_L2 = 0;
		} else {
			diff_kVAh_L2 = kVAh_L2 - prev_kVAh_L2;
		}
		prev_kVAh_L2 = kVAh_L2;

		if ((prev_kWh_L3 == 0) || (prev_kWh_L3 > kWh_L3)) {
			diff_kWh_L3 = 0;
		} else {
			diff_kWh_L3 = kWh_L3 - prev_kWh_L3;
		}
		prev_kWh_L3 = kWh_L3;

		if ((prev_kVARh_L3 == 0) || (prev_kVARh_L3 > kVARh_L3)) {
			diff_kVARh_L3 = 0;
		} else {
			diff_kVARh_L3 = kVARh_L3 - prev_kVARh_L3;
		}
		prev_kVARh_L3 = kVARh_L3;

		if ((prev_kVAh_L3 == 0) || (prev_kVAh_L3 > kVAh_L3)) {
			diff_kVAh_L3 = 0;
		} else {
			diff_kVAh_L3 = kVAh_L3 - prev_kVAh_L3;
		}
		prev_kVAh_L3 = kVAh_L3;
	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		// System
		data.append("|DEVICEID=" + getId() + "-0-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + vformat.format(kWh_System * Scalar_Energy) + ",kWh");
		data.append(";Reactive Energy Reading=" + vformat.format(kVARh_System * Scalar_Energy) + ",kVARh");
		data.append(";Apparent Energy Reading=" + vformat.format(kVAh_System * Scalar_Energy) + ",kVAh");
		data.append(";Active Energy=" + vformat.format(diff_kWh_System) + ",Wh");
		data.append(";Reactive Energy=" + vformat.format(diff_kVARh_System) + ",VARh");
		data.append(";Apparent Energy=" + vformat.format(diff_kVAh_System) + ",VAh");
		data.append(";Current Neutral=" + vformat.format(A_Neutral * Scalar_A) + ",A");
		data.append(";Frequency=" + vformat.format(Hz_System * Scalar_Hz) + ",Hz");
		data.append(";Active Power=" + vformat.format(kW_System * Scalar_Power) + ",kW");
		data.append(";Reactive Power=" + vformat.format(kVAR_System * Scalar_Power) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(kVA_System * Scalar_Power) + ",kVA");
		data.append(";Voltage L1-L2=" + vformat.format(V_L1_L2 * Scalar_V) + ",V");
		data.append(";Voltage L2-L3=" + vformat.format(V_L2_L3 * Scalar_V) + ",V");
		data.append(";Voltage L1-L3=" + vformat.format(V_L1_L3 * Scalar_V) + ",V");
		data.append(";Current=" + vformat.format((A_L1 + A_L2 + A_L3) * Scalar_A) + ",A");
		data.append(";Power Factor=" + vformat.format(PF_System * Scalar_PF) + ",None");
		data.append(";Voltage=" + vformat.format(V_L1 * Scalar_V) + ",V");
		data.append(";Peak Demand=" + vformat.format(kW_Demand_System * Scalar_Power) + ",kW");

		// Phase L1
		data.append("|DEVICEID=" + getId() + "-0-1");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + vformat.format(kWh_L1 * Scalar_Energy) + ",kWh");
		data.append(";Reactive Energy Reading=" + vformat.format(kVARh_L1 * Scalar_Energy) + ",kVARh");
		data.append(";Apparent Energy Reading=" + vformat.format(kVAh_L1 * Scalar_Energy) + ",kVAh");
		data.append(";Active Energy=" + vformat.format(diff_kWh_L1) + ",Wh");
		data.append(";Reactive Energy=" + vformat.format(diff_kVARh_L1) + ",VARh");
		data.append(";Apparent Energy=" + vformat.format(diff_kVAh_L1) + ",VAh");
		data.append(";Active Power=" + vformat.format(kW_L1 * Scalar_Power) + ",kW");
		data.append(";Reactive Power=" + vformat.format(kVAR_L1 * Scalar_Power) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(kVA_L1 * Scalar_Power) + ",kVA");
		data.append(";Power Factor=" + vformat.format(PF_L1 * Scalar_PF) + ",None");
		data.append(";Voltage=" + vformat.format(V_L1 * Scalar_V) + ",V");
		data.append(";Current=" + vformat.format(A_L1 * Scalar_A) + ",A");
		data.append(";Frequency=" + vformat.format(Hz_System * Scalar_Hz) + ",Hz");
		data.append(";Peak Demand=" + vformat.format(kW_Demand_L1 * Scalar_Power) + ",kW");

		// Phase L2
		data.append("|DEVICEID=" + getId() + "-0-2");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + vformat.format(kWh_L2 * Scalar_Energy) + ",kWh");
		data.append(";Reactive Energy Reading=" + vformat.format(kVARh_L2 * Scalar_Energy) + ",kVARh");
		data.append(";Apparent Energy Reading=" + vformat.format(kVAh_L2 * Scalar_Energy) + ",kVAh");
		data.append(";Active Energy=" + vformat.format(diff_kWh_L2) + ",Wh");
		data.append(";Reactive Energy=" + vformat.format(diff_kVARh_L2) + ",VARh");
		data.append(";Apparent Energy=" + vformat.format(diff_kVAh_L2) + ",VAh");
		data.append(";Active Power=" + vformat.format(kW_L2 * Scalar_Power) + ",kW");
		data.append(";Reactive Power=" + vformat.format(kVAR_L2 * Scalar_Power) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(kVA_L2 * Scalar_Power) + ",kVA");
		data.append(";Power Factor=" + vformat.format(PF_L2 * Scalar_PF) + ",None");
		data.append(";Voltage=" + vformat.format(V_L2 * Scalar_V) + ",V");
		data.append(";Current=" + vformat.format(A_L2 * Scalar_A) + ",A");
		data.append(";Frequency=" + vformat.format(Hz_System * Scalar_Hz) + ",Hz");
		data.append(";Peak Demand=" + vformat.format(kW_Demand_L2 * Scalar_Power) + ",kW");

		// Phase L3
		data.append("|DEVICEID=" + getId() + "-0-3");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + vformat.format(kWh_L3 * Scalar_Energy) + ",kWh");
		data.append(";Reactive Energy Reading=" + vformat.format(kVARh_L3 * Scalar_Energy) + ",kVARh");
		data.append(";Apparent Energy Reading=" + vformat.format(kVAh_L3 * Scalar_Energy) + ",kVAh");
		data.append(";Active Energy=" + vformat.format(diff_kWh_L3) + ",Wh");
		data.append(";Reactive Energy=" + vformat.format(diff_kVARh_L3) + ",VARh");
		data.append(";Apparent Energy=" + vformat.format(diff_kVAh_L3) + ",VAh");
		data.append(";Active Power=" + vformat.format(kW_L3 * Scalar_Power) + ",kW");
		data.append(";Reactive Power=" + vformat.format(kVAR_L3 * Scalar_Power) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(kVA_L3 * Scalar_Power) + ",kVA");
		data.append(";Power Factor=" + vformat.format(PF_L3 * Scalar_PF) + ",None");
		data.append(";Voltage=" + vformat.format(V_L3 * Scalar_V) + ",V");
		data.append(";Current=" + vformat.format(A_L3 * Scalar_A) + ",A");
		data.append(";Frequency=" + vformat.format(Hz_System * Scalar_Hz) + ",Hz");
		data.append(";Peak Demand=" + vformat.format(kW_Demand_L3 * Scalar_Power) + ",kW");

		return data.toString();

	}

}
