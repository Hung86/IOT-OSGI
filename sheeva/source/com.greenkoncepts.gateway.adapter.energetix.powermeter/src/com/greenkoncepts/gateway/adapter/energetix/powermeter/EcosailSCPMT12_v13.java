package com.greenkoncepts.gateway.adapter.energetix.powermeter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class EcosailSCPMT12_v13 extends EcosailDevice {
	public final static boolean isAbs = true;
	public final static int MAX_ENERGY_DIFF = 10; // kWh

	final static int CHANNEL_NUM = 4;

	// ----- Device Control
	public static final int MBREG_FACTORY_RESET = 40099;
	public static final int MBREG_FACTORY_RESET_CH1 = 40110;
	public static final int MBREG_FACTORY_RESET_CH2 = 40210;
	public static final int MBREG_FACTORY_RESET_CH3 = 40310;
	public static final int MBREG_FACTORY_RESET_CH4 = 40410;
	public static final int MBREG_SAVE_REBOOT = 40100;
	// ----- Device Configuration
	public static final int MBREG_CONFIG_START = 40003;
	public static final int MBREG_CONFIG_NUM = 2;
	public static final int posAddress = 0;
	public static final int posBaudrate = 1;
	private int[] cfData = new int[MBREG_CONFIG_NUM];
	// ----- CH Configuration
	public static final int MBREG_CONFIG_CH_START = 40101;
	public static final int MBREG_CONFIG_CH_NUM = 6;
	public static final int posCTType = 0;
	public static final int posCTRating = 1;
	public static final int posDataScalar = 2;
	public static final int posDemandWindow = 3;
	public static final int posStatus = 4;
	public static final int posSignedMode = 5;
	private int[][] cfChannelData = new int[CHANNEL_NUM][MBREG_CONFIG_CH_NUM];

	// ----- Device Metering Data for SCPM-T12
	public static final int MBREG_DATA_START = 40111;
	public static final int MBREG_DATA_NUM = 71;
	public static final int OFFSET_kWh_System_MSW = 0;
	public static final int OFFSET_kWh_System_LSW = 1;
	public static final int OFFSET_kWh_System_P_MSW = 2;
	public static final int OFFSET_kWh_System_P_LSW = 3;
	public static final int OFFSET_kWh_System_N_MSW = 4;
	public static final int OFFSET_kWh_System_N_LSW = 5;
	public static final int OFFSET_kWh_L1_P_MSW = 6;
	public static final int OFFSET_kWh_L1_P_LSW = 7;
	public static final int OFFSET_kWh_L1_N_MSW = 8;
	public static final int OFFSET_kWh_L1_N_LSW = 9;
	public static final int OFFSET_kWh_L2_P_MSW = 10;
	public static final int OFFSET_kWh_L2_P_LSW = 11;
	public static final int OFFSET_kWh_L2_N_MSW = 12;
	public static final int OFFSET_kWh_L2_N_LSW = 13;
	public static final int OFFSET_kWh_L3_P_MSW = 14;
	public static final int OFFSET_kWh_L3_P_LSW = 15;
	public static final int OFFSET_kWh_L3_N_MSW = 16;
	public static final int OFFSET_kWh_L3_N_LSW = 17;
	public static final int OFFSET_kVARh_System_MSW = 18;
	public static final int OFFSET_kVARh_System_LSW = 19;
	public static final int OFFSET_kVARh_System_P_MSW = 20;
	public static final int OFFSET_kVARh_System_P_LSW = 21;
	public static final int OFFSET_kVARh_System_N_MSW = 22;
	public static final int OFFSET_kVARh_System_N_LSW = 23;
	public static final int OFFSET_kVARh_L1_P_MSW = 24;
	public static final int OFFSET_kVARh_L1_P_LSW = 25;
	public static final int OFFSET_kVARh_L1_N_MSW = 26;
	public static final int OFFSET_kVARh_L1_N_LSW = 27;
	public static final int OFFSET_kVARh_L2_P_MSW = 28;
	public static final int OFFSET_kVARh_L2_P_LSW = 29;
	public static final int OFFSET_kVARh_L2_N_MSW = 30;
	public static final int OFFSET_kVARh_L2_N_LSW = 31;
	public static final int OFFSET_kVARh_L3_P_MSW = 32;
	public static final int OFFSET_kVARh_L3_P_LSW = 33;
	public static final int OFFSET_kVARh_L3_N_MSW = 34;
	public static final int OFFSET_kVARh_L3_N_LSW = 35;
	public static final int OFFSET_kVAh_System_MSW = 36;
	public static final int OFFSET_kVAh_System_LSW = 37;
	public static final int OFFSET_kVAh_L1_MSW = 38;
	public static final int OFFSET_kVAh_L1_LSW = 39;
	public static final int OFFSET_kVAh_L2_MSW = 40;
	public static final int OFFSET_kVAh_L2_LSW = 41;
	public static final int OFFSET_kVAh_L3_MSW = 42;
	public static final int OFFSET_kVAh_L3_LSW = 43;
	public static final int OFFSET_kW_L1 = 44;
	public static final int OFFSET_kW_L2 = 45;
	public static final int OFFSET_kW_L3 = 46;
	public static final int OFFSET_kVAR_L1 = 47;
	public static final int OFFSET_kVAR_L2 = 48;
	public static final int OFFSET_kVAR_L3 = 49;
	public static final int OFFSET_kVA_L1 = 50;
	public static final int OFFSET_kVA_L2 = 51;
	public static final int OFFSET_kVA_L3 = 52;
	public static final int OFFSET_PF_L1 = 53;
	public static final int OFFSET_PF_L2 = 54;
	public static final int OFFSET_PF_L3 = 55;
	public static final int OFFSET_V_L1 = 56;
	public static final int OFFSET_V_L2 = 57;
	public static final int OFFSET_V_L3 = 58;
	public static final int OFFSET_A_L1 = 59;
	public static final int OFFSET_A_L2 = 60;
	public static final int OFFSET_A_L3 = 61;
	public static final int OFFSET_A_Neutral = 62;
	public static final int OFFSET_Hz_System = 63;
	public static final int OFFSET_kW_Demand_L1 = 64;
	public static final int OFFSET_kW_Demand_L2 = 65;
	public static final int OFFSET_kW_Demand_L3 = 66;
	public static final int OFFSET_V_L1_L2 = 67;
	public static final int OFFSET_V_L2_L3 = 68;
	public static final int OFFSET_V_L1_L3 = 69;

	private String[] channelType = {"power", "power", "power", "power"};
	private boolean[] hadGettingConfig = { false, false, false, false };

	private double[] Scalar_Energy = { 0.001, 0.001, 0.001, 0.001 };
	private double[] Scalar_Power = { 0.01, 0.01, 0.01, 0.01 };
	private double[] Scalar_A = { 0.01, 0.01, 0.01, 0.01 };
	private double[] Scalar_V = { 0.1, 0.1, 0.1, 0.1 };
	private double[] Scalar_PF = { 0.01, 0.01, 0.01, 0.01 };
	private double[] Scalar_Hz = { 0.01, 0.01, 0.01, 0.01 };

	private long[] kWh_System_prev = { 0, 0, 0, 0 };
	private long[] kVARh_System_prev = { 0, 0, 0, 0 };
	private long[] kVAh_System_prev = { 0, 0, 0, 0 };
	private long[] kVAh_L2_prev = { 0, 0, 0, 0 };
	private long[] kWh_L1_P_prev = { 0, 0, 0, 0 };
	private long[] kWh_L1_N_prev = { 0, 0, 0, 0 };
	private long[] kVARh_L1_P_prev = { 0, 0, 0, 0 };
	private long[] kVARh_L1_N_prev = { 0, 0, 0, 0 };
	private long[] kVAh_L1_prev = { 0, 0, 0, 0 };
	private long[] kWh_L2_P_prev = { 0, 0, 0, 0 };
	private long[] kWh_L2_N_prev = { 0, 0, 0, 0 };
	private long[] kVARh_L2_P_prev = { 0, 0, 0, 0 };
	private long[] kVARh_L2_N_prev = { 0, 0, 0, 0 };
	private long[] kWh_L3_P_prev = { 0, 0, 0, 0 };
	private long[] kWh_L3_N_prev = { 0, 0, 0, 0 };
	private long[] kVARh_L3_P_prev = { 0, 0, 0, 0 };
	private long[] kVARh_L3_N_prev = { 0, 0, 0, 0 };
	private long[] kVAh_L3_prev = { 0, 0, 0, 0 };

	private long[] kWh_System = { 0, 0, 0, 0 };
	private long[] kWh_System_P = { 0, 0, 0, 0 };
	private long[] kWh_System_N = { 0, 0, 0, 0 };
	private long[] kWh_L1_P = { 0, 0, 0, 0 };
	private long[] kWh_L1_N = { 0, 0, 0, 0 };
	private long[] kWh_L2_P = { 0, 0, 0, 0 };
	private long[] kWh_L2_N = { 0, 0, 0, 0 };
	private long[] kWh_L3_P = { 0, 0, 0, 0 };
	private long[] kWh_L3_N = { 0, 0, 0, 0 };
	private long[] kVARh_System = { 0, 0, 0, 0 };
	private long[] kVARh_System_P = { 0, 0, 0, 0 };
	private long[] kVARh_System_N = { 0, 0, 0, 0 };
	private long[] kVARh_L1_P = { 0, 0, 0, 0 };
	private long[] kVARh_L1_N = { 0, 0, 0, 0 };
	private long[] kVARh_L2_P = { 0, 0, 0, 0 };
	private long[] kVARh_L2_N = { 0, 0, 0, 0 };
	private long[] kVARh_L3_P = { 0, 0, 0, 0 };
	private long[] kVARh_L3_N = { 0, 0, 0, 0 };
	private long[] kVAh_System = { 0, 0, 0, 0 };
	private long[] kVAh_L1 = { 0, 0, 0, 0 };
	private long[] kVAh_L2 = { 0, 0, 0, 0 };
	private long[] kVAh_L3 = { 0, 0, 0, 0 };
	private long[] kWh_System_diff = { 0, 0, 0, 0 };
	private long[] kWh_L1_P_diff = { 0, 0, 0, 0 };
	private long[] kWh_L1_N_diff = { 0, 0, 0, 0 };
	private long[] kWh_L2_P_diff = { 0, 0, 0, 0 };
	private long[] kWh_L2_N_diff = { 0, 0, 0, 0 };
	private long[] kWh_L3_P_diff = { 0, 0, 0, 0 };
	private long[] kWh_L3_N_diff = { 0, 0, 0, 0 };
	private long[] kVARh_System_diff = { 0, 0, 0, 0 };
	private long[] kVARh_L1_P_diff = { 0, 0, 0, 0 };
	private long[] kVARh_L1_N_diff = { 0, 0, 0, 0 };
	private long[] kVARh_L2_P_diff = { 0, 0, 0, 0 };
	private long[] kVARh_L2_N_diff = { 0, 0, 0, 0 };
	private long[] kVARh_L3_P_diff = { 0, 0, 0, 0 };
	private long[] kVARh_L3_N_diff = { 0, 0, 0, 0 };
	private long[] kVAh_System_diff = { 0, 0, 0, 0 };
	private long[] kVAh_L1_diff = { 0, 0, 0, 0 };
	private long[] kVAh_L2_diff = { 0, 0, 0, 0 };
	private long[] kVAh_L3_diff = { 0, 0, 0, 0 };
	private int[] kW_L1 = { 0, 0, 0, 0 };
	private int[] kW_L2 = { 0, 0, 0, 0 };
	private int[] kW_L3 = { 0, 0, 0, 0 };
	private int[] kVAR_L1 = { 0, 0, 0, 0 };
	private int[] kVAR_L2 = { 0, 0, 0, 0 };
	private int[] kVAR_L3 = { 0, 0, 0, 0 };
	private int[] kVA_L1 = { 0, 0, 0, 0 };
	private int[] kVA_L2 = { 0, 0, 0, 0 };
	private int[] kVA_L3 = { 0, 0, 0, 0 };
	private int[] PF_L1 = { 0, 0, 0, 0 };
	private int[] PF_L2 = { 0, 0, 0, 0 };
	private int[] PF_L3 = { 0, 0, 0, 0 };
	private int[] V_L1 = { 0, 0, 0, 0 };
	private int[] V_L2 = { 0, 0, 0, 0 };
	private int[] V_L3 = { 0, 0, 0, 0 };
	private int[] A_L1 = { 0, 0, 0, 0 };
	private int[] A_L2 = { 0, 0, 0, 0 };
	private int[] A_L3 = { 0, 0, 0, 0 };
	private int[] A_Neutral = { 0, 0, 0, 0 };
	private int[] Hz_System = { 0, 0, 0, 0 };
	private int[] kW_Demand_L1 = { 0, 0, 0, 0 };
	private int[] kW_Demand_L2 = { 0, 0, 0, 0 };
	private int[] kW_Demand_L3 = { 0, 0, 0, 0 };
	private int[] V_L1_L2 = { 0, 0, 0, 0 };
	private int[] V_L2_L3 = { 0, 0, 0, 0 };
	private int[] V_L1_L3 = { 0, 0, 0, 0 };

	private Logger mLogger = LoggerFactory.getLogger("EcosailSCPMT12");

	public EcosailSCPMT12_v13(int addr, String category) {
		super(category, addr);
	}

	@Override
	public String getDeviceData() {
		byte[] data = null;
		for (int ch = 0; ch < CHANNEL_NUM; ch++) {
			if (!hadGettingConfig[ch]) {
				data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_CH_START + ch * 100, MBREG_CONFIG_CH_NUM);
				if (decodingData(ch, data, CONFIG_MODE)) {
					setDataScalar(ch);
					hadGettingConfig[ch] = true;
				}
			}
			data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START + ch * 100, MBREG_DATA_NUM);
			if (decodingData(ch, data, DATA_MODE)) {
				calculateDecodedData(ch);
			} else {
				break;
			}
		}
		return createDataSendToServer();
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			if ((dataIdx >= 0) && (dataIdx < 4)) {
				byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START + dataIdx * 100, MBREG_DATA_NUM);
				decodingData(dataIdx, data, DATA_MODE);
			}
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			for (int j = 0; j < CHANNEL_NUM; j++) {
				Map<String, String> item = new HashMap<String, String>();
				String chan = "ch" + j + "_";
				item.put(chan + "kWh_System", vformat.format(kWh_System[j] * Scalar_Energy[j]));
				item.put(chan + "kVARh_System", vformat.format(kVARh_System[j] * Scalar_Energy[j]));
				item.put(chan + "kVAh_System", vformat.format(kVAh_System[j] * Scalar_Energy[j]));
				item.put(chan + "A_Neutral", vformat.format(A_Neutral[j] * Scalar_A[j]));
				item.put(chan + "Hz_System", vformat.format(Hz_System[j] * Scalar_Hz[j]));
				item.put(chan + "kW_System", vformat.format((Math.abs(kW_L1[j]) + Math.abs(kW_L2[j]) + Math.abs(kW_L3[j])) * Scalar_Power[j]));
				item.put(chan + "kVAR_System", vformat.format((Math.abs(kVAR_L1[j]) + Math.abs(kVAR_L2[j]) + Math.abs(kVAR_L3[j])) * Scalar_Power[j]));
				item.put(chan + "kVA_System", vformat.format((kVA_L1[j] + kVA_L2[j] + kVA_L3[j]) * Scalar_Power[j]));
				item.put(chan + "V_System", vformat.format(V_L1[j] * Scalar_V[j]));
				item.put(chan + "A_System", vformat.format((Math.abs(A_L1[j]) + Math.abs(A_L2[j]) + Math.abs(A_L3[j])) * Scalar_A[j]));
				item.put(chan + "kW_Demand_System",
						vformat.format((Math.abs(kW_Demand_L1[j]) + Math.abs(kW_Demand_L2[j]) + Math.abs(kW_Demand_L3[j])) * Scalar_Power[j]));
				item.put(chan + "V_L1_L2", vformat.format(Math.abs(V_L1_L2[j] * Scalar_V[j])));
				item.put(chan + "V_L2_L3", vformat.format(Math.abs(V_L2_L3[j] * Scalar_V[j])));
				item.put(chan + "V_L1_L3", vformat.format(Math.abs(V_L1_L3[j] * Scalar_V[j])));
				// Phase L1
				item.put(chan + "kWh_L1", vformat.format((kWh_L1_P[j] + kWh_L1_N[j]) * Scalar_Energy[j]));
				item.put(chan + "kVARh_L1", vformat.format((kVARh_L1_P[j] + kVARh_L1_N[j]) * Scalar_Energy[j]));
				item.put(chan + "kVAh_L1", vformat.format(kVAh_L1[j] * Scalar_Energy[j]));
				item.put(chan + "kW_L1", vformat.format(Math.abs(kW_L1[j]) * Scalar_Power[j]));
				item.put(chan + "kVAR_L1", vformat.format(Math.abs(kVAR_L1[j]) * Scalar_Power[j]));
				item.put(chan + "kVA_L1", vformat.format(kVA_L1[j] * Scalar_Power[j]));
				item.put(chan + "PF_L1", vformat.format(PF_L1[j] * Scalar_PF[j]));
				item.put(chan + "V_L1", vformat.format(V_L1[j] * Scalar_V[j]));
				item.put(chan + "A_L1", vformat.format(Math.abs(A_L1[j]) * Scalar_A[j]));
				item.put(chan + "kW_Demand_L1", vformat.format(Math.abs(kW_Demand_L1[j]) * Scalar_Power[j]));
				// Phase L2
				item.put(chan + "kWh_L2", vformat.format((kWh_L2_P[j] + kWh_L2_N[j]) * Scalar_Energy[j]));
				item.put(chan + "kVARh_L2", vformat.format((kVARh_L2_P[j] + kVARh_L2_N[j]) * Scalar_Energy[j]));
				item.put(chan + "kVAh_L2", vformat.format(kVAh_L2[j] * Scalar_Energy[j]));
				item.put(chan + "kW_L2", vformat.format(Math.abs(kW_L2[j]) * Scalar_Power[j]));
				item.put(chan + "kVAR_L2", vformat.format(Math.abs(kVAR_L2[j]) * Scalar_Power[j]));
				item.put(chan + "kVA_L2", vformat.format(kVA_L2[j] * Scalar_Power[j]));
				item.put(chan + "PF_L2", vformat.format(PF_L2[j] * Scalar_PF[j]));
				item.put(chan + "V_L2", vformat.format(V_L2[j] * Scalar_V[j]));
				item.put(chan + "A_L2", vformat.format(Math.abs(A_L2[j]) * Scalar_A[j]));
				item.put(chan + "kW_Demand_L2", vformat.format(Math.abs(kW_Demand_L2[j]) * Scalar_Power[j]));
				// Phase L3
				item.put(chan + "kWh_L3", "" + vformat.format((kWh_L3_P[j] + kWh_L3_N[j]) * Scalar_Energy[j]));
				item.put(chan + "kVARh_L3", "" + vformat.format((kVARh_L3_P[j] + kVARh_L3_N[j]) * Scalar_Energy[j]));
				item.put(chan + "kVAh_L3", "" + vformat.format(kVAh_L3[j] * Scalar_Energy[j]));
				item.put(chan + "kW_L3", "" + vformat.format(Math.abs(kW_L3[j]) * Scalar_Power[j]));
				item.put(chan + "kVAR_L3", "" + vformat.format(Math.abs(kVAR_L3[j]) * Scalar_Power[j]));
				item.put(chan + "kVA_L3", "" + vformat.format(kVA_L3[j] * Scalar_Power[j]));
				item.put(chan + "PF_L3", "" + vformat.format(PF_L3[j] * Scalar_PF[j]));
				item.put(chan + "V_L3", "" + vformat.format(V_L3[j] * Scalar_V[j]));
				item.put(chan + "A_L3", "" + vformat.format(Math.abs(A_L3[j]) * Scalar_A[j]));
				item.put(chan + "kW_Demand_L3", vformat.format(Math.abs(kW_Demand_L3[j]) * Scalar_Power[j]));

				real_time_data.add(item);
			}
		}
		return real_time_data;
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		byte[] data = null;
		device_config.clear();
		for (int ch = 0; ch < CHANNEL_NUM; ch++) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_CH_START + ch * 100, MBREG_CONFIG_CH_NUM);
			if (decodingData(ch, data, CONFIG_MODE)) {
				device_config.put(MBREG_CONFIG_CH_START + ch * 100 + posCTType, String.valueOf(cfChannelData[ch][posCTType]));
				device_config.put(MBREG_CONFIG_CH_START + ch * 100 + posCTRating, String.valueOf(cfChannelData[ch][posCTRating]));
				device_config.put(MBREG_CONFIG_CH_START + ch * 100 + posDataScalar, String.valueOf(cfChannelData[ch][posDataScalar]));
				device_config.put(MBREG_CONFIG_CH_START + ch * 100 + posDemandWindow, String.valueOf(cfChannelData[ch][posDemandWindow]));
				device_config.put(MBREG_CONFIG_CH_START + ch * 100 + posStatus, String.valueOf(cfChannelData[ch][posStatus]));
				device_config.put(MBREG_CONFIG_CH_START + ch * 100 + posSignedMode, String.valueOf(cfChannelData[ch][posSignedMode]));
			}

		}

		data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START, MBREG_CONFIG_NUM);
		if (decodingData(5, data, CONFIG_MODE)) {
			device_config.put(MBREG_CONFIG_START + posAddress, String.valueOf(cfData[posAddress]));
			device_config.put(MBREG_CONFIG_START + posBaudrate, String.valueOf(cfData[posBaudrate]));
		}
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		if ((config != null) && (!config.isEmpty())) {
			byte[] data;
			byte[] results = null;
			if (config.containsKey(MBREG_FACTORY_RESET)) {
				data = ModbusUtil.unsignedShortToRegister(Integer.parseInt(config.get(MBREG_FACTORY_RESET)));
				results = modbus.writeMultipleRegisters(modbusid, MBREG_FACTORY_RESET, data);
				if (results != null) {
					registers.addAll(config.keySet());
				}
			} else if (config.containsKey(MBREG_FACTORY_RESET_CH1)) {
				data = ModbusUtil.unsignedShortToRegister(Integer.parseInt(config.get(MBREG_FACTORY_RESET_CH1)));
				results = modbus.writeMultipleRegisters(modbusid, MBREG_FACTORY_RESET_CH1, data);
				if (results != null) {
					registers.addAll(config.keySet());
				}
			} else if (config.containsKey(MBREG_FACTORY_RESET_CH2)) {
				data = ModbusUtil.unsignedShortToRegister(Integer.parseInt(config.get(MBREG_FACTORY_RESET_CH2)));
				results = modbus.writeMultipleRegisters(modbusid, MBREG_FACTORY_RESET_CH2, data);
				if (results != null) {
					registers.addAll(config.keySet());
				}
			} else if (config.containsKey(MBREG_FACTORY_RESET_CH3)) {
				data = ModbusUtil.unsignedShortToRegister(Integer.parseInt(config.get(MBREG_FACTORY_RESET_CH3)));
				results = modbus.writeMultipleRegisters(modbusid, MBREG_FACTORY_RESET_CH3, data);
				if (results != null) {
					registers.addAll(config.keySet());
				}
			} else if (config.containsKey(MBREG_FACTORY_RESET_CH4)) {
				data = ModbusUtil.unsignedShortToRegister(Integer.parseInt(config.get(MBREG_FACTORY_RESET_CH4)));
				results = modbus.writeMultipleRegisters(modbusid, MBREG_FACTORY_RESET_CH4, data);
				if (results != null) {
					registers.addAll(config.keySet());
				}
			} else {
				List<Integer> cfDataRegs = new ArrayList<Integer>();
				List<Integer> cfChannel1DataRegs = new ArrayList<Integer>();
				List<Integer> cfChannel2DataRegs = new ArrayList<Integer>();
				List<Integer> cfChannel3DataRegs = new ArrayList<Integer>();
				List<Integer> cfChannel4DataRegs = new ArrayList<Integer>();
				for (Integer n : config.keySet()) {
					if (n < MBREG_CONFIG_CH_START) {
						cfData[n - MBREG_CONFIG_START] = Integer.parseInt(config.get(n));
						cfDataRegs.add(n);
					} else if (n < MBREG_CONFIG_CH_START + 100) {
						cfChannelData[0][n - MBREG_CONFIG_CH_START] = Integer.parseInt(config.get(n));
						cfChannel1DataRegs.add(n);
					} else if (n < MBREG_CONFIG_CH_START + 200) {
						cfChannelData[1][n - (MBREG_CONFIG_CH_START + 100)] = Integer.parseInt(config.get(n));
						cfChannel2DataRegs.add(n);
					} else if (n < MBREG_CONFIG_CH_START + 300) {
						cfChannelData[2][n - (MBREG_CONFIG_CH_START + 200)] = Integer.parseInt(config.get(n));
						cfChannel3DataRegs.add(n);
					} else {
						cfChannelData[3][n - (MBREG_CONFIG_CH_START + 300)] = Integer.parseInt(config.get(n));
						cfChannel4DataRegs.add(n);
					}

				}
				if (!cfDataRegs.isEmpty()) {
					data = new byte[cfData.length * 2];
					for (int i = 0; i < cfData.length; i++) {
						byte[] temp = ModbusUtil.unsignedShortToRegister(cfData[i]);
						data[2 * i] = temp[0];
						data[2 * i + 1] = temp[1];
					}
					results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG_START, data);
					if (results != null) {
						registers.addAll(cfDataRegs);
					}
				}

				if (!cfChannel1DataRegs.isEmpty()) {
					data = new byte[cfChannelData[0].length * 2];
					for (int i = 0; i < cfChannelData[0].length; i++) {
						byte[] temp = ModbusUtil.unsignedShortToRegister(cfChannelData[0][i]);
						data[2 * i] = temp[0];
						data[2 * i + 1] = temp[1];
					}
					results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG_CH_START, data);
					if (results != null) {
						registers.addAll(cfChannel1DataRegs);
					}
				}

				if (!cfChannel2DataRegs.isEmpty()) {
					data = new byte[cfChannelData[1].length * 2];
					for (int i = 0; i < cfChannelData[1].length; i++) {
						byte[] temp = ModbusUtil.unsignedShortToRegister(cfChannelData[1][i]);
						data[2 * i] = temp[0];
						data[2 * i + 1] = temp[1];
					}
					results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG_CH_START + 100, data);
					if (results != null) {
						registers.addAll(cfChannel2DataRegs);
					}
				}

				if (!cfChannel3DataRegs.isEmpty()) {
					data = new byte[cfChannelData[2].length * 2];
					for (int i = 0; i < cfChannelData[2].length; i++) {
						byte[] temp = ModbusUtil.unsignedShortToRegister(cfChannelData[2][i]);
						data[2 * i] = temp[0];
						data[2 * i + 1] = temp[1];
					}
					results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG_CH_START + 200, data);
					if (results != null) {
						registers.addAll(cfChannel3DataRegs);
					}
				}

				if (!cfChannel4DataRegs.isEmpty()) {
					data = new byte[cfChannelData[3].length * 2];
					for (int i = 0; i < cfChannelData[3].length; i++) {
						byte[] temp = ModbusUtil.unsignedShortToRegister(cfChannelData[3][i]);
						data[2 * i] = temp[0];
						data[2 * i + 1] = temp[1];
					}
					results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG_CH_START + 300, data);
					if (results != null) {
						registers.addAll(cfChannel4DataRegs);
					}
				}
			}
			if (!registers.isEmpty()) {
				byte[] save = new byte[2];
				save[0] = 0;
				save[1] = 1;
				modbus.writeMultipleRegisters(modbusid, MBREG_SAVE_REBOOT, save);
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
			kWh_System[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kWh_System_MSW);
			kWh_System_P[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kWh_System_P_MSW);
			kWh_System_N[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kWh_System_N_MSW);
			kWh_L1_P[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kWh_L1_P_MSW);
			kWh_L1_N[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kWh_L1_N_MSW);
			kWh_L2_P[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kWh_L2_P_MSW);
			kWh_L2_N[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kWh_L2_N_MSW);
			kWh_L3_P[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kWh_L3_P_MSW);
			kWh_L3_N[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kWh_L3_N_MSW);
			kVARh_System[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kVARh_System_MSW);
			kVARh_System_P[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kVARh_System_P_MSW);
			kVARh_System_N[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kVARh_System_N_MSW);
			kVARh_L1_P[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kVARh_L1_P_MSW);
			kVARh_L1_N[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kVARh_L1_N_MSW);
			kVARh_L2_P[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kVARh_L2_P_MSW);
			kVARh_L2_N[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kVARh_L2_N_MSW);
			kVARh_L3_P[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kVARh_L3_P_MSW);
			kVARh_L3_N[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kVARh_L3_N_MSW);
			kVAh_System[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kVAh_System_MSW);
			kVAh_L1[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kVAh_L1_MSW);
			kVAh_L2[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kVAh_L2_MSW);
			kVAh_L3[idx] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 2 * OFFSET_kVAh_L3_MSW);

			kW_L1[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_kW_L1);
			kW_L2[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_kW_L2);
			kW_L3[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_kW_L3);
			kVAR_L1[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_kVAR_L1);
			kVAR_L2[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_kVAR_L2);
			kVAR_L3[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_kVAR_L3);
			kVA_L1[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_kVA_L1);
			kVA_L2[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_kVA_L2);
			kVA_L3[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_kVA_L3);
			PF_L1[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_PF_L1);
			PF_L2[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_PF_L2);
			PF_L3[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_PF_L3);
			V_L1[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_V_L1);
			V_L2[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_V_L2);
			V_L3[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_V_L3);
			A_L1[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_A_L1);
			A_L2[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_A_L2);
			A_L3[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_A_L3);
			A_Neutral[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_A_Neutral);
			Hz_System[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_Hz_System);
			kW_Demand_L1[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_kW_Demand_L1);
			kW_Demand_L2[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_kW_Demand_L2);
			kW_Demand_L3[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_kW_Demand_L3);
			V_L1_L2[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_V_L1_L2);
			V_L2_L3[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_V_L2_L3);
			V_L1_L3[idx] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + 2 * OFFSET_V_L1_L3);
			return true;
		}
		if (mode == CONFIG_MODE) {
			if (idx == 5) {
				cfData[posAddress] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posAddress);
				cfData[posBaudrate] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posBaudrate);
			} else {
				for (int i = 0; i < MBREG_CONFIG_CH_NUM; i++) {
					cfChannelData[idx][posCTType] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posCTType);
					cfChannelData[idx][posCTRating] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posCTRating);
					cfChannelData[idx][posDataScalar] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posDataScalar);
					cfChannelData[idx][posDemandWindow] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posDemandWindow);
					cfChannelData[idx][posStatus] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posStatus);
					cfChannelData[idx][posSignedMode] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posSignedMode);
				}
			}
			return true;
		}

		return true;
	}

	private void setDataScalar(int idx) {
		int scalar = cfChannelData[idx][posDataScalar];
		switch (scalar) {
			case 0:
				Scalar_Energy[idx] = 0.0001;
				Scalar_Power[idx] = 0.001;
				Scalar_A[idx] = 0.001;
				break;
			case 1:
				Scalar_Energy[idx] = 0.001;
				Scalar_Power[idx] = 0.01;
				Scalar_A[idx] = 0.01;
				break;
			case 2:
				Scalar_Energy[idx] = 0.01;
				Scalar_Power[idx] = 0.1;
				Scalar_A[idx] = 0.1;
				break;
			case 3:
				Scalar_Energy[idx] = 0.1;
				Scalar_Power[idx] = 1.0;
				Scalar_A[idx] = 1.0;
				break;
			default:
		}
	}

	private void calculateDecodedData(int idx) {
		if ((kWh_System[idx] >= kWh_System_prev[idx]) && (kWh_System_prev[idx] != 0)) {
			kWh_System_diff[idx] = kWh_System[idx] - kWh_System_prev[idx];
		} else {
			// log(LOG_ERROR, name + " kWh_System less than previous");
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kWh_System[idx] < 100) && ((MAX_INT - kWh_System_prev[idx]) < 100)) {
				// log(LOG_ERROR, name + " kWh_System is overflow");
				kWh_System_diff[idx] = MAX_INT - kWh_System_prev[idx] + kWh_System[idx];
			} else {
				kWh_System_diff[idx] = 0;
			}
		}
		if (isDebug) {
			mLogger.debug("Device " + getId() + " > kWh_System prev:" + kWh_System_prev[idx] + ";kWh_System curr:" + kWh_System[idx]
					+ ";kWh_System_diff:" + kWh_System_diff[idx]);
		}
		kWh_System_prev[idx] = kWh_System[idx];

		if ((kVARh_System[idx] >= kVARh_System_prev[idx]) && (kVARh_System_prev[idx] != 0)) {
			kVARh_System_diff[idx] = kVARh_System[idx] - kVARh_System_prev[idx];
		} else {
			// log(LOG_ERROR, name + " kVARh_System less than previous");
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kVARh_System[idx] < 100) && ((MAX_INT - kVARh_System_prev[idx]) < 100)) {
				// log(LOG_ERROR, name + " kVARh_System is overflow");
				kVARh_System_diff[idx] = MAX_INT - kVARh_System_prev[idx] + kVARh_System[idx];
			} else {
				// reset to 0
				kVARh_System_diff[idx] = 0;
			}
		}
		if (isDebug) {
			mLogger.debug("Device " + getId() + " > kVARh_System prev:" + kVARh_System_prev[idx] + ";kVARh_System curr:" + kVARh_System[idx]
					+ ";kVARh_System_diff:" + kVARh_System_diff[idx]);
		}
		kVARh_System_prev[idx] = kVARh_System[idx];

		if (kVAh_System[idx] >= kVAh_System_prev[idx]) {
			kVAh_System_diff[idx] = kVAh_System[idx] - kVAh_System_prev[idx];
		} else {
			// log(LOG_ERROR, name + " kVAh_System less than previous");
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kVAh_System[idx] < 100) && ((MAX_INT - kVAh_System_prev[idx]) < 100)) {
				// log(LOG_ERROR, name + " kVAh_System is overflow");
				kVAh_System_diff[idx] = MAX_INT - kVAh_System_prev[idx] + kVAh_System[idx];
			} else {
				// reset to 0
				kVAh_System_diff[idx] = 0;
			}
		}
		if (isDebug) {
			mLogger.debug("Device " + getId() + " > kVAh_System prev:" + kVAh_System_prev[idx] + ";kVAh_System curr:" + kVAh_System[idx]
					+ ";kVAh_System_diff:" + kVAh_System_diff[idx]);
		}
		kVAh_System_prev[idx] = kVAh_System[idx];

		if (kWh_L1_P[idx] >= kWh_L1_P_prev[idx]) {
			kWh_L1_P_diff[idx] = kWh_L1_P[idx] - kWh_L1_P_prev[idx];
		} else {
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kWh_L1_P[idx] < 100) && ((MAX_INT - kWh_L1_P_prev[idx]) < 100)) {
				// log(LOG_ERROR, name + " kWh_L1_P is overflow");
				kWh_L1_P_diff[idx] = MAX_INT - kWh_L1_P_prev[idx] + kWh_L1_P[idx];
			} else {
				// reset to 0
				kWh_L1_P_diff[idx] = 0;
			}
		}
		if (isDebug) {
			mLogger.debug("Device " + getId() + " > kWh_L1_P prev:" + kWh_L1_P_prev[idx] + ";kWh_L1_P curr:" + kWh_L1_P[idx] + ";kWh_L1_P_diff:"
					+ kWh_L1_P_diff[idx]);
		}
		kWh_L1_P_prev[idx] = kWh_L1_P[idx];

		if (kWh_L1_N[idx] >= kWh_L1_N_prev[idx]) {
			kWh_L1_N_diff[idx] = kWh_L1_N[idx] - kWh_L1_N_prev[idx];
		} else {
			// log(LOG_ERROR, name + " kWh_L1_N less than previous");
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kWh_L1_N[idx] < 100) && ((MAX_INT - kWh_L1_N_prev[idx]) < 100)) {
				// log(LOG_ERROR, name + " kWh_L1_N is overflow");
				kWh_L1_N_diff[idx] = MAX_INT - kWh_L1_N_prev[idx] + kWh_L1_N[idx];
			} else {
				// reset to 0
				kWh_L1_N_diff[idx] = 0;
			}
		}
		if (isDebug) {
			mLogger.debug("Device " + getId() + " > kWh_L1_N prev:" + kWh_L1_N_prev[idx] + ";kWh_L1_N curr:" + kWh_L1_N[idx] + ";kWh_L1_N_diff:"
					+ kWh_L1_N_diff[idx]);
		}
		kWh_L1_N_prev[idx] = kWh_L1_N[idx];

		if (kVARh_L1_P[idx] >= kVARh_L1_P_prev[idx]) {
			kVARh_L1_P_diff[idx] = kVARh_L1_P[idx] - kVARh_L1_P_prev[idx];
		} else {
			// log(LOG_ERROR, name + " kVARh_L1_P less than previous");
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kVARh_L1_P[idx] < 100) && ((MAX_INT - kVARh_L1_P_prev[idx]) < 100)) {
				// log(LOG_ERROR, name + " kVARh_L1_P is overflow");
				kVARh_L1_P_diff[idx] = MAX_INT - kVARh_L1_P_prev[idx] + kVARh_L1_P[idx];
			} else {
				// reset to 0
				kVARh_L1_P_diff[idx] = 0;
			}
		}
		if (isDebug) {
			mLogger.debug("Device " + getId() + " > kVARh_L1_P prev:" + kVARh_L1_P_prev[idx] + ";kVARh_L1_P curr:" + kVARh_L1_P[idx]
					+ ";kVARh_L1_P_diff:" + kVARh_L1_P_diff[idx]);
		}
		kVARh_L1_P_prev[idx] = kVARh_L1_P[idx];

		if (kVARh_L1_N[idx] >= kVARh_L1_N_prev[idx]) {
			kVARh_L1_N_diff[idx] = kVARh_L1_N[idx] - kVARh_L1_N_prev[idx];
		} else {
			// log(LOG_ERROR, name + " kVARh_L1_N less than previous");
			// 4294967295
			// long max = (long) (Math.pow(2, 32)) ;
			if ((kVARh_L1_N[idx] < 100) && ((MAX_INT - kVARh_L1_N_prev[idx]) < 100)) {
				// log(LOG_ERROR, name + " kVARh_L1_N is overflow");
				kVARh_L1_N_diff[idx] = MAX_INT - kVARh_L1_N_prev[idx] + kVARh_L1_N[idx];
			} else {
				// reset to 0
				kVARh_L1_N_diff[idx] = 0;
			}
		}
		if (isDebug) {
			mLogger.debug("Device " + getId() + " > kVARh_L1_N prev:" + kVARh_L1_N_prev[idx] + ";kVARh_L1_N curr:" + kVARh_L1_N[idx]
					+ ";kVARh_L1_N_diff:" + kVARh_L1_N_diff[idx]);
		}
		kVARh_L1_N_prev[idx] = kVARh_L1_N[idx];

		if (kVAh_L1[idx] >= kVAh_L1_prev[idx]) {
			kVAh_L1_diff[idx] = kVAh_L1[idx] - kVAh_L1_prev[idx];
		} else {
			// log(LOG_ERROR, name + " kVAh_L1 less than previous");
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kVAh_L1[idx] < 100) && ((MAX_INT - kVAh_L1_prev[idx]) < 100)) {
				// log(LOG_ERROR, name + " kVAh_L1 is overflow");
				kVAh_L1_diff[idx] = MAX_INT - kVAh_L1_prev[idx] + kVAh_L1[idx];
			} else {
				// reset to 0
				kVAh_L1_diff[idx] = 0;
			}
		}
		if (isDebug) {
			mLogger.debug("Device " + getId() + " > kVAh_L1 prev:" + kVAh_L1_prev[idx] + ";kVAh_L1 curr:" + kVAh_L1[idx] + ";kVAh_L1_diff:"
					+ kVAh_L1_diff[idx]);
		}
		kVAh_L1_prev[idx] = kVAh_L1[idx];

		if (kWh_L2_P[idx] >= kWh_L2_P_prev[idx]) {
			kWh_L2_P_diff[idx] = kWh_L2_P[idx] - kWh_L2_P_prev[idx];
		} else {
			// log(LOG_ERROR, name + " kWh_L2_P less than previous");
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kWh_L2_P[idx] < 100) && ((MAX_INT - kWh_L2_P_prev[idx]) < 100)) {
				// log(LOG_ERROR, name + " kWh_L2_P is overflow");
				kWh_L2_P_diff[idx] = MAX_INT - kWh_L2_P_prev[idx] + kWh_L2_P[idx];
			} else {
				// reset to 0
				kWh_L2_P_diff[idx] = 0;
			}
		}
		if (isDebug) {
			mLogger.debug("Device " + getId() + " > kWh_L2_P prev:" + kWh_L2_P_prev[idx] + ";kWh_L2_P curr:" + kWh_L2_P[idx] + ";kWh_L2_P_diff:"
					+ kWh_L2_P_diff[idx]);
		}
		kWh_L2_P_prev[idx] = kWh_L2_P[idx];

		if (kWh_L2_N[idx] >= kWh_L2_N_prev[idx]) {
			kWh_L2_N_diff[idx] = kWh_L2_N[idx] - kWh_L2_N_prev[idx];
		} else {
			// log(LOG_ERROR, name + " kWh_L2_N less than previous");
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kWh_L2_N[idx] < 100) && ((MAX_INT - kWh_L2_N_prev[idx]) < 100)) {
				// log(LOG_ERROR, name + " kWh_L2_N is overflow");
				kWh_L2_N_diff[idx] = MAX_INT - kWh_L2_N_prev[idx] + kWh_L2_N[idx];
			} else {
				// reset to 0
				kWh_L2_N_diff[idx] = 0;
			}
		}
		if (isDebug) {
			mLogger.debug("Device " + getId() + " > kWh_L2_N prev:" + kWh_L2_N_prev[idx] + ";kWh_L2_N curr:" + kWh_L2_N[idx] + ";kWh_L2_N_diff:"
					+ kWh_L2_N_diff[idx]);
		}
		kWh_L2_N_prev[idx] = kWh_L2_N[idx];

		if (kVARh_L2_P[idx] >= kVARh_L2_P_prev[idx]) {
			kVARh_L2_P_diff[idx] = kVARh_L2_P[idx] - kVARh_L2_P_prev[idx];
		} else {
			// log(LOG_ERROR, name + " kVARh_L2_P less than previous");
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kVARh_L2_P[idx] < 100) && ((MAX_INT - kVARh_L2_P_prev[idx]) < 100)) {
				// log(LOG_ERROR, name + " kVARh_L2_P is overflow");
				kVARh_L2_P_diff[idx] = MAX_INT - kVARh_L2_P_prev[idx] + kVARh_L2_P[idx];
			} else {
				// reset to 0
				kVARh_L2_P_diff[idx] = 0;
			}
		}
		if (isDebug) {
			mLogger.debug("Device " + getId() + " > kVARh_L2_P prev:" + kVARh_L2_P_prev[idx] + ";kVARh_L2_P curr:" + kVARh_L2_P[idx]
					+ ";kVARh_L2_P_diff:" + kVARh_L2_P_diff[idx]);
		}
		kVARh_L2_P_prev[idx] = kVARh_L2_P[idx];

		if (kVARh_L2_N[idx] >= kVARh_L2_N_prev[idx]) {
			kVARh_L2_N_diff[idx] = kVARh_L2_N[idx] - kVARh_L2_N_prev[idx];
		} else {
			// log(LOG_ERROR, name + " kVARh_L2_N less than previous");
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kVARh_L2_N[idx] < 100) && ((MAX_INT - kVARh_L2_N_prev[idx]) < 100)) {
				// log(LOG_ERROR, name + " kVARh_L2_N is overflow");
				kVARh_L2_N_diff[idx] = MAX_INT - kVARh_L2_N_prev[idx] + kVARh_L2_N[idx];
			} else {
				// reset to 0
				kVARh_L2_N_diff[idx] = 0;
			}
		}
		if (isDebug) {
			mLogger.debug("Device " + getId() + " > kVARh_L2_N prev:" + kVARh_L2_N_prev[idx] + ";kVARh_L2_N curr:" + kVARh_L2_N[idx]
					+ ";kVARh_L2_N_diff:" + kVARh_L2_N_diff[idx]);
		}
		kVARh_L2_N_prev[idx] = kVARh_L2_N[idx];

		if (kVAh_L2[idx] >= kVAh_L2_prev[idx]) {
			kVAh_L2_diff[idx] = kVAh_L2[idx] - kVAh_L2_prev[idx];
		} else {
			// log(LOG_ERROR, name + " kVAh_L2 less than previous");
			// 4294967295
			// long max = (long) (Math.pow(2, 32)) ;
			if ((kVAh_L2[idx] < 100) && ((MAX_INT - kVAh_L2_prev[idx]) < 100)) {
				// log(LOG_ERROR, name + " kVAh_L2 is overflow");
				kVAh_L2_diff[idx] = MAX_INT - kVAh_L2_prev[idx] + kVAh_L2[idx];
			} else {
				// reset to 0
				kVAh_L2_diff[idx] = 0;
			}
		}
		if (isDebug) {
			mLogger.debug("Device " + getId() + " > kVAh_L2 prev:" + kVAh_L2_prev[idx] + ";kVAh_L2 curr:" + kVAh_L2[idx] + ";kVAh_L2_diff:"
					+ kVAh_L2_diff[idx]);
		}
		kVAh_L2_prev[idx] = kVAh_L2[idx];

		if (kWh_L3_P[idx] >= kWh_L3_P_prev[idx]) {
			kWh_L3_P_diff[idx] = kWh_L3_P[idx] - kWh_L3_P_prev[idx];
		} else {
			// log(LOG_ERROR, name + " kWh_L3_P less than previous");
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kWh_L3_P[idx] < 100) && ((MAX_INT - kWh_L3_P_prev[idx]) < 100)) {
				// log(LOG_ERROR, name + " kWh_L3_P is overflow");
				kWh_L3_P_diff[idx] = MAX_INT - kWh_L3_P_prev[idx] + kWh_L3_P[idx];
			} else {
				// reset to 0
				kWh_L3_P_diff[idx] = 0;
			}
		}
		if (isDebug) {
			mLogger.debug("Device " + getId() + " > kWh_L3_P prev:" + kWh_L3_P_prev[idx] + ";kWh_L3_P curr:" + kWh_L3_P[idx] + ";kWh_L3_P_diff:"
					+ kWh_L3_P_diff[idx]);
		}
		kWh_L3_P_prev[idx] = kWh_L3_P[idx];

		if (kWh_L3_N[idx] >= kWh_L3_N_prev[idx]) {
			kWh_L3_N_diff[idx] = kWh_L3_N[idx] - kWh_L3_N_prev[idx];
		} else {
			// log(LOG_ERROR, name + " kWh_L3_N less than previous");
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kWh_L3_N[idx] < 100) && ((MAX_INT - kWh_L3_N_prev[idx]) < 100)) {
				// log(LOG_ERROR, name + " kWh_L3_N is overflow");
				kWh_L3_N_diff[idx] = MAX_INT - kWh_L3_N_prev[idx] + kWh_L3_N[idx];
			} else {
				// reset to 0
				kWh_L3_N_diff[idx] = 0;
			}
		}
		if (isDebug) {
			mLogger.debug("Device " + getId() + " > kWh_L3_N prev:" + kWh_L3_N_prev[idx] + ";kWh_L3_N curr:" + kWh_L3_N[idx] + ";kWh_L3_N_diff:"
					+ kWh_L3_N_diff[idx]);
		}
		kWh_L3_N_prev[idx] = kWh_L3_N[idx];

		if (kVARh_L3_P[idx] >= kVARh_L3_P_prev[idx]) {
			kVARh_L3_P_diff[idx] = kVARh_L3_P[idx] - kVARh_L3_P_prev[idx];
		} else {
			// log(LOG_ERROR, name + " kVARh_L3_P less than previous");
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kVARh_L3_P[idx] < 100) && ((MAX_INT - kVARh_L3_P_prev[idx]) < 100)) {
				// log(LOG_ERROR, name + " kVARh_L3_P is overflow");
				kVARh_L3_P_diff[idx] = MAX_INT - kVARh_L3_P_prev[idx] + kVARh_L3_P[idx];
			} else {
				// reset to 0
				kVARh_L3_P_diff[idx] = 0;
			}
		}
		if (isDebug) {
			mLogger.debug("Device " + getId() + " > kVARh_L3_P prev:" + kVARh_L3_P_prev[idx] + ";kVARh_L3_P curr:" + kVARh_L3_P[idx]
					+ ";kVARh_L3_P_diff:" + kVARh_L3_P_diff[idx]);
		}
		kVARh_L3_P_prev[idx] = kVARh_L3_P[idx];

		if (kVARh_L3_N[idx] >= kVARh_L3_N_prev[idx]) {
			kVARh_L3_N_diff[idx] = kVARh_L3_N[idx] - kVARh_L3_N_prev[idx];
		} else {
			// log(LOG_ERROR, name + " kVARh_L3_N less than previous");
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kVARh_L3_N[idx] < 100) && ((MAX_INT - kVARh_L3_N_prev[idx]) < 100)) {
				// log(LOG_ERROR, name + " kVARh_L3_N is overflow");
				kVARh_L3_N_diff[idx] = MAX_INT - kVARh_L3_N_prev[idx] + kVARh_L3_N[idx];
			} else {
				// reset to 0
				kVARh_L3_N_diff[idx] = 0;
			}
		}
		if (isDebug) {
			mLogger.debug("Device " + getId() + " > kVARh_L3_N prev:" + kVARh_L3_N_prev[idx] + ";kVARh_L3_N curr:" + kVARh_L3_N[idx]
					+ ";kVARh_L3_N_diff:" + kVARh_L3_N_diff[idx]);
		}
		kVARh_L3_N_prev[idx] = kVARh_L3_N[idx];

		if (kVAh_L3[idx] >= kVAh_L3_prev[idx]) {
			kVAh_L3_diff[idx] = kVAh_L3[idx] - kVAh_L3_prev[idx];
		} else {
			// log(LOG_ERROR, name + " kVAh_L3 less than previous");
			// 4294967295 long max = (long) (Math.pow(2, 32)) ;
			if ((kVAh_L3[idx] < 100) && ((MAX_INT - kVAh_L3_prev[idx]) < 100)) {
				// log(LOG_ERROR, name + " kVAh_L3 is overflow");
				kVAh_L3_diff[idx] = MAX_INT - kVAh_L3_prev[idx] + kVAh_L3[idx];
			} else {
				// reset to 0
				kVAh_L3_diff[idx] = 0;
			}
		}
		if (isDebug) {
			mLogger.debug("Device " + getId() + " > kVAh_L3 prev:" + kVAh_L3_prev[idx] + ";kVAh_L3 curr:" + kVAh_L3[idx] + ";kVAh_L3_diff:"
					+ kVAh_L3_diff[idx]);
		}
		kVAh_L3_prev[idx] = kVAh_L3[idx];
	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		for (int ch = 0; ch < CHANNEL_NUM; ch++) {
			if ("solar".equalsIgnoreCase(channelType[ch])) {
				data.append("|DEVICEID="+getId()+"-"+ch+"-0");
				data.append(";TIMESTAMP="+timestamp);
				data.append(";Generated Energy Reading=" + vformat.format(kWh_System[ch]*Scalar_Energy[ch]) + ",kWh");
				data.append(";Generated Energy=" + vformat.format(kWh_System_diff[ch]*Scalar_Energy[ch]*1000) + ",Wh");
				data.append(";Active Energy=" + vformat.format(kWh_System_diff[ch]*Scalar_Energy[ch]*(-1000)) + ",Wh");

				data.append("|DEVICEID="+getId()+"-"+ch+"-1");
				data.append(";TIMESTAMP="+timestamp);
				data.append(";Generated Energy Reading=" + vformat.format((kWh_L1_P[ch]+kWh_L1_N[ch])*Scalar_Energy[ch]) + ",kWh");
				data.append(";Generated Energy=" + vformat.format((kWh_L1_P_diff[ch]+kWh_L1_N_diff[ch])*Scalar_Energy[ch]*1000) + ",Wh");
				data.append(";Active Energy=" + vformat.format((kWh_L1_P_diff[ch]+kWh_L1_N_diff[ch])*Scalar_Energy[ch]*(-1000)) + ",Wh");

				data.append("|DEVICEID="+getId()+"-"+ch+"-2");
				data.append(";TIMESTAMP="+timestamp);
				data.append(";Generated Energy Reading=" + vformat.format((kWh_L2_P[ch]+kWh_L2_N[ch])*Scalar_Energy[ch]) + ",kWh");
				data.append(";Generated Energy=" + vformat.format((kWh_L2_P_diff[ch]+kWh_L2_N_diff[ch])*Scalar_Energy[ch]*1000) + ",Wh");
				data.append(";Active Energy=" + vformat.format((kWh_L2_P_diff[ch]+kWh_L2_N_diff[ch])*Scalar_Energy[ch]*(-1000)) + ",Wh");

				data.append("|DEVICEID="+getId()+"-"+ch+"-3");
				data.append(";TIMESTAMP="+timestamp);
				data.append(";Generated Energy Reading=" + vformat.format((kWh_L3_P[ch]+kWh_L3_N[ch])*Scalar_Energy[ch]) + ",kWh");
				data.append(";Generated Energy=" + vformat.format((kWh_L3_P_diff[ch]+kWh_L3_N_diff[ch])*Scalar_Energy[ch]*1000) + ",Wh");
				data.append(";Active Energy=" + vformat.format((kWh_L3_P_diff[ch]+kWh_L3_N_diff[ch])*Scalar_Energy[ch]*(-1000)) + ",Wh");
				continue;
			}
			// System
			data.append("|DEVICEID=" + getId() + "-" + ch + "-0");
			data.append(";TIMESTAMP=" + timestamp);
			data.append(";Active Energy Reading=" + vformat.format(kWh_System[ch] * Scalar_Energy[ch]) + ",kWh");
			data.append(";Reactive Energy Reading=" + vformat.format(kVARh_System[ch] * Scalar_Energy[ch]) + ",kVARh");
			data.append(";Apparent Energy Reading=" + vformat.format(kVAh_System[ch] * Scalar_Energy[ch]) + ",kVAh");
			data.append(";Active Energy=" + vformat.format(kWh_System_diff[ch] * Scalar_Energy[ch] * 1000.0) + ",Wh");
			data.append(";Reactive Energy=" + vformat.format(kVARh_System_diff[ch] * Scalar_Energy[ch] * 1000.0) + ",VARh");
			data.append(";Apparent Energy=" + vformat.format(kVAh_System_diff[ch] * Scalar_Energy[ch] * 1000.0) + ",VAh");
			data.append(";Current Neutral=" + vformat.format(A_Neutral[ch] * Scalar_A[ch]) + ",A");
			data.append(";Frequency=" + vformat.format(Hz_System[ch] * Scalar_Hz[ch]) + ",Hz");
			data.append(";Active Power=" + vformat.format((Math.abs(kW_L1[ch]) + Math.abs(kW_L2[ch]) + Math.abs(kW_L3[ch])) * Scalar_Power[ch])
					+ ",kW");
			data.append(";Reactive Power="
					+ vformat.format((Math.abs(kVAR_L1[ch]) + Math.abs(kVAR_L2[ch]) + Math.abs(kVAR_L3[ch])) * Scalar_Power[ch]) + ",kVAR");
			data.append(";Apparent Power=" + vformat.format((kVA_L1[ch] + kVA_L2[ch] + kVA_L3[ch]) * Scalar_Power[ch]) + ",kVA");
			data.append(";Voltage=" + vformat.format(V_L1[ch] * Scalar_V[ch]) + ",V");
			data.append(";Current=" + vformat.format((Math.abs(A_L1[ch]) + Math.abs(A_L2[ch]) + Math.abs(A_L3[ch])) * Scalar_A[ch]) + ",A");
			data.append(";Peak Demand="
					+ vformat.format((Math.abs(kW_Demand_L1[ch]) + Math.abs(kW_Demand_L2[ch]) + Math.abs(kW_Demand_L3[ch])) * Scalar_Power[ch])
					+ ",kW");
			data.append(";Voltage L1-L2=" + vformat.format(Math.abs(V_L1_L2[ch] * Scalar_V[ch])) + ",V");
			data.append(";Voltage L2-L3=" + vformat.format(Math.abs(V_L2_L3[ch] * Scalar_V[ch])) + ",V");
			data.append(";Voltage L1-L3=" + vformat.format(Math.abs(V_L1_L3[ch] * Scalar_V[ch])) + ",V");

			// Phase L1
			data.append("|DEVICEID=" + getId() + "-" + ch + "-1");
			data.append(";TIMESTAMP=" + timestamp);
			data.append(";Active Energy Reading=" + vformat.format((kWh_L1_P[ch] + kWh_L1_N[ch]) * Scalar_Energy[ch]) + ",kWh");
			data.append(";Reactive Energy Reading=" + vformat.format((kVARh_L1_P[ch] + kVARh_L1_N[ch]) * Scalar_Energy[ch]) + ",kVARh");
			data.append(";Apparent Energy Reading=" + vformat.format(kVAh_L1[ch] * Scalar_Energy[ch]) + ",kVAh");
			data.append(";Active Energy=" + vformat.format((kWh_L1_P_diff[ch] + kWh_L1_N_diff[ch]) * Scalar_Energy[ch] * 1000.0) + ",Wh");
			data.append(";Reactive Energy=" + vformat.format((kVARh_L1_P_diff[ch] + kVARh_L1_N_diff[ch]) * Scalar_Energy[ch] * 1000.0) + ",VARh");
			data.append(";Apparent Energy=" + vformat.format(kVAh_L1_diff[ch] * Scalar_Energy[ch] * 1000.0) + ",VAh");
			data.append(";Active Power=" + vformat.format(Math.abs(kW_L1[ch]) * Scalar_Power[ch]) + ",kW");
			data.append(";Reactive Power=" + vformat.format(Math.abs(kVAR_L1[ch]) * Scalar_Power[ch]) + ",kVAR");
			data.append(";Apparent Power=" + vformat.format(kVA_L1[ch] * Scalar_Power[ch]) + ",kVA");
			data.append(";Power Factor=" + vformat.format(PF_L1[ch] * Scalar_PF[ch]) + ",None");
			data.append(";Voltage=" + vformat.format(V_L1[ch] * Scalar_V[ch]) + ",V");
			data.append(";Current=" + vformat.format(Math.abs(A_L1[ch]) * Scalar_A[ch]) + ",A");
			data.append(";Frequency=" + vformat.format(Hz_System[ch] * Scalar_Hz[ch]) + ",Hz");
			data.append(";Peak Demand=" + vformat.format(Math.abs(kW_Demand_L1[ch]) * Scalar_Power[ch]) + ",kW");

			// Phase L2
			data.append("|DEVICEID=" + getId() + "-" + ch + "-2");
			data.append(";TIMESTAMP=" + timestamp);
			data.append(";Active Energy Reading=" + vformat.format((kWh_L2_P[ch] + kWh_L2_N[ch]) * Scalar_Energy[ch]) + ",kWh");
			data.append(";Reactive Energy Reading=" + vformat.format((kVARh_L2_P[ch] + kVARh_L2_N[ch]) * Scalar_Energy[ch]) + ",kVARh");
			data.append(";Apparent Energy Reading=" + vformat.format(kVAh_L2[ch] * Scalar_Energy[ch]) + ",kVAh");
			data.append(";Active Energy=" + vformat.format((kWh_L2_P_diff[ch] + kWh_L2_N_diff[ch]) * Scalar_Energy[ch] * 1000.0) + ",Wh");
			data.append(";Reactive Energy=" + vformat.format((kVARh_L2_P_diff[ch] + kVARh_L2_N_diff[ch]) * Scalar_Energy[ch] * 1000.0) + ",VARh");
			data.append(";Apparent Energy=" + vformat.format((kVAh_L2_diff[ch]) * Scalar_Energy[ch] * 1000.0) + ",VAh");
			data.append(";Active Power=" + vformat.format(Math.abs(kW_L2[ch]) * Scalar_Power[ch]) + ",kW");
			data.append(";Reactive Power=" + vformat.format(Math.abs(kVAR_L2[ch]) * Scalar_Power[ch]) + ",kVAR");
			data.append(";Apparent Power=" + vformat.format(kVA_L2[ch] * Scalar_Power[ch]) + ",kVA");
			data.append(";Power Factor=" + vformat.format(PF_L2[ch] * Scalar_PF[ch]) + ",None");
			data.append(";Voltage=" + vformat.format(V_L2[ch] * Scalar_V[ch]) + ",V");
			data.append(";Current=" + vformat.format(Math.abs(A_L2[ch]) * Scalar_A[ch]) + ",A");
			data.append(";Frequency=" + vformat.format(Hz_System[ch] * Scalar_Hz[ch]) + ",Hz");
			data.append(";Peak Demand=" + vformat.format(Math.abs(kW_Demand_L2[ch]) * Scalar_Power[ch]) + ",kW");

			// Phase L3
			data.append("|DEVICEID=" + getId() + "-" + ch + "-3");
			data.append(";TIMESTAMP=" + timestamp);
			data.append(";Active Energy Reading=" + vformat.format((kWh_L3_P[ch] + kWh_L3_N[ch]) * Scalar_Energy[ch]) + ",kWh");
			data.append(";Reactive Energy Reading=" + vformat.format((kVARh_L3_P[ch] + kVARh_L3_N[ch]) * Scalar_Energy[ch]) + ",kVARh");
			data.append(";Apparent Energy Reading=" + vformat.format(kVAh_L3[ch] * Scalar_Energy[ch]) + ",kVAh");
			data.append(";Active Energy=" + (kWh_L3_P_diff[ch] + kWh_L3_N_diff[ch]) * Scalar_Energy[ch] * 1000.0 + ",Wh");
			data.append(";Reactive Energy=" + vformat.format((kVARh_L3_P_diff[ch] + kVARh_L3_N_diff[ch]) * Scalar_Energy[ch] * 1000.0) + ",VARh");
			data.append(";Apparent Energy=" + vformat.format(kVAh_L3_diff[ch] * Scalar_Energy[ch] * 1000.0) + ",VAh");
			data.append(";Active Power=" + vformat.format(Math.abs(kW_L3[ch]) * Scalar_Power[ch]) + ",kW");
			data.append(";Reactive Power=" + vformat.format(Math.abs(kVAR_L3[ch]) * Scalar_Power[ch]) + ",kVAR");
			data.append(";Apparent Power=" + vformat.format(kVA_L3[ch] * Scalar_Power[ch]) + ",kVA");
			data.append(";Power Factor=" + vformat.format(PF_L3[ch] * Scalar_PF[ch]) + ",None");
			data.append(";Voltage=" + vformat.format(V_L3[ch] * Scalar_V[ch]) + ",V");
			data.append(";Current=" + vformat.format(Math.abs(A_L3[ch]) * Scalar_A[ch]) + ",A");
			data.append(";Frequency=" + vformat.format(Hz_System[ch] * Scalar_Hz[ch]) + ",Hz");
			data.append(";Peak Demand=" + vformat.format(Math.abs(kW_Demand_L3[ch]) * Scalar_Power[ch]) + ",kW");
		}
		return data.toString();
	}

	public void setAttributes(List<Map<String, String>> attributes) {
		if ((attributes != null) && (attributes.size() == 4)) {
			for (Map<String, String> item : attributes) {
				int chan = Integer.parseInt(item.get("channel"));
				channelType[chan] = item.get("type");
			}
		}
	}
}
