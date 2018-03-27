package com.greenkoncepts.gateway.api.adapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greenkoncepts.gateway.api.database.AExecutiveDatabase;
import com.greenkoncepts.gateway.api.database.DbService;

public abstract class AHttpAdapter implements Adapter {
	protected String bundleVer = "1.0";
	public static int TICK_PERIOD = 60;// 60s
	protected DbService dbService;
	private AExecutiveDatabase dbExecute;
	protected String adapterName;
	/**
	 * Maximum tolerance accept for not receive data after sending
	 */
	protected int COMMUNICATION_ERROR_MAX = 1000;

	protected ScheduledExecutorService scheduledExecutorService;

	protected int nonStoredDataModeNonActiveCounter = 0;

	protected int MAX_NON_ACTIVE_COUNT = 5;

	protected int[] error4eachhour = new int[24];

	protected int index = 0;

	protected int communicationErrorPer24Hours = 0;
	// ** web console**/
	protected int adapterMode = STORED_DATA_MODE;
	//
	protected String adapterClass = getClass().getSimpleName();
	
	protected Logger mLogger = LoggerFactory.getLogger(adapterClass);
	
	protected List<AHttpDevice> _deviceList = new ArrayList<AHttpDevice>();
	
	protected String hiddenConfigFile = "." + adapterClass + ".prop";
		

	public void setDbExecute(AExecutiveDatabase db) {
		dbExecute = db;
	}

	public void setDbService(DbService db) {
		dbService = db;
	}

	public void clearDbService(DbService db) {
		dbService = null;
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
	public int getCommunicationErrorIn24hrs() {
		return communicationErrorPer24Hours;
	}

	@Override
	public int getAdapterType() {
		return MODBUS_TYPE;
	}

	@Override
	public String getAdapterName() {
		// TODO Auto-generated method stub
		return adapterName;
	}


	@Override
	public Map<String, String> getAdapterSettings() {
		return dbExecute.getAdapterSettings();
	}

	@Override
	public List<Map<String, String>> getDeviceList() {
		// TODO Auto-generated method stub
		return dbExecute.getDeviceList();
	}

	@Override
	public boolean insertDeviceList(List<Map<String, String>> deviceList) {
		return dbExecute.insertDeviceList(deviceList);
	}

	@Override
	public boolean updateAdapterSettings(Map<String, String> attributes) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateDeviceList(List<Map<String, String>> deviceList) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateDataObject(List<Map<String, Object>> objectList) {
		return dbExecute.updateDataObject(objectList);
	}

	@Override
	public boolean deleteDeviceList(List<Map<String, String>> listDevice) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Map<String, String>> getAllDeviceAttribute() {
		return dbExecute.getAllDeviceAttributes();
	}

	@Override
	public boolean insertAdapterSettings(Map<String, String> attribute) {
		boolean result = dbExecute.insertAdapterSettings(attribute);
		return result;
	}

	@Override
	public boolean deleteAdapter() {
		boolean result = dbExecute.deleteAdapter();
		return result;
	}
	
	public boolean setNodeValue(String category, String deviceId, Object data) {
		return false;
	}
	
	@Override
	public boolean insertDeviceAttributes(String address, String deviceid, List<Map<String, String>> attributes) {
		return false;
	}

	@Override
	public boolean updateDeviceAttributes(String address, String deviceInstance, List<Map<String, String>> attributes) {
		return false;
	}

	@Override
	public List<Map<String, String>> getDeviceAttributes(String address, String deviceInstance) {
		return new ArrayList<Map<String,String>>();
	}
	
	public boolean insertDeviceConfigurations(String instanceId, List<Map<String, String>> devConfig) {
		return false;
	}
	
	public boolean updateDeviceConfigurations(String instanceId, List<Map<String, String>> devConfig) {
		return false;
	}
	
	public List<Map<String, String>> getDeviceConfigurations(String instanceId) {
		return new ArrayList<Map<String, String>>();
	}
	
	public boolean importDeviceData(String deviceid, List<Map<String, String>> attributes) {
		return false;
	}
	
	
	public void checkAdapterCommunication() {
		try {
			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			if (hour != index) {
				index = hour;
				error4eachhour[index] = 0;
			}

			for (AHttpDevice dev : _deviceList) {
				if (dev.getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
					error4eachhour[hour]++;
				}
			}

			communicationErrorPer24Hours = 0;
			for (int i = 0; i < 24; i++) {
				communicationErrorPer24Hours += error4eachhour[i];
			}
			

			// If Adapter not in STORED_DATA_MODE
			if (adapterMode != STORED_DATA_MODE) {
				mLogger.info("This mode is not STORED_DATA_MODE,non active counter=" + nonStoredDataModeNonActiveCounter);
				nonStoredDataModeNonActiveCounter++;
				// channel) for more than 10 min
				if (nonStoredDataModeNonActiveCounter > MAX_NON_ACTIVE_COUNT) {
					nonStoredDataModeNonActiveCounter = 0;
					// Swich back to STORED_DATA_MODE
					adapterMode = STORED_DATA_MODE;
				}
			}
		} catch (Exception e) {
			mLogger.error("Exception", e);;
		}
	}

	@Override
	public String getMetaData(Integer time) {
		String msg = "ADAPTER=" + adapterClass + ",VERSION=" + bundleVer + ";";

		if (_deviceList.isEmpty()) {
			return msg;
		}

		for (AHttpDevice dev : _deviceList) {
			msg += dev.getDeviceMetaData();
		}

		return msg;
	}
	@Override
	synchronized public List<Map<String, String>> getRealTimeData(String address , String deviceid, String dataindex, String length) throws Exception {
		int id = Integer.parseInt(deviceid);
		boolean isRefesh = false;
		isRefesh = (adapterMode == LIVE_DATA_MODE ? true : false);
		nonStoredDataModeNonActiveCounter = 0;

		for (AHttpDevice d : _deviceList) {
			if (d.deviceId() == id) {
				return d.getRealTimeData(0, isRefesh);
			}
		}
		
		return new ArrayList<Map<String, String>>();
	}
	

	@Override
	public List<Object> getExportData() {
		return dbExecute.getExportData();
	}

	@Override
	public boolean importData(Map<String, String> adapters, List<Map<String, String>> devices, List<Map<String, String>> attributes) {
		return dbExecute.importData(adapters, devices, attributes);
	}
}
