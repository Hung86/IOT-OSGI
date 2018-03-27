package com.greenkoncepts.gateway.api.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.database.AExecutiveDatabase;
import com.greenkoncepts.gateway.api.database.DbService;


public abstract class ABacnetAdapter implements Adapter {
	public static int SCANNING_NODE = 0;
	public static int REAL_READING_NODE = 1;
	public static int USED_REAL_READING_NODE = 2;
	public static int VIRTUAL_READING_NODE = 3;
	public static int USED_VIRTUAL_READING_NODE = 4;
	public static int WRITTING_NODE = 5;
	public static int USED_WRITTING_NODE = 6;
	
	protected String adapterName;
	protected int webConsoleNodeType = USED_REAL_READING_NODE;
	protected DbService dbService;
	protected int adapterMode = STORED_DATA_MODE;
	private AExecutiveDatabase dbExecute;
	
	public void setDbExecute (AExecutiveDatabase db) {
		dbExecute = db;
	}
	public void setDbService(DbService db) {
		dbService = db;
	}

	public void clearDbService(DbService db) {
		dbService = null;
	}
	
	@Override
	public int getCommunicationErrorIn24hrs() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNodeType() {
		return webConsoleNodeType;
	}
	
	public void setNodeType(int nodeType) {
		webConsoleNodeType = nodeType;
	}
	@Override
	public int getMode() {
		return adapterMode;
	}

	@Override
	public void setMode(int mode) {
		adapterMode = mode;
	}
	
	@Override
	public String getAdapterName() {
		return adapterName;
	}
	abstract public boolean setDefaultValue() throws Exception;


	// access adapter's data from database

	abstract public List<Map<String, String>> getNodeScanningPage(String deviceAdress, String instanceId,String indexPage,int paging,String type);
	abstract public List<Map<String, String>> getRealNodeReadingPage(String deviceAdress, String instanceId,String indexPage,int paging);
	abstract public List<Map<String, String>> getNodeWrittingPage(String deviceAdress, String instanceId,String indexPage,int paging);
	abstract public List<Map<String, String>> getVirtualNodeReadingPage(String deviceAdress, String instanceId, String indexPage,int paging);
	
	abstract public List<Map<String,String>> getValidationRule(String instanceId, String deviceAddress) ;
	
	abstract public Map<String,String> getValidationRuleById(String instanceId, String deviceAddress,String validationId) ;

	abstract public boolean insertValidationRule(String instanceId, String deviceAddress,String expressions,String action,String dataPoint);
	abstract public boolean updateDeviceAttributesScanning(String address, String deviceInstance, List<Map<String, String>> attributes);
	abstract public boolean updateDeviceAttributesConfiguration(String address, String deviceInstance, List<Map<String, String>> attributes);
	abstract public int numOfNode(String deviceAdress, String instanceId, int nodeType,String type);
	abstract public boolean deleteDeviceAttribute(String address, String instanceId, List<String> dataPoints);
	abstract public boolean deleteValidation(String vadationId);
	abstract public List<Map<String, String>> scanDevice(boolean scanning);
	abstract public void scanDeviceObjectIdentifier(String address, String instanceid);

	abstract public boolean updateValidationRule(String expressions, String action, String id);
	abstract public boolean checkUniqueObjectIdentifier(String objectIdentifier,String deviceInstanceId, String deviceAddress);

	@Override
	public List<Map<String, String>> getDeviceAttributes(String address, String deviceid) {
		return new ArrayList<Map<String,String>>();
	}
	
	@Override
	public Map<String, String> getAdapterSettings() {

		return dbExecute.getAdapterSettings();
	}
	
	@Override
	public boolean insertDeviceList(List<Map<String, String>> deviceList) {
		return dbExecute.insertDeviceList(deviceList);
	}
	
	@Override
	public List<Map<String, String>> getDeviceList() {
		return dbExecute.getDeviceList();
	}
	
	@Override
	public boolean updateAdapterSettings(Map<String, String> attributes) {
		return dbExecute.updateAdapterSettings(attributes);
	}

	@Override
	public boolean deleteDeviceList(List<Map<String, String>> listDevice) {
		// TODO Auto-generated method stub
		return dbExecute.deleteDeviceList(listDevice);
	}

	@Override
	public boolean updateDeviceList(List<Map<String, String>> deviceList) {
		// TODO Auto-generated method stub
		return dbExecute.updateDeviceList(deviceList);
	}
	@Override
	public List<Map<String, String>> getAllDeviceAttribute() {
		return dbExecute.getAllDeviceAttributes();
	}
	
	@Override
	public boolean updateDataObject(List<Map<String, Object>> objectList) {
		return dbExecute.updateDataObject(objectList);
	}
	

	@Override
	public boolean insertAdapterSettings(Map<String, String> attribute) {
		boolean result = dbExecute.insertAdapterSettings(attribute);
		return result;
	}

	@Override
	public boolean deleteAdapter() {
		boolean result =  dbExecute.deleteAdapter();
		return result;
	}
	
	
	@Override
	public List<Object> getExportData() {
		return dbExecute.getExportData();
	}
	
	@Override
	public boolean importData(Map<String, String> adapters, List<Map<String, String>> devices, List<Map<String, String>> attributes){
		return dbExecute.importData(adapters, devices, attributes);
	}

}