package com.greenkoncepts.gateway.adapter.siemens;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.FuncUtil;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class SitransMag6000 extends SiemensDevice {
	public static final int MBREG_DATA_START = 3002;//3000-3024
	public static final int MBREG_DATA_NUM = 25;
	public static final int posFAbsoluteVolumeflow = 0;
	public static final int posDTotalizer1 = 12;
	public static final int posDTotalizer2 = 16;
	public static final int posFTotalizer1 = 20;
	public static final int posFTotalizer2 = 22;
	
	public static final int MBREG_CONFIG1_START = 528;//528-530
	public static final int MBREG_CONFIG1_NUM = 3;
	
	public static final int MBREG_CONFIG2_START = 2101;//2101,2103
	public static final int MBREG_CONFIG2_NUM = 3;
	
	public static final int MBREG_CONFIG3_START = 2900;//2900-2915
	public static final int MBREG_CONFIG3_NUM = 16;
	
	private float fAbsoluteVolumeflow = 0;
	private double preDTotalizer1 = 0;
	private double dTotalizer1 = 0;
	private double dTotalizer2 = 0;
	private float fTotalizer1 = 0;
	private float fTotalizer2 = 0;
	private double dconsumption = 0;
	
	private short[] config1 = new short[MBREG_CONFIG1_NUM];
	private short[] config2 = new short[MBREG_CONFIG2_NUM];
	private short[] config3 = new short[MBREG_CONFIG3_NUM];

	public SitransMag6000(int addr, String category) {
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
	public Map<Integer, String> getDeviceConfig() {
		device_config.clear();
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG1_START, MBREG_CONFIG1_NUM);
		if (decodingData(0, data, CONFIG_MODE)) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG2_START, MBREG_CONFIG2_NUM);
			if (decodingData(1, data, CONFIG_MODE)) {
				data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG3_START, MBREG_CONFIG3_NUM);
				if (decodingData(2, data, CONFIG_MODE)) {
					for (int i = 0; i < MBREG_CONFIG1_NUM; i++) {
						device_config.put(MBREG_CONFIG1_START + i, String.valueOf(config1[i]));
					}
					
					for (int i = 0; i < MBREG_CONFIG2_NUM; i++) {
						device_config.put(MBREG_CONFIG2_START + i, String.valueOf(config2[i]));
					}
					
					for (int i = 0; i < MBREG_CONFIG3_NUM; i++) {
						device_config.put(MBREG_CONFIG3_START + i, String.valueOf(config3[i]));
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
			byte[] data = null;
			byte[] results = null;
			List<Integer> config1Regs = new ArrayList<Integer>();
			List<Integer> config2Regs = new ArrayList<Integer>();
			List<Integer> config3Regs = new ArrayList<Integer>();
			
			for (Integer n : config.keySet()) {
				if (n < MBREG_CONFIG2_START) {
					config1[n - MBREG_CONFIG1_START] = Short.parseShort(config.get(n));
					config1Regs.add(n);
				} else if (n < MBREG_CONFIG3_START ) {
					config2[n - MBREG_CONFIG2_START] =  Short.parseShort(config.get(n));
					config2Regs.add(n);
				} else {
					config3[n - MBREG_CONFIG3_START] = Short.parseShort(config.get(n));
					config3Regs.add(n);
				}

			}
			
			if (!config1Regs.isEmpty()) {
				data = new byte[MBREG_CONFIG1_NUM * 2];
				for (int i = 0; i < MBREG_CONFIG1_NUM; i++) {
					byte[] temp = ModbusUtil.unsignedShortToRegister(config1[i]);
					data[2 * i] = temp[0];
					data[2 * i + 1] = temp[1];
				}
				results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG1_START, data);
				if (results != null) {
					registers.addAll(config1Regs);
				}
			}

			if (!config2Regs.isEmpty()) {
				data = new byte[MBREG_CONFIG2_NUM * 2];
				for (int i = 0; i < MBREG_CONFIG2_NUM; i++) {
					byte[] temp = ModbusUtil.unsignedShortToRegister(config2[i]);
					data[2 * i] = temp[0];
					data[2 * i + 1] = temp[1];
				}
				results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG2_START, data);
				if (results != null) {
					registers.addAll(config2Regs);
				}
			}
			
			if (!config3Regs.isEmpty()) {
				data = new byte[MBREG_CONFIG3_NUM * 2];
				for (int i = 0; i < MBREG_CONFIG3_NUM; i++) {
					byte[] temp = ModbusUtil.unsignedShortToRegister(config3[i]);
					data[2 * i] = temp[0];
					data[2 * i + 1] = temp[1];
				}
				results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG3_START, data);
				if (results != null) {
					registers.addAll(config3Regs);
				}
			}	
		}
		return registers;
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
			if (decodingData(0, data, DATA_MODE)) {
				calculateDecodedData() ;
			}
		}
		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			item.put("fAbsoluteVolumeflow", vformat.format(fAbsoluteVolumeflow));
			item.put("dTotalizer1", vformat.format(dTotalizer1));
			item.put("dTotalizer2", vformat.format(dTotalizer2));
			item.put("fTotalizer1", vformat.format(fTotalizer1));
			item.put("fTotalizer2", vformat.format(fTotalizer2));
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
			fAbsoluteVolumeflow = FuncUtil.registersToIEEE754FloatHighFirst(data, OFFSET_DATA+ posFAbsoluteVolumeflow * 2);
			dTotalizer1 = FuncUtil.registersToDouble(data, OFFSET_DATA + posDTotalizer1 * 2);
			dTotalizer2 = FuncUtil.registersToDouble(data, OFFSET_DATA + posDTotalizer2 * 2);
			fTotalizer1 = FuncUtil.registersToIEEE754FloatHighFirst(data, OFFSET_DATA + posFTotalizer1 * 2);
			fTotalizer2 = FuncUtil.registersToIEEE754FloatHighFirst(data, OFFSET_DATA + posFTotalizer2 * 2);
			return true;
		}

		if (mode == CONFIG_MODE) {
			if (idx == 0) {
				for (int i = 0; i < MBREG_CONFIG1_NUM; i++) {
					config1[i] = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + i*2);
				}
			} else if (idx == 1) {
				config2[0] = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*0);
				config2[2] = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*2);
			} else if (idx == 2) {
				for (int i = 0; i < MBREG_CONFIG3_NUM; i++) {
					if ((i == 4) || (i == 5) || (i == 8) || (i == 9) || (i == 10) || (i == 11)) {
						continue;
					}
					config3[i] = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + i*2);
				}
			}
			return true;
		}

		return true;
	}
	
	private void calculateDecodedData() {
		if ((preDTotalizer1 == 0) || (preDTotalizer1 > dTotalizer1)) {
			dconsumption = 0.0;
		} else {
			dconsumption = dTotalizer1 - preDTotalizer1;
		}
		preDTotalizer1 = dTotalizer1;
		fAbsoluteVolumeflow = fAbsoluteVolumeflow*1000;
	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		data.append("|DEVICEID=" + getId() + "-" + 0 + "-" + 0);
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Flow Rate=" + vformat.format(fAbsoluteVolumeflow) + ",l/s");
		data.append(";Water Volume Reading=" + vformat.format(dTotalizer1) + ",cu m");
		data.append(";Water Volume=" + vformat.format(dconsumption) + ",cu m");

		return data.toString();

	}
	

}
