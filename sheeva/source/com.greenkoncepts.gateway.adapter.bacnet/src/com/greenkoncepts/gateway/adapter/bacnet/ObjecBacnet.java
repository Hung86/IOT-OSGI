package com.greenkoncepts.gateway.adapter.bacnet;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.Network;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.service.confirmed.CreateObjectRequest;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.enumerated.Segmentation;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;

public class ObjecBacnet {
	private LocalDevice localDevice;
	private Address address;
	private Network network;
	private Logger mLogger = LoggerFactory.getLogger(getClass().getSimpleName());
	public ObjecBacnet(LocalDevice d, Address addr, Network net) {
		localDevice = d;
		address = addr;
		network = net;
	}
	public void defaultObjectList() {
		//create electrical Measure
		for (int i = 0; i < 100; i++) {
			List<PropertyValue> values = new ArrayList<PropertyValue>();
			values.add(new PropertyValue(PropertyIdentifier.objectName, new CharacterString("Active Energy Reading")));
			 values.add(new PropertyValue(PropertyIdentifier.presentValue, new Real(((float)(15.5 * i)))));
			 values.add(new PropertyValue(PropertyIdentifier.units, EngineeringUnits.kilowattHours));
			 ObjectIdentifier newOI = new ObjectIdentifier(ObjectType.analogInput, 1000 + i);
			 CreateObjectRequest newRO = new CreateObjectRequest(newOI, new SequenceOf<PropertyValue>(values));
			 try {
				localDevice.send(address, network, 1476, Segmentation.segmentedBoth, newRO);
			} catch (BACnetException e) {
				mLogger.error("BACnetException", e);
			}
		}
		
		//create water Measure
		for (int i = 0; i < 100; i++) {
			List<PropertyValue> values = new ArrayList<PropertyValue>();
			values.add(new PropertyValue(PropertyIdentifier.objectName, new CharacterString("Water Volume Reading")));
			 values.add(new PropertyValue(PropertyIdentifier.presentValue, new Real(((float)(17.5 * i)))));
			 values.add(new PropertyValue(PropertyIdentifier.units, EngineeringUnits.cubicMeters));
			 ObjectIdentifier newOI = new ObjectIdentifier(ObjectType.binaryInput, 2000 + i);
			 CreateObjectRequest newRO = new CreateObjectRequest(newOI, new SequenceOf<PropertyValue>(values));
			 try {
				localDevice.send(address, network, 1476, Segmentation.segmentedBoth, newRO);
			} catch (BACnetException e) {
				mLogger.error("BACnetException", e);
			}
		}
		
		//create electrical Measure
		for (int i = 0; i < 100; i++) {
			List<PropertyValue> values = new ArrayList<PropertyValue>();
			values.add(new PropertyValue(PropertyIdentifier.objectName, new CharacterString("Active Power")));
			 values.add(new PropertyValue(PropertyIdentifier.presentValue, new Real(((float)(3.45 * i)))));
			 values.add(new PropertyValue(PropertyIdentifier.units, EngineeringUnits.kilowatts));
			 ObjectIdentifier newOI = new ObjectIdentifier(ObjectType.accumulator, 3000 + i);
			 CreateObjectRequest newRO = new CreateObjectRequest(newOI, new SequenceOf<PropertyValue>(values));
			 try {
				localDevice.send(address, network, 1476, Segmentation.segmentedBoth, newRO);
			} catch (BACnetException e) {
				mLogger.error("BACnetException", e);
			}
		}
		
	}
}
