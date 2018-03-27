package com.greenkoncepts.gateway.transport.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.greenkoncepts.gateway.api.transport.GWModbus;

public class GWModbusTCPImp implements GWModbus {
	private int errorCount = 0;
	private ModbusTCPMaster mtm = null;
	private String gk_address = null;
	private int gk_port = 0;
	private int gk_timeout = 0;
	private boolean hasConnection = false;
	protected Logger mLogger = LoggerFactory.getLogger(getClass().getSimpleName());
	
	public GWModbusTCPImp(String addr, String port, String timeout){
		gk_address = addr;
		gk_port = Integer.parseInt(port);
		gk_timeout = Integer.parseInt(timeout);
		mtm = new ModbusTCPMaster(gk_address, gk_port, gk_timeout);
	}
	
	public String getPort() {
		return String.valueOf(gk_port);
	}
	
	public boolean isConnect() {
		return hasConnection;
	}
	
	public void connect() {
		if (!isConnect()) {
			try {
				if (mtm != null) {
					mtm.connect();
					hasConnection = true;
					errorCount = 0;
				}
			} catch (Exception e) {
				mLogger.error("Exception", e);
			}
		}
	}

	public void disconnect() {
		if (isConnect()) {
			try {
				if (mtm != null) {
					mtm.disconnect();
					hasConnection = false;
				}
			} catch (Exception e) {
				mLogger.error("Exception", e);
			}
		}
	}

	@Override
	public void reconnect() {
		try {
			if (isConnect()) {
				disconnect();
				Thread.sleep(500);
			}
			connect();
		} catch (Exception e) {
			mLogger.error("Exception", e);
		}
	}
	
	@Override
	public int getErrorCount() {
		return errorCount;
	}

	@Override
	public byte[] readCoilRegisters(int unitid, int ref, int count) {
		byte[] results = null;
		try {
			results= mtm.readCoils(unitid, ref, count);
		} catch (ModbusException e) {
			errorCount++;
			mLogger.error("ModbusException", e);
		} catch (Exception e) {
			errorCount++;
			mLogger.error("Exception", e);
		}
		
		return results;
	}
	
	public byte[] readHoldingRegisters(int rtuid, int ref, int count)  {
		byte[] results = null;
		try {
			results= mtm.readMultipleRegisters(rtuid, ref, count);
		} catch (ModbusException e) {
			errorCount++;
			mLogger.error("ModbusException", e);
		} catch (Exception e) {
			errorCount++;
			mLogger.error("Exception", e);
		}
		
		return results;
	}
	
	public byte[] readInputRegisters(int rtuid, int ref, int count)  {
		byte[] results = null;
		try {
			results = mtm.readInputRegisters(rtuid, ref, count);
		} catch (ModbusException e) {
			errorCount++;
			mLogger.error("ModbusException", e);
		} catch (Exception e) {
			errorCount++;
			mLogger.error("Exception", e);
		}
		
		return results;
	}
	
	@Override
	public byte[] writeMultipleRegisters(int unitid, int ref, byte[] data) {
		byte[] results = null;
		try {
			int len = data.length/2;
			Register[] registers = new Register[len];
			for (int i = 0; i < len; i++) {
				registers[i] = new SimpleRegister(data[2*i], data[2*i + 1]);
			}
			results =  mtm.writeMultipleRegisters(unitid, ref, registers);
		} catch (ModbusException e) {
			errorCount++;
			mLogger.error("ModbusException", e);
		} catch (Exception e) {
			errorCount++;
			mLogger.error("Exception", e);
		}
		
		return results;
	}
	
	@Override
	public byte[] writeSingleRegister(int unitid, int ref, byte[] data) {
		byte[] results = null;
		try {
			Register register = new SimpleRegister(data[0], data[1]);
			results = mtm.writeSingleRegister(unitid, ref, register);
		} catch (ModbusException e) {
			errorCount++;
			mLogger.error("ModbusException", e);
		} catch (Exception e) {
			errorCount++;
			mLogger.error("Exception", e);
		}
		
		return results;
	}
	public String toString() {
		return "Modbus TCP(" + gk_address + "," + gk_port + "," + gk_timeout + ")";
	}
	
}
