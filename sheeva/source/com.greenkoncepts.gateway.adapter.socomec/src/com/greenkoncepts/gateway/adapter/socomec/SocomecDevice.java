package com.greenkoncepts.gateway.adapter.socomec;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class SocomecDevice extends AModbusDevice {
	public SocomecDevice(String category, int addr) {
		super(addr, category);
	}
}
