package com.greenkoncepts.gateway.adapter.janitza;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class JanitzaDevice extends AModbusDevice {
	public JanitzaDevice(String category, int addr) {
		super(addr, category);
	}
}
