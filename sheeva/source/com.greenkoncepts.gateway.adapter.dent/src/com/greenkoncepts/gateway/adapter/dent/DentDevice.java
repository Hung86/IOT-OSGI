package com.greenkoncepts.gateway.adapter.dent;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class DentDevice extends AModbusDevice {
	public DentDevice(String category, int addr) {
		super(addr, category);
	}
}
