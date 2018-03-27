package com.greenkoncepts.gateway.adapter.schneider;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class IEM3X00Series extends SchneiderDevice {
	static public int MBREG_DATA_START_0 = 3000;
	static public int MBREG_DATA_NUM_0 = 112;
	static public int MBREG_DATA_START_1 = 3204;
	static public int MBREG_DATA_NUM_1 = 24;

	static public int MBREG_CONFIG_START_0 = 30;
	static public int MBREG_CONFIG_NUM_0 = 60;
	static public int MBREG_CONFIG_START_1 = 2014;
	static public int MBREG_CONFIG_NUM_1 = 23;

	private String meterName = "";
	private String meterModel = "";
	private String manufacturer = "";
	private int numberOfPhases = 0;
	private int numberOfWires = 0;
	private int powerSystem = 0;
	private int nominalFrequency = 0;
	private int numberVTs = 0;
	private float VTPrimary = 0;
	private int VTSecondary = 0;
	private int numberCTs = 0;
	private int CTPrimary = 0;
	private int CTSecondary = 0;
	private int VTConnectionType = 0;


	private float currentA = 0;
	private float currentB = 0;
	private float currentC = 0;
	private float currentAverage = 0;
	private float voltageAB = 0;
	private float voltageBC = 0;
	private float voltageCA = 0;
	private float voltageAN = 0;
	private float voltageBN = 0;
	private float voltageCN = 0;
	private float voltageLNAverage = 0;
	private float activePowerA = 0;
	private float activePowerB = 0;
	private float activePowerC = 0;
	private float activePowerTotal = 0;
	private float apparentPowerTotal = 0;
	private float reactivePowerTotal = 0;
	private float powerFactor = 0;
	private float frequency = 0;

	private long prevActiveEnergyTotal = 0;
	private long activeEnergyTotal = 0;
	private long activeEnergyConsume = 0;
	//private long prevApparentEnergyTotal = 0;
	//private long apparentEnergyTotal = 0;
	private long apparentEnergyConsume = 0;
	private long prevReactiveEnergyTotal = 0;
	private long reactiveEnergyTotal = 0;
	private long reactiveEnergyConsume = 0;

	// private int reactivePowerA = 0;
	// private int reactivePowerB = 0;
	// private int reactivePowerC = 0;
	// private int apparentPowerA = 0;
	// private int apparentPowerB = 0;
	// private int apparentPowerC = 0;
	// private int activePowerTotalDemandPeak = 0;

	static public int posCurrentA = OFFSET_DATA + 2 * 0;
	static public int posCurrentB = OFFSET_DATA + 2 * 2;
	static public int posCurrentC = OFFSET_DATA + 2 * 4;
	static public int posCurrentAverage = OFFSET_DATA + 2 * 10;
	
	static public int posVoltageAB = OFFSET_DATA + 2 * 20;
	static public int posVoltageBC = OFFSET_DATA + 2 * 22;
	static public int posVoltageCA = OFFSET_DATA + 2 * 24;
	static public int posVoltageAN = OFFSET_DATA + 2 * 28;
	static public int posVoltageBN = OFFSET_DATA + 2 * 30;
	static public int posVoltageCN = OFFSET_DATA + 2 * 32;
	static public int posVoltageLNAverage = OFFSET_DATA + 2 * 36;
	
	static public int posActivePowerA = OFFSET_DATA + 2 * 54;
	static public int posActivePowerB = OFFSET_DATA + 2 * 56;
	static public int posActivePowerC = OFFSET_DATA + 2 * 58;
	static public int posActivePowerTotal = OFFSET_DATA + 2 * 60;
	static public int posReactivePowerTotal = OFFSET_DATA + 2 * 68;
	static public int posApparentPowerTotal = OFFSET_DATA + 2 * 76;

	static public int posActiveEnergyTotal = OFFSET_DATA + 2 * 0;
	static public int posReactiveEnergyTotal = OFFSET_DATA + 2 * 16;
	//static public int posApparentEnergyTotal = OFFSET_DATA + 2 * 4;


	static public int posPowerFactor = OFFSET_DATA + 2 * 84;
	static public int posFrequency = OFFSET_DATA + 2 * 110;

	public IEM3X00Series(int addr, String category) {
		super(category, addr);
	}

	@Override
	public String getDeviceData() {
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START_0, MBREG_DATA_NUM_0);
		if (decodingData(0, data, DATA_MODE)) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START_1, MBREG_DATA_NUM_1);
			if (decodingData(1, data, DATA_MODE)) {
				calculateDecodedData();
			}
		}
		return createDataSendToServer();
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START_0, MBREG_DATA_NUM_0);
			if (decodingData(0, data, DATA_MODE)) {
				data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START_1, MBREG_DATA_NUM_1);
				decodingData(0, data, DATA_MODE);
			}
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			item.put("act_energy", vformat.format(activeEnergyTotal));
			item.put("rea_energy", vformat.format(reactiveEnergyTotal));
			//item.put("app_energy", vformat.format(apparentEnergyTotal));

			item.put("act_power", vformat.format(activePowerTotal));
			item.put("act_power_l1", vformat.format(activePowerA));
			item.put("act_power_l2", vformat.format(activePowerB));
			item.put("act_power_l3", vformat.format(activePowerC));

			item.put("rea_power", vformat.format(reactivePowerTotal));
			item.put("app_power", vformat.format(apparentPowerTotal));

			item.put("voltage", vformat.format(voltageLNAverage));
			item.put("voltage_l1", vformat.format(voltageAN));
			item.put("voltage_l2", vformat.format(voltageBN));
			item.put("voltage_l3", vformat.format(voltageCN));

			item.put("current", vformat.format(currentAverage));
			item.put("current_l1", vformat.format(currentA));
			item.put("current_l2", vformat.format(currentB));
			item.put("current_l3", vformat.format(currentC));

			item.put("v_l1_l2", vformat.format(voltageAB));
			item.put("v_l2_l3", vformat.format(voltageBC));
			item.put("v_l3_l1", vformat.format(voltageCA));

			item.put("pow_factor", vformat.format(powerFactor));
			item.put("frequency", vformat.format(frequency));
			real_time_data.add(item);
		}
		return real_time_data;
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		device_config.clear();
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START_0, MBREG_CONFIG_NUM_0);
		if (decodingData(0, data, CONFIG_MODE)) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START_1, MBREG_CONFIG_NUM_1);
			if (decodingData(1, data, CONFIG_MODE)) {
				device_config.put(30, meterName);
				device_config.put(50, meterModel);
				device_config.put(70, manufacturer);
				device_config.put(2014, String.valueOf(numberOfPhases));
				device_config.put(2015, String.valueOf(numberOfWires));
				device_config.put(2016, String.valueOf(powerSystem));
				device_config.put(2017, String.valueOf(nominalFrequency));
				device_config.put(2025, String.valueOf(numberVTs));
				device_config.put(2026, String.valueOf(VTPrimary));
				device_config.put(2028, String.valueOf(VTSecondary));
				device_config.put(2029, String.valueOf(numberCTs));
				device_config.put(2030, String.valueOf(CTPrimary));
				device_config.put(2031, String.valueOf(CTSecondary));
				device_config.put(2036, String.valueOf(VTConnectionType));
			}
		}
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		return new ArrayList<Integer>();

	}

	private boolean decodingData(int idx, byte[] data, int mode) {
		if (data == null) {
			errorCount++;
			return false;
		}

		errorCount = 0;

		if (mode == DATA_MODE) {
			if (idx == 0) {
				currentA = ModbusUtil.ieee754RegistersToFloat(data, posCurrentA);
				currentB = ModbusUtil.ieee754RegistersToFloat(data, posCurrentB);
				currentC = ModbusUtil.ieee754RegistersToFloat(data, posCurrentC);
				currentAverage = ModbusUtil.ieee754RegistersToFloat(data, posCurrentAverage);

				voltageAB = ModbusUtil.ieee754RegistersToFloat(data, posVoltageAB);
				voltageBC = ModbusUtil.ieee754RegistersToFloat(data, posVoltageBC);
				voltageCA = ModbusUtil.ieee754RegistersToFloat(data, posVoltageCA);
				voltageAN = ModbusUtil.ieee754RegistersToFloat(data, posVoltageAN);
				voltageBN = ModbusUtil.ieee754RegistersToFloat(data, posVoltageBN);
				voltageCN = ModbusUtil.ieee754RegistersToFloat(data, posVoltageCN);
				voltageLNAverage = ModbusUtil.ieee754RegistersToFloat(data, posVoltageLNAverage);

				activePowerA = ModbusUtil.ieee754RegistersToFloat(data, posActivePowerA);
				activePowerB = ModbusUtil.ieee754RegistersToFloat(data, posActivePowerB);
				activePowerC = ModbusUtil.ieee754RegistersToFloat(data, posActivePowerC);

				activePowerTotal = ModbusUtil.ieee754RegistersToFloat(data, posActivePowerTotal);
				reactivePowerTotal = ModbusUtil.ieee754RegistersToFloat(data, posReactivePowerTotal);
				apparentPowerTotal = ModbusUtil.ieee754RegistersToFloat(data, posApparentPowerTotal);

				powerFactor = ModbusUtil.ieee754RegistersToFloat(data, posPowerFactor);
				frequency = ModbusUtil.ieee754RegistersToFloat(data, posFrequency);

			} else if (idx == 1) {
				activeEnergyTotal = ModbusUtil.registersToLong(data, posActiveEnergyTotal);
				//apparentEnergyTotal = ModbusUtil.registersToLong(data, posApparentEnergyTotal);
				reactiveEnergyTotal = ModbusUtil.registersToLong(data, posReactiveEnergyTotal);
			}
			return true;
		}

		if (mode == CONFIG_MODE) {
			if (idx == 0) {
				meterName = ModbusUtil.registerUTF8ToString(data, OFFSET_DATA + 2 * 0, 20);
				meterModel = ModbusUtil.registerUTF8ToString(data, OFFSET_DATA + 2 * 20, 20);
				manufacturer = ModbusUtil.registerUTF8ToString(data, OFFSET_DATA + 2 * 40, 20);
			} else if (idx == 1) {
				numberOfPhases = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2*0);
				numberOfWires = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2*1);
				powerSystem = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2*2);
				nominalFrequency = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2*3);
				numberVTs = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2*11);
				VTPrimary = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2*12);;
				VTSecondary = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2*14);
				numberCTs = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2*15);
				CTPrimary = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2*16);
				CTSecondary = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2*17);
				VTConnectionType = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2*22);
			} 
			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		if ((prevActiveEnergyTotal == 0) || (activeEnergyTotal < prevActiveEnergyTotal)) {
			activeEnergyConsume = 0;
		} else {
			activeEnergyConsume = activeEnergyTotal - prevActiveEnergyTotal;
		}

		prevActiveEnergyTotal = activeEnergyTotal;

