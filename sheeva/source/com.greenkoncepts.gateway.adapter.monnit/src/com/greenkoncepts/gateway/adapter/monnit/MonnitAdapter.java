package com.greenkoncepts.gateway.adapter.monnit;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.greenkoncepts.gateway.api.adapter.AHttpAdapter;
import com.greenkoncepts.gateway.api.adapter.AHttpDevice;
import com.greenkoncepts.gateway.util.Util;

public class MonnitAdapter extends AHttpAdapter {
	public final static String DEVICE_MONNIT_TEMPERATURE = "2051"; 	// Monnit
	public final static String DEVICE_MONNIT_VIBRATION = "2052"; 	// Monnit

	private int _numDevices = 0;

	private ExecutiveDatabaseImp dbExecute = new ExecutiveDatabaseImp(adapterClass);

	protected void activator() {
		adapterName = "Monnit";
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

		//	Map<String, String> adapterSettings = dbExecute.getAdapterSettings();
			if (_props.size() > 0) {
				mLogger.info(adapterName + " : " + _props);
		
				//List<Map<String, String>> deviceList = dbExecute.getDeviceList();
				_numDevices = Integer.parseInt(_props.getProperty("device_num"));
				if (_numDevices > 0) {
					String device_category = null;
					String device_gwid = null;
					String device_instanceid = "1";
					MonnitDevice dev = null;

					for (int i = 0; i < _numDevices; i++) {
						device_gwid = _props.getProperty("device_" + i + "_gatewayid");
						device_category = _props.getProperty("device_" + i + "_category");
						device_instanceid = _props.getProperty("device_" + i + "_deviceid");
				
						dev = null;
						//List<Map<String, String>> deviceAtributes = dbExecute.getXDKSettings(device_instanceid);
						if (device_category.equalsIgnoreCase(DEVICE_MONNIT_TEMPERATURE)) {
							dev = new Temperature(Integer.parseInt(device_instanceid), device_category, device_gwid);
						} else if (device_category.equalsIgnoreCase(DEVICE_MONNIT_VIBRATION)) {
							dev = new Vibration(Integer.parseInt(device_instanceid), device_category, device_gwid);
						}
						if (dev != null) {
							_deviceList.add(dev);
						}
					}
				} else {
					mLogger.error(adapterName + " has no any devices in configuration.");
					return;
				}
			} else {
				mLogger.error(adapterName + " has no any devices in configuration.");
				//mLogger.error(adapterName + " is initialized automatically.");
//				adapterSettings = new HashMap<String, String>();
//				adapterSettings.put("protocol", "");
//				adapterSettings.put("serial_port", "");
//				adapterSettings.put("baudrate", "");
//				adapterSettings.put("stop_bit", "");
//				adapterSettings.put("parity", "");
//				adapterSettings.put("address", "");
//				adapterSettings.put("port", "");
//				adapterSettings.put("query_timeout", "");
//				dbExecute.insertAdapterSettings(adapterSettings);
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
}
