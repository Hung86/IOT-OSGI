package com.greenkoncepts.gateway.adapter.contrec;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class Model212 extends ContrecDevice {
	public static final float KW_PER_RT = 3.5168525f;
	public static final int MBREG_CONFIG1_START = 0x0000;
	public static final int MBREG_CONFIG1_NUM = 24;
	public static final int MBREG_CONFIG2_START = 185;
	public static final int MBREG_CONFIG2_NUM = 5;

	public static final int MBREG_DATA_START = 0x0019;
	public static final int MBREG_DATA_NUM = 0x0022;

	private int[] cfData = new int[MBREG_CONFIG1_NUM];
	private int[] cfUnit = new int[MBREG_CONFIG2_NUM];

	private float prevCoolingEnergyConsumption = 0.0f;
	private float coolingEnergyConsumption= 0.0f;
	private float coolingConsumption = 0.0f;
	private float coolingLoadRT = 0.0f;
	private float coolingLoadRTh = 0.0f;
	private float flowRate = 0.0f;
	private float chilledWaterSupplyTemperature;
	private float chilledWaterReturnTemperature;
	private float temperatureDiff;
	private float coolingLoadKWC;
	
	private int operationTime = 0;

	public Model212(int addr, String category) {
		super(category, addr);
		coolingEnergyConsumption = 0f;
		coolingLoadRT = 0f;
		flowRate = 0f;
		chilledWaterSupplyTemperature = 0f;
		chilledWaterReturnTemperature = 0f;
		temperatureDiff = 0f;
		coolingLoadKWC = 0f;
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
			Map<String, String> item = new Hashtable<String, String>();
			item.put("coolingEnergyConsumption", vformat.format(coolingEnergyConsumption));
			item.put("coolingLoadRT", vformat.format(coolingLoadRT));
			item.put("coolingLoadKWC", vformat.format(coolingLoadKWC));
			item.put("flowRate", vformat.format(flowRate));
			item.put("supplyTemperature", vformat.format(chilledWaterSupplyTemperature));
			item.put("returnTemperature", vformat.format(chilledWaterReturnTemperature));
			item.put("temperatureDiff", vformat.format(temperatureDiff));
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
				for (int i = 0; i < 15; i++) {
					device_config.put(MBREG_CONFIG1_START + i + 1, String.valueOf(cfData[i]));
				}

				for (int i = 15; i < MBREG_CONFIG1_NUM; i = (i + 2)) {
					byte[] kwL1lowReg = ModbusUtil.unsignedShortToRegister(cfData[i]);
					byte[] kwL1highReg = ModbusUtil.unsignedShortToRegister(cfData[i + 1]);
					float result = ModbusUtil.ieee754RegistersToFloat(kwL1highReg, kwL1lowReg);
					device_config.put(MBREG_CONFIG1_START + i + 1, String.valueOf(result));
				}

				for (int i = 0; i < MBREG_CONFIG2_NUM; i++) {
					device_config.put(MBREG_CONFIG2_START + i + 1, String.valueOf(cfUnit[i]));
				}
			}

		}
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		if ((config != null) && (!config.isEmpty())) {
			byte[] data = new byte[MBREG_CONFIG1_NUM * 2];
			List<Integer> cfDataRegs = new ArrayList<Integer>();
			for (Integer n : config.keySet()) {
				if ((n >= 16) && (n <= 24)) {
					float value = Float.parseFloat(config.get(n));
					byte[] temp = ModbusUtil.floatToRegisters(value);
					data[2 * (n - 1)] = temp[0];
					data[2 * (n - 1) + 1] = temp[1];
					data[2 * (n - 1) + 2] = temp[2];
					data[2 * (n - 1) + 3] = temp[3];
					cfDataRegs.add(n);
				} else if (n < MBREG_CONFIG2_START) {
					cfData[n - 1] = Integer.parseInt(config.get(n));
					cfDataRegs.add(n);
				}
			}
			if (!cfDataRegs.isEmpty()) {
				for (int i = 0; i < MBREG_CONFIG1_NUM; i++) {
					byte[] temp = ModbusUtil.unsignedShortToRegister(cfData[i]);
					data[2 * i] = temp[0];
					data[2 * i + 1] = temp[1];
				}
				byte[] results = modbus.writeMultipleRegisters(modbusid, MBREG_CONFIG1_START, data);
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
			coolingEnergyConsumption = ModbusUtil.ieee754RegistersToFloatLowFirst(data, OFFSET_DATA + 0);
			coolingLoadKWC = ModbusUtil.ieee754RegistersToFloatLowFirst(data, OFFSET_DATA + 40);
			coolingLoadRT = coolingLoadKWC / Model212.KW_PER_RT;
			coolingLoadRTh = coolingLoadRT / 60;
			flowRate = ModbusUtil.ieee754RegistersToFloatLowFirst(data, OFFSET_DATA + 48) / 60;
			chilledWaterSupplyTemperature = ModbusUtil.ieee754RegistersToFloatLowFirst(data, OFFSET_DATA + 56);
			chilledWaterReturnTemperature = ModbusUtil.ieee754RegistersToFloatLowFirst(data, OFFSET_DATA + 60);
			temperatureDiff = ModbusUtil.ieee754RegistersToFloatLowFirst(data, OFFSET_DATA + 64);
			return true;
		}
		
		if (mode == CONFIG_MODE) {
			if (idx == 0) {
				for (int i = 0; i < MBREG_CONFIG1_NUM; i++) {
					cfData[i] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * i);
				}
			} else if (idx == 1) {

				for (int i = 0; i < MBREG_CONFIG2_NUM; i++) {
					cfUnit[i] = ModbusUtil.registerBEToShort(data, OFFSET_DATA + 2 * i);
				}
			}
			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		if ((prevCoolingEnergyConsumption == 0) || (prevCoolingEnergyConsumption > coolingEnergyConsumption)) {
			coolingConsumption = 0;
		} else {
			coolingConsumption = coolingEnergyConsumption - prevCoolingEnergyConsumption;
		}
		prevCoolingEnergyConsumption = coolingEnergyConsumption;
		
		if (flowRate <= 0) {
			operationTime = 0;
		} else {
			operationTime = 1;
		}
	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		sb.append("|DEVICEID=" + getId() + "-0-0");
		sb.append(";TIMESTAMP=" + timestamp);
		sb.append(";Operation Time=" + operationTime + ",minute");
		sb.append(";BTU Meter Reading" + "=" + vformat.format(coolingEnergyConsumption) + ",kWh");

		//sb.append("|DEVICEID=" + getId() + "-1-0");
		//sb.append(";TIMESTAMP=" + timestamp);
		//sb.append(";Operation Time=" + operationTime + ",minute");
		sb.append(";CH Water Consumption=" + vformat.format(coolingConsumption * 1000f) + ",Wh");

		//sb.append("|DEVICEID=" + getId() + "-2-0");
		//sb.append(";TIMESTAMP=" + timestamp);
		//sb.append(";Operation Time=" + operationTime + ",minute");
		sb.append(";Cooling Load" + "=" + vformat.format(coolingLoadRT) + ",RT");

		//sb.append("|DEVICEID=" + getId() + "-3-0");
		//sb.append(";TIMESTAMP=" + timestamp);
		//sb.append(";Operation Time=" + operationTime + ",minute");
		sb.append(";Flow Rate" + "=" + vformat.format(flowRate) + ",l/s");

		//sb.append("|DEVICEID=" + getId() + "-4-0");
		//sb.append(";TIMESTAMP=" + timestamp);
		//sb.append(";Operation Time=" + operationTime + ",minute");
		sb.append(";CH Supply Temp" + "=" + vformat.format(chilledWaterSupplyTemperature) + ",C");

		//sb.append("|DEVICEID=" + getId() + "-5-0");
		//sb.append(";TIMESTAMP=" + timestamp);
		//sb.append(";Operation Time=" + operationTime + ",minute");
		sb.append(";CH Return Temp" + "=" + vformat.format(chilledWaterReturnTemperature) + ",C");

		//sb.append("|DEVICEID=" + getId() + "-6-0");
		//sb.append(";TIMESTAMP=" + timestamp);
		//sb.append(";Operation Time=" + operationTime + ",minute");
		sb.append(";CH Temp Diff" + "=" + vformat.format(Math.abs(temperatureDiff)) + ",C");

		//sb.append("|DEVICEID=" + getId() + "-7-0");
		//sb.append(";TIMESTAMP=" + timestamp);
		//sb.append(";Operation Time=" + operationTime + ",minute");
		sb.append(";CH Water Consumption RTh=" + vformat.format(coolingLoadRTh) + ",RTh");

		//sb.append("|DEVICEID=" + getId() + "-8-0");
		//sb.append(";TIMESTAMP=" + timestamp);
		//sb.append(";Operation Time=" + operationTime + ",minute");
		sb.append(";Cooling Load kWc" + "=" + vformat.format(coolingLoadKWC) + ",kWc");
		return sb.toString();
	}

}
