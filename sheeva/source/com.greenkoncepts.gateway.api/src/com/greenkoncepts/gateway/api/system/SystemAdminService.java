package com.greenkoncepts.gateway.api.system;

/**
 * Service to perform basic system tasks.
 * 
 * Copyright (c) 2012 Eurotech Inc. All rights reserved.
 */
public interface SystemAdminService 
{
	/**
	 * Gets the amount of time this device has been up in milliseconds.
	 * 
	 * @return				How long this device has been up in milliseconds.
	 */
	public String getUptime();
	
	/**
	 * Reboots the device.
	 */
	public void reboot();
	
	/**
	 * Synchronizes data on flash with memory.
	 */
	public void sync();
}
