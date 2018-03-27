package com.greenkoncepts.gateway.adapter.riello;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class Multicom30X extends RielloDevice {
	public static int MBREG_STATUS_START = 0x0000;
	public static int MBREG_STATUS_NUM = 0x0004;

	public static int OFFSET_STATUS_REG0 = 0;
	public static int offTestInProgress = 0x0002;
	public static int offShutdownActive = 0x0008;
	public static int offBatteryCharged = 0x0020;
	public static int offBatteryCharging = 0x0040;
	public static int offBypassBad = 0x0080;
	public static int offNormalOperation = 0x0200;
	public static int offOnBypass = 0x0800;
	public static int offBatteryLow = 0x1000;
	public static int offBatteryWorking = 0x2000;
	public static int offUPSLocked = 0x4000;
	public static int offOutputPowered = 0x8000;

	public static int OFFSET_STATUS_REG1 = 1;
	public static int offInputMainsPresent = 0x1000;
	public static int offAlarmTemperature = 0x2000;
	public static int offAlarmOverload = 0x4000;
	public static int offUPSFailure = 0x8000;

	public static int OFFSET_STATUS_REG3 = 3;
	public static int offCommunicationLost = 0x8000;
	
	public static int MBREG_COMMAND_CODE = 112;

	public static int MBREG_DATA_START = 0x000B;
	public static int MBREG_DATA_NUM = 0x0035;

	private static final float CURRENT_SCALAR = 0.1f;
	private static final float FREQUENCY_SCALAR = 0.1f;
	private static final float BATTERY_VOLTAGE_SCALAR = 0.1f;
	private static final float BATTERY_CURRENT_SCALAR = 0.1f;
	private static final float TOTAL_ENERGY_SCALAR = 0.1f;
	private static final float POWER_SCALAR = 0.1f;

	private int[] cfData = new int[MBREG_STATUS_NUM];

	private float inputVoltageL1N = 0.0f;
	private float inputVoltageL2N = 0.0f;
	private float inputVoltageL3N = 0.0f;
	private float inputCurrentL1 = 0.0f;
	private float inputCurrentL2 = 0.0f;
	private float inputCurrentL3 = 0.0f;
	private float inputFrequency = 0.0f;
	private float bypassMainsVoltageL1N = 0.0f;
	private float bypassMainsVoltageL2N = 0.0f;
	private float bypassMainsVoltageL3N = 0.0f;
	private float bypassFrequency = 0.0f;
	private float outputStarVoltageL1N = 0.0f;
	private float outputStarVoltageL2N = 0.0f;
	private float outputStarVoltageL3N = 0.0f;
	private float outputCurrentL1 = 0.0f;
	private float outputCurrentL2 = 0.0f;
	private float outputCurrentL3 = 0.0f;
	private float outputPeakCurrentL1 = 0.0f;
	private float outputPeakCurrentL2 = 0.0f;
	private float outputPeakCurrentL3 = 0.0f;
	private int loadPhaseL1 = 0;
	private int loadPhaseL2 = 0;
	private int loadPhaseL3 = 0;
	private float outputActivePowerL1 = 0.0f;
	private float outputActivePowerL2 = 0.0f;
	private float outputActivePowerL3 = 0.0f;
	private float outputFrequency = 0.0f;
	private float batteryVoltage = 0.0f;
	private float positiveBatteryVoltage = 0.0f;
	private float negativeBatteryVoltage = 0.0f;
	private float batteryCurrent = 0.0f;
	private int remainingBatteryCapacity = 0;
	private int remainingBackupTimeMins = 0;
	private float activeEnergy = 0.0f;
	private float totalOutputEnergy = 0.0f;
	private float prevTotalOutputEnergy = 0.0f;
	private float internalUpsTemperature = 0.0f;
	private float sensor1Temperature = 0.0f;
	private float sensor2Temperature = 0.0f;
	
	private String testingInProgress;
	private String shutdownActive;
	private String batteryCharged;
	private String batteryCharging;
	private String bypassBad;
	private String normalOperation;
	private String onBypass;
	private String batteryLow;
	private String batteryWorking;
	private String upsLocked;
	private String outputPowered;
	private String inputMainsPresent;
	private String alarmTemperature;
	private String alarmOverload;
	private String upsFailure;
	private String lostCommsWithUps;

	public Multicom30X(int addr, String category) {
		super(category, addr);
	}

	@Override
	public String getDeviceData() {
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
		if (decodingData(0, data, DATA_MODE)) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_STATUS_START, MBREG_STATUS_NUM);
			if (decodingData(1, data, DATA_MODE)) {
				calculateDecodedData();
			}
		}
		return createDataSendToServer();
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
			decodingData(0, data, DATA_MODE);
			data = modbus.readHoldingRegisters(modbusid, MBREG_STATUS_START, MBREG_STATUS_NUM);
			decodingData(1, data, DATA_MODE);
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			item.put("inputVoltageL1N", vformat.format(inputVoltageL1N));
			item.put("inputVoltageL2N", vformat.format(inputVoltageL2N));
			item.put("inputVoltageL3N", vformat.format(inputVoltageL3N));
			item.put("inputCurrentL1", vformat.format(inputCurrentL1));
			item.put("inputCurrentL2", vformat.format(inputCurrentL2));
			item.put("inputCurrentL3", vformat.format(inputCurrentL3));
			item.put("inputFrequency", vformat.format(inputFrequency));

			item.put("bypassMainsVoltageL1N", vformat.format(bypassMainsVoltageL1N));
			item.put("bypassMainsVoltageL2N", vformat.format(bypassMainsVoltageL2N));
			item.put("bypassMainsVoltageL3N", vformat.format(bypassMainsVoltageL3N));
			item.put("bypassFrequency", vformat.format(bypassFrequency));
			item.put("outputStarVoltageL1N", vformat.format(outputStarVoltageL1N));
			item.put("outputStarVoltageL2N", vformat.format(outputStarVoltageL2N));
			item.put("outputStarVoltageL3N", vformat.format(outputStarVoltageL3N));

			item.put("outputCurrentL1", vformat.format(outputCurrentL1));
			item.put("outputCurrentL2", vformat.format(outputCurrentL2));
			item.put("outputCurrentL3", vformat.format(outputCurrentL3));
			item.put("outputPeakCurrentL1", vformat.format(outputPeakCurrentL1));
			item.put("outputPeakCurrentL2", vformat.format(outputPeakCurrentL2));
			item.put("outputPeakCurrentL3", vformat.format(outputPeakCurrentL3));
			item.put("loadPhaseL1", String.valueOf(loadPhaseL1));
			item.put("loadPhaseL2", String.valueOf(loadPhaseL2));
			item.put("loadPhaseL3", String.valueOf(loadPhaseL3));
			item.put("outputActivePowerL1", vformat.format(outputActivePowerL1));
			item.put("outputActivePowerL2", vformat.format(outputActivePowerL2));
			item.put("outputActivePowerL3", vformat.format(outputActivePowerL3));
			item.put("outputFrequency", vformat.format(outputFrequency));

			item.put("batteryVoltage", vformat.format(batteryVoltage));
			item.put("positiveBatteryVoltage", vformat.format(positiveBatteryVoltage));
			item.put("negativeBatteryVoltage", vformat.format(negativeBatteryVoltage));
			item.put("batteryCurrent", vformat.format(batteryCurrent));
			item.put("remainingBatteryCapacity", String.valueOf(remainingBatteryCapacity));
			item.put("remainingBackupTimeMins", String.valueOf(remainingBackupTimeMins));

			item.put("totalOutputEnergy", vformat.format(totalOutputEnergy));

			item.put("internalUpsTemperature", vformat.format(internalUpsTemperature));
			item.put("sensor1Temperature", vformat.format(sensor1Temperature));
			item.put("sensor2Temperature", vformat.format(sensor2Temperature));
			item.put("lostCommsWithUps", lostCommsWithUps);
			item.put("alarmOverload", alarmOverload);
			real_time_data.add(item);
		}
		return real_time_data;
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		device_config.clear();
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_STATUS_START, MBREG_STATUS_NUM);
		if (decodingData(0, data, CONFIG_MODE)) {
			device_config.put(OFFSET_STATUS_REG0 * 100 + 1, String.valueOf(cfData[OFFSET_STATUS_REG0] & offTestInProgress));
			device_config.put(OFFSET_STATUS_REG0 * 100 + 3, String.valueOf(cfData[OFFSET_STATUS_REG0] & offShutdownActive));
			device_config.put(OFFSET_STATUS_REG0 * 100 + 5, String.valueOf(cfData[OFFSET_STATUS_REG0] & offBatteryCharged));
			device_config.put(OFFSET_STATUS_REG0 * 100 + 6, String.valueOf(cfData[OFFSET_STATUS_REG0] & offBatteryCharging));
			device_config.put(OFFSET_STATUS_REG0 * 100 + 7, String.valueOf(cfData[OFFSET_STATUS_REG0] & offBypassBad));
			device_config.put(OFFSET_STATUS_REG0 * 100 + 9, String.valueOf(cfData[OFFSET_STATUS_REG0] & offNormalOperation));
			device_config.put(OFFSET_STATUS_REG0 * 100 + 11, String.valueOf(cfData[OFFSET_STATUS_REG0] & offOnBypass));
			device_config.put(OFFSET_STATUS_REG0 * 100 + 12, String.valueOf(cfData[OFFSET_STATUS_REG0] & offBatteryLow));
			device_config.put(OFFSET_STATUS_REG0 * 100 + 13, String.valueOf(cfData[OFFSET_STATUS_REG0] & offBatteryWorking));
			device_config.put(OFFSET_STATUS_REG0 * 100 + 14, String.valueOf(cfData[OFFSET_STATUS_REG0] & offUPSLocked));
			device_config.put(OFFSET_STATUS_REG0 * 100 + 15, String.valueOf(cfData[OFFSET_STATUS_REG0] & offOutputPowered));

			device_config.put(OFFSET_STATUS_REG1 * 100 + 12, String.valueOf(cfData[OFFSET_STATUS_REG1] & offInputMainsPresent));
			device_config.put(OFFSET_STATUS_REG1 * 100 + 13, String.valueOf(cfData[OFFSET_STATUS_REG1] & offAlarmTemperature));
			device_config.put(OFFSET_STATUS_REG1 * 100 + 14, String.valueOf(cfData[OFFSET_STATUS_REG1] & offAlarmOverload));
			device_config.put(OFFSET_STATUS_REG1 * 100 + 15, String.valueOf(cfData[OFFSET_STATUS_REG1] & offUPSFailure));

			device_config.put(OFFSET_STATUS_REG3 * 100 + 15, String.valueOf(cfData[OFFSET_STATUS_REG3] & offCommunicationLost));
		}
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		if ((config != null) && (!config.isEmpty())) {
			List<Integer> cfSetupRegs = new ArrayList<Integer>();
			byte[] results = null;
			int shift = 0;
			for (Integer n : config.keySet()) {
				if (n == MBREG_COMMAND_CODE) {
					int val = Integer.parseInt(config.get(n));
					byte[] data = ModbusUtil.unsignedShortToRegister(val);
					results = modbus.writeSingleRegister(modbusid, n, data);
					if (results != null) {
						registers.add(n);
					};
					break;
				}
				if (n < OFFSET_STATUS_REG1 * 100) {
					shift = n;
					cfData[OFFSET_STATUS_REG0] = cfData[OFFSET_STATUS_REG0] & (Integer.parseInt(config.get(n)) << shift);

				} else if (n < OFFSET_STATUS_REG3 * 100) {
					shift = n - (OFFSET_STATUS_REG1 * 100);
					cfData[OFFSET_STATUS_REG1] = cfData[OFFSET_STATUS_REG1] & (Integer.parseInt(config.get(n)) << shift);
				} else {
					shift = n - (OFFSET_STATUS_REG3 * 100);
					cfData[OFFSET_STATUS_REG3] = cfData[OFFSET_STATUS_REG3] & (Integer.parseInt(config.get(n)) << shift);
				}
				cfSetupRegs.add(n);
			}

			if (!cfSetupRegs.isEmpty()) {
				byte[] data = new byte[MBREG_STATUS_NUM * 2];
				for (int i = 0; i < MBREG_STATUS_NUM; i++) {
					byte[] temp = ModbusUtil.unsignedShortToRegister(cfData[i]);
					data[2 * i] = temp[0];
					data[2 * i + 1] = temp[1];
				}
				results = modbus.writeMultipleRegisters(modbusid, MBREG_STATUS_START, data);
				if (results != null) {
					registers.addAll(cfSetupRegs);
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
				inputVoltageL1N = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 0);
				inputVoltageL2N = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 1);
				inputVoltageL3N = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 2);
				inputCurrentL1 = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 3) * CURRENT_SCALAR;
				inputCurrentL2 = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 4) * CURRENT_SCALAR;
				inputCurrentL3 = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 5) * CURRENT_SCALAR;
				inputFrequency = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 6) * FREQUENCY_SCALAR;

				bypassMainsVoltageL1N = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 10);
				bypassMainsVoltageL2N = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 11);
				bypassMainsVoltageL3N = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 12);
				bypassFrequency = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 13) * FREQUENCY_SCALAR;
				outputStarVoltageL1N = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 14);
				outputStarVoltageL2N = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 15);
				outputStarVoltageL3N = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 16);

				outputCurrentL1 = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 20) * CURRENT_SCALAR;
				outputCurrentL2 = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 21) * CURRENT_SCALAR;
				outputCurrentL3 = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 22) * CURRENT_SCALAR;
				outputPeakCurrentL1 = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 23) * CURRENT_SCALAR;
				outputPeakCurrentL2 = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 24) * CURRENT_SCALAR;
				outputPeakCurrentL3 = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 25) * CURRENT_SCALAR;
				loadPhaseL1 = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 26);
				loadPhaseL2 = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 27);
				loadPhaseL3 = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 28);
				outputActivePowerL1 = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 29) * POWER_SCALAR;
				outputActivePowerL2 = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 30) * POWER_SCALAR;
				outputActivePowerL3 = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 31) * POWER_SCALAR;
				outputFrequency = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 32) * FREQUENCY_SCALAR;

				batteryVoltage = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 36) * BATTERY_VOLTAGE_SCALAR;
				positiveBatteryVoltage = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 37) * BATTERY_VOLTAGE_SCALAR;
				negativeBatteryVoltage = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 38) * BATTERY_VOLTAGE_SCALAR;
				batteryCurrent = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 39) * BATTERY_CURRENT_SCALAR;
				remainingBatteryCapacity = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 40);
				remainingBackupTimeMins = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 42);
				totalOutputEnergy = ModbusUtil.registersMEToInt(data, OFFSET_DATA + 2 * 47) * TOTAL_ENERGY_SCALAR;
				internalUpsTemperature = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 50);
				sensor1Temperature = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 51);
				sensor2Temperature = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * 52);
				return true;
			}

			if (idx == 1) {
				int reg1 = ModbusUtil.registerToShort(data, OFFSET_DATA); 
				testingInProgress = (reg1 & 0x2) != 0 ? "1" : "0";
				
				shutdownActive = (reg1 & 0x8) != 0 ? "1" : "0";
				
				batteryCharged = (reg1 & 0x20) != 0 ? "1" : "0";
				batteryCharging = (reg1 & 0x40) != 0 ? "1" : "0";
				bypassBad = (reg1 & 0x80) != 0 ? "1" : "0";
				
				normalOperation = (reg1 & 0x200) != 0 ? "1" : "0";
				
				onBypass = (reg1 & 0x800) != 0 ? "1" : "0";
				batteryLow = (reg1 & 0x1000) != 0 ? "1" : "0";
				batteryWorking = (reg1 & 0x2000) != 0 ? "1" : "0";
				upsLocked = (reg1 & 0x4000) != 0 ? "1" : "0";
				outputPowered = (reg1 & 0x8000) != 0 ? "1" : "0";
				
				int reg2 = ModbusUtil.registerToShort(data, OFFSET_DATA + 2);
				inputMainsPresent = (reg2 & 0x1000) != 0 ? "1" : "0";
				alarmTemperature = (reg2 & 0x2000) != 0 ? "1" : "0";
				alarmOverload = (reg2 & 0x4000) != 0 ? "1" : "0";
				upsFailure = (reg2 & 0x8000) != 0 ? "1" : "0";
				
				// reg3 is padding
				
				int reg4 = ModbusUtil.registerToShort(data, OFFSET_DATA + 6);
				lostCommsWithUps = (reg4 & 0x8000) != 0 ? "1" : "0";
				return true;
			}
		}

		if (mode == CONFIG_MODE) {
			for (int i = 0; i < MBREG_STATUS_NUM; i++) {
				cfData[i] = ModbusUtil.registerToShort(data, OFFSET_DATA + 2 * i);
			}
			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		if ((prevTotalOutputEnergy == 0) || (prevTotalOutputEnergy > totalOutputEnergy)) {
			activeEnergy = 0.0f;
		} else {
			activeEnergy = totalOutputEnergy - prevTotalOutputEnergy;
		}
		prevTotalOutputEnergy = totalOutputEnergy;
	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
//		data.append("|DEVICEID=" + getId() + "-0" + "-0");
//		data.append(";timestamp=" + timestamp);
//
//		data.append(";Input Voltage L1-N=" + vformat.format(inputVoltageL1N) + ",V");
//		data.append(";Input Voltage L2-N=" + vformat.format(inputVoltageL2N) + ",V");
//		data.append(";Input Voltage L3-N=" + vformat.format(inputVoltageL3N) + ",V");
//		data.append(";Input Current L1=" + vformat.format(inputCurrentL1) + ",A");
//		data.append(";Input Current L2=" + vformat.format(inputCurrentL2) + ",A");
//		data.append(";Input Current L3=" + vformat.format(inputCurrentL3) + ",A");
//		data.append(";Input Frequency=" + vformat.format(inputFrequency) + ",Hz");
//		// Bypass Measures
//		data.append(";Bypass Voltage L1-N=" + vformat.format(bypassMainsVoltageL1N) + ",V");
//		data.append(";Bypass Voltage L2-N=" + vformat.format(bypassMainsVoltageL2N) + ",V");
//		data.append(";Bypass Voltage L3-N=" + vformat.format(bypassMainsVoltageL3N) + ",V");
//		data.append(";Bypass Frequency=" + vformat.format(bypassFrequency) + ",Hz");
//		// Output Measures
//		data.append(";Output Voltage L1-N=" + vformat.format(outputStarVoltageL1N) + ",V");
//		data.append(";Output Voltage L2-N=" + vformat.format(outputStarVoltageL2N) + ",V");
//		data.append(";Output Voltage L3-N=" + vformat.format(outputStarVoltageL3N) + ",V");
//		data.append(";Output Current L1=" + vformat.format(outputCurrentL1) + ",A");
//		data.append(";Output Current L2=" + vformat.format(outputCurrentL2) + ",A");
//		data.append(";Output Current L3=" + vformat.format(outputCurrentL3) + ",A");
//		data.append(";Output Peak Current L1=" + vformat.format(outputPeakCurrentL1) + ",A");
//		data.append(";Output Peak Current L2=" + vformat.format(outputPeakCurrentL2) + ",A");
//		data.append(";Output Peak Current L3=" + vformat.format(outputPeakCurrentL3) + ",A");
//		data.append(";Load Phase L1=" + loadPhaseL1 + ",%");
//		data.append(";Load Phase L2=" + loadPhaseL2 + ",%");
//		data.append(";Load Phase L3=" + loadPhaseL3 + ",%");
//		data.append(";Output Active Power L1=" + vformat.format(outputActivePowerL1) + ",kW");
//		data.append(";Output Active Power L2=" + vformat.format(outputActivePowerL2) + ",kW");
//		data.append(";Output Active Power L3=" + vformat.format(outputActivePowerL3) + ",kW");
//		data.append(";Output Frequency=" + vformat.format(outputFrequency) + ",Hz");
//		// Battery Measures
//		data.append(";Battery Voltage=" + vformat.format(batteryVoltage) + ",V");
//		data.append(";Positive Battery Voltage=" + vformat.format(positiveBatteryVoltage) + ",V");
//		data.append(";Negative Battery Voltage=" + vformat.format(negativeBatteryVoltage) + ",V");
//		data.append(";Battery Current=" + vformat.format(batteryCurrent) + ",A");
//		data.append(";Remain Battery Capacity=" + remainingBatteryCapacity + ",%");
//		data.append(";Remain Battery Time=" + remainingBackupTimeMins + ",min");
//		data.append(";Total Output Energy=" + vformat.format(totalOutputEnergy) + ",kWh");
//		data.append(";Internal UPS Temperature=" + vformat.format(internalUpsTemperature) + ",C");
//		data.append(";Sensor 1 Temperature=" + vformat.format(sensor1Temperature) + ",C");
//		data.append(";Sensor 2 Temperature=" + vformat.format(sensor2Temperature) + ",C");
//		data.append(";Total Output Active Power=" + vformat.format(outputActivePowerL1 + outputActivePowerL2 + outputActivePowerL3) + ",kW");
//		data.append(";Average Load Phase=" + vformat.format((loadPhaseL1 + loadPhaseL2 + loadPhaseL3) / 3.0f) + ",%");

		// data.append(";Active Energy" + "=" + vformat.format(activeEnergy*1000) + ",Wh");
		
		data.append("|DEVICEID=" + getId() + "-0" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Voltage" + "=" + vformat.format(inputVoltageL1N) + ",V");

		data.append("|DEVICEID=" + getId() + "-1" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Voltage" + "=" + vformat.format(inputVoltageL2N) + ",V");

		data.append("|DEVICEID=" + getId() + "-2" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Voltage" + "=" + vformat.format(inputVoltageL3N) + ",V");

		data.append("|DEVICEID=" + getId() + "-3" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Current" + "=" + vformat.format(inputCurrentL1) + ",A");

		data.append("|DEVICEID=" + getId() + "-4" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Current" + "=" + vformat.format(inputCurrentL2) + ",A");

		data.append("|DEVICEID=" + getId() + "-5" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Current" + "=" + vformat.format(inputCurrentL3) + ",A");

		data.append("|DEVICEID=" + getId() + "-6" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Frequency" + "=" + vformat.format(inputFrequency) + ",Hz");

		data.append("|DEVICEID=" + getId() + "-7" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Voltage" + "=" + vformat.format(bypassMainsVoltageL1N) + ",V");

		data.append("|DEVICEID=" + getId() + "-8" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Voltage" + "=" + vformat.format(bypassMainsVoltageL2N) + ",V");

		data.append("|DEVICEID=" + getId() + "-9" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Voltage" + "=" + vformat.format(bypassMainsVoltageL3N) + ",V");

		data.append("|DEVICEID=" + getId() + "-10" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Frequency" + "=" + vformat.format(bypassMainsVoltageL1N) + ",Hz");

		data.append("|DEVICEID=" + getId() + "-11" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Voltage" + "=" + vformat.format(outputStarVoltageL1N) + ",V");

		data.append("|DEVICEID=" + getId() + "-12" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Voltage" + "=" + vformat.format(outputStarVoltageL2N) + ",V");

		data.append("|DEVICEID=" + getId() + "-13" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Voltage" + "=" + vformat.format(outputStarVoltageL3N) + ",V");

		data.append("|DEVICEID=" + getId() + "-14" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Current" + "=" + vformat.format(outputCurrentL1) + ",A");

		data.append("|DEVICEID=" + getId() + "-15" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Current" + "=" + vformat.format(outputCurrentL2) + ",A");

		data.append("|DEVICEID=" + getId() + "-16" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Current" + "=" + vformat.format(outputCurrentL3) + ",A");

		data.append("|DEVICEID=" + getId() + "-17" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Peak Current" + "=" + vformat.format(outputPeakCurrentL1) + ",A");

		data.append("|DEVICEID=" + getId() + "-18" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Peak Current" + "=" + vformat.format(outputPeakCurrentL2) + ",A");

		data.append("|DEVICEID=" + getId() + "-19" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Peak Current" + "=" + vformat.format(outputPeakCurrentL3) + ",A");

		data.append("|DEVICEID=" + getId() + "-20" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Load Phase" + "=" + Integer.toString(loadPhaseL1) + ",%");

		data.append("|DEVICEID=" + getId() + "-21" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Load Phase" + "=" + Integer.toString(loadPhaseL2) + ",%");

		data.append("|DEVICEID=" + getId() + "-22" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Load Phase" + "=" + Integer.toString(loadPhaseL3) + ",%");

		data.append("|DEVICEID=" + getId() + "-23" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Active Power" + "=" + vformat.format(outputActivePowerL1) + ",kW");

		data.append("|DEVICEID=" + getId() + "-24" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Active Power" + "=" + vformat.format(outputActivePowerL2) + ",kW");

		data.append("|DEVICEID=" + getId() + "-25" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Active Power" + "=" + vformat.format(outputActivePowerL3) + ",kW");

		data.append("|DEVICEID=" + getId() + "-26" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Frequency" + "=" + vformat.format(outputFrequency) + ",Hz");

		data.append("|DEVICEID=" + getId() + "-27" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Voltage" + "=" + vformat.format(batteryVoltage) + ",V");

		data.append("|DEVICEID=" + getId() + "-28" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Voltage" + "=" + vformat.format(positiveBatteryVoltage) + ",V");

		data.append("|DEVICEID=" + getId() + "-29" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Voltage" + "=" + vformat.format(negativeBatteryVoltage) + ",V");

		data.append("|DEVICEID=" + getId() + "-30" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Current" + "=" + vformat.format(batteryCurrent) + ",A");

		data.append("|DEVICEID=" + getId() + "-31" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Battery Percentage" + "=" + Integer.toString(remainingBatteryCapacity) + ",%");

		data.append("|DEVICEID=" + getId() + "-32" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Battery Time" + "=" + Integer.toString(remainingBackupTimeMins) + ",min");

		data.append("|DEVICEID=" + getId() + "-33" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Active Energy Reading" + "=" + vformat.format(totalOutputEnergy) + ",kWh");
		data.append(";Active Energy" + "=" + vformat.format(activeEnergy * 1000) + ",Wh");

		data.append("|DEVICEID=" + getId() + "-34" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Temperature" + "=" + vformat.format(internalUpsTemperature) + ",C");

		data.append("|DEVICEID=" + getId() + "-35" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Temperature" + "=" + vformat.format(sensor1Temperature) + ",C");

		data.append("|DEVICEID=" + getId() + "-36" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Temperature" + "=" + vformat.format(sensor2Temperature) + ",C");

		data.append("|DEVICEID=" + getId() + "-37" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Active Power" + "=" + vformat.format(outputActivePowerL1 + outputActivePowerL2 + outputActivePowerL3) + ",kW");

		data.append("|DEVICEID=" + getId() + "-38" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Load Phase" + "=" + Integer.toString((loadPhaseL1 + loadPhaseL2 + loadPhaseL3) / 3) + ",%");
		
		data.append("|DEVICEID=" + getId() + "-45" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Alarm Status" + "=" + lostCommsWithUps + ",None");
		
		data.append("|DEVICEID=" + getId() + "-47" + "-0");
		data.append(";timestamp=" + timestamp);
		data.append(";Alarm Status" + "=" + alarmOverload + ",None");

		return data.toString();

	}

}
