package com.greenkoncepts.gateway.adapter.energetix.powermeter;

import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class EcosailDevice extends AModbusDevice {
	public EcosailDevice(String category, int addr) {
		super(addr, category);
	}

	public void setAttributes(List<Map<String, String>> attributes) {
		
	}
}
