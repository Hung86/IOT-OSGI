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

public class Model212 extends DummyDevice {
	public static final float KW_PER_RT = 3.5168525f;
	
    static private final float MAX_FLOW_RATE = 2;
    static private final float MIN_FLOW_RATE = 0;
    static private final float MAX_SUPPLY_TEMP = 9;
    static private final float MIN_SUPPLY_TEMP = 4;
    static private final float MAX_RETURN_TEMP = 14;
    static private final float MIN_RETURN_TEMP = 10;
    
	private double coolingEnergyConsumption= 0.0;
	private double coolingConsumption = 0.0;
	private double coolingLoadRT = 0.0;
	private double coolingLoadRTh = 0.0;
	private double flowRate = 0.0;
	private double chilledWaterSupplyTemperature = 0;
	private double chilledWaterReturnTemperature = 0;
	private double temperatureDiff = 0;
	private double coolingLoadKWC = 0;
	private int operationTime = 0;
	private String _savefile;

	public Model212(int addr, String cat) {
		super(addr, cat);
		_savefile = "Dummy_Data_MODEL212_" + addr;
		loadData();
	}

	@Override
	public String getDeviceData() {
		calculateDecodedData();
		return createDataSendToServer();
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			calculateDecodedData();
		}

		real_time_data.clear();
		if (getStatus() == GKProtocol.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			item.put("coolingEnergyConsumption", vformat.format(coolingEnergyConsumption));
			item.put("coolingLoadRT", vformat.format(coolingLoadRT));
			item.put("coolingLoadKWC", vformat.format(coolingLoadKWC));
			item.put("flowRate", vformat.format(flowRate));
			item.put("supplyTemperature", vformat.format(chilledWaterSupplyTemperature));
			item.put("returnTemperature", vformat.format(chilledWaterReturnTemperature));
			item.put("temperatureDiff", vformat.format(temperatureDiff));
			real_time_data.add(item);
		}
		return real_time_data;
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		return null;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		return null;
	}

	private void calculateDecodedData() {
	    double randomInt =  FuncUtil.randomWithRange(0,0.5);
	    double resultBet =  FuncUtil.randomWithRange(0,100);
	    
	    double fr;
	    if(resultBet < 34){
	    	fr = (float) (flowRate - randomInt);
	    }else if(resultBet > 67){
	    	fr = (float) (flowRate + randomInt);
	    }else{
	    	fr = flowRate;
	    }
	    if(fr > MAX_FLOW_RATE){
	    	fr = MAX_FLOW_RATE;
	    }else if(fr < MIN_FLOW_RATE){
	    	fr = MIN_FLOW_RATE;
	    }
	    flowRate = fr;
	    if (flowRate <= 0) {
			operationTime = 0;
		} else {
			operationTime = 1;
		}
	    
	    randomInt =  FuncUtil.randomWithRange(0,1);
	    resultBet =  FuncUtil.randomWithRange(0,100);
	    double stemp;
	    if(resultBet < 34){
	    	stemp = (float) (chilledWaterSupplyTemperature - randomInt);
	    }else if(resultBet > 67){
	    	stemp = (float) (chilledWaterSupplyTemperature + randomInt);
	    }else{
	    	stemp = (float) chilledWaterSupplyTemperature;
	    }
	    if(stemp > MAX_SUPPLY_TEMP){
	    	stemp = MAX_SUPPLY_TEMP;
	    }else if(stemp < MIN_SUPPLY_TEMP){
	    	stemp = MIN_SUPPLY_TEMP;
	    }
	    chilledWaterSupplyTemperature = stemp;
	    
	    randomInt =  FuncUtil.randomWithRange(0,1);
	    resultBet =  FuncUtil.randomWithRange(0,100);
	    double rtemp;
	    if(resultBet < 34){
	    	rtemp = (float) (chilledWaterReturnTemperature - randomInt);
	    }else if(resultBet > 67){
	    	rtemp = (float) (chilledWaterReturnTemperature + randomInt);
	    }else{
	    	rtemp = chilledWaterReturnTemperature;
	    }
	    if(rtemp > MAX_RETURN_TEMP){
	    	rtemp = MAX_RETURN_TEMP;
	    }else if(rtemp < MIN_RETURN_TEMP){
	    	rtemp = MIN_RETURN_TEMP;
	    }
	    chilledWaterReturnTemperature = rtemp;
	    
	    double Q = flowRate*(chilledWaterReturnTemperature-chilledWaterSupplyTemperature)*4.19;
	    coolingConsumption = Q*60;
	    coolingEnergyConsumption += (coolingConsumption/1000);
	    coolingLoadRT = Q;
	    coolingLoadRTh =  Q/60;
	    coolingLoadKWC = Q*KW_PER_RT;
	    temperatureDiff = Math.abs(chilledWaterReturnTemperature - chilledWaterSupplyTemperature);
	    saveData();
	}

	private String createDataSendToServer() {
		if (getStatus() != GKProtocol.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		sb.append("|DEVICEID=" + getId() + "-0-0");
		sb.append(";TIMESTAMP=" + timestamp);
		sb.append(";Operation Time=" + operationTime + ",minute");
		sb.append(";BTU Meter Reading" + "=" + vformat.format(coolingEnergyConsumption) + ",kWh");

		//sb.append("|DEVICEID=" + getId() + "-1-0");
		//sb.append(";TIMESTAMP=" + timestamp);
		//sb.append(";Operation Time=" + operationTime + ",minute");
		sb.append(";CH Water Consumption=" + vformat.format(coolingConsumption) + ",Wh");

		//sb.append("|DEVICEID=" + getId() + "-2-0");
		//sb.append(";TIMESTAMP=" + timestamp);
		//sb.append(";Operation Time=" + operationTime + ",minute");
		sb.append(";Cooling Load" + "=" + vformat.format(coolingLoadRT) + ",RT");

		//sb.append("|DEVICEID=" + getId() + "-3-0");
		//sb.append(";TIMESTAMP=" + timestamp);
		//sb.append(";Operation Time=" + operationTime + ",minute");
		sb.append(";Flow Rate" + "=" + vformat.format(flowRate) + ",l/s");

		//sb.append("|DEVICEID=" + getId() + "-4-0");
		//sb.append(";TIMESTAMP=" + timestamp);
		//sb.append(";Operation Time=" + operationTime + ",minute");
		sb.append(";CH Supply Temp" + "=" + vformat.format(chilledWaterSupplyTemperature) + ",C");

		//sb.append("|DEVICEID=" + getId() + "-5-0");
		//sb.append(";TIMESTAMP=" + timestamp);
		//sb.append(";Operation Time=" + operationTime + ",minute");
		sb.append(";CH Return Temp" + "=" + vformat.format(chilledWaterReturnTemperature) + ",C");

		//sb.append("|DEVICEID=" + getId() + "-6-0");
		//sb.append(";TIMESTAMP=" + timestamp);
		//sb.append(";Operation Time=" + operationTime + ",minute");
		sb.append(";CH Temp Diff" + "=" + vformat.format(Math.abs(temperatureDiff)) + ",C");

		//sb.append("|DEVICEID=" + getId() + "-7-0");
		//sb.append(";TIMESTAMP=" + timestamp);
		//sb.append(";Operation Time=" + operationTime + ",minute");
		sb.append(";CH Water Consumption RTh=" + vformat.format(coolingLoadRTh) + ",RTh");

		//sb.append("|DEVICEID=" + getId() + "-8-0");
		//sb.append(";TIMESTAMP=" + timestamp);
		//sb.append(";Operation Time=" + operationTime + ",minute");
		sb.append(";Cooling Load kWc" + "=" + vformat.format(coolingLoadKWC) + ",kWc");
		return sb.toString();
	}

	
    public boolean saveData() {
    	mLogger.info("save data");
    	Properties _props = new Properties();
    	try {
    		_props.setProperty("cooling_energy",String.valueOf(coolingEnergyConsumption));
			_props.setProperty("flow_rate",String.valueOf(flowRate));
			_props.setProperty("supply_temp",String.valueOf(this.chilledWaterSupplyTemperature));
			_props.setProperty("return_temp",String.valueOf(this.chilledWaterReturnTemperature));
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
			try {
				coolingEnergyConsumption = Double.parseDouble(_props.getProperty("cooling_energy"));
			} catch (NumberFormatException ex) {
			}
			try {
				flowRate = Float.parseFloat(_props.getProperty("flow_rate"));
			} catch (NumberFormatException ex) {
			}
			try {
				chilledWaterSupplyTemperature = Float.parseFloat(_props.getProperty("supply_temp"));
			} catch (NumberFormatException ex) {
			}
			try {
				chilledWaterReturnTemperature = Float.parseFloat(_props.getProperty("return_temp"));
			} catch (NumberFormatException ex) {
			}
			return true;
		} catch( FileNotFoundException e ) {
			mLogger.warn("Can't find save file: " + _savefile );
			
		} catch( IOException e ) {
			mLogger.error("Read failure for file: " + _savefile );
		}
		return false;
    }

}
