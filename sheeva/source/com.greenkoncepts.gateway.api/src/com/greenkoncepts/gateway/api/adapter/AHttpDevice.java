package com.greenkoncepts.gateway.api.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AHttpDevice implements IDevice {
	static final public long MAX_INT = (long) (Math.pow(2, 32));

	protected boolean isDebug;
	protected String category;
	protected int deviceid;
	protected long timestamp;
	protected int errorCount;
	protected List<Map<String, String>> real_time_data;;
	protected Map<Integer, String> device_config;
	
	protected DecimalFormat vformat = new DecimalFormat("#########0.0000");
	protected Logger mLogger = LoggerFactory.getLogger(getClass().getName());
	
	public AHttpDevice(int id, String cat) {
		deviceid = id;
		category = cat;
		timestamp = 0;
		errorCount = 0;
		real_time_data = new ArrayList<Map<String, String>>();
		device_config = new HashMap<Integer, String>();
	}
	
	public String getDeviceMetaData() {
		return "DEVICEID=" + getId() + ",STATUS=" + getStatus() + ";";
	}

	public String getId() {
		return category + "-" + deviceid;
	}
	
	public int deviceId() {
		return deviceid;
	}
	
	public int getStatus() {
		if (errorCount <= 3) {
			return DEVICE_STATUS_ONLINE;
		} else if (errorCount < 10) {
			return DEVICE_STATUS_UNKNOWN;
		} else {
			return DEVICE_STATUS_OFFLINE;
		}
	}
	
	abstract public String getDeviceData() ;
	abstract public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh);
	abstract public boolean setDataSensors(Object dataJson);
	abstract public void setDeviceAttributes(Object data);
}
