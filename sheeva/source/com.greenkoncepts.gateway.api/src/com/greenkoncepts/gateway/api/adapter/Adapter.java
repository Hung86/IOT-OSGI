package com.greenkoncepts.gateway.api.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface Adapter {
	public static short STORED_DATA_MODE = 0;
	public static short LIVE_DATA_MODE = 1;
	public static short CONFIGURATION_MODE = 2;

	public static short OTHER_TYPE = -1;
	public static short BACNET_TYPE = 0;
	public static short MODBUS_TYPE = 1;
	public static short OPCUA_TYPE = 2;
	public static short HTTP_TYPE = 3;
	public static short SEGMENT_TYPE = 4;

	public String getMetaData(Integer time);

	public ArrayList<String> getData(Integer time) throws Exception;

	public int getMode();

	public void setMode(int mode);

	public int getCommunicationErrorIn24hrs();

	public int getAdapterType();

	public String getAdapterName();
	
	public boolean setNodeValue(String category, String deviceId, Object data);
	
	public List<Map<String, String>> getRealTimeData(String address, String instanceId, String dataindex, String length) throws Exception;


	// access adapter's data from database
	public Map<String, String> getAdapterSettings();

	public List<Map<String, String>> getDeviceList();

	public List<Map<String, String>> getAllDeviceAttribute();

	public boolean insertAdapterSettings(Map<String, String> attribute);

	public boolean insertDeviceList(List<Map<String, String>> deviceList);

	public boolean updateAdapterSettings(Map<String, String> attributes);

	public boolean updateDataObject(List<Map<String, Object>> objectList);

	public boolean updateDeviceList(List<Map<String, String>> deviceList);
	
	public boolean insertDeviceAttributes(String address, String deviceid, List<Map<String, String>> attributes);
	
	public boolean updateDeviceAttributes(String address, String deviceid, List<Map<String, String>> attributes);

	public List<Map<String, String>> getDeviceAttributes(String address, String deviceid);

	public boolean deleteAdapter();
	
	public boolean deleteDeviceList(List<Map<String, String>> listDevice);

	public List<Object> getExportData();

	public boolean importData(Map<String, String> adapters, List<Map<String, String>> devices, List<Map<String, String>> attributes);

	// ///////////////////////////////////////////////
	// ///////////////////////////////////////////////
	// ///////////////////////////////////////////////
}
