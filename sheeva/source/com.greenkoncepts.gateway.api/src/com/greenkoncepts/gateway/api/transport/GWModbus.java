package com.greenkoncepts.gateway.api.transport;

public interface GWModbus {
	public boolean isConnect() ;
	public void connect() ;
	public void disconnect();
	public void reconnect();
	public int getErrorCount();
	public byte[] readCoilRegisters(int unitid, int ref, int count) ;
	public byte[] readHoldingRegisters(int unitid, int ref, int count) ;
	public byte[] readInputRegisters(int unitid, int ref, int count);
	public byte[] writeSingleRegister(int unitid, int ref, byte[] data);
	public byte[] writeMultipleRegisters(int unitid, int ref, byte[] data);
	public String toString();
}
