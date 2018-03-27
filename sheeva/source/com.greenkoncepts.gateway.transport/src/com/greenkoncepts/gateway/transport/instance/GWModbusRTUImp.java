package com.greenkoncepts.gateway.transport.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import com.greenkoncepts.gateway.api.transport.GWModbus;

public class GWModbusRTUImp implements GWModbus {
	private int errorCount = 0;
	private ModbusSerialMaster msm = null;
	private SerialParameters params = null;
	private boolean hasConnection = false;
	private Logger mLogger = LoggerFactory.getLogger(getClass().getSimpleName());

	public GWModbusRTUImp(String port, String baudrate, String stopbit, String parity, String timeout) {
		params = new SerialParameters();
		params.setPortName(port);
		params.setBaudRate(baudrate);
		params.setDatabits(8);
		params.setParity(parity);
		params.setStopbits(stopbit);
		params.setEncoding("rtu");
		params.setEcho(false);
		params.setQueryingTimeout(timeout);
		msm = new ModbusSerialMaster(params);
	}
	
	public String getBaudrate() {
		return params.getBaudRateString();
	}
	
	public String getStopbit() {
		return params.getStopbitsString();
	}
	
	public String getParity() {
		return params.getParityString();
	}

	public boolean isConnect() {
		return hasConnection;
	}

	public void connect() {
		if (!isConnect()) {
			try {
				if (msm != null) {
					msm.connect();
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
				if (msm != null) {
					msm.disconnect();
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
			results = msm.readCoils(unitid, ref, count);
		} catch (ModbusException e) {
			errorCount++;
			mLogger.error("ModbusException", e);
		} catch (Exception e) {
			errorCount++;
			mLogger.error("Exception", e);
		}

		return results;
	}
	
	public byte[] readHoldingRegisters(int unitid, int ref, int count) {
		byte[] results = null;
		try {
			results = msm.readMultipleRegisters(unitid, ref, count);
		} catch (ModbusException e) {
			errorCount++;
			mLogger.error("ModbusException", e);
		} catch (Exception e) {
			errorCount++;
			mLogger.error("Exception", e);
		}

		return results;
	}

	public byte[] readInputRegisters(int unitid, int ref, int count) {
		byte[] results = null;
		try {
			results = msm.readInputRegisters(unitid, ref, count);
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
			int len = data.length / 2;
			Register[] registers = new Register[len];
			for (int i = 0; i < len; i++) {
				registers[i] = new SimpleRegister(data[2 * i], data[2 * i + 1]);
			}
			results = msm.writeMultipleRegisters(unitid, ref, registers);
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
			results = msm.writeSingleRegister(unitid, ref, register);
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
		return "Modbus RTU(" + params.getPortName() + "," + params.getBaudRateString() + "," + params.getStopbitsString() + ","
				+ params.getParityString() + "," + params.getQueryingTimeoutString() + ")";
	}

}
