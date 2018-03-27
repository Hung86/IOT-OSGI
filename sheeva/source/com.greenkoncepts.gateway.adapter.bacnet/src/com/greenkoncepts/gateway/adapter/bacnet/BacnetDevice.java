package com.greenkoncepts.gateway.adapter.bacnet;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.nfunk.jep.SymbolTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greenkoncepts.expression.GKEP;
import com.greenkoncepts.gateway.api.adapter.ABacnetAdapter;
import com.greenkoncepts.gateway.util.Util;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.Network;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.gk.GKRemoteDevice;
import com.serotonin.bacnet4j.gk.GKRemoteObject;
import com.serotonin.bacnet4j.service.acknowledgement.ReadPropertyAck;
import com.serotonin.bacnet4j.service.acknowledgement.ReadPropertyMultipleAck;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyMultipleRequest;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyRequest;
import com.serotonin.bacnet4j.service.confirmed.WritePropertyMultipleRequest;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.PropertyReference;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.ReadAccessResult;
import com.serotonin.bacnet4j.type.constructed.ReadAccessResult.Result;
import com.serotonin.bacnet4j.type.constructed.ReadAccessSpecification;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.constructed.WriteAccessSpecification;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.enumerated.Segmentation;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public class BacnetDevice {
	public final static int DEVICE_STATUS_UNKNOWN = 0;
	public final static int DEVICE_STATUS_ERROR = 1;
	public final static int DEVICE_STATUS_ONLINE = 2;
	public final static int DEVICE_STATUS_OFFLINE = 3;
	
	
	private String name;
	private String address;
	private int category;
	private int instanceid;
	private int alternativeid;
	private GKRemoteDevice gkRemoteDevice;
	private LocalDevice localDevice;

	private List<String> realDatapointIdList = new ArrayList<String>();
	private List<Integer> virtualDatapointIdList = new ArrayList<Integer>();
	// Map<Integer, DataPoint> datapointTable = new LinkedHashMap<Integer, DataPoint>();
	private Map<String, RealPoint> realDatapointMap = new LinkedHashMap<String, RealPoint>();
	private Map<Integer, VirtualPoint> virtualDatapointMap = new LinkedHashMap<Integer, VirtualPoint>();
	private Map<String, WrittingPoint> writtingDataPointMap = new LinkedHashMap<String, WrittingPoint>();
	private List<ValidationRule> validationRuleList = new ArrayList<ValidationRule>();
	private Logger mLogger = LoggerFactory.getLogger(getClass().getName());
	private DecimalFormat vformat = new DecimalFormat("#########0.0000");
	private long queryingtTimeout = 0;
	private int maxOiPerQuery = 30;
	private int maxChannelPerMsg = 30;
	protected int errorCount = 0;
	

	BacnetDevice(String address, String networkNumber, String networkAddress, String instanceid, String alternativeid, String category) throws UnknownHostException {
		this.address = address;
		this.instanceid = Integer.parseInt(instanceid);
		this.alternativeid = Integer.parseInt(alternativeid);
		this.category = Integer.parseInt(category);


		// Setting corresponding device inside Bacnet server
		InetAddress inetAddress = InetAddress.getByName(this.address);
		int nwnum = Util.getIntValueOf(networkNumber);
		if ((networkAddress != null) && (networkAddress.length() > 2) && (nwnum != 0)) {
			String netAddrString = networkAddress.substring(1, networkAddress.length() - 1);
			String[] netAddrArrayString = netAddrString.split(",");
			byte[] netAddrArrayBytes = new byte[netAddrArrayString.length];
			for (int i = 0; i < netAddrArrayBytes.length; i++) {
				netAddrArrayBytes[i] = (byte) Util.getIntValueOf(netAddrArrayString[i]);
			}

			Network network = new Network(nwnum, netAddrArrayBytes);
			gkRemoteDevice = new GKRemoteDevice(this.instanceid, new Address(network, inetAddress.getAddress(), 0xBAC0), network);
			mLogger.info("Remote Bacnet Device is created with network = " + network.toString());

		} else {
			gkRemoteDevice = new GKRemoteDevice(this.instanceid, new Address(inetAddress.getAddress(), 0xBAC0), null);
			mLogger.info("Remote Bacnet Device is created without network");
		}
	}
	
	public void setNodeData(Map<String, RealPoint> realMap, Map<Integer, VirtualPoint> virtualMap, Map<String, WrittingPoint> writtingMap) {
		this.realDatapointMap = realMap;
		this.virtualDatapointMap = virtualMap;
		this.writtingDataPointMap = writtingMap;
		this.realDatapointIdList = new ArrayList<String>(realMap.keySet());
		this.virtualDatapointIdList = new ArrayList<Integer>(virtualMap.keySet());
		
		if (gkRemoteDevice != null) {
			gkRemoteDevice.setObjectList(new SequenceOf<ObjectIdentifier>(getObjectIdentfierList()));
			gkRemoteDevice.setConnect(true);
		}
	}

	public List<ObjectIdentifier> getObjectIdentfierList() {
		List<ObjectIdentifier> OIList = new ArrayList<ObjectIdentifier>();
		for (String dpkey : realDatapointMap.keySet()) {
			RealPoint realDataPoint = realDatapointMap.get(dpkey);
			if (realDataPoint.objectIdentifier != null) {
				OIList.add(realDataPoint.objectIdentifier);
			}
		}
		return OIList;
	}

//	public void evaluateValidationRules() {
//		for (ValidationRule item : validationRuleList) {
//			GKEP parser = new GKEP();
//			parser.setAllowUndeclared(true);
//			parser.parseExpression(item.condition);
//			SymbolTable symbols = parser.getSymbolTable();
//			for (Iterator<?> iter = symbols.keySet().iterator(); iter.hasNext();) {
//				String key = (String) iter.next();
//				ReadingPoint dp = getDataPoint(Integer.parseInt(key.substring(1)));
//				if (dp != null) {
//					parser.addVariable(key, dp.measureValue);
//				}
//			}
//
//			if (parser.getValue() != 0.0) {
//				String[] actions = item.action.split("=");
//				String dpId1 = actions[0].trim().substring(1);
//				DataPoint dp1 = getDataPoint(Integer.parseInt(dpId1));
//				if (dp1 != null) {
//					if (actions[1].startsWith("N")) {
//						String dpId2 = actions[1].trim().substring(1);
//						DataPoint dp2 = getDataPoint(Integer.parseInt(dpId2));
//						dp1.measureValue = dp2.measureValue;
//					} else {
//						dp1.measureValue = Integer.parseInt(actions[1].trim());
//					}
//				}
//			}
//		}
//	}
//
//	public DataPoint getDataPoint(int id) {
//		DataPoint dp = realDatapointMap.get(id);
//		if (dp == null) {
//			dp = virtualDatapointMap.get(id);
//		}
//		return dp;
//	}

	public List<ReadingPoint> getReadingDatapointList(int nodeType, int index, int length) {
		List<ReadingPoint> readingDpList = new ArrayList<ReadingPoint>();
		int offset;

		if (nodeType == ABacnetAdapter.USED_REAL_READING_NODE) {
			if (index == -1) {
				readingDpList =  new ArrayList<ReadingPoint>(realDatapointMap.values());
			}
			
			String key;
			for (int i = 0; i < length; i++) {
				offset = index + i;
				key = realDatapointIdList.get(offset);
				readingDpList.add(realDatapointMap.get(key));
			}
		} else if (nodeType == ABacnetAdapter.USED_VIRTUAL_READING_NODE) {
			if (index == -1) {
				readingDpList =  new ArrayList<ReadingPoint>(virtualDatapointMap.values());
			}
			
			Integer key;
			for (int i = 0; i < length; i++) {
				offset = index + i;
				key = virtualDatapointIdList.get(offset);
				readingDpList.add(virtualDatapointMap.get(key));
			}
		}


		return readingDpList;
	}
	

	public int getIdSentToServer() {
		return (alternativeid <= 0) ? instanceid : alternativeid;
	}

	public int getDeviceStatus() {
		int status = DEVICE_STATUS_UNKNOWN;
		if (gkRemoteDevice == null) {
			status = DEVICE_STATUS_OFFLINE;
		} else	if (errorCount > 3) {
			status = DEVICE_STATUS_OFFLINE;
		} else {
			status = DEVICE_STATUS_ONLINE;
		}
		
		mLogger.debug("[getDeviceStatus] Device (" + gkRemoteDevice.toString() + ") has status " + status);
		return status;
	}

	public boolean setDataPointValue(Map<String, String> datapoints) {
		List<String> results = new ArrayList<String>();

		if ((datapoints == null) || (datapoints.size() == 0)) {
			return false;
		}

		List<WriteAccessSpecification> writeProperties = new ArrayList<WriteAccessSpecification>();
		List<PropertyValue> pvs;
		try {
			for (String key : datapoints.keySet()) {
				WrittingPoint writtinDP = null;
				for (String oi : writtingDataPointMap.keySet()) {
					WrittingPoint temp = writtingDataPointMap.get(oi);
					if (String.valueOf(temp.channelId).equals(key)) {
						writtinDP = temp;
						break;
					}
				}
				
				if (writtinDP == null) {
					mLogger.info("[setDataPointValue] can not find Object Identidier for channel " + key);
					continue;
				}
				
				//check validation
				
				String value = datapoints.get(key);
				ObjectType objectType = writtinDP.objectIdentifier.getObjectType();
				pvs = new ArrayList<PropertyValue>();

				if ((objectType == ObjectType.analogInput) || (objectType == ObjectType.analogOutput)) {
					float set_val = Float.valueOf(value);
					if (writtinDP.min_value != null) {
						float min_val = Float.valueOf(writtinDP.min_value);
						if ((!Float.isNaN(min_val)) && (set_val < min_val)) {
							mLogger.info("[setDataPointValue] " + writtinDP.objectIdentifier.toString() + " set unsuccessfully because set value " + set_val + " less than min value " + min_val);
							continue;
						}
					}
					
					if (writtinDP.max_value != null) {
						float max_val = Float.valueOf(writtinDP.max_value);
						if ((!Float.isNaN(max_val)) && (set_val > max_val)) {
							mLogger.info("[setDataPointValue] " + writtinDP.objectIdentifier.toString() + " set unsuccessfully because set value " + set_val + " greater than max value " + max_val);
							continue;
						}
					}
					Real val = new Real(set_val);
					pvs.add(new PropertyValue(PropertyIdentifier.presentValue, val));
					mLogger.info("[setDataPointValue] " + writtinDP.objectIdentifier.toString() + " set value to " + val);
					
				} else if ((objectType == ObjectType.binaryInput) || (objectType == ObjectType.binaryOutput)) {
					int temp = Integer.valueOf(value);
					BinaryPV val = BinaryPV.active;
					if (temp == 0) {
						val = BinaryPV.inactive;
					}
					pvs.add(new PropertyValue(PropertyIdentifier.presentValue, val));
					mLogger.info("[setDataPointValue] " + writtinDP.objectIdentifier.toString() + " set value to " + val);
					
				}

				writeProperties.add(new WriteAccessSpecification(writtinDP.objectIdentifier, new SequenceOf<PropertyValue>(pvs)));
				results.add(key);
			}

			if (!writeProperties.isEmpty()) {
				Address address = gkRemoteDevice.getAddress();
				Network network = gkRemoteDevice.getNetwork();
				InetSocketAddress addr = new InetSocketAddress(address.toIpString(), 0xBAC0);

				try {
					localDevice.send(addr, network, 1476, Segmentation.segmentedBoth, new WritePropertyMultipleRequest(
							new SequenceOf<WriteAccessSpecification>(writeProperties)));
				} catch (BACnetException e) {
					results.clear();
					mLogger.warn("BACnetException", e);
				}
			}
		} catch (Exception e) {
			mLogger.error("Exception", e);
		}
		return results.isEmpty() ? false : true;
	}
	
	public List<String> setDataPointDefaultValue() {
		List<String> results = new ArrayList<String>();
		
		List<WriteAccessSpecification> writeProperties = new ArrayList<WriteAccessSpecification>();
		List<PropertyValue> pvs;
		try {
			for (String key : writtingDataPointMap.keySet()) {
				WrittingPoint writtinDP = writtingDataPointMap.get(key);
				
				if (writtinDP.default_value == null) {
					mLogger.info("[setDataPointDefaultValue] don't run default value setting for Object Identidier " + key);
					continue;
				}
				
				String value = writtinDP.default_value;
				ObjectType objectType = writtinDP.objectIdentifier.getObjectType();
				pvs = new ArrayList<PropertyValue>();

				if ((objectType == ObjectType.analogInput) || (objectType == ObjectType.analogOutput)) {
					float set_val = Float.valueOf(value);
					if (writtinDP.min_value != null) {
						float min_val = Float.valueOf(writtinDP.min_value);
						if ((!Float.isNaN(min_val)) && (set_val < min_val)) {
							mLogger.info("[setDataPointDefaultValue] " + writtinDP.objectIdentifier.toString() + " set unsuccessfully because set value " + set_val + " less than min value " + min_val);
							continue;
						}
					}
					
					if (writtinDP.max_value != null) {
						float max_val = Float.valueOf(writtinDP.max_value);
						if ((!Float.isNaN(max_val)) && (set_val > max_val)) {
							mLogger.info("[setDataPointDefaultValue] " + writtinDP.objectIdentifier.toString() + " set unsuccessfully because set value " + set_val + " greater than max value " + max_val);
							continue;
						}
					}
					Real val = new Real(set_val);
					pvs.add(new PropertyValue(PropertyIdentifier.presentValue, val));
					mLogger.info("[setDataPointDefaultValue] " + writtinDP.objectIdentifier.toString() + " set value to " + set_val);

				} else if ((objectType == ObjectType.binaryInput) || (objectType == ObjectType.binaryOutput)) {
					int temp = Integer.valueOf(value);
					BinaryPV val = BinaryPV.active;
					if (temp == 0) {
						val = BinaryPV.inactive;
					}
					pvs.add(new PropertyValue(PropertyIdentifier.presentValue, val));
					mLogger.info("[setDataPointDefaultValue] " + writtinDP.objectIdentifier.toString() + " set value to " + val);
				}

				writeProperties.add(new WriteAccessSpecification(writtinDP.objectIdentifier, new SequenceOf<PropertyValue>(pvs)));
				results.add(key);
			}

			if (!writeProperties.isEmpty()) {
				Address address = gkRemoteDevice.getAddress();
				Network network = gkRemoteDevice.getNetwork();
				InetSocketAddress addr = new InetSocketAddress(address.toIpString(), 0xBAC0);

				try {
					localDevice.send(addr, network, 1476, Segmentation.segmentedBoth, new WritePropertyMultipleRequest(
							new SequenceOf<WriteAccessSpecification>(writeProperties)));
				} catch (BACnetException e) {
					results.clear();
					mLogger.warn("BACnetException", e);
				}
			}
		} catch (Exception e) {
			mLogger.error("Exception" ,e);
		}
		return results;
	}

	public List<String> getDeviceData() throws Exception {
		List<String> result = new ArrayList<String>();

		int instanceid = getInstanceid();
		if (instanceid != gkRemoteDevice.getInstanceNumber()) {
			mLogger.error("Query instance id of device:" + instanceid + " and remote device:" + gkRemoteDevice.getInstanceNumber() + "are different");
			result.add("|DEVICEID=" + getCategory() + "-" + gkRemoteDevice.getInstanceNumber() + ";TIMESTAMP=" + System.currentTimeMillis()
					+ ";ERROR=Communication timeout");
			return result;
		}
		// not online case
		if (getDeviceStatus() != 2) {
			mLogger.error("Device " + instanceid + " is in Error case !");
			result.add("|DEVICEID=" + getCategory() + "-" + gkRemoteDevice.getInstanceNumber() + ";TIMESTAMP=" + System.currentTimeMillis()
					+ ";ERROR=Communication timeout");
			return result;
		}
		// online case
		mLogger.info("Querying for device " + instanceid + " that is online !");

		Map<String, RealPoint> realDpMap = getRealDatapointMap();
		Map<Integer, VirtualPoint> virtualDpMap = getVirtualDatapointMap();
		int msgCount = 0;
		StringBuilder dataBuffer = new StringBuilder();

		for (String realKey : realDpMap.keySet()) {
			RealPoint realDP = realDpMap.get(realKey);
			GKRemoteObject dataOI = gkRemoteDevice.getObject(realDP.objectIdentifier);
			if (dataOI != null) {
				Encodable encode = dataOI.getProperty(PropertyIdentifier.presentValue);
				// Encodable unit_encode = dataOI.getProperty(PropertyIdentifier.units);
				if (encode != null) {
					double valueOfDataOI = Double.parseDouble(encode.toString()) * realDP.measureRatio;
					if (realDP.hasConsumption) {
						if (realDP.measureValue <= 0) {
							realDP.consumptionValue = 0;
						} else if (valueOfDataOI >= realDP.measureValue) {
							realDP.consumptionValue = (valueOfDataOI - realDP.measureValue) * realDP.consumptionRatio;
						} else {
							realDP.consumptionValue = 0;
						}
					}
					realDP.measureValue = valueOfDataOI;
					realDP.hasError = false;
				} else {
					mLogger.debug("Encode is null. It can't get Present Value");
					realDP.hasError = true;
				}
			} else {
				mLogger.debug("Not found data for the Object Identifier " + realDP.objectIdentifier);
				realDP.hasError = true;
			}
		}

		// ///////////////
		for (String realKey : realDpMap.keySet()) {
			RealPoint realDP = realDpMap.get(realKey);
			if (!"".equals(realDP.condition)) {
				GKEP parserCondition = new GKEP();
				parserCondition.setAllowUndeclared(true);
				parserCondition.parseExpression(realDP.condition);
				parserCondition.getSymbolTable();

				SymbolTable conSymbols = parserCondition.getSymbolTable();
				for (Iterator<?> iter = conSymbols.keySet().iterator(); iter.hasNext();) {
					String key = (String) iter.next();
					RealPoint tempRealDP = realDpMap.get(key);
					parserCondition.addVariable(key, tempRealDP.measureValue);
				}
				if (parserCondition.getValue() > 0) {
					String actionExpr[] = realDP.action.split("=");
					if (actionExpr.length == 2) {
						GKEP parserAction = new GKEP();
						parserAction.setAllowUndeclared(true);
						parserAction.parseExpression(actionExpr[1]);
						parserAction.getSymbolTable();
						SymbolTable actSymbols = parserAction.getSymbolTable();
						for (Iterator<?> iter = actSymbols.keySet().iterator(); iter.hasNext();) {
							String key = (String) iter.next();
							RealPoint tempRealDP = realDpMap.get(key);
							parserAction.addVariable(key, tempRealDP.measureValue);
						}
						realDP.measureValue = parserAction.getValue();
						mLogger.info("...validation rule : oi = " + realDP.objectIdentifierName + ", measureValue = " + realDP.measureValue
								+ ", channel id =" + realDP.channelId);
					}
				}
			}

			if (((msgCount % maxChannelPerMsg) == 0) && (msgCount != 0)) {
				result.add(dataBuffer.toString());
				dataBuffer = new StringBuilder();
			}
			StringBuilder message = new StringBuilder();
			long timeStamp = gkRemoteDevice.getQueryTime();
			mLogger.debug("Time from bacnet device:" + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(timeStamp)));
			message.append("|DEVICEID=" + getCategory() + "-" + getIdSentToServer() + "-" + realDP.channelId + "-0");
			message.append(";TIMESTAMP=" + timeStamp);
			mLogger.debug(realDP.toString());
			if (realDP.hasError) {
				message.append(";ERROR=Invalid data point");
			} else {
				message.append(";" + realDP.measureName + "=" + vformat.format(realDP.measureValue) + "," + realDP.measureUnit);
				if (realDP.hasConsumption) {
					message.append(";" + realDP.consumptionName + "=" + vformat.format(realDP.consumptionValue) + "," + realDP.consumptionUnit);
				}
			}

			msgCount++;
			dataBuffer.append(message);
		}
		// ///////////////

		for (Integer virtualKey : virtualDpMap.keySet()) {
			if (((msgCount % maxChannelPerMsg) == 0) && (msgCount != 0)) {
				result.add(dataBuffer.toString());
				dataBuffer = new StringBuilder();
			}
			VirtualPoint virtualDP = virtualDpMap.get(virtualKey);
			List<String> oiNameList = virtualDP.variables;
			GKEP parser = virtualDP.parser;
			for (String oiName : oiNameList) {
				RealPoint dependingDP = realDpMap.get(oiName);
				double val = 0;
				if (dependingDP != null) {
					val = dependingDP.measureValue;
				} else {
					if (oiName.contains("consumption")) {
						String[] var = oiName.split("_");
						if (var.length == 2) {
							dependingDP = realDpMap.get(var[1]);
							if (dependingDP != null) {
								val = dependingDP.consumptionValue;
							}
						}
					}
				}
				parser.addVariable(oiName, val);
			}

			double virtualDPValue = parser.getValue();
			virtualDP.measureValue = virtualDPValue * virtualDP.measureRatio;

			StringBuilder message = new StringBuilder();
			long timeStamp = gkRemoteDevice.getQueryTime();
			mLogger.debug("Time from bacnet device:" + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(timeStamp)));
			mLogger.debug(virtualDP.toString());
			message.append("|DEVICEID=" + getCategory() + "-" + getIdSentToServer() + "-" + virtualDP.channelId + "-0");
			message.append(";TIMESTAMP=" + timeStamp);
			message.append(";" + virtualDP.measureName + "=" + vformat.format(virtualDP.measureValue) + "," + virtualDP.measureUnit);
			msgCount++;
			dataBuffer.append(message);
		}

		result.add(dataBuffer.toString());
		return result;
	}
	
	public void queryingConcurrenceBACnetRemoteDevice() {
		int instancenumber = gkRemoteDevice.getInstanceNumber();
		Address address = gkRemoteDevice.getAddress();
		Network network = gkRemoteDevice.getNetwork();
		InetSocketAddress addr = new InetSocketAddress(address.toIpString(), 0xBAC0);
		try {
			mLogger.info("[queryingConcurrenceBACnetRemoteDevice] querying data for " + addr +", id " + instancenumber + ", network " + network +". Set state to false");

			// Get object name
			ReadPropertyRequest read = new ReadPropertyRequest(new ObjectIdentifier(ObjectType.device, instancenumber), PropertyIdentifier.objectName);
			ReadPropertyAck ack = (ReadPropertyAck) localDevice.send(addr, network, 1476, Segmentation.segmentedBoth, read);
			CharacterString charstring = (CharacterString) ack.getValue();
			mLogger.info("[queryingConcurrenceBACnetRemoteDevice] Object name:" + charstring);

			// Get MaxApduLenthAccepted
			read = new ReadPropertyRequest(new ObjectIdentifier(ObjectType.device, instancenumber), PropertyIdentifier.maxApduLengthAccepted);
			ack = (ReadPropertyAck) localDevice.send(addr, network, 1476, Segmentation.segmentedBoth, read);
			UnsignedInteger unsignint = (UnsignedInteger) ack.getValue();
			//Japan Project
			//int apdumaxLength = 1476;
			int apdumaxLength = unsignint.intValue();
			mLogger.info("[queryingConcurrenceBACnetRemoteDevice] MaxApduLenthAccepted:" + unsignint);

			SequenceOf<ObjectIdentifier> objectList = gkRemoteDevice.getObjectList();
			SequenceOf<PropertyReference> propertyDemands = new SequenceOf<PropertyReference>();
			propertyDemands.add(new PropertyReference(PropertyIdentifier.objectName));
			propertyDemands.add(new PropertyReference(PropertyIdentifier.units));
			propertyDemands.add(new PropertyReference(PropertyIdentifier.presentValue));
			//propertyDemands.add(new PropertyReference(PropertyIdentifier.description));
			//propertyDemands.add(new PropertyReference(PropertyIdentifier.deviceType));
			//propertyDemands.add(new PropertyReference(PropertyIdentifier.outOfService));

			SequenceOf<ReadAccessSpecification> dataAccessRequests = new SequenceOf<ReadAccessSpecification>();

			int i = 0;
			int sizeOfObjectList = objectList.getValues().size();
			//int mod = sizeOfObjectList % maxOiPerQuery;
			for (ObjectIdentifier oid : objectList) {
				dataAccessRequests.add(new ReadAccessSpecification(oid, propertyDemands));
				i++;
				if (((i % maxOiPerQuery) == 0) || (sizeOfObjectList == i))
				{
					ReadPropertyMultipleRequest readMul = new ReadPropertyMultipleRequest(dataAccessRequests);
					ReadPropertyMultipleAck ackMul = (ReadPropertyMultipleAck) localDevice.send(addr, network, apdumaxLength,
							Segmentation.segmentedBoth, readMul);
					SequenceOf<ReadAccessResult> values = (SequenceOf<ReadAccessResult>) ackMul.getListOfReadAccessResults();
					for (ReadAccessResult value : values)
					{
						mLogger.debug("[queryingConcurrenceBACnetRemoteDevice]  ReadAccessResult  ");

						SequenceOf<Result> listOfResults = value.getListOfResults();
						GKRemoteObject gkrmobject = gkRemoteDevice.getObject(value.getObjectIdentifier());
						if (gkrmobject == null) {
							gkrmobject = new GKRemoteObject(gkRemoteDevice, value.getObjectIdentifier());

						}
						for (Result result : listOfResults) {
							if (!result.isError()) {
								Encodable encode = (Encodable) result.getReadResult().getDatum();
								PropertyIdentifier pid = result.getPropertyIdentifier();
								gkrmobject.setProperty(pid, encode);
							}
						}
						gkRemoteDevice.setObject(gkrmobject);
//						mLogger.debug("Object:" + gkrmobject.getId() + ",name:" + gkrmobject.getProperty(PropertyIdentifier.objectName)
//								+ ",instance:" + gkrmobject.getInstanceId() + ",value:" + gkrmobject.getProperty(PropertyIdentifier.presentValue)
//								+ ",unit:" + gkrmobject.getProperty(PropertyIdentifier.units));

					}
					dataAccessRequests.getValues().clear();
				}
			}
			errorCount = 0;
		} catch (BACnetException e) {
			errorCount++;
			mLogger.error("BACnetException", e);
		} catch (BACnetServiceException e) {
			errorCount++;
			mLogger.error("BACnetServiceException", e);
		} catch (Exception e) {
			errorCount++;
			mLogger.error("Exception", e);
		}

		long time = System.currentTimeMillis();
		gkRemoteDevice.setQueryTime(time);
		mLogger.info("Set state to true");
		mLogger.debug("finish query at:" + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(time)));

	}

	public void queryingSequenceBACnetRemoteDevice() {
		int instancenumber = gkRemoteDevice.getInstanceNumber();
		Address address = gkRemoteDevice.getAddress();
		Network network = gkRemoteDevice.getNetwork();
		InetSocketAddress addr = new InetSocketAddress(address.toIpString(), 0xBAC0);
		try {
			mLogger.info("[queryingSequenceBACnetRemoteDevice] querying data for " + addr +", id " + instancenumber + ", network " + network +". Set state to false");
			// Get object name
			ReadPropertyRequest read = new ReadPropertyRequest(new ObjectIdentifier(ObjectType.device, instancenumber), PropertyIdentifier.objectName);
			ReadPropertyAck ack = (ReadPropertyAck) localDevice.send(addr, network, 1476, Segmentation.segmentedBoth, read);
			CharacterString charstring = (CharacterString) ack.getValue();
			mLogger.info("[queryingSequenceBACnetRemoteDevice] Object name:" + charstring);

			// Get MaxApduLenthAccepted
			read = new ReadPropertyRequest(new ObjectIdentifier(ObjectType.device, instancenumber), PropertyIdentifier.maxApduLengthAccepted);
			ack = (ReadPropertyAck) localDevice.send(addr, network, 1476, Segmentation.segmentedBoth, read);
			UnsignedInteger unsignint = (UnsignedInteger) ack.getValue();
			//Japan Project
			//int apdumaxLength = 1476;
			int apdumaxLength = unsignint.intValue();
			mLogger.info("[queryingSequenceBACnetRemoteDevice] MaxApduLenthAccepted:" + unsignint);

			SequenceOf<ObjectIdentifier> objectList = gkRemoteDevice.getObjectList();
			SequenceOf<PropertyReference> propertyDemands = new SequenceOf<PropertyReference>();
			propertyDemands.add(new PropertyReference(PropertyIdentifier.objectName));
			propertyDemands.add(new PropertyReference(PropertyIdentifier.units));
			propertyDemands.add(new PropertyReference(PropertyIdentifier.presentValue));
			// propertyDemands.add(new PropertyReference(PropertyIdentifier.description));
			// propertyDemands.add(new PropertyReference(PropertyIdentifier.deviceType));
			// propertyDemands.add(new PropertyReference(PropertyIdentifier.outOfService));

			for (ObjectIdentifier oid : objectList) {
				GKRemoteObject gkrmobject = gkRemoteDevice.getObject(oid);
				if (gkrmobject == null) {
					gkrmobject = new GKRemoteObject(gkRemoteDevice, oid);

				}
				Encodable encode;
				PropertyIdentifier pid = null;
				for (PropertyReference property : propertyDemands) {
					pid = property.getPropertyIdentifier();
					read = new ReadPropertyRequest(oid, pid);
					ack = (ReadPropertyAck) localDevice.send(addr, network, apdumaxLength, Segmentation.segmentedBoth, read);
					mLogger.debug("[queryingSequenceBACnetRemoteDevice]  ReadPropertyAck  " + ack);

					encode = (Encodable) ack.getValue();
					gkrmobject.setProperty(pid, encode);
				}
				gkRemoteDevice.setObject(gkrmobject);
				mLogger.debug("Object:" + gkrmobject.getId() + ",name:" + gkrmobject.getProperty(PropertyIdentifier.objectName)
						+ ",instance:" + gkrmobject.getInstanceId() + ",value:" + gkrmobject.getProperty(PropertyIdentifier.presentValue)
						+ ",unit:" + gkrmobject.getProperty(PropertyIdentifier.units));
			}
			errorCount = 0;
		} catch (BACnetException e) {
			errorCount++;
			mLogger.error("BACnetException", e);
		} catch (BACnetServiceException e) {
			errorCount++;
			mLogger.error("BACnetServiceException", e);
		}  catch (Exception e) {
			errorCount++;
			mLogger.error("Exception", e);
		}

		long time = System.currentTimeMillis();
		gkRemoteDevice.setQueryTime(time);
		mLogger.info("Set state to true");
		mLogger.debug("finish query at:" + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(time)));
	}

	// Setter and Getter

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public int getInstanceid() {
		return instanceid;
	}

	public void setInstanceid(int instanceid) {
		this.instanceid = instanceid;
	}

	public int getAlternativeid() {
		return alternativeid;
	}

	public void setAlternativeid(int alternativeid) {
		this.alternativeid = alternativeid;
	}

	public GKRemoteDevice getGkRemoteDevice() {
		return gkRemoteDevice;
	}

	public void setGkRemoteDevice(GKRemoteDevice gkRemoteDevice) {
		this.gkRemoteDevice = gkRemoteDevice;
	}

	public List<String> getRealDatapointIdList() {
		return realDatapointIdList;
	}

	public void setRealDatapointIdList(List<String> realDatapointIdList) {
		this.realDatapointIdList = realDatapointIdList;
	}

	public Map<String, RealPoint> getRealDatapointMap() {
		return realDatapointMap;
	}

	public void setRealDatapointMap(Map<String, RealPoint> realDatapointMap) {
		this.realDatapointMap = realDatapointMap;
	}

	public Map<Integer, VirtualPoint> getVirtualDatapointMap() {
		return virtualDatapointMap;
	}

	public void setVirtualDatapointMap(Map<Integer, VirtualPoint> virtualDatapointMap) {
		this.virtualDatapointMap = virtualDatapointMap;
	}

	public List<ValidationRule> getValidationRuleList() {
		return validationRuleList;
	}

	public void setValidationRuleList(List<ValidationRule> validationRuleList) {
		this.validationRuleList = validationRuleList;
	}

	public long getQueryingtTimeout() {
		return queryingtTimeout;
	}

	public void setQueryingtTimeout(long queryingtTimeout) {
		this.queryingtTimeout = queryingtTimeout;
	}
	
	public LocalDevice getLocalDevice() {
		return localDevice;
	}

	public void setLocalDevice(LocalDevice localdevice) {
		this.localDevice = localdevice;
	}

	public int getMaxOiPerQuery() {
		return maxOiPerQuery;
	}

	public void setMaxOiPerQuery(int maxOiPerQuery) {
		this.maxOiPerQuery = maxOiPerQuery;
	}

	public int getMaxChannelPerMsg() {
		return maxChannelPerMsg;
	}

	public void setMaxChannelPerMsg(int maxChannelPerMsg) {
		this.maxChannelPerMsg = maxChannelPerMsg;
	}

	public List<Integer> getVirtualDatapointIdList() {
		return virtualDatapointIdList;
	}

	public void setVirtualDatapointIdList(List<Integer> virtualDatapointIdList) {
		this.virtualDatapointIdList = virtualDatapointIdList;
	}
}
