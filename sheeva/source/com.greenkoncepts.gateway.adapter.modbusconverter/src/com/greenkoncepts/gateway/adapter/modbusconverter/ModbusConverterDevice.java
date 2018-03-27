package com.greenkoncepts.gateway.adapter.modbusconverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public abstract class ModbusConverterDevice extends AModbusDevice {

	List<Map<String, Integer>> commandList = new ArrayList<Map<String, Integer>>();
	public Hashtable<Integer, ArrayList<DataPoint>> nodeCommandMapping = new Hashtable<Integer, ArrayList<DataPoint>>();

	public ModbusConverterDevice(String category, int addr, HashMap<Integer,DataPoint> nodeTable) {
		super(addr, category);
		ArrayList<Integer> nodeRegisters = new ArrayList<Integer>(nodeTable.keySet());
		Collections.sort(nodeRegisters);
		int startingRegister = -1;
		int numRegisters = 0;
		DataPoint tempNode = null;
		ArrayList<DataPoint> datapointList = null;
		for (int i = 0; i < nodeRegisters.size(); i++) {
			tempNode = nodeTable.get(nodeRegisters.get(i));
			if (tempNode.register == startingRegister + numRegisters) {
				numRegisters += ModbusUtil.getNumOfRegisterFromDataType(tempNode.dataType);
			} else {
				if (startingRegister != -1) {
					Map<String, Integer> command = new Hashtable<String, Integer>();
					command.put("register", startingRegister);
					command.put("length", numRegisters);
					commandList.add(command);
				}
				datapointList = new ArrayList<DataPoint>();
				startingRegister = tempNode.register;
				nodeCommandMapping.put(startingRegister, datapointList);
				numRegisters = ModbusUtil.getNumOfRegisterFromDataType(tempNode.dataType);
			}
			tempNode.index = 2 * (tempNode.register - startingRegister);
			datapointList.add(tempNode);
		}

		if (startingRegister != -1) {
			Map<String, Integer> command = new Hashtable<String, Integer>();
			command.put("register", startingRegister);
			command.put("length", numRegisters);
			commandList.add(command);
		}
		
		for (Map<String, Integer> cmd : commandList) {
			mLogger.debug("------commandList : register = " + cmd.get("register") + " - length = " + cmd.get("length"));
		}
		
		for (Integer key : nodeCommandMapping.keySet()) {
			ArrayList<DataPoint> dataList = nodeCommandMapping.get(key);
			if (dataList != null) {
				for (DataPoint dp : dataList) {
					mLogger.debug("------nodeCommandMapping : " + dp.toString());
				}
			}
		}

	}

}
