package com.greenkoncepts.gateway.adapter.schneider;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class SchneiderPM710 extends SchneiderDevice {

	static public final int MBREG_DATA_START = 3999;
	static public final int MBREG_DATA_NUM = 45;

	static public final int MBREG_SCALE_FACTOR_START = 4105;
	static public final int MBREG_SCALE_FACTOR_NUM = 4;

	static public final int MBREG_CONFIG_START = 4117;
	static public final int MBREG_CONFIG_NUM = 12;

	private boolean hasScaleFactor = false;

	private double scaleFactorI = 0;
	private double scaleFactorV = 0;
	private double scaleFactorW = 0;
	private double scaleFactorE = 0;

	private int cfScalar[] = new int[MBREG_SCALE_FACTOR_NUM];
	private int cfData[] = new int[MBREG_CONFIG_NUM];

	private long prevActiveEnergyTotal = 0;
	private long activeEnergyTotal = 0;
	private long activeEnergyConsume = 0;
	private long prevApparentEnergyTotal = 0;
	private long apparentEnergyTotal = 0;
	private long apparentEnergyConsume = 0;
	private long prevReactiveEnergyTotal = 0;
	private long reactiveEnergyTotal = 0;
	private long reactiveEnergyConsume = 0;
	private int activePowerTotal = 0;
	private int apparentPowerTotal = 0;
	private int reactivePowerTotal = 0;
	private int powerFactor = 0;
	private int voltageLNAverage = 0;
	private int currentAverage = 0;
	private int frequency = 0;
	private int currentA = 0;
	private int currentB = 0;
	private int currentC = 0;
	private int voltageAB = 0;
	private int voltageBC = 0;
	private int voltageCA = 0;
	private int voltageAN = 0;
	private int voltageBN = 0;
	private int voltageCN = 0;
	private int activePowerA = 0;
	private int activePowerB = 0;
	private int activePowerC = 0;
	private int reactivePowerA = 0;
	private int reactivePowerB = 0;
	private int reactivePowerC = 0;
	private int apparentPowerA = 0;
	private int apparentPowerB = 0;
	private int apparentPowerC = 0;
	private int activePowerTotalDemandPeak = 0;

	static public int posActiveEnergyTotal = OFFSET_DATA + 2 * 0;
	static public int posApparentEnergyTotal = OFFSET_DATA + 2 * 2;
	static public int posReactiveEnergyTotal = OFFSET_DATA + 2 * 4;
	static public int posActivePowerTotal = OFFSET_DATA + 2 * 6;
	static public int posApparentPowerTotal = OFFSET_DATA + 2 * 7;
	static public int posReactivePowerTotal = OFFSET_DATA + 2 * 8;
	static public int posPowerFactor = OFFSET_DATA + 2 * 9;
	static public int posVoltageLNAverage = OFFSET_DATA + 2 * 11;
	static public int posCurrentAverage = OFFSET_DATA + 2 * 12;
	static public int posFrequency = OFFSET_DATA + 2 * 13;
	static public int posCurrentA = OFFSET_DATA + 2 * 20;
	static public int posCurrentB = OFFSET_DATA + 2 * 21;
	static public int posCurrentC = OFFSET_DATA + 2 * 22;
	static public int posVoltageAB = OFFSET_DATA + 2 * 30;
	static public int posVoltageBC = OFFSET_DATA + 2 * 31;
	static public int posVoltageCA = OFFSET_DATA + 2 * 32;
	static public int posVoltageAN = OFFSET_DATA + 2 * 33;
	static public int posVoltageBN = OFFSET_DATA + 2 * 34;
	static public int posVoltageCN = OFFSET_DATA + 2 * 35;
	static public int posActivePowerA = OFFSET_DATA + 2 * 36;
	static public int posActivePowerB = OFFSET_DATA + 2 * 37;
	static public int posActivePowerC = OFFSET_DATA + 2 * 38;
	static public int posApparentPowerA = OFFSET_DATA + 2 * 39;
	static public int posApparentPowerB = OFFSET_DATA + 2 * 40;
	static public int posApparentPowerC = OFFSET_DATA + 2 * 41;
	static public int posReactivePowerA = OFFSET_DATA + 2 * 42;
	static public int posReactivePowerB = OFFSET_DATA + 2 * 43;
	static public int posReactivePowerC = OFFSET_DATA + 2 * 44;
	static public int posActivePowerTotalDemandPeak = OFFSET_DATA + 2 * 17;

	public SchneiderPM710(int addr, String category) {
		super(category, addr);
	}

	@Override
	public String getDeviceData() {
		byte[] data = null;
		if (!hasScaleFactor) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_SCALE_FACTOR_START, MBREG_SCALE_FACTOR_NUM);
			if (decodingData(0, data, CONFIG_MODE)) {
				hasScaleFactor = true;
				setDataScalar();
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
			item.put("act_energy", vformat.format(activeEnergyTotal * scaleFactorE));
			item.put("rea_energy", vformat.format(reactiveEnergyTotal * scaleFactorE));
			item.put("app_energy", vformat.format(apparentEnergyTotal * scaleFactorE));

			item.put("act_power", vformat.format(activePowerTotal * scaleFactorW));
			item.put("act_power_l1", vformat.format(activePowerA * scaleFactorW));
			item.put("act_power_l2", vformat.format(activePowerB * scaleFactorW));
			item.put("act_power_l3", vformat.format(activePowerC * scaleFactorW));

			item.put("rea_power", vformat.format(reactivePowerTotal * scaleFactorW));
			item.put("rea_power_l1", vformat.format(reactivePowerA * scaleFactorW));
			item.put("rea_power_l2", vformat.format(reactivePowerB * scaleFactorW));
			item.put("rea_power_l3", vformat.format(reactivePowerC * scaleFactorW));

			item.put("app_power", vformat.format(apparentPowerTotal * scaleFactorW));
			item.put("app_power_l1", vformat.format(apparentPowerA * scaleFactorW));
			item.put("app_power_l2", vformat.format(apparentPowerB * scaleFactorW));
			item.put("app_power_l3", vformat.format(apparentPowerC * scaleFactorW));

			item.put("voltage", vformat.format(voltageLNAverage * scaleFactorV));
			item.put("voltage_l1", vformat.format(voltageAN * scaleFactorV));
			item.put("voltage_l2", vformat.format(voltageBN * scaleFactorV));
			item.put("voltage_l3", vformat.format(voltageCN * scaleFactorV));

			item.put("current", vformat.format(currentAverage * scaleFactorI));
			item.put("current_l1", vformat.format(currentA * scaleFactorI));
			item.put("current_l2", vformat.format(currentB * scaleFactorI));
			item.put("current_l3", vformat.format(currentC * scaleFactorI));

			item.put("v_l1_l2", vformat.format(voltageAB * scaleFactorV));
			item.put("v_l2_l3", vformat.format(voltageBC * scaleFactorV));
			item.put("v_l3_l1", vformat.format(voltageCA * scaleFactorV));

			item.put("peak_demand", vformat.format(activePowerTotalDemandPeak * scaleFactorW));
			item.put("pow_factor", vformat.format(powerFactor * 0.0001));
			item.put("frequency", vformat.format(frequency * 0.01));
			real_time_data.add(item);
		}
		return real_time_data;
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		device_config.clear();
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_SCALE_FACTOR_START, MBREG_SCALE_FACTOR_NUM);
		if (decodingData(0, data, CONFIG_MODE)) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START, MBREG_CONFIG_NUM);
			if (decodingData(1, data, CONFIG_MODE)) {
				for (int i = 0; i < MBREG_SCALE_FACTOR_NUM; i++) {
					device_config.put(MBREG_SCALE_FACTOR_START + i, String.valueOf(cfScalar[i]));
				}

				for (int j = 0; j < MBREG_CONFIG_NUM; j++) {
					device_config.put(MBREG_CONFIG_START + j, String.valueOf(cfData[j]));
				}
			}
		}
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		if ((config != null) && (!config.isEmpty())) {
			List<Integer> cfDataRegs = new ArrayList<Integer>();
			byte[] results = null;
			for (Integer n : config.keySet()) {
				cfData[n - MBREG_CONFIG_START] = Integer.parseInt(config.get(n));
				cfDataRegs.add(n);
			}

			if (!cfDataRegs.isEmpty()) {
				byte[] data = new byte[MBREG_CONFIG_NUM * 2];
				for (int i = 0; i < MBREG_CONFIG_NUM; i++) {
					byte[] temp = ModbusUtil.unsignedShortToRegister(cfData[i]);
					data[2 * i] = temp[0];
					data[2 * i + 1] = temp[1];
				}
				results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG_START, data);
				if (results != null) {
					registers.addAll(cfDataRegs);
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
			activeEnergyTotal = Math.abs(ModbusUtil.registersBEToLong(data, posActiveEnergyTotal));
			apparentEnergyTotal = Math.abs(ModbusUtil.registersBEToLong(data, posApparentEnergyTotal));
			reactiveEnergyTotal = Math.abs(ModbusUtil.registersBEToLong(data, posReactiveEnergyTotal));
			activePowerTotal = ModbusUtil.registerToShort(data, posActivePowerTotal);
			apparentPowerTotal = Math.abs(ModbusUtil.registerToShort(data, posApparentPowerTotal));
			reactivePowerTotal = ModbusUtil.registerToShort(data, posReactivePowerTotal);
			powerFactor = ModbusUtil.registerToShort(data, posPowerFactor);
			voltageLNAverage = ModbusUtil.registerToShort(data, posVoltageLNAverage);
			currentAverage = ModbusUtil.registerToShort(data, posCurrentAverage);
			frequency = ModbusUtil.registerToShort(data, posFrequency);
			activePowerTotalDemandPeak = ModbusUtil.registerToShort(data, posActivePowerTotalDemandPeak);
			currentA = ModbusUtil.registerToShort(data, posCurrentA);
			currentB = ModbusUtil.registerToShort(data, posCurrentB);
			currentC = ModbusUtil.registerToShort(data, posCurrentC);
			voltageAB = ModbusUtil.registerToShort(data, posVoltageAB);
			voltageBC = ModbusUtil.registerToShort(data, posVoltageBC);
			voltageCA = ModbusUtil.registerToShort(data, posVoltageCA);
			voltageAN = ModbusUtil.registerToShort(data, posVoltageAN);
			voltageBN = ModbusUtil.registerToShort(data, posVoltageBN);
			voltageCN = ModbusUtil.registerToShort(data, posVoltageCN);
			activePowerA = ModbusUtil.registerToShort(data, posActivePowerA);
			activePowerB = ModbusUtil.registerToShort(data, posActivePowerB);
			activePowerC = ModbusUtil.registerToShort(data, posActivePowerC);
			apparentPowerA = Math.abs(ModbusUtil.registerToShort(data, posApparentPowerA));
			apparentPowerB = Math.abs(ModbusUtil.registerToShort(data, posApparentPowerB));
			apparentPowerC = Math.abs(ModbusUtil.registerToShort(data, posApparentPowerC));
			reactivePowerA = ModbusUtil.registerToShort(data, posReactivePowerA);
			reactivePowerB = ModbusUtil.registerToShort(data, posReactivePowerB);
			reactivePowerC = ModbusUtil.registerToShort(data, posReactivePowerC);
			return true;
		}

		if (mode == CONFIG_MODE) {
			if (idx == 0) {
				for (int i = 0; i < MBREG_SCALE_FACTOR_NUM; i++) {
					cfScalar[i] = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * i);
				}
			} else if (idx == 1) {
				for (int i = 0; i < MBREG_CONFIG_NUM; i++) {
					cfData[i] = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * i);
				}
			}
			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		if (prevActiveEnergyTotal == 0) {
			activeEnergyConsume = 0;
		} else if (activeEnergyTotal >= prevActiveEnergyTotal) {
			activeEnergyConsume = activeEnergyTotal - prevActiveEnergyTotal;
		} else {
			if ((activeEnergyTotal < 100) && ((MAX_INT - prevActiveEnergyTotal) < 100)) {
				activeEnergyConsume = MAX_INT - prevActiveEnergyTotal + activeEnergyTotal;
			} else {
				activeEnergyConsume = 0;
			}
		}
		prevActiveEnergyTotal = activeEnergyTotal;

		if (prevApparentEnergyTotal == 0) {
			apparentEnergyConsume = 0;
		} else if (apparentEnergyTotal >= prevApparentEnergyTotal) {
			apparentEnergyConsume = apparentEnergyTotal - prevApparentEnergyTotal;
		} else {
			if ((apparentEnergyTotal < 100) && ((MAX_INT - prevApparentEnergyTotal) < 100)) {
				apparentEnergyConsume = MAX_INT - prevApparentEnergyTotal + apparentEnergyTotal;
			} else {
				apparentEnergyConsume = 0;
			}
		}
		prevApparentEnergyTotal = apparentEnergyTotal;

		if (prevReactiveEnergyTotal == 0) {
			reactiveEnergyConsume = 0;
		} else if (reactiveEnergyTotal >= prevReactiveEnergyTotal) {
			reactiveEnergyConsume = reactiveEnergyTotal - prevReactiveEnergyTotal;
		} else {
			if ((reactiveEnergyTotal < 100) && ((MAX_INT - prevReactiveEnergyTotal) < 100)) {
				reactiveEnergyConsume = MAX_INT - prevReactiveEnergyTotal + reactiveEnergyTotal;
			} else {
				reactiveEnergyConsume = 0;
			}
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
		data.append(";Active Energy Reading=" + vformat.format(activeEnergyTotal * scaleFactorE) + ",kWh");
		data.append(";Active Energy=" + vformat.format(activeEnergyConsume * scaleFactorE * 1000) + ",Wh");
		data.append(";Apparent Energy Reading=" + vformat.format(apparentEnergyTotal * scaleFactorE) + ",kVAh");
		data.append(";Apparent Energy=" + vformat.format(apparentEnergyConsume * scaleFactorE * 1000) + ",VAh");
		data.append(";Reactive Energy Reading=" + vformat.format(reactiveEnergyTotal * scaleFactorE) + ",kVARh");
		data.append(";Reactive Energy=" + vformat.format(reactiveEnergyConsume * scaleFactorE * 1000) + ",VARh");
		data.append(";Active Power=" + vformat.format(activePowerTotal * scaleFactorW) + ",kW");
		data.append(";Reactive Power=" + vformat.format(reactivePowerTotal * scaleFactorW) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(apparentPowerTotal * scaleFactorW) + ",kVA");
		data.append(";Power Factor=" + vformat.format(powerFactor * 0.0001) + ",None");
		data.append(";Current=" + vformat.format(currentAverage * scaleFactorI) + ",A");
		data.append(";Voltage=" + vformat.format(voltageLNAverage * scaleFactorV) + ",V");
		data.append(";Voltage L1-L2=" + vformat.format(voltageAB * scaleFactorV) + ",V");
		data.append(";Voltage L2-L3=" + vformat.format(voltageBC * scaleFactorV) + ",V");
		data.append(";Voltage L3-L1=" + vformat.format(voltageCA * scaleFactorV) + ",V");
		data.append(";Frequency=" + vformat.format(frequency * 0.01) + ",Hz");
		data.append(";Peak Demand=" + vformat.format(activePowerTotalDemandPeak * scaleFactorW) + ",kW");

		data.append("|DEVICEID=" + getId() + "-0-1");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(activePowerA * scaleFactorW) + ",kW");
		data.append(";Reactive Power=" + vformat.format(reactivePowerA * scaleFactorW) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(apparentPowerA * scaleFactorW) + ",kVA");
		data.append(";Current=" + vformat.format(currentA * scaleFactorI) + ",A");
		data.append(";Voltage=" + vformat.format(voltageAN * scaleFactorV) + ",V");

		data.append("|DEVICEID=" + getId() + "-0-2");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(activePowerB * scaleFactorW) + ",kW");
		data.append(";Reactive Power=" + vformat.format(reactivePowerB * scaleFactorW) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(apparentPowerB * scaleFactorW) + ",kVA");
		data.append(";Current=" + vformat.format(currentB * scaleFactorI) + ",A");
		data.append(";Voltage=" + vformat.format(voltageBN * scaleFactorV) + ",V");

		data.append("|DEVICEID=" + getId() + "-0-3");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(activePowerC * scaleFactorW) + ",kW");
		data.append(";Reactive Power=" + vformat.format(reactivePowerC * scaleFactorW) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(apparentPowerC * scaleFactorW) + ",kVA");
		data.append(";Current=" + vformat.format(currentC * scaleFactorI) + ",A");
		data.append(";Voltage=" + vformat.format(voltageCN * scaleFactorV) + ",V");
		return data.toString();
	}

	private void setDataScalar() {
		scaleFactorI = Math.pow(10, cfScalar[0]);
		if (scaleFactorI == 0) {
			scaleFactorI = 1;
		}

		scaleFactorV = Math.pow(10, cfScalar[1]);
		if (scaleFactorV == 0) {
			scaleFactorV = 1;
		}

		scaleFactorW = Math.pow(10, cfScalar[2]);
		if (scaleFactorW == 0) {
			scaleFactorW = 1;
		}

		scaleFactorE = /* Math.pow(10, cfScalar[3]) */0.01;// Temporarily use .01
		if (scaleFactorE == 0) {
			scaleFactorE = 1;
		}
	}
}
