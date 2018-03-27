package com.greenkoncepts.gateway.adapter.energetix.powermeter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class EcosailSCPMM12 extends EcosailDevice {

	public static int CHANNEL_NUM = 12;
	// ----- Device Configuration
	public static int MBREG_CONFIG_START = 40003;
	public static int MBREG_CONFIG_NUM = 2;
	public static int MBREG_FACTORY_RESET = 40099;
	public static int MBREG_SAVE_REBOOT = 40100;
	public static int posAddress = 0;
	public static int posBaudrate = 1;
	private int[] cfData = new int[MBREG_CONFIG_NUM];

	// ----- Device Data
	public static int MBREG_DATA_START = 44001;
	public static int MBREG_DATA_NUM = 80;

	private long preReading[] = new long[CHANNEL_NUM];
	private long reading[] = new long[CHANNEL_NUM]; // Wh
	private int energy[] = new int[CHANNEL_NUM]; // Wh
	private int power[] = new int[CHANNEL_NUM]; // W
	private int power_avg[] = new int[CHANNEL_NUM]; // W
	private int power_max[] = new int[CHANNEL_NUM]; // W

	private int offset1 = 2 * (44001 - 44001);
	private int offset2 = 2 * (44033 - 44001);
	private int offset3 = 2 * (44049 - 44001);
	private int offset4 = 2 * (44065 - 44001);

	public EcosailSCPMM12(int addr, String category) {
		super(category, addr);
		for (int i = 0; i < CHANNEL_NUM; i++) {
			reading[i] = 0;
			energy[i] = 0;
			power[i] = 0;
			power_avg[i] = 0;
			power_max[i] = 0;
		}
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
			for (int i = 0; i < CHANNEL_NUM; i++) {
				Map<String, String> item = new Hashtable<String, String>();
				item.put("kWh_" + i, vformat.format(reading[i] / 1000.0));
				item.put("kW_" + i, vformat.format(power[i] / 1000.0));
				item.put("kW_Avg_" + i, vformat.format(power_avg[i] / 1000.0));
				item.put("kW_Demand_" + i, vformat.format(power_max[i] / 1000.0));
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
			} else {
				for (Integer n : config.keySet()) {
					cfData[n - MBREG_CONFIG_START] = Integer.parseInt(config.get(n));
				}
				data = new byte[cfData.length * 2];
				for (int i = 0; i < cfData.length; i++) {
					byte[] temp = ModbusUtil.unsignedShortToRegister(cfData[i]);
					data[2 * i] = temp[0];
					data[2 * i + 1] = temp[1];
				}
				results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG_START, data);
			}
			if (results != null) {
				registers.addAll(config.keySet());
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
			for (int i = 0; i < CHANNEL_NUM; i++) {
				reading[i] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + offset1 + 4 * i);
				power[i] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + offset2 + 2 * i);
				power_avg[i] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + offset3 + 2 * i);
				power_max[i] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + offset4 + 2 * i);
			}
			return true;
		}
		if (mode == CONFIG_MODE) {
			cfData[posAddress] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posAddress);
			cfData[posBaudrate] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posBaudrate);
			return true;
		}

		return true;

	}

	private void calculateDecodedData() {
		for (int i = 0; i < CHANNEL_NUM; i++) {
			if ((preReading[i] == 0) || (preReading[i] > reading[i])) {
				energy[i] = 0;
			} else {
				energy[i] = (int) (reading[i] - preReading[i]);
			}
			preReading[i] = reading[i];
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
			data.append(";Active Energy Reading=" + (reading[ch] / 1000.0) + ",kWh");
			data.append(";Active Energy=" + energy[ch] + ",Wh");
			data.append(";Active Power=" + power[ch] / 1000.0 + ",kW");
			data.append(";Active Average Power=" + power_avg[ch] / 1000.0 + ",kW");
			data.append(";Peak Demand=" + power_max[ch] / 1000.0 + ",kW");
		}
		return data.toString();
	}
}
