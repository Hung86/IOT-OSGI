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

public class MPR46S extends DummyDevice {

	public static double Scalar_Energy = 1;
	public static double Scalar_Power = 1;
	public static double Scalar_A = 1;
	public static double Scalar_V = 1;
	public static double Scalar_PF = 1;
	public static double Scalar_Hz = 1;


	private double kWh_System = 0;
	private double kWh_L1 = 0;
	private double kWh_L2 = 0;
	private double kWh_L3 = 0;
	private double kVARh_System = 0;
	private double kVARh_L1 = 0;
	private double kVARh_L2 = 0;
	private double kVARh_L3 = 0;
	private double kVAh_System = 0;
	private double kVAh_L1 = 0;
	private double kVAh_L2 = 0;
	private double kVAh_L3 = 0;
	private double kWh_System_diff = 0;
	private double kWh_L1_diff = 0;
	private double kWh_L2_diff = 0;
	private double kWh_L3_diff = 0;
	private double kVARh_System_diff = 0;
	private double kVARh_L1_diff = 0;
	private double kVARh_L2_diff = 0;
	private double kVARh_L3_diff = 0;
	private double kVAh_System_diff = 0;
	private double kVAh_L1_diff = 0;
	private double kVAh_L2_diff = 0;
	private double kVAh_L3_diff = 0;
	private double kW_System = 0;
	private double kW_L1 = 0;
	private double kW_L2 = 0;
	private double kW_L3 = 0;
	private double kVAR_System = 0;
	private double kVAR_L1 = 0;
	private double kVAR_L2 = 0;
	private double kVAR_L3 = 0;
	private double kVA_System = 0;
	private double kVA_L1 = 0;
	private double kVA_L2 = 0;
	private double kVA_L3 = 0;
	private double PF_L1 = 0.8;
	private double PF_L2 = 0.8;
	private double PF_L3 = 0.8;
	private double V_L1 = 230;
	private double V_L2 = 230;
	private double V_L3 = 230;
	private double V_System;
	private double A_L1 = 0;
	private double A_L2 = 0;
	private double A_L3 = 0;
	private double A_System = 0;
	private double A_Neutral = 0;
	private int Hz_System = 50;
	private double kW_Demand_L1 = 0.05;
	private double kW_Demand_L2 = 0.06;
	private double kW_Demand_L3 = 0.07;
	public int V_L1_L2 = 400;
    public int V_L2_L3 = 400;
    public int V_L1_L3 = 400;//
    public double PF_System = 0.8;


    double KW_L1_MAX = 0.236;
   	double KW_L1_MIN = 0;
   	double KW_L1_DELTA_SCALE=0.6;
   	double KW_L1_DELTA = KW_L1_MAX*KW_L1_DELTA_SCALE;
   	
    double PF_L1_MAX = 1;
    double PF_L1_MIN = 0.8;
    double PF_L1_DELTA = 0.03;
    
    double KW_L2_MAX = 0.236;
	double KW_L2_MIN = 0;
	double KW_L2_DELTA_SCALE=0.6;
	double KW_L2_DELTA = KW_L2_MAX*KW_L2_DELTA_SCALE;
	
    double PF_L2_MAX = 1;
    double PF_L2_MIN = 0.8;
    double PF_L2_DELTA = 0.03;
    
    double KW_L3_MAX = 0.236;
	double KW_L3_MIN = 0;
	double KW_L3_DELTA_SCALE = 0.6;
	double KW_L3_DELTA = KW_L3_MAX*KW_L3_DELTA_SCALE;
	
    double PF_L3_MAX = 1;
    double PF_L3_MIN = 0.8;
    double PF_L3_DELTA = 0.03;

    private String _savefile;
    
