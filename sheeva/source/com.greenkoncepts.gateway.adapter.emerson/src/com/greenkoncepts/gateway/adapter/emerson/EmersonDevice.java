package com.greenkoncepts.gateway.adapter.emerson;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class EmersonDevice  extends AModbusDevice {
	public EmersonDevice(String category, int addr) {
		super(addr, category);
	}
}
