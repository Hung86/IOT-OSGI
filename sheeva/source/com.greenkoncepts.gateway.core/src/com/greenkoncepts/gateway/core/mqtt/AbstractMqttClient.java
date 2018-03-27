package com.greenkoncepts.gateway.core.mqtt;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Abstract MQTT client implementation that is extended by publisher and the subscriber
 * Author: Thilina
 * Date: 7/19/14
 */
public class AbstractMqttClient {
    private Logger mlogger = (Logger) LoggerFactory.getLogger(getClass().getSimpleName());

    private MqttClient client;

    public AbstractMqttClient(String brokerUrl, String clientId, MqttCallback callback) throws MqttException {
        try {
            client = new MqttClient(brokerUrl, clientId);
            client.setCallback(callback);
            client.connect();
            mlogger.info("Connected to " + brokerUrl + " with client-id " + clientId);
        } catch (MqttException e) {
        	mlogger.error("Error instantiating the client", e);
            throw e;
        }
    }

    public void disconnect(){
        try {
            client.disconnect();
            mlogger.info("Disconnected from client.");
        } catch (MqttException e) {
        	mlogger.error("Error disconnecting!", e);
        } finally {
            if(client.isConnected()){
                try {
                    client.disconnect();
                } catch (MqttException e) {
                	mlogger.error("Error disconnecting!", e);
                }
            }
        }
    }

    protected MqttClient getClient(){
        return client;
    }
}
