package com.greenkoncepts.gateway.adapter.epower;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.FuncUtil;
import com.greenkoncepts.gateway.util.FuncUtil.RegisterMixEndian;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class ECM770 extends EpowerDevice {
	final static int CHANNEL_NUM = 6;
	private DecimalFormat vformat = new DecimalFormat("#########0.0000");

	// ----- Metering Data
	public static final int MBREG_DATA_START1 = 0;
	public static final int MBREG_DATA_LENGTH1 = 45;

	public static final int MBREG_DATA_START2 = 100;
	public static final int MBREG_DATA_LENGTH2 = 76;

	public static final int MBREG_CONFIG_START = 5000;// 45001-45011
	public static final int MBREG_CONFIG_LENGTH = 12; // Register table is wrong idx at 45004, have to be increased by 1 for 45004
	private int cfData[] = new int[MBREG_CONFIG_LENGTH];

	private int iVoltageA = 0;
	private int iVoltageB = 0;
	private int iVoltageC = 0;
	private int iAveragePhaseVoltage = 0;
	private int iVoltageAB = 0;
	private int iVoltageBC = 0;
	private int iVoltageCA = 0;
	private int iAverageLineVoltage = 0;
	private int iPositiveSequenceVoltage = 0;
	private int iNegativeSequenceVoltage = 0;
	private int iNeutralvoltage = 0;
	private int iCurrentA = 0;
	private int iCurrentB = 0;
	private int iCurrentC = 0;
	private int iAverageCurrent = 0;
	private int iPositiveSequenceCurrent = 0;
	private int iNegativeSequenceCurrent = 0;
	private int iNeutralCurrent = 0;
	private int iActivePowerA = 0;
	private int iActivePowerB = 0;
	private int iActivePowerC = 0;
	private int iActivePowerTotal = 0;
	private int iReactivePowerA = 0;
	private int iReactivePowerB = 0;
	private int iReactivePowerC = 0;
	private int iReactivePowerTotal = 0;
	private int iApparentPowerA = 0;
	private int iApparentPowerB = 0;
	private int iApparentPowerC = 0;
	private int iApparentPowerTotal = 0;
	private int iPowerFactorA = 0;
	private int iPowerFactorB = 0;
	private int iPowerFactorC = 0;
	private int iPowerFactorTotal = 0;
	private int iFrequency = 0;

	private int iActiveEnergyReadingQ1A = 0;
	private int iActiveEnergyReadingQ1B = 0;
	private int iActiveEnergyReadingQ1C = 0;

	private int iActiveEnergyReadingQ2A = 0;
	private int iActiveEnergyReadingQ2B = 0;
	private int iActiveEnergyReadingQ2C = 0;

	private int iActiveEnergyReadingQ3A = 0;
	private int iActiveEnergyReadingQ3B = 0;
	private int iActiveEnergyReadingQ3C = 0;

	private int iActiveEnergyReadingQ4A = 0;
	private int iActiveEnergyReadingQ4B = 0;
	private int iActiveEnergyReadingQ4C = 0;

	private int iReactiveEnergyReadingQ1A = 0;
	private int iReactiveEnergyReadingQ1B = 0;
	private int iReactiveEnergyReadingQ1C = 0;

	private int iReactiveEnergyReadingQ2A = 0;
	private int iReactiveEnergyReadingQ2B = 0;
	private int iReactiveEnergyReadingQ2C = 0;

	private int iReactiveEnergyReadingQ3A = 0;
	private int iReactiveEnergyReadingQ3B = 0;
	private int iReactiveEnergyReadingQ3C = 0;

	private int iReactiveEnergyReadingQ4A = 0;
	private int iReactiveEnergyReadingQ4B = 0;
	private int iReactiveEnergyReadingQ4C = 0;

	private int iApparentEnergyReadingA = 0;
	private int iApparentEnergyReadingB = 0;
	private int iApparentEnergyReadingC = 0;

	private int iActiveEnergyReadingTotal = 0;
	private int iReactiveEnergyReadingTotal = 0;
	private int iApparentEnergyReadingTotal = 0;

	private double dVoltageA = 0;
	private double dVoltageB = 0;
	private double dVoltageC = 0;
	private double dAveragePhaseVoltage = 0;
	private double dVoltageAB = 0;
	private double dVoltageBC = 0;
	private double dVoltageCA = 0;
	private double dAverageLineVoltage = 0;
	private double dPositiveSequenceVoltage = 0;
	private double dNegativeSequenceVoltage = 0;
	private double dNeutralvoltage = 0;
	private double dCurrentA = 0;
	private double dCurrentB = 0;
	private double dCurrentC = 0;
	private double dAverageCurrent = 0;
	private double dPositiveSequenceCurrent = 0;
	private double dNegativeSequenceCurrent = 0;
	private double dNeutralCurrent = 0;
	private double dActivePowerA = 0;
	private double dActivePowerB = 0;
	private double dActivePowerC = 0;
	private double dActivePowerTotal = 0;
	private double dReactivePowerA = 0;
	private double dReactivePowerB = 0;
	private double dReactivePowerC = 0;
	private double dReactivePowerTotal = 0;
	private double dApparentPowerA = 0;
	private double dApparentPowerB = 0;
	private double dApparentPowerC = 0;
	private double dApparentPowerTotal = 0;
	private double dPowerFactorA = 0;
	private double dPowerFactorB = 0;
	private double dPowerFactorC = 0;
	private double dPowerFactorTotal = 0;
	private double dFrequency = 0;

	private double dActiveEnergyReadingA = 0;
	private double dActiveEnergyReadingB = 0;
	private double dActiveEnergyReadingC = 0;

	private double dReactiveEnergyReadingA = 0;
	private double dReactiveEnergyReadingB = 0;
	private double dReactiveEnergyReadingC = 0;

	private double dApparentEnergyReadingA = 0;
	private double dApparentEnergyReadingB = 0;
	private double dApparentEnergyReadingC = 0;

	private double dActiveEnergyReadingTotal = 0;
	private double dReactiveEnergyReadingTotal = 0;
	private double dApparentEnergyReadingTotal = 0;

	private double dPrevActiveEnergyReadingA = 0;
	private double dPrevActiveEnergyReadingB = 0;
	private double dPrevActiveEnergyReadingC = 0;

	private double dPrevReactiveEnergyReadingA = 0;
	private double dPrevReactiveEnergyReadingB = 0;
	private double dPrevReactiveEnergyReadingC = 0;

	private double dPrevApparentEnergyReadingA = 0;
	private double dPrevApparentEnergyReadingB = 0;
	private double dPrevApparentEnergyReadingC = 0;

	private double dPrevActiveEnergyReadingTotal = 0;
	private double dPrevReactiveEnergyReadingTotal = 0;
	private double dPrevApparentEnergyReadingTotal = 0;

	private double dActiveEnergyA = 0;
	private double dActiveEnergyB = 0;
	private double dActiveEnergyC = 0;

	private double dReactiveEnergyA = 0;
	private double dReactiveEnergyB = 0;
	private double dReactiveEnergyC = 0;

	private double dApparentEnergyA = 0;
	private double dApparentEnergyB = 0;
	private double dApparentEnergyC = 0;

	private double dActiveEnergyTotal = 0;
	private double dReactiveEnergyTotal = 0;
	private double dApparentEnergyTotal = 0;

	static private int posVoltageA = OFFSET_DATA + 2 * 0;
	static private int posVoltageB = OFFSET_DATA + 2 * 1;
	static private int posVoltageC = OFFSET_DATA + 2 * 2;
	static private int posAveragePhaseVoltage = OFFSET_DATA + 2 * 4;
	static private int posVoltageAB = OFFSET_DATA + 2 * 5;
	static private int posVoltageBC = OFFSET_DATA + 2 * 6;
	static private int posVoltageCA = OFFSET_DATA + 2 * 7;
	static private int posAverageLineVoltage = OFFSET_DATA + 2 * 9;
	static private int posPositiveSequenceVoltage = OFFSET_DATA + 2 * 10;
	static private int posNegativeSequenceVOltage = OFFSET_DATA + 2 * 11;
	static private int posNeutralvoltage = OFFSET_DATA + 2 * 12;
	static private int posCurrentA = OFFSET_DATA + 2 * 13;
	static private int posCurrentB = OFFSET_DATA + 2 * 14;
	static private int posCurrentC = OFFSET_DATA + 2 * 15;
	static private int posAverageCurrent = OFFSET_DATA + 2 * 17;
	static private int posPositiveSequenceCurrent = OFFSET_DATA + 2 * 23;
	static private int posNegativeSequenceCurrent = OFFSET_DATA + 2 * 24;
	static private int posNeutralCurrent = OFFSET_DATA + 2 * 25;
	static private int posActivePowerA = OFFSET_DATA + 2 * 26;
	static private int posActivePowerB = OFFSET_DATA + 2 * 27;
	static private int posActivePowerC = OFFSET_DATA + 2 * 28;
	static private int posActivePowerTotal = OFFSET_DATA + 2 * 29;
	static private int posReactivePowerA = OFFSET_DATA + 2 * 31;
	static private int posReactivePowerB = OFFSET_DATA + 2 * 32;
	static private int posReactivePowerC = OFFSET_DATA + 2 * 33;
	static private int posReactivePowerTotal = OFFSET_DATA + 2 * 34;
	static private int posApparentPowerA = OFFSET_DATA + 2 * 36;
	static private int posApparentPowerB = OFFSET_DATA + 2 * 37;
	static private int posApparentPowerC = OFFSET_DATA + 2 * 38;
	static private int posApparentPowerTotal = OFFSET_DATA + 2 * 39;
	static private int posPowerFactorA = OFFSET_DATA + 2 * 40;
	static private int posPowerFactorB = OFFSET_DATA + 2 * 41;
	static private int posPowerFactorC = OFFSET_DATA + 2 * 42;
	static private int posPowerFactorTotal = OFFSET_DATA + 2 * 43;
	static private int posFrequency = OFFSET_DATA + 2 * 44;

	static private int posActiveEnergyReadingQ1A = OFFSET_DATA + 2 * 0; // 40101
	static private int posActiveEnergyReadingQ1B = OFFSET_DATA + 2 * 2; // 40103
	static private int posActiveEnergyReadingQ1C = OFFSET_DATA + 2 * 4; // 40105

	static private int posActiveEnergyReadingQ2A = OFFSET_DATA + 2 * 16;
	static private int posActiveEnergyReadingQ2B = OFFSET_DATA + 2 * 18;
	static private int posActiveEnergyReadingQ2C = OFFSET_DATA + 2 * 20;

	static private int posActiveEnergyReadingQ3A = OFFSET_DATA + 2 * 32;
	static private int posActiveEnergyReadingQ3B = OFFSET_DATA + 2 * 34;
	static private int posActiveEnergyReadingQ3C = OFFSET_DATA + 2 * 36;

	static private int posActiveEnergyReadingQ4A = OFFSET_DATA + 2 * 48;
	static private int posActiveEnergyReadingQ4B = OFFSET_DATA + 2 * 50;
	static private int posActiveEnergyReadingQ4C = OFFSET_DATA + 2 * 52;

	static private int posReactiveEnergyReadingQ1A = OFFSET_DATA + 2 * 8;
	static private int posReactiveEnergyReadingQ1B = OFFSET_DATA + 2 * 10;
	static private int posReactiveEnergyReadingQ1C = OFFSET_DATA + 2 * 12;

	static private int posReactiveEnergyReadingQ2A = OFFSET_DATA + 2 * 24;
	static private int posReactiveEnergyReadingQ2B = OFFSET_DATA + 2 * 26;
	static private int posReactiveEnergyReadingQ2C = OFFSET_DATA + 2 * 28;

	static private int posReactiveEnergyReadingQ3A = OFFSET_DATA + 2 * 40;
	static private int posReactiveEnergyReadingQ3B = OFFSET_DATA + 2 * 42;
	static private int posReactiveEnergyReadingQ3C = OFFSET_DATA + 2 * 44;

	static private int posReactiveEnergyReadingQ4A = OFFSET_DATA + 2 * 56;
	static private int posReactiveEnergyReadingQ4B = OFFSET_DATA + 2 * 58;
	static private int posReactiveEnergyReadingQ4C = OFFSET_DATA + 2 * 60;

	static private int posApparentEnergyReadingA = OFFSET_DATA + 2 * 64;
	static private int posApparentEnergyReadingB = OFFSET_DATA + 2 * 66;
	static private int posApparentEnergyReadingC = OFFSET_DATA + 2 * 68;

	static private int posActiveEnergyReadingTotal = OFFSET_DATA + 2 * 72;
	static private int posReactiveEnergyReadingTotal = OFFSET_DATA + 2 * 74;
	static private int posApparentEnergyReadingTotal = OFFSET_DATA + 2 * 70;

	static private double calFactor_1 = 0.1;
	static private double calFactor_01 = 0.01;
	static private double calFactor_001 = 0.001;
	static private double calFactor_0001 = 0.0001;
	
	//calculate CT Ratio, default PT Ratio = 1
	private boolean hasCTRatio = false;
	private double dCTRatio = 1;
	private int iCTRatio = 1;

	public ECM770(String category, int addr) {
		super(category, addr);
	}

	@Override
	public String getDeviceData() {
		if (!hasCTRatio) {
			byte[] ratioByte = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START, 1);
			if (decodingData(2, ratioByte, DATA_MODE)) {
				hasCTRatio = true;
			}
		}
		
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START1, MBREG_DATA_LENGTH1);

		if (decodingData(0, data, DATA_MODE)) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START2, MBREG_DATA_LENGTH2);
			if (decodingData(1, data, DATA_MODE)) {
				calculateDecodedData();
			}
		}

		return createDataSendToServer();
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START1, MBREG_DATA_LENGTH1);
			if (decodingData(0, data, DATA_MODE)) {
				data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START2, MBREG_DATA_LENGTH2);
				if (decodingData(1, data, DATA_MODE)) {
					calculateDecodedData();
				}
			}
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			item.put("Active_Energy_Reading_System", vformat.format(dActiveEnergyReadingTotal));
			item.put("Apparent_Energy_Reading_System", vformat.format(dApparentEnergyReadingTotal));
			item.put("Reactive_Energy_Reading_System", vformat.format(dReactiveEnergyReadingTotal));
			item.put("Active_Power_System", vformat.format(dActivePowerTotal));
			item.put("Reactive_Power_System", vformat.format(dReactivePowerTotal));
			item.put("Apparent_Power_System", vformat.format(dApparentPowerTotal));
			item.put("Power_Factor_System", vformat.format(dPowerFactorTotal));
			item.put("Current_System", vformat.format(dAverageCurrent));
			item.put("Voltage_System", vformat.format(dAveragePhaseVoltage));
			item.put("Voltage_L1-L2_System", vformat.format(dVoltageAB));
			item.put("Voltage_L2-L3_System", vformat.format(dVoltageBC));
			item.put("Voltage_L3-L1_System", vformat.format(dVoltageCA));
			item.put("Frequency_System", vformat.format(dFrequency));
			item.put("Current_Neutral_System", vformat.format(dNeutralCurrent));

			item.put("Active_Energy_Reading_L1", vformat.format(dActiveEnergyReadingA));
			item.put("Apparent_Energy_Reading_L1", vformat.format(dApparentEnergyReadingA));
			item.put("Reactive_Energy_Reading_L1", vformat.format(dReactiveEnergyReadingA));
			item.put("Active_Power_L1", vformat.format(dActivePowerA));
			item.put("Reactive_Power_L1", vformat.format(dReactivePowerA));
			item.put("Apparent_Power_L1", vformat.format(dApparentPowerA));
			item.put("Current_L1", vformat.format(dCurrentA));
			item.put("Voltage_L1", vformat.format(dVoltageA));
			item.put("Power_Factor_L1", vformat.format(dPowerFactorA));

			item.put("Active_Energy_Reading_L2", vformat.format(dActiveEnergyReadingB));
			item.put("Apparent_Energy_Reading_L2", vformat.format(dApparentEnergyReadingB));
			item.put("Reactive_Energy_Reading_L2", vformat.format(dReactiveEnergyReadingB));
			item.put("Active_Power_L2", vformat.format(dActivePowerB));
			item.put("Reactive_Power_L2", vformat.format(dReactivePowerB));
			item.put("Apparent_Power_L2", vformat.format(dApparentPowerB));
			item.put("Current_L2", vformat.format(dCurrentB));
			item.put("Voltage_L2", vformat.format(dVoltageB));
			item.put("Power_Factor_L2", vformat.format(dPowerFactorB));

			item.put("Active_Energy_Reading_L3", vformat.format(dActiveEnergyReadingC));
			item.put("Apparent_Energy_Reading_L3", vformat.format(dApparentEnergyReadingC));
			item.put("Reactive_Energy_Reading_L3", vformat.format(dReactiveEnergyReadingC));
			item.put("Active_Power_L3", vformat.format(dActivePowerC));
			item.put("Reactive_Power_L3", vformat.format(dReactivePowerC));
			item.put("Apparent_Power_L3", vformat.format(dApparentPowerC));
			item.put("Current_L3", vformat.format(dCurrentC));
			item.put("Voltage_L3", vformat.format(dVoltageC));
			item.put("Power_Factor_L3", vformat.format(dPowerFactorC));
			real_time_data.add(item);
		}
		return real_time_data;
	}

	private boolean decodingData(int idx, byte[] data, int mode) {
		if (data == null) {
			errorCount++;
			return false;
		}

		errorCount = 0;
		if (mode == DATA_MODE) {
			if (idx == 0) {
				iVoltageA = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posVoltageA);
				iVoltageB = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posVoltageB);
				iVoltageC = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posVoltageC);
				iAveragePhaseVoltage = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posAveragePhaseVoltage);
				iVoltageAB = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posVoltageAB);
				iVoltageBC = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posVoltageBC);
				iVoltageCA = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posVoltageCA);
				iAverageLineVoltage = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posAverageLineVoltage);
				iPositiveSequenceVoltage = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posPositiveSequenceVoltage);
				iNegativeSequenceVoltage = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posNegativeSequenceVOltage);
				iNeutralvoltage = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posNeutralvoltage);
				iCurrentA = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posCurrentA);
				iCurrentB = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posCurrentB);
				iCurrentC = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posCurrentC);
				iAverageCurrent = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posAverageCurrent);
				iPositiveSequenceCurrent = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posPositiveSequenceCurrent);
				iNegativeSequenceCurrent = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posNegativeSequenceCurrent);
				iNeutralCurrent = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posNeutralCurrent);
				iActivePowerA = FuncUtil.RegisterBigEndian.registerToShort(data, posActivePowerA);
				iActivePowerB = FuncUtil.RegisterBigEndian.registerToShort(data, posActivePowerB);
				iActivePowerC = FuncUtil.RegisterBigEndian.registerToShort(data, posActivePowerC);
				iActivePowerTotal = RegisterMixEndian.registersToInt(data, posActivePowerTotal);
				iReactivePowerA = FuncUtil.RegisterBigEndian.registerToShort(data, posReactivePowerA);
				iReactivePowerB = FuncUtil.RegisterBigEndian.registerToShort(data, posReactivePowerB);
				iReactivePowerC = FuncUtil.RegisterBigEndian.registerToShort(data, posReactivePowerC);
				iReactivePowerTotal = RegisterMixEndian.registersToInt(data, posReactivePowerTotal);
				iApparentPowerA = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posApparentPowerA);
				iApparentPowerB = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posApparentPowerB);
				iApparentPowerC = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posApparentPowerC);
				iApparentPowerTotal = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posApparentPowerTotal);
				iPowerFactorA = FuncUtil.RegisterBigEndian.registerToShort(data, posPowerFactorA);
				iPowerFactorB = FuncUtil.RegisterBigEndian.registerToShort(data, posPowerFactorB);
				iPowerFactorC = FuncUtil.RegisterBigEndian.registerToShort(data, posPowerFactorC);
				iPowerFactorTotal = FuncUtil.RegisterBigEndian.registerToShort(data, posPowerFactorTotal);
				iFrequency = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posFrequency);
				return true;
			}

			if (idx == 1) {
				iActiveEnergyReadingQ1A = ModbusUtil.registersMEToInt(data, posActiveEnergyReadingQ1A);
				iActiveEnergyReadingQ1B = ModbusUtil.registersMEToInt(data, posActiveEnergyReadingQ1B);
				iActiveEnergyReadingQ1C = ModbusUtil.registersMEToInt(data, posActiveEnergyReadingQ1C);

				iActiveEnergyReadingQ2A = ModbusUtil.registersMEToInt(data, posActiveEnergyReadingQ2A);
				iActiveEnergyReadingQ2B = ModbusUtil.registersMEToInt(data, posActiveEnergyReadingQ2B);
				iActiveEnergyReadingQ2C = ModbusUtil.registersMEToInt(data, posActiveEnergyReadingQ2C);

				iActiveEnergyReadingQ3A = ModbusUtil.registersMEToInt(data, posActiveEnergyReadingQ3A);
				iActiveEnergyReadingQ3B = ModbusUtil.registersMEToInt(data, posActiveEnergyReadingQ3B);
				iActiveEnergyReadingQ3C = ModbusUtil.registersMEToInt(data, posActiveEnergyReadingQ3C);

				iActiveEnergyReadingQ4A = ModbusUtil.registersMEToInt(data, posActiveEnergyReadingQ4A);
				iActiveEnergyReadingQ4B = ModbusUtil.registersMEToInt(data, posActiveEnergyReadingQ4B);
				iActiveEnergyReadingQ4C = ModbusUtil.registersMEToInt(data, posActiveEnergyReadingQ4C);

				iReactiveEnergyReadingQ1A = ModbusUtil.registersMEToInt(data, posReactiveEnergyReadingQ1A);
				iReactiveEnergyReadingQ1B = ModbusUtil.registersMEToInt(data, posReactiveEnergyReadingQ1B);
				iReactiveEnergyReadingQ1C = ModbusUtil.registersMEToInt(data, posReactiveEnergyReadingQ1C);

				iReactiveEnergyReadingQ2A = ModbusUtil.registersMEToInt(data, posReactiveEnergyReadingQ2A);
				iReactiveEnergyReadingQ2B = ModbusUtil.registersMEToInt(data, posReactiveEnergyReadingQ2B);
				iReactiveEnergyReadingQ2C = ModbusUtil.registersMEToInt(data, posReactiveEnergyReadingQ2C);

				iReactiveEnergyReadingQ3A = ModbusUtil.registersMEToInt(data, posReactiveEnergyReadingQ3A);
				iReactiveEnergyReadingQ3B = ModbusUtil.registersMEToInt(data, posReactiveEnergyReadingQ3B);
				iReactiveEnergyReadingQ3C = ModbusUtil.registersMEToInt(data, posReactiveEnergyReadingQ3C);

				iReactiveEnergyReadingQ4A = ModbusUtil.registersMEToInt(data, posReactiveEnergyReadingQ4A);
				iReactiveEnergyReadingQ4B = ModbusUtil.registersMEToInt(data, posReactiveEnergyReadingQ4B);
				iReactiveEnergyReadingQ4C = ModbusUtil.registersMEToInt(data, posReactiveEnergyReadingQ4C);

				iApparentEnergyReadingA = ModbusUtil.registersMEToInt(data, posApparentEnergyReadingA);
				iApparentEnergyReadingB = ModbusUtil.registersMEToInt(data, posApparentEnergyReadingB);
				iApparentEnergyReadingC = ModbusUtil.registersMEToInt(data, posApparentEnergyReadingC);

				iActiveEnergyReadingTotal = ModbusUtil.registersMEToInt(data, posActiveEnergyReadingTotal);
				iReactiveEnergyReadingTotal = ModbusUtil.registersMEToInt(data, posReactiveEnergyReadingTotal);
				iApparentEnergyReadingTotal = ModbusUtil.registersMEToInt(data, posApparentEnergyReadingTotal);

				return true;
			}
			
			if (idx == 2) {
				iCTRatio = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA);
				dCTRatio = iCTRatio/5.0;
				return true;
			}
		}
		if (mode == CONFIG_MODE) {
			for (int i = 0; i < MBREG_CONFIG_LENGTH; i++) {
				cfData[i] = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2 * i);
			}
			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		infoDebug();
		dVoltageA = iVoltageA * calFactor_01;
		dVoltageB = iVoltageB * calFactor_01;
		dVoltageC = iVoltageC * calFactor_01;
		dAveragePhaseVoltage = iAveragePhaseVoltage * calFactor_01;
		dVoltageAB = iVoltageAB * calFactor_01;
		dVoltageBC = iVoltageBC * calFactor_01;
		dVoltageCA = iVoltageCA * calFactor_01;
		dAverageLineVoltage = iAverageLineVoltage * calFactor_01;
		dPositiveSequenceVoltage = iPositiveSequenceVoltage * calFactor_01;
		dNegativeSequenceVoltage = iNegativeSequenceVoltage * calFactor_01;
		dNeutralvoltage = iNeutralvoltage * calFactor_01;
		dCurrentA = iCurrentA * calFactor_0001 * dCTRatio;
		dCurrentB = iCurrentB * calFactor_0001 * dCTRatio;
		dCurrentC = iCurrentC * calFactor_0001 * dCTRatio;
		dAverageCurrent = iAverageCurrent * calFactor_0001 * dCTRatio;
		dPositiveSequenceCurrent = iPositiveSequenceCurrent * calFactor_0001 * dCTRatio;
		dNegativeSequenceCurrent = iNegativeSequenceCurrent * calFactor_0001 * dCTRatio;
		dNeutralCurrent = iNeutralCurrent * calFactor_0001 * dCTRatio;
		dActivePowerA = (iActivePowerA * calFactor_1 * dCTRatio) / 1000;
		dActivePowerB = (iActivePowerB * calFactor_1 * dCTRatio) / 1000;
		dActivePowerC = (iActivePowerC * calFactor_1 * dCTRatio) / 1000;
		dActivePowerTotal = (iActivePowerTotal * calFactor_1 * dCTRatio) / 1000;
		dReactivePowerA = (iReactivePowerA * calFactor_1 * dCTRatio) / 1000;
		dReactivePowerB = (iReactivePowerB * calFactor_1 * dCTRatio) / 1000;
		dReactivePowerC = (iReactivePowerC * calFactor_1 * dCTRatio) / 1000;
		dReactivePowerTotal = (iReactivePowerTotal * calFactor_1 * dCTRatio) / 1000;
		dApparentPowerA = (iApparentPowerA * calFactor_1 * dCTRatio) / 1000;
		dApparentPowerB = (iApparentPowerB * calFactor_1 * dCTRatio) / 1000;
		dApparentPowerC = (iApparentPowerC * calFactor_1 * dCTRatio) / 1000;
		dApparentPowerTotal = (iApparentPowerTotal * calFactor_1 * dCTRatio) / 1000;
		dPowerFactorA = iPowerFactorA * calFactor_001;
		dPowerFactorB = iPowerFactorB * calFactor_001;
		dPowerFactorC = iPowerFactorC * calFactor_001;
		dPowerFactorTotal = iPowerFactorTotal * calFactor_001;
		dFrequency = iFrequency * calFactor_01;

		dActiveEnergyReadingA = (iActiveEnergyReadingQ1A + iActiveEnergyReadingQ2A + iActiveEnergyReadingQ3A + iActiveEnergyReadingQ4A) * calFactor_1;
		dActiveEnergyReadingB = (iActiveEnergyReadingQ1B + iActiveEnergyReadingQ2B + iActiveEnergyReadingQ3B + iActiveEnergyReadingQ4B) * calFactor_1;
		dActiveEnergyReadingC = (iActiveEnergyReadingQ1C + iActiveEnergyReadingQ2C + iActiveEnergyReadingQ3C + iActiveEnergyReadingQ4C) * calFactor_1;

		dReactiveEnergyReadingA = (iReactiveEnergyReadingQ1A + iReactiveEnergyReadingQ2A + iReactiveEnergyReadingQ3A + iReactiveEnergyReadingQ4A)
				* calFactor_1;
		dReactiveEnergyReadingB = (iReactiveEnergyReadingQ1B + iReactiveEnergyReadingQ2B + iReactiveEnergyReadingQ3B + iReactiveEnergyReadingQ4B)
				* calFactor_1;
		dReactiveEnergyReadingC = (iReactiveEnergyReadingQ1C + iReactiveEnergyReadingQ2C + iReactiveEnergyReadingQ3C + iReactiveEnergyReadingQ4C)
				* calFactor_1;

		dApparentEnergyReadingA = iApparentEnergyReadingA * calFactor_1;
		dApparentEnergyReadingB = iApparentEnergyReadingB * calFactor_1;
		dApparentEnergyReadingC = iApparentEnergyReadingC * calFactor_1;

		dActiveEnergyReadingTotal = iActiveEnergyReadingTotal * calFactor_1;
		dReactiveEnergyReadingTotal = iReactiveEnergyReadingTotal * calFactor_1;
		dApparentEnergyReadingTotal = iApparentEnergyReadingTotal * calFactor_1;

		if ((dPrevActiveEnergyReadingA == 0) || (dPrevActiveEnergyReadingA > dActiveEnergyReadingA)) {
			dActiveEnergyA = 0;
		} else {
			dActiveEnergyA = (dActiveEnergyReadingA - dPrevActiveEnergyReadingA) * 1000;
		}
		dPrevActiveEnergyReadingA = dActiveEnergyReadingA;

		if ((dPrevActiveEnergyReadingB == 0) || (dPrevActiveEnergyReadingB > dActiveEnergyReadingB)) {
			dActiveEnergyB = 0;
		} else {
			dActiveEnergyB = (dActiveEnergyReadingB - dPrevActiveEnergyReadingB) * 1000;
		}
		dPrevActiveEnergyReadingB = dActiveEnergyReadingB;

		if ((dPrevActiveEnergyReadingC == 0) || (dPrevActiveEnergyReadingC > dActiveEnergyReadingC)) {
			dActiveEnergyC = 0;
		} else {
			dActiveEnergyC = (dActiveEnergyReadingC - dPrevActiveEnergyReadingC) * 1000;
		}
		dPrevActiveEnergyReadingC = dActiveEnergyReadingC;

		if ((dPrevActiveEnergyReadingTotal == 0) || (dPrevActiveEnergyReadingTotal > dActiveEnergyReadingTotal)) {
			dActiveEnergyTotal = 0;
		} else {
			dActiveEnergyTotal = (dActiveEnergyReadingTotal - dPrevActiveEnergyReadingTotal) * 1000;
		}
		dPrevActiveEnergyReadingTotal = dActiveEnergyReadingTotal;

		if ((dPrevReactiveEnergyReadingA == 0) || (dPrevReactiveEnergyReadingA > dReactiveEnergyReadingA)) {
			dReactiveEnergyA = 0;
		} else {
			dReactiveEnergyA = (dReactiveEnergyReadingA - dPrevReactiveEnergyReadingA) * 1000;
		}
		dPrevReactiveEnergyReadingA = dReactiveEnergyReadingA;

		if ((dPrevReactiveEnergyReadingB == 0) || (dPrevReactiveEnergyReadingB > dReactiveEnergyReadingB)) {
			dReactiveEnergyB = 0;
		} else {
			dReactiveEnergyB = (dReactiveEnergyReadingB - dPrevReactiveEnergyReadingB) * 1000;
		}
		dPrevReactiveEnergyReadingB = dReactiveEnergyReadingB;

		if ((dPrevReactiveEnergyReadingC == 0) || (dPrevReactiveEnergyReadingC > dReactiveEnergyReadingC)) {
			dReactiveEnergyC = 0;
		} else {
			dReactiveEnergyC = (dReactiveEnergyReadingC - dPrevReactiveEnergyReadingC) * 1000;
		}
		dPrevReactiveEnergyReadingC = dReactiveEnergyReadingC;

		if ((dPrevReactiveEnergyReadingTotal == 0) || (dPrevReactiveEnergyReadingTotal > dReactiveEnergyReadingTotal)) {
			dReactiveEnergyTotal = 0;
		} else {
			dReactiveEnergyTotal = (dReactiveEnergyReadingTotal - dPrevReactiveEnergyReadingTotal) * 1000;
		}
		dPrevReactiveEnergyReadingTotal = dReactiveEnergyReadingTotal;

		if ((dPrevApparentEnergyReadingA == 0) || (dPrevApparentEnergyReadingA > dApparentEnergyReadingA)) {
			dApparentEnergyA = 0;
		} else {
			dApparentEnergyA = (dApparentEnergyReadingA - dPrevApparentEnergyReadingA) * 1000;
		}
		dPrevApparentEnergyReadingA = dApparentEnergyReadingA;

		if ((dPrevApparentEnergyReadingB == 0) || (dPrevApparentEnergyReadingB > dApparentEnergyReadingB)) {
			dApparentEnergyB = 0;
		} else {
			dApparentEnergyB = (dApparentEnergyReadingB - dPrevApparentEnergyReadingB) * 1000;
		}
		dPrevApparentEnergyReadingB = dApparentEnergyReadingB;

		if ((dPrevApparentEnergyReadingC == 0) || (dPrevApparentEnergyReadingC > dApparentEnergyReadingC)) {
			dApparentEnergyC = 0;
		} else {
			dApparentEnergyC = (dApparentEnergyReadingC - dPrevApparentEnergyReadingC) * 1000;
		}
		dPrevApparentEnergyReadingC = dApparentEnergyReadingC;

		if ((dPrevApparentEnergyReadingTotal == 0) || (dPrevApparentEnergyReadingTotal > dApparentEnergyReadingTotal)) {
			dApparentEnergyTotal = 0;
		} else {
			dApparentEnergyTotal = (dApparentEnergyReadingTotal - dPrevApparentEnergyReadingTotal) * 1000;
		}
		dPrevApparentEnergyReadingTotal = dApparentEnergyReadingTotal;

	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		data.append("|DEVICEID=" + getId() + "-0-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + vformat.format(dActiveEnergyReadingTotal) + ",kWh");
		data.append(";Active Energy=" + vformat.format(dActiveEnergyTotal) + ",Wh");
		data.append(";Apparent Energy Reading=" + vformat.format(dApparentEnergyReadingTotal) + ",kVAh");
		data.append(";Apparent Energy=" + vformat.format(dApparentEnergyTotal) + ",VAh");
		data.append(";Reactive Energy Reading=" + vformat.format(dReactiveEnergyReadingTotal) + ",kVARh");
		data.append(";Reactive Energy=" + vformat.format(dReactiveEnergyTotal) + ",VARh");
		data.append(";Active Power=" + vformat.format(dActivePowerTotal) + ",kW");
		data.append(";Reactive Power=" + vformat.format(dReactivePowerTotal) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(dApparentPowerTotal) + ",kVA");
		data.append(";Power Factor=" + vformat.format(dPowerFactorTotal) + ",None");
		data.append(";Current=" + vformat.format(dAverageCurrent) + ",A");
		data.append(";Voltage=" + vformat.format(dAveragePhaseVoltage) + ",V");
		data.append(";Voltage L1-L2=" + vformat.format(dVoltageAB) + ",V");
		data.append(";Voltage L2-L3=" + vformat.format(dVoltageBC) + ",V");
		data.append(";Voltage L1-L3=" + vformat.format(dVoltageCA) + ",V");
		data.append(";Frequency=" + vformat.format(dFrequency) + ",Hz");
		data.append(";Current Neutral=" + vformat.format(dNeutralCurrent) + ",A");

		data.append("|DEVICEID=" + getId() + "-0-1");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + vformat.format(dActiveEnergyReadingA) + ",kWh");
		data.append(";Active Energy=" + vformat.format(dActiveEnergyA) + ",Wh");
		data.append(";Apparent Energy Reading=" + vformat.format(dApparentEnergyReadingA) + ",kVAh");
		data.append(";Apparent Energy=" + vformat.format(dApparentEnergyA) + ",VAh");
		data.append(";Reactive Energy Reading=" + vformat.format(dReactiveEnergyReadingA) + ",kVARh");
		data.append(";Reactive Energy=" + vformat.format(dReactiveEnergyA) + ",VARh");
		data.append(";Active Power=" + vformat.format(dActivePowerA) + ",kW");
		data.append(";Reactive Power=" + vformat.format(dReactivePowerA) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(dApparentPowerA) + ",kVA");
		data.append(";Current=" + vformat.format(dCurrentA) + ",A");
		data.append(";Voltage=" + vformat.format(dVoltageA) + ",V");
		data.append(";Power Factor=" + vformat.format(dPowerFactorA) + ",None");

		data.append("|DEVICEID=" + getId() + "-0-2");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + vformat.format(dActiveEnergyReadingB) + ",kWh");
		data.append(";Active Energy=" + vformat.format(dActiveEnergyB) + ",Wh");
		data.append(";Apparent Energy Reading=" + vformat.format(dApparentEnergyReadingB) + ",kVAh");
		data.append(";Apparent Energy=" + vformat.format(dApparentEnergyB) + ",VAh");
		data.append(";Reactive Energy Reading=" + vformat.format(dReactiveEnergyReadingB) + ",kVARh");
		data.append(";Reactive Energy=" + vformat.format(dReactiveEnergyB) + ",VARh");
		data.append(";Active Power=" + vformat.format(dActivePowerB) + ",kW");
		data.append(";Reactive Power=" + vformat.format(dReactivePowerB) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(dApparentPowerB) + ",kVA");
		data.append(";Current=" + vformat.format(dCurrentB) + ",A");
		data.append(";Voltage=" + vformat.format(dVoltageB) + ",V");
		data.append(";Power Factor=" + vformat.format(dPowerFactorB) + ",None");

		data.append("|DEVICEID=" + getId() + "-0-3");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + vformat.format(dActiveEnergyReadingC) + ",kWh");
		data.append(";Active Energy=" + vformat.format(dActiveEnergyC) + ",Wh");
		data.append(";Apparent Energy Reading=" + vformat.format(dApparentEnergyReadingC) + ",kVAh");
		data.append(";Apparent Energy=" + vformat.format(dApparentEnergyC) + ",VAh");
		data.append(";Reactive Energy Reading=" + vformat.format(dReactiveEnergyReadingC) + ",kVARh");
		data.append(";Reactive Energy=" + vformat.format(dReactiveEnergyC) + ",VARh");
		data.append(";Active Power=" + vformat.format(dActivePowerC) + ",kW");
		data.append(";Reactive Power=" + vformat.format(dReactivePowerC) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(dApparentPowerC) + ",kVA");
		data.append(";Current=" + vformat.format(dCurrentC) + ",A");
		data.append(";Voltage=" + vformat.format(dVoltageC) + ",V");
		data.append(";Power Factor=" + vformat.format(dPowerFactorC) + ",None");

		return data.toString();
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START, MBREG_CONFIG_LENGTH);
		device_config.clear();
		if (decodingData(0, data, CONFIG_MODE)) {
			for (int i = 0; i < MBREG_CONFIG_LENGTH; i++) {
				device_config.put(MBREG_CONFIG_START + i, String.valueOf(cfData[i]));
			}
		}
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		if ((config != null) && (!config.isEmpty())) {
			byte[] results = null;
			byte[] data = new byte[MBREG_CONFIG_LENGTH * 2];
			for (Integer n : config.keySet()) {
				cfData[n - MBREG_CONFIG_START] = Integer.parseInt(config.get(n));
			}

			for (int i = 0; i < MBREG_CONFIG_LENGTH; i++) {
				byte[] temp = ModbusUtil.unsignedShortToRegister(cfData[i]);
				data[2 * i] = temp[0];
				data[2 * i + 1] = temp[1];
			}
			results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG_START, data);
			if (results != null) {
				registers.addAll(config.keySet());
			}

		}
		return registers;
	}
	
	private void infoDebug() {
		mLogger.debug("=== Device Address = " + modbusid);
		mLogger.debug("===RawCTRatio = " +  iCTRatio + " - calculated CTRatio = " + dCTRatio);
	
		mLogger.debug("===iVoltageA = " +  iVoltageA);
		mLogger.debug("===iVoltageB = " +  iVoltageB);
		mLogger.debug("===iVoltageC = " +  iVoltageC);
		mLogger.debug("===iAveragePhaseVoltage = " +  iAveragePhaseVoltage);
		mLogger.debug("===iVoltageAB = " +  iVoltageAB);
		mLogger.debug("===iVoltageBC = " +  iVoltageBC);
		mLogger.debug("===iVoltageCA = " +  iVoltageCA);
		mLogger.debug("===iAverageLineVoltage = " +  iAverageLineVoltage);
		mLogger.debug("===iPositiveSequenceVoltage = " +  iPositiveSequenceVoltage);
		mLogger.debug("===iNegativeSequenceVoltage = " +  iNegativeSequenceVoltage);
		mLogger.debug("===iNeutralvoltage = " +  iNeutralvoltage);
		mLogger.debug("===iCurrentA = " +  iCurrentA);
		mLogger.debug("===iCurrentB = " +  iCurrentB);
		mLogger.debug("===iCurrentC = " +  iCurrentC);
		mLogger.debug("===iAverageCurrent = " +  iAverageCurrent);
		mLogger.debug("===iPositiveSequenceCurrent = " +  iPositiveSequenceCurrent);
		mLogger.debug("===iNegativeSequenceCurrent = " +  iNegativeSequenceCurrent);
		mLogger.debug("===iNeutralCurrent = " +  iNeutralCurrent);
		mLogger.debug("===iActivePowerA = " +  iActivePowerA);
		mLogger.debug("===iActivePowerB = " +  iActivePowerB);
		mLogger.debug("===iActivePowerC = " +  iActivePowerC);
		mLogger.debug("===iActivePowerTotal = " +  iActivePowerTotal);
		mLogger.debug("===iReactivePowerA = " +  iReactivePowerA);
		mLogger.debug("===iReactivePowerB = " +  iReactivePowerB);
		mLogger.debug("===iReactivePowerC = " +  iReactivePowerC);
		mLogger.debug("===iReactivePowerTotal = " +  iReactivePowerTotal);
		mLogger.debug("===iApparentPowerA = " +  iApparentPowerA);
		mLogger.debug("===iApparentPowerB = " +  iApparentPowerB);
		mLogger.debug("===iApparentPowerC = " +  iApparentPowerC);
		mLogger.debug("===iApparentPowerTotal = " +  iApparentPowerTotal);
		mLogger.debug("===iPowerFactorA = " +  iPowerFactorA);
		mLogger.debug("===iPowerFactorB = " +  iPowerFactorB);
		mLogger.debug("===iPowerFactorC = " +  iPowerFactorC);
		mLogger.debug("===iPowerFactorTotal = " +  iPowerFactorTotal);
		mLogger.debug("===iFrequency = " +  iFrequency);

		mLogger.debug("===iActiveEnergyReadingQ1A = " +  iActiveEnergyReadingQ1A);
		mLogger.debug("===iActiveEnergyReadingQ1B = " +  iActiveEnergyReadingQ1B);
		mLogger.debug("===iActiveEnergyReadingQ1C = " +  iActiveEnergyReadingQ1C);

		mLogger.debug("===iActiveEnergyReadingQ2A = " +  iActiveEnergyReadingQ2A);
		mLogger.debug("===iActiveEnergyReadingQ2B = " +  iActiveEnergyReadingQ2B);
		mLogger.debug("===iActiveEnergyReadingQ2C = " +  iActiveEnergyReadingQ2C);

		mLogger.debug("===iActiveEnergyReadingQ3A = " +  iActiveEnergyReadingQ3A);
		mLogger.debug("===iActiveEnergyReadingQ3B = " +  iActiveEnergyReadingQ3B);
		mLogger.debug("===iActiveEnergyReadingQ3C = " +  iActiveEnergyReadingQ3C);

		mLogger.debug("===iActiveEnergyReadingQ4A = " +  iActiveEnergyReadingQ4A);
		mLogger.debug("===iActiveEnergyReadingQ4B = " +  iActiveEnergyReadingQ4B);
		mLogger.debug("===iActiveEnergyReadingQ4C = " +  iActiveEnergyReadingQ4C);

		mLogger.debug("===iReactiveEnergyReadingQ1A = " +  iReactiveEnergyReadingQ1A);
		mLogger.debug("===iReactiveEnergyReadingQ1B = " +  iReactiveEnergyReadingQ1B);
		mLogger.debug("===iReactiveEnergyReadingQ1C = " +  iReactiveEnergyReadingQ1C);

		mLogger.debug("===iReactiveEnergyReadingQ2A = " +  iReactiveEnergyReadingQ2A);
		mLogger.debug("===iReactiveEnergyReadingQ2B = " +  iReactiveEnergyReadingQ2B);
		mLogger.debug("===iReactiveEnergyReadingQ2C = " +  iReactiveEnergyReadingQ2C);

		mLogger.debug("===iReactiveEnergyReadingQ3A = " +  iReactiveEnergyReadingQ3A);
		mLogger.debug("===iReactiveEnergyReadingQ3B = " +  iReactiveEnergyReadingQ3B);
		mLogger.debug("===iReactiveEnergyReadingQ3C = " +  iReactiveEnergyReadingQ3C);

		mLogger.debug("===iReactiveEnergyReadingQ4A = " +  iReactiveEnergyReadingQ4A);
		mLogger.debug("===iReactiveEnergyReadingQ4B = " +  iReactiveEnergyReadingQ4B);
		mLogger.debug("===iReactiveEnergyReadingQ4C = " +  iReactiveEnergyReadingQ4C);

		mLogger.debug("===iApparentEnergyReadingA = " +  iApparentEnergyReadingA);
		mLogger.debug("===iApparentEnergyReadingB = " +  iApparentEnergyReadingB);
		mLogger.debug("===iApparentEnergyReadingC = " +  iApparentEnergyReadingC);

		mLogger.debug("===iActiveEnergyReadingTotal = " +  iActiveEnergyReadingTotal);
		mLogger.debug("===iReactiveEnergyReadingTotal = " +  iReactiveEnergyReadingTotal);
		mLogger.debug("===iApparentEnergyReadingTotal = " +  iApparentEnergyReadingTotal);
		mLogger.debug("===================================================================");
	}
}
