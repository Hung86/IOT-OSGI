package com.greenkoncepts.gateway.adapter.bacnet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.nfunk.jep.SymbolTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greenkoncepts.expression.GKEP;
import com.greenkoncepts.gateway.api.adapter.ABacnetAdapter;
import com.greenkoncepts.gateway.util.FuncUtil;
import com.greenkoncepts.gateway.util.Util;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.Network;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DefaultDeviceEventListener;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.gk.GKRemoteDevice;
import com.serotonin.bacnet4j.service.acknowledgement.ReadPropertyAck;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyRequest;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.PropertyReference;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.enumerated.Segmentation;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.util.ArrayUtils;
import com.serotonin.util.IpAddressUtils;

public class BacnetAdapter extends ABacnetAdapter {

	private String bundleVer = "1.0";
	private int _port = 47808;
	private int _query_timeout = 4000;
	private int _numDevices = 1;
	private int tickPeriod = 60000; // repeat every 60s.
	private Timer tickTimer = new Timer();

	// BACnet protocol
	//private int instance_id;
	private LocalDevice localdevice;
	//private RemoteDevice remotedevice;
	private List<GKRemoteDevice> scanedRemoteDeviceList = new ArrayList<GKRemoteDevice>();
	//private List<GKRemoteDevice> newRemoteDevicesList = new ArrayList<GKRemoteDevice>();
	private int idxScanedList = 0;
	private int lenScanedList = 0;
	private DefaultDeviceEventListener localListen = null;
	private List<BacnetDevice> queryDevicelist = new ArrayList<BacnetDevice>();
	private boolean isDeviceScanning = false;

	private int _bacnetBroadCastMode = 0;
	private boolean multiPropertiesAccess = true;
	private int delay_start_query = 0;
	/*
	 * Maximum tolerance accept for nonStoredDataModeNonActiveCounter
	 */
	protected final int MAX_NON_ACTIVE_COUNT = 5;

	/**
	 * Maximum channel can be fit in 1 string
	 */
	private int maxChannelPerMsg = 50;

	/**
	 * Maximum BACNET object identifiers can be retrieved by 1 query
	 */
	private int maxOiPerQuery = 100;
	
	private boolean dummyData = false;
	private Map<Integer, ArrayList<Double>> dummyDataRange = new HashMap<Integer, ArrayList<Double>>();
	// private Hashtable<String, String> _devicelist = new Hashtable<String, String>();
	// private Hashtable<String, ArrayList<Integer>> _deviceRealNodeIdList = new Hashtable<String, ArrayList<Integer>>();
	private DecimalFormat vformat = new DecimalFormat("#######0.0000");
	private int nonStoredDataModeNonActiveCounter = 0;
	private long interval60Seconds = 0;

	private String adapterClass = getClass().getSimpleName();
	private String hiddenConfigFile = "." + adapterClass + ".prop";
	private Logger mLogger = LoggerFactory.getLogger(adapterClass);
	private ExecutiveDatabaseImp dbExecute = new ExecutiveDatabaseImp(adapterClass);

