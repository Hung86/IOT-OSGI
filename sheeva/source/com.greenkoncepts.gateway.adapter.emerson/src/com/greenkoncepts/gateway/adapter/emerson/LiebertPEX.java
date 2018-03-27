package com.greenkoncepts.gateway.adapter.emerson;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

//For CRAC
public class LiebertPEX extends EmersonDevice {
	public static int MBREG_DATA1_START = 22;
	public static int MBREG_DATA1_NUM = 5;

	public static int posTemperatureSetpoint = 0;
	public static int posHumiditySetpoint = 4;

	public static int MBREG_DATA2_START = 99;
	public static int MBREG_DATA2_NUM = 32;

	public static int posOperatingState = 0;
	public static int posReturnTemperature = 10;
	public static int posSupplyTemperature = 12;

	public static int MBREG_DATA3_START = 129;
	public static int MBREG_DATA3_NUM = 1;

	public static int posReturnHumidity = 0;

	public static float dataScalar = 0.1f;

	private int temperatureSetPoint = 0;
	private int humiditySetPoint = 0;
	private int operatingState = 0;
	private int returnTemperature = 0;
	private int supplyTemperature = 0;
	private int returnHumidity = 0;
	private int operationTime = 0;

	public LiebertPEX(int addr, String category) {
		super(category, addr);
	}

	@Override
	public String getDeviceData() {
		byte[] data = modbus.readInputRegisters(modbusid, MBREG_DATA1_START, MBREG_DATA1_NUM);
		if (decodingData(0, data, DATA_MODE)) {
			data = modbus.readInputRegisters(modbusid, MBREG_DATA2_START, MBREG_DATA2_NUM);
			if (decodingData(1, data, DATA_MODE)) {
				data = modbus.readInputRegisters(modbusid, MBREG_DATA3_START, MBREG_DATA3_NUM);
				if (decodingData(2, data, DATA_MODE)) {
					calculateDecodedData();
				}
			}
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
			item.put("temperatureSetPoint", vformat.format(temperatureSetPoint * dataScalar));
			item.put("humiditySetPoint", vformat.format(humiditySetPoint));
			item.put("supplyTemperature", vformat.format(supplyTemperature * dataScalar));
			item.put("returnTemperature", vformat.format(returnTemperature * dataScalar));
			item.put("returnHumidity", vformat.format(returnHumidity));
			item.put("operatingState", String.valueOf(operatingState));
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
			if (idx == 0) {
				temperatureSetPoint = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posTemperatureSetpoint);
				humiditySetPoint = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posHumiditySetpoint);
			} else if (idx == 1) {
				operatingState = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posOperatingState);
				returnTemperature = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posReturnTemperature);
				supplyTemperature = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posSupplyTemperature);

				if ((operatingState & 0x0001) == 1) {
					operationTime = 1;
				} else {
					operationTime = 0;
				}
			} else if (idx == 2) {
				returnHumidity = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * posReturnHumidity);
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

		data.append(";Temperature Set point=" + vformat.format(temperatureSetPoint * dataScalar) + ",C");

		data.append(";Humidity Set point=" + vformat.format(humiditySetPoint) + ",%");

		data.append(";CH Supply Temp=" + vformat.format(supplyTemperature * dataScalar) + ",C");

		data.append(";CH Return Temp=" + vformat.format(returnTemperature * dataScalar) + ",C");

		data.append(";Humidity=" + vformat.format(returnHumidity) + ",%");

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