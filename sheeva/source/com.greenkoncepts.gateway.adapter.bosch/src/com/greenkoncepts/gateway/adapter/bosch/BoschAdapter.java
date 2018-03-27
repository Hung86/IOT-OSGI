package com.greenkoncepts.gateway.adapter.bosch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.greenkoncepts.gateway.api.adapter.AHttpAdapter;
import com.greenkoncepts.gateway.api.adapter.AHttpDevice;
import com.greenkoncepts.gateway.api.bridge.BridgeMaster;
import com.greenkoncepts.gateway.util.Util;

public class BoschAdapter extends AHttpAdapter {
	public final static String DEVICE_BOSCH_XDK = "2050"; 	// Bosch XDK
	
	
	private int _numDevices = 0;
	private BridgeMaster bridgeMaster;
	private ExecutiveDatabaseImp dbExecute = new ExecutiveDatabaseImp(adapterClass);

	protected void activator() {
		adapterName = "Bosch";
		if (dbService == null ) {
			new Thread() {
				public void run() {
					while (dbService == null) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							mLogger.error("InterruptedException", e);
						}
						mLogger.error("...waiting instance of database");
					}
					initialize();
				}
			}.start();
		} else {
			initialize();
		}
	}

	protected void deactivator() {
		// Stop ExecutorSerivce
		try {
			if (scheduledExecutorService != null) {
				scheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS);
				scheduledExecutorService.shutdownNow();
			}
		} catch (InterruptedException e) {
			mLogger.error("InterruptedException", e);
		}
		mLogger.info(adapterName + " is stopped. ");
	}

	private void initialize() {
		try {
			mLogger.info("...instance of database and transport are available now");
			setDbExecute(dbExecute);
			dbExecute.setDatabaseService(dbService);
			dbExecute.initTables();
			Properties _props = Util.getPropertiesFile(hiddenConfigFile);
			try {
				COMMUNICATION_ERROR_MAX = Integer.parseInt(_props.getProperty("ErrorMax"));
			} catch (NumberFormatException e) {
				//COMMUNICATION_ERROR_MAX = 30;
			}

			Map<String, String> adapterSettings = dbExecute.getAdapterSettings();
			if (adapterSettings.size() > 0) {
				mLogger.info(adapterName + " : " + adapterSettings);
		
				List<Map<String, String>> deviceList = dbExecute.getDeviceList();
				_numDevices = deviceList.size();
				if (_numDevices > 0) {
					String device_category = null;
					String device_instanceid = "1";
					BoschDevice dev = null;

					for (int i = 0; i < _numDevices; i++) {
						Map<String, String> deviceItem = deviceList.get(i);
						device_category = deviceItem.get("device_category");
						device_instanceid = deviceItem.get("device_instanceid");
						dev = null;
						List<Map<String, String>> deviceAtributes = dbExecute.getXDKSettings(device_instanceid);
						if (device_category.equalsIgnoreCase(DEVICE_BOSCH_XDK)) {
							List<Map<String, String>> deviceConfigs = dbExecute.getXDKConfigs(device_instanceid);
							dev = new XDK(Integer.parseInt(device_instanceid),device_category, Integer.parseInt(deviceConfigs.get(deviceConfigs.size() - 1).get("value")), this);
						}
						if (dev != null) {
							dev.setDeviceAttributes(deviceAtributes);
							_deviceList.add(dev);
						}
					}
				} else {
					mLogger.error(adapterName + " has no any devices in configuration.");
					return;
				}
			} else {
				mLogger.error(adapterName + " is initialized automatically.");
				adapterSettings = new HashMap<String, String>();
				adapterSettings.put("protocol", "");
				adapterSettings.put("serial_port", "");
				adapterSettings.put("baudrate", "");
				adapterSettings.put("stop_bit", "");
				adapterSettings.put("parity", "");
				adapterSettings.put("address", "");
				adapterSettings.put("port", "");
				adapterSettings.put("query_timeout", "");
				dbExecute.insertAdapterSettings(adapterSettings);
				return;
			}

			// Set serial error for each hour to 0
			for (int i = 0; i < 24; i++) {
				error4eachhour[i] = 0;
			}
			scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
			try {
				scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						checkAdapterCommunication();
					}
				}, 0, TICK_PERIOD, TimeUnit.SECONDS);
			} catch (RejectedExecutionException e) {
				mLogger.error("scheduledExecutorService:" , e);
			} catch (NullPointerException e) {
				mLogger.error("scheduledExecutorService:" , e);
			} catch (IllegalArgumentException e) {
				mLogger.error("scheduledExecutorService:" , e);
			}
			mLogger.info(adapterName + " is started! ");
		} catch (Exception e) {
			mLogger.error("Exception", e);
		}
	}
	
	@Override
	synchronized public ArrayList<String> getData(Integer time) throws Exception {
		ArrayList<String> msg = new ArrayList<String>();
		if (_deviceList.isEmpty()) {
			return msg;
		}

		for (AHttpDevice dev : _deviceList) {
			if (adapterMode != STORED_DATA_MODE) {
				mLogger.info("Mode:" + adapterMode);
				return msg;
			}
			String msg_dev = dev.getDeviceData();
			if (!msg_dev.equals("")) {
				msg.add(msg_dev);
			} else {
				mLogger.error("No data for device " + dev.getId());
			}
		}
		
		return msg;
	}

	
	@Override
	public int getAdapterType() {
		return HTTP_TYPE;
	}
	
	@Override
	synchronized public boolean setNodeValue(String category, String deviceId, Object data) {
		for (AHttpDevice dev : _deviceList) {
			if(dev.setDataSensors(data)) {
				return true;
			}
		}	
		return false;
	}
	
	// database
	@Override
	public boolean updateDeviceAttributes(String address, String deviceid, List<Map<String, String>> attributes) {
		return dbExecute.updateXDKSettings(deviceid, attributes);
	}

	@Override
	public boolean insertDeviceAttributes(String address, String deviceid, List<Map<String, String>> attributes) {
		return dbExecute.InsertXDKSettings(deviceid, attributes);
	}
	
	@Override
	public List<Map<String, String>> getDeviceAttributes(String address, String deviceid) {
		return dbExecute.getXDKSettings(deviceid);
	}

	@Override
	public List<Map<String, String>> getDeviceConfigurations(String instanceId) {
		return dbExecute.getXDKConfigs(instanceId);
	}
	
	@Override
	public boolean insertDeviceConfigurations(String instanceId, List<Map<String, String>> xdkConfig) {
		return dbExecute.InsertXDKConfigs(instanceId, xdkConfig);
	}
	
	@Override
	public boolean updateDeviceConfigurations(String instanceId, List<Map<String, String>> xdkConfig) {
		return dbExecute.updateXDKConfigs(instanceId, xdkConfig);
	}
	
	
	@Override
	public boolean importDeviceData(String deviceid, List<Map<String, String>> attributes) {
		return dbExecute.importXDKData(deviceid, attributes);
	}

	public void setBridgeMaster(BridgeMaster bm) {
		bridgeMaster = bm;
	}

	public void clearBridgeMaster(BridgeMaster bm) {
		bridgeMaster = null;
	}
	
	public BridgeMaster getBridgeMaster() {
		return bridgeMaster;
	}
}
