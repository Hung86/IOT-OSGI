package com.greenkoncepts.gateway.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greenkoncepts.gateway.api.adapter.Adapter;
import com.greenkoncepts.gateway.api.bridge.BridgeMaster;
import com.greenkoncepts.gateway.api.database.DbService;
import com.greenkoncepts.gateway.api.task.ITaskExecute;
import com.greenkoncepts.gateway.core.mqtt.sub.Subscriber;
import com.greenkoncepts.gateway.util.Util;
import com.greenkoncepts.gateway.watchdog.DeadlockDetector;

public class BridgeMasterImp implements BridgeMaster{
	private ArrayList<Adapter> adapterServices = new ArrayList<Adapter>();
	private ArrayList<DeadlockDetector> deadlockServices = new ArrayList<DeadlockDetector>();
	private ITaskExecute taskExecute;
	private ExecutiveDatabaseImp dbExecute = new ExecutiveDatabaseImp();
	private Logger mLogger = LoggerFactory.getLogger(getClass().getSimpleName());
	private Subscriber mqttSub = null; ;
	
	private DbService dbService;
	Object adapterSync = new Object();
	Object deadlockSync = new Object();
	Bridge bridge;
	String bridgeMode = "socket";
	

	protected void activator() {
		if (dbService == null) {
			new Thread() {
				public void run() {
					while (dbService == null) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							mLogger.error("InterruptedException", e);
						}
						System.out.println("Bridge Master...waiting instance of database");
					}
					initialize();
				}
			}.start();
		} else {
			initialize();
		}
	}
	
	private void initialize() {
		System.out.println("Bridge Master...instance of database is available now");

		dbExecute.setDatabaseService(dbService);
		dbExecute.createBridge();
		dbExecute.createGatewayStatus();
		
		Map<String, String> bridgeSettings = getConfig();
		if("socket".equals(bridgeSettings.get("bridge_mode"))){
			bridge = new SocketBridge(adapterServices, deadlockServices);
		}else if("mqtt".equals(bridgeSettings.get("bridge_mode"))){
			bridge = new MqttClientBridge(adapterServices, deadlockServices);
		}
		
		if(bridge != null){
			bridge.setDbExecute(dbExecute);
			bridge.initSettings(bridgeSettings);
			bridge.start();
		}
		
		if (mqttSub == null) {
			try {
				mqttSub = new Subscriber("tcp://localhost:1883",
						MqttClient.generateClientId(),
						bridgeSettings.get("gateway_id")+"/#", this);
			} catch (MqttException e) {
				mLogger.error("MqttException", e);
			}
		}
	}
	
	protected void deactivator() {
		if(bridge != null){
			bridge.stop();
		}
		
		if (mqttSub != null) {
			mqttSub.disconnect();
		}
	}
	
	
	@Override
	public List<Adapter> getAdapterServices() {
		return adapterServices;
	}
	
	@Override
	public ITaskExecute getTaskExecute() {
		return taskExecute;
	}
	
	public void setTaskExecute(ITaskExecute T) {

		taskExecute = T;
	}

	public void unsetTaskExecute(ITaskExecute T) {
		taskExecute = null;
	}

	public void setDeadlockDetector( DeadlockDetector service) {
		synchronized(deadlockSync) {
			deadlockServices.add(service);
		}
	}
	
	public void unsetDeadlockDetector( DeadlockDetector service) {
		synchronized(deadlockSync) {
			if(deadlockServices.contains(service))
			{
				//remove adapter
				deadlockServices.remove(service);
			}
		}
	}
	
	public void setDbService(DbService db) {

		dbService = db;
	}

	public void clearDbService(DbService db) {
		dbService = null;
	}
	
	// Method will be used by DS to set the Adapter service
	public void setAdapter(Adapter service) {
		synchronized(adapterSync)
		{
			adapterServices.add(service);
		}
	}

	// Method will be used by DS to unset the Adapter service
	public void unsetAdapter(Adapter service) {
		synchronized(adapterSync)
		{
			if(adapterServices.contains(service))
			{
				//remove adapter
				adapterServices.remove(service);
			}
		}
	}

	
	private Map<String, String> getConfig() {
		Map<String,String> map = dbExecute.getCurrentBridgeSettings();
		System.out.println("-------------------map = " + map);
		if(map.size()==0){
			String gatewayId = Util.getMac();
			if (gatewayId == null) {
				gatewayId = "000000000000";
			}
			System.out.println("[BridgeMaster] create default config !");
			dbExecute.insertBridge("socket", "info", "GKC", gatewayId, "test.greenkoncepts.com",
					"4548", "1.8", "60", "60", "5", "0","300", "80000", "20", gatewayId,
					"test/GKC", "GKC-" + gatewayId + "/version=1.8", "true", "2", "false", "60",
					"60", "test.greenkoncepts.com", "","0");
			return dbExecute.getCurrentBridgeSettings();
		} 
		return map;
	}

	@Override
	public Map<String, Long> getBridgeStatus() {
		return bridge.getBridgeStatus();
	}

	@Override
	public String getBridgeName() {
		return bridge.getClass().getSimpleName();
	}

	public void clearBufferStorage(String store) {
		if(bridge != null){
			bridge.clearBufferStorage(store);
		}
	}

	@Override
	public void adapterSendDeviceState(String adapter, String data) {
		if(bridge != null){
			bridge.adapterSendDeviceState(adapter, data);
		}
	}

	@Override
	public Map<String, String> getGatewayStatus() {
		return dbExecute.getGatewayStatus();
	}

	@Override
	public Map<String, String> getCurrentBridgeSettings() {
		return dbExecute.getCurrentBridgeSettings();
	}

	@Override
	public Map<String, String> getBridgeSettingsBy(String mode) {
		return dbExecute.getBridgeSettingsBy(mode);
	}

	@Override
	public boolean updateBridgeSettings(Map<String, String> settings) {
		return dbExecute.updateBridgeSettings(settings);
	}

	@Override
	public long getUsableStorage(String location) {
		if(bridge != null){
			return bridge.getUsableStorage(location);
		}
		return 0;
	}

	@Override
	public boolean updateGatewayStatus(Long totalReadData, Long totalSentData) {
		if(bridge != null){
			return bridge.resetGatewayStatus(totalReadData, totalSentData);
		}
		return false;
	}

	@Override
	public int getSendingInterval() {
		if (bridge != null) {
			return bridge.tickPeriod_internet;
		}
		return -1;
	}

}
