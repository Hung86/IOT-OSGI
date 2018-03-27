package com.greenkoncepts.gateway.adapter.circuitcontroller;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class CircuitControllerDevice extends AModbusDevice {
	public CircuitControllerDevice(int id, String cat) {
		super(id, cat);
	}
	
}
