package com.greenkoncepts.gateway.adapter.sitelab;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.greenkoncepts.gateway.api.adapter.AModbusAdapter;
import com.greenkoncepts.gateway.util.Util;

public class SitelabAdapter extends AModbusAdapter {
	public final static String DEVICE_WATER_SITELAB_SL1168 = "7026";

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

	protected void activator() {
		adapterName = "Sitelab";
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
			scheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS);
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
					SitelabDevice dev = null;

					for (int i = 0; i < _numDevices; i++) {
						Map<String, String> deviceItem = deviceList.get(i);
						device_category = deviceItem.get("device_category");
						device_instanceid = deviceItem.get("device_instanceid");
						dev = null;
						if (device_category.equals(DEVICE_WATER_SITELAB_SL1168)) {
							dev = new SitelabSL1168(Integer.parseInt(device_instanceid), device_category);
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
}
