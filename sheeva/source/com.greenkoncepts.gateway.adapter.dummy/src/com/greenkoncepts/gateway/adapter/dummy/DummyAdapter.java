package com.greenkoncepts.gateway.adapter.dummy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.greenkoncepts.gateway.api.adapter.AModbusAdapter;
import com.greenkoncepts.gateway.api.adapter.AModbusDevice;
import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.Util;

public class DummyAdapter extends AModbusAdapter {
	private int _numDevices = 0;
	private ExecutiveDatabaseImp dbExecute = new ExecutiveDatabaseImp(adapterClass);

	protected void activator() {
		adapterName = "Dummy";

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

	private void initialize() {
		try {
			mLogger.info("...instance of database and transport are available now");
			setDbExecute(dbExecute);
			dbExecute.setDatabaseService(dbService);
			dbExecute.initTables();
			Properties _props = Util.getPropertiesFile(hiddenConfigFile);
			Map<String, String> adapterSettings = dbExecute.getAdapterSettings();

			if (adapterSettings.size() > 0) {
				mLogger.info(adapterName + " : " + adapterSettings);

				List<Map<String, String>> deviceList = dbExecute.getDeviceList();
				_numDevices = deviceList.size();
				if (_numDevices > 0) {
					String device_category = null;
					String device_instanceid = "1";
					DummyDevice dev = null;

					for (int i = 0; i < _numDevices; i++) {
						Map<String, String> deviceItem = deviceList.get(i);
						device_category = deviceItem.get("device_category");
						device_instanceid = deviceItem.get("device_instanceid");
						dev = null;
						List<Map<String, String>> deviceAtributes = new ArrayList<Map<String,String>>();
						if (device_category.equalsIgnoreCase(GKProtocol.DEVICE_DI_16)) {
							for (int ch = 0 ; ch < DI16.CHANNEL_NUM; ch++) {
								Map<String, String> item = new HashMap<String, String>();
								String name = _props.getProperty("device" + device_instanceid + "_ch" + ch +"_name", "");
								if ("".equals(name)) {
									continue;
								}
								item.put("name", name);
								item.put("unit", _props.getProperty("device" + device_instanceid + "_ch" + ch +"_unit", ""));
								item.put("ratio", _props.getProperty("device" + device_instanceid + "_ch" + ch +"_ratio", "1"));
								deviceAtributes.add(item);
							}
							
							DI16 d = new DI16(Integer.parseInt(device_instanceid), GKProtocol.DEVICE_DI_16);
							d.setDeviceAttributes(deviceAtributes);
							dev = (DummyDevice) d;
						} else if (device_category.equalsIgnoreCase(GKProtocol.DEVICE_DI_16_STATE)) {
							for (int ch = 0 ; ch < DI16_Trip.CHANNEL_NUM; ch++) {
								Map<String, String> item = new HashMap<String, String>();
								String name = _props.getProperty("device" + device_instanceid + "_ch" + ch +"_name", "");
								if ("".equals(name)) {
									break;
								}
								item.put("name", name);
								item.put("value", _props.getProperty("device" + device_instanceid + "_ch" + ch +"_value", ""));
								deviceAtributes.add(item);
							}
							DI16_Trip d = new DI16_Trip(Integer.parseInt(device_instanceid), GKProtocol.DEVICE_DI_16_STATE);
							d.setDeviceAttributes(deviceAtributes);
							dev = (DummyDevice) d;
						} else if (device_category.equalsIgnoreCase(GKProtocol.DEVICE_PM200)) {
							PM200_v20 d = new PM200_v20(Integer.parseInt(device_instanceid),GKProtocol.DEVICE_PM200);
							for(int ch = 0;ch < PM200_v20.CHANNEL_NUM; ch++){
								
								Double max_l1 = null,min_l1 = null,delta_l1 = null,scale_l1 = null;
								Double max_l2 = null,min_l2 = null,delta_l2 = null,scale_l2 = null;
								Double max_l3 = null,min_l3 = null,delta_l3 = null,scale_l3 = null;
								
								if (_props.containsKey("device" + device_instanceid + "_ch"+ch+"_l1_kw_max")) {
									max_l1 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_ch"+ch+"_l1_kw_max","0"));
								}
							
								if (_props.containsKey("device" + device_instanceid + "_ch"+ch+"_l1_kw_min")) {
									min_l1 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_ch"+ch+"_l1_kw_min","0"));
								}
							
								if (_props.containsKey("device" + device_instanceid + "_ch"+ch+"_l1_kw_delta")) {
									delta_l1 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_ch"+ch+"_l1_kw_delta","0"));
								}
								
								if (_props.containsKey("device" + device_instanceid + "_ch"+ch+"_l1_kw_scale")) {
									scale_l1 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_ch"+ch+"_l1_kw_scale","0"));
								}
						
								d.setL1SimData(ch,max_l1, min_l1, delta_l1, scale_l1);
								
								if (_props.containsKey("device" + device_instanceid + "_ch"+ch+"_l2_kw_max")) {
									max_l2 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_ch"+ch+"_l2_kw_max","0"));
								}
								
								if (_props.containsKey("device" + device_instanceid + "_ch"+ch+"_l2_kw_min")) {
									min_l2 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_ch"+ch+"_l2_kw_min","0"));
								}

								if (_props.containsKey("device" + device_instanceid + "_"+ch+"_l2_kw_delta")) {
									delta_l2 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_"+ch+"_l2_kw_delta","0"));
								}
								
								if (_props.containsKey("device" + device_instanceid + "_ch"+ch+"_l2_kw_scale")) {
									scale_l2 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_ch"+ch+"_l2_kw_scale","0"));
								}
							
								d.setL2SimData(ch,max_l2, min_l2, delta_l2, scale_l2);
								
								if (_props.containsKey("device" + device_instanceid + "_ch"+ch+"_l3_kw_max")) {
									max_l3 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_ch"+ch+"_l3_kw_max","0"));
								}
								
								if (_props.containsKey("device" + device_instanceid + "_ch"+ch+"_l3_kw_min")) {
									min_l3 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_ch"+ch+"_l3_kw_min","0"));
								}
								
								if (_props.containsKey("device" + device_instanceid + "_ch"+ch+"_l3_kw_delta")) {
									delta_l3 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_ch"+ch+"_l3_kw_delta","0"));
								}
								
								if (_props.containsKey("device" + device_instanceid + "_ch"+ch+"_l3_kw_scale")) {
									scale_l3 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_ch"+ch+"_l3_kw_scale","0"));
								}
							
								d.setL3SimData(ch,max_l3, min_l3, delta_l3, scale_l3);
							}
							dev = (DummyDevice) d;
						} else if (device_category.equalsIgnoreCase(GKProtocol.DEVICE_BTU_CONTREC_MODEL212)) {
							Model212 d = new Model212(Integer.parseInt(device_instanceid), GKProtocol.DEVICE_BTU_CONTREC_MODEL212);
							dev = (DummyDevice) d;
						} else if (device_category.equalsIgnoreCase(GKProtocol.DEVICE_MULTICOM_30X)) {
							Multicom30X d
							= new Multicom30X(Integer.parseInt(device_instanceid), GKProtocol.DEVICE_MULTICOM_30X);
							Double max_l1 = null,min_l1 = null;
							Double max_l2 = null,min_l2 = null;
							Double max_l3 = null,min_l3 = null;
							
							if (_props.containsKey("device" + device_instanceid + "_max_output_kw_l1")) {
								max_l1 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_max_output_kw_l1","0"));
							}
							
							if (_props.containsKey("device" + device_instanceid + "_min_output_kw_l1")) {
								min_l1 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_min_output_kw_l1","0"));
							}
							
							if (_props.containsKey("device" + device_instanceid + "_max_output_kw_l2")) {
								max_l2 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_max_output_kw_l2","0"));
							}
							
							if (_props.containsKey("device" + device_instanceid + "_min_output_kw_l2")) {
								min_l2 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_min_output_kw_l2","0"));
							}
							
							if (_props.containsKey("device" + device_instanceid + "_max_output_kw_l3")) {
								max_l3 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_max_output_kw_l3","0"));
							}
							
							if (_props.containsKey("device" + device_instanceid + "_min_output_kw_l3")) {
								min_l3 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_min_output_kw_l3","0"));
							}
							
							d.setSimData(max_l1, min_l1, max_l2, min_l2, max_l3, min_l3);
							dev = (DummyDevice) d;
						} else if (device_category.equalsIgnoreCase(GKProtocol.DEVICE_MPR46S)) {
							MPR46S d = new MPR46S(Integer.parseInt(device_instanceid), GKProtocol.DEVICE_MPR46S);
							Double max_l1 = null, min_l1 = null, delta_l1 = null, scale_l1 = null;
							Double max_l2 = null, min_l2 = null, delta_l2 = null, scale_l2 = null;
							Double max_l3 = null, min_l3 = null, delta_l3 = null, scale_l3 = null;
							
							if (_props.containsKey("device" + device_instanceid + "_l1_kw_max")) {
								max_l1 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_l1_kw_max","0"));
							}
							
							if (_props.containsKey("device" + device_instanceid + "_l1_kw_min")) {
								min_l1 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_l1_kw_min","0"));
							}
							
							if (_props.containsKey("device" + device_instanceid + "_l1_kw_delta")) {
								delta_l1 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_l1_kw_delta","0"));
							}
							
							if (_props.containsKey("device" + device_instanceid + "_l1_kw_scale")) {
								scale_l1 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_l1_kw_scale","0"));
							}
						
							d.setL1SimData(max_l1, min_l1, delta_l1, scale_l1);
							
							if (_props.containsKey("device" + device_instanceid + "_l2_kw_max")) {
								max_l2 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_l2_kw_max","0"));
							}
							
							if (_props.containsKey("device" + device_instanceid + "_l2_kw_min")) {
								min_l2 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_l2_kw_min","0"));
							}
							
							if (_props.containsKey("device" + device_instanceid + "_l2_kw_delta")) {
								delta_l2 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_l2_kw_delta","0"));
							}

							if (_props.containsKey("device" + device_instanceid + "_l2_kw_scale")) {
								scale_l2 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_l2_kw_scale","0"));
							}
						
							d.setL2SimData(max_l2, min_l2, delta_l2, scale_l2);
							
							if (_props.containsKey("device" + device_instanceid + "_l3_kw_max")) {
								max_l3 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_l3_kw_max","0"));
							}
							
							if (_props.containsKey("device" + device_instanceid + "_l3_kw_min")) {
								min_l3 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_l3_kw_min","0"));
							}
							
							if (_props.containsKey("device" + device_instanceid + "_l3_kw_delta")) {
								delta_l3 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_l3_kw_delta","0"));
							}
							
							if (_props.containsKey("device" + device_instanceid + "_l3_kw_scale")) {
								scale_l3 = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_l3_kw_scale","0"));
							}
							
							d.setL3SimData(max_l3, min_l3, delta_l3, scale_l3);
							dev = (DummyDevice) d;
						} else if (device_category.equalsIgnoreCase(GKProtocol.DEVICE_TSA01)) {
							TSA01_16TH d = new TSA01_16TH(Integer.parseInt(device_instanceid), GKProtocol.DEVICE_TSA01);
							dev = (DummyDevice) d;
						} else if (device_category.equalsIgnoreCase(GKProtocol.DEVICE_INEPRO_PRO1250D)) {
							Pro1250D d = new Pro1250D(Integer.parseInt(device_instanceid), GKProtocol.DEVICE_INEPRO_PRO1250D);

							Double max_sys = null, min_sys = null, delta_sys = null, scale_sys = null;
							if (_props.containsKey("device" + device_instanceid + "_sys_kw_max")) {
								max_sys = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_sys_kw_max","0"));
							}

							if (_props.containsKey("device" + device_instanceid + "_sys_kw_min")) {
								min_sys = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_sys_kw_min","0"));
							}

							if (_props.containsKey("device" + device_instanceid + "_sys_kw_delta")) {
								delta_sys = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_sys_kw_delta","0"));
							}

							if (_props.containsKey("device" + device_instanceid + "_sys_kw_scale")) {
								scale_sys = Double.parseDouble(_props.getProperty("device" + device_instanceid + "_sys_kw_scale","0"));
							}

							d.setSimData(max_sys, min_sys, delta_sys, scale_sys);
							dev = (DummyDevice) d;
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
				mLogger.error(adapterName + " is initialized automatically.");
				adapterSettings = new HashMap<String, String>();
				adapterSettings.put("protocol", "none");
				adapterSettings.put("serial_port", "none");
				adapterSettings.put("baudrate", "none");
				adapterSettings.put("stop_bit", "none");
				adapterSettings.put("parity", "none");
				adapterSettings.put("address", "none");
				adapterSettings.put("port", "none");
				adapterSettings.put("query_timeout", "none");
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


	protected void deactivator() {
		mLogger.info(this.getClass().getSimpleName() + " is stoped. ");
	}
	@Override
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

		} catch (Exception e) {
			mLogger.error("Exception", e);
		}
	}

	@Override
	public String getMetaData(Integer time) {
		String msg = "ADAPTER=" + adapterClass + ",VERSION=" + bundleVer + ";";

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
		for (AModbusDevice d : _deviceList) {
			if (d.modbusId() == id) {
				return d.getRealTimeData(0, isRefesh);
			}
		}
		return new ArrayList<Map<String, String>>();
	}

	@Override
	public List<Integer> setConfig(String modbusid, Map<Integer, String> data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, String> getConfig(String modbusid) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int getAdapterType() {
		return OTHER_TYPE;
	}

}