	public MPR46S(int addr, String cat) {
		super(addr, cat);
		_savefile = "Dummy_Data_MPR46S_" + addr;
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
			// System
			item.put("kWh_System", vformat.format(kWh_System * Scalar_Energy));
			item.put("kVARh_System", vformat.format(kVARh_System * Scalar_Energy));
			item.put("kVAh_System", vformat.format(kVAh_System * Scalar_Energy));
			item.put("A_Neutral", vformat.format(A_Neutral * Scalar_A));
			item.put("Hz_System", vformat.format(Hz_System * Scalar_Hz));
			item.put("kW_System", vformat.format(kW_System * Scalar_Power));
			item.put("kVAR_System", vformat.format(kVAR_System * Scalar_Power));
			item.put("kVA_System", vformat.format(kVA_System * Scalar_Power));
			item.put("PF_System", vformat.format(PF_System * Scalar_PF));
			item.put("V_L1_L2", vformat.format(V_L1_L2 * Scalar_V));
			item.put("V_L2_L3", vformat.format(V_L2_L3 * Scalar_V));
			item.put("V_L1_L3", vformat.format(V_L1_L3 * Scalar_V));
			item.put("kW_Demand_System", vformat.format(((kW_Demand_L1)+(kW_Demand_L2)+(kW_Demand_L3))));
			item.put("V_System", vformat.format(V_L1 * Scalar_V));

			// Phase L1
			item.put("kWh_L1", vformat.format((kWh_L1) * Scalar_Energy));
			item.put("kVARh_L1", vformat.format((kVARh_L1) * Scalar_Energy));
			item.put("kVAh_L1", vformat.format(kVAh_L1 * Scalar_Energy));
			item.put("kW_L1", vformat.format(kW_L1 * Scalar_Power));
			item.put("kVAR_L1", vformat.format(kVAR_L1 * Scalar_Power));
			item.put("kVA_L1", vformat.format(kVA_L1 * Scalar_Power));
			item.put("PF_L1", vformat.format(PF_L1 * Scalar_PF));
			item.put("V_L1", vformat.format(V_L1 * Scalar_V));
			item.put("A_L1", vformat.format(A_L1 * Scalar_A));
			item.put("kW_Demand_L1", vformat.format(kW_Demand_L1 * Scalar_Power));

			// Phase L2
			item.put("kWh_L2", vformat.format(kWh_L2 * Scalar_Energy));
			item.put("kVARh_L2", vformat.format(kVARh_L2 * Scalar_Energy));
			item.put("kVAh_L2", vformat.format(kVAh_L2 * Scalar_Energy));
			item.put("kW_L2", vformat.format(kW_L2 * Scalar_Power));
			item.put("kVAR_L2", vformat.format(kVAR_L2 * Scalar_Power));
			item.put("kVA_L2", vformat.format(kVA_L2 * Scalar_Power));
			item.put("PF_L2", vformat.format(PF_L2 * Scalar_PF));
			item.put("V_L2", vformat.format(V_L2 * Scalar_V));
			item.put("A_L2", vformat.format(A_L2 * Scalar_A));
			item.put("kW_Demand_L2", vformat.format(kW_Demand_L2 * Scalar_Power));

			// Phase L3
			item.put("kWh_L3", "" + vformat.format((kWh_L3) * Scalar_Energy));
			item.put("kVARh_L3", "" + vformat.format(kVARh_L3 * Scalar_Energy));
			item.put("kVAh_L3", "" + vformat.format(kVAh_L3 * Scalar_Energy));
			item.put("kW_L3", "" + vformat.format(kW_L3 * Scalar_Power));
			item.put("kVAR_L3", "" + vformat.format(kVAR_L3 * Scalar_Power));
			item.put("kVA_L3", "" + vformat.format(kVA_L3 * Scalar_Power));
			item.put("PF_L3", "" + vformat.format(PF_L3 * Scalar_PF));
			item.put("V_L3", "" + vformat.format(V_L3 * Scalar_V));
			item.put("A_L3", "" + vformat.format(A_L3 * Scalar_A));
			item.put("kW_Demand_L3", vformat.format(kW_Demand_L3 * Scalar_Power));
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
		double random;
		double probability;
		////L1
		//Calculate System Apparent Power
		random = FuncUtil.randomWithRange(0,KW_L1_DELTA)/*(KW_L1_MAX_DELTA + (Math.random() * (KW_L1_MAX_DELTA - KW_L1_MIN_DELTA)))*/;
		probability = FuncUtil.randomWithRange(0,100)/*(100 + (Math.random() * 100))*/;
		if(probability < 35){
			if((kW_L1 - random) <= KW_L1_MIN)
			{
				kW_L1 = KW_L1_MIN;
			}else{
				kW_L1 -= random;
			}
		}else if(probability > 65){
			if((kW_L1 + random) >= KW_L1_MAX)
			{
				kW_L1 = KW_L1_MAX;
			}else{
				kW_L1 += random;
			}
			
		}
		
		//Calculate System Power Factor
		random = FuncUtil.randomWithRange(0,PF_L1_DELTA)/*(PF_L1_MAX_DELTA + (Math.random() * (PF_L1_MAX_DELTA - PF_L1_MIN_DELTA)))*/;
		probability = FuncUtil.randomWithRange(0,100)/*(100 + (Math.random() * 100))*/;
		if(probability < 35){
			if((PF_L1 - random) <= PF_L1_MIN)
			{
				PF_L1 = PF_L1_MIN;
			}else{
				PF_L1 -= random;
			}
		}else if(probability > 65){
			if((PF_L1 + random) >= PF_L1_MAX)
			{
				PF_L1 = PF_L1_MAX;
			}else{
				PF_L1 += random;
			}
			
		}
		//Calculate Active Power
		kVA_L1= kW_L1/(PF_L1);
		//Calculate Reactive Power
		kVAR_L1 = kVA_L1 - kW_L1;
		//Calculate System Current
		A_L1 = 1000*kVA_L1/V_L1;
		//Calculate Active Energy
		kWh_L1_diff = kW_L1/60;
		kWh_L1 += kWh_L1_diff;
		if(kWh_L1 == Double.MAX_VALUE || kWh_L1 < 0){
			kWh_L1 = 0;
		}
		//Calculate Reactive Energy
		kVARh_L1_diff = kVAR_L1/60;
		kVARh_L1 += kVARh_L1_diff;
		if(kVARh_L1 == Double.MAX_VALUE || kVARh_L1 < 0){
			kVARh_L1 = 0;
		}
		//Calculate Apparent Energy
		kVAh_L1_diff = kVA_L1/60;
		kVAh_L1 += kVAh_L1_diff;
		if(kVAh_L1 == Double.MAX_VALUE || kVAh_L1 < 0){
			kVAh_L1 = 0;
		}
		
		////L2
		//Calculate System Apparent Power
		random = FuncUtil.randomWithRange(0,KW_L2_DELTA)/*(KVA_L2_MAX_DELTA + (Math.random() * (KVA_L2_MAX_DELTA - KVA_L2_MIN_DELTA)))*/;
		probability = FuncUtil.randomWithRange(0,100)/*(100 + (Math.random() * 100))*/;
		if(probability < 35){
			if((kW_L2 - random) <= KW_L2_MIN)
			{
				kW_L2 = KW_L2_MIN;
			}else{
				kW_L2 -= random;
			}
		}else if(probability > 65){
			if((kW_L2 + random) >= KW_L2_MAX)
			{
				kW_L2 = KW_L2_MAX;
			}else{
				kW_L2 += random;
			}
			
		}
		//Calculate System Power Factor
		random = FuncUtil.randomWithRange(0,PF_L2_DELTA)/*(PF_L2_MAX_DELTA + (Math.random() * (PF_L2_MAX_DELTA - PF_L2_MIN_DELTA)))*/;
		probability = FuncUtil.randomWithRange(0,100)/*(100 + (Math.random() * 100))*/;
		if(probability < 35){
			if((PF_L2 - random) <= PF_L2_MIN)
			{
				PF_L2 = PF_L2_MIN;
			}else{
				PF_L2 -= random;
			}
		}else if(probability > 65){
			if((PF_L2 + random) >= PF_L2_MAX)
			{
				PF_L2 = PF_L2_MAX;
			}else{
				PF_L2 += random;
			}
			
		}
		//Calculate Active Power
		kVA_L2 = kW_L2/(PF_L2);
		//Calculate Reactive Power
		kVAR_L2 = kVA_L2 - kW_L2;
		//Calculate System Current
		A_L2 = 1000*kVA_L2/V_L2;
		//Calculate Active Energy
		kWh_L2_diff = kW_L2/60;
		kWh_L2 += kWh_L2_diff;
		if(kWh_L2 == Double.MAX_VALUE || kWh_L2 < 0){
			kWh_L2 = 0;
		}
		//Calculate Reactive Energy
		kVARh_L2_diff = kVAR_L2/60;
		kVARh_L2 += kVARh_L2_diff;
		if(kVARh_L2 == Double.MAX_VALUE || kVARh_L2 < 0){
			kVARh_L2 = 0;
		}
		//Calculate Apparent Energy
		kVAh_L2_diff = kVA_L2/60;
		kVAh_L2 += kVAh_L2_diff;
		if(kVAh_L2 == Double.MAX_VALUE || kVAh_L2 < 0){
			kVAh_L2 = 0;
		}

		////L3
		//Calculate System Apparent Power
		random = FuncUtil.randomWithRange(0,KW_L3_DELTA)/*(KW_L3_MAX_DELTA + (Math.random() * (KW_L3_MAX_DELTA - KW_L3_MIN_DELTA)))*/;
		probability = FuncUtil.randomWithRange(0,100)/*(100 + (Math.random() * 100))*/;
		if(probability < 35){
			if((kW_L3 - random) <= KW_L3_MIN)
			{
				kW_L3 = KW_L3_MIN;
			}else{
				kW_L3 -= random;
			}
		}else if(probability > 65){
			if((kW_L3 + random) >= KW_L3_MAX)
			{
				kW_L3 = KW_L3_MAX;
			}else{
				kW_L3 += random;
			}
			
		}
		
		//Calculate System Power Factor
		random = FuncUtil.randomWithRange(0,PF_L3_DELTA)/*(PF_L3_MAX_DELTA + (Math.random() * (PF_L3_MAX_DELTA - PF_L3_MIN_DELTA)))*/;
		probability = FuncUtil.randomWithRange(0,100)/*(100 + (Math.random() * 100))*/;
		if(probability < 35){
			if((PF_L3 - random) <= PF_L3_MIN)
			{
				PF_L3 = PF_L3_MIN;
			}else{
				PF_L3 -= random;
			}
		}else if(probability > 65){
			if((PF_L3 + random) >= PF_L3_MAX)
			{
				PF_L3 = PF_L3_MAX;
			}else{
				PF_L3 += random;
			}
			
		}
		//Calculate Active Power
		kVA_L3 = kW_L3/(PF_L3);
		//Calculate Reactive Power
		kVAR_L3 = kVA_L3 - kW_L3;
		//Calculate System Current
		A_L3 = 1000*kVA_L3/V_L3;
		//Calculate Active Energy
		kWh_L3_diff = kW_L3/60;
		kWh_L3 += kWh_L3_diff;
		if(kWh_L3 == Double.MAX_VALUE || kWh_L3 < 0){
			kWh_L3 = 0;
		}
		//Calculate Reactive Energy
		kVARh_L3_diff = kVAR_L3/60;
		kVARh_L3 += kVARh_L3_diff;
		if(kVARh_L3 == Double.MAX_VALUE || kVARh_L3 < 0){
			kVARh_L3 = 0;
		}
		//Calculate Apparent Energy
		kVAh_L3_diff = kVA_L3/60;
		kVAh_L3 += kVAh_L3_diff;
		if(kVAh_L3 == Double.MAX_VALUE || kVAh_L3 < 0){
			kVAh_L3 = 0;
		}
		
		////System
		//Calculate Active Energy
		kWh_System_diff = kWh_L1_diff + kWh_L2_diff + kWh_L3_diff;
		kWh_System += kWh_System_diff;
		if(kWh_System == Double.MAX_VALUE || kWh_System < 0){
			kWh_System = 0;
		}
		kW_System = kW_L1 + kW_L2 + kW_L3;
		//Calculate Reactive Energy
		kVARh_System_diff = kVARh_L1_diff + kVARh_L2_diff + kVARh_L3_diff;
		kVARh_System += kVARh_System_diff;
		if(kVARh_System == Double.MAX_VALUE || kVARh_System < 0){
			kVARh_System = 0;
		}
		kVAR_System = kVAR_L1 + kVAR_L2 + kVAR_L3;
		//Calculate Apparent Energy
		kVAh_System_diff = kVAh_L1_diff + kVAh_L2_diff + kVAh_L3_diff;
		kVAh_System += kVAh_System_diff;
		if(kVAh_System == Double.MAX_VALUE || kVAh_System < 0){
			kVAh_System = 0;
		}
		kVA_System = kVA_L1 + kVA_L2 +kVA_L3;
		//Calculate Power Factor
		if(kVA_System != 0){
			PF_System = kW_System/kVA_System;
		}else{
			PF_System = 1;
		}
		//Calculate Current
		A_System = A_L1 + A_L2 + A_L3;
		//Calculate Voltage
		if(A_System != 0){
			V_System = 1000*kVA_System/A_System;
		}else{
			V_System = V_L1;
		}
		//Calculate Neutral Current
		A_Neutral = Math.abs(A_L1*A_L1 + A_L2*A_L2 + A_L3*A_L3 -A_L1*A_L2 - A_L2*A_L3 - A_L3*A_L1);
		
		saveData();
	}

