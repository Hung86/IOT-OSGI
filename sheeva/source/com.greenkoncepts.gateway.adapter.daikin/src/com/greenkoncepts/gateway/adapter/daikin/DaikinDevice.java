package com.greenkoncepts.gateway.adapter.daikin;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class DaikinDevice extends AModbusDevice {
	public DaikinDevice(String category, int addr) {
		super(addr, category);
	}

}
