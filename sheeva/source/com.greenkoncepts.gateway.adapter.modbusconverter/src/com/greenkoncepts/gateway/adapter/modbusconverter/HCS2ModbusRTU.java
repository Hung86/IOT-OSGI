package com.greenkoncepts.gateway.adapter.modbusconverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class HCS2ModbusRTU extends ModbusConverterDevice {

	public HCS2ModbusRTU(int address, String category, HashMap<Integer,DataPoint> nodeTable) {
		super(category, address, nodeTable);
	}

	@Override
	public String getDeviceData() {
		byte[] data = null;
		for (Map<String, Integer> item : commandList) {
			data = modbus.readInputRegisters(modbusid, item.get("register"), item.get("length"));
			if(decodingData(item.get("register"), data, DATA_MODE)) {
				calculateDecodedData(item.get("register"));
			}
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
			for (Map<String, Integer> item : commandList) {
				byte[] data = modbus.readInputRegisters(modbusid, item.get("register"), item.get("length"));
				decodingData(item.get("register"), data, DATA_MODE);
			}
		}

		real_time_data.clear();
	//	if (getStatus() == GKProtocol.DEVICE_STATUS_ONLINE) {
			for (Integer reg : nodeCommandMapping.keySet()) {
				ArrayList<DataPoint> datapointList = nodeCommandMapping.get(reg);
				if (datapointList != null) {
					for (DataPoint dp : datapointList) {
						Map<String, String> item = new Hashtable<String, String>();
						item.put("row", String.valueOf(dp.channelIdx));
						item.put("channel_" + dp.channelIdx, "Channel " + dp.channelIdx);
						item.put("register_" + dp.channelIdx, String.valueOf(dp.register));
						item.put("name_" + dp.channelIdx, dp.measureName);
						item.put("data_" + dp.channelIdx, String.valueOf(dp.measureValue));
						item.put("unit_" + dp.channelIdx, dp.measureUnit);
						real_time_data.add(item);

					}
				}
			}
	//	}
			return real_time_data;
	}

	
	private boolean decodingData(int reg, byte[] data, int mode) {
		if (data == null) {
			errorCount++;
			return false;
		}

		errorCount = 0;
		if (mode == DATA_MODE) {
			int queryingRegister = reg;
			ArrayList<DataPoint> datapoint = nodeCommandMapping.get(queryingRegister);
			if (datapoint != null) {
				for (DataPoint dp : datapoint) {
					if (dp.dataType.equalsIgnoreCase("float")) {
						float tempValue = ModbusUtil.ieee754RegistersToFloatLowFirst(data, OFFSET_DATA + dp.index);
						dp.rawValue = vformat.format(tempValue);
						continue;
					}

					if (dp.dataType.equalsIgnoreCase("short") || dp.dataType.equalsIgnoreCase("ushort")) {
						int tempValue = ModbusUtil.registerToShort(data, OFFSET_DATA + dp.index);
						dp.rawValue = String.valueOf(tempValue);
						continue;
					}

					if (dp.dataType.equalsIgnoreCase("int") || dp.dataType.equalsIgnoreCase("integer")) {
						int tempValue = ModbusUtil.registersBEToInt(data, OFFSET_DATA + dp.index);
						dp.rawValue = String.valueOf(tempValue);
						continue;
					}

					if (dp.dataType.equalsIgnoreCase("uint") || dp.dataType.equalsIgnoreCase("uinteger")) {
						Long tempValue = ModbusUtil.registersBEToLong(data, OFFSET_DATA + dp.index);
						dp.rawValue = String.valueOf(tempValue);
						continue;
					}
				}
			}
			return true;
		}

		if (mode == CONFIG_MODE) {

			return true;
		}

		return true;
	}
	
	private void calculateDecodedData(int reg) {
		ArrayList<DataPoint> datapointList = nodeCommandMapping.get(reg);
		if (datapointList != null) {
			mLogger.debug("------------- Device Id = " + modbusid);
			for (DataPoint dp : datapointList) {
				mLogger.debug("------------- channel = " + dp.channelIdx + ", measure = " + dp.measureName + ", value = " + dp.rawValue);
				if (dp.dataType.equalsIgnoreCase("float")) {
					double currReading = Double.parseDouble(dp.rawValue);
					dp.measureValue = currReading * dp.measureRatio;
				} else {
					long currReading = Long.parseLong(dp.rawValue);
					dp.measureValue = currReading * dp.measureRatio;

				}
				if (dp.hasConsumption) {
					if ((dp.prevmMeasureValue != 0) && (dp.measureValue >= dp.prevmMeasureValue)) {
						dp.consumedValue = (dp.measureValue - dp.prevmMeasureValue) * dp.consumedRatio;
					} else {
						dp.consumedValue = 0;
					}
					dp.prevmMeasureValue = dp.measureValue;
				}
			}
		}
	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		Hashtable<String, StringBuilder> hashDeviceData = new Hashtable<String, StringBuilder>();
		StringBuilder sbData = new StringBuilder();
		for (Integer reg : nodeCommandMapping.keySet()) {
			ArrayList<DataPoint> datapointList = nodeCommandMapping.get(reg);
			for (DataPoint dp : datapointList) {
				StringBuilder sb = hashDeviceData.get(dp.channelIdx + "-" + dp.subchannelIdx);
				if (sb == null) {
					sb = new StringBuilder();
					sb.append("|DEVICEID=" + getId() + "-" + dp.channelIdx + "-" + dp.subchannelIdx);
					sb.append(";TIMESTAMP=" + timestamp);
					hashDeviceData.put(dp.channelIdx + "-" + dp.subchannelIdx, sb);
				}
				sb.append(";" + dp.measureName + "=" + vformat.format(dp.measureValue) + "," + dp.measureUnit);
				if (dp.hasConsumption) {
					sb.append(";" + dp.consumedName + "=" + vformat.format(dp.consumedValue) + "," + dp.consumedUnit);
				}
			}
		}

		for (String key : hashDeviceData.keySet()) {
			sbData.append(hashDeviceData.get(key));
		}
		return sbData.toString();
	}

}
