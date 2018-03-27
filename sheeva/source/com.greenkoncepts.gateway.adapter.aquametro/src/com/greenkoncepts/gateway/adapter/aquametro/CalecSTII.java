package com.greenkoncepts.gateway.adapter.aquametro;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.FuncUtil;

public class CalecSTII extends AquametroDevice {

	public static int MBREG[][] = { { 100, 2 }, { 200, 2 }, { 500, 2 }, { 600, 2 }, { 800, 2 }, { 810, 2 }, { 820, 2 } };

	public static short posDataEnergyReading = 0;
	public static short posDataWaterVolumeReading = 0;
	public static short posDataPowerReading = 0;
	public static short posDataFlowRate = 0;
	public static short posDataCHReturnTemperature = 0;
	public static short posDataCHSupplyTemperature = 0;
	public static short posDataCHDiffTemperature = 0;

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

	private int operationTime = 0;

	public CalecSTII(String category, int addr) {
		super(category, addr);
	}

	@Override
	public String getDeviceData() {
		byte[] data = null;
		boolean ok = true;
		for (int i = 0; i < MBREG.length; i++) {
			data = modbus.readHoldingRegisters(modbusid, MBREG[i][0], MBREG[i][1]);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			byte[] data = null;
			boolean ok = true;
			for (int i = 0; i < 5; i++) {
				data = modbus.readHoldingRegisters(modbusid, MBREG[i][0], MBREG[i][1]);
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
				dataWaterVolumeReading = FuncUtil.registersToIEEE754FloatHighFirst(data, OFFSET_DATA + 2 * posDataWaterVolumeReading);
			} else if (idx == 2) {
				dataPowerReading = FuncUtil.registersToIEEE754FloatHighFirst(data, OFFSET_DATA + 2 * posDataPowerReading);
			} else if (idx == 3) {
				dataFlowRate = FuncUtil.registersToIEEE754FloatHighFirst(data, OFFSET_DATA + 2 * posDataFlowRate);
			} else if (idx == 4) {
				dataCHReturnTemperature = FuncUtil.registersToIEEE754FloatHighFirst(data, OFFSET_DATA + 2 * posDataCHReturnTemperature);
			} else if (idx == 5) {
				dataCHSupplyTemperature = FuncUtil.registersToIEEE754FloatHighFirst(data, OFFSET_DATA + 2 * posDataCHSupplyTemperature);
			} else if (idx == 6) {
				dataCHDiffTemperature = FuncUtil.registersToIEEE754FloatHighFirst(data, OFFSET_DATA + 2 * posDataCHDiffTemperature);
			}
			return true;
		}

		if (mode == CONFIG_MODE) {

		}

		return true;
	}

	private void calculateDecodedData() {
		infoDebug();
		
		dataFlowRate = dataFlowRate * 0.28;
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
	}
}
