package com.greenkoncepts.gateway.adapter.dent;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class PowerScout3 extends DentDevice {
	public static int MBREG_DATA_START = 4000;
	public static int MBREG_DATA_NUM = 70;
	public static int MBREG_CONFIG_START = 4300;
	public static int MBREG_CONFIG_NUM = 3;

	public static int posCTValue = 0;
	public static int posDataScalar = 1;
	public static int posDemandWindowSize = 2;

	private int[] cfData = new int[MBREG_CONFIG_NUM];
	private boolean hasGettingConfig = false;

	private double Scalar_kW_kWh_Demand = 0.1;
	private double Scalar_kVAR_kVARh = 0.1;
	private double Scalar_kVA_kVAh = 0.1;
	private double Scalar_Power_Factor = 0.01;
	private double Scalar_Amps = 0.1;
	private double Scalar_Volts = 0.1;
	private double Scalar_Freq = 0.1;

	private int prev_kWh_System = 0;
	private int kWh_System = 0;
	private int kWh_delta_System = 0;
	private int kW_System = 0;
	private int kW_Demand_System_Max = 0;
	private int prev_kVARh_System = 0;
	private int kVARh_System = 0;
	private int kVARh_delta_System = 0;
	private int kVAR_System = 0;
	private int prev_kVAh_System = 0;
	private int kVAh_System = 0;
	private int kVAh_delta_System = 0;
	private int kVA_System = 0;
	//private int Displacement_PF_System = 0;
	private int Apparent_PF_System = 0;
	//private int Amps_System_Avg = 0;
	private int Volts_Line_to_Neutral_Avg = 0;
	private int Volts_L1_to_L2 = 0;
	private int Volts_L2_to_L3 = 0;
	private int Volts_L1_to_L3 = 0;
	private int Line_Frequency = 0;

	private int prev_kWh_L1 = 0;
	private int kWh_L1 = 0;
	private int kWh_delta_L1 = 0;
	private int kW_L1 = 0;
	private int prev_kVARh_L1 = 0;
	private int kVARh_L1 = 0;
	private int kVARh_delta_L1 = 0;
	private int kVAR_L1 = 0;
	private int prev_kVAh_L1 = 0;
	private int kVAh_L1 = 0;
	private int kVAh_delta_L1 = 0;
	private int kVA_L1 = 0;
	//private int Displacement_PF_L1 = 0;
	private int Apparent_PF_L1 = 0;
	private int Amps_L1 = 0;
	private int Volts_L1_to_Neutral = 0;

	private int prev_kWh_L2 = 0;
	private int kWh_L2 = 0;
	private int kWh_delta_L2 = 0;
	private int kW_L2 = 0;
	private int prev_kVARh_L2 = 0;
	private int kVARh_L2 = 0;
	private int kVARh_delta_L2 = 0;
	private int kVAR_L2 = 0;
	private int prev_kVAh_L2 = 0;
	private int kVAh_L2 = 0;
	private int kVAh_delta_L2 = 0;
	private int kVA_L2 = 0;
	//private int Displacement_PF_L2 = 0;
	private int Apparent_PF_L2 = 0;
	private int Amps_L2 = 0;
	private int Volts_L2_to_Neutral = 0;

	private int prev_kWh_L3 = 0;
	private int kWh_L3 = 0;
	private int kWh_delta_L3 = 0;
	private int kW_L3 = 0;
	private int prev_kVARh_L3 = 0;
	private int kVARh_L3 = 0;
	private int kVARh_delta_L3 = 0;
	private int kVAR_L3 = 0;
	private int prev_kVAh_L3 = 0;
	private int kVAh_L3 = 0;
	private int kVAh_delta_L3 = 0;
	private int kVA_L3 = 0;
	//private int Displacement_PF_L3 = 0;
	private int Apparent_PF_L3 = 0;
	private int Amps_L3 = 0;
	private int Volts_L3_to_Neutral = 0;

	public PowerScout3(String category, int addr) {
		super(category, addr);
		if (category.equals(DentAdapter.DEVICE_PM100_18)) {
			Scalar_Freq = 0.01;
			MBREG_CONFIG_START = 4300;
		}
		else if (category.equals(DentAdapter.DEVICE_PM100_3)) {
			Scalar_Freq = 0.1;
			MBREG_CONFIG_START = 4600;
		}
		hasGettingConfig = false;
	}

	@Override
	public String getDeviceData() {
		byte[] data = null;
		if (!hasGettingConfig) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START, MBREG_CONFIG_NUM);
			if (decodingData(0, data, CONFIG_MODE)) {
				setDeviceDataScalar();
				hasGettingConfig = true;
			}
		}
		data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
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
			// data from System
			item.put("act_energy", formatValue(kWh_System * Scalar_kW_kWh_Demand));
			item.put("act_power", formatValue(kW_System * Scalar_kW_kWh_Demand));
			item.put("rea_energy", formatValue(kVARh_System * Scalar_kVAR_kVARh));
			item.put("rea_power", formatValue(kVAR_System * Scalar_kVAR_kVARh));
			item.put("app_energy", formatValue(kVAh_System * Scalar_kVA_kVAh));
			item.put("app_power", formatValue(kVA_System * Scalar_kVA_kVAh));
			item.put("pow_factor", formatValue(Apparent_PF_System * Scalar_Power_Factor));
			item.put("current", formatValue((Amps_L1 + Amps_L2 + Amps_L3) * Scalar_Amps));
			item.put("voltage", formatValue(Volts_Line_to_Neutral_Avg * Scalar_Volts));
			item.put("v_l1_l2", formatValue(Volts_L1_to_L2 * Scalar_Volts));
			item.put("v_l2_l3", formatValue(Volts_L2_to_L3 * Scalar_Volts));
			item.put("v_l1_l3", formatValue(Volts_L1_to_L3 * Scalar_Volts));
			item.put("frequency", formatValue(Line_Frequency * Scalar_Freq));
			item.put("peak_demand", formatValue(kW_Demand_System_Max * Scalar_kW_kWh_Demand));
			// data from Phase L1
			item.put("act_energy_l1", formatValue(kWh_L1 * Scalar_kW_kWh_Demand));
			item.put("act_power_l1", formatValue(kW_L1 * Scalar_kW_kWh_Demand));
			item.put("rea_energy_l1", formatValue(kVARh_L1 * Scalar_kVAR_kVARh));
			item.put("rea_power_l1", formatValue(kVAR_L1 * Scalar_kVAR_kVARh));
			item.put("app_energy_l1", formatValue(kVAh_L1 * Scalar_kVA_kVAh));
			item.put("app_power_l1", formatValue(kVA_L1 * Scalar_kVA_kVAh));
			item.put("pow_factor_l1", formatValue(Apparent_PF_L1 * Scalar_Power_Factor));
			item.put("current_l1", formatValue((Amps_L1 * Scalar_Amps) * Scalar_Amps));
			item.put("voltage_l1", formatValue(Volts_L1_to_Neutral * Scalar_Volts));
			// data from Phase L2
			item.put("act_energy_l2", formatValue(kWh_L2 * Scalar_kW_kWh_Demand));
			item.put("act_power_l2", formatValue(kW_L2 * Scalar_kW_kWh_Demand));
			item.put("rea_energy_l2", formatValue(kVARh_L2 * Scalar_kVAR_kVARh));
			item.put("rea_power_l2", formatValue(kVAR_L2 * Scalar_kVAR_kVARh));
			item.put("app_energy_l2", formatValue(kVAh_L2 * Scalar_kVA_kVAh));
			item.put("app_power_l2", formatValue(kVA_L2 * Scalar_kVA_kVAh));
			item.put("pow_factor_l2", formatValue(Apparent_PF_L2 * Scalar_Power_Factor));
			item.put("current_l2", formatValue((Amps_L2 * Scalar_Amps) * Scalar_Amps));
			item.put("voltage_l2", formatValue(Volts_L2_to_Neutral * Scalar_Volts));
			// data from Phase L3
			item.put("act_energy_l3", formatValue(kWh_L3 * Scalar_kW_kWh_Demand));
			item.put("act_power_l3", formatValue(kW_L3 * Scalar_kW_kWh_Demand));
			item.put("rea_energy_l3", formatValue(kVARh_L3 * Scalar_kVAR_kVARh));
			item.put("rea_power_l3", formatValue(kVAR_L3 * Scalar_kVAR_kVARh));
			item.put("app_energy_l3", formatValue(kVAh_L3 * Scalar_kVA_kVAh));
			item.put("app_power_l3", formatValue(kVA_L3 * Scalar_kVA_kVAh));
			item.put("pow_factor_l3", formatValue(Apparent_PF_L3 * Scalar_Power_Factor));
			item.put("current_l3", formatValue((Amps_L3 * Scalar_Amps) * Scalar_Amps));
			item.put("voltage_l3", formatValue(Volts_L3_to_Neutral * Scalar_Volts));
			real_time_data.add(item);
		}
		return real_time_data;
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START, MBREG_CONFIG_NUM);
		device_config.clear();
		if (decodingData(0, data, CONFIG_MODE)) {
			device_config.put(MBREG_CONFIG_START + posCTValue, String.valueOf(cfData[posCTValue]));
			device_config.put(MBREG_CONFIG_START + posDataScalar, String.valueOf(cfData[posDataScalar]));
			device_config.put(MBREG_CONFIG_START + posDemandWindowSize, String.valueOf(cfData[posDemandWindowSize]));
		}
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		if ((config != null) && (!config.isEmpty())) {
			for (Integer n : config.keySet()) {
				byte[] data = ModbusUtil.unsignedShortToRegister(Integer.parseInt(config.get(n)));
				byte[] results = modbus.writeSingleRegister(modbusid, n, data);
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
			kWh_System = ModbusUtil.registersMEToInt(data, (4000 - 4000) * 2 + OFFSET_DATA);
			kW_System = ModbusUtil.registerToShort(data, (4002 - 4000) * 2 + OFFSET_DATA);
			kW_Demand_System_Max = ModbusUtil.registerToShort(data, (4003 - 4000) * 2 + OFFSET_DATA);
			kVARh_System = ModbusUtil.registersMEToInt(data, (4007 - 4000) * 2 + OFFSET_DATA);
			kVAR_System = ModbusUtil.registerToShort(data, (4009 - 4000) * 2 + OFFSET_DATA);
			kVAh_System = ModbusUtil.registersMEToInt(data, (4010 - 4000) * 2 + OFFSET_DATA);
			kVA_System = ModbusUtil.registerToShort(data, (4012 - 4000) * 2 + OFFSET_DATA);
			//Displacement_PF_System = ModbusUtil.registerToShort(data, (4013 - 4000) * 2 + OFFSET_DATA);
			Apparent_PF_System = ModbusUtil.registerToShort(data, (4014 - 4000) * 2 + OFFSET_DATA);
			//Amps_System_Avg = ModbusUtil.registerToShort(data, (4015 - 4000) * 2 + OFFSET_DATA);
			Volts_Line_to_Neutral_Avg = ModbusUtil.registerToShort(data, (4017 - 4000) * 2 + OFFSET_DATA);
			Volts_L1_to_L2 = ModbusUtil.registerToShort(data, (4018 - 4000) * 2 + OFFSET_DATA);
			Volts_L2_to_L3 = ModbusUtil.registerToShort(data, (4019 - 4000) * 2 + OFFSET_DATA);
			Volts_L1_to_L3 = ModbusUtil.registerToShort(data, (4020 - 4000) * 2 + OFFSET_DATA);
			Line_Frequency = ModbusUtil.registerToShort(data, (4021 - 4000) * 2 + OFFSET_DATA);
			kWh_L1 = ModbusUtil.registersMEToInt(data, (4022 - 4000) * 2 + OFFSET_DATA);
			kW_L1 = ModbusUtil.registerToShort(data, (4028 - 4000) * 2 + OFFSET_DATA);
			kVARh_L1 = ModbusUtil.registersMEToInt(data, (4031 - 4000) * 2 + OFFSET_DATA);
			kVAR_L1 = ModbusUtil.registerToShort(data, (4037 - 4000) * 2 + OFFSET_DATA);
			kVAh_L1 = ModbusUtil.registersMEToInt(data, (4040 - 4000) * 2 + OFFSET_DATA);
			kVA_L1 = ModbusUtil.registerToShort(data, (4046 - 4000) * 2 + OFFSET_DATA);
			//Displacement_PF_L1 = ModbusUtil.registerToShort(data, (4049 - 4000) * 2 + OFFSET_DATA);
			Apparent_PF_L1 = ModbusUtil.registerToShort(data, (4052 - 4000) * 2 + OFFSET_DATA);
			Amps_L1 = ModbusUtil.registerToShort(data, (4055 - 4000) * 2 + OFFSET_DATA);
			Volts_L1_to_Neutral = ModbusUtil.registerToShort(data, (4058 - 4000) * 2 + OFFSET_DATA);
			kWh_L2 = ModbusUtil.registersMEToInt(data, (4024 - 4000) * 2 + OFFSET_DATA);
			kW_L2 = ModbusUtil.registerToShort(data, (4029 - 4000) * 2 + OFFSET_DATA);
			kVARh_L2 = ModbusUtil.registersMEToInt(data, (4033 - 4000) * 2 + OFFSET_DATA);
			kVAR_L2 = ModbusUtil.registerToShort(data, (4038 - 4000) * 2 + OFFSET_DATA);
			kVAh_L2 = ModbusUtil.registersMEToInt(data, (4042 - 4000) * 2 + OFFSET_DATA);
			kVA_L2 = ModbusUtil.registerToShort(data, (4047 - 4000) * 2 + OFFSET_DATA);
			//Displacement_PF_L2 = ModbusUtil.registerToShort(data, (4050 - 4000) * 2 + OFFSET_DATA);
			Apparent_PF_L2 = ModbusUtil.registerToShort(data, (4053 - 4000) * 2 + OFFSET_DATA);
			Amps_L2 = ModbusUtil.registerToShort(data, (4056 - 4000) * 2 + OFFSET_DATA);
			Volts_L2_to_Neutral = ModbusUtil.registerToShort(data, (4059 - 4000) * 2 + OFFSET_DATA);
			kWh_L3 = ModbusUtil.registersMEToInt(data, (4026 - 4000) * 2 + OFFSET_DATA);
			kW_L3 = ModbusUtil.registerToShort(data, (4030 - 4000) * 2 + OFFSET_DATA);
			kVARh_L3 = ModbusUtil.registersMEToInt(data, (4035 - 4000) * 2 + OFFSET_DATA);
			kVAR_L3 = ModbusUtil.registerToShort(data, (4039 - 4000) * 2 + OFFSET_DATA);
			kVAh_L3 = ModbusUtil.registersMEToInt(data, (4044 - 4000) * 2 + OFFSET_DATA);
			kVA_L3 = ModbusUtil.registerToShort(data, (4048 - 4000) * 2 + OFFSET_DATA);
			//Displacement_PF_L3 = ModbusUtil.registerToShort(data, (4051 - 4000) * 2 + OFFSET_DATA);
			Apparent_PF_L3 = ModbusUtil.registerToShort(data, (4054 - 4000) * 2 + OFFSET_DATA);
			Amps_L3 = ModbusUtil.registerToShort(data, (4057 - 4000) * 2 + OFFSET_DATA);
			Volts_L3_to_Neutral = ModbusUtil.registerToShort(data, (4060 - 4000) * 2 + OFFSET_DATA);
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
		// kWh_System
		if ((prev_kWh_System == 0) || (prev_kWh_System > kWh_System)) {
			kWh_delta_System = 0;
		} else {
			kWh_delta_System = kWh_System - prev_kWh_System;
		}
		prev_kWh_System = kWh_System;
		// kVARh_System
		if ((prev_kVARh_System == 0) || (prev_kVARh_System > kVARh_System)) {
			kVARh_delta_System = 0;
		} else {
			kVARh_delta_System = kVARh_System - prev_kVARh_System;
		}
		prev_kVARh_System = kVARh_System;
		// kVARh_System
		if ((prev_kVAh_System == 0) || (prev_kVAh_System > kVAh_System)) {
			kVAh_delta_System = 0;
		} else {
			kVAh_delta_System = kVAh_System - prev_kVAh_System;
		}
		prev_kVAh_System = kVAh_System;

		// kWh_L1
		if ((prev_kWh_L1 == 0) || (prev_kWh_L1 > kWh_L1)) {
			kWh_delta_L1 = 0;
		} else {
			kWh_delta_L1 = kWh_L1 - prev_kWh_L1;
		}
		prev_kWh_L1 = kWh_L1;
		// kVARh_L1
		if ((prev_kVARh_L1 == 0) || (prev_kVARh_L1 > kVARh_L1)) {
			kVARh_delta_L1 = 0;
		} else {
			kVARh_delta_L1 = kVARh_L1 - prev_kVARh_L1;
		}
		prev_kVARh_L1 = kVARh_L1;
		// kVAh_L1
		if ((prev_kVAh_L1 == 0) || (prev_kVAh_L1 > kVAh_L1)) {
			kVAh_delta_L1 = 0;
		} else {
			kVAh_delta_L1 = kVAh_L1 - prev_kVAh_L1;
		}
		prev_kVAh_L1 = kVAh_L1;

		// kWh_L2
		if ((prev_kWh_L2 == 0) || (prev_kWh_L2 > kWh_L2)) {
			kWh_delta_L2 = 0;
		} else {
			kWh_delta_L2 = kWh_L2 - prev_kWh_L2;
		}
		prev_kWh_L2 = kWh_L2;

		// kVARh_L2
		if ((prev_kVARh_L2 == 0) || (prev_kVARh_L2 > kVARh_L2)) {
			kVARh_delta_L2 = 0;
		} else {
			kVARh_delta_L2 = kVARh_L2 - prev_kVARh_L2;
		}
		prev_kVARh_L2 = kVARh_L2;

		// kVAh_L2
		if ((prev_kVAh_L2 == 0) || (prev_kVAh_L2 > kVAh_L2)) {
			kVAh_delta_L2 = 0;
		} else {
			kVAh_delta_L2 = kVAh_L2 - prev_kVAh_L2;
		}
		prev_kVAh_L2 = kVAh_L2;

		// kWh_L3
		if ((prev_kWh_L3 == 0) || (prev_kWh_L3 > kWh_L3)) {
			kWh_delta_L3 = 0;
		} else {
			kWh_delta_L3 = kWh_L3 - prev_kWh_L3;
		}
		prev_kWh_L3 = kWh_L3;

		// kVARh_L3
		if ((prev_kVARh_L3 == 0) || (prev_kVARh_L3 > kVARh_L3)) {
			kVARh_delta_L3 = 0;
		} else {
			kVARh_delta_L3 = kVARh_L3 - prev_kVARh_L3;
		}
		prev_kVARh_L3 = kVARh_L3;

		// kVAh_L3
		if ((prev_kVAh_L3 == 0) || (prev_kVAh_L3 > kVAh_L3)) {
			kVAh_delta_L3 = 0;
		} else {
			kVAh_delta_L3 = kVAh_L3 - prev_kVAh_L3;
		}
		prev_kVAh_L3 = kVAh_L3;

	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		return "|"
				+ "DEVICEID=" + getId()
				+ "-0-0"
				+ ";TIMESTAMP=" + timestamp
				+ ";Active Energy Reading=" + formatValue(kWh_System * Scalar_kW_kWh_Demand) + ",kWh"
				+ ";Active Energy=" + formatValue(kWh_delta_System * Scalar_kW_kWh_Demand * 1000.0) + ",Wh"
				+ ";Active Power=" + formatValue(kW_System * Scalar_kW_kWh_Demand) + ",kW"
				+ ";Reactive Energy Reading=" + formatValue(kVARh_System * Scalar_kVAR_kVARh) + ",kVARh"
				+ ";Reactive Energy=" + formatValue(kVARh_delta_System * Scalar_kVAR_kVARh) + ",kVARh"
				+ ";Reactive Power=" + formatValue(kVAR_System * Scalar_kVAR_kVARh) + ",kVAR"
				+ ";Apparent Energy Reading=" + formatValue(kVAh_System * Scalar_kVA_kVAh) + ",kVAh"
				+ ";Apparent Energy=" + formatValue(kVAh_delta_System * Scalar_kVA_kVAh) + ",kVAh"
				+ ";Apparent Power=" + formatValue(kVA_System * Scalar_kVA_kVAh) + ",kVA"
				+ ";Power Factor=" + formatValue(Apparent_PF_System * Scalar_Power_Factor) + ",None"
				// + ";Current=" + formatValue(Amps_System_Avg*Scalar_Amps) + ",A"
				+ ";Current=" + formatValue((Amps_L1 + Amps_L2 + Amps_L3) * Scalar_Amps) + ",A"
				+ ";Voltage=" + formatValue(Volts_Line_to_Neutral_Avg * Scalar_Volts) + ",V"
				+ ";Voltage L1-L2=" + formatValue(Volts_L1_to_L2 * Scalar_Volts) + ",V"
				+ ";Voltage L2-L3=" + formatValue(Volts_L2_to_L3 * Scalar_Volts) + ",V"
				+ ";Voltage L1-L3=" + formatValue(Volts_L1_to_L3 * Scalar_Volts) + ",V"
				+ ";Frequency=" + formatValue(Line_Frequency * Scalar_Freq) + ",Hz"
				+ ";Peak Demand=" + formatValue(kW_Demand_System_Max * Scalar_kW_kWh_Demand) + ",kW"

				+ "|"
				+ "DEVICEID=" + getId()
				+ "-0-1"
				+ ";TIMESTAMP=" + timestamp
				+ ";Active Energy Reading=" + formatValue(kWh_L1 * Scalar_kW_kWh_Demand) + ",kWh"
				+ ";Active Energy=" + formatValue(kWh_delta_L1 * Scalar_kW_kWh_Demand * 1000.0) + ",Wh"
				+ ";Active Power=" + formatValue(kW_L1 * Scalar_kW_kWh_Demand) + ",kW"
				+ ";Reactive Energy Reading=" + formatValue(kVARh_L1 * Scalar_kVAR_kVARh) + ",kVARh"
				+ ";Reactive Energy=" + formatValue(kVARh_delta_L1 * Scalar_kVAR_kVARh) + ",kVARh"
				+ ";Reactive Power=" + formatValue(kVAR_L1 * Scalar_kVAR_kVARh) + ",kVAR"
				+ ";Apparent Energy Reading=" + formatValue(kVAh_L1 * Scalar_kVA_kVAh) + ",kVAh"
				+ ";Apparent Energy=" + formatValue(kVAh_delta_L1 * Scalar_kVA_kVAh) + ",kVAh"
				+ ";Apparent Power=" + formatValue(kVA_L1 * Scalar_kVA_kVAh) + ",kVA"
				+ ";Power Factor=" + formatValue(Apparent_PF_L1 * Scalar_Power_Factor) + ",None"
				+ ";Current=" + formatValue(Amps_L1 * Scalar_Amps) + ",A"
				+ ";Voltage=" + formatValue(Volts_L1_to_Neutral * Scalar_Volts) + ",V"

				+ "|"
				+ "DEVICEID=" + getId()
				+ "-0-2"
				+ ";TIMESTAMP=" + timestamp
				+ ";Active Energy Reading=" + formatValue(kWh_L2 * Scalar_kW_kWh_Demand) + ",kWh"
				+ ";Active Energy=" + formatValue(kWh_delta_L2 * Scalar_kW_kWh_Demand * 1000.0) + ",Wh"
				+ ";Active Power=" + formatValue(kW_L2 * Scalar_kW_kWh_Demand) + ",kW"
				+ ";Reactive Energy Reading=" + formatValue(kVARh_L2 * Scalar_kVAR_kVARh) + ",kVARh"
				+ ";Reactive Energy=" + formatValue(kVARh_delta_L2 * Scalar_kVAR_kVARh) + ",kVARh"
				+ ";Reactive Power=" + formatValue(kVAR_L2 * Scalar_kVAR_kVARh) + ",kVAR"
				+ ";Apparent Energy Reading=" + formatValue(kVAh_L2 * Scalar_kVA_kVAh) + ",kVAh"
				+ ";Apparent Energy=" + formatValue(kVAh_delta_L2 * Scalar_kVA_kVAh) + ",kVAh"
				+ ";Apparent Power=" + formatValue(kVA_L2 * Scalar_kVA_kVAh) + ",kVA"
				+ ";Power Factor=" + formatValue(Apparent_PF_L2 * Scalar_Power_Factor) + ",None"
				+ ";Current=" + formatValue(Amps_L2 * Scalar_Amps) + ",A"
				+ ";Voltage=" + formatValue(Volts_L2_to_Neutral * Scalar_Volts) + ",V"

				+ "|"
				+ "DEVICEID=" + getId()
				+ "-0-3"
				+ ";TIMESTAMP=" + timestamp
				+ ";Active Energy Reading=" + formatValue(kWh_L3 * Scalar_kW_kWh_Demand) + ",kWh"
				+ ";Active Energy=" + formatValue(kWh_delta_L3 * Scalar_kW_kWh_Demand * 1000.0) + ",Wh"
				+ ";Active Power=" + formatValue(kW_L3 * Scalar_kW_kWh_Demand) + ",kW"
				+ ";Reactive Energy Reading=" + formatValue(kVARh_L3 * Scalar_kVAR_kVARh) + ",kVARh"
				+ ";Reactive Energy=" + formatValue(kVARh_delta_L3 * Scalar_kVAR_kVARh) + ",kVARh"
				+ ";Reactive Power=" + formatValue(kVAR_L3 * Scalar_kVAR_kVARh) + ",kVAR"
				+ ";Apparent Energy Reading=" + formatValue(kVAh_L3 * Scalar_kVA_kVAh) + ",kVAh"
				+ ";Apparent Energy=" + formatValue(kVAh_delta_L3 * Scalar_kVA_kVAh) + ",kVAh"
				+ ";Apparent Power=" + formatValue(kVA_L3 * Scalar_kVA_kVAh) + ",kVA"
				+ ";Power Factor=" + formatValue(Apparent_PF_L3 * Scalar_Power_Factor) + ",None"
				+ ";Current=" + formatValue(Amps_L3 * Scalar_Amps) + ",A"
				+ ";Voltage=" + formatValue(Volts_L3_to_Neutral * Scalar_Volts) + ",V"
				;
	}

	private String formatValue(double value) {
		return vformat.format(Math.rint(value * 1000.0) / 1000.0);
	}

	private void setDeviceDataScalar() {
		switch (cfData[posDataScalar]) {
			case 0:
				Scalar_kW_kWh_Demand = 0.00001;
				Scalar_kVAR_kVARh = 0.00001;
				Scalar_kVA_kVAh = 0.00001;
				Scalar_Power_Factor = 0.01;
				Scalar_Amps = 0.01;
				Scalar_Volts = 0.1;
				break;
			case 1:
				Scalar_kW_kWh_Demand = 0.001;
				Scalar_kVAR_kVARh = 0.001;
				Scalar_kVA_kVAh = 0.001;
				Scalar_Power_Factor = 0.01;
				Scalar_Amps = 0.1;
				Scalar_Volts = 0.1;
				break;
			case 2:
				Scalar_kW_kWh_Demand = 0.01;
				Scalar_kVAR_kVARh = 0.01;
				Scalar_kVA_kVAh = 0.01;
				Scalar_Power_Factor = 0.01;
				Scalar_Amps = 0.1;
				Scalar_Volts = 0.1;
				break;
			case 3:
				Scalar_kW_kWh_Demand = 0.1;
				Scalar_kVAR_kVARh = 0.1;
				Scalar_kVA_kVAh = 0.1;
				Scalar_Power_Factor = 0.01;
				Scalar_Amps = 0.1;
				Scalar_Volts = 0.1;
				break;
			case 4:
				Scalar_kW_kWh_Demand = 1.0;
				Scalar_kVAR_kVARh = 1.0;
				Scalar_kVA_kVAh = 1.0;
				Scalar_Power_Factor = 0.01;
				Scalar_Amps = 1.0;
				Scalar_Volts = 1.0;
				break;
			case 5:
				Scalar_kW_kWh_Demand = 10.0;
				Scalar_kVAR_kVARh = 10.0;
				Scalar_kVA_kVAh = 10.0;
				Scalar_Power_Factor = 0.01;
				Scalar_Amps = 1.0;
				Scalar_Volts = 1.0;
				break;
			case 6:
			default:
				Scalar_kW_kWh_Demand = 100.0;
				Scalar_kVAR_kVARh = 100.0;
				Scalar_kVA_kVAh = 100.0;
				Scalar_Power_Factor = 0.01;
				Scalar_Amps = 1.0;
				Scalar_Volts = 1.0;
		}
	}

}
