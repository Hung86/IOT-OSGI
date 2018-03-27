package com.greenkoncepts.gateway.adapter.sitelab;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class SitelabDevice extends AModbusDevice {
	public SitelabDevice(String category, int addr) {
		super(addr, category);
	}
}
