package com.greenkoncepts.gateway.adapter.energetix.sensor;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class MbsensorDevice extends AModbusDevice {
	static protected int INVALID_TEMP = 0xFC19;
	int pikeCount;
	volatile protected boolean validData = true;
	
	public MbsensorDevice(String category, int addr) {
		super(addr, category);
		validData = true;
		pikeCount = 0;
	}
}

