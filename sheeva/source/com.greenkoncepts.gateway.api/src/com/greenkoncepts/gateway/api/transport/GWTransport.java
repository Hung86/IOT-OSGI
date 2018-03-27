package com.greenkoncepts.gateway.api.transport;

public interface GWTransport {
	GWModbus getModbusRTUInstance(String port, String baudrate, String stopbit, String parity, String timeout);
	GWModbus getModbusTCPInstance(String addr, String port, String timeout);
}
