package com.greenkoncepts.gateway.adapter.dent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class PowerScout3plus extends DentDevice {
	private DecimalFormat vformat = new DecimalFormat("#########0.0000");

	public static int MBREG_DATA1_START = 4000;
	public static int MBREG_DATA1_NUM = 70;
	public static int MBREG_DATA2_START = 7000;
	public static int MBREG_DATA2_NUM = 7;

	public static int MBREG_CONFIG_START = 4524;
	public static int MBREG_CONFIG_NUM = 88;
	public static int posCTType = 0;
	public static int posSlaveID = 1;
	public static int posCTPhaseShift = 74;
	public static int posCTInteger = 75;
	public static int posCTDecimal = 76;
	public static int posDataScalar = 77;
	public static int posDemandWindowSize = 78;
	public static int posVoltsMultiplier = 79;
	public static int posAmpsMultiplier = 80;
	public static int posCommunicationSetting = 81;
	public static int posServiceType = 82;
	public static int posSetLineFrequency = 84;
	public static int posCommunicationsSettings = 87;

	private int CTType = 0;
	private int slaveID = 0;
	private int CTPhaseShift = 0;
	private int CTInteger = 0;
	private int CTDecimal = 0;
	private int dataScalar = 0;
	private int demandWindowSize = 0;
	private int voltsMultiplier = 0;
	private int ampsMultiplier = 0;
	private int communicationSetting = 0;
	private int serviceType = 0;
	private int setLineFrequency = 0;
	private int communicationsSettings = 0;

	private double Scalar_kW_kWh_Demand = 0.1;
	private double Scalar_kVAR_kVARh = 0.1;
	private double Scalar_kVA_kVAh = 0.1;
	private double Scalar_Power_Factor = 0.01;
	private double Scalar_Amps = 0.1;
	private double Scalar_Volts = 0.1;
	private double Scalar_Freq = 0.1;

	private long prev_kWh_System = 0;
	private long kWh_System = 0;
	private long kWh_delta_System = 0;
	private int kW_System = 0;
	private int kW_Demand_System_Max = 0;
	private long prev_kVARh_System = 0;
	private long kVARh_System = 0;
	private long kVARh_delta_System = 0;
	private int kVAR_System = 0;
	private long prev_kVAh_System = 0;
	private long kVAh_System = 0;
	private long kVAh_delta_System = 0;
	private int kVA_System = 0;
	// private int Displacement_PF_System = 0;
	private int Apparent_PF_System = 0;
	// private int Amps_System_Avg = 0;
	private int Volts_Line_to_Neutral_Avg = 0;
	private int Volts_L1_to_L2 = 0;
	private int Volts_L2_to_L3 = 0;
	private int Volts_L1_to_L3 = 0;
	private int Line_Frequency = 0;

	private long prev_kWh_L1 = 0;
	private long kWh_L1 = 0;
	private long kWh_delta_L1 = 0;
	private int kW_L1 = 0;
	private long prev_kVARh_L1 = 0;
	private long kVARh_L1 = 0;
	private long kVARh_delta_L1 = 0;
	private int kVAR_L1 = 0;
	private long prev_kVAh_L1 = 0;
	private long kVAh_L1 = 0;
	private long kVAh_delta_L1 = 0;
	private int kVA_L1 = 0;
	// private int Displacement_PF_L1 = 0;
	private int Apparent_PF_L1 = 0;
	private int Amps_L1 = 0;
	private int Volts_L1_to_Neutral = 0;

	private long prev_kWh_L2 = 0;
	private long kWh_L2 = 0;
	private long kWh_delta_L2 = 0;
	private int kW_L2 = 0;
	private long prev_kVARh_L2 = 0;
	private long kVARh_L2 = 0;
	private long kVARh_delta_L2 = 0;
	private int kVAR_L2 = 0;
	private long prev_kVAh_L2 = 0;
	private long kVAh_L2 = 0;
	private long kVAh_delta_L2 = 0;
	private int kVA_L2 = 0;
	// private int Displacement_PF_L2 = 0;
	private int Apparent_PF_L2 = 0;
	private int Amps_L2 = 0;
	private int Volts_L2_to_Neutral = 0;

	private long prev_kWh_L3 = 0;
	private long kWh_L3 = 0;
	private long kWh_delta_L3 = 0;
	private int kW_L3 = 0;
	private long prev_kVARh_L3 = 0;
	private long kVARh_L3 = 0;
	private long kVARh_delta_L3 = 0;
	private int kVAR_L3 = 0;
	private long prev_kVAh_L3 = 0;
	private long kVAh_L3 = 0;
	private long kVAh_delta_L3 = 0;
	private int kVA_L3 = 0;
	// private int Displacement_PF_L3 = 0;
	private int Apparent_PF_L3 = 0;
	private int Amps_L3 = 0;
	private int Volts_L3_to_Neutral = 0;

	private long prev_kWh_Negative;
	private long kWh_Negative;
	private long kWh_delta_Negative;
	private int kW_Negative;
	// private int kW_Demand_Negative_Max;

	private boolean hasGettingConfig = false;

	final public long MAX_INT = (long) (Math.pow(2, 32));

	public PowerScout3plus(String category, int addr) {
		super(category, addr);
		Scalar_Freq = 0.1;
		hasGettingConfig = true;
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
		data = modbus.readHoldingRegisters(modbusid, MBREG_DATA1_START, MBREG_DATA1_NUM);
		if (decodingData(0, data, DATA_MODE)) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_DATA2_START, MBREG_DATA2_NUM);
			if (decodingData(1, data, DATA_MODE)) {
				calculateDecodedData();
			}
		}
		return createDataSendToServer();
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA1_START, MBREG_DATA1_NUM);
			decodingData(0, data, DATA_MODE);
			data = modbus.readHoldingRegisters(modbusid, MBREG_DATA2_START, MBREG_DATA2_NUM);
			decodingData(1, data, DATA_MODE);
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
			device_config.put(MBREG_CONFIG_START + posCTType, String.valueOf(CTType));
			device_config.put(MBREG_CONFIG_START + posSlaveID, String.valueOf(slaveID));
			device_config.put(MBREG_CONFIG_START + posCTPhaseShift, String.valueOf(CTPhaseShift));
			device_config.put(MBREG_CONFIG_START + posCTInteger, String.valueOf(CTInteger));
			device_config.put(MBREG_CONFIG_START + posCTDecimal, String.valueOf(CTDecimal));
			device_config.put(MBREG_CONFIG_START + posDataScalar, String.valueOf(dataScalar));
			device_config.put(MBREG_CONFIG_START + posDemandWindowSize, String.valueOf(demandWindowSize));
			device_config.put(MBREG_CONFIG_START + posVoltsMultiplier, String.valueOf(voltsMultiplier));
			device_config.put(MBREG_CONFIG_START + posAmpsMultiplier, String.valueOf(ampsMultiplier));
			device_config.put(MBREG_CONFIG_START + posCommunicationSetting, String.valueOf(communicationSetting));
			device_config.put(MBREG_CONFIG_START + posServiceType, String.valueOf(serviceType));
			device_config.put(MBREG_CONFIG_START + posSetLineFrequency, String.valueOf(setLineFrequency));
			device_config.put(MBREG_CONFIG_START + posCommunicationsSettings, String.valueOf(communicationsSettings));

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
			if (idx == 0) {
				kWh_System = ModbusUtil.registersMEToLong(data, (4000 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kWh_System:"+kWh_System);
				kVARh_System = ModbusUtil.registersMEToLong(data, (4007 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kVARh_System:"+kVARh_System);
				kVAh_System = ModbusUtil.registersMEToLong(data, (4010 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kVAh_System:"+kVAh_System);
				kWh_L1 = ModbusUtil.registersMEToLong(data, (4022 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kWh_L1:"+kWh_L1);
				kVARh_L1 = ModbusUtil.registersMEToLong(data, (4031 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kVARh_L1:"+kVARh_L1);
				kVAh_L1 = ModbusUtil.registersMEToLong(data, (4040 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kVAh_L1:"+kVAh_L1);
				kWh_L2 = ModbusUtil.registersMEToLong(data, (4024 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kWh_L2:"+kWh_L2);
				kVARh_L2 = ModbusUtil.registersMEToLong(data, (4033 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kVARh_L2:"+kVARh_L2);
				kVAh_L2 = ModbusUtil.registersMEToLong(data, (4042 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kVAh_L2:"+kVAh_L2);
				kWh_L3 = ModbusUtil.registersMEToLong(data, (4026 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kWh_L3:"+kWh_L3);
				kVARh_L3 = ModbusUtil.registersMEToLong(data, (4035 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kVARh_L3:"+kVARh_L3);
				kVAh_L3 = ModbusUtil.registersMEToLong(data, (4044 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kVAh_L3:"+kVAh_L3);
				kW_System = ModbusUtil.registerToShort(data, (4002 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kW_System:"+kW_System);
				kW_Demand_System_Max = ModbusUtil.registerToShort(data, (4003 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kW_Demand_System_Max:"+kW_Demand_System_Max);
				kVAR_System = ModbusUtil.registerToShort(data, (4009 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kVAR_System:"+kVAR_System);
				kVA_System = ModbusUtil.registerToShort(data, (4012 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kVA_System:"+kVA_System);
				// Displacement_PF_System = ModbusUtil.registerToShort(data, (4013-4000)*2+OFFSET_DATA);
				// log(LOG_INFO,name+"Displacement_PF_System:"+Displacement_PF_System);
				Apparent_PF_System = ModbusUtil.registerToShort(data, (4014 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"Apparent_PF_System:"+Apparent_PF_System);
				// Amps_System_Avg = ModbusUtil.registerToShort(data, (4015-4000)*2+OFFSET_DATA);
				// log(LOG_INFO,name+"Amps_System_Avg:"+Amps_System_Avg);
				Volts_Line_to_Neutral_Avg = ModbusUtil.registerToShort(data, (4017 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"Volts_Line_to_Neutral_Avg:"+Volts_Line_to_Neutral_Avg);
				Volts_L1_to_L2 = ModbusUtil.registerToShort(data, (4018 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"Volts_L1_to_L2:"+Volts_L1_to_L2);
				Volts_L2_to_L3 = ModbusUtil.registerToShort(data, (4019 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"Volts_L2_to_L3:"+Volts_L2_to_L3);
				Volts_L1_to_L3 = ModbusUtil.registerToShort(data, (4020 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"Volts_L1_to_L3:"+Volts_L1_to_L3);
				Line_Frequency = ModbusUtil.registerToShort(data, (4021 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"Line_Frequency:"+Line_Frequency);
				kW_L1 = ModbusUtil.registerToShort(data, (4028 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kW_L1:"+kW_L1);
				kVAR_L1 = ModbusUtil.registerToShort(data, (4037 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kVAR_L1:"+kVAR_L1);
				kVA_L1 = ModbusUtil.registerToShort(data, (4046 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kVA_L1:"+kVA_L1);
				// Displacement_PF_L1 = ModbusUtil.registerToShort(data, (4049-4000)*2+OFFSET_DATA);
				// log(LOG_INFO,name+"Displacement_PF_L1:"+Displacement_PF_L1);
				Apparent_PF_L1 = ModbusUtil.registerToShort(data, (4052 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"Apparent_PF_L1:"+Apparent_PF_L1);
				Amps_L1 = ModbusUtil.registerToShort(data, (4055 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"Amps_L1:"+Amps_L1);
				Volts_L1_to_Neutral = ModbusUtil.registerToShort(data, (4058 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"Volts_L1_to_Neutral:"+Volts_L1_to_Neutral);
				kW_L2 = ModbusUtil.registerToShort(data, (4029 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kW_L2:"+kW_L2);
				kVAR_L2 = ModbusUtil.registerToShort(data, (4038 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kVAR_L2:"+kVAR_L2);
				kVA_L2 = ModbusUtil.registerToShort(data, (4047 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kVA_L2:"+kVA_L2);
				// Displacement_PF_L2 = ModbusUtil.registerToShort(data, (4050-4000)*2+OFFSET_DATA);
				// log(LOG_INFO,name+"Displacement_PF_L2:"+Displacement_PF_L2);
				Apparent_PF_L2 = ModbusUtil.registerToShort(data, (4053 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"Apparent_PF_L2:"+Apparent_PF_L2);
				Amps_L2 = ModbusUtil.registerToShort(data, (4056 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"Amps_L2:"+Amps_L2);
				Volts_L2_to_Neutral = ModbusUtil.registerToShort(data, (4059 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"Volts_L2_to_Neutral:"+Volts_L2_to_Neutral);
				kW_L3 = ModbusUtil.registerToShort(data, (4030 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kW_L3:"+kW_L3);
				kVAR_L3 = ModbusUtil.registerToShort(data, (4039 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kVAR_L3:"+kVAR_L3);
				kVA_L3 = ModbusUtil.registerToShort(data, (4048 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kVA_L3:"+kVA_L3);
				// Displacement_PF_L3 = ModbusUtil.registerToShort(data, (4051-4000)*2+OFFSET_DATA);
				// log(LOG_INFO,name+"Displacement_PF_L3:"+Displacement_PF_L3);
				Apparent_PF_L3 = ModbusUtil.registerToShort(data, (4054 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"Apparent_PF_L3:"+Apparent_PF_L3);
				Amps_L3 = ModbusUtil.registerToShort(data, (4057 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"Amps_L3:"+Amps_L3);
				Volts_L3_to_Neutral = ModbusUtil.registerToShort(data, (4060 - 4000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"Volts_L3_to_Neutral:"+Volts_L3_to_Neutral);
			} else if (idx == 1) {
				kWh_Negative = ModbusUtil.registersMEToInt(data, (7000 - 7000) * 2 + OFFSET_DATA);
				kW_Negative = ModbusUtil.registerToShort(data, (7002 - 7000) * 2 + OFFSET_DATA);
				// log(LOG_INFO,name+"kW_Negative:"+kW_Negative);
				// kW_Demand_Negative_Max = ModbusUtil.registerToShort(data, (7005-7000)*2+OFFSET_DATA);
				// log(LOG_INFO,name+"kW_Demand_Negative_Max:"+kW_Demand_Negative_Max);
			}
			return true;
		}
		if (mode == CONFIG_MODE) {
			CTType = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * posCTType);
			slaveID = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * posSlaveID);
			CTPhaseShift = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * posCTPhaseShift);
			CTInteger = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * posCTInteger);
			CTDecimal = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * posCTDecimal);
			dataScalar = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * posDataScalar);
			demandWindowSize = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * posDemandWindowSize);
			voltsMultiplier = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * posVoltsMultiplier);
			ampsMultiplier = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * posAmpsMultiplier);
			communicationSetting = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * posCommunicationSetting);
			serviceType = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * posServiceType);
			setLineFrequency = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * posSetLineFrequency);
			communicationsSettings = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * posCommunicationsSettings);
			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		if (kWh_System >= prev_kWh_System) {
			kWh_delta_System = kWh_System - prev_kWh_System;
		} else {
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kWh_System < 100) && ((MAX_INT - prev_kWh_System) < 100)) {
				// log(LOG_ERROR,name+" kWh_System is overflow");
				kWh_delta_System = MAX_INT - prev_kWh_System + kWh_System;
			} else
			{
				kWh_delta_System = 0;
			}
		}
		prev_kWh_System = kWh_System;

		if (kVARh_System >= prev_kVARh_System) {
			kVARh_delta_System = kVARh_System - prev_kVARh_System;
		} else {
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kVARh_System < 100) && ((MAX_INT - prev_kVARh_System) < 100)) {
				kVARh_delta_System = MAX_INT - prev_kVARh_System + kVARh_System;
			} else
			{
				kVARh_delta_System = 0;
			}
		}

		prev_kVARh_System = kVARh_System;

		if (kVAh_System >= prev_kVAh_System) {
			kVAh_delta_System = kVAh_System - prev_kVAh_System;
		} else {
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kVAh_System < 100) && ((MAX_INT - prev_kVAh_System) < 100)) {
				kVAh_delta_System = MAX_INT - prev_kVAh_System + kVAh_System;
			} else
			{
				kVAh_delta_System = 0;
			}
		}
		prev_kVAh_System = kVAh_System;

		if (kWh_L1 >= prev_kWh_L1) {
			kWh_delta_L1 = kWh_L1 - prev_kWh_L1;
		} else {
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kWh_L1 < 100) && ((MAX_INT - prev_kWh_L1) < 100)) {
				kWh_delta_L1 = MAX_INT - prev_kWh_L1 + kWh_L1;
			} else
			{
				kWh_delta_L1 = 0;
			}
		}
		;
		prev_kWh_L1 = kWh_L1;

		if (kVARh_L1 >= prev_kVARh_L1) {
			kVARh_delta_L1 = kVARh_L1 - prev_kVARh_L1;
		} else {
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kVARh_L1 < 100) && ((MAX_INT - prev_kVARh_L1) < 100)) {
				kVARh_delta_L1 = MAX_INT - prev_kVARh_L1 + kVARh_L1;
			} else
			{
				kVARh_delta_L1 = 0;
			}
		}
		prev_kVARh_L1 = kVARh_L1;

		if (kVAh_L1 >= prev_kVAh_L1) {
			kVAh_delta_L1 = kVAh_L1 - prev_kVAh_L1;
		} else {
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kVAh_L1 < 100) && ((MAX_INT - prev_kVAh_L1) < 100)) {
				kVAh_delta_L1 = MAX_INT - prev_kVAh_L1 + kVAh_L1;
			} else
			{
				kVAh_delta_L1 = 0;
			}
		}
		prev_kVAh_L1 = kVAh_L1;

		if (kWh_L2 >= prev_kWh_L2) {
			kWh_delta_L2 = kWh_L2 - prev_kWh_L2;
		} else {
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kWh_L2 < 100) && ((MAX_INT - prev_kWh_L2) < 100)) {
				kWh_delta_L2 = MAX_INT - prev_kWh_L2 + kWh_L2;
			} else
			{
				kWh_delta_L2 = 0;
			}
		}
		prev_kWh_L2 = kWh_L2;

		if (kVARh_L2 >= prev_kVARh_L2) {
			kVARh_delta_L2 = kVARh_L2 - prev_kVARh_L2;
		} else {
			if ((kVARh_L2 < 100) && ((MAX_INT - prev_kVARh_L2) < 100)) {
				kVARh_delta_L2 = MAX_INT - prev_kVARh_L2 + kVARh_L2;
			} else
			{
				kVARh_delta_L2 = 0;
			}
		}
		prev_kVARh_L2 = kVARh_L2;

		if (kVAh_L2 >= prev_kVAh_L2) {
			kVAh_delta_L2 = kVAh_L2 - prev_kVAh_L2;
		} else {
			if ((kVAh_L2 < 100) && ((MAX_INT - prev_kVAh_L2) < 100)) {
				kVAh_delta_L2 = MAX_INT - prev_kVAh_L2 + kVAh_L2;
			} else
			{
				kVAh_delta_L2 = 0;
			}
		}
		prev_kVAh_L2 = kVAh_L2;

		if (kWh_L3 >= prev_kWh_L3) {
			kWh_delta_L3 = kWh_L3 - prev_kWh_L3;
		} else {
			if ((kWh_L3 < 100) && ((MAX_INT - prev_kWh_L3) < 100)) {
				kWh_delta_L3 = MAX_INT - prev_kWh_L3 + kWh_L3;
			} else
			{
				kWh_delta_L3 = 0;
			}
		}
		prev_kWh_L3 = kWh_L3;

		if (kVARh_L3 >= prev_kVARh_L3) {
			kVARh_delta_L3 = kVARh_L3 - prev_kVARh_L3;
		} else {
			if ((kVARh_L3 < 100) && ((MAX_INT - prev_kVARh_L3) < 100)) {
				kVARh_delta_L3 = MAX_INT - prev_kVARh_L3 + kVARh_L3;
			} else
			{
				kVARh_delta_L3 = 0;
			}
		}
		prev_kVARh_L3 = kVARh_L3;

		if (kVAh_L3 >= prev_kVAh_L3) {
			kVAh_delta_L3 = kVAh_L3 - prev_kVAh_L3;
		} else {
			if ((kVAh_L3 < 100) && ((MAX_INT - prev_kVAh_L3) < 100)) {
				kVAh_delta_L3 = MAX_INT - prev_kVAh_L3 + kVAh_L3;
			} else
			{
				kVAh_delta_L3 = 0;
			}
		}
		prev_kVAh_L3 = kVAh_L3;

		if ((prev_kWh_Negative == 0) || (prev_kWh_Negative > kWh_Negative)) {
			kWh_delta_Negative = 0;
		} else {
			kWh_delta_Negative = kWh_Negative - prev_kWh_Negative;
		}
		prev_kWh_Negative = kWh_Negative;

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
		data.append(";Active Energy Reading=" + formatValue(kWh_System * Scalar_kW_kWh_Demand) + ",kWh");
		data.append(";Active Energy=" + formatValue(kWh_delta_System * Scalar_kW_kWh_Demand * 1000.0) + ",Wh");
		data.append(";Active Power=" + formatValue(kW_System * Scalar_kW_kWh_Demand) + ",kW");
		data.append(";Reactive Energy Reading=" + formatValue(kVARh_System * Scalar_kVAR_kVARh) + ",kVARh");
		data.append(";Reactive Energy=" + formatValue(kVARh_delta_System * Scalar_kVAR_kVARh) + ",kVARh");
		data.append(";Reactive Power=" + formatValue(kVAR_System * Scalar_kVAR_kVARh) + ",kVAR");
		data.append(";Apparent Energy Reading=" + formatValue(kVAh_System * Scalar_kVA_kVAh) + ",kVAh");
		data.append(";Apparent Energy=" + formatValue(kVAh_delta_System * Scalar_kVA_kVAh) + ",kVAh");
		data.append(";Apparent Power=" + formatValue(kVA_System * Scalar_kVA_kVAh) + ",kVA");
		data.append(";Power Factor=" + formatValue(Apparent_PF_System * Scalar_Power_Factor) + ",None");
		data.append(";Current=" + formatValue((Amps_L1 + Amps_L2 + Amps_L3) * Scalar_Amps) + ",A");
		data.append(";Voltage=" + formatValue(Volts_Line_to_Neutral_Avg * Scalar_Volts) + ",V");
		data.append(";Voltage L1-L2=" + formatValue(Volts_L1_to_L2 * Scalar_Volts) + ",V");
		data.append(";Voltage L2-L3=" + formatValue(Volts_L2_to_L3 * Scalar_Volts) + ",V");
		data.append(";Voltage L1-L3=" + formatValue(Volts_L1_to_L3 * Scalar_Volts) + ",V");
		data.append(";Frequency=" + formatValue(Line_Frequency * Scalar_Freq) + ",Hz");
		data.append(";Peak Demand=" + formatValue(kW_Demand_System_Max * Scalar_kW_kWh_Demand) + ",kW");
		data.append(";Regenerated Energy Reading=" + formatValue(kWh_Negative * Scalar_kW_kWh_Demand) + ",kWh");
		data.append(";Regenerated Energy=" + formatValue(kWh_delta_Negative * Scalar_kW_kWh_Demand * 1000.0) + ",Wh");
		data.append(";Regenerated Power=" + formatValue(kW_Negative * Scalar_kW_kWh_Demand) + ",kW");
		// Phase L1
		data.append("|DEVICEID=" + getId() + "-0-1");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + formatValue(kWh_L1 * Scalar_kW_kWh_Demand) + ",kWh");
		data.append(";Active Energy=" + formatValue(kWh_delta_L1 * Scalar_kW_kWh_Demand * 1000.0) + ",Wh");
		data.append(";Active Power=" + formatValue(kW_L1 * Scalar_kW_kWh_Demand) + ",kW");
		data.append(";Reactive Energy Reading=" + formatValue(kVARh_L1 * Scalar_kVAR_kVARh) + ",kVARh");
		data.append(";Reactive Energy=" + formatValue(kVARh_delta_L1 * Scalar_kVAR_kVARh) + ",kVARh");
		data.append(";Reactive Power=" + formatValue(kVAR_L1 * Scalar_kVAR_kVARh) + ",kVAR");
		data.append(";Apparent Energy Reading=" + formatValue(kVAh_L1 * Scalar_kVA_kVAh) + ",kVAh");
		data.append(";Apparent Energy=" + formatValue(kVAh_delta_L1 * Scalar_kVA_kVAh) + ",kVAh");
		data.append(";Apparent Power=" + formatValue(kVA_L1 * Scalar_kVA_kVAh) + ",kVA");
		data.append(";Power Factor=" + formatValue(Apparent_PF_L1 * Scalar_Power_Factor) + ",None");
		data.append(";Current=" + formatValue(Amps_L1 * Scalar_Amps) + ",A");
		data.append(";Voltage=" + formatValue(Volts_L1_to_Neutral * Scalar_Volts) + ",V");
		// Phase L2
		data.append("|DEVICEID=" + getId() + "-0-2");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + formatValue(kWh_L2 * Scalar_kW_kWh_Demand) + ",kWh");
		data.append(";Active Energy=" + formatValue(kWh_delta_L2 * Scalar_kW_kWh_Demand * 1000.0) + ",Wh");
		data.append(";Active Power=" + formatValue(kW_L2 * Scalar_kW_kWh_Demand) + ",kW");
		data.append(";Reactive Energy Reading=" + formatValue(kVARh_L2 * Scalar_kVAR_kVARh) + ",kVARh");
		data.append(";Reactive Energy=" + formatValue(kVARh_delta_L2 * Scalar_kVAR_kVARh) + ",kVARh");
		data.append(";Reactive Power=" + formatValue(kVAR_L2 * Scalar_kVAR_kVARh) + ",kVAR");
		data.append(";Apparent Energy Reading=" + formatValue(kVAh_L2 * Scalar_kVA_kVAh) + ",kVAh");
		data.append(";Apparent Energy=" + formatValue(kVAh_delta_L2 * Scalar_kVA_kVAh) + ",kVAh");
		data.append(";Apparent Power=" + formatValue(kVA_L2 * Scalar_kVA_kVAh) + ",kVA");
		data.append(";Power Factor=" + formatValue(Apparent_PF_L2 * Scalar_Power_Factor) + ",None");
		data.append(";Current=" + formatValue(Amps_L2 * Scalar_Amps) + ",A");
		data.append(";Voltage=" + formatValue(Volts_L2_to_Neutral * Scalar_Volts) + ",V");
		// Phase L3
		data.append("|DEVICEID=" + getId() + "-0-3");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + formatValue(kWh_L3 * Scalar_kW_kWh_Demand) + ",kWh");
		data.append(";Active Energy=" + formatValue(kWh_delta_L3 * Scalar_kW_kWh_Demand * 1000.0) + ",Wh");
		data.append(";Active Power=" + formatValue(kW_L3 * Scalar_kW_kWh_Demand) + ",kW");
		data.append(";Reactive Energy Reading=" + formatValue(kVARh_L3 * Scalar_kVAR_kVARh) + ",kVARh");
		data.append(";Reactive Energy=" + formatValue(kVARh_delta_L3 * Scalar_kVAR_kVARh) + ",kVARh");
		data.append(";Reactive Power=" + formatValue(kVAR_L3 * Scalar_kVAR_kVARh) + ",kVAR");
		data.append(";Apparent Energy Reading=" + formatValue(kVAh_L3 * Scalar_kVA_kVAh) + ",kVAh");
		data.append(";Apparent Energy=" + formatValue(kVAh_delta_L3 * Scalar_kVA_kVAh) + ",kVAh");
		data.append(";Apparent Power=" + formatValue(kVA_L3 * Scalar_kVA_kVAh) + ",kVA");
		data.append(";Power Factor=" + formatValue(Apparent_PF_L3 * Scalar_Power_Factor) + ",None");
		data.append(";Current=" + formatValue(Amps_L3 * Scalar_Amps) + ",A");
		data.append(";Voltage=" + formatValue(Volts_L3_to_Neutral * Scalar_Volts) + ",V");

		return data.toString();
	}

	private String formatValue(double value) {
		return vformat.format(Math.rint(value * 1000.0) / 1000.0);
	}

	private void setDeviceDataScalar() {
		switch (dataScalar) {
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
