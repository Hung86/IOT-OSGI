package com.greenkoncepts.gateway.adapter.bacnet;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

public abstract class DataPoint {
	public int datapointId;
	public int channelId;
	public String name;
	public String measureName;
	public String measureUnit;
	public boolean hasError;
	public String condition;
	public String action;
	protected Logger mLogger;
	DataPoint() {
		datapointId = -1;
		channelId = -1;
		name = "";
		measureName = "";
		measureUnit = "";
		hasError = false;
		condition = "";
		action = "";
		mLogger = LoggerFactory.getLogger(getClass().getName());
	}
	public abstract void initializeObject(Map<String, String> dp);
	public abstract String toString();
	
	
	public static ObjectIdentifier getObjectIdentifierByName(String OIName) {
		if (OIName != null) {
			ObjectType obtype = null;
			if (OIName.startsWith("ai")) {
				obtype = ObjectType.analogInput;
			} else if (OIName.startsWith("ao")) {
				obtype = ObjectType.analogOutput;
			} else if (OIName.startsWith("av")) {
				obtype = ObjectType.analogValue;
			} else if (OIName.startsWith("bi")) {
				obtype = ObjectType.binaryInput;
			} else if (OIName.startsWith("bo")) {
				obtype = ObjectType.binaryOutput;
			} else if (OIName.startsWith("ac")) {
				obtype = ObjectType.accumulator;
			} else {
				return null;
			}
			int objectIdentifierId = Integer.parseInt(OIName.substring(2));
			return new ObjectIdentifier(obtype, objectIdentifierId);
		}
		
		return null;
	}

}