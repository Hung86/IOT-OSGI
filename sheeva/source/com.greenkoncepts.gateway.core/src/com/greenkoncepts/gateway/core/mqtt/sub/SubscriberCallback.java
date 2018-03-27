package com.greenkoncepts.gateway.core.mqtt.sub;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenkoncepts.gateway.api.adapter.Adapter;
import com.greenkoncepts.gateway.api.bridge.BridgeMaster;
import com.greenkoncepts.gateway.api.task.ITaskExecute;

public class SubscriberCallback implements MqttCallback {

	 private Logger mLogger = LoggerFactory.getLogger(getClass().getSimpleName());
	 private BridgeMaster bridge  ;

	public SubscriberCallback (BridgeMaster bridge) {
		super();
		this.bridge = bridge;
	}
	
     @Override
    public void connectionLost(Throwable throwable) {
    	mLogger.info("Connection Lost!");
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
    	String msg = new String(mqttMessage.getPayload());
    	mLogger.debug("Message Arrived. Topic: " + s + ", Message: " + msg);
    	
    	MqttSubTask task = new MqttSubTask(bridge, msg);
    	ITaskExecute taskExecute = bridge.getTaskExecute();
    	if (taskExecute != null) {
    		taskExecute.addTask(task);
    	} else {
    		mLogger.info("...No Task executes the arrived message");
    	}
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
