	package com.greenkoncepts.gateway.adapter.dummy;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class DummyDevice extends AModbusDevice {	
	public DummyDevice(int addr, String cat) {
		super(addr, cat);
	}

}
