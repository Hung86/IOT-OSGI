package com.greenkoncepts.gateway.adapter.kam;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class KamDevice extends AModbusDevice {
	public KamDevice(String category, int addr) {
		super(addr, category);
		mLogger.info("==== instance device address " + addr);
	}
}

