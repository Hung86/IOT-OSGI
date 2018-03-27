package com.greenkoncepts.gateway.adapter.siemens;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class SitransFue950 extends SiemensDevice {

	public static final int MBREG_DATA_START = 100;
	public static final int MBREG_DATA_NUM = 14;

	public static final int OFFSET_kWh = 0;
	public static final int OFFSET_Volume = 2;
	public static final int OFFSET_Power = 4;
	public static final int OFFSET_FlowRate = 6;
	public static final int OFFSET_Water_Supply_Temp = 8;
	public static final int OFFSET_Water_Return_Temp = 10;
	public static final int OFFSET_Diff_Temp = 12;

	private float prevEnergy = 0;
	private float Energy = 0;
	private float prevVolume = 0;
	private float Volume = 0;
	private float Power = 0;
	private float FlowRate = 0;
	private float WaterSupplyTemperature = 0;
	private float WaterReturnTemperature = 0;
	private float DiffTemperature = 0;
	private float DiffEnergy = 0;
	private float DiffVolume = 0;

	private double Scalar_Energy = 0.001;
	private double Scalar_Volume = 1;
	private double Scalar_Power = 0.001;

	public SitransFue950(int addr, String category) {
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
			Map<String, String> item = new Hashtable<String, String>();
			item.put("Energy", vformat.format(Energy * Scalar_Energy));
			item.put("Power", vformat.format(Power * Scalar_Power));
			item.put("Volume", vformat.format(Volume * Scalar_Volume));
			item.put("FlowRate", vformat.format(FlowRate));
			item.put("WaterSupplyTemperature", vformat.format(WaterSupplyTemperature));
			item.put("WaterReturnTemperature", vformat.format(WaterReturnTemperature));
			item.put("DiffTemperature", vformat.format(DiffTemperature));
			real_time_data.add(item);
		}
		return real_time_data;
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		device_config.clear();
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		return registers;
	}

	private boolean decodingData(int idx, byte[] data, int mode) {
		if (data == null) {
			errorCount++;
			return false;
		}

		errorCount = 0;

		if (mode == DATA_MODE) {
			Energy = ModbusUtil.ieee754RegistersToFloatLowFirst(data, ModbusUtil.MB_RESP_DATA_POS + OFFSET_kWh * 2);
			Volume = ModbusUtil.ieee754RegistersToFloatLowFirst(data, ModbusUtil.MB_RESP_DATA_POS + OFFSET_Volume * 2);
			Power = ModbusUtil.ieee754RegistersToFloatLowFirst(data, ModbusUtil.MB_RESP_DATA_POS + OFFSET_Power * 2);
			FlowRate = ModbusUtil.ieee754RegistersToFloatLowFirst(data, ModbusUtil.MB_RESP_DATA_POS + OFFSET_FlowRate * 2);
			WaterSupplyTemperature = ModbusUtil.ieee754RegistersToFloatLowFirst(data, ModbusUtil.MB_RESP_DATA_POS + OFFSET_Water_Supply_Temp * 2);
			WaterReturnTemperature = ModbusUtil.ieee754RegistersToFloatLowFirst(data, ModbusUtil.MB_RESP_DATA_POS + OFFSET_Water_Return_Temp * 2);
			DiffTemperature = ModbusUtil.ieee754RegistersToFloatLowFirst(data, ModbusUtil.MB_RESP_DATA_POS + OFFSET_Diff_Temp * 2);
			return true;
		}

		if (mode == CONFIG_MODE) {

			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		if ((prevEnergy != 0) && (Energy >= prevEnergy)) {
			DiffEnergy = Energy - prevEnergy;
		} else {
			if ((Energy < 100) && ((Float.MAX_VALUE - prevEnergy) < 100)) {
				DiffEnergy = Float.MAX_VALUE - prevEnergy + Energy;
			} else {
				DiffEnergy = 0;
			}
		}
		prevEnergy = Energy;

		if ((prevVolume != 0) && (Volume >= prevVolume)) {
			DiffVolume = Volume - prevVolume;
		} else {
			if ((Volume < 100) && ((Float.MAX_VALUE - prevVolume) < 100)) {
				DiffVolume = Float.MAX_VALUE - prevVolume + Volume;
			} else {
				DiffVolume = 0;
			}
		}
		prevVolume = Volume;
	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		data.append("|DEVICEID=" + getId() + "-0-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + vformat.format(Energy * Scalar_Energy) + ",kWh");
		data.append(";Active Energy=" + vformat.format(DiffEnergy * Scalar_Energy) + ",Wh");
		data.append("|DEVICEID=" + getId() + "-1-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(Power * Scalar_Power) + ",kW");
		data.append("|DEVICEID=" + getId() + "-2-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Water Volume Reading=" + vformat.format(Volume * Scalar_Volume) + ",cu m" + ";Water Volume="
				+ vformat.format(DiffVolume * Scalar_Volume) + ",cu m");
		data.append("|DEVICEID=" + getId() + "-3-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Flow Rate=" + vformat.format(FlowRate) + ",l/s");
		data.append("|DEVICEID=" + getId() + "-4-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Temperature=" + vformat.format(WaterSupplyTemperature) + ",C");
		data.append("|DEVICEID=" + getId() + "-5-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Temperature=" + vformat.format(WaterReturnTemperature) + ",C");
		data.append("|DEVICEID=" + getId() + "-6-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Temperature=" + vformat.format(DiffTemperature) + ",C");

		return data.toString();
	}

}
