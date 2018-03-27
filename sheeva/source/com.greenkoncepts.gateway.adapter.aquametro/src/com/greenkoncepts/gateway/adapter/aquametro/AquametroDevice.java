package com.greenkoncepts.gateway.adapter.aquametro;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class AquametroDevice extends AModbusDevice {

	public AquametroDevice(String category, int addr) {
		super(addr, category);
	}
}
