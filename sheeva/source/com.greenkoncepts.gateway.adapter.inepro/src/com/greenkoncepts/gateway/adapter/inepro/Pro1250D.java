package com.greenkoncepts.gateway.adapter.inepro;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class Pro1250D extends IneproDevice{
	public static final int MREG_ADDRESS_CONFIG = 0x000F;
	public static final int MREG_BAUDRATE_CONFIG = 0xF800;
	
	public static final int MREG_DATA_START = 0x011E;
	public static final int MREG_DATA_NUM = 2;

	private long prevActiveEnergyReading = 0l;
	private long activeEnergyReading = 0l;
	private long activeEnergy = 0l;
	private int scaleEnergy = 1;
	
	public Pro1250D(int address, String category, int scale) {
		super(category, address);
		scaleEnergy = scale;

	}
	
	@Override
	public String getDeviceData() {
		byte[] data = modbus.readHoldingRegisters(modbusid, MREG_DATA_START, MREG_DATA_NUM);
		if (decodingData(0, data, DATA_MODE)) {
			calculateDecodedData();
		}
		return createDataSendToServer();
	}
	
	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			byte[] data = modbus.readHoldingRegisters(modbusid, MREG_DATA_START, MREG_DATA_START);
			decodingData(0, data, DATA_MODE);
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			item.put("activeEnergyReading" , vformat.format(((float)activeEnergyReading)/scaleEnergy));
			real_time_data.add(item);
		}
		return real_time_data;
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		device_config.clear();
		//device_config.put(MREG_ADDRESS_CONFIG, String.valueOf(modbusid));
		return device_config;
	}


	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		if ((config != null) && (!config.isEmpty())) { 
			for (Integer n : config.keySet()) {
			byte[] data = ModbusUtil.unsignedShortToRegister(Integer.parseInt(config.get(n)));
			byte[] results = modbus.writeMultipleRegisters(modbusid, n, data);
			if (results != null) {
				registers.add(n);
				modbusid = Integer.parseInt(config.get(n));
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
			activeEnergyReading = Math.abs(ModbusUtil.registersBEToLong(data, ModbusUtil.MB_RESP_DATA_POS));
			return true;
		}

		if (mode == CONFIG_MODE) {

			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		if (prevActiveEnergyReading == 0) {
			activeEnergy = 0;
		} else if (activeEnergyReading >= prevActiveEnergyReading ){
			activeEnergy = activeEnergyReading - prevActiveEnergyReading;
		} else {
			if((activeEnergyReading < 100) && ((MAX_INT - prevActiveEnergyReading) < 100)){
				activeEnergy = MAX_INT - prevActiveEnergyReading + activeEnergyReading;
			} else {
				activeEnergy = 0;
			}
		}
		prevActiveEnergyReading = activeEnergyReading;
		
	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		data.append("|DEVICEID=" + getId() + "-0-0");
		data.append(";TIMESTAMP=" + System.currentTimeMillis());
		data.append(";Generated Energy Reading="+ vformat.format(((float)activeEnergyReading)/scaleEnergy) + ",kWh");
		data.append(";Generated Energy="+ vformat.format((((float)activeEnergy)/scaleEnergy)*1000) + ",Wh");
		data.append(";Active Energy=" + vformat.format((((float)activeEnergy)/scaleEnergy)*(-1000)) + ",Wh");

		return data.toString();

	}
	


}
