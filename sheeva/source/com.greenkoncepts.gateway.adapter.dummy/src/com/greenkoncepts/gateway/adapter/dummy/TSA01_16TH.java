package com.greenkoncepts.gateway.adapter.dummy;

import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.util.FuncUtil;

public class TSA01_16TH extends DummyDevice {
	private DecimalFormat vformat = new DecimalFormat("#####0.0000");
	static public int CHANNEL_NUM = 16;
	protected short cfModbusId = 0;


	private double calTemperature[] = new double[CHANNEL_NUM];
	private double calHumidity[] = new double[CHANNEL_NUM];


	public TSA01_16TH(int addr, String cat) {
		super(addr, cat);		
		for (int i = 0; i < CHANNEL_NUM; i++) {
			calTemperature[i] = 0;
			calHumidity[i] = 0;
		}
	}

	@Override
	public String getDeviceData() {
		calculateDecodedData();
		return createDataSendToServer();
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			calculateDecodedData();
		}

		real_time_data.clear();
		if (getStatus() == GKProtocol.DEVICE_STATUS_ONLINE) {
			for (int i = 0; i < CHANNEL_NUM; i++) {
				Map<String, String> item = new Hashtable<String, String>();
				item.put("temperature_" + i, vformat.format(calTemperature[i]));
				item.put("humidity_" + i, vformat.format(calHumidity[i]));
				real_time_data.add(item);
			}
		}
		return real_time_data;
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		return null;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		return null;
	}


	private void calculateDecodedData() {
		for (int ch = 0; ch < CHANNEL_NUM; ch++) {
			calTemperature[ch] = FuncUtil.randomWithRange(24, 28);
			calHumidity[ch] = FuncUtil.randomWithRange(50, 60);
		}
	}

	private String createDataSendToServer() {
		if (getStatus() != GKProtocol.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		for (int ch = 0; ch < CHANNEL_NUM; ch++) {
			data.append("|DEVICEID=" + getId() + "-" + ch + "-0");
			data.append(";TIMESTAMP=" + timestamp);
			data.append(";Temperature=" + calTemperature[ch] + ",C");
			data.append(";Humidity=" + calHumidity[ch] + ",%");
		}
		return data.toString();
	}

}
