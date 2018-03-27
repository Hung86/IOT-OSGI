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

public class Multicom30X extends DummyDevice {

	private double inputVoltageL1N = 237;
	private double inputVoltageL2N = 237;
	private double inputVoltageL3N = 238;
	private double inputCurrentL1 = 1;
	private double inputCurrentL2 = 1;
	private double inputCurrentL3 = 1;
	private double inputFrequency = 50;
	private double bypassMainsVoltageL1N = 237;
	private double bypassMainsVoltageL2N = 237;
	private double bypassMainsVoltageL3N = 238;
	private double bypassFrequency = 50
			;
	private double outputStarVoltageL1N = 235;
	private double outputStarVoltageL2N = 236;
	private double outputStarVoltageL3N = 237;
	private double outputCurrentL1 = 1;
	private double outputCurrentL2 = 1;
	private double outputCurrentL3 = 1;
	private double outputPeakCurrentL1 = 1;
	private double outputPeakCurrentL2 = 1;
	private double outputPeakCurrentL3 = 1;
	private int loadPhaseL1 = 6;
	private int loadPhaseL2 = 7;
	private int loadPhaseL3 = 8;
	private double outputActivePowerL1 = 1;
	private double outputActivePowerL2 = 1;
	private double outputActivePowerL3 = 1;
	private double outputFrequency = 50;
	
	private double batteryVoltage = 240;
	private double positiveBatteryVoltage = 0;
	private double negativeBatteryVoltage = 0;
	private double batteryCurrent = 1;
	private int remainingBatteryCapacity = 60;
	private int remainingBackupTimeMins = 240;
	
	//private double activeEnergy = 0.0f;
	private double totalOutputEnergy = 0.0f;
	private double internalUpsTemperature = 0.0f;
	private double sensor1Temperature = 0.0f;
	private double sensor2Temperature = 0.0f;
	
    double OUTPUT_KW_L1_MAX = 2;
	double OUTPUT_KW_L1_MIN = 1;
	
    double OUTPUT_KW_L2_MAX = 2;
	double OUTPUT_KW_L2_MIN = 1;
	
    double OUTPUT_KW_L3_MAX = 2;
	double OUTPUT_KW_L3_MIN = 1;
    
    final int MIN_ENERGY = 0;
    final int MAX_ENERGY = 3;
    
    final int MIN_INTERNAL_TEMP = 22;
    final int MAX_INTERNAL_TEMP = 50;
    
    final int MIN_SENSOR1_TEMP = 22;
    final int MAX_SENSOR1_TEMP = 35;
    
    final int MIN_SENSOR2_TEMP = 22;
    final int MAX_SENSOR2_TEMP = 37;
			
    private String _savefile;
    
