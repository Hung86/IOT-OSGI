package com.greenkoncepts.gateway.adapter.contrec;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class ContrecDevice extends AModbusDevice {
	public ContrecDevice(String category, int addr) {
		super(addr, category);
	}
}
