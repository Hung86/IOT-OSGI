package com.greenkoncepts.gateway.core.mqtt.sub;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greenkoncepts.gateway.api.bridge.BridgeMaster;
import com.greenkoncepts.gateway.core.mqtt.AbstractMqttClient;


public class Subscriber extends AbstractMqttClient{

    public static final String BROKER_URL = "tcp://192.168.1.124:1883";
    public static final String TOPIC = "test/GKC/GKC-F0AD4E00EABB/version=1.8";
    private static final String CLIENT_ID = "thilinamb-subscriber";

    private Logger mlogger = (Logger) LoggerFactory.getLogger(getClass().getSimpleName());

    public Subscriber(BridgeMaster bridge) throws MqttException {
        super(BROKER_URL, CLIENT_ID, new SubscriberCallback(bridge));
        try {
            // subscribe
            getClient().subscribe(TOPIC);
            mlogger.info("Connected to " + BROKER_URL + " and subscribed to topic " + TOPIC);
        } catch (MqttException e) {
        	mlogger.error("Error instantiating the subscriber", e);
            throw e;
        }
    }
    
    public Subscriber(String brokerUrl, String clientId, String topic, BridgeMaster bridge) throws MqttException {
        super(brokerUrl, clientId, new SubscriberCallback(bridge));
        try {
            // subscribe
            getClient().subscribe(topic);
            mlogger.info("Connected to " + brokerUrl + " and subscribed to topic " + topic);
        } catch (MqttException e) {
        	mlogger.error("Error instantiating the subscriber", e);
            throw e;
        }
    }

//    public static void main(String[] args) throws MqttException {
//        Subscriber subscriber = new Subscriber(BROKER_URL, CLIENT_ID, TOPIC);
//        Scanner scanner = new Scanner(System.in);
//        while (scanner.hasNextLine()){
//            String line = scanner.nextLine();
//            if(line.trim().toLowerCase().equals("quit")){
//                break;
//            }
//        }
//        subscriber.disconnect();
//        logger.info("Exit!");
//    }

}
