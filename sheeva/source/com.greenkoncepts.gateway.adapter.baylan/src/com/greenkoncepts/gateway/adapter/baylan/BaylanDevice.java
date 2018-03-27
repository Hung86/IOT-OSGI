package com.greenkoncepts.gateway.adapter.baylan;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class BaylanDevice extends AModbusDevice {
	public BaylanDevice(String category, int addr) {
		super(addr, category);
	}
}