	public Multicom30X(int addr, String cat) {
		super(addr, cat);
		_savefile = "Dummy_Data_Multicom30X_" + addr;
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
			item.put("inputVoltageL1N", vformat.format(inputVoltageL1N));
			item.put("inputVoltageL2N", vformat.format(inputVoltageL2N));
			item.put("inputVoltageL3N", vformat.format(inputVoltageL3N));
			item.put("inputCurrentL1", vformat.format(inputCurrentL1));
			item.put("inputCurrentL2", vformat.format(inputCurrentL2));
			item.put("inputCurrentL3", vformat.format(inputCurrentL3));
			item.put("inputFrequency", vformat.format(inputFrequency));

			item.put("bypassMainsVoltageL1N", vformat.format(bypassMainsVoltageL1N));
			item.put("bypassMainsVoltageL2N", vformat.format(bypassMainsVoltageL2N));
			item.put("bypassMainsVoltageL3N", vformat.format(bypassMainsVoltageL3N));
			item.put("bypassFrequency", vformat.format(bypassFrequency));
			item.put("outputStarVoltageL1N", vformat.format(outputStarVoltageL1N));
			item.put("outputStarVoltageL2N", vformat.format(outputStarVoltageL2N));
			item.put("outputStarVoltageL3N", vformat.format(outputStarVoltageL3N));

			item.put("outputCurrentL1", vformat.format(outputCurrentL1));
			item.put("outputCurrentL2", vformat.format(outputCurrentL2));
			item.put("outputCurrentL3", vformat.format(outputCurrentL3));
			item.put("outputPeakCurrentL1", vformat.format(outputPeakCurrentL1));
			item.put("outputPeakCurrentL2", vformat.format(outputPeakCurrentL2));
			item.put("outputPeakCurrentL3", vformat.format(outputPeakCurrentL3));
			item.put("loadPhaseL1", String.valueOf(loadPhaseL1));
			item.put("loadPhaseL2", String.valueOf(loadPhaseL2));
			item.put("loadPhaseL3", String.valueOf(loadPhaseL3));
			item.put("outputActivePowerL1", vformat.format(outputActivePowerL1));
			item.put("outputActivePowerL2", vformat.format(outputActivePowerL2));
			item.put("outputActivePowerL3", vformat.format(outputActivePowerL3));
			item.put("outputFrequency", vformat.format(outputFrequency));

			item.put("batteryVoltage", vformat.format(batteryVoltage));
			item.put("positiveBatteryVoltage", vformat.format(positiveBatteryVoltage));
			item.put("negativeBatteryVoltage", vformat.format(negativeBatteryVoltage));
			item.put("batteryCurrent", vformat.format(batteryCurrent));
			item.put("remainingBatteryCapacity", String.valueOf(remainingBatteryCapacity));
			item.put("remainingBackupTimeMins", String.valueOf(remainingBackupTimeMins));

			item.put("totalOutputEnergy", vformat.format(totalOutputEnergy));

			item.put("internalUpsTemperature", vformat.format(internalUpsTemperature));
			item.put("sensor1Temperature", vformat.format(sensor1Temperature));
			item.put("sensor2Temperature", vformat.format(sensor2Temperature));
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
		double minimum = 0;
		double maximum = 1;
		double randomInt;
		double minBet = 0;
		double maxBet = 100;
		double resultBet;
		//float energy;
	    float power = (float) FuncUtil.randomWithRange(OUTPUT_KW_L1_MIN,OUTPUT_KW_L1_MAX)/*(OUTPUT_KW_L1_MAX_DELTA + (Math.random() * (OUTPUT_KW_L1_MAX_DELTA - OUTPUT_KW_L1_MIN_DELTA)))*/;
	    resultBet = FuncUtil.randomWithRange(0,100)/*(100 + (Math.random() * 100))*/;
		if(resultBet < 35){
			if((outputActivePowerL1 - power) <= OUTPUT_KW_L1_MIN)
			{
				outputActivePowerL1 = OUTPUT_KW_L1_MIN;
			}else{
				outputActivePowerL1 -= power;
			}
		}else if(resultBet > 65){
			if((outputActivePowerL1 + power) >= OUTPUT_KW_L1_MAX)
			{
				outputActivePowerL1 = OUTPUT_KW_L1_MAX;
			}else{
				outputActivePowerL1 += power;
			}
			
		}
		
		outputCurrentL1 = (float) (outputActivePowerL1*1000/(outputStarVoltageL1N));
		if(outputPeakCurrentL1 < outputCurrentL1){
			outputPeakCurrentL1 = outputCurrentL1;
		}
		float currnetleakpercentage = (float) FuncUtil.randomWithRange(0.9,1);
		inputCurrentL1 = (float) (outputCurrentL1*(1+(1-currnetleakpercentage)));
		power = (float) FuncUtil.randomWithRange(OUTPUT_KW_L2_MIN,OUTPUT_KW_L2_MAX)/*(KW_L1_MAX_DELTA + (Math.random() * (KW_L1_MAX_DELTA - KW_L1_MIN_DELTA)))*/;
	    resultBet = FuncUtil.randomWithRange(0,100)/*(100 + (Math.random() * 100))*/;
		if(resultBet < 35){
			if((outputActivePowerL2 - power) <= OUTPUT_KW_L2_MIN)
			{
				outputActivePowerL2 = OUTPUT_KW_L2_MIN;
			}else{
				outputActivePowerL2 -= power;
			}
		}else if(resultBet > 65){
			if((outputActivePowerL2 + power) >= OUTPUT_KW_L2_MAX)
			{
				outputActivePowerL2 = OUTPUT_KW_L2_MAX;
			}else{
				outputActivePowerL2 += power;
			}
			
		}
		
		outputCurrentL2 = (float) (outputActivePowerL2*1000/(outputStarVoltageL2N));
		if(outputPeakCurrentL2 < outputCurrentL2){
			outputPeakCurrentL2 = outputCurrentL2;
		}
		currnetleakpercentage = (float) FuncUtil.randomWithRange(0.9,1);
		inputCurrentL2= (float) (outputCurrentL2*(1+(1-currnetleakpercentage)));
		power = (float) FuncUtil.randomWithRange(OUTPUT_KW_L3_MIN,OUTPUT_KW_L3_MAX)/*(KW_L1_MAX_DELTA + (Math.random() * (KW_L1_MAX_DELTA - KW_L1_MIN_DELTA)))*/;
	    resultBet = FuncUtil.randomWithRange(0,100)/*(100 + (Math.random() * 100))*/;
		if(resultBet < 35){
			if((outputActivePowerL3 - power) <= OUTPUT_KW_L3_MIN)
			{
				outputActivePowerL3 = OUTPUT_KW_L3_MIN;
			}else{
				outputActivePowerL3 -= power;
			}
		}else if(resultBet > 65){
			if((outputActivePowerL3 + power) >= OUTPUT_KW_L3_MAX)
			{
				outputActivePowerL3 = OUTPUT_KW_L3_MAX;
			}else{
				outputActivePowerL3 += power;
			}
			
		}
		
		outputCurrentL3 = (float) (outputActivePowerL3*1000/(outputStarVoltageL3N));
		if(outputPeakCurrentL3 < outputCurrentL3){
			outputPeakCurrentL3 = outputCurrentL3;
		}
		currnetleakpercentage = (float) FuncUtil.randomWithRange(0.9,1);
		inputCurrentL3 = (float) (outputCurrentL3*(1+(1-currnetleakpercentage)));
				
	    totalOutputEnergy = (outputActivePowerL1+outputActivePowerL2+outputActivePowerL3)/60;

//        data.append(";Total Output Energy="+vformat.format(totalOutputEnergy)+",kWh"); 
        
	    
	    minimum = 0;
	    maximum = 1;
	    randomInt =  FuncUtil.randomWithRange(minimum,maximum);
	    resultBet =  FuncUtil.randomWithRange(minBet,maxBet);
	    double temp;
	    if(resultBet < 34){
	    	temp = (float) (internalUpsTemperature - randomInt);
	    }else if(resultBet > 67){
	    	temp = (float) (internalUpsTemperature + randomInt);
	    }else{
	    	temp = internalUpsTemperature;
	    }
	    if(temp > MAX_INTERNAL_TEMP){
	    	temp = MAX_INTERNAL_TEMP;
	    }else if(temp < MIN_INTERNAL_TEMP){
	    	temp = MIN_INTERNAL_TEMP;
	    }
	    internalUpsTemperature = temp;
	    
		minimum = 0;
	    maximum = 1;
	    randomInt =  FuncUtil.randomWithRange(minimum,maximum);
	    resultBet =  FuncUtil.randomWithRange(minBet,maxBet);
	    if(resultBet < 34){
	    	temp = (float) (sensor1Temperature - randomInt);
	    }else if(resultBet > 67){
	    	temp = (float) (sensor1Temperature + randomInt);
	    }else{
	    	temp = sensor1Temperature;
	    }
	    if(temp > MAX_SENSOR1_TEMP){
	    	temp = MAX_SENSOR1_TEMP;
	    }else if(temp < MIN_SENSOR1_TEMP){
	    	temp = MIN_SENSOR1_TEMP;
	    }
	    sensor1Temperature = temp;
	    
	    minimum = 0;
	    maximum = 0.5;
	    randomInt =  FuncUtil.randomWithRange(minimum,maximum);
	    resultBet =  FuncUtil.randomWithRange(minBet,maxBet);
	    if(resultBet < 34){
	    	temp = (float) (sensor2Temperature - randomInt);
	    }else if(resultBet > 67){
	    	temp = (float) (sensor2Temperature + randomInt);
	    }else{
	    	temp = sensor2Temperature;
	    }
	    if(temp > MAX_SENSOR2_TEMP){
	    	temp = MAX_SENSOR2_TEMP;
	    }else if(temp < MIN_SENSOR2_TEMP){
	    	temp = MIN_SENSOR2_TEMP;
	    }
	    sensor2Temperature = temp;
	    
	    saveData();
	}

