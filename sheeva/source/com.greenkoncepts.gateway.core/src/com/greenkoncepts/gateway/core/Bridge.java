package com.greenkoncepts.gateway.core;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greenkoncepts.gateway.api.adapter.ABacnetAdapter;
import com.greenkoncepts.gateway.api.adapter.Adapter;
import com.greenkoncepts.gateway.watchdog.DeadlockDetector;

public abstract class Bridge {
	static public int SOCKET = 0;
	static public int MQTT = 1;
	protected int bridgeErrorMax = 0;
	protected ArrayList<Adapter> adapterServices;
	protected ArrayList<DeadlockDetector> deadlockServices;
	protected Object dataSync;
	protected Object metadataSync;
	protected String metamsg;

	protected String customerId = "GKC";
	protected String gatewayId = "";
	protected String protocolVer = "1.8";
	protected String remoteHost ;
	protected int remotePort;
	protected int sendingProtocol = 0;

	protected long 		uptime;
	protected String	bundleName;
	protected String	_configFile;

	protected int delay_start_query = 5;
	protected int delay_start_internet = 90;
	protected int tickPeriod_query = 60;
	protected int tickPeriod_internet = 60;
	protected int tickPeriod_status = 60;
	
	protected ArrayList<String> msgbuf;
	protected ArrayList<String> msgbuf_storage;
	protected int bufferSendLimit;
	protected Logger mLogger;
	protected DataBuffer bufferManagement;
	
	protected int bridgeError = 0;
	protected int bridgeErrorPerMinute = 0;
	protected int bridgeErrorPer24Hours = 0;
	/*private String recordingData = "RecordingData.prop";*/
	protected ExecutiveDatabaseImp dbExecute;

	
	public Bridge(ArrayList<Adapter> adapters, ArrayList<DeadlockDetector> deadlockDetector, int protocol){
		adapterServices = adapters;
		deadlockServices = deadlockDetector;
		dataSync = new Object();
		metadataSync = new Object();
		msgbuf = new ArrayList<String>();
		msgbuf_storage = new ArrayList<String>();
		bufferSendLimit = 80000; // Max bytes (100k) can be sent in one shot
		bundleName = getClass().getSimpleName();
		_configFile = bundleName + ".prop";
		bufferManagement = new DataBuffer();
		mLogger = LoggerFactory.getLogger(getClass().getSimpleName());
		uptime=0;
		sendingProtocol = protocol;
		/*Properties record = Util.getPropertiesFile(recordingData);
		if (!record.isEmpty()) {
			totalReadingData = Long.parseLong(record.getProperty("totalReadingData"));
			totalSentData = Long.parseLong(record.getProperty("totalSentData"));
		}*/

	}
	
	abstract public void start();
	abstract public void stop();
	abstract public boolean sendMessage(String msg);
	abstract public void checkBridgeStatus();
	abstract public boolean adapterSendDeviceState(String adapter, String data);
	abstract public boolean adapterSendMetaData(String Adapter, String Data);
	abstract public boolean adapterSendData(String Adapter, String Data);

				
	///////////////////////
	public void initSettings(Map<String, String> settings) {
		mLogger.info("[initSettings] should be implemented by derived class");
		Map<String,String > gateWayStatus = dbExecute.getGatewayStatus() ;
		totalReadingData = Long.valueOf(gateWayStatus.get("total_reading_data"));
		totalSentData    = Long.valueOf(gateWayStatus.get("total_sent_data")) ;
	}
	
	void getMetadata() {
		mLogger.info("getMetadata");
		StringBuffer _metadata = new StringBuffer("PROTOCOL=" + protocolVer + ";GATEWAY=" + customerId + "-" + gatewayId + ";UPTIME=" + uptime + ";");
		try {
			if (adapterServices != null) {
				Iterator<Adapter> it = adapterServices.iterator();
				while (it.hasNext()) {
					Adapter a = it.next();
					String s = a.getMetaData(null);
					if (s != null) {
						_metadata.append(s);
					}
				}
			}
			_metadata.append("\n");

			synchronized (metadataSync) {
				metamsg = _metadata.toString();
			}
		} catch (Exception e) {
			mLogger.error("Exception", e);
		}
	}

