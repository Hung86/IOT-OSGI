package com.greenkoncepts.gateway.adapter.dummy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.greenkoncepts.gateway.util.FuncUtil;

public class Pro1250D extends DummyDevice {

    private double KW_SYS_MAX = 0.236;
	private double KW_SYS_MIN = 0;
	private double KW_SYS_DELTA_SCALE=0.6;
	private double KW_SYS_DELTA = KW_SYS_MAX * KW_SYS_DELTA_SCALE;
	
	private double activeEnergyReading = 0;
	private double activeEnergy = 0;
	
	private String _savefile;
	
	public Pro1250D(int addr, String cat) {
		super(addr, cat);
		_savefile = "Dummy_Data_Pro1250D_" + addr;
		loadData();
	}

	@Override
	public String getDeviceData() {
		calculateDecodedData();
		return createDataSendToServer();
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
		real_time_data.clear();
		if (getStatus() == GKProtocol.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			item.put("activeEnergyReading" , vformat.format(activeEnergyReading));
			real_time_data.add(item);
		}
		return real_time_data;
	}
	
	public void setSimData(Double max, Double min, Double delta, Double scale) {
		if (max != null) {
			KW_SYS_MAX = max;
		}

		if (min != null) {
			KW_SYS_MIN = min;
		}

		if (KW_SYS_MIN > KW_SYS_MAX) {
			double tmp = KW_SYS_MAX;
			KW_SYS_MAX = KW_SYS_MIN;
			KW_SYS_MIN = tmp;
		}

		if (scale != null) {
			KW_SYS_DELTA_SCALE = scale;
		}

		if (delta != null) {
			KW_SYS_DELTA = delta;
		} else {
			KW_SYS_DELTA = (KW_SYS_MAX - KW_SYS_MIN) * KW_SYS_DELTA_SCALE;
		}

		activeEnergy = KW_SYS_MIN;
	}
	
	private void calculateDecodedData() {
		//Calculate System Apparent Power
		double random = FuncUtil.randomWithRange(0,KW_SYS_DELTA)/*(KW_L3_MAX_DELTA + (Math.random() * (KW_L3_MAX_DELTA - KW_L3_MIN_DELTA)))*/;
		double probability = FuncUtil.randomWithRange(0,100);
		if(probability < 35){
			if((activeEnergy - random) <= KW_SYS_MIN)
			{
				activeEnergy = KW_SYS_MIN;
			}else{
				activeEnergy -= random;
			}
		}else if(probability > 65){
			if((activeEnergy + random) >= KW_SYS_MAX)
			{
				activeEnergy = KW_SYS_MAX;
			}else{
				activeEnergy += random;
			}
			
		}
		
		activeEnergyReading += activeEnergy/1000;
		saveData();
	}
	
	private String createDataSendToServer() {
		if (getStatus() != GKProtocol.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		data.append("|DEVICEID=" + getId() + "-0-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Generated Energy Reading="+ vformat.format(activeEnergyReading) + ",kWh");
		data.append(";Generated Energy="+ vformat.format(activeEnergy) + ",Wh");
		data.append(";Active Energy=" + vformat.format(-activeEnergy) + ",Wh");
		return data.toString();
	}
	
	   public boolean saveData() {
	    	mLogger.info("save data");
	    	Properties _props = new Properties();
	    	try {
				_props.setProperty("Active_Energy_Reading",String.valueOf(activeEnergyReading));
			    File file = new File(_savefile);
			    file.createNewFile();
			    file.setReadable(true, false);
			    file.setWritable(true, false);
			    _props.store(new FileOutputStream(file), _savefile);
			    return true;
			} catch (Exception e) {
				mLogger.error("Exception", e);
			}
			return false;
	    }
	    
		public boolean loadData() {
	    	mLogger.info("load data");
	    	Properties _props = new Properties();
			try {
				FileInputStream in = new FileInputStream( _savefile );
				_props.load( in );
				in.close();
				activeEnergyReading = Double.parseDouble(_props.getProperty("Active_Energy_Reading","0"));
				return true;
			} catch( FileNotFoundException e ) {
				mLogger.warn("Can't find save file: " + _savefile );
				
			} catch( IOException e ) {
				mLogger.error("Read failure for file: " + _savefile );
			}
			return false;
	    }

}
