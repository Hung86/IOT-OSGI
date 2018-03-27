package com.greenkoncepts.gateway.phidgets;

import java.util.List;

import com.greenkoncepts.gateway.api.adapter.AModbusDevice;

public abstract class PhidgetsDevice extends AModbusDevice {
	static final String SENSOR_VIBRATION_1104 = "1104";
	static final String SENSOR_MOTION_1111 = "1111";
	static final String SENSOR_PRESSURE_1115 = "1115";
	static final String SENSOR_TEMP_1124 = "1124";
	static final String SENSOR_TEMP_RH_1125 = "1125";
	static final String SENSOR_PRECISION_LUX_1127 = "1127";
	static final String SENSOR_PH_ORP_1130 = "1130";
	static final String SENSOR_SOUND_1133 = "1133";
	static final String SENSOR_LUX_1000_1142 = "1142";
	static final String SENSOR_LUX_70000_1143 = "1143";
	static final String DUMMY_DEVICE = "9999";
	
	List<?> input_types;
	
	public PhidgetsDevice(String category, int addr,List<?> inputs) {
		super(addr, category);
		input_types = inputs;
	}

	
	public abstract void stop();
	
	public int getCommunicationErrors() {
		return errorCount;
	}
}
