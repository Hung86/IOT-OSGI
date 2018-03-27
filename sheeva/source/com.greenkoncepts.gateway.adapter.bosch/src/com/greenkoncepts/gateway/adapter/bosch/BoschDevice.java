package com.greenkoncepts.gateway.adapter.bosch;

import com.greenkoncepts.gateway.api.adapter.AHttpDevice;

public abstract class BoschDevice extends AHttpDevice {

	public BoschDevice(int id, String cat) {
		super(id, cat);
	}
}