	void getData() {
		mLogger.info("getData");
		ArrayList<String> _data = new ArrayList<String>();
		try {
			if (adapterServices != null) {
				Iterator<Adapter> it = adapterServices.iterator();
				while (it.hasNext()) {
					Adapter a = it.next();
					ArrayList<String> s = a.getData(null);
					if (s != null) {
						try {
							mLogger.debug("Add " + s.size() + " fields from adapter into master array(already have " + _data.size()
									+ " fields inside)...");
							_data.addAll(s);
						} catch (NullPointerException e) {
							mLogger.error("NullPointerException", e);
						}
					}
				}
			}

			synchronized (dataSync) {
				msgbuf.clear();
				if (_data.isEmpty()) {
					msgbuf.add(0, "|ERROR=No data found\n");
				}
				else {
					msgbuf.addAll(_data);
				}
				
				readDataInLastMinute = 0;
				for (String item : msgbuf) {
					readDataInLastMinute = readDataInLastMinute + item.length();
				}
				totalReadingData = totalReadingData + readDataInLastMinute;
			}
		} catch (Exception e) {
			mLogger.error("Exception", e);
		}
	}
	

	public void doQueryingDevice() {
		mLogger.info("[doQueryingDevice] do querying data !");
		try {
			uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000 / 60;

			String s = "Checking AdapterServices";
			if (adapterServices == null || adapterServices.isEmpty())
				s += " ... NONE!";
			else {
				s += " (";
				for (int i = 0; i < adapterServices.size(); i++) {
					s += adapterServices.get(i).getAdapterName();
					if (i < adapterServices.size() - 1)
						s += ", ";
					else
						s += ") ... ";
				}
			}
			mLogger.info(s);

		} catch (Exception e) {
			mLogger.error("Exception", e);
		}
		getData();
		getMetadata();

		mLogger.info("[doQueryingDevice] Checking buffer size ... " + msgbuf.size());
		if (!deadlockServices.isEmpty()) {
			deadlockServices.get(0).pingDeadlock(bundleName, tickPeriod_query);
		} else {
			mLogger.error("[doQueryingDevice] Deadlock Detector is not active !");
		}

	}

	public void doCheckingStatus() {
		mLogger.info("[doCheckingStatus] do checking status !");
		checkBridgeStatus();
		bridgeErrorCounter();
		
	}

	public void doSendingMetadata() {
		synchronized (metadataSync) {
			if (metamsg == null) {
				metamsg = "Metadata Empty";
			}
			sendMessage(metamsg);
		}
	}
	
