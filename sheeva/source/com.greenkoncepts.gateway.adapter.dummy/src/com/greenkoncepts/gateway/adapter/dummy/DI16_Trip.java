package com.greenkoncepts.gateway.adapter.dummy;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class DI16_Trip extends DummyDevice {
	public static int CHANNEL_NUM = 16;
	String[] names = new String[CHANNEL_NUM];
	String[] values =new String[CHANNEL_NUM];
	public DI16_Trip(int addr, String cat) {
		super(addr, cat);
		for (int i = 0; i < CHANNEL_NUM; i++) {
			names[i] = "Trip Status";
			values[i] = "0";
		}
	}
	
	public void setDeviceAttributes(List<Map<String, String>> attr) {
		if (attr.isEmpty()) {
			    mLogger.warn("[DI 16 State] Device " + modbusid + " is set all channels to default setting");
			return;
		}
		
		for (int ch = 0; ch < CHANNEL_NUM && ch < attr.size(); ch++) {
			names[ch] = attr.get(ch).get("name");
			values[ch] = attr.get(ch).get("value");
		}
	}

	@Override
	public String getDeviceData() {
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

		real_time_data.clear();
		if (getStatus() == GKProtocol.DEVICE_STATUS_ONLINE) {
			for (int i = 0; i < CHANNEL_NUM; i++) {
				Map<String, String> item = new Hashtable<String, String>();
				item.put("data_" + i,  values[i]);
				item.put("name_" + i, names[i]);
				real_time_data.add(item);
			}
		}
		return real_time_data;
	}

	private String createDataSendToServer() {
		if (getStatus() != GKProtocol.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		StringBuffer data = new StringBuffer();
		timestamp = System.currentTimeMillis();
		for (int ch = 0; ch < CHANNEL_NUM; ch++) {
			data.append("|DEVICEID=" + getId() + "-" + ch + "-0");
			data.append(";TIMESTAMP=" + timestamp);
			data.append(";" + names[ch] + "=" + values[ch]+ ",None");
		}
		return data.toString();
	}
}
