package com.greenkoncepts.gateway.adapter.aquametro;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.FuncUtil;

public class AmtronMag extends AquametroDevice {

	public static int MBREG_READING[][] = { { 100, 2 },{104, 1}, { 200, 3 }, { 500, 3 }, { 600, 3 }, { 800, 3 }, { 810, 3 }, { 820, 3 }};
	public static int MBREG_WRITING[][] = { { 20, 1 },{21, 1}, { 22, 1 }, { 104, 1 }, { 202, 1 }, { 502, 1 }, { 602, 1 }, { 802, 1 }, { 812, 1 } , { 822, 1 }};

	public static short posDataEnergyReading = 0;
	public static short posUnitEnergyReading = 0;
	public static short posDataWaterVolumeReading = 0;
	public static short posUnitWaterVolumeReading = 2;
	public static short posDataPowerReading = 0;
	public static short posUnitPowerReading = 2;
	public static short posDataFlowRate = 0;
	public static short posUnitFlowRate = 2;
	public static short posDataCHReturnTemperature = 0;
	public static short posUnitCHReturnTemperature = 2;
	public static short posDataCHSupplyTemperature = 0;
	public static short posUnitCHSupplyTemperature = 2;
	public static short posDataCHDiffTemperature = 0;
	public static short posUnitCHDiffTemperature = 2;

	private double dataPrevEnergyReading = 0;
	private double dataEnergyReading = 0;
	private double dataEnergyConsumption = 0;
	private double dataPrevWaterVolumeReading = 0;
	private double dataWaterVolumeReading = 0;
	private double dataWaterVolumeConsumption = 0;
	private double dataPowerReading = 0;
	private double dataFlowRate = 0;
	private double dataCHReturnTemperature = 0;
	private double dataCHSupplyTemperature = 0;
	private double dataCHDiffTemperature = 0;
	
	private double dataCoolingLoadRT = 0;
	private double dataCHWaterConsumptionRTH = 0;

	private int unitEnergyReading = 0;
	private int unitWaterVolumeReading = 0;
	private int unitPowerReading = 0;
	private int unitFlowRate = 0;
	private int unitCHReturnTemperature = 0;
	private int unitCHSupplyTemperature = 0;
	private int unitCHDiffTemperature = 0;
	
	private int[] cfData = {0,0,0,0,0,0,0,0,0,0};

	private int operationTime = 0;

	public AmtronMag(String category, int addr) {
		super(category, addr);
	}