	private String createDataSendToServer() {
		if (getStatus() != GKProtocol.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		// System
		data.append("|DEVICEID=" + getId() + "-0-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + vformat.format(kWh_System * Scalar_Energy) + ",kWh");
		data.append(";Reactive Energy Reading=" + vformat.format(kVARh_System * Scalar_Energy) + ",kVARh");
		data.append(";Apparent Energy Reading=" + vformat.format(kVAh_System * Scalar_Energy) + ",kVAh");
		data.append(";Active Energy=" + vformat.format(kWh_System_diff*1000) + ",Wh");
		data.append(";Reactive Energy=" + vformat.format(kVARh_System_diff*1000) + ",VARh");
		data.append(";Apparent Energy=" + vformat.format(kVAh_System_diff*1000) + ",VAh");
		data.append(";Current Neutral=" + vformat.format(A_Neutral * Scalar_A) + ",A");
		data.append(";Frequency=" + vformat.format(Hz_System * Scalar_Hz) + ",Hz");
		data.append(";Active Power=" + vformat.format(kW_System * Scalar_Power) + ",kW");
		data.append(";Reactive Power=" + vformat.format(kVAR_System * Scalar_Power) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(kVA_System * Scalar_Power) + ",kVA");
		data.append(";Voltage L1-L2=" + vformat.format(V_L1_L2 * Scalar_V) + ",V");
		data.append(";Voltage L2-L3=" + vformat.format(V_L2_L3 * Scalar_V) + ",V");
		data.append(";Voltage L1-L3=" + vformat.format(V_L1_L3 * Scalar_V) + ",V");
		data.append(";Current=" + vformat.format((A_L1 + A_L2 + A_L3) * Scalar_A) + ",A");
		data.append(";Power Factor=" + vformat.format(PF_System * Scalar_PF) + ",None");
		data.append(";Voltage=" + vformat.format(V_L1 * Scalar_V) + ",V");
		data.append(";Peak Demand=" + vformat.format((kW_Demand_L1)+(kW_Demand_L2)+(kW_Demand_L3)) + ",kW");

		// Phase L1
		data.append("|DEVICEID=" + getId() + "-0-1");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + vformat.format(kWh_L1 * Scalar_Energy) + ",kWh");
		data.append(";Reactive Energy Reading=" + vformat.format(kVARh_L1 * Scalar_Energy) + ",kVARh");
		data.append(";Apparent Energy Reading=" + vformat.format(kVAh_L1 * Scalar_Energy) + ",kVAh");
		data.append(";Active Energy=" + vformat.format(kWh_L1_diff*1000) + ",Wh");
		data.append(";Reactive Energy=" + vformat.format(kVARh_L1_diff*1000) + ",VARh");
		data.append(";Apparent Energy=" + vformat.format(kVAh_L1_diff*1000) + ",VAh");
		data.append(";Active Power=" + vformat.format(kW_L1 * Scalar_Power) + ",kW");
		data.append(";Reactive Power=" + vformat.format(kVAR_L1 * Scalar_Power) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(kVA_L1 * Scalar_Power) + ",kVA");
		data.append(";Power Factor=" + vformat.format(PF_L1 * Scalar_PF) + ",None");
		data.append(";Voltage=" + vformat.format(V_L1 * Scalar_V) + ",V");
		data.append(";Current=" + vformat.format(A_L1 * Scalar_A) + ",A");
		data.append(";Frequency=" + vformat.format(Hz_System * Scalar_Hz) + ",Hz");
		data.append(";Peak Demand=" + vformat.format(kW_Demand_L1 * Scalar_Power) + ",kW");

		// Phase L2
		data.append("|DEVICEID=" + getId() + "-0-2");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + vformat.format(kWh_L2 * Scalar_Energy) + ",kWh");
		data.append(";Reactive Energy Reading=" + vformat.format(kVARh_L2 * Scalar_Energy) + ",kVARh");
		data.append(";Apparent Energy Reading=" + vformat.format(kVAh_L2 * Scalar_Energy) + ",kVAh");
		data.append(";Active Energy=" + vformat.format(kWh_L2_diff*1000) + ",Wh");
		data.append(";Reactive Energy=" + vformat.format(kVARh_L2_diff*1000) + ",VARh");
		data.append(";Apparent Energy=" + vformat.format(kVAh_L2_diff*1000) + ",VAh");
		data.append(";Active Power=" + vformat.format(kW_L2 * Scalar_Power) + ",kW");
		data.append(";Reactive Power=" + vformat.format(kVAR_L2 * Scalar_Power) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(kVA_L2 * Scalar_Power) + ",kVA");
		data.append(";Power Factor=" + vformat.format(PF_L2 * Scalar_PF) + ",None");
		data.append(";Voltage=" + vformat.format(V_L2 * Scalar_V) + ",V");
		data.append(";Current=" + vformat.format(A_L2 * Scalar_A) + ",A");
		data.append(";Frequency=" + vformat.format(Hz_System * Scalar_Hz) + ",Hz");
		data.append(";Peak Demand=" + vformat.format(kW_Demand_L2 * Scalar_Power) + ",kW");

		// Phase L3
		data.append("|DEVICEID=" + getId() + "-0-3");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + vformat.format(kWh_L3 * Scalar_Energy) + ",kWh");
		data.append(";Reactive Energy Reading=" + vformat.format(kVARh_L3 * Scalar_Energy) + ",kVARh");
		data.append(";Apparent Energy Reading=" + vformat.format(kVAh_L3 * Scalar_Energy) + ",kVAh");
		data.append(";Active Energy=" + vformat.format(kWh_L3_diff*1000) + ",Wh");
		data.append(";Reactive Energy=" + vformat.format(kVARh_L3_diff*1000) + ",VARh");
		data.append(";Apparent Energy=" + vformat.format(kVAh_L3_diff*1000) + ",VAh");
		data.append(";Active Power=" + vformat.format(kW_L3 * Scalar_Power) + ",kW");
		data.append(";Reactive Power=" + vformat.format(kVAR_L3 * Scalar_Power) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(kVA_L3 * Scalar_Power) + ",kVA");
		data.append(";Power Factor=" + vformat.format(PF_L3 * Scalar_PF) + ",None");
		data.append(";Voltage=" + vformat.format(V_L3 * Scalar_V) + ",V");
		data.append(";Current=" + vformat.format(A_L3 * Scalar_A) + ",A");
		data.append(";Frequency=" + vformat.format(Hz_System * Scalar_Hz) + ",Hz");
		data.append(";Peak Demand=" + vformat.format(kW_Demand_L3 * Scalar_Power) + ",kW");

