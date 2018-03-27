package com.greenkoncepts.gateway.adapter.bacnet;

import java.util.Map;

import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

class WrittingPoint extends DataPoint {
	public ObjectIdentifier objectIdentifier;
	public String objectIdentifierName;
	public String default_value;
	public String max_value;
	public String min_value;
	
	public WrittingPoint(){
		super();
		objectIdentifier = null;
		objectIdentifierName = "";
		default_value = null;
		max_value = null;
		min_value = null;
	}

	@Override
	public void initializeObject(Map<String, String> dp) {
		String tempValue = dp.get("data_point");
		if ((tempValue != null) && (!tempValue.isEmpty())) {
			datapointId = Integer.parseInt(tempValue);
		}
		
		tempValue = dp.get("channel");
		if ((tempValue != null) && (!tempValue.isEmpty())) {
			channelId = Integer.parseInt(tempValue);
		}
		
		tempValue = dp.get("default_value");
		if ((tempValue != null) && (!tempValue.isEmpty())) {
			default_value = tempValue;
		}
		
		tempValue = dp.get("max_value");
		if ((tempValue != null) && (!tempValue.isEmpty())) {
			max_value = tempValue;
		}
		
		tempValue = dp.get("min_value");
		if ((tempValue != null) && (!tempValue.isEmpty())) {
			min_value = tempValue;
		}
		
		tempValue = dp.get("name");
		if ((tempValue != null) && (!tempValue.isEmpty())) {
			name = tempValue;
		}
		
		tempValue = dp.get("measure_name");
		if ((tempValue != null) && (!tempValue.isEmpty())) {
			measureName = tempValue;
		}
		
		tempValue = dp.get("measure_unit");
		if ((tempValue != null) && (!tempValue.isEmpty())) {
			measureUnit = tempValue;
		}
		
		tempValue = dp.get("object_identifier");
		if ((tempValue != null) && (!tempValue.isEmpty())) {
			objectIdentifierName = tempValue;
			buildObjectIdentifier(objectIdentifierName);
		}
		
	}

	void buildObjectIdentifier(String OIName) {
		objectIdentifier = getObjectIdentifierByName(OIName);
		if (objectIdentifier == null) {
			mLogger.error("There is not definition of Object Identifier");
		}
	}
	
	@Override
	public String toString() {
		return ("Writing Datapoint : datapointId = " + datapointId + ", channelId = " + channelId +  ", default_value = " + default_value 
				+  ", max_value = " + max_value +  ", min_value = " + min_value + ",  objectIdentifier = " + objectIdentifierName 
				+ ", measureName = " + measureName + ", measureUnit = " + measureUnit + ", condition = " + condition + ", action = " + action);
	}
	
}