package com.greenkoncepts.gateway.adapter.inepro;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class IneproDevice extends AModbusDevice {
	public IneproDevice(String category, int addr) {
		super(addr, category);
	}
}