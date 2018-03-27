package com.greenkoncepts.gateway.adapter.schneider;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class SchneiderDevice extends AModbusDevice {
	public SchneiderDevice(String category, int addr) {
		super(addr, category);
	}
}
