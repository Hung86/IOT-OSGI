package com.greenkoncepts.gateway.control.rest.server;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenkoncepts.gateway.api.adapter.Adapter;
import com.greenkoncepts.gateway.api.task.ITask;

public class XDKTask implements ITask{
	static Logger mLogger = LoggerFactory.getLogger("XDKTask");
	private RestServiceTracker tracker;
	private String data;
	public XDKTask(RestServiceTracker tracker, String data) {
		super();
		this.tracker = tracker;
		this.data = data;
	}
	
	@Override
	public void run() {
		if (tracker != null	) {
			Adapter foundAdapter = tracker.getAdapterService("BoschAdapter");
			if (foundAdapter == null) {
				mLogger.error("... Adapter is invalid");
			} else {
				ObjectMapper objectMapper = new ObjectMapper();
				try {
					@SuppressWarnings("unchecked")
					Map<String, Object> oiMap = objectMapper.readValue(data, Map.class);
					if (oiMap != null) {
						foundAdapter.setNodeValue("", "", oiMap);
					}
				} catch (JsonParseException e) {
					mLogger.error("JsonParseException", e);
				} catch (JsonMappingException e) {
					mLogger.error("JsonMappingException", e);
				} catch (IOException e) {
					mLogger.error("IOException", e);
				} catch (Exception e) {
					mLogger.error("Exception", e);
				}
			}
		}
	}
}