	protected void activator() {
		adapterName = "BacNet Generic";
		if (dbService == null) {
			new Thread() {
				public void run() {
					while (dbService == null) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							mLogger.error("InterruptedException" , e);
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

	private void initialize() {
		mLogger.info("...instance of database is available now");
		setDbExecute(dbExecute);
		dbExecute.setDatabaseService(dbService);
		dbExecute.initTables();
		startLocalDevice();
		try {
			adapterMode = STORED_DATA_MODE;
			interval60Seconds = System.currentTimeMillis();
			nonStoredDataModeNonActiveCounter = 0;

			// These Settings only reserve for developer and project team
			Properties hiddenSettings = getHiddenConfigurations(hiddenConfigFile);
			try {
				tickPeriod = Integer.parseInt(hiddenSettings.getProperty("query_duration"));
				if (tickPeriod < 1) {
					tickPeriod = 1;
				}
			} catch (NumberFormatException e) {
				mLogger.info(adapterName + " uses default Query Duration");
			}
			mLogger.info(adapterName + " has Query Duration = " + tickPeriod);

			try {
				_bacnetBroadCastMode = Integer.parseInt(hiddenSettings.getProperty("bacnet_broadcast_mode"));
			} catch (NumberFormatException e) {
				mLogger.info(adapterName + " uses default bacnet broadcast mode ");
			}
			mLogger.info(adapterName + " has bacnet broadcast mode = " + _bacnetBroadCastMode);

			try {
				maxChannelPerMsg = Integer.parseInt(hiddenSettings.getProperty("maximum_channel_per_msg"));
			} catch (NumberFormatException e) {
				mLogger.info(adapterName + " uses default maxChannelPerMsg");
			}
			mLogger.info(adapterName + " has maxChannelPerMsg = " + maxChannelPerMsg);

			try {
				delay_start_query = Integer.parseInt(hiddenSettings.getProperty("delay_start_query", "0"));
			} catch (NumberFormatException e) {
				mLogger.info(adapterName + " uses default delay_start_query");
			}
			mLogger.info(adapterName + " has delay_start_query = " + delay_start_query);
			
			
			try {
				dummyData = Boolean.parseBoolean(hiddenSettings.getProperty("dummy_data"));
			} catch (NumberFormatException e) {
				mLogger.info(adapterName + " uses default dummyData");
			}
			mLogger.info(adapterName + " has dummyData = " + dummyData);
			mLogger.info(adapterName + " has Query Host Mode = " + Util.osName());

			Map<String, String> adapterSettings = dbExecute.getAdapterSettings();

			if (adapterSettings.size() > 0) {
				try {
					_port = Integer.parseInt(adapterSettings.get("port"));
				} catch (NumberFormatException e) {
					mLogger.error(adapterName + " has wrong port setting in configuration. Use default. ");
				}
				mLogger.info(adapterName + " has port = " + _port);
				try {
					multiPropertiesAccess = Boolean.parseBoolean(adapterSettings.get("multi_property"));
				} catch (NumberFormatException e) {
					mLogger.info(adapterName + " uses default Multi Properties Access");
				}
				mLogger.info(adapterName + " has  Multi Properties Access = " + multiPropertiesAccess);
				try {
					maxOiPerQuery = Integer.parseInt(adapterSettings.get("max_oi_per_query"));
				} catch (NumberFormatException e) {
					mLogger.info(adapterName + " uses default maxOIPerQuery");
				}
				mLogger.info(adapterName + " has maxOIPerQuery = " + maxOiPerQuery);
				try {
					_query_timeout = Integer.parseInt(adapterSettings.get("query_timeout"));
				} catch (NumberFormatException e) {
					mLogger.error(adapterName + " has wrong query_delay setting in configuration. Use default. ");
				}
				mLogger.info(adapterName + " has  Query Timeout = " + _query_timeout);
				try {
					_numDevices = dbExecute.numOfDevice();
				} catch (NumberFormatException e) {
					mLogger.error(adapterName + " has wrong device number in configuration. ");
					return;
				}
				mLogger.info(adapterName + " has  _numDevices = " + _numDevices);
				
				List<Map<String, String>> deviceList = dbExecute.getDeviceList();
				System.out.println(adapterName + " has _port " + _port + ", _query_timeout " + _query_timeout + ", _numDevices " + deviceList.size());
				if (deviceList.size() > 0) {
					String device_category = null;
					String device_address = null;
					String device_networNumber = null;
					String device_networkAddress = null;
					String device_instanceid = "-1";
					String device_alternativeid = "-1";
					BacnetDevice device = null;
					_numDevices = deviceList.size();
					for (int i = 0; i < _numDevices; i++) {
						Map<String, String> deviceItem = deviceList.get(i);
						device_address = deviceItem.get("device_address");
						device_networNumber = deviceItem.get("device_network_number");
						device_networkAddress = deviceItem.get("device_network_address");
						device_category = deviceItem.get("device_category");
						device_instanceid = deviceItem.get("device_instanceid");
						device_alternativeid = deviceItem.get("device_alternativeid");
						
						Map<String, WrittingPoint> writtingDpMap = new LinkedHashMap<String, WrittingPoint>();
						Map<String, RealPoint> realDpMap = new LinkedHashMap<String, RealPoint>();
						Map<Integer, VirtualPoint> virtualDpMap = new LinkedHashMap<Integer, VirtualPoint>();
						List<Map<String, String>> deviceAtributes = dbExecute.getActivedNodeAttributes(device_address, device_instanceid);
						System.out.println("Information Device " + i + " : device_address " + device_address + ", device_instanceid "
								+ device_instanceid + ", device_alternativeid " + device_alternativeid + ", device_category " + device_category);
						if (deviceAtributes.size() > 0) {
							for (Map<String, String> attribute : deviceAtributes) {
								DataPoint dp;
								int dpid = Integer.parseInt(attribute.get("data_point"));
								String dpType = attribute.get("type");
								if ("setting".equals(dpType)) {
									WrittingPoint writtingDp = new WrittingPoint();
									writtingDp.initializeObject(attribute);
									writtingDpMap.put(writtingDp.objectIdentifierName, writtingDp);
									dp = writtingDp;
								} else {
									if (!attribute.get("formula").isEmpty()) {
										VirtualPoint virutalDP = new VirtualPoint();
										virutalDP.initializeObject(attribute);
										virtualDpMap.put(dpid, virutalDP);
										dp = virutalDP;
									} else {
										RealPoint realDP = new RealPoint();
										realDP.initializeObject(attribute);
										realDpMap.put(realDP.objectIdentifierName, realDP);
										dp = realDP;
										if (dummyData) {
											try {
												ArrayList<Double> valueList = new ArrayList<Double>();
												valueList.add(Double.parseDouble(hiddenSettings.getProperty("device_" + device_instanceid + "_"
														+ dp.channelId + "_min")));
												valueList.add(Double.parseDouble(hiddenSettings.getProperty("device_" + device_instanceid + "_"
														+ dp.channelId + "_max")));
												dummyDataRange.put(dp.channelId, valueList);
											} catch (Exception e) {
												mLogger.error(adapterName + " does not set dummy data for device " + device_instanceid
														+ " , channel " + dp.channelId);
											}
										}
									}
								}
								Map<String, String> validationRules = dbExecute.getValidationRuleByDataPoint(dpid);
								if (!validationRules.isEmpty()) {
									dp.condition = validationRules.get("condition");
									dp.action = validationRules.get("action");
								}
								System.out.println(dp);
							}
							// gkRemoteDeviceList.add(remoteBacnetDevice);
						}
						device = new BacnetDevice(device_address, device_networNumber, device_networkAddress, device_instanceid,
								device_alternativeid, device_category);
						device.setNodeData(realDpMap, virtualDpMap, writtingDpMap);
						device.setQueryingtTimeout(_query_timeout);
						device.setMaxOiPerQuery(maxOiPerQuery);
						device.setMaxChannelPerMsg(maxChannelPerMsg);
						device.setLocalDevice(localdevice);
						queryDevicelist.add(device);

					}
				} else {
					mLogger.info("No any devices are connected to " + adapterName);
					return;
				}
			} else {
				adapterSettings = new HashMap<String, String>();
				adapterSettings.put("port", String.valueOf(_port));
				adapterSettings.put("query_timeout", String.valueOf(_query_timeout));
				dbExecute.insertAdapterSettings(adapterSettings);
				return;
			}

			final Calendar calendar = Calendar.getInstance();
			delay_start_query = delay_start_query + (60 - calendar.get(Calendar.SECOND));
			
			mLogger.info("-------------delay_start_query = " + delay_start_query);
			// initialize();
			tickTimer.schedule(new TimerTask() {
				public void run() {
					try {
						mLogger.info("-----Adapter queries Bacnet Device  at time : minute=" + calendar.get(Calendar.MINUTE) + "; second=" + calendar.get(Calendar.SECOND));
						onTick();
					} catch (Exception e) {
						mLogger.error("Exception " , e);
					}
				}
			},1000* delay_start_query, tickPeriod);

			mLogger.info(adapterName + " is started! with = " + tickPeriod);
		} catch (Exception e) {
			mLogger.error("Exception " , e);
		}
		
		
	}

	protected void deactivator() {
		stopLocalDevice();
		mLogger.info(adapterName + " is stopped. ");
	}

	synchronized public String getMetaData(Integer time) {
		String msg = "ADAPTER=" + adapterClass + ",VERSION=" + bundleVer + ";";
		/*** real data ***/
		for (int i = 0; i < queryDevicelist.size(); i++) {
			mLogger.info("getMetaData device:" + i);
			BacnetDevice dev = queryDevicelist.get(i);
			msg += "DEVICEID=" + dev.getCategory() + "-" + dev.getIdSentToServer() + ",STATUS=" + dev.getDeviceStatus() + ";";
		}

		return msg;
	}

	synchronized public ArrayList<String> getData(Integer measureTime) throws Exception {
		if (dummyData) {
			return getDummyData();
		}
		ArrayList<String> msg = new ArrayList<String>();
		for (int i = 0; i < queryDevicelist.size(); i++) {
			if (adapterMode != STORED_DATA_MODE) {
				mLogger.info("Mode:" + adapterMode);
				return msg;
			}
			List<String> result = queryDevicelist.get(i).getDeviceData();
			msg.addAll(result);
		}
		
		return msg;
	}

	private void onTick() {
		if (!dummyData) {
			doBACnetQuery();
		}

		if (adapterMode != STORED_DATA_MODE) {
			if ((System.currentTimeMillis() - interval60Seconds) >= 60000) {
				nonStoredDataModeNonActiveCounter++;
				if (nonStoredDataModeNonActiveCounter > MAX_NON_ACTIVE_COUNT) {
					adapterMode = STORED_DATA_MODE;
					nonStoredDataModeNonActiveCounter = 0;
				}
			}
		}
	}

	/**
	 * Initialize the BACnet LocalDevice
	 */
	public void startLocalDevice() {
		if (Util.osName().equals("linux")) {
			String ip = findLocalIP();
			mLogger.info("Current IP address : " + ip);
			localdevice = new LocalDevice(1234, ip);
		} else {
			InetAddress hostip = null;
			try {
				hostip = InetAddress.getLocalHost();
				mLogger.info("Current IP address : " + hostip.getHostAddress());
			} catch (UnknownHostException e) {
				mLogger.error("UnknownHostException" , e);
				return;
			}
			localdevice = new LocalDevice(1234, hostip.toString());
		}
		localListen = new DefaultDeviceEventListener() {
			public void iAmReceived(RemoteDevice d) {
				mLogger.info("[DefaultDeviceEventListener] Found name:" + d.getName() + ", instance:" + d.getInstanceNumber() + ",network:"
						+ d.getAddress().toIpString() + ",network:" + d.getNetwork());

				GKRemoteDevice temp = new GKRemoteDevice(d.getInstanceNumber(), d.getAddress(), d.getNetwork());
				int index = scanedRemoteDeviceList.indexOf(temp);
				if (index == -1) {
					mLogger.info("[DefaultDeviceEventListener] add device into list , size = " + scanedRemoteDeviceList.size());
					scanedRemoteDeviceList.add(temp);
				}
				else {
					mLogger.info("[DefaultDeviceEventListener] device exists in list");
				}
			}
		};
		
		try {
			localdevice.initialize();
		} catch (IOException e) {
			mLogger.error("IOException", e);
			return;
		}
	}

	/**
	 * Terminate the BACnet LocalDevice
	 */
	public void stopLocalDevice() {
		try {
			Thread.sleep(100);
			localdevice.terminate();
		} catch (Exception e) {
			localdevice.terminate();
		}
	}

	public String findLocalIP() {
		String ip = null;
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				// filters out 127.0.0.1 and inactive interfaces
				if (iface.isLoopback() || !iface.isUp())
					continue;

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					ip = addr.getHostAddress();
					mLogger.info(iface.getDisplayName() + " " + ip);
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		return ip;
	}

	public byte[] findLocalIPBytes() {
		byte[] ip = new byte[4];
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				// filters out 127.0.0.1 and inactive interfaces
				if (iface.isLoopback() || !iface.isUp())
					continue;

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					ip = addr.getAddress();
					// mLogger.info(iface.getDisplayName() + " " + ip);
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		return ip;
	}

	public void findBacnetDevice() {
		// get local network

		byte[] ip = new byte[4];
		if (_bacnetBroadCastMode == 0) {
			// ip[0] = (byte) 192; // A
			// ip[1] = (byte) 168; // B
			// ip[2] = (byte) 1; // C
			// ip[3] = (byte) 255; // D
			if (Util.osName().equals("linux")) {
				ip = findLocalIPBytes();
			} else {
				InetAddress hostip = null;
				try {
					hostip = InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					mLogger.error("UnknownHostException", e);
					return;
				}
				ip = hostip.getAddress();
			}
			// ip[0] = (byte) 1; // A
			// ip[1] = (byte) 1; // B
			// ip[2] = (byte) 1; // C
			ip[3] = (byte) 255;// set to broadcast address
		} else {
			ip[0] = (byte) 255; // A
			ip[1] = (byte) 255; // B
			ip[2] = (byte) 255; // C
			ip[3] = (byte) 255; // D
		}
		WhoIsRequest whois = new WhoIsRequest();
		String ipstr = IpAddressUtils.toIpString(ip);
		mLogger.info("Broadcast address : " + ipstr);
		// System.out.println(ipstr);
		localdevice.getEventHandler().addListener(localListen);
		localdevice.setBroadcastAddress(ipstr);
		try {
			localdevice.sendBroadcast(whois);
		} catch (BACnetException e) {
			mLogger.error("BACnetException" + " at " + ipstr, e);
		}
	}

	synchronized public void doBACnetQuery() {
		for (int i = 0; i < queryDevicelist.size(); i++) {
			try {
				if (multiPropertiesAccess) {
					queryDevicelist.get(i).queryingConcurrenceBACnetRemoteDevice();
				} else {
					queryDevicelist.get(i).queryingSequenceBACnetRemoteDevice();
				}
			} catch (Exception e) {
				mLogger.error("Exception", e);

			}
		}
	}


	@Override
	public int getAdapterType() {
		// TODO Auto-generated method stub
		return BACNET_TYPE;
	}

	private Properties getHiddenConfigurations(String filename) {
		return Util.getPropertiesFile(filename);
	}

	@Override
	synchronized public List<Map<String, String>> getRealTimeData(String address, String instanceId, String dataindex, String length) {
		List<Map<String, String>> realTimeData = new ArrayList<Map<String, String>>();
		try {
			int index = Integer.parseInt(dataindex);
			int len = Integer.parseInt(length);
			BacnetDevice device = getBacnetDeviceByInstanceId(address, Integer.parseInt(instanceId));
			System.out.println("----nodeType = " + webConsoleNodeType + " - address=" + address + " - instanceId = " + instanceId + "- dataindex=" + dataindex
					+ " - length = " + length);
			
			List<ReadingPoint> dpList = device.getReadingDatapointList(webConsoleNodeType, index, len);
			for (int i = 0; i < dpList.size(); i++) {
				Map<String, String> deviceData = new HashMap<String, String>();
				deviceData.put("idx_" + (i + 1), String.valueOf((index + i)));
				deviceData.put("name_" + (i + 1), dpList.get(i).name);
				deviceData.put("measure_" + (i + 1), dpList.get(i).measureName);
				deviceData.put("value_" + (i + 1), vformat.format(dpList.get(i).measureValue));
				deviceData.put("unit_" + (i + 1), dpList.get(i).measureUnit);
				deviceData.put("channel_" + (i + 1), String.valueOf(dpList.get(i).channelId));
				if (webConsoleNodeType == USED_REAL_READING_NODE) {
					deviceData.put("oi_" + (i + 1), ((RealPoint)dpList.get(i)).objectIdentifierName);

				}
				realTimeData.add(deviceData);
			}

		} catch (Exception e) {
			mLogger.error("Exception" , e);
		}
		return realTimeData;
	}

	@Override
	public List<Map<String, String>> scanDevice(boolean scanning) {
		// Simulation Data
		List<Map<String, String>> deviceList = new ArrayList<Map<String, String>>();

		try {   			
    		if (!scanning) {
    			isDeviceScanning = false;
    			lenScanedList = 0;
    			idxScanedList = 0;
    			localdevice.getEventHandler().removeListener(localListen);
    			scanedRemoteDeviceList.clear();
    			return deviceList;
    		}
			//System.out.println("-----------------------isDeviceScanning 0 = " + isDeviceScanning);

    		if (!isDeviceScanning) {
    			findBacnetDevice();
    			isDeviceScanning = true;
    		//	System.out.println("-----------------------isDeviceScanning = " + isDeviceScanning);
    		} else {
    			int currentLen =  scanedRemoteDeviceList.size();
    			mLogger.info("scanDevice : found " + (currentLen - lenScanedList) + " of " + currentLen + " devices");
    			lenScanedList = currentLen;

    			for (int ii = 0; ii < 5 && idxScanedList < lenScanedList; ii++) {
    				GKRemoteDevice ro = scanedRemoteDeviceList.get(idxScanedList);
    				Map<String, String> device = new HashMap<String, String>();
    				device.put("device_address", ro.getAddress().toIpString());
    				if (ro.getNetwork() != null) {
    					device.put("device_network_number", String.valueOf(ro.getNetwork().getNetworkNumber()));
    					device.put("device_network_address", ArrayUtils.toString(ro.getNetwork().getNetworkAddress()));
    				} else {
    					device.put("device_network_number", "0");
    					device.put("device_network_address", "[-1]");
    				}
    				try {
    					ReadPropertyRequest read = new ReadPropertyRequest(new ObjectIdentifier(ObjectType.device, ro.getInstanceNumber()),
    							PropertyIdentifier.objectName);
    					ReadPropertyAck ack = (ReadPropertyAck) localdevice.send(new InetSocketAddress(ro.getAddress().toIpString(), 0xBAC0),
    							ro.getNetwork(), 1476, Segmentation.segmentedBoth, read);
    					device.put("device_name", ack.getValue().toString());
    				} catch (BACnetException e) {
    					device.put("device_name", "");
    					mLogger.warn("BACnetException" , e);
    				}
    				device.put("device_category", "10000");
    				device.put("device_instanceid", String.valueOf(ro.getInstanceNumber()));
    				device.put("device_alternativeid", "-1");
    				deviceList.add(device);

    				idxScanedList++;
    			}
	
    		}

		}catch (Exception e){
			mLogger.error("Exception", e);
		}

		System.out.println("-----------------------device 1 = " + deviceList + " - idxScanedList = " + idxScanedList);

		return deviceList;

	}

	@Override
	public void scanDeviceObjectIdentifier(String address, String instanceid) {
		if (!dbExecute.deleteAllDeviceAttributes(address, instanceid)) {

		}
		List<Map<String, String>> deviceAttributes = new ArrayList<Map<String, String>>();
		boolean ok = true;
		if (ok) {
			int instance_id = Integer.parseInt(instanceid);
			Network dnwk = null;
			BacnetDevice dev = getBacnetDeviceByInstanceId(address, instance_id);
			if (dev != null) {
				GKRemoteDevice remoteDevice = dev.getGkRemoteDevice();
				remoteDevice.setQuering(true);
				try {
					dnwk = remoteDevice.getNetwork();
					mLogger.info("[scanDeviceObjectIdentifier] scanning Bacnet device for instance:" + instance_id + ";Add:" + address + ";Network:"
							+ dnwk);
					InetSocketAddress addr = new InetSocketAddress(address, 0xBAC0);

					// Get MaxApduLenthAccepted
					ReadPropertyRequest read = new ReadPropertyRequest(new ObjectIdentifier(ObjectType.device, instance_id),
							PropertyIdentifier.maxApduLengthAccepted);
					ReadPropertyAck ack = (ReadPropertyAck) localdevice.send(addr, dnwk, 1476, Segmentation.segmentedBoth, read);
					UnsignedInteger unsignint = (UnsignedInteger) ack.getValue();
					int apdumaxLength = unsignint.intValue();
					remoteDevice.setMaxAPDULengthAccepted(apdumaxLength);
					mLogger.info("[scanDeviceObjectIdentifier] MaxApduLengthAccepted:" + unsignint);

					// Get ObjectList
					read = new ReadPropertyRequest(new ObjectIdentifier(ObjectType.device, instance_id), PropertyIdentifier.objectList);
					ack = (ReadPropertyAck) localdevice.send(addr, dnwk, apdumaxLength, Segmentation.segmentedBoth, read);
					@SuppressWarnings("unchecked")
					SequenceOf<ObjectIdentifier> objectList = (SequenceOf<ObjectIdentifier>) ack.getValue();
					SequenceOf<PropertyReference> propertyDemands = new SequenceOf<PropertyReference>();
					propertyDemands.add(new PropertyReference(PropertyIdentifier.objectName));
					propertyDemands.add(new PropertyReference(PropertyIdentifier.units));

					for (ObjectIdentifier oid : objectList) {
						mLogger.info("[scanDeviceObjectIdentifier] Found Object Identifier  " + oid);

						Map<String, String> attribute = new HashMap<String, String>();
						String oiname = mapOIBacnetToOIAdapter(oid);
						attribute.put("object_identifier", oiname);
						if (oiname == null) {
							continue;
						}

						Encodable encode = null;
						PropertyIdentifier pid = null;
						for (PropertyReference property : propertyDemands) {
							try {
								pid = property.getPropertyIdentifier();
								read = new ReadPropertyRequest(oid, pid);
								ack = (ReadPropertyAck) localdevice.send(addr, dnwk, apdumaxLength, Segmentation.segmentedBoth, read);
								encode = (Encodable) ack.getValue();
								mLogger.info("[scanDeviceObjectIdentifier] ReadPropertyAck  " + ack);

								if (pid.equals(PropertyIdentifier.objectName)) {
									attribute.put("oi_measure_name", encode.toString());
									continue;
								}

								if (pid.equals(PropertyIdentifier.units)) {
									attribute.put("oi_measure_unit", encode.toString());
									continue;
								}
							} catch (BACnetException e) {
								mLogger.warn("BACnetException", e);
							}
						}
						deviceAttributes.add(attribute);
					}

				} catch (Exception e) {
					mLogger.error("Exception", e);
				}
				remoteDevice.setQuering(false);
			}

		}

		dbExecute.InsertDeviceAttributes(address, instanceid, deviceAttributes);
	}

	public BacnetDevice getBacnetDeviceByInstanceId(String address, int instanceId) {
		for (BacnetDevice item : queryDevicelist) {
			if ((item.getAddress().equals(address)) && (item.getInstanceid() == instanceId)) {
				return item;
			}

		}
		return null;
	}
	
	public BacnetDevice getBacnetDeviceByInstanceId(int instanceId) {
		for (BacnetDevice item : queryDevicelist) {
			if (item.getInstanceid() == instanceId) {
				return item;
			}
		}
		return null;
	}

	public BacnetDevice getBacnetDeviceByDelegatedId(int delegatedId) {
		if (delegatedId == -1) {
			return null;
		}
		
		for (BacnetDevice item : queryDevicelist) {
			if (item.getAlternativeid() == delegatedId) {
				return item;
			}

		}
		return null;
	}
	
	public int findDataPointId(int delegatedId, int channel) {
		
		return -1;
	}
	public String mapOIBacnetToOIAdapter(ObjectIdentifier oi) {
		if (oi != null) {
			if (ObjectType.analogInput.intValue() == oi.getObjectType().intValue()) {
				return "ai" + oi.getInstanceNumber();
			}
			if (ObjectType.analogOutput.intValue() == oi.getObjectType().intValue()) {
				return "ao" + oi.getInstanceNumber();
			}
			if (ObjectType.analogValue.intValue() == oi.getObjectType().intValue()) {
				return "av" + oi.getInstanceNumber();
			}
			if (ObjectType.binaryInput.intValue() == oi.getObjectType().intValue()) {
				return "bi" + oi.getInstanceNumber();
			}
			if (ObjectType.binaryOutput.intValue() == oi.getObjectType().intValue()) {
				return "bo" + oi.getInstanceNumber();
			}
			if (ObjectType.binaryValue.intValue() == oi.getObjectType().intValue()) {
				return "bv" + oi.getInstanceNumber();
			}
			if (ObjectType.accumulator.intValue() == oi.getObjectType().intValue()) {
				return "ac" + oi.getInstanceNumber();
			}
		}
		return null;
	}

	@Override
	public List<Map<String, String>> getNodeScanningPage(String deviceAdress, String instanceId, String indexPage, int paging,String type) {
		return dbExecute.getNodeScanningPage(deviceAdress, instanceId, indexPage, paging,type);
	}

	@Override
	public boolean updateDeviceAttributes(String address, String deviceInstance, List<Map<String, String>> attributes) {
		return dbExecute.updateDeviceAttributes(address, deviceInstance, attributes);
	}
	
	@Override
	public int numOfNode(String deviceAdress, String instanceId, int nodeType,String type) {
		if (nodeType == SCANNING_NODE) {
			return dbExecute.numberOfRealNode(deviceAdress, instanceId,type);
		}
		
		if (nodeType == REAL_READING_NODE) {
			return dbExecute.numberOfReadNode(deviceAdress, instanceId,type);
		}
		
		if (nodeType == USED_REAL_READING_NODE) {
			BacnetDevice dev = getBacnetDeviceByInstanceId(deviceAdress, Integer.parseInt(instanceId));
			return dev.getRealDatapointIdList().size();
		}
		
		if (nodeType == VIRTUAL_READING_NODE) {
			return dbExecute.numberOfVirtualNode(deviceAdress, instanceId,type);
		}
		
		if (nodeType == USED_VIRTUAL_READING_NODE) {
			BacnetDevice dev = getBacnetDeviceByInstanceId(deviceAdress, Integer.parseInt(instanceId));
			return dev.getVirtualDatapointIdList().size();
		}
		
		if (nodeType == WRITTING_NODE) {
			return dbExecute.numberOfWriteNode(deviceAdress, instanceId);
		}
		
		if (nodeType == USED_WRITTING_NODE) {
			return 0;
		}
		
		return 0;
	}

	@Override
	public List<Map<String, String>> getVirtualNodeReadingPage(String deviceAdress, String instanceId, String indexPage, int paging) {
		List<Map<String, String>> virtualNodeList = dbExecute.getVirtualNodeReadingPage(deviceAdress, instanceId, indexPage, paging);
//		List<Map<String, String>> realNodeList = dbExecute.getRealNodeAttributes(deviceAdress, instanceId, "1", Integer.MAX_VALUE);
//		Map<String, String> convertedName = new HashMap<String, String>();
//
//		for (Map<String, String> realItem : realNodeList) {
//			String nameKey = "N" + realItem.get("data_point");
//			String nameValue = realItem.get("measure_name") + "[" + realItem.get("object_identifier") + "]";
//			convertedName.put(nameKey, nameValue);
//		}
//		for (Map<String, String> virtualItem : virtualNodeList) {
//
//			String expr = virtualItem.get("formula");
//			if (expr == null) {
//				continue;
//			}
//			virtualItem.remove("formula");
//			GKEP parser = new GKEP();
//			parser.setAllowUndeclared(true);
//			parser.parseExpression(expr);
//			SymbolTable symbols = parser.getSymbolTable();
//			for (Iterator<?> iter = symbols.keySet().iterator(); iter.hasNext();) {
//				String key = (String) iter.next();
//				String value = convertedName.get(key);
//				if (value != null) {
//					expr = expr.replaceAll("\\b" + key + "\\b", value);
//				}
//			}
//			virtualItem.put("formula", expr);
//
//		}
		return virtualNodeList;
	}

	public boolean deleteDeviceAttribute(String address, String instanceId, List<String> dataPoints) {
		return dbExecute.deleteDeviceAttributes(address, instanceId, dataPoints);
	}

	@Override
	public boolean insertValidationRule(String deviceIntanceId, String deviceAddress, String expressions, String action, String dataPoint) {
		return dbExecute.insertValidationRule(deviceIntanceId, deviceAddress, expressions, action, dataPoint);
	}

	@Override
	public List<Map<String, String>> getValidationRule(String instanceId, String deviceAddress) {
		//List<Map<String, String>> NodeList = dbExecute.getAllAttributes(deviceAddress, instanceId);
		// dbservice.getRealNodeAttributes(deviceAddress, instanceId, "0", Integer.MAX_VALUE);
		/*List<Map<String, String>> realNodeList = dbExecute.getRealNodeAttributes(deviceAddress, instanceId, "1", Integer.MAX_VALUE);*/
		List<Map<String, String>> validationRuleList = dbExecute.getValidationRule(instanceId, deviceAddress);
		Map<String, String> convertedName = new HashMap<String, String>();
		/*System.out.println("getValidationRule : AllNodeList = " + NodeList);*/
		/*System.out.println("getValidationRule : realNodeList = " + realNodeList);*/

//		for (Map<String, String> nodeItem : NodeList) {
//			String nameKey = "N" + nodeItem.get("data_point");
//			String nameValue = null;
//			if (!nodeItem.get("formula").isEmpty()) {
//				nameValue = nodeItem.get("name") + "[" + nodeItem.get("measure_name") + "]";
//			} else {
//				nameValue = nodeItem.get("name") + "[" + nodeItem.get("measure_name") + "]";
//			}
//			convertedName.put(nameKey, nameValue);
//		}

		for (Map<String, String> Item : validationRuleList) {

			//String expr1 = Item.get("condition");
			String expr2 = Item.get("action");
			GKEP parser = new GKEP();
			parser.setAllowUndeclared(true);
//			if (expr1 != null) {
//				parser.parseExpression(expr1);
//				Item.remove("condition");
//			}

			if (expr2 != null) {
				parser.parseExpression(expr2);
				Item.remove("action");
			}

			SymbolTable symbols = parser.getSymbolTable();
			for (Iterator<?> iter = symbols.keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				String value = convertedName.get(key);
				if (value != null) {
//					if (expr1 != null) {
//						expr1 = expr1.replaceAll("\\b" + key + "\\b", value);
//					}
					if (expr2 != null) {
						expr2 = expr2.replaceAll("\\b" + key + "\\b", value);
					}

				}
			}

//			if (expr1 != null) {
//				Item.put("condition", expr1);
//			}

			if (expr2 != null) {
				Item.put("action", expr2);
			}

		}
		return validationRuleList;
	}

	@Override
	public boolean deleteValidation(String vadationId) {
		return dbExecute.deleteValidation(vadationId);
	}

	@Override
	public boolean insertDeviceAttributes(String address, String deviceInstance, List<Map<String, String>> attributes) {
		return dbExecute.InsertDeviceAttributes(address, deviceInstance, attributes);
	}

	@Override
	public Map<String, String> getValidationRuleById(String instanceId, String deviceAddress, String validationId) {
		List<Map<String, String>> list = dbExecute.getValidationRule(instanceId, deviceAddress);
		for (Map<String, String> map : list) {
			if (map.get("id").equals(validationId))
				return map;
		}
		return null;
	}

	@Override
	public boolean updateValidationRule(String expressions, String action, String id) {
		return dbExecute.updateValidationRule(expressions, action, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	synchronized public boolean setNodeValue(String category, String instanceId, Object deviceData) {
		BacnetDevice dev = this.getBacnetDeviceByInstanceId(Integer.valueOf(instanceId));
		if (dev != null) {
			Map<String, String> datapoints = (Map<String, String>)deviceData;
			return dev.setDataPointValue(datapoints);
		}
		return false;
	}


	@Override
	synchronized public boolean setDefaultValue() throws Exception {
		for (BacnetDevice item : queryDevicelist) {
			item.setDataPointDefaultValue();
		}
		return true;
	}
	
	@Override
	public boolean checkUniqueObjectIdentifier(String objectIdentifier,String deviceInstanceId, String deviceAddress) {
		return dbExecute.checkUniqueObjectIdentify(objectIdentifier,deviceInstanceId,deviceAddress);
	}

	@Override
	public boolean updateDeviceAttributesScanning(String address,
			String deviceInstance, List<Map<String, String>> attributes) {
		return dbExecute.updateDeviceAttributesScanning(address, deviceInstance, attributes);
	}

	@Override
	public List<Map<String, String>> getRealNodeReadingPage(String deviceAdress,
			String instanceId, String indexPage, int paging) {
		return dbExecute.getRealNodeReadingPage(deviceAdress, instanceId, indexPage, paging);
	}

	@Override
	public List<Map<String, String>> getNodeWrittingPage(
			String deviceAdress, String instanceId, String indexPage, int paging) {
		return dbExecute.getNodeWrittingPage(deviceAdress, instanceId, indexPage, paging);
	}

	@Override
	public boolean updateDeviceAttributesConfiguration(String address,
			String deviceInstance, List<Map<String, String>> attributes) {
		return dbExecute.updateDeviceAttributesConfiguration(address, deviceInstance, attributes);
	}
	
	private ArrayList<String> getDummyData() {
		ArrayList<String> msg = new ArrayList<String>();
		for (int i = 0; i < queryDevicelist.size(); i++) {
			if (adapterMode != STORED_DATA_MODE) {
				mLogger.info("Mode:" + adapterMode);
				return msg;
			}
			BacnetDevice querydevice = queryDevicelist.get(i);
			GKRemoteDevice remoteBacnetDevice = querydevice.getGkRemoteDevice();
			long timeStamp = System.currentTimeMillis();
			int instanceid = querydevice.getInstanceid();
			
			if (instanceid != remoteBacnetDevice.getInstanceNumber()) {
				mLogger.error(" query device:" + instanceid + " and remote device:" + remoteBacnetDevice.getInstanceNumber()
						+ "are different");
				continue;
			}

			// online case
			mLogger.info("Create dummy data at " + i + " for device " + instanceid + " that is online !");

			Map<String, RealPoint> realDpMap = querydevice.getRealDatapointMap();
			Map<Integer, VirtualPoint> virtualDpMap = querydevice.getVirtualDatapointMap();
			int msgCount = 0;
			StringBuilder dataBuffer = new StringBuilder();

			for (String realKey : realDpMap.keySet()) {
				if (((msgCount % maxChannelPerMsg) == 0) && (msgCount != 0)) {
					msg.add(dataBuffer.toString());
					dataBuffer = new StringBuilder();
				}
				RealPoint realDP = realDpMap.get(realKey);
				StringBuilder message = new StringBuilder();
				mLogger.debug("Time from bacnet device:" + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(timeStamp)));
				message.append("|DEVICEID=" + querydevice.getCategory() + "-" + querydevice.getIdSentToServer() + "-" + realDP.channelId + "-0");
				message.append(";TIMESTAMP=" + timeStamp);

				double valueOfDataOI = 0;
				if (dummyDataRange.containsKey(realDP.channelId)) {
					ArrayList<Double> range = dummyDataRange.get(realDP.channelId);
					valueOfDataOI = FuncUtil.randomWithRange(range.get(0),range.get(1));
					valueOfDataOI = valueOfDataOI * realDP.measureRatio;
				} else {
					valueOfDataOI = realDP.channelId * realDP.measureRatio;
				}
				message.append(";" + realDP.measureName + "=" + vformat.format(valueOfDataOI) + ","
						+ realDP.measureUnit);
				if (realDP.hasConsumption) {
					if (realDP.measureValue <= 0) {
						realDP.consumptionValue = 0;
					} else if (valueOfDataOI >= realDP.measureValue) {
						realDP.consumptionValue = (valueOfDataOI - realDP.measureValue) * realDP.consumptionRatio;
					} else {
						realDP.consumptionValue = 0;
					}

					message.append(";" + realDP.consumptionName + "="
							+ vformat.format(realDP.consumptionValue) + "," + realDP.consumptionUnit);
				}
				realDP.measureValue = valueOfDataOI;

				msgCount++;
				dataBuffer.append(message);
			}
			
			for (Integer virtualKey : virtualDpMap.keySet()) {
				if (((msgCount % maxChannelPerMsg) == 0) && (msgCount != 0)) {
					msg.add(dataBuffer.toString());
					dataBuffer = new StringBuilder();
				}
				VirtualPoint virtualDP = virtualDpMap.get(virtualKey);
				List<String> oiNameList = virtualDP.variables;
				GKEP parser = virtualDP.parser;
				for (String oiName : oiNameList) {
					RealPoint dependingDP = realDpMap.get(oiName);
					double val = 0;
					if (dependingDP != null) {
						val = dependingDP.measureValue;
					} else {
						if (oiName.contains("consumption")){
							String[] var = oiName.split("_");
							if (var.length == 2) {
								dependingDP = realDpMap.get(var[1]);
								if (dependingDP != null) {
									val = dependingDP.consumptionValue;
								}
							}
						} 
					}
					parser.addVariable(oiName, val);
				}
				
				double virtualDPValue = parser.getValue();
				virtualDP.measureValue = virtualDPValue * virtualDP.measureRatio;

				StringBuilder message = new StringBuilder();
				mLogger.debug("Time from bacnet device:"
						+ new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(timeStamp)));
				mLogger.debug(virtualDP.toString());
				message.append("|DEVICEID=" + querydevice.getCategory() + "-" + querydevice.getIdSentToServer() + "-"
						+ virtualDP.channelId + "-0");
				message.append(";TIMESTAMP=" + timeStamp);
				message.append(";" + virtualDP.measureName + "=" + vformat.format(virtualDP.measureValue) + ","
						+ virtualDP.measureUnit);
				msgCount++;
				dataBuffer.append(message);
			}

			msg.add(dataBuffer.toString());
		}
		
		return msg;
	}

}