	@Override
	public String getDeviceData() {
		byte[] data = null;
		boolean ok = true;
		for (int i = 0; i < MBREG_READING.length; i++) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_READING[i][0], MBREG_READING[i][1]);
			if (!decodingData(i, data, DATA_MODE)) {
				ok = false;
				break;
			}
		}
		if (ok) {
			calculateDecodedData();
		}
		return createDataSendToServer();
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		byte[] data = null;
		device_config.clear();
		for (int i = 0; i < MBREG_WRITING.length; i++) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_WRITING[i][0], MBREG_WRITING[i][1]);
			if (decodingData(i, data, CONFIG_MODE)) {
				device_config.put(MBREG_WRITING[i][0], String.valueOf(cfData[i]));
			}
		}
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		if ((config != null) && (!config.isEmpty())) {
			for (Integer n : config.keySet()) {
				byte[] data = FuncUtil.RegisterBigEndian.unsignedShortToRegister(Integer.parseInt(config.get(n)));
				byte[] results = modbus.writeSingleRegister(modbusid, n, data);
				if (results != null) {
					registers.add(n);
				}
			}

		}
		return registers;
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			byte[] data = null;
			boolean ok = true;
			for (int i = 0; i < MBREG_READING.length; i++) {
				data = modbus.readHoldingRegisters(modbusid, MBREG_READING[i][0], MBREG_READING[i][1]);
				if (!decodingData(i, data, DATA_MODE)) {
					ok = false;
					break;
				}
			}
			if (ok) {
				calculateDecodedData();
			}
		}
		
		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			
			item.put("Operation_Time", String.valueOf(operationTime));

			item.put("BTU_Meter_Reading", vformat.format(dataEnergyReading));

			item.put("CH_Water_Consumption", vformat.format(dataEnergyConsumption));

			item.put("Water_Volume_Reading", vformat.format(dataWaterVolumeReading));

			item.put("Water_Volume", vformat.format(dataWaterVolumeConsumption));

			item.put("Flow_Rate", vformat.format(dataFlowRate));

			item.put("CH_Supply_Temp", vformat.format(dataCHSupplyTemperature));

			item.put("CH_Return_Temp", vformat.format(dataCHReturnTemperature));

			item.put("CH_Temp_Diff", vformat.format(Math.abs(dataCHDiffTemperature)));

			item.put("Cooling_Load_kWc", vformat.format(dataPowerReading));
			
			item.put("Cooling_Load", vformat.format(dataCoolingLoadRT));
			
			item.put("CH_Water_Consumption_RTH", vformat.format(dataCHWaterConsumptionRTH));
			
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
				dataEnergyReading = FuncUtil.registersToIEEE754FloatHighFirst(data, OFFSET_DATA + 2 * posDataEnergyReading);
			} else if (idx == 1) {
				unitEnergyReading = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2 * posUnitEnergyReading);
			} else if (idx == 2) {
				dataWaterVolumeReading = FuncUtil.registersToIEEE754FloatHighFirst(data, OFFSET_DATA + 2 * posDataWaterVolumeReading);
				unitWaterVolumeReading = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2 * posUnitWaterVolumeReading);
			} else if (idx == 3) {
				dataPowerReading = FuncUtil.registersToIEEE754FloatHighFirst(data, OFFSET_DATA + 2 * posDataPowerReading);
				unitPowerReading = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2 * posUnitPowerReading);
			} else if (idx == 4) {
				dataFlowRate = FuncUtil.registersToIEEE754FloatHighFirst(data, OFFSET_DATA + 2 * posDataFlowRate);
				unitFlowRate = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2 * posUnitFlowRate);
			} else if (idx == 5) {
				dataCHReturnTemperature = FuncUtil.registersToIEEE754FloatHighFirst(data, OFFSET_DATA + 2 * posDataCHReturnTemperature);
				unitCHReturnTemperature = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2 * posUnitCHReturnTemperature);
			} else if (idx == 6) {
				dataCHSupplyTemperature = FuncUtil.registersToIEEE754FloatHighFirst(data, OFFSET_DATA + 2 * posDataCHSupplyTemperature);
				unitCHSupplyTemperature = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2 * posUnitCHSupplyTemperature);
			} else if (idx == 7) {
				dataCHDiffTemperature = FuncUtil.registersToIEEE754FloatHighFirst(data, OFFSET_DATA + 2 * posDataCHDiffTemperature);
				unitCHDiffTemperature = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2 * posUnitCHDiffTemperature);
			}
			return true;
		}

		if (mode == CONFIG_MODE) {
			cfData[idx] = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA);
		}

		return true;
	}

	private void calculateDecodedData() {
		infoDebug();
		switch (unitEnergyReading) {
			case 1:// MWh
				dataEnergyReading = dataEnergyReading * 1000;
			case 2:// MJ
				dataEnergyReading = dataEnergyReading * 0.28;
			case 3:// GJ
				dataEnergyReading = dataEnergyReading * 277.78;
			default:
		}

		switch (unitPowerReading) {
			case 0:// W
				dataPowerReading = dataPowerReading * 0.001;
			case 2:// MW
				dataPowerReading = dataPowerReading * 1000;
			case 3:// MJ/h
				dataPowerReading = dataPowerReading * 0.28;
			case 4:// GJ/h
				dataPowerReading = dataPowerReading * 277.78;
			default:
		}

		switch (unitFlowRate) {
			case 1:// l/min
				dataFlowRate = dataFlowRate * 0.0167;
			case 2:// l/h
				dataFlowRate = dataFlowRate * 0.00028;
			case 3:// m3/h
				dataFlowRate = dataFlowRate * 0.28;
			default:
		}

		if (unitCHReturnTemperature == 1) { // F to C
			dataCHReturnTemperature = (dataCHReturnTemperature - 32) / 1.8;
		}

		if (unitCHSupplyTemperature == 1) {// F to C
			dataCHSupplyTemperature = (dataCHSupplyTemperature - 32) / 1.8;
		}

		//dataCHDiffTemperature = dataCHDiffTemperature - 273.15; // Kelvin to Celsius

		if ((dataPrevEnergyReading == 0) || (dataPrevEnergyReading > dataEnergyReading)) {
			dataEnergyConsumption = 0;
		} else {
			dataEnergyConsumption = (dataEnergyReading - dataPrevEnergyReading) * 1000.0;
		}

		dataPrevEnergyReading = dataEnergyReading;

		if ((dataPrevWaterVolumeReading == 0) || (dataPrevWaterVolumeReading > dataWaterVolumeReading)) {
			dataWaterVolumeConsumption = 0;
		} else {
			dataWaterVolumeConsumption = dataWaterVolumeReading - dataPrevWaterVolumeReading;
		}

		dataPrevWaterVolumeReading = dataWaterVolumeReading;

		dataCoolingLoadRT = dataPowerReading/3.517;
		dataCHWaterConsumptionRTH = dataCoolingLoadRT/60;
		if (dataFlowRate <= 0) {
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

		sb.append(";BTU Meter Reading=" + vformat.format(dataEnergyReading) + ",kWh");

		sb.append(";CH Water Consumption=" + vformat.format(dataEnergyConsumption) + ",Wh");

		sb.append(";Water Volume Reading=" + vformat.format(dataWaterVolumeReading) + ",cu m");

		sb.append(";Water Volume=" + vformat.format(dataWaterVolumeConsumption) + ",cu m");

		sb.append(";Flow Rate=" + vformat.format(dataFlowRate) + ",l/s");

		sb.append(";CH Supply Temp=" + vformat.format(dataCHSupplyTemperature) + ",C");

		sb.append(";CH Return Temp=" + vformat.format(dataCHReturnTemperature) + ",C");

		sb.append(";CH Temp Diff=" + vformat.format(Math.abs(dataCHDiffTemperature)) + ",C");

		sb.append(";Cooling Load kWc=" + vformat.format(dataPowerReading) + ",kWc");
		
		sb.append(";Cooling Load=" + vformat.format(dataCoolingLoadRT) + ",RT");
		
		sb.append(";CH Water Consumption RTh=" + vformat.format(dataCHWaterConsumptionRTH) + ",RTh");
		
		sb.append(";Cooling Load RTh=" + vformat.format(dataCHWaterConsumptionRTH) + ",RTh");
		
		return sb.toString();
	}
	
	private void infoDebug() {
		mLogger.debug("=== Device Address = " + modbusid);
		mLogger.debug("===dataEnergyReading = " + dataEnergyReading);
		mLogger.debug("===dataWaterVolumeReading = " + dataWaterVolumeReading);
		mLogger.debug("===dataPowerReading = " + dataPowerReading);
		mLogger.debug("===dataFlowRate = " + dataFlowRate);
		mLogger.debug("===dataCHReturnTemperature = " + dataCHReturnTemperature);
		mLogger.debug("===dataCHSupplyTemperature = " + dataCHSupplyTemperature);
		mLogger.debug("===dataCHDiffTemperature = " + dataCHDiffTemperature);

		mLogger.debug("===unitEnergyReading = " + unitEnergyReading);
		mLogger.debug("===unitWaterVolumeReading = " + unitWaterVolumeReading);
		mLogger.debug("===unitPowerReading = " + unitPowerReading);
		mLogger.debug("===unitFlowRate = " + unitFlowRate);
		mLogger.debug("===unitCHReturnTemperature = " + unitCHReturnTemperature);
		mLogger.debug("===unitCHSupplyTemperature = " + unitCHSupplyTemperature);
		mLogger.debug("===unitCHDiffTemperature = " + unitCHDiffTemperature);
	}

}
