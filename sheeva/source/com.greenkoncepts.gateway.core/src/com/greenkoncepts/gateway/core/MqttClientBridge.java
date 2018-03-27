package com.greenkoncepts.gateway.core;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import com.greenkoncepts.gateway.api.adapter.ABacnetAdapter;
import com.greenkoncepts.gateway.api.adapter.Adapter;
import com.greenkoncepts.gateway.watchdog.DeadlockDetector;
public class MqttClientBridge extends Bridge implements MqttCallback {

	private String mqttTopic = "test/GKC";
	private String mqttSubTopic = "GKC-xxxxxxxx/version=1.8";
	private String mqttPassword="8KlbaxXTVZuVsejRyjIoBGr3X3I";
	private String mqttUser="gkadmin";
	private String sslSocketPass ="noCandy4u";
	private int mqttQos = 2;
	private boolean mqttSsl = true;
	private int mqttKeepAliveInterval = 60;
	private int mqttConnectionTimeOut = 60;
	private boolean mqttCleanSession = false;
	private String mqttClientId = "";
	private long lastUpdatedMetadata = 0;
	private int metadataIntervalTime = 300;
	
	private String rest_host = "test.greenkoncepts.com";
	private String rest_port = "";
	private String rest_prefix = "ems/gatewayip?gateway=";
	private String rest_url = "";
	
	private MqttClient mqttClient;
	private String brokerUrl;
	private MqttConnectOptions 	conOpt;
	private MqttMessage mqttMessage;

	
	ScheduledExecutorService mqttBridgeThreads = Executors.newScheduledThreadPool(2);
	
	MqttClientBridge(ArrayList<Adapter> adapters, ArrayList<DeadlockDetector> deadlockDetector) {
		super(adapters, deadlockDetector, MQTT);
		remoteHost = "mqtttest.greenkoncepts.com";
		remotePort = 1883;
	}

