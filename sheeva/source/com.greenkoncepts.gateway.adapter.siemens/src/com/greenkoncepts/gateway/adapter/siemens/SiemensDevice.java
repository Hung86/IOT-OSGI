package com.greenkoncepts.gateway.adapter.siemens;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class SiemensDevice extends AModbusDevice {
	public SiemensDevice(String category, int addr) {
		super(addr, category);
	}
}
