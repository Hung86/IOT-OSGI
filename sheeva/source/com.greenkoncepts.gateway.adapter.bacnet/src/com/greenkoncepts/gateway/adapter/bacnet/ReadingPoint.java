package com.greenkoncepts.gateway.adapter.bacnet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.nfunk.jep.SymbolTable;

import com.greenkoncepts.expression.GKEP;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

public abstract class ReadingPoint extends DataPoint {

	public float measureRatio;
	public double measureValue;
	ReadingPoint() {
		super();
		measureRatio = 1;
		measureValue = 0;

	}
}

class RealPoint extends ReadingPoint {
	public ObjectIdentifier objectIdentifier;
	public String objectIdentifierName;
	public String consumptionName;
	public String consumptionUnit;
	public float consumptionRatio;
	public double consumptionValue;
	public boolean hasConsumption;
	private Hashtable<String, List<String>> _consumptionTable  = new Hashtable<String, List<String>>();


	// datapoint

	RealPoint() {
		super();
		objectIdentifier = null;
		hasConsumption = false;
		consumptionName = "";
		consumptionUnit = "";
		consumptionRatio  = 1;
		consumptionValue = 0;
		objectIdentifierName = "";
	}
	
	public void initializeObject(Map<String, String> dp) {
		_consumptionTable.put("Active Energy Reading", Arrays.asList("Active Energy", "Wh", "1000"));
		_consumptionTable.put("Reactive Energy Reading", Arrays.asList("Reactive Energy", "VARh", "1000"));
		_consumptionTable.put("Apparent Energy Reading", Arrays.asList("Apparent Energy", "VAh", "1000"));
		_consumptionTable.put("Regenerated Energy Reading", Arrays.asList("Regenerated Energy", "Wh", "1000"));
		_consumptionTable.put("Cooling Consumption Reading", Arrays.asList("Cooling Consumption", "Wh", "1000"));
		_consumptionTable.put("Heating Consumption Reading", Arrays.asList("Heating Consumption", "Wh", "1000"));
		_consumptionTable.put("Water Volume Reading", Arrays.asList("Water Volume", "cu m", "1"));
		_consumptionTable.put("Diesel Volume Reading", Arrays.asList("Diesel Volume", "l", "1"));
		
		String tempValue = dp.get("data_point");
		if ((tempValue != null) && (!tempValue.isEmpty())) {
			datapointId = Integer.parseInt(tempValue);
		}
		
		tempValue = dp.get("channel");
		if ((tempValue != null) && (!tempValue.isEmpty())) {
			channelId = Integer.parseInt(tempValue);
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
		
		tempValue = dp.get("measure_ratio");
		if ((tempValue != null) && (!tempValue.isEmpty())) {
			measureRatio = Float.parseFloat(tempValue);
		}
		
		tempValue = dp.get("consumption");
		if ((tempValue != null) && (!tempValue.isEmpty())) {
			hasConsumption = Boolean.parseBoolean(tempValue);
			if (hasConsumption) {
				List<String> consumption = _consumptionTable.get(measureName);
				consumptionName = consumption.get(0);
				consumptionUnit = consumption.get(1);
				consumptionRatio = Float.parseFloat(consumption.get(2));
			}
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
//		if (OIName != null) {
//			ObjectType obtype = null;
//			if (OIName.startsWith("ai")) {
//				obtype = ObjectType.analogInput;
//			} else if (OIName.startsWith("ao")) {
//				obtype = ObjectType.analogOutput;
//			} else if (OIName.startsWith("av")) {
//				obtype = ObjectType.analogValue;
//			} else if (OIName.startsWith("bi")) {
//				obtype = ObjectType.binaryInput;
//			} else if (OIName.startsWith("bo")) {
//				obtype = ObjectType.binaryOutput;
//			} else {
//				mLogger.error("Object Identifier has wrong object type in configuration.");
//			}
//			int objectIdentifierId = Integer.parseInt(OIName.substring(2));
//			objectIdentifier = new ObjectIdentifier(obtype, objectIdentifierId);
//		} else {
//			mLogger.error("There is not definition of Object Identifier");
//		}
//		String expr ="(1 == 3) && (N2 > 3)";
//		GKEP parser = new GKEP();
//		parser.
//		parser.setAllowUndeclared(true);
//		parser.parseExpression(expr);
//		parser.
//		String expr1 ="(N3 == 3) && (N4 > 3)";
//		parser.parseExpression(expr1);
////		parser.addVariable("a", 3);
////		parser.addVariable("b", 3);
////		parser.addVariable("c", 3);
////		parser.addVariable("D", 4);
//		System.out.println("-------- 1 1 :"  + parser.getValue());
//		SymbolTable symbols = parser.getSymbolTable();
//		for (Iterator iter = symbols.keySet().iterator(); iter.hasNext();) {
//			String key = (String) iter.next();
//			System.out.println("-----------------2 : " + key);
//		}
	}

	@Override
	public String toString() {
		return ("Real Datapoint : datapointId = " + datapointId + ", channelId = " + channelId + ",  objectIdentifier = " + objectIdentifierName
				+ ", measureName = " + measureName + ", measureUnit = " + measureUnit + ", measureRatio = " + measureRatio
				+ ", hasConsumption  = " + hasConsumption + ", consumptionName = " + consumptionName + ", consumptionUnit = " + consumptionUnit
				+ ", consumptionRatio = " + consumptionRatio
				+ ", condition = " + condition + ", action = " + action);
	}

}

class VirtualPoint extends ReadingPoint {
	public String formula;
	public List<String> variables;
	public GKEP parser;

	VirtualPoint() {
		super();
		variables = new ArrayList<String>();
		parser = new GKEP();
		formula = "";
	}
	
	public void initializeObject(Map<String, String> dp) {
		String tempValue = dp.get("data_point");
		if ((tempValue != null) && (!tempValue.isEmpty())) {
			datapointId = Integer.parseInt(tempValue);
		}
		
		tempValue = dp.get("channel");
		if ((tempValue != null) && (!tempValue.isEmpty())) {
			channelId = Integer.parseInt(tempValue);
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
		
		tempValue = dp.get("measure_ratio");
		if ((tempValue != null) && (!tempValue.isEmpty())) {
			measureRatio = Float.parseFloat(tempValue);
		}
		
		tempValue = dp.get("formula");
		if ((tempValue != null) && (!tempValue.isEmpty())) {
			formula = tempValue;
			buildFormulaParser(formula);
		}		
	}

	void buildFormulaParser(String formula) {
		parser.setAllowUndeclared(true);
		parser.parseExpression(formula);
		parser.getSymbolTable();
		if (parser.hasError()) {
			// print sth
		}
		SymbolTable symbols = parser.getSymbolTable();
		for (Iterator<?> iter = symbols.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			variables.add(key);
		}

	}

	@Override
	public String toString() {
		return ("Virtual Datapoint : datapointId = " + datapointId + ", channelId = " + channelId + ", formula = " + formula + ", measureName = "
				+ measureName + ", measureUnit = " + measureUnit + ", measureRatio = " + measureRatio + ", size of datapoint list  = " + (variables.size())
				+ ", condition = " + condition + ", action = " + action);
	}
}

class ValidationRule {
	String condition;
	String action;
	public ValidationRule(String condition,String action) {
		this.condition = condition;
		this.action = action;
	}
}
