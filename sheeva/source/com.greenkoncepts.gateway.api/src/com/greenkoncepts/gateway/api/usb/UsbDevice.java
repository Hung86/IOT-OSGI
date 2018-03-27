package com.greenkoncepts.gateway.api.usb;

/**
 * Interface for all USB devices
 */
public interface UsbDevice {
	
	/**
	 * The vendor ID of the device
	 * 
	 * @return		The vendor ID of the device
	 */
	public String getVendorId();
	
	/**
	 * The product ID of the device
	 * 
	 * @return		The product ID of the device
	 */
	public String getProductId();
	
	/**
	 * The manufacturer name of the device
	 * 
	 * @return		The manufacturer name of the device
	 */
	public String getManufacturerName();
	
	/**
	 * The product name of the device
	 * 
	 * @return		The product name of the device
	 */
	public String getProductName();
	
	/**
	 * The USB bus number of the device
	 * 
	 * @return		The USB bus number of the device
	 */
	public String getUsbBusNumber();
	
	/**
	 * The USB device path
	 * 
	 * @return		The USB device path
	 */
	public String getUsbDevicePath();
	
	/**
	 * The complete USB port (USB bus number plus USB device path)
	 * 
	 * @return		The complete USB port (USB bus number plus USB device path)
	 */
	public String getUsbPort();
	
}