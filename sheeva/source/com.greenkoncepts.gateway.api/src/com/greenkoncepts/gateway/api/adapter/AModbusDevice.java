package com.greenkoncepts.gateway.api.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greenkoncepts.gateway.api.transport.GWModbus;

public abstract class AModbusDevice implements IDevice {
	static public int DATA_MODE = 0;
	static public int CONFIG_MODE = 1;
	static public int OFFSET_DATA = 1;
	static final public long MAX_INT = (long) (Math.pow(2, 32));

	protected boolean isDebug;
	protected String category;
	protected int modbusid;
	protected GWModbus modbus;
	protected long timestamp;
	protected int errorCount;
	protected List<Map<String, String>> real_time_data;;
	protected Map<Integer, String> device_config;

	protected DecimalFormat vformat = new DecimalFormat("#########0.0000");
	protected Logger mLogger = LoggerFactory.getLogger(getClass().getName());
	
	public AModbusDevice(int id, String cat) {
		modbusid = id;
		category = cat;
		timestamp = 0;
		errorCount = 0;
		isDebug = false;
		real_time_data = new ArrayList<Map<String, String>>();
		device_config = new HashMap<Integer, String>();
	}

	public abstract String getDeviceData();

	public abstract Map<Integer, String> getDeviceConfig();

	public abstract List<Integer> setDeviceConfig(Map<Integer, String> config);

	public abstract List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh);
	
	public boolean setNodeValue(Object data) {
		return false;
	}

	public void setModbusProtocol(GWModbus modbusProtocol) {
		modbus = modbusProtocol;
	}

	public String getDeviceMetaData() {
		return "DEVICEID=" + getId() + ",STATUS=" + getStatus() + ";";
	}

	public int modbusId() {
		return modbusid;
	}

	public String getId() {
		return category + "-" + modbusid;
	}

	public int getStatus() {
		if (errorCount <= 1) {
			return DEVICE_STATUS_ONLINE;
		} else if (errorCount < 3) {
			return DEVICE_STATUS_UNKNOWN;
		} else {
			return DEVICE_STATUS_OFFLINE;
		}
	}

	public void setDebugFlag(boolean flag) {
		isDebug = flag;
	}
}