	private String createDataSendToServer() {
		if (getStatus() != GKProtocol.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		data.append("|DEVICEID=" + getId() + "-0" + "-0");
		data.append(";TIMESTAMP=" + timestamp);

		data.append(";Input Voltage L1-N=" + vformat.format(inputVoltageL1N) + ",V");
		data.append(";Input Voltage L2-N=" + vformat.format(inputVoltageL2N) + ",V");
		data.append(";Input Voltage L3-N=" + vformat.format(inputVoltageL3N) + ",V");
		data.append(";Input Current L1=" + vformat.format(inputCurrentL1) + ",A");
		data.append(";Input Current L2=" + vformat.format(inputCurrentL2) + ",A");
		data.append(";Input Current L3=" + vformat.format(inputCurrentL3) + ",A");
		data.append(";Input Frequency=" + vformat.format(inputFrequency) + ",Hz");
		// Bypass Measures
		data.append(";Bypass Voltage L1-N=" + vformat.format(bypassMainsVoltageL1N) + ",V");
		data.append(";Bypass Voltage L2-N=" + vformat.format(bypassMainsVoltageL2N) + ",V");
		data.append(";Bypass Voltage L3-N=" + vformat.format(bypassMainsVoltageL3N) + ",V");
		data.append(";Bypass Frequency=" + vformat.format(bypassFrequency) + ",Hz");
		// Output Measures
		data.append(";Output Voltage L1-N=" + vformat.format(outputStarVoltageL1N) + ",V");
		data.append(";Output Voltage L2-N=" + vformat.format(outputStarVoltageL2N) + ",V");
		data.append(";Output Voltage L3-N=" + vformat.format(outputStarVoltageL3N) + ",V");
		data.append(";Output Current L1=" + vformat.format(outputCurrentL1) + ",A");
		data.append(";Output Current L2=" + vformat.format(outputCurrentL2) + ",A");
		data.append(";Output Current L3=" + vformat.format(outputCurrentL3) + ",A");
		data.append(";Output Peak Current L1=" + vformat.format(outputPeakCurrentL1) + ",A");
		data.append(";Output Peak Current L2=" + vformat.format(outputPeakCurrentL2) + ",A");
		data.append(";Output Peak Current L3=" + vformat.format(outputPeakCurrentL3) + ",A");
		data.append(";Load Phase L1=" + loadPhaseL1 + ",%");
		data.append(";Load Phase L2=" + loadPhaseL2 + ",%");
		data.append(";Load Phase L3=" + loadPhaseL3 + ",%");
		data.append(";Output Active Power L1=" + vformat.format(outputActivePowerL1) + ",kW");
		data.append(";Output Active Power L2=" + vformat.format(outputActivePowerL2) + ",kW");
		data.append(";Output Active Power L3=" + vformat.format(outputActivePowerL3) + ",kW");
		data.append(";Output Frequency=" + vformat.format(outputFrequency) + ",Hz");
		// Battery Measures
		data.append(";Battery Voltage=" + vformat.format(batteryVoltage) + ",V");
		data.append(";Positive Battery Voltage=" + vformat.format(positiveBatteryVoltage) + ",V");
		data.append(";Negative Battery Voltage=" + vformat.format(negativeBatteryVoltage) + ",V");
		data.append(";Battery Current=" + vformat.format(batteryCurrent) + ",A");
		data.append(";Remain Battery Capacity=" + remainingBatteryCapacity + ",%");
		data.append(";Remain Battery Time=" + remainingBackupTimeMins + ",min");
		data.append(";Total Output Energy=" + vformat.format(totalOutputEnergy) + ",kWh");
		data.append(";Internal UPS Temperature=" + vformat.format(internalUpsTemperature) + ",C");
		data.append(";Sensor 1 Temperature=" + vformat.format(sensor1Temperature) + ",C");
		data.append(";Sensor 2 Temperature=" + vformat.format(sensor2Temperature) + ",C");
		data.append(";Total Output Active Power=" + vformat.format(outputActivePowerL1 + outputActivePowerL2 + outputActivePowerL3) + ",kW");
		data.append(";Average Load Phase=" + vformat.format((loadPhaseL1 + loadPhaseL2 + loadPhaseL3) / 3.0f) + ",%");

		return data.toString();

	}
	
	
	public void setSimData(Double max_l1,Double min_l1,Double max_l2,Double min_l2,Double max_l3,Double min_l3){
	    if(max_l1 != null){
	    	OUTPUT_KW_L1_MAX = max_l1;
	    }if(min_l1 != null){
	    	OUTPUT_KW_L1_MIN = min_l1;
	    }
	    if(OUTPUT_KW_L1_MIN > OUTPUT_KW_L1_MAX){
	    	double tmp = OUTPUT_KW_L1_MAX;
	    	OUTPUT_KW_L1_MAX = OUTPUT_KW_L1_MIN;
	    	OUTPUT_KW_L1_MIN = tmp;
	    }
	    if(max_l2 != null){
	    	OUTPUT_KW_L2_MAX = max_l2;
	    }if(min_l1 != null){
	    	OUTPUT_KW_L2_MIN = min_l2;
	    }
	    if(OUTPUT_KW_L2_MIN > OUTPUT_KW_L2_MAX){
	    	double tmp = OUTPUT_KW_L2_MAX;
	    	OUTPUT_KW_L2_MAX = OUTPUT_KW_L2_MIN;
	    	OUTPUT_KW_L2_MIN = tmp;
	    }
	    if(max_l3 != null){
	    	OUTPUT_KW_L3_MAX = max_l3;
	    }if(min_l3 != null){
	    	OUTPUT_KW_L3_MIN = min_l3;
	    }
	    if(OUTPUT_KW_L3_MIN > OUTPUT_KW_L3_MAX){
	    	double tmp = OUTPUT_KW_L1_MAX;
	    	OUTPUT_KW_L3_MAX = OUTPUT_KW_L1_MIN;
	    	OUTPUT_KW_L3_MIN = tmp;
	    }
	}
	
    public boolean saveData() {
    	mLogger.info("save data");
    	Properties _props = new Properties();
    	try {
    		_props.setProperty("internalUpsTemperature",String.valueOf(internalUpsTemperature));
			_props.setProperty("sensor1Temperature",String.valueOf(sensor1Temperature));
			_props.setProperty("sensor2Temperature",String.valueOf(sensor2Temperature));
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
				internalUpsTemperature = Long.parseLong(_props.getProperty("internalUpsTemperature"));
			} catch (NumberFormatException ex) {
				//mLogger.error("Wrong volume data,use default " );
			}
			try {
				sensor1Temperature = Long.parseLong(_props.getProperty("sensor1Temperature"));
			} catch (NumberFormatException ex) {
				//mLogger.error("Wrong volume data,use default " );
			}
			try {
				sensor2Temperature = Long.parseLong(_props.getProperty("sensor2Temperature"));
			} catch (NumberFormatException ex) {
				//mLogger.error("Wrong volume data,use default " );
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
