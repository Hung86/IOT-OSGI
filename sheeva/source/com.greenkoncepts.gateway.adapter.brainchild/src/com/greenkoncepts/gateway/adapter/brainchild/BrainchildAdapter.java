package com.greenkoncepts.gateway.adapter.brainchild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.greenkoncepts.gateway.api.adapter.AModbusAdapter;
import com.greenkoncepts.gateway.api.bridge.BridgeMaster;
import com.greenkoncepts.gateway.util.Util;

public class BrainchildAdapter extends AModbusAdapter {
	public final static String DEVICE_DI_16 = "3000"; 	// 16 channel digital input
	public final static String DEVICE_DI_16_STATE = "3002"; 	// 16 channel digital input to check 1/0 of input state
	public final static String DEVICE_AII_8 = "3001"; 	// 8 channel digital input
	public final static String DEVICE_DAIO_8 = "3003"; 	// 8 channel digital input
	
	private List<BrainchildDevice> _deviceStateList = new ArrayList<BrainchildDevice>();
	private String _communicated_mode = MODBUS_RTU;
	private String _serial_port = "/dev/ttyUSB0";
	private String _baudrate = "9600";
	private String _stopbit = "1";
	private String _parity = "none";
	private String _query_timeout = "500";
	private String _address = "192.168.1.1";
	private String _port = "502";
	private int _numDevices = 0;

	private ExecutiveDatabaseImp dbExecute = new ExecutiveDatabaseImp(adapterClass);
	
	//trip status
	private BridgeMaster bridgeMaster;

	protected void activator() {
		adapterName = "Brainchild";

		if (dbService == null || (tranpsort == null)) {
			new Thread() {
				public void run() {
					while (dbService == null || (tranpsort == null)) {
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
		// Stop ExecutorSerivce
		try {
			scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS);
			scheduledExecutorService.shutdownNow();
		} catch (InterruptedException e) {
			mLogger.error("InterruptedException", e);
		}
		modbusProtocol.disconnect();
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
				_communicated_mode = adapterSettings.get("protocol");
				_query_timeout = adapterSettings.get("query_timeout");

				if (_communicated_mode.equalsIgnoreCase(MODBUS_RTU)) {
					_serial_port = adapterSettings.get("serial_port");
					_baudrate = adapterSettings.get("baudrate");
					_stopbit = adapterSettings.get("stop_bit");
					_parity = adapterSettings.get("parity");
					modbusProtocol = tranpsort.getModbusRTUInstance(_serial_port, _baudrate, _stopbit, _parity, _query_timeout);
				} else if (_communicated_mode.equalsIgnoreCase(MODBUS_TCP)) {
					_port = adapterSettings.get("port");
					_address = adapterSettings.get("address");
					modbusProtocol = tranpsort.getModbusTCPInstance(_address, _port, _query_timeout);
				}
				if (modbusProtocol != null) {
					modbusProtocol.connect();
					if (modbusProtocol.isConnect()) {
						mLogger.info(adapterName + " initialized successfully for" + modbusProtocol);
					} else {
						mLogger.error(adapterName + " can not find for " + modbusProtocol);
					}
				} else {
					mLogger.error(adapterName + " can not define communicating protocol");
				}

				List<Map<String, String>> deviceList = dbExecute.getDeviceList();
				_numDevices = deviceList.size();
				if (_numDevices > 0) {
					String device_category = null;
					String device_instanceid = "1";
					BrainchildDevice dev = null;

					for (int i = 0; i < _numDevices; i++) {
						Map<String, String> deviceItem = deviceList.get(i);
						device_category = deviceItem.get("device_category");
						device_instanceid = deviceItem.get("device_instanceid");
						dev = null;
						List<Map<String, String>> deviceAtributes = dbExecute.getDeviceAttributes(device_instanceid);
						if (deviceAtributes.size() > 0) {
							if (device_category.equalsIgnoreCase(DEVICE_DI_16)) {
								dev = new Brainchild16DI(Integer.parseInt(device_instanceid), device_category);
							} else if (device_category.equalsIgnoreCase(DEVICE_AII_8)) {
								dev = new Brainchild8AII(Integer.parseInt(device_instanceid), device_category);
							} else if (device_category.equalsIgnoreCase(DEVICE_DAIO_8)) {
								dev = new Brainchild8DAIO(Integer.parseInt(device_instanceid), device_category);
							} else if (device_category.equalsIgnoreCase(DEVICE_DI_16_STATE)) {
								dev = new Brainchild16DIState(Integer.parseInt(device_instanceid), device_category, bridgeMaster);
								_deviceStateList.add(dev);
							} 
						}
						if (dev != null) {
							dev.setDeviceAttributes(deviceAtributes);
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
				adapterSettings.put("protocol", _communicated_mode);
				adapterSettings.put("serial_port", _serial_port);
				adapterSettings.put("baudrate", _baudrate);
				adapterSettings.put("stop_bit", _stopbit);
				adapterSettings.put("parity", _parity);
				adapterSettings.put("address", _address);
				adapterSettings.put("port", _port);
				adapterSettings.put("query_timeout", _query_timeout);
				dbExecute.insertAdapterSettings(adapterSettings);
				return;
			}

			// Set serial error for each hour to 0
			for (int i = 0; i < 24; i++) {
				error4eachhour[i] = 0;
			}
			scheduledExecutorService = Executors.newScheduledThreadPool(2);
			try {
				scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						checkAdapterCommunication();
					}
				}, 0, TICK_PERIOD, TimeUnit.SECONDS);
				
				if (!_deviceStateList.isEmpty()) {
					scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
						@Override
						public void run() {
							tripStatusThread();
						}
					}, 5, QUERY_PERIOD, TimeUnit.SECONDS);
				}
				
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
	private void tripStatusThread() {
		try {
			for (BrainchildDevice dev : _deviceStateList) {
				String data = dev.getDeviceStateData();
				if (!data.isEmpty()) {
					if (bridgeMaster != null) {
						bridgeMaster.adapterSendDeviceState(adapterClass, data);
					} else {
						mLogger.error("bridgeMaster is null");
					}
				}
			}
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
	public void setBridgeMaster(BridgeMaster bm) {
		bridgeMaster = bm;
	}

	public void clearBridgeMaster(BridgeMaster bm) {
		bridgeMaster = null;
	}
}
