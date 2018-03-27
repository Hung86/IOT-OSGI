package com.greenkoncepts.gateway.adapter.ge.aquatrans;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class GeAquatransAT600 extends GeAquatransDevice {
	
	public static final int MBREG_DATA1_START = 0x2200;
	public static final int MBREG_DATA2_START = 0x2248;
	public static final int MBREG_DATA_NUM = 0x08;
	public static final int offVelocity = 0;
	public static final int offFlowRate = 2;
	public static final int offVolumeReading = 0;
	
	public static final int MBREG_CONFIG1_START = 0x1500;
	public static final int MBREG_CONFIG1_NUM = 0x08;

	public static final int MBREG_CONFIG2_START = 0x2000;
	public static final int MBREG_CONFIG2_NUM = 0x24;

	public static final int MBREG_CONFIG3_START = 0x2100;
	public static final int MBREG_CONFIG3_NUM = 0x10;

	
	private long[] cfConfig1 = new long[MBREG_CONFIG1_NUM/2];
	private float[] cfConfig2 = new float[MBREG_CONFIG2_NUM/2];
	private long[] cfConfig3 = new long[MBREG_CONFIG3_NUM/2];
	
	private float velocity = 0.0f;
	private float flow_rate = 0.0f;
	private float volume_reading = 0.0f;
	private float pre_volume_reading = 0.0f;
	private float volume = 0.0f;
		
	GeAquatransAT600(int addr, String category) {
		super(category,addr);
	}
	
	@Override
	public String getDeviceData() {
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA1_START, MBREG_DATA_NUM);
		if (decodingData(0, data, DATA_MODE)) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_DATA2_START, MBREG_DATA_NUM);
			if (decodingData(1, data, DATA_MODE)) {
				calculateDecodedData();	
			}
		}
		return createDataSendToServer();
	}
	
	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA1_START, MBREG_DATA_NUM);
			if (decodingData(0, data, DATA_MODE)) {
				data = modbus.readHoldingRegisters(modbusid, MBREG_DATA2_START, MBREG_DATA_NUM);
				decodingData(1, data, DATA_MODE);
			}
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			item.put("Velocity", Float.toString(velocity));
			item.put("Volumetric", Float.toString(flow_rate));
			item.put("Volume_Reading", Float.toString(volume_reading));
			real_time_data.add(item);
		}
		return real_time_data;
	}
	
	
	@Override
	public Map<Integer, String> getDeviceConfig() {
		device_config.clear();
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG1_START, MBREG_CONFIG1_NUM);
		if (decodingData(0, data, CONFIG_MODE)) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG2_START, MBREG_CONFIG2_NUM);
			if (decodingData(1, data, CONFIG_MODE)) {
				data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG3_START, MBREG_CONFIG3_NUM);
				if (decodingData(2, data, CONFIG_MODE)) {
					for (int i = 0; i < cfConfig1.length; i++) {
						device_config.put(MBREG_CONFIG1_START + 2*i, String.valueOf(cfConfig1[i]));
					}
					for (int k = 0; k < cfConfig2.length; k++) {
						device_config.put(MBREG_CONFIG2_START + 2*k, String.valueOf(cfConfig1[k]));
					}
					for (int m = 0; m < cfConfig3.length; m++) {
						device_config.put(MBREG_CONFIG3_START + 2*m, String.valueOf(cfConfig1[m]));
					}
				}
			}
		}
		return device_config;
	}
	
	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		if ((config != null) && (!config.isEmpty())) { 
			List<Integer> cfConfig1Regs = new ArrayList<Integer>();
			List<Integer> cfConfig2Regs = new ArrayList<Integer>();
			List<Integer> cfConfig3Regs = new ArrayList<Integer>();
			byte[] results = null;
			for (Integer n : config.keySet()) {
				if (n < MBREG_CONFIG2_START) {
					cfConfig1[n - MBREG_CONFIG1_START] = Long.parseLong(config.get(n));
					cfConfig1Regs.add(n);
				} else if (n < MBREG_CONFIG3_START) {
					cfConfig2[n - MBREG_CONFIG2_START] = Float.parseFloat(config.get(n));
					cfConfig2Regs.add(n);
				} else {
					cfConfig3[n - MBREG_CONFIG1_START] = Long.parseLong(config.get(n));
					cfConfig3Regs.add(n);
				}
				if (!cfConfig1Regs.isEmpty()) {
					byte[] data = new byte[cfConfig1.length * 4];
					for (int i = 0; i < cfConfig1.length; i++) {
						byte[] temp = ModbusUtil.uintToRegisters(cfConfig1[i]);
						data[4 * i] = temp[0];
						data[4 * i + 1] = temp[1];
						data[4 * i + 2] = temp[2];
						data[4 * i + 3] = temp[3];
					}
					results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG1_START, data);
					if (results != null) {
						registers.addAll(cfConfig1Regs);
					}
				}
				
				if (!cfConfig2Regs.isEmpty()) {
					byte[] data = new byte[cfConfig2.length * 4];
					for (int i = 0; i < cfConfig2.length; i++) {
						byte[] temp = ModbusUtil.floatToRegisters(cfConfig2[i]);
						data[4 * i] = temp[0];
						data[4 * i + 1] = temp[1];
						data[4 * i + 2] = temp[2];
						data[4 * i + 3] = temp[3];
					}
					results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG2_START, data);
					if (results != null) {
						registers.addAll(cfConfig2Regs);
					}
				}
				
				if (!cfConfig3Regs.isEmpty()) {
					byte[] data = new byte[cfConfig3.length * 4];
					for (int i = 0; i < cfConfig3.length; i++) {
						byte[] temp = ModbusUtil.uintToRegisters(cfConfig3[i]);
						data[4 * i] = temp[0];
						data[4 * i + 1] = temp[1];
						data[4 * i + 2] = temp[2];
						data[4 * i + 3] = temp[3];
					}
					results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG3_START, data);
					if (results != null) {
						registers.addAll(cfConfig3Regs);
					}
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
				velocity = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2*offVelocity);
				flow_rate = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 2*offFlowRate);
			} else if (idx == 1) {
				volume_reading = ModbusUtil.ieee754RegistersToFloat(data,OFFSET_DATA + 2*offVolumeReading);
			}
			return true;
		}

		if (mode == CONFIG_MODE) {
			if (idx == 0) {
				for (int i = 0; i < cfConfig1.length; i++) {
					cfConfig1[i] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 4 * i);
				}
			} else if (idx == 1) {
				for (int i = 0; i <  cfConfig2.length; i++) {
					cfConfig2[i] = ModbusUtil.ieee754RegistersToFloat(data, OFFSET_DATA + 4 * i);
				}
			}else if (idx == 2) {
				for (int i = 0; i <  cfConfig3.length; i++) {
					cfConfig3[i] = ModbusUtil.registersBEToLong(data, OFFSET_DATA + 4 * i);
				}
			}
			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		if ((pre_volume_reading == 0) || (pre_volume_reading > volume_reading)) {
			volume = 0.0f;
		} else {
			volume = volume_reading - pre_volume_reading;
		}
		pre_volume_reading = volume_reading;
	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		data.append("|DEVICEID=" + getId() + "-" + 0 + "-" + 0);
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Water Velocity=" + velocity + ",m/s");
		data.append(";Flow Rate=" + flow_rate + ",l/s");
		data.append(";Water Volume Reading=" + volume_reading + ",cu m");
		data.append(";Water Volume=" + volume + ",cu m");

		return data.toString();

	}

}
