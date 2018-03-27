package com.greenkoncepts.gateway.adapter.energetix.powermeter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class EcosailSCPMS6 extends EcosailDevice {
	final static int CHANNEL_NUM = 6;
	// ----- Device Control
	public static int MBREG_CHANNEL_RESET = 46010;
	public static int MBREG_FACTORY_RESET = 40099;
	public static int MBREG_SAVE_REBOOT = 40100;

	// ----- Device Configuration
	public static int MBREG_CONFIG_START = 40003;
	public static int MBREG_CONFIG_NUM = 2;

	public static int posAddress = 0;
	public static int posBaudrate = 1;
	private int[] cfData = new int[MBREG_CONFIG_NUM];

	// ----- CH Configuration
	public static int MBREG_CONFIG_CH_START = 46001;
	public static int MBREG_CONFIG_CH_NUM = 6;
	public static int posCTType = 0;
	public static int posCTRating = 1;
	public static int posDataScalar = 2;
	public static int posDemandWindow = 3;
	public static int posStatus = 4;
	public static int posSignedMode = 5;
	private int[] cfChannelData = new int[MBREG_CONFIG_CH_NUM];
	// ----- Metering Data
	public static int MBREG_DATA_START = 46011;
	public static int MBREG_DATA_NUM = 92;

	private short lineFrequency = 0;
	private short voltageRMS = 0;
	private short currentRMS[] = new short[CHANNEL_NUM];
	private long preActiveEnergyReadingSum[] = new long[CHANNEL_NUM]; // KWh
	private long activeEnergyReadingSum[] = new long[CHANNEL_NUM]; // KWh
	private long activeEnergySum[] = new long[CHANNEL_NUM];
	private long preReactiveEnergyReadingSum[] = new long[CHANNEL_NUM]; // kVARh
	private long reactiveEnergyReadingSum[] = new long[CHANNEL_NUM]; // kVARh
	private long reactiveEnergySum[] = new long[CHANNEL_NUM];
	private short powerFactor[] = new short[CHANNEL_NUM];
	private short apparentPower[] = new short[CHANNEL_NUM];

	private final int offsetLineFrequency = 2 * 0;
	private final int offsetVoltageRMS = 2 * 1;
	private final int offsetCurrentRMS[] = { 2 * 2, 2 * 3, 2 * 4, 2 * 5, 2 * 6, 2 * 7 };
	private final int offsetActiveEnergyReadingSum[] = { 2 * 8, 2 * 10, 2 * 12, 2 * 14, 2 * 16, 2 * 18 };
	private final int offsetReactiveEnergyReadingSum[] = { 2 * 44, 2 * 46, 2 * 48, 2 * 50, 2 * 52, 2 * 54 };
	private final int offsetPowerFactor[] = { 2 * 80, 2 * 81, 2 * 82, 2 * 83, 2 * 84, 2 * 85 };
	private final int offsetApparentPower[] = { 2 * 86, 2 * 87, 2 * 88, 2 * 89, 2 * 90, 2 * 91 };

	public EcosailSCPMS6(int addr, String category) {
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
			byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
			decodingData(0, data, DATA_MODE);
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			for (int j = 0; j < CHANNEL_NUM; j++) {
				Map<String, String> item = new Hashtable<String, String>();
				item.put("Hz_" + j, vformat.format((lineFrequency / 100.0)));
				item.put("V_" + j, vformat.format((voltageRMS / 10.0)));
				item.put("A_" + j, vformat.format((currentRMS[j] / 100.0)));
				item.put("kWh_S" + j, vformat.format((activeEnergyReadingSum[j] / 1000.0)));
				item.put("kWARh_S" + j, vformat.format((reactiveEnergyReadingSum[j] / 1000.0)));
				item.put("PF_" + j, vformat.format((powerFactor[j] / 100.0)));
				item.put("kVA_" + j, vformat.format((apparentPower[j] / 100.0)));
				real_time_data.add(item);
			}
		}
		return real_time_data;
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START, MBREG_CONFIG_NUM);
		device_config.clear();
		if (decodingData(0, data, CONFIG_MODE)) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_CH_START, MBREG_CONFIG_CH_NUM);
			if (decodingData(1, data, CONFIG_MODE)) {
				device_config.put(MBREG_CONFIG_START + posAddress, String.valueOf(cfData[posAddress]));
				device_config.put(MBREG_CONFIG_START + posBaudrate, String.valueOf(cfData[posBaudrate]));
				device_config.put(MBREG_CONFIG_CH_START + posCTType, String.valueOf(cfChannelData[posCTType]));
				device_config.put(MBREG_CONFIG_CH_START + posCTRating, String.valueOf(cfChannelData[posCTRating]));
				device_config.put(MBREG_CONFIG_CH_START + posDataScalar, String.valueOf(cfChannelData[posDataScalar]));
				device_config.put(MBREG_CONFIG_CH_START + posDemandWindow, String.valueOf(cfChannelData[posDemandWindow]));
				device_config.put(MBREG_CONFIG_CH_START + posStatus, String.valueOf(cfChannelData[posStatus]));
				device_config.put(MBREG_CONFIG_CH_START + posSignedMode, String.valueOf(cfChannelData[posSignedMode]));
			}
		}
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		if ((config != null) && (!config.isEmpty())) {
			byte[] data;
			byte[] results = null;
			if (config.containsKey(MBREG_CHANNEL_RESET)) {
				data = ModbusUtil.unsignedShortToRegister(Integer.parseInt(config.get(MBREG_CHANNEL_RESET)));
				results = modbus.writeMultipleRegisters(modbusid, MBREG_CHANNEL_RESET, data);
				if (results != null) {
					registers.addAll(config.keySet());
				}
			} else if (config.containsKey(MBREG_FACTORY_RESET)) {
				data = ModbusUtil.unsignedShortToRegister(Integer.parseInt(config.get(MBREG_FACTORY_RESET)));
				results = modbus.writeMultipleRegisters(modbusid, MBREG_FACTORY_RESET, data);
				if (results != null) {
					registers.addAll(config.keySet());
				}
			} else {
				List<Integer> cfDataRegs = new ArrayList<Integer>();
				List<Integer> cfChannelDataRegs = new ArrayList<Integer>();
				for (Integer n : config.keySet()) {
					if (n < MBREG_CONFIG_CH_START) {
						cfData[n - MBREG_CONFIG_START] = Integer.parseInt(config.get(n));
						cfDataRegs.add(n);
					} else {
						cfChannelData[n - MBREG_CONFIG_CH_START] = Integer.parseInt(config.get(n));
						cfChannelDataRegs.add(n);
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

				if (!cfChannelDataRegs.isEmpty()) {
					data = new byte[cfChannelData.length * 2];
					for (int i = 0; i < cfChannelData.length; i++) {
						byte[] temp = ModbusUtil.unsignedShortToRegister(cfChannelData[i]);
						data[2 * i] = temp[0];
						data[2 * i + 1] = temp[1];
					}
					results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG_CH_START, data);
					if (results != null) {
						registers.addAll(cfChannelDataRegs);
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
			lineFrequency = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + offsetLineFrequency);
			voltageRMS = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + offsetVoltageRMS);
			for (int i = 0; i < CHANNEL_NUM; i++) {
				currentRMS[i] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + offsetCurrentRMS[i]);
				activeEnergyReadingSum[i] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + offsetActiveEnergyReadingSum[i]);
				reactiveEnergyReadingSum[i] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + offsetReactiveEnergyReadingSum[i]);
				powerFactor[i] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + offsetPowerFactor[i]);
				apparentPower[i] = ModbusUtil.registerBEToSShort(data, OFFSET_DATA + offsetApparentPower[i]);
			}
			return true;
		}
		if (mode == CONFIG_MODE) {
			if (idx == 0) {
				cfData[posAddress] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posAddress);
				cfData[posBaudrate] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posBaudrate);
			} else if (idx == 1) {
				cfChannelData[posCTType] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posCTType);
				cfChannelData[posCTRating] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posCTRating);
				cfChannelData[posDataScalar] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posDataScalar);
				cfChannelData[posDemandWindow] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posDemandWindow);
				cfChannelData[posStatus] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posStatus);
				cfChannelData[posSignedMode] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posSignedMode);
			}
			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		for (int i = 0; i < CHANNEL_NUM; i++) {
			if ((preActiveEnergyReadingSum[i] == 0) || (preActiveEnergyReadingSum[i] > activeEnergyReadingSum[i])) {
				activeEnergySum[i] = 0;
			} else {
				activeEnergySum[i] = activeEnergyReadingSum[i] - preActiveEnergyReadingSum[i];
			}
			preActiveEnergyReadingSum[i] = activeEnergyReadingSum[i];

			if ((preReactiveEnergyReadingSum[i] == 0) || (preReactiveEnergyReadingSum[i] > reactiveEnergyReadingSum[i])) {
				reactiveEnergySum[i] = 0;
			} else {
				reactiveEnergySum[i] = reactiveEnergyReadingSum[i] - preReactiveEnergyReadingSum[i];
			}
			preReactiveEnergyReadingSum[i] = reactiveEnergyReadingSum[i];
		}
	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		for (int ch = 0; ch < CHANNEL_NUM; ch++) {
			data.append("|DEVICEID=" + getId() + "-" + ch + "-0");
			data.append(";TIMESTAMP=" + timestamp);
			data.append(";Frequency=" + (lineFrequency / 100.0) + ",Hz");
			data.append(";Voltage=" + (voltageRMS / 10.0) + ",V");
			data.append(";Current=" + (currentRMS[ch] / 100.0) + ",A");
			data.append(";Active Energy Reading=" + (activeEnergyReadingSum[ch] / 1000.0) + ",kWh");
			data.append(";Active Energy=" + activeEnergySum[ch] + ",Wh");
			data.append(";Reactive Energy Reading=" + (reactiveEnergyReadingSum[ch] / 1000.0) + ",kVARh");
			data.append(";Reactive Energy=" + reactiveEnergySum[ch] + ",VARh");
			data.append(";Power Factor=" + (powerFactor[ch] / 100.0) + ",None");
			data.append(";Apparent Power=" + (apparentPower[ch] / 100.0) + ",kVA");
		}
		return data.toString();
	}

}
