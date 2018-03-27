package com.greenkoncepts.gateway.adapter.emerson;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class LiebertNXR extends EmersonDevice {
	public static int MBREG_DATA_START = 1050;
	public static int MBREG_DATA_NUM = 47;

	public static float scalar_0_1 = 0.1f;
	public static float scalar_0_01 = 0.01f;

	// input
	public static int posInputCableVoltageAB = 11;
	public static int posInputCableVoltageBC = 12;
	public static int posInputCableVoltageCA = 13;
	public static int posInputPhaseCurrentA = 14;
	public static int posInputPhaseCurrentB = 15;
	public static int posInputPhaseCurrentC = 16;
	public static int posInputFrequency = 17;
	public static int posInputPhaseVoltageA = 0;
	public static int posInputPhaseVoltageB = 1;
	public static int posInputPhaseVoltageC = 2;
	// bypass
	public static int posBypassPhaseVoltageA = 21;
	public static int posBypassPhaseVoltageB = 22;
	public static int posBypassPhaseVoltageC = 23;
	public static int posBypassFrequency = 24;

	// Battery
	public static int posBatteryBackupTime = 40;
	public static int posPositiveBatteryVoltage = 41;
	public static int posPositiveBatteryCurrent = 42;
	public static int posNegativeBatteryVoltage = 43;
	public static int posNegativeBatteryCurrent = 44;
	public static int posBatteryAgingCoefficient = 45;
	public static int posBatteryTemperature = 46;

	// Output
	public static int posOutputPhaseVoltageA = 3;
	public static int posOutputPhaseVoltageB = 4;
	public static int posOutputPhaseVoltageC = 5;
	public static int posOutputPhaseCurrentA = 6;
	public static int posOutputPhaseCurrentB = 7;
	public static int posOutputPhaseCurrentC = 8;
	public static int posOutputFrequency = 10;
	public static int posOutputActivePowerPhsA = 31;
	public static int posOutputActivePowerPhsB = 32;
	public static int posOutputActivePowerPhsC = 33;
	public static int posOutputReactivePowerPhsA = 34;
	public static int posOutputReactivePowerPhsB = 35;
	public static int posOutputReactivePowerPhsC = 36;
	public static int posOutputApparentPowerPhsA = 37;
	public static int posOutputApparentPowerPhsB = 38;
	public static int posOutputApparentPowerPhsC = 39;

	// input
	private int InputCableVoltageAB = 0;
	private int InputCableVoltageBC = 0;
	private int InputCableVoltageCA = 0;
	private int InputPhaseCurrentA = 0;
	private int InputPhaseCurrentB = 0;
	private int InputPhaseCurrentC = 0;
	private int InputFrequency = 0;
	private int InputPhaseVoltageA = 0;
	private int InputPhaseVoltageB = 0;
	private int InputPhaseVoltageC = 0;
	// bypass
	private int BypassPhaseVoltageA = 0;
	private int BypassPhaseVoltageB = 0;
	private int BypassPhaseVoltageC = 0;
	private int BypassFrequency = 0;

	// Battery
	private int BatteryBackupTime = 0;
	private int PositiveBatteryVoltage = 0;
	private int PositiveBatteryCurrent = 0;
	private int NegativeBatteryVoltage = 0;
	private int NegativeBatteryCurrent = 0;
	private int BatteryAgingCoefficient = 0;
	private int BatteryTemperature = 0;

	// Output
	private int OutputPhaseVoltageA = 0;
	private int OutputPhaseVoltageB = 0;
	private int OutputPhaseVoltageC = 0;
	private int OutputPhaseCurrentA = 0;
	private int OutputPhaseCurrentB = 0;
	private int OutputPhaseCurrentC = 0;
	private int OutputFrequency = 0;
	private int OutputActivePowerPhsA = 0;
	private int OutputActivePowerPhsB = 0;
	private int OutputActivePowerPhsC = 0;
	private int OutputReactivePowerPhsA = 0;
	private int OutputReactivePowerPhsB = 0;
	private int OutputReactivePowerPhsC = 0;
	private int OutputApparentPowerPhsA = 0;
	private int OutputApparentPowerPhsB = 0;
	private int OutputApparentPowerPhsC = 0;
	private int TotalOutputActivePower = 0;
	private int TotalOutputReactivePower = 0;
	private int TotalOutputApparentPower = 0;

	public LiebertNXR(int addr, String category) {
		super(category, addr);
	}

	@Override
	public String getDeviceData() {
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
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
			Map<String, String> item = new Hashtable<String, String>();
			item.put("InputCableVoltageAB", vformat.format(InputCableVoltageAB * scalar_0_1));
			item.put("InputCableVoltageBC", vformat.format(InputCableVoltageBC * scalar_0_1));
			item.put("InputCableVoltageCA", vformat.format(InputCableVoltageCA * scalar_0_1));
			item.put("InputPhaseCurrentA", vformat.format(InputPhaseCurrentA * scalar_0_1));
			item.put("InputPhaseCurrentB", vformat.format(InputPhaseCurrentB * scalar_0_1));
			item.put("InputPhaseCurrentC", vformat.format(InputPhaseCurrentC * scalar_0_1));
			item.put("InputFrequency", vformat.format(InputFrequency * scalar_0_01));
			item.put("InputPhaseVoltageA", vformat.format(InputPhaseVoltageA * scalar_0_1));
			item.put("InputPhaseVoltageB", vformat.format(InputPhaseVoltageB * scalar_0_1));
			item.put("InputPhaseVoltageC", vformat.format(InputPhaseVoltageC * scalar_0_1));

			// bypass
			item.put("BypassPhaseVoltageA", vformat.format(BypassPhaseVoltageA * scalar_0_1));
			item.put("BypassPhaseVoltageB", vformat.format(BypassPhaseVoltageB * scalar_0_1));
			item.put("BypassPhaseVoltageC", vformat.format(BypassPhaseVoltageC * scalar_0_1));
			item.put("BypassFrequency", vformat.format(BypassFrequency * scalar_0_01));

			// Battery
			item.put("BatteryBackupTime", vformat.format(BatteryBackupTime));
			item.put("PositiveBatteryVoltage", vformat.format(PositiveBatteryVoltage * scalar_0_1));
			item.put("PositiveBatteryCurrent", vformat.format(PositiveBatteryCurrent * scalar_0_1));
			item.put("NegativeBatteryVoltage", vformat.format(NegativeBatteryVoltage * scalar_0_1));
			item.put("NegativeBatteryCurrent", vformat.format(NegativeBatteryCurrent * scalar_0_1));
			item.put("BatteryAgingCoefficient", vformat.format(BatteryAgingCoefficient * scalar_0_1));
			item.put("BatteryTemperature", vformat.format(BatteryTemperature * scalar_0_1));

			// Output
			item.put("OutputPhaseVoltageA", vformat.format(OutputPhaseVoltageA * scalar_0_1));
			item.put("OutputPhaseVoltageB", vformat.format(OutputPhaseVoltageB * scalar_0_1));
			item.put("OutputPhaseVoltageC", vformat.format(OutputPhaseVoltageC * scalar_0_1));
			item.put("OutputPhaseCurrentA", vformat.format(OutputPhaseCurrentA * scalar_0_1));
			item.put("OutputPhaseCurrentB", vformat.format(OutputPhaseCurrentB * scalar_0_1));
			item.put("OutputPhaseCurrentC", vformat.format(OutputPhaseCurrentC * scalar_0_1));
			item.put("OutputFrequency", vformat.format(OutputFrequency * scalar_0_01));
			item.put("OutputActivePowerPhsA", vformat.format(OutputActivePowerPhsA * scalar_0_01));
			item.put("OutputActivePowerPhsB", vformat.format(OutputActivePowerPhsB * scalar_0_01));
			item.put("OutputActivePowerPhsC", vformat.format(OutputActivePowerPhsC * scalar_0_01));
			item.put("OutputReactivePowerPhsA", vformat.format(OutputReactivePowerPhsA * scalar_0_01));
			item.put("OutputReactivePowerPhsB", vformat.format(OutputReactivePowerPhsB * scalar_0_01));
			item.put("OutputReactivePowerPhsC", vformat.format(OutputReactivePowerPhsC * scalar_0_01));
			item.put("OutputApparentPowerPhsA", vformat.format(OutputApparentPowerPhsA * scalar_0_01));
			item.put("OutputApparentPowerPhsB", vformat.format(OutputApparentPowerPhsB * scalar_0_01));
			item.put("OutputApparentPowerPhsC", vformat.format(OutputApparentPowerPhsC * scalar_0_01));
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
			InputCableVoltageAB = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posInputCableVoltageAB);
			InputCableVoltageBC = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posInputCableVoltageBC);
			InputCableVoltageCA = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posInputCableVoltageCA);
			InputPhaseCurrentA = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posInputPhaseCurrentA);
			InputPhaseCurrentB = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posInputPhaseCurrentB);
			InputPhaseCurrentC = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posInputPhaseCurrentC);
			InputFrequency = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posInputFrequency);
			InputPhaseVoltageA = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posInputPhaseVoltageA);
			InputPhaseVoltageB = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posInputPhaseVoltageB);
			InputPhaseVoltageC = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posInputPhaseVoltageC);
			// bypass
			BypassPhaseVoltageA = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posBypassPhaseVoltageA);
			BypassPhaseVoltageB = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posBypassPhaseVoltageB);
			BypassPhaseVoltageC = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posBypassPhaseVoltageC);
			BypassFrequency = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posBypassFrequency);

			// Battery
			BatteryBackupTime = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posBatteryBackupTime);
			PositiveBatteryVoltage = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posPositiveBatteryVoltage);
			PositiveBatteryCurrent = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posPositiveBatteryCurrent);
			NegativeBatteryVoltage = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posNegativeBatteryVoltage);
			NegativeBatteryCurrent = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posNegativeBatteryCurrent);
			BatteryTemperature = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posBatteryTemperature);
			BatteryAgingCoefficient = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posBatteryAgingCoefficient);

			// Output
			OutputPhaseVoltageA = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posOutputPhaseVoltageA);
			OutputPhaseVoltageB = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posOutputPhaseVoltageB);
			OutputPhaseVoltageC = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posOutputPhaseVoltageC);
			OutputPhaseCurrentA = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posOutputPhaseCurrentA);
			OutputPhaseCurrentB = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posOutputPhaseCurrentB);
			OutputPhaseCurrentC = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posOutputPhaseCurrentC);
			OutputFrequency = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posOutputFrequency);
			OutputActivePowerPhsA = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posOutputActivePowerPhsA);
			OutputActivePowerPhsB = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posOutputActivePowerPhsB);
			OutputActivePowerPhsC = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posOutputActivePowerPhsC);
			OutputReactivePowerPhsA = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posOutputReactivePowerPhsA);
			OutputReactivePowerPhsB = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posOutputReactivePowerPhsB);
			OutputReactivePowerPhsC = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posOutputReactivePowerPhsC);
			OutputApparentPowerPhsA = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posOutputApparentPowerPhsA);
			OutputApparentPowerPhsB = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posOutputApparentPowerPhsB);
			OutputApparentPowerPhsC = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posOutputApparentPowerPhsC);

			return true;
		}
		if (mode == CONFIG_MODE) {
			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		TotalOutputActivePower = OutputActivePowerPhsA + OutputActivePowerPhsB + OutputActivePowerPhsC;
		TotalOutputReactivePower = OutputReactivePowerPhsA + OutputReactivePowerPhsB + OutputReactivePowerPhsC;
		TotalOutputApparentPower = OutputApparentPowerPhsA + OutputApparentPowerPhsB + OutputApparentPowerPhsC;
	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		data.append("|DEVICEID=" + getId() + "-" + 0 + "-0");
		data.append(";TIMESTAMP=" + timestamp);
		// input
		data.append(";Input Voltage L1-L2=" + vformat.format(InputCableVoltageAB * scalar_0_1) + ",V");
		data.append(";Input Voltage L2-L3=" + vformat.format(InputCableVoltageBC * scalar_0_1) + ",V");
		data.append(";Input Voltage L1-L3=" + vformat.format(InputCableVoltageCA * scalar_0_1) + ",V");
		data.append(";Input Current L1=" + vformat.format(InputPhaseCurrentA * scalar_0_1) + ",A");
		data.append(";Input Current L2=" + vformat.format(InputPhaseCurrentB * scalar_0_1) + ",A");
		data.append(";Input Current L3=" + vformat.format(InputPhaseCurrentC * scalar_0_1) + ",A");
		data.append(";Input Frequency=" + vformat.format(InputFrequency * scalar_0_01) + ",Hz");
		data.append(";Input Voltage L1-N=" + vformat.format(InputPhaseVoltageA * scalar_0_1) + ",V");
		data.append(";Input Voltage L2-N=" + vformat.format(InputPhaseVoltageB * scalar_0_1) + ",V");
		data.append(";Input Voltage L3-N=" + vformat.format(InputPhaseVoltageC * scalar_0_1) + ",V");
		// bypass
		data.append(";Bypass Voltage L1-N=" + vformat.format(BypassPhaseVoltageA * scalar_0_1) + ",V");
		data.append(";Bypass Voltage L2-N=" + vformat.format(BypassPhaseVoltageB * scalar_0_1) + ",V");
		data.append(";Bypass Voltage L3-N=" + vformat.format(BypassPhaseVoltageC * scalar_0_1) + ",V");
		data.append(";Bypass Frequency=" + vformat.format(BypassFrequency * scalar_0_01) + ",Hz");

		// Battery
		data.append(";Remain Battery Time=" + vformat.format(BatteryBackupTime) + ",Min");
		data.append(";Battery Voltage=" + vformat.format((PositiveBatteryVoltage + NegativeBatteryVoltage) * scalar_0_1) + ",V");
		data.append(";Battery Current=" + vformat.format(PositiveBatteryCurrent * scalar_0_1) + ",A");
		data.append(";Battery Aging Coefficient=" + vformat.format(BatteryAgingCoefficient * scalar_0_1) + ",None");
		data.append(";Battery Temperature=" + vformat.format(BatteryTemperature * scalar_0_1) + ",C");

		// Output
		data.append(";Output Voltage L1-N=" + vformat.format(OutputPhaseVoltageA * scalar_0_1) + ",V");
		data.append(";Output Voltage L2-N=" + vformat.format(OutputPhaseVoltageB * scalar_0_1) + ",V");
		data.append(";Output Voltage L3-N=" + vformat.format(OutputPhaseVoltageC * scalar_0_1) + ",V");
		data.append(";Output Current L1=" + vformat.format(OutputPhaseCurrentA * scalar_0_1) + ",A");
		data.append(";Output Current L2=" + vformat.format(OutputPhaseCurrentB * scalar_0_1) + ",A");
		data.append(";Output Current L3=" + vformat.format(OutputPhaseCurrentC * scalar_0_1) + ",A");
		data.append(";Output Frequency=" + vformat.format(OutputFrequency * scalar_0_01) + ",Hz");
		data.append(";Output Active Power L1=" + vformat.format(OutputActivePowerPhsA * scalar_0_01) + ",kW");
		data.append(";Output Active Power L2=" + vformat.format(OutputActivePowerPhsB * scalar_0_01) + ",kW");
		data.append(";Output Active Power L3=" + vformat.format(OutputActivePowerPhsC * scalar_0_01) + ",kW");
		data.append(";Total Output Active Power=" + vformat.format(TotalOutputActivePower * scalar_0_01) + ",kW");
		data.append(";Output Reactive Power L1=" + vformat.format(OutputReactivePowerPhsA * scalar_0_01) + ",kVAR");
		data.append(";Output Reactive Power L2=" + vformat.format(OutputReactivePowerPhsB * scalar_0_01) + ",kVAR");
		data.append(";Output Reactive Power L3=" + vformat.format(OutputReactivePowerPhsC * scalar_0_01) + ",kVAR");
		data.append(";Total Output Reactive Power=" + vformat.format(TotalOutputReactivePower * scalar_0_01) + ",kVAR");
		data.append(";Output Apparent Power L1=" + vformat.format(OutputApparentPowerPhsA * scalar_0_01) + ",kVA");
		data.append(";Output Apparent Power L2=" + vformat.format(OutputApparentPowerPhsB * scalar_0_01) + ",kVA");
		data.append(";Output Apparent Power L3=" + vformat.format(OutputApparentPowerPhsC * scalar_0_01) + ",kVA");
		data.append(";Total Output Apparent Power=" + vformat.format(TotalOutputApparentPower * scalar_0_01) + ",kVA");
		return data.toString();
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		// TODO Auto-generated method stub
		return null;
	}
}