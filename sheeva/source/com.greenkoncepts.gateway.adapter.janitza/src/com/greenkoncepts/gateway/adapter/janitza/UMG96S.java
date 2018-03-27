package com.greenkoncepts.gateway.adapter.janitza;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class UMG96S extends JanitzaDevice {

	public static final int MBREG_CONFIG1_START = 0;
	public static final int MBREG_CONFIG1_NUM = 74;

	public static final int offDigitalOutput1 = 4;
	public static final int offDigitalOutput2 = 6;
	public static final int offComparator1ALimit = 13;
	public static final int offComparator1BLimit = 18;
	public static final int offComparator1CLimit = 23;
	public static final int offComparator2ALimit = 28;
	public static final int offComparator2BLimit = 33;
	public static final int offComparator2CLimit = 38;
	public static final int offAnalogOutput1_20mA = 48;
	public static final int offAnalogOutput1_4mA = 50;
	public static final int offAnalogOutput2_20mA = 53;

	public static final int MBREG_CONFIG2_START = 600;
	public static final int MBREG_CONFIG2_NUM = 4;

	public static final int MBREG_DATA1_START = 200;
	public static final int MBREG_DATA1_NUM = 18;

	public static final int OFFSET_V_L1 = 0;
	public static final int OFFSET_V_L2 = 1;
	public static final int OFFSET_V_L3 = 2;
	public static final int OFFSET_V_L1_L2 = 3;
	public static final int OFFSET_V_L2_L3 = 4;
	public static final int OFFSET_V_L3_L1 = 5;
	public static final int OFFSET_A_L1 = 6;
	public static final int OFFSET_A_L2 = 7;
	public static final int OFFSET_A_L3 = 8;
	public static final int OFFSET_kW_L1 = 9;
	public static final int OFFSET_kW_L2 = 10;
	public static final int OFFSET_kW_L3 = 11;
	public static final int OFFSET_kVAR_L1 = 12;
	public static final int OFFSET_kVAR_L2 = 13;
	public static final int OFFSET_kVAR_L3 = 14;
	public static final int OFFSET_kVA_L1 = 15;
	public static final int OFFSET_kVA_L2 = 16;
	public static final int OFFSET_kVA_L3 = 17;

	public static final int MBREG_DATA3_START = 275;
	public static final int MBREG_DATA3_NUM = 7;
	public static final int OFFSET_Hz_System = 0;
	public static final int OFFSET_PF_System = 1;
	public static final int OFFSET_kW_System = 4;
	public static final int OFFSET_kVAR_System = 5;
	public static final int OFFSET_kVA_System = 6;

	public static final int MBREG_DATA2_START = 416;
	public static final int MBREG_DATA2_NUM = 6;
	public static final int OFFSET_kWh_System = 0;
	public static final int OFFSET_kVARh_System = 1;
	public static final int OFFSET_kVAh_System = 2;

	private double Scalar_Energy = 0.001;
	private double Scalar_Power = 0.001;
	private double Scalar_A = 0.001;
	private double Scalar_V = 0.1;
	private double Scalar_PF = 0.01;
	private double Scalar_Hz = 0.01;

	private int cfData1[] = new int[MBREG_CONFIG1_NUM];
	private int cfData2[] = new int[MBREG_CONFIG2_NUM];

	private int V_L1 = 0;
	private int V_L2 = 0;
	private int V_L3 = 0;
	private int V_L1_L2 = 0;
	private int V_L2_L3 = 0;
	private int V_L3_L1 = 0;
	private int A_L1 = 0;
	private int A_L2 = 0;
	private int A_L3 = 0;
	private int kW_L1 = 0;
	private int kW_L2 = 0;
	private int kW_L3 = 0;
	private int kVAR_L1 = 0;
	private int kVAR_L2 = 0;
	private int kVAR_L3 = 0;
	private int kVA_L1 = 0;
	private int kVA_L2 = 0;
	private int kVA_L3 = 0;
	private int PF_System = 0;
	private int kW_System = 0;
	private int kVAR_System = 0;
	private int kVA_System = 0;
	private int Hz_System = 0;
	private int prev_kWh_System = 0;
	private int kWh_System = 0;

	private int kWh_System_diff = 0;

	public UMG96S(int addr, String category) {
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
			item.put("voltage_l1", vformat.format(V_L1 * Scalar_V));
			item.put("voltage_l2", vformat.format(V_L2 * Scalar_V));
			item.put("voltage_l3", vformat.format(V_L3 * Scalar_V));
			item.put("v_l1_l2", vformat.format(V_L1_L2 * Scalar_V));
			item.put("v_l2_l3", vformat.format(V_L2_L3 * Scalar_V));
			item.put("v_l3_l1", vformat.format(V_L3_L1 * Scalar_V));
			item.put("current_l1", vformat.format(A_L1 * Scalar_A));
			item.put("current_l2", vformat.format(A_L2 * Scalar_A));
			item.put("current_l3", vformat.format(A_L3 * Scalar_A));

			item.put("act_power_l1", vformat.format(kW_L1 * Scalar_Power));
			item.put("act_power_l2", vformat.format(kW_L2 * Scalar_Power));
			item.put("act_power_l3", vformat.format(kW_L3 * Scalar_Power));

			item.put("rea_power_l1", vformat.format(kVAR_L1 * Scalar_Power));
			item.put("rea_power_l2", vformat.format(kVAR_L2 * Scalar_Power));
			item.put("rea_power_l3", vformat.format(kVAR_L3 * Scalar_Power));

			item.put("app_power_l1", vformat.format(kVA_L1 * Scalar_Power));
			item.put("app_power_l2", vformat.format(kVA_L2 * Scalar_Power));
			item.put("app_power_l3", vformat.format(kVA_L3 * Scalar_Power));

			item.put("act_energy", vformat.format(kWh_System * Scalar_Energy));
			item.put("powert_factor", vformat.format(PF_System * Scalar_PF));
			item.put("act_power", vformat.format(kW_System * Scalar_Power));
			item.put("frequency", vformat.format(Hz_System * Scalar_PF));
			item.put("rea_power", vformat.format(kVAR_System * Scalar_Power));
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
				for (int i = 0; i < MBREG_CONFIG1_NUM; i++) {
					switch (i) {
						case offDigitalOutput1:
						case offDigitalOutput2:
						case offComparator1ALimit:
						case offComparator1BLimit:
						case offComparator1CLimit:
						case offComparator2ALimit:
						case offComparator2BLimit:
						case offComparator2CLimit:
						case offAnalogOutput1_20mA:
						case offAnalogOutput1_4mA:
						case offAnalogOutput2_20mA:
							byte[] temp = new byte[4];
							byte[] temp1 = ModbusUtil.unsignedShortToRegister(cfData1[i]);
							byte[] temp2 = ModbusUtil.unsignedShortToRegister(cfData1[i + 1]);
							temp[0] = temp1[0];
							temp[1] = temp1[1];
							temp[2] = temp2[0];
							temp[3] = temp2[1];
							long val = ModbusUtil.registersBEToLong(temp);
							device_config.put(MBREG_CONFIG1_START + i, String.valueOf(val));
							i++;
							break;

						default:
							device_config.put(MBREG_CONFIG1_START + i, String.valueOf(cfData1[i]));
							break;
					}
				}

				for (int k = 0; k < MBREG_CONFIG2_NUM; k++) {
					device_config.put(MBREG_CONFIG2_START + k, String.valueOf(cfData2[k]));
				}
			}
		}
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		if ((config != null) && (!config.isEmpty())) {
			List<Integer> cfConfig1Regs = new ArrayList<Integer>();
			List<Integer> cfConfig2Regs = new ArrayList<Integer>();
			byte[] results = null;
			for (Integer n : config.keySet()) {
				if (n < MBREG_CONFIG2_START) {
					int idx = n - MBREG_CONFIG1_START;
					switch (n) {
						case MBREG_CONFIG1_START + offDigitalOutput1:
						case MBREG_CONFIG1_START + offDigitalOutput2:
						case MBREG_CONFIG1_START + offComparator1ALimit:
						case MBREG_CONFIG1_START + offComparator1BLimit:
						case MBREG_CONFIG1_START + offComparator1CLimit:
						case MBREG_CONFIG1_START + offComparator2ALimit:
						case MBREG_CONFIG1_START + offComparator2BLimit:
						case MBREG_CONFIG1_START + offComparator2CLimit:
						case MBREG_CONFIG1_START + offAnalogOutput1_20mA:
						case MBREG_CONFIG1_START + offAnalogOutput1_4mA:
						case MBREG_CONFIG1_START + offAnalogOutput2_20mA:
							byte[] data = ModbusUtil.uintToRegisters(Integer.parseInt(config.get(n)));
							cfData1[idx] = ModbusUtil.registerBEToShort(data, 0);
							cfData1[idx + 1] = ModbusUtil.registerBEToShort(data, 2);
							break;

						default:
							cfData1[idx] = Integer.parseInt(config.get(n));
							break;
					}
					cfConfig1Regs.add(n);
				} else {
					cfData2[n - MBREG_CONFIG2_START] = Integer.parseInt(config.get(n));
					cfConfig2Regs.add(n);
				}

				if (!cfConfig1Regs.isEmpty()) {
					byte[] data = new byte[MBREG_CONFIG1_NUM * 2];
					for (int i = 0; i < MBREG_CONFIG1_NUM; i++) {
						byte[] temp = ModbusUtil.unsignedShortToRegister(cfData1[i]);
						data[2 * i] = temp[0];
						data[2 * i + 1] = temp[1];
					}
					results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG1_START, data);
					if (results != null) {
						registers.addAll(cfConfig1Regs);
					}
				}

				if (!cfConfig2Regs.isEmpty()) {
					byte[] data = new byte[MBREG_CONFIG2_NUM * 2];
					for (int i = 0; i < MBREG_CONFIG2_NUM; i++) {
						byte[] temp = ModbusUtil.unsignedShortToRegister(cfData2[i]);
						data[2 * i] = temp[0];
						data[2 * i + 1] = temp[1];
					}
					results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG2_START, data);
					if (results != null) {
						registers.addAll(cfConfig2Regs);
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
				V_L1 = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_V_L1 * 2);
				V_L2 = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_V_L2 * 2);
				V_L3 = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_V_L3 * 2);
				V_L1_L2 = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_V_L1_L2 * 2);
				V_L2_L3 = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_V_L2_L3 * 2);
				V_L3_L1 = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_V_L3_L1 * 2);
				A_L1 = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_A_L1 * 2);
				A_L2 = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_A_L2 * 2);
				A_L3 = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_A_L3 * 2);

				kW_L1 = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_kW_L1 * 2);
				kW_L2 = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_kW_L2 * 2);
				kW_L3 = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_kW_L3 * 2);

				kVAR_L1 = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_kVAR_L1 * 2);
				kVAR_L2 = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_kVAR_L2 * 2);
				kVAR_L3 = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_kVAR_L3 * 2);

				kVA_L1 = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_kVA_L1 * 2);
				kVA_L2 = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_kVA_L2 * 2);
				kVA_L3 = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_kVA_L3 * 2);
			} else if (idx == 1) {
				kWh_System = ModbusUtil.registersBEToInt(data, OFFSET_DATA + OFFSET_kWh_System);
			} else if (idx == 2) {
				Hz_System = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_Hz_System * 2);
				PF_System = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_PF_System * 2);
				kW_System = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_kW_System * 2);
				kVAR_System = ModbusUtil.registerBEToShort(data, OFFSET_DATA + OFFSET_kVAR_System * 2);
			}
			return true;
		}

		if (mode == CONFIG_MODE) {
			if (idx == 0) {
				for (int i = 0; i < MBREG_CONFIG1_NUM; i++) {
					cfData1[i] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * i);
				}
			} else if (idx == 1) {
				for (int i = 0; i < MBREG_CONFIG2_NUM; i++) {
					cfData2[i] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * i);
				}
			}
			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		if (kWh_System >= prev_kWh_System) {
			kWh_System_diff = kWh_System - prev_kWh_System;
		} else {
			if ((kWh_System < 100) && ((Integer.MAX_VALUE - prev_kWh_System) < 100)) {
				kWh_System_diff = Integer.MAX_VALUE - prev_kWh_System + kWh_System;
			} else {
				kWh_System_diff = 0;
			}
		}
		prev_kWh_System = kWh_System;
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
		data.append(";Voltage L1_L2=" + vformat.format(V_L1_L2 * Scalar_V) + ",V" + ";Voltage L2_L3=" + vformat.format(V_L2_L3 * Scalar_V) + ",V"
				+ ";Voltage L1_L3=" + vformat.format(V_L3_L1 * Scalar_V) + ",V");
		data.append(";Active Energy Reading=" + vformat.format(kWh_System * Scalar_Energy) + ",kWh" + ";Active Energy="
				+ vformat.format((kWh_System_diff) * Scalar_Energy * 1000.0) + ",Wh");
		data.append(";Active Power=" + vformat.format(kW_System * Scalar_Power) + ",kW" + ";Reactive Power="
				+ vformat.format(kVAR_System * Scalar_Power) + ",kVAR" + ";Apparent Power=" + vformat.format(kVA_System * Scalar_Power) + ",kVA"
				+ ";Power Factor=" + vformat.format(PF_System * Scalar_PF) + ",None" + ";Frequency=" + vformat.format(Hz_System * Scalar_Hz)
				+ ",Hz");
		// Phase L1
		data.append("|DEVICEID=" + getId() + "-0-1");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Current=" + vformat.format(A_L1 * Scalar_A) + ",A" + ";Voltage=" + vformat.format(V_L1 * Scalar_V) + ",V" + ";Active Power="
				+ vformat.format(kW_L1 * Scalar_Power) + ",kW" + ";Reactive Power=" + vformat.format(kVAR_L1 * Scalar_Power) + ",kVAR"
				+ ";Apparent Power=" + vformat.format(kVA_L1 * Scalar_Power) + ",kVA");
		// Phase L2
		data.append("|DEVICEID=" + getId() + "-0-2");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Current=" + vformat.format(A_L2 * Scalar_A) + ",A" + ";Voltage=" + vformat.format(V_L2 * Scalar_V) + ",V" + ";Active Power="
				+ vformat.format(kW_L2 * Scalar_Power) + ",kW" + ";Reactive Power=" + vformat.format(kVAR_L2 * Scalar_Power) + ",kVAR"
				+ ";Apparent Power=" + vformat.format(kVA_L2 * Scalar_Power) + ",kVA");
		// Phase L3
		data.append("|DEVICEID=" + getId() + "-0-3");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Current=" + vformat.format(A_L3 * Scalar_A) + ",A" + ";Voltage=" + vformat.format(V_L3 * Scalar_V) + ",V" + ";Active Power="
				+ vformat.format(kW_L3 * Scalar_Power) + ",kW" + ";Reactive Power=" + vformat.format(kVAR_L3 * Scalar_Power) + ",kVAR"
				+ ";Apparent Power=" + vformat.format(kVA_L3 * Scalar_Power) + ",kVA");

		return data.toString();
	}

}
