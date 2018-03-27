package com.greenkoncepts.gateway.adapter.monnit;

import com.greenkoncepts.gateway.api.adapter.AHttpDevice;

public abstract class MonnitDevice extends AHttpDevice {
	public String gatewayid = "-1";
	public String messageDate = "";
	public boolean hasValue = false;
	
	public MonnitDevice(int id, String cat) {
		super(id, cat);
	}
}
