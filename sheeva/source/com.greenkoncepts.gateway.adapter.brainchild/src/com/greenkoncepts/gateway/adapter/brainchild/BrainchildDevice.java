package com.greenkoncepts.gateway.adapter.brainchild;

import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class BrainchildDevice extends AModbusDevice {
	public BrainchildDevice(String category, int addr) {
		super(addr, category);
	}
	abstract public void setDeviceAttributes(List<Map<String, String>> attr);
	abstract public String getDeviceStateData();
}