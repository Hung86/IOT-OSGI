package com.greenkoncepts.gateway.adapter.riello;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class RielloDevice extends AModbusDevice {
	public RielloDevice(String category, int addr) {
		super(addr, category);
	}
}
