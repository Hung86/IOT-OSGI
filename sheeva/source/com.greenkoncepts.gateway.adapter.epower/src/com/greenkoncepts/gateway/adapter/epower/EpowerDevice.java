package com.greenkoncepts.gateway.adapter.epower;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class EpowerDevice extends AModbusDevice {
	public EpowerDevice(String category, int addr) {
		super(addr, category);
	}
}
