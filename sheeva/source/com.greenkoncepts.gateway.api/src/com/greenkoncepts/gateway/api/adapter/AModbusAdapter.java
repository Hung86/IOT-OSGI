package com.greenkoncepts.gateway.api.adapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greenkoncepts.gateway.api.database.AExecutiveDatabase;
import com.greenkoncepts.gateway.api.database.DbService;
import com.greenkoncepts.gateway.api.transport.GWModbus;
import com.greenkoncepts.gateway.api.transport.GWTransport;

public abstract class AModbusAdapter implements Adapter {
	protected String bundleVer = "1.0";
	public static String MODBUS_RTU = "rtu";
	public static String MODBUS_TCP = "tcp";
	public static int TICK_PERIOD = 60;// 60s
	public static int QUERY_PERIOD = 1;// 1s
	protected GWTransport tranpsort;
	protected GWModbus modbusProtocol;
	protected DbService dbService;
	private AExecutiveDatabase dbExecute;
	protected String adapterName;
	/**
	 * Maximum tolerance accept for not receive data after sending
	 */
	protected int COMMUNICATION_ERROR_MAX = 1000;

	protected ScheduledExecutorService scheduledExecutorService;

	/**
	 * Active when Adapter on LIVE_DATA_MODE and CONFIGURATION_MODE, it increases every 1 minute, it clears to 0 when call Hashtable<String, String>
	 * getData(int addr,int channel) in LIVE_DATA_MODE
	 */
	protected int nonStoredDataModeNonActiveCounter = 0;
	/**
	 * Maximum tolerance accept for nonStoredDataModeNonActiveCounter
	 */
	protected int MAX_NON_ACTIVE_COUNT = 5;

	/**
	 * Array with 24 elements. each element stores serial error in 1 hour
	 */
	protected int[] error4eachhour = new int[24];

	protected int index = 0;

	/**
	 * Total serial error in most recent 24 hour.
	 */

	protected int communicationErrorPer24Hours = 0;
	// ** web console**/
	protected int adapterMode = STORED_DATA_MODE;
	
	//
	protected String adapterClass = getClass().getSimpleName();
	
	protected Logger mLogger = LoggerFactory.getLogger(adapterClass);
	
	protected List<AModbusDevice> _deviceList = new ArrayList<AModbusDevice>();
	
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

	public void setTransport(GWTransport ts) {
		tranpsort = ts;
	}