//		if ((prevApparentEnergyTotal == 0) || (apparentEnergyTotal >= prevApparentEnergyTotal)) {
//			apparentEnergyConsume = 0;
//		} else {
//			apparentEnergyConsume = apparentEnergyTotal - prevApparentEnergyTotal;
//		}
//		prevApparentEnergyTotal = apparentEnergyTotal;

		if ((prevReactiveEnergyTotal == 0) || (reactiveEnergyTotal >= prevReactiveEnergyTotal)) {
			reactiveEnergyConsume = 0;
		} else {
			reactiveEnergyConsume = reactiveEnergyTotal - prevReactiveEnergyTotal;
		}

		prevReactiveEnergyTotal = reactiveEnergyTotal;

	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		data.append("|DEVICEID=" + getId() + "-0-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + vformat.format(activeEnergyTotal) + ",kWh");
		data.append(";Active Energy=" + vformat.format(activeEnergyConsume * 1000) + ",Wh");
		//data.append(";Apparent Energy Reading=" + vformat.format(apparentEnergyTotal) + ",kVAh");
		data.append(";Apparent Energy=" + vformat.format(apparentEnergyConsume * 1000) + ",VAh");
		data.append(";Reactive Energy Reading=" + vformat.format(reactiveEnergyTotal) + ",kVARh");
		data.append(";Reactive Energy=" + vformat.format(reactiveEnergyConsume * 1000) + ",VARh");
		data.append(";Active Power=" + vformat.format(activePowerTotal) + ",kW");
		data.append(";Reactive Power=" + vformat.format(reactivePowerTotal) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(apparentPowerTotal) + ",kVA");
		data.append(";Power Factor=" + vformat.format(powerFactor) + ",None");
		data.append(";Current=" + vformat.format(currentAverage) + ",A");
		data.append(";Voltage=" + vformat.format(voltageLNAverage) + ",V");
		data.append(";Voltage L1-L2=" + vformat.format(voltageAB) + ",V");
		data.append(";Voltage L2-L3=" + vformat.format(voltageBC) + ",V");
		data.append(";Voltage L3-L1=" + vformat.format(voltageCA) + ",V");
		data.append(";Frequency=" + vformat.format(frequency) + ",Hz");

		data.append("|DEVICEID=" + getId() + "-0-1");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(activePowerA) + ",kW");
		data.append(";Current=" + vformat.format(currentA) + ",A");
		data.append(";Voltage=" + vformat.format(voltageAN) + ",V");

		data.append("|DEVICEID=" + getId() + "-0-2");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(activePowerB) + ",kW");
		data.append(";Current=" + vformat.format(currentB) + ",A");
		data.append(";Voltage=" + vformat.format(voltageBN) + ",V");

		data.append("|DEVICEID=" + getId() + "-0-3");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(activePowerC) + ",kW");
		data.append(";Current=" + vformat.format(currentC) + ",A");
		data.append(";Voltage=" + vformat.format(voltageCN) + ",V");
		return data.toString();
	}
}