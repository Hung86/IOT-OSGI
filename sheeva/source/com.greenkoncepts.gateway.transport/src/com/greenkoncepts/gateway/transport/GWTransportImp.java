package com.greenkoncepts.gateway.transport;

import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greenkoncepts.gateway.api.transport.GWModbus;
import com.greenkoncepts.gateway.api.transport.GWTransport;
import com.greenkoncepts.gateway.transport.instance.GWModbusRTUImp;
import com.greenkoncepts.gateway.transport.instance.GWModbusTCPImp;

public class GWTransportImp implements GWTransport{
	private Map<String, GWModbus> modbusMap = new Hashtable<String, GWModbus>();
	private Logger mLogger = LoggerFactory.getLogger(getClass().getName());
	
	
	protected void activator() {
		
	}
	
	protected void deactivator() {
		
	}
	
	@Override
	synchronized public GWModbus getModbusRTUInstance(String port, String baudrate, String stopbit, String parity, String timeout) {
		if (modbusMap.containsKey(port)) {
			GWModbusRTUImp existedModbus = (GWModbusRTUImp) modbusMap.get(port);
			if (existedModbus.getBaudrate().equals(baudrate) &&
					existedModbus.getParity().equals(parity) &&
					existedModbus.getStopbit().equals(stopbit)) {
				mLogger.info("[getModbusRTUInstance] port " +  port + " was initial !");
				return existedModbus;

			}
			mLogger.error("[getModbusRTUInstance] port " +  port + " is being used !");
			return null;
		}
		mLogger.info("[getModbusRTUInstance] port " +  port + " is initial new instance at first time !");
		GWModbus modbusRTU = new GWModbusRTUImp(port, baudrate, stopbit, parity, timeout);
		modbusMap.put(port, modbusRTU);
		return modbusRTU;
	}

	@Override
	synchronized public GWModbus getModbusTCPInstance(String addr, String port, String timeout) {
		if (modbusMap.containsKey(addr)) {
			GWModbusTCPImp existedModbus = (GWModbusTCPImp)modbusMap.get(addr);
			if (existedModbus.getPort().equals(port)) {
				mLogger.info("[getModbusTCPInstance] ip address " +  addr + " was initial !");
				return existedModbus;
			}
			
			mLogger.error("[getModbusTCPInstance] ip address " +  addr + " is being used !");
			return null;
		}
		mLogger.info("[getModbusTCPInstance] ip address " +  addr + " is initial new instance at first time !");
		GWModbus modbusTCP = new GWModbusTCPImp(addr, port, timeout);
		modbusMap.put(addr, modbusTCP);
		return modbusTCP;
	}
	
}