	public void doSendingData() {
		mLogger.info("[doSendingData] do sending data !");
		synchronized (dataSync) {
			boolean hadSentBuffer = false;
			String s = null;
			int last_idx = 0;
			sentDataInLastMinute = 0;
			try {
				while (!msgbuf.isEmpty()) {
					last_idx = msgbuf.size() - 1;
					s = msgbuf.get(last_idx);
					if ((sentDataInLastMinute + s.length()) > bufferSendLimit) {
						break;
					}

					if (sendMessage(s)) {
						msgbuf.remove(last_idx);
						mLogger.debug("[doSendingData] Successfully send,remove sent data out of buffer");
						sentDataInLastMinute += s.length();
					} else {
						mLogger.error("[doSendingData] can not send message data out ...");
						break;
						
					}
					mLogger.info("[doSendingData] sent " + sentDataInLastMinute
							+ " bytes. check sending data in NAND = "
							+ hadSentBuffer);

					if (msgbuf.isEmpty()) {
						ArrayList<String> loadMsgBuffer = bufferManagement
								.loadCurrentBuffer();
						if (!loadMsgBuffer.isEmpty()) {
							msgbuf.addAll(loadMsgBuffer);
							hadSentBuffer = true;
						}
					} else {
						mLogger.debug("[doSendingData]...remain data in the message buffer cache");
					}

					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						mLogger.error("InterruptedException", e);
					}
				}
			} catch (Exception e) {
				mLogger.error("===========================", e);
			}
			if (sendingProtocol == SOCKET) {
				sendMessage("|END\n");
			}
			totalSentData = totalSentData + sentDataInLastMinute;

			if (!msgbuf.isEmpty()) {
				msgbuf_storage.addAll(msgbuf);
			}
		}
	}
	
	public void doBufferingData() {
		synchronized (dataSync) {
			if (!msgbuf_storage.isEmpty()) {
				mLogger.info("[doBufferingData] do buffering data with data size = " + msgbuf_storage.size());
				bufferManagement.saveBufferData(msgbuf_storage, true);
				msgbuf_storage.clear();
			} else {
				mLogger.info("[doBufferingData] all memory data was sent out. No data is left !");
			}
		}
	}

	
	private int[] error4eachhour = new int[24];
	private long sentDataInLastMinute = 0;
	private long readDataInLastMinute = 0;
	private long totalReadingData = 0;
	private long totalSentData = 0;
	private int index = 0;
	private int defaultSettingCounter = 0;


	public void bridgeErrorCounter() {
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if (hour != index) {
			index = hour;
			error4eachhour[index] = 0;
		}
		error4eachhour[index] += bridgeErrorPerMinute;
		
		bridgeErrorPerMinute = 0;
		bridgeErrorPer24Hours = 0;
		for (int i = 0; i < 24; i++) {
			bridgeErrorPer24Hours += error4eachhour[i];
		}
		
		if (bridgeError > 0) {
			defaultSettingCounter++;
		} else {
			defaultSettingCounter = 0;
		}
		if ((defaultSettingCounter > 0) && ((defaultSettingCounter %30) == 0)) {
			if (adapterServices != null) {
				Iterator<Adapter> it = adapterServices.iterator();
				while (it.hasNext()) {
					Adapter a = it.next();
					if (a.getAdapterType() == Adapter.BACNET_TYPE) {
						try {
							((ABacnetAdapter) a).setDefaultValue();
						} catch (Exception e) {
							mLogger.error("Checking Connection Exception " , e);
						}
						break;
					}
				}
			}
		}

		mLogger.debug("bridgeErrorCounter(), connection close time per minute=" + bridgeErrorPerMinute + " and per 24 hrs =" + bridgeErrorPer24Hours);
		/*Properties saveRecordData = new Properties();*/
		
		dbExecute.updateGatewayStatus( totalReadingData, totalSentData);
		/*saveRecordData.setProperty("totalReadingData", String.valueOf(totalReadingData));
		saveRecordData.setProperty("totalSentData", String.valueOf(totalSentData));
		Util.setPropertiesFile(recordingData, saveRecordData);*/
	}
	
	public Map<String, Long> getBridgeStatus() {
		Map<String, Long> bridgeStatus = new HashMap<String, Long>();
		Map<String,String> map = dbExecute.getGatewayStatus() ;
		bridgeStatus.put("bridgeErrorPer24Hours", (long)bridgeErrorPer24Hours);
		bridgeStatus.put("readDataInLastMinute", readDataInLastMinute);
		bridgeStatus.put("totalReadingData", Long.valueOf(map.get("total_reading_data")));
		bridgeStatus.put("sentDataInLastMinute", sentDataInLastMinute);
		bridgeStatus.put("totalSentData", Long.valueOf(map.get("total_sent_data")));
		bridgeStatus.put("startTimeReading", Long.valueOf(map.get("start_time_reading")));
		bridgeStatus.put("startTimeSending", Long.valueOf(map.get("start_time_sending")));
		return bridgeStatus;
	}
	
	public void clearBufferStorage(String store) {
		bufferManagement.deleteAllBuffer(store);
	}
	
	public long getUsableStorage(String location) {
		if (DataBuffer.SD_STORAGE.equals(location)) {
			return bufferManagement.usableSpaceSdCard();
		}
		return bufferManagement.usableSpaceNand();
	}

	public void setDbExecute(ExecutiveDatabaseImp dbExecute) {
		this.dbExecute = dbExecute;
	}
	
	public boolean resetGatewayStatus(Long resestTotalReadData, Long resetTotalSentData) {
		if(resestTotalReadData != null){
			totalReadingData = 0;
		}
		if(resetTotalSentData != null){
			totalSentData = 0;
		}
		return dbExecute.updateGatewayStatus(resestTotalReadData, resetTotalSentData);
		 
	}
}