	@Override
	public void connectionLost(Throwable arg0) {
		mLogger.error("connectionLost : " + arg0.getMessage());	
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		mLogger.info("deliveryComplete : " + arg0.getMessageId());	
	}

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		mLogger.info("messageArrived : arg0 = " + arg0 + ", message is  " + arg1.isDuplicate());		
	}

	@Override
	public void start() {
		try {
			loadRestSettings();
			configMqttClient();
			final Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			delay_start_query = delay_start_query + (60 - calendar.get(Calendar.SECOND));
			mLogger.info("------delay_start_query = " + delay_start_query +", tickPeriod_query = " + tickPeriod_query +", tickPeriod_internet = " + tickPeriod_internet);
			mLogger.info("------querying will start at time : minute=" + calendar.get(Calendar.MINUTE) + "; second=" + calendar.get(Calendar.SECOND));
			
			mqttBridgeThreads.scheduleAtFixedRate(new Runnable(){
				@Override
				public void run() {
					 doQueryingDevice();
				}		
			}
			, delay_start_query, tickPeriod_query, TimeUnit.SECONDS);
			
			mqttBridgeThreads.scheduleAtFixedRate(new Runnable(){
				@Override
				public void run() {
					doBufferingData();
					doCheckingStatus();
				}		
			}
			, 0, tickPeriod_status, TimeUnit.SECONDS);
			
			mqttBridgeThreads.scheduleAtFixedRate(new Runnable(){
				@Override
				public void run() {
					doSendingMetadata();
					doSendingData();
				}		
			}
			, delay_start_internet, tickPeriod_internet, TimeUnit.SECONDS);
			
		} catch (Exception e) {
			mLogger.error("Exception", e);
		}

	}

	@Override
	public synchronized void stop() {
		mqttBridgeThreads.shutdown();
		disconnectMqttServer();
		//mLogger.info(bundleName + "<" + bundleVer + ">" + " is stoped! ");
	}

	@Override
	public boolean adapterSendDeviceState(String adapter, String data) {
		return sendMessage(data);
	}

	@Override
	public boolean adapterSendMetaData(String Adapter, String Data) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean adapterSendData(String Adapter, String Data) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void loadRestSettings() {
		if((rest_host == null) || ("".equals(rest_host))){
			if(remoteHost.startsWith("mqtt")){
				rest_host =remoteHost.substring(4);
			}else {
				rest_host = remoteHost;
			}
		}
		if(!rest_host.startsWith("http://") && !rest_host.startsWith("https://")){
			rest_url = "https://";
		}
		
		if("".equals(rest_port)){
			rest_url += rest_host+"/"+rest_prefix +customerId+"-"+gatewayId;
		}else{
			rest_url += rest_host+":"+rest_port+"/"+rest_prefix +customerId+"-"+gatewayId;
		}
	}
	
	public void initSettings(Map<String, String> settings) {
		try {
			super.initSettings(settings);
			customerId = settings.get("customer_id").trim();
			gatewayId = settings.get("gateway_id").trim();
			remoteHost = settings.get("remote_host").trim();
			remotePort = Integer.parseInt(settings.get("remote_port").trim());
			mqttTopic = settings.get("mqtt_topic").trim();
			mqttSubTopic = settings.get("mqtt_sub_topic").trim();
			mqttSsl = Boolean.parseBoolean(settings.get("mqtt_ssl").trim());
			mqttQos = Integer.parseInt(settings.get("mqtt_qos").trim());
			mqttCleanSession = Boolean.parseBoolean(settings.get("mqtt_clean_session").trim());
			mqttKeepAliveInterval = Integer.parseInt(settings.get("mqtt_keep_alive_interval"));
			mqttConnectionTimeOut = Integer.parseInt(settings.get("mqtt_connection_timeout"));
			//mqttClientId = settings.get("mqtt_client_id");
			tickPeriod_query = Integer.parseInt(settings.get("check_period_query"));
			tickPeriod_internet = Integer.parseInt(settings.get("check_period_internet"));
			delay_start_query = Integer.parseInt(settings.get("delay_start_query"));
			metadataIntervalTime = Integer.parseInt(settings.get("metadata_interval_time"));
			//delay_start_internet = Integer.parseInt(settings.get("delay_start_internet"));
			bufferSendLimit = Integer.parseInt(settings.get("buffer_send_limit"));
			rest_host = settings.get("rest_host");
			rest_port = settings.get("rest_port");
			
			if (mqttSubTopic.contains("1.9")) {
				protocolVer = "1.9";
			}
			mqttClientId = MqttClient.generateClientId();
			mLogger.info("[initSettings] MQTT generate client id : " + mqttClientId);
			settings.put("mqtt_client_id", mqttClientId);
			
			dbExecute.updateBridgeSettings(settings);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		}

	}
	
	private void configMqttClient() {
		mLogger.info("configure MQTT client ...");
		
		String protocol = "tcp://";
		if (mqttSsl) {
			protocol = "ssl://";
		}
		// configure for MQTT protocol to broker
		brokerUrl = protocol + remoteHost + ":" + remotePort;		
    	try {
        	MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence("mqtt_data/persistance/");
        	
	    	conOpt = new MqttConnectOptions();
	    	conOpt.setCleanSession(mqttCleanSession);
	    	conOpt.setKeepAliveInterval(mqttKeepAliveInterval);//modify
	    	conOpt.setConnectionTimeout(mqttConnectionTimeOut);

	    	if (mqttSsl) {
	    	    String CA_FILE = "ssl/ca.crt";
	    	    String CRT_FILE = "ssl/client.crt";
	    	    String KEY_FILE = "ssl/client.key";
				conOpt.setSocketFactory(SslUtil.getSocketFactory(CA_FILE, CRT_FILE, KEY_FILE, sslSocketPass));	
	  	    }
	    	
	    	conOpt.setPassword(mqttPassword.toCharArray());
	        conOpt.setUserName(mqttUser);
	        
	        mqttMessage = new MqttMessage();
	        mqttMessage.setQos(mqttQos);
	        
	        mqttClient = new MqttClient(this.brokerUrl,mqttClientId, dataStore);
	        mqttClient.setCallback(this);
	        mqttClient.setTimeToWait(15000);
    	} catch (MqttException e) {
			mLogger.error("MqttException", e);
		}  catch (Exception e) {
			mLogger.error("Exception", e);
		}
	}
	
	private void connectMqttServer() {
		mLogger.info("Connecting to MQTT server : " + brokerUrl);
		if (!mqttClient.isConnected()) {
			try {
				mqttClient.connect(conOpt);
				bridgeError = 0;
				mLogger.info("Established connection successfully to MQTT broker");
			} catch (MqttSecurityException e) {
				mLogger.error("MqttSecurityException", e);
			} catch (MqttException e) {
				mLogger.error("MqttException", e);
			}
		}
	}
	
	private void reconnectMqttServer() {
		mLogger.info("Re-Connecting to MQTT server : " + brokerUrl);
			try {
				mqttClient.reconnect();
				bridgeError = 0;
				mLogger.info("Established connection successfully to MQTT broker");
			} catch (MqttSecurityException e) {
				mLogger.error("MqttSecurityException", e);
			} catch (MqttException e) {
				mLogger.error("MqttException", e);
			}
		
	}
	
	private void disconnectMqttServer() {
		mLogger.info("Disconnecting to MQTT server : " + brokerUrl);
		if (mqttClient.isConnected()) {
			try {
				mqttClient.disconnect();
				mLogger.info("Disconnected connection to MQTT broker");
			} catch (MqttException e) {
				mLogger.error("MqttException", e);
			}
		}
		
	}
	
	public void doSendingMetadata() {
		if ((lastUpdatedMetadata != 0) && ((System.currentTimeMillis() - lastUpdatedMetadata) < (metadataIntervalTime*1000) ) ) {
			return;
		}
		lastUpdatedMetadata = System.currentTimeMillis(); 
		
		synchronized (metadataSync) {
			String rest_result = null;
			try {
				if (metamsg == null) {
					metamsg = "";
				}
				mLogger.info("Send Metadata to : " + rest_url);
				rest_result = REST.httpPost(rest_url, metamsg, false);
			} catch (Exception e) {
				mLogger.error("Exception", e);
			}
			if (rest_result != null) {
				mLogger.info("Sent message: " + metamsg);
				mLogger.info("Successfully send metadata to server, with result: " + rest_result);
			}
		}
	}
	
	public synchronized boolean sendMessage(String msg) {
		String topic = mqttTopic+"/"+mqttSubTopic;
		mLogger.info("sending message to MQTT server : " + brokerUrl + ", topic : " + topic);
		if (mqttClient.isConnected()) {
			mLogger.info("mqtt client is connected to server !");
			mqttMessage.setPayload(msg.getBytes());
			try {
				mqttClient.publish(topic, mqttMessage);
				mLogger.info("Sent message: " + msg);
				bridgeError = 0;
				return true;
			} catch (MqttPersistenceException e) {
				mLogger.error("MqttPersistenceException", e);
			} catch (MqttException e) {
				mLogger.error("MqttException", e);
			}
		} else {
			mLogger.info("mqtt client is disconnected to server !");
		}
		bridgeError++;
		bridgeErrorPerMinute++;
		return false;
	}
	

	public synchronized void checkBridgeStatus() {
		if (mqttClient == null) {
			mLogger.error("Checking Connection ... mqttClient is null. Waiting initialization ... !");
		} else if (!mqttClient.isConnected()) {
			mLogger.error("Checking Connection ... mqttClient disconected to Server ! Trying connection ");
			connectMqttServer();
		} else if (bridgeError > bridgeErrorMax) {
			mLogger.error("Checking Connection ... too many error !  Trying re-connection. Error = " + bridgeError);
			reconnectMqttServer();
		} else {
			mLogger.info("Checking Connection ... Ok ! . Error = " + bridgeError);
		}
	}
	
}