	public void clearTransport(GWTransport ts) {
		tranpsort = null;
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

	public int getModbusProtocolStatus() {
		// TODO Auto-generated method stub
		return 0;
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
	public boolean deleteAdapter() {
		boolean result = dbExecute.deleteAdapter();
		return result;
	}
	
	@Override
	public boolean insertAdapterSettings(Map<String, String> attribute) {
		boolean result = dbExecute.insertAdapterSettings(attribute);
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
	public boolean updateDeviceAttributes(String address, String deviceid, List<Map<String, String>> attributes) {
		return false;
	}

	@Override
	public List<Map<String, String>> getDeviceAttributes(String address, String deviceid) {
		return new ArrayList<Map<String,String>>();
	}
	
	synchronized public void restartCommunicationPort() {
		if (modbusProtocol != null) {
			modbusProtocol.reconnect();
		}
	}
	
	public void checkAdapterCommunication() {
		try {
			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			if (hour != index) {
				index = hour;
				error4eachhour[index] = 0;
			}

			for (AModbusDevice dev : _deviceList) {
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
			if (modbusProtocol == null) {
				return;
			}
			mLogger.info("...check accumulated communication error modbus protocol =" + modbusProtocol.getErrorCount() + " and per 24 hrs ="
					+ communicationErrorPer24Hours);
			if (!modbusProtocol.isConnect()) {
				mLogger.error("Try to initialize for " + modbusProtocol + ". Error Count (" + modbusProtocol.getErrorCount() + ").");
				modbusProtocol.connect();
				if (modbusProtocol.isConnect()) {
					mLogger.info("Initialize  for "  + modbusProtocol + " successfully !");
				}
			} else if (modbusProtocol.getErrorCount() > COMMUNICATION_ERROR_MAX) {
				mLogger.error("Too many error ... Try to initialize for " + modbusProtocol + ". Error Count (" + modbusProtocol.getErrorCount() + ").");
				restartCommunicationPort();
			} else {
				mLogger.debug("Check communication status ... " + modbusProtocol.getErrorCount());
			}
		} catch (Exception e) {
			mLogger.error("Exception", e);;
		}
	}

	@Override
	public String getMetaData(Integer time) {
		String msg = "ADAPTER=" + adapterClass + ",VERSION=" + bundleVer + ";";
		if ((modbusProtocol == null) || (!modbusProtocol.isConnect())) {
			mLogger.error("[getMetaData] Mobus protocol is not ready !");
			return msg;
		}
		if (_deviceList.isEmpty()) {
			return msg;
		}

		for (AModbusDevice dev : _deviceList) {
			msg += dev.getDeviceMetaData();
		}

		return msg;
	}

	@Override
	synchronized public ArrayList<String> getData(Integer time) throws Exception {
		ArrayList<String> msg = new ArrayList<String>();
		if ((modbusProtocol == null) || (!modbusProtocol.isConnect())) {
			mLogger.error("[getData] Mobus protocol is not ready !");
			return msg;
		}
		if (_deviceList.isEmpty()) {
			return msg;
		}

		for (AModbusDevice dev : _deviceList) {
			if (adapterMode != STORED_DATA_MODE) {
				mLogger.info("Mode:" + adapterMode);
				return msg;
			}
			String msg_dev = dev.getDeviceData();
			msg.add(msg_dev);
		}
		return msg;
	}

	@Override
	synchronized public List<Map<String, String>> getRealTimeData(String address, String modbusid, String dataindex, String length) throws Exception {
		int id = Integer.parseInt(modbusid);
		boolean isRefesh = false;
		isRefesh = (adapterMode == LIVE_DATA_MODE ? true : false);
		nonStoredDataModeNonActiveCounter = 0;
		if ((modbusProtocol == null) || (!modbusProtocol.isConnect())) {
			mLogger.error("[getRealTimeData] Mobus protocol is not ready !");
		} else {
			for (AModbusDevice d : _deviceList) {
				if (d.modbusId() == id) {
					return d.getRealTimeData(0, isRefesh);
				}
			}
		}
		return new ArrayList<Map<String, String>>();
	}

	synchronized public List<Integer> setConfig(String modbusid, Map<Integer, String> data) throws Exception {
		int id = Integer.parseInt(modbusid);
		if ((modbusProtocol == null) || (!modbusProtocol.isConnect())) {
			mLogger.error("[setConfig] Mobus protocol is not ready !");
		} else {
			for (AModbusDevice d : _deviceList) {
				if (d.modbusId() == id) {
					return d.setDeviceConfig(data);
				}
			}
		}
		return new ArrayList<Integer>();
	}

	synchronized public Map<Integer, String> getConfig(String modbusid) throws Exception {
		int id = Integer.parseInt(modbusid);
		if ((modbusProtocol == null) || (!modbusProtocol.isConnect())) {
			mLogger.error("[getConfig] Mobus protocol is not ready !");
		} else {
			for (AModbusDevice d : _deviceList) {
				if (d.modbusId() == id) {
					return d.getDeviceConfig();
				}
			}
		}
		return new HashMap<Integer, String>();
	}

	@Override
	public List<Object> getExportData() {
		return dbExecute.getExportData();
	}

	@Override
	public boolean importData(Map<String, String> adapters, List<Map<String, String>> devices, List<Map<String, String>> attributes) {
		return dbExecute.importData(adapters, devices, attributes);
	}
	


	// access adapter's data from database
}
