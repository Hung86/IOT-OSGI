package com.greenkoncepts.gateway.adapter.entes;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class EntesDevice extends AModbusDevice {
	public EntesDevice(String category, int addr) {
		super(addr, category);
	}
}