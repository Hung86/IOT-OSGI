package com.greenkoncepts.gateway.adapter.ge.aquatrans;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

abstract class GeAquatransDevice extends AModbusDevice {
	public GeAquatransDevice(String category, int addr) {
		super(addr, category);
	}
}