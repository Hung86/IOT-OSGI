package com.greenkoncepts.gateway.core.mqtt.sub;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenkoncepts.gateway.api.adapter.Adapter;
import com.greenkoncepts.gateway.api.bridge.BridgeMaster;
import com.greenkoncepts.gateway.api.task.ITask;

public class MqttSubTask implements ITask{
	static Logger mLogger = LoggerFactory.getLogger("MqttSubTask");
	private BridgeMaster bridge;

	private String data;
	public MqttSubTask (BridgeMaster bridge, String data) {
		this.data = data;
		this.bridge = bridge;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		if (bridge == null) {
			return;
		}
		List<Adapter> adapters = bridge.getAdapterServices();
		if (adapters != null) {
			Map<String, Object> oiMap = null;
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				oiMap = objectMapper.readValue(data, Map.class);
			} catch (JsonParseException e) {
				mLogger.error("JsonParseException", e);
			} catch (JsonMappingException e) {
				mLogger.error("JsonMappingException", e);
			} catch (IOException e) {
				mLogger.error("IOException", e);
			} catch (Exception e) {
				mLogger.error("Exception", e);
			}
			if (oiMap != null) {
				for (Adapter adapter : adapters) {
					if (adapter.setNodeValue(null, null, oiMap)) {
						break;
					}
				}
			}
		}
	}

}
