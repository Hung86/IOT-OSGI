package com.greenkoncepts.gateway.adapter.dummy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class LightingControl extends DummyDevice {


	private long burningHours = 0;
	private int relayStatus = 0;
	private double activeEnergy = 0;
	private int analogOutput = 0;
	private int pereiodTime = 0;
	public LightingControl(int addr, String cat) {
		super(addr, cat);
		restoreBurningHours();
	}

	@Override
	public String getDeviceData() {
		generateDummyData();
		StringBuffer data = new StringBuffer();
		timestamp = System.currentTimeMillis();
		data.append("|DEVICEID=" + getId() + "-0-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Relay Status=" + relayStatus+ ",None");
		data.append(";Running Hours Lamp=" + burningHours + ",None");
		data.append(";Active Energy=" + activeEnergy + ",Wh");
		data.append(";Analog Output=" + analogOutput + "None");
		return data.toString();
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void generateDummyData() {
		pereiodTime++;
		if (pereiodTime < 30) {
			relayStatus = 0;
			activeEnergy = 0;
		} else {
			relayStatus = 1;
			activeEnergy = 80 + (Math.random() * 30); 

			if (pereiodTime > 60) {
				pereiodTime = 0;
			}
		}
		
		analogOutput= 60;
		if (relayStatus != 0) {
			burningHours++;
			saveBurningHours();
		}
		
	}

	private void saveBurningHours() {
    	Properties _props = new Properties();
    	try {
    		_props.setProperty("burning_hours", ""+burningHours);
		    File file = new File("lighting_data.prop");
		    if (!file.exists()) {
		    	file.createNewFile();
		    }
		    file.setReadable(true, false);
		    file.setWritable(true, false);
		    _props.store(new FileOutputStream(file), "Save data");
		} catch (IOException e) {
			mLogger.error("IOException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		}
	}
	
	private void restoreBurningHours() {
    	Properties _props = new Properties();
    	try {
    		FileInputStream in = new FileInputStream( "lighting_data.prop" );
    		_props.load( in );
			in.close();
    		burningHours = Long.parseLong(_props.getProperty("burning_hours","0"));
		} catch (IOException e) {
			mLogger.error("IOException", e);
		} catch (NumberFormatException e) {
			mLogger.error("NumberFormatException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		}
	}
}