		return data.toString();

	}

	
	public void setL1SimData(Double max, Double min, Double delta, Double scale) {
		if (max != null) {
			KW_L1_MAX = max;
		}
		if (min != null) {
			KW_L1_MIN = min;
		}
		if (KW_L1_MIN > KW_L1_MAX) {
			double tmp = KW_L1_MAX;
			KW_L1_MAX = KW_L1_MIN;
			KW_L1_MIN = tmp;
		}

		if (scale != null) {
			KW_L1_DELTA_SCALE = scale;
		}

		if (delta != null) {
			KW_L1_DELTA = delta;
		} else {
			KW_L1_DELTA = (KW_L1_MAX - KW_L1_MIN) * KW_L1_DELTA_SCALE;
		}
		
		kW_L1 = KW_L1_MIN;
	}

	public void setL2SimData(Double max, Double min, Double delta, Double scale) {
		if (max != null) {
			KW_L2_MAX = max;
		}
		if (min != null) {
			KW_L2_MIN = min;
		}
		if (KW_L2_MIN > KW_L2_MAX) {
			double tmp = KW_L2_MAX;
			KW_L2_MAX = KW_L2_MIN;
			KW_L2_MIN = tmp;
		}

		if (scale != null) {
			KW_L2_DELTA_SCALE = scale;
		}

		if (delta != null) {
			KW_L2_DELTA = delta;
		} else {
			KW_L2_DELTA = (KW_L2_MAX - KW_L2_MIN) * KW_L2_DELTA_SCALE;
		}
		
		kW_L2 = KW_L2_MIN;
	}

	public void setL3SimData(Double max, Double min, Double delta, Double scale) {
		if (max != null) {
			KW_L3_MAX = max;
		}
		if (min != null) {
			KW_L3_MIN = min;
		}
		if (KW_L3_MIN > KW_L3_MAX) {
			double tmp = KW_L3_MAX;
			KW_L3_MAX = KW_L3_MIN;
			KW_L3_MIN = tmp;
		}

		if (scale != null) {
			KW_L3_DELTA_SCALE = scale;
		}

		if (delta != null) {
			KW_L3_DELTA = delta;
		} else {
			KW_L3_DELTA = (KW_L3_MAX - KW_L3_MIN) * KW_L3_DELTA_SCALE;
		}
		
		kW_L3 = KW_L3_MIN;
	}
	
	
    public boolean saveData() {
    	mLogger.info("save data");
    	Properties _props = new Properties();
    	try {
			_props.setProperty("kWh_System",String.valueOf(kWh_System));
    		_props.setProperty("kVARh_System",String.valueOf(kVARh_System));
    		_props.setProperty("kVAh_System",String.valueOf(kVAh_System));
    		
    		_props.setProperty("kWh_L1",String.valueOf(kWh_L1));
    		_props.setProperty("kVARh_L1",String.valueOf(kVARh_L1));
    		_props.setProperty("kVAh_L1",String.valueOf(kVAh_L1));
    		
    		_props.setProperty("kWh_L2",String.valueOf(kWh_L2));
    		_props.setProperty("kVARh_L2",String.valueOf(kVARh_L2));
    		_props.setProperty("kVAh_L2",String.valueOf(kVAh_L2));
    		
    		_props.setProperty("kWh_L3",String.valueOf(kWh_L3));
    		_props.setProperty("kVARh_L3",String.valueOf(kVARh_L3));
    		_props.setProperty("kVAh_L3",String.valueOf(kVAh_L3));
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
				kWh_System = Double.parseDouble(_props.getProperty("kWh_System"));
			} catch (NumberFormatException ex) {
				//mLogger.error("Wrong volume data,use default " );
			}
			try {
				kVARh_System = Double.parseDouble(_props.getProperty("kVARh_System"));
			} catch (NumberFormatException ex) {
				//mLogger.error("Wrong volume data,use default " );
			}
			try {
				kVAh_System = Double.parseDouble(_props.getProperty("kVAh_System"));
			} catch (NumberFormatException ex) {
				//mLogger.error("Wrong volume data,use default " );
			}
			
			try {
				kWh_L1 = Double.parseDouble(_props.getProperty("kWh_L1"));
			} catch (NumberFormatException ex) {
				//mLogger.error("Wrong volume data,use default " );
			}
			try {
				kVARh_L1 = Double.parseDouble(_props.getProperty("kVARh_L1"));
			} catch (NumberFormatException ex) {
				//mLogger.error("Wrong volume data,use default " );
			}
			try {
				kVAh_L1 = Double.parseDouble(_props.getProperty("kVAh_L1"));
			} catch (NumberFormatException ex) {
				//mLogger.error("Wrong volume data,use default " );
			}
			
			try {
				kWh_L2 = Double.parseDouble(_props.getProperty("kWh_L2"));
			} catch (NumberFormatException ex) {
				//mLogger.error("Wrong volume data,use default " );
			}
			try {
				kVARh_L2 = Double.parseDouble(_props.getProperty("kVARh_L2"));
			} catch (NumberFormatException ex) {
				//mLogger.error("Wrong volume data,use default " );
			}
			try {
				kVAh_L2 = Double.parseDouble(_props.getProperty("kVAh_L2"));
			} catch (NumberFormatException ex) {
				//mLogger.error("Wrong volume data,use default " );
			}
			
			try {
				kWh_L3 = Double.parseDouble(_props.getProperty("kWh_L3"));
			} catch (NumberFormatException ex) {
				//mLogger.error("Wrong volume data,use default " );
			}
			try {
				kVARh_L3 = Double.parseDouble(_props.getProperty("kVARh_L3"));
			} catch (NumberFormatException ex) {
				//mLogger.error("Wrong volume data,use default " );
			}
			try {
				kVAh_L3 = Double.parseDouble(_props.getProperty("kVAh_L3"));
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
