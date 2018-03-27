package com.greenkoncepts.gateway.adapter.emerson;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

// For CRAC
public class LiebertCRV extends EmersonDevice{    
	public static int MBREG_DATA1_START = 384;
	public static int MBREG_DATA1_NUM = 91;
	
	public static int posSupplyAirTemperature = 0;
	public static int posReturnAirTemperature = 2;
	public static int posAirTemperatureSetPoint = 12;
	public static int posReturnHumidity = 27;
	public static int posHumiditySetPoint = 28;
	public static int posSupplyChilledWaterTemperature = 59;
	public static int posSystemStatus = 89;
	public static int posSystemOperatingState = 90;
	

	
//	public static int MBREG_DATA2_START = 569;
//	public static int MBREG_DATA2_NUM = 13;
//
//	public static int posSystemInputRMSAB = 0;
//	public static int posSystemInputRMSAN = 1;
//	public static int posSystemInputRMSCurrentA = 2;
//	public static int posSystemInputRMSBC = 3;
//	public static int posSystemInputRMSBN = 4;
//	public static int posSystemInputRMSCurrentB = 5;
//	public static int posSystemInputRMSCA = 6;
//	public static int posSystemInputRMSCN = 7;
//	public static int posSystemInputRMSCurrentC = 8;
//	public static int posEnergyConsumption = 9;
//	public static int posInstantaneousPower = 11;	
	
	public static float dataScalar = 0.1f;
	
	
	private int supplyAirTemperature = 0;
	private int returnAirTemperature = 0;
	private int airTemperatureSetPoint = 0;
	private int returnHumidity = 0;
	private int humiditySetPoint = 0;
	private int supplyChilledWaterTemperature = 0;
	private int systemStatus = 0;
	private int systemOperatingState = 0;
	private int operationTime = 0;



	
//	private int systemInputRMSAB = 0;
//	private int systemInputRMSAN = 0;
//	private int systemInputRMSCurrentA = 0;
//	private int systemInputRMSBC = 0;
//	private int systemInputRMSBN = 0;
//	private int systemInputRMSCurrentB = 0;
//	private int systemInputRMSCA = 0;
//	private int systemInputRMSCN = 0;
//	private int systemInputRMSCurrentC = 0;
//	private long energyConsumption = 0;
//	private long instantaneousPower = 0;
	
	public LiebertCRV(int addr, String category) {
		super(category, addr);
	}
	
	@Override
	public String getDeviceData() {
		byte[] data = modbus.readInputRegisters(modbusid, MBREG_DATA1_START, MBREG_DATA1_NUM);
		if (decodingData(0, data, DATA_MODE)) {
			calculateDecodedData();
		}
		return createDataSendToServer();
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			byte[] data = modbus.readInputRegisters(modbusid, MBREG_DATA1_START, MBREG_DATA1_NUM);
			decodingData(0, data, DATA_MODE);
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			item.put("supplyAirTemperature", vformat.format((supplyAirTemperature * dataScalar)));
			item.put("returnAirTemperature", vformat.format((returnAirTemperature * dataScalar)));
			item.put("airTemperatureSetPoint", vformat.format((airTemperatureSetPoint * dataScalar)));
			item.put("returnHumidity", vformat.format((returnHumidity * dataScalar)));
			item.put("humiditySetPoint", vformat.format((humiditySetPoint)));
			item.put("supplyChilledWaterTemperature", vformat.format((supplyChilledWaterTemperature * dataScalar)));
			item.put("systemStatus", vformat.format((systemStatus)));
			item.put("systemOperatingState", vformat.format((systemOperatingState)));
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
			supplyAirTemperature = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posSupplyAirTemperature);
			returnAirTemperature = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posReturnAirTemperature);
			airTemperatureSetPoint = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posAirTemperatureSetPoint);
			returnHumidity = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posReturnHumidity);
			humiditySetPoint = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posHumiditySetPoint);
			supplyChilledWaterTemperature = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posSupplyChilledWaterTemperature);
			systemStatus = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posSystemStatus);
			systemOperatingState = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posSystemOperatingState);
			
			if (systemOperatingState == 1) {
				operationTime = 1;
			} else {
				operationTime = 0;
			}
			return true;
		}
		if (mode == CONFIG_MODE) {
			return true;
		}

		return true;
	}
	
	private void calculateDecodedData() {
	}
	
	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		
		data.append("|DEVICEID=" + getId() + "-0-0");
		
		data.append(";TIMESTAMP=" + timestamp);
		
		data.append(";Operation Time=" + operationTime + ",minute");

		data.append(";Supply Air Temp=" + vformat.format(supplyAirTemperature * dataScalar) + ",C");

		data.append(";Return Air Temp=" + vformat.format(returnAirTemperature * dataScalar) + ",C");

		data.append(";Temperature Set point=" + vformat.format(airTemperatureSetPoint * dataScalar) + ",C");

		data.append(";Humidity=" + vformat.format(returnHumidity * dataScalar) + ",%");

		data.append(";Humidity Set point=" + vformat.format(humiditySetPoint) + ",%");

		data.append(";CH Supply Temp=" + vformat.format(supplyChilledWaterTemperature * dataScalar) + ",C");

		data.append(";Equipment Status=" + systemStatus + ",None");
		
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
