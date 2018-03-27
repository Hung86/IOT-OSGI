package com.greenkoncepts.gateway.phidgets;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.greenkoncepts.gateway.api.adapter.AModbusAdapter;
import com.greenkoncepts.gateway.api.adapter.AModbusDevice;
import com.greenkoncepts.gateway.util.Util;

public class PhidgetsAdapter extends AModbusAdapter {
	public final static String DEVICE_PHIDGETS_IK888 = "2010";
	
	private int _numDevices = 0;

	private ExecutiveDatabaseImp dbExecute = new ExecutiveDatabaseImp(adapterClass);
	
	protected void activator() {
		adapterName = "Phidgets";
		
		if (dbService == null) {
			new Thread() {
				public void run() {
					while (dbService == null) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							mLogger.error("InterruptedException", e);
						}
						mLogger.error("...waiting instance of database or tranpsort");
					}
					initialize();
				}
			}.start();
		} else {
			initialize();
		}
	}

	protected void deactivator() {
		for (AModbusDevice dev : _deviceList) {
			((PhidgetsDevice)dev).stop();
		}
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
				List<Map<String, String>> deviceList = dbExecute.getDeviceList();
				_numDevices = deviceList.size();
				if (_numDevices > 0) {
					String device_category = null;
					String device_instanceid = "1";
					PhidgetsDevice dev = null;
					
					for (int i = 0; i < _numDevices; i++) {
						Map<String, String> deviceItem = deviceList.get(i);
						device_category = deviceItem.get("device_category");
						device_instanceid = deviceItem.get("device_instanceid");
						dev = null;
						List<Map<String, String>> deviceAtributes = dbExecute.getDeviceAttributes(device_instanceid);

						if (device_category.equals(DEVICE_PHIDGETS_IK888)) {
							List<String> ais = new ArrayList<String>();
							List<Integer> thresholds = new ArrayList<Integer>();
							List<Integer> triggerMins = new ArrayList<Integer>();
							
							for (int j = 0; j< 8; j++) {
								String sensorType = deviceAtributes.get(j).get("sensor_type");
								if ((sensorType == null) || (sensorType.equals(""))){
									ais.add("none");
									thresholds.add(0);
									triggerMins.add(0);
									mLogger.warn("Device " + device_instanceid + " Channel " + j + " is not defined sensor type, IGNORE this channel");
									continue;
								}
								ais.add(sensorType);
								int threshold = 0;
								int triggerMin = 0;
								try{
									threshold = Integer.parseInt(deviceAtributes.get(j).get("threshold"));
								}catch (NumberFormatException e){
									if(sensorType.equals(InterfaceKit888.SENSOR_MOTION_1111)){
										threshold = InterfaceKit888.MOTION_TOLERANCE;
									}
								}
								try{
									triggerMin = Integer.parseInt( deviceAtributes.get(j).get("trigger_min"));
								}catch (NumberFormatException e){
									if(sensorType.equals(InterfaceKit888.SENSOR_MOTION_1111)){
										triggerMin = InterfaceKit888.MOTION_TRIGGER_TOLERANCE;
									}
								}
								thresholds.add(threshold);
								triggerMins.add(triggerMin);
								
							}
							InterfaceKit888 kit = new InterfaceKit888(device_category,Integer.parseInt(device_instanceid),ais);
							kit.setThresholds(thresholds);
							kit.setTriggerMin(triggerMins);
							dev = kit;
						}

						if (dev != null) {
							dev.setModbusProtocol(modbusProtocol);
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
				mLogger.error("scheduledExecutorService:" + e.toString());
			} catch (NullPointerException e) {
				mLogger.error("scheduledExecutorService:" + e.toString());
			} catch (IllegalArgumentException e) {
				mLogger.error("scheduledExecutorService:" + e.toString());
			}
			mLogger.info(adapterName + " is started! ");
		} catch (Exception e) {
			mLogger.error("Exception", e);
		}
	}

	// database
	@Override
	public boolean updateDeviceAttributes(String address, String modbusid, List<Map<String, String>> attributes) {
		return dbExecute.updateDeviceAttributes(modbusid, attributes);
	}

	@Override
	public List<Map<String, String>> getDeviceAttributes(String address, String instanceId) {
		return dbExecute.getDeviceAttributes(instanceId);
	}
	
	@Override
	public boolean insertDeviceAttributes(String address, String deviceInstanceId, List<Map<String, String>> attributes) {
		return dbExecute.InsertDeviceAttributes(deviceInstanceId, attributes);
		
	}

	@Override
	public int getAdapterType() {
		return OTHER_TYPE;
	}
}
