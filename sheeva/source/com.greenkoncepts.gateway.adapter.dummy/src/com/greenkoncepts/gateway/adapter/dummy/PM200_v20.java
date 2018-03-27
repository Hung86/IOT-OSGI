package com.greenkoncepts.gateway.adapter.dummy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.greenkoncepts.gateway.util.FuncUtil;

public class PM200_v20 extends DummyDevice {


	final static int CHANNEL_NUM = 4;

	private double[]KW_L1_MAX = {0.236,0.236,0.236,0.236};
	private double[]KW_L1_MIN = {0, 0, 0 ,0};
	private double[]KW_L1_DELTA_SCALE={0.6, 0.6,0.6,0.6};
	private double[]KW_L1_DELTA = {KW_L1_MAX[0]*KW_L1_DELTA_SCALE[0],KW_L1_MAX[1]*KW_L1_DELTA_SCALE[1],KW_L1_MAX[2]*KW_L1_DELTA_SCALE[2],KW_L1_MAX[3]*KW_L1_DELTA_SCALE[3]};
	
    private double[]PF_L1_MAX = {1 ,1 ,1 ,1};
    private double[]PF_L1_MIN = {0.8, 0.8, 0.8, 0.8};
    private double[]PF_L1_DELTA = {0.03, 0.03, 0.03, 0.03};
    
	private double[]KW_L2_MAX = {0.236,0.236,0.236,0.236};
	private double[]KW_L2_MIN = {0, 0, 0 ,0};
	private double[]KW_L2_DELTA_SCALE={0.6, 0.6,0.6,0.6};
	private double[]KW_L2_DELTA = {KW_L2_MAX[0]*KW_L2_DELTA_SCALE[0],KW_L2_MAX[1]*KW_L2_DELTA_SCALE[1],KW_L2_MAX[2]*KW_L2_DELTA_SCALE[2],KW_L2_MAX[3]*KW_L2_DELTA_SCALE[3]};
	
    private double[]PF_L2_MAX = {1 ,1 ,1 ,1};
    private double[]PF_L2_MIN = {0.8, 0.8, 0.8, 0.8};
    private double[]PF_L2_DELTA = {0.03, 0.03, 0.03, 0.03};
    
	private double[]KW_L3_MAX = {0.236,0.236,0.236,0.236};
	private double[]KW_L3_MIN = {0, 0, 0 ,0};
	private double[]KW_L3_DELTA_SCALE={0.6, 0.6,0.6,0.6};
	private double[]KW_L3_DELTA = {KW_L3_MAX[0]*KW_L3_DELTA_SCALE[0],KW_L3_MAX[1]*KW_L3_DELTA_SCALE[1],KW_L3_MAX[2]*KW_L3_DELTA_SCALE[2],KW_L3_MAX[3]*KW_L3_DELTA_SCALE[3]};
	
    private double[]PF_L3_MAX = {1 ,1 ,1 ,1};
    private double[]PF_L3_MIN = {0.8, 0.8, 0.8, 0.8};
    private double[]PF_L3_DELTA = {0.03, 0.03, 0.03, 0.03};
    

	private double[] Scalar_Energy = { 1, 1, 1, 1 };
	private double[] Scalar_Power = { 1, 1, 1, 1 };
	private double[] Scalar_A = { 1, 1, 1, 1 };
	private double[] Scalar_V = { 1, 1, 1, 1 };
	private double[] Scalar_PF = { 1, 1, 1, 1 };
	private double[] Scalar_Hz = { 1, 1, 1, 1 };


	private double[] kWh_L1 = { 0, 0, 0, 0 };
	private double[] kWh_L1_diff = { 0, 0, 0, 0 };
	private double[] kVARh_L1 = { 0, 0, 0, 0 };
	private double[] kVARh_L1_diff = { 0, 0, 0, 0 };
	private double[] kWh_L2 = { 0, 0, 0, 0 };
	private double[] kWh_L2_diff = { 0, 0, 0, 0 };
	private double[] kVARh_L2 = { 0, 0, 0, 0 };
	private double[] kVARh_L2_diff = { 0, 0, 0, 0 };
	private double[] kWh_L3 = { 0, 0, 0, 0 };
	private double[] kWh_L3_diff = { 0, 0, 0, 0 };
	private double[] kVARh_L3 = { 0, 0, 0, 0 };
	private double[] kVARh_L3_diff = { 0, 0, 0, 0 };

	private double[] kWh_System = { 0, 0, 0, 0 };
	private double[] kWh_L1_P = { 0, 0, 0, 0 };
	private double[] kWh_L1_N = { 0, 0, 0, 0 };
	private double[] kWh_L2_P = { 0, 0, 0, 0 };
	private double[] kWh_L2_N = { 0, 0, 0, 0 };
	private double[] kWh_L3_P = { 0, 0, 0, 0 };
	private double[] kWh_L3_N = { 0, 0, 0, 0 };
	private double[] kVARh_System = { 0, 0, 0, 0 };
	private double[] kVARh_L1_P = { 0, 0, 0, 0 };
	private double[] kVARh_L1_N = { 0, 0, 0, 0 };
	private double[] kVARh_L2_P = { 0, 0, 0, 0 };
	private double[] kVARh_L2_N = { 0, 0, 0, 0 };
	private double[] kVARh_L3_P = { 0, 0, 0, 0 };
	private double[] kVARh_L3_N = { 0, 0, 0, 0 };
	private double[] kVAh_System = { 0, 0, 0, 0 };
	private double[] kVAh_L1 = { 0, 0, 0, 0 };
	private double[] kVAh_L2 = { 0, 0, 0, 0 };
	private double[] kVAh_L3 = { 0, 0, 0, 0 };
	private double[] kWh_System_diff = { 0, 0, 0, 0 };
	private double[] kWh_L1_P_diff = { 0, 0, 0, 0 };
	private double[] kWh_L1_N_diff = { 0, 0, 0, 0 };
	private double[] kWh_L2_P_diff = { 0, 0, 0, 0 };
	private double[] kWh_L2_N_diff = { 0, 0, 0, 0 };
	private double[] kWh_L3_P_diff = { 0, 0, 0, 0 };
	private double[] kWh_L3_N_diff = { 0, 0, 0, 0 };
	private double[] kVARh_System_diff = { 0, 0, 0, 0 };
	private double[] kVARh_L1_P_diff = { 0, 0, 0, 0 };
	private double[] kVARh_L1_N_diff = { 0, 0, 0, 0 };
	private double[] kVARh_L2_P_diff = { 0, 0, 0, 0 };
	private double[] kVARh_L2_N_diff = { 0, 0, 0, 0 };
	private double[] kVARh_L3_P_diff = { 0, 0, 0, 0 };
	private double[] kVARh_L3_N_diff = { 0, 0, 0, 0 };
	private double[] kVAh_System_diff = { 0, 0, 0, 0 };
	private double[] kVAh_L1_diff = { 0, 0, 0, 0 };
	private double[] kVAh_L2_diff = { 0, 0, 0, 0 };
	private double[] kVAh_L3_diff = { 0, 0, 0, 0 };
	private double[] kW_System = { 0, 0, 0, 0 };
	private double[] kW_L1 = { 0, 0, 0, 0 };
	private double[] kW_L2 = { 0, 0, 0, 0 };
	private double[] kW_L3 = { 0, 0, 0, 0 };
	private double[] kVAR_System = { 0, 0, 0, 0 };
	private double[] kVAR_L1 = { 0, 0, 0, 0 };
	private double[] kVAR_L2 = { 0, 0, 0, 0 };
	private double[] kVAR_L3 = { 0, 0, 0, 0 };
	private double[] kVA_System = { 0, 0, 0, 0 };
	private double[] kVA_L1 = { 0, 0, 0, 0 };
	private double[] kVA_L2 = { 0, 0, 0, 0 };
	private double[] kVA_L3 = { 0, 0, 0, 0 };
	private double[] PF_L1 = { 0.8, 0.8, 0.8, 0.8 };
	private double[] PF_L2 = { 0.8, 0.8, 0.8, 0.8 };
	private double[] PF_L3 = { 0.8, 0.8, 0.8, 0.8 };
	private double[] V_System = { 0, 0, 0, 0 };
	private double[] V_L1 = { 230, 230, 230, 230 };
	private double[] V_L2 = { 230, 230, 230, 230 };
	private double[] V_L3 = { 230, 230, 230, 230 };
	private double[] A_System = { 0, 0, 0, 0 };
	private double[] A_L1 = { 0, 0, 0, 0 };
	private double[] A_L2 = { 0, 0, 0, 0 };
	private double[] A_L3 = { 0, 0, 0, 0 };
	private double[] A_Neutral = { 0, 0, 0, 0 };
	private double[] Hz_System = { 50, 50, 50, 50 };
	private double[] kW_Demand_L1 = { 0.05, 0.05, 0.05, 00.05 };
	private double[] kW_Demand_L2 = { 0.06, 0.06, 0.06, 0.06 };
	private double[] kW_Demand_L3 = { 0.07, 0.07, 0.07, 0.07 };
	private double[] V_L1_L2 = { 400, 400, 400, 400 };
	private double[] V_L2_L3 = { 400, 400, 400, 400 };
	private double[] V_L1_L3 = { 400, 400, 400, 400 };
	private double[] PF_System = { 0.8, 0.8, 0.8, 0.8};

	private String _savefile;
	
	public PM200_v20(int addr, String cat) {
		super(addr, cat);
		_savefile = "Dummy_Data_PM200_" + addr;
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
			for (int j = 0; j < CHANNEL_NUM; j++) {
				Map<String, String> item = new HashMap<String, String>();
				String chan = "ch" + j + "_";
				item.put(chan + "kWh_System", vformat.format(kWh_System[j] * Scalar_Energy[j]));
				item.put(chan + "kVARh_System", vformat.format(kVARh_System[j] * Scalar_Energy[j]));
				item.put(chan + "kVAh_System", vformat.format(kVAh_System[j] * Scalar_Energy[j]));
				item.put(chan + "A_Neutral", vformat.format(A_Neutral[j] * Scalar_A[j]));
				item.put(chan + "Hz_System", vformat.format(Hz_System[j] * Scalar_Hz[j]));
				item.put(chan + "kW_System", vformat.format(kW_System[j] * Scalar_Power[j]));
				item.put(chan + "kVAR_System", vformat.format(kVAR_System[j] * Scalar_Power[j]));
				item.put(chan + "kVA_System", vformat.format(kVA_System[j]* Scalar_Power[j]));
				item.put(chan + "V_System", vformat.format(V_System[j] * Scalar_V[j]));
				item.put(chan + "A_System", vformat.format(A_System[j] * Scalar_A[j]));
				item.put(chan + "kW_Demand_System",
						vformat.format((Math.abs(kW_Demand_L1[j]) + Math.abs(kW_Demand_L2[j]) + Math.abs(kW_Demand_L3[j])) * Scalar_Power[j]));
				item.put(chan + "V_L1_L2", vformat.format(Math.abs(V_L1_L2[j] * Scalar_V[j])));
				item.put(chan + "V_L2_L3", vformat.format(Math.abs(V_L2_L3[j] * Scalar_V[j])));
				item.put(chan + "V_L1_L3", vformat.format(Math.abs(V_L1_L3[j] * Scalar_V[j])));
				item.put(chan + "PF_System", vformat.format(PF_System[j] * Scalar_PF[j]));
				// Phase L1
				item.put(chan + "kWh_L1", vformat.format((kWh_L1[j]) * Scalar_Energy[j]));
				item.put(chan + "kVARh_L1", vformat.format((kVARh_L1[j]) * Scalar_Energy[j]));
				item.put(chan + "kVAh_L1", vformat.format(kVAh_L1[j] * Scalar_Energy[j]));
				item.put(chan + "kW_L1", vformat.format(Math.abs(kW_L1[j]) * Scalar_Power[j]));
				item.put(chan + "kVAR_L1", vformat.format(Math.abs(kVAR_L1[j]) * Scalar_Power[j]));
				item.put(chan + "kVA_L1", vformat.format(kVA_L1[j] * Scalar_Power[j]));
				item.put(chan + "PF_L1", vformat.format(PF_L1[j] * Scalar_PF[j]));
				item.put(chan + "V_L1", vformat.format(V_L1[j] * Scalar_V[j]));
				item.put(chan + "A_L1", vformat.format(Math.abs(A_L1[j]) * Scalar_A[j]));
				item.put(chan + "kW_Demand_L1", vformat.format(Math.abs(kW_Demand_L1[j]) * Scalar_Power[j]));
				// Phase L2
				item.put(chan + "kWh_L2", vformat.format((kWh_L2[j]) * Scalar_Energy[j]));
				item.put(chan + "kVARh_L2", vformat.format((kVARh_L2[j]) * Scalar_Energy[j]));
				item.put(chan + "kVAh_L2", vformat.format(kVAh_L2[j] * Scalar_Energy[j]));
				item.put(chan + "kW_L2", vformat.format(Math.abs(kW_L2[j]) * Scalar_Power[j]));
				item.put(chan + "kVAR_L2", vformat.format(Math.abs(kVAR_L2[j]) * Scalar_Power[j]));
				item.put(chan + "kVA_L2", vformat.format(kVA_L2[j] * Scalar_Power[j]));
				item.put(chan + "PF_L2", vformat.format(PF_L2[j] * Scalar_PF[j]));
				item.put(chan + "V_L2", vformat.format(V_L2[j] * Scalar_V[j]));
				item.put(chan + "A_L2", vformat.format(Math.abs(A_L2[j]) * Scalar_A[j]));
				item.put(chan + "kW_Demand_L2", vformat.format(Math.abs(kW_Demand_L2[j]) * Scalar_Power[j]));
				// Phase L3
				item.put(chan + "kWh_L3", "" + vformat.format((kWh_L3[j]) * Scalar_Energy[j]));
				item.put(chan + "kVARh_L3", "" + vformat.format((kVARh_L3[j]) * Scalar_Energy[j]));
				item.put(chan + "kVAh_L3", "" + vformat.format(kVAh_L3[j] * Scalar_Energy[j]));
				item.put(chan + "kW_L3", "" + vformat.format(Math.abs(kW_L3[j]) * Scalar_Power[j]));
				item.put(chan + "kVAR_L3", "" + vformat.format(Math.abs(kVAR_L3[j]) * Scalar_Power[j]));
				item.put(chan + "kVA_L3", "" + vformat.format(kVA_L3[j] * Scalar_Power[j]));
				item.put(chan + "PF_L3", "" + vformat.format(PF_L3[j] * Scalar_PF[j]));
				item.put(chan + "V_L3", "" + vformat.format(V_L3[j] * Scalar_V[j]));
				item.put(chan + "A_L3", "" + vformat.format(Math.abs(A_L3[j]) * Scalar_A[j]));
				item.put(chan + "kW_Demand_L3", vformat.format(Math.abs(kW_Demand_L3[j]) * Scalar_Power[j]));

				real_time_data.add(item);
			}
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
		for (int i = 0; i < CHANNEL_NUM; i++) {
			////L1
			//Calculate System Apparent Power
			random = FuncUtil.randomWithRange(0,KW_L1_DELTA[i])/*(KW_L1_MAX_DELTA + (Math.random() * (KW_L1_MAX_DELTA - KW_L1_MIN_DELTA)))*/;
			probability = FuncUtil.randomWithRange(0,100)/*(100 + (Math.random() * 100))*/;
			if(probability < 35){
				if((kW_L1[i] - random) <= KW_L1_MIN[i])
				{
					kW_L1[i] = KW_L1_MIN[i];
				}else{
					kW_L1[i] -= random;
				}
			}else if(probability > 65){
				if((kW_L1[i] + random) >= KW_L1_MAX[i])
				{
					kW_L1[i] = KW_L1_MAX[i];
				}else{
					kW_L1[i] += random;
				}
			
			}
			
			//Calculate System Power Factor
			random = FuncUtil.randomWithRange(0,PF_L1_DELTA[i])/*(PF_L1_MAX_DELTA + (Math.random() * (PF_L1_MAX_DELTA - PF_L1_MIN_DELTA)))*/;
			probability = FuncUtil.randomWithRange(0,100)/*(100 + (Math.random() * 100))*/;
			if(probability < 35){
				if((PF_L1[i] - random) <= PF_L1_MIN[i])
				{
					PF_L1[i] = PF_L1_MIN[i];
				}else{
					PF_L1[i] -= random;
				}
			}else if(probability > 65){
				if((PF_L1[i] + random) >= PF_L1_MAX[i])
				{
					PF_L1[i] = PF_L1_MAX[i];
				}else{
					PF_L1[i] += random;
				}
				
			}else{
				//Do nothing
			}
			
			//Calculate Active Power
			kVA_L1[i]= kW_L1[i]/(PF_L1[i]);
			//Calculate Reactive Power
			kVAR_L1[i] = kVA_L1[i] - kW_L1[i];
			//Calculate System Current
			A_L1[i] = 1000*kVA_L1[i]/V_L1[i];
			//Calculate Active Energy
			kWh_L1_diff[i] = kW_L1[i]/60;
			kWh_L1[i] += kWh_L1_diff[i];
			if(kWh_L1[i] == Double.MAX_VALUE || kWh_L1[i] < 0){
				kWh_L1[i] = 0;
			}
			//Calculate Reactive Energy
			kVARh_L1_diff[i] = kVAR_L1[i]/60;
			kVARh_L1[i] += kVARh_L1_diff[i];
			if(kVARh_L1[i] == Double.MAX_VALUE || kVARh_L1[i] < 0){
				kVARh_L1[i] = 0;
			}
			//Calculate Apparent Energy
			kVAh_L1_diff[i] = kVA_L1[i]/60;
			kVAh_L1[i] += kVAh_L1_diff[i];
			if(kVAh_L1[i] == Double.MAX_VALUE || kVAh_L1[i] < 0){
				kVAh_L1[i] = 0;
			}
			
			////L2
			//Calculate System Apparent Power
			random = FuncUtil.randomWithRange(0,KW_L2_DELTA[i])/*(KW_L2_MAX_DELTA + (Math.random() * (KW_L2_MAX_DELTA - KW_L2_MIN_DELTA)))*/;
			probability = FuncUtil.randomWithRange(0,100)/*(100 + (Math.random() * 100))*/;
			if(probability < 35){
				if((kW_L2[i] - random) <= KW_L2_MIN[i])
				{
					kW_L2[i] = KW_L2_MIN[i];
				}else{
					kW_L2[i] -= random;
				}
			}else if(probability > 65){
				if((kW_L2[i] + random) >= KW_L2_MAX[i])
				{
					kW_L2[i] = KW_L2_MAX[i];
				}else{
					kW_L2[i] += random;
				}
			
			}
			
			//Calculate System Power Factor
			random = FuncUtil.randomWithRange(0,PF_L2_DELTA[i])/*(PF_L2_MAX_DELTA + (Math.random() * (PF_L2_MAX_DELTA - PF_L2_MIN_DELTA)))*/;
			probability = FuncUtil.randomWithRange(0,100)/*(100 + (Math.random() * 100))*/;
			if(probability < 35){
				if((PF_L2[i] - random) <= PF_L2_MIN[i])
				{
					PF_L2[i] = PF_L2_MIN[i];
				}else{
					PF_L2[i] -= random;
				}
			}else if(probability > 65){
				if((PF_L2[i] + random) >= PF_L2_MAX[i])
				{
					PF_L2[i] = PF_L2_MAX[i];
				}else{
					PF_L2[i] += random;
				}
				
			}else{
				//Do nothing
			}
			
			//Calculate Active Power
			kVA_L2[i]= kW_L2[i]/(PF_L2[i]);
			//Calculate Reactive Power
			kVAR_L2[i] = kVA_L2[i] - kW_L2[i];
			//Calculate System Current
			A_L2[i] = 1000*kVA_L2[i]/V_L2[i];
			//Calculate Active Energy
			kWh_L2_diff[i] = kW_L2[i]/60;
			kWh_L2[i] += kWh_L2_diff[i];
			if(kWh_L2[i] == Double.MAX_VALUE || kWh_L2[i] < 0){
				kWh_L2[i] = 0;
			}
			//Calculate Reactive Energy
			kVARh_L2_diff[i] = kVAR_L2[i]/60;
			kVARh_L2[i] += kVARh_L2_diff[i];
			if(kVARh_L2[i] == Double.MAX_VALUE || kVARh_L2[i] < 0){
				kVARh_L2[i] = 0;
			}
			//Calculate Apparent Energy
			kVAh_L2_diff[i] = kVA_L2[i]/60;
			kVAh_L2[i] += kVAh_L2_diff[i];
			if(kVAh_L2[i] == Double.MAX_VALUE || kVAh_L2[i] < 0){
				kVAh_L2[i] = 0;
			}
			
			////L3
			//Calculate System Apparent Power
			random = FuncUtil.randomWithRange(0,KW_L3_DELTA[i])/*(KW_L3_MAX_DELTA + (Math.random() * (KW_L3_MAX_DELTA - KW_L3_MIN_DELTA)))*/;
			probability = FuncUtil.randomWithRange(0,100)/*(100 + (Math.random() * 100))*/;
			if(probability < 35){
				if((kW_L3[i] - random) <= KW_L3_MIN[i])
				{
					kW_L3[i] = KW_L3_MIN[i];
				}else{
					kW_L3[i] -= random;
				}
			}else if(probability > 65){
				if((kW_L3[i] + random) >= KW_L3_MAX[i])
				{
					kW_L3[i] = KW_L3_MAX[i];
				}else{
					kW_L3[i] += random;
				}
			
			}
			
			//Calculate System Power Factor
			random = FuncUtil.randomWithRange(0,PF_L3_DELTA[i])/*(PF_L3_MAX_DELTA + (Math.random() * (PF_L3_MAX_DELTA - PF_L3_MIN_DELTA)))*/;
			probability = FuncUtil.randomWithRange(0,100)/*(100 + (Math.random() * 100))*/;
			if(probability < 35){
				if((PF_L3[i] - random) <= PF_L3_MIN[i])
				{
					PF_L3[i] = PF_L3_MIN[i];
				}else{
					PF_L3[i] -= random;
				}
			}else if(probability > 65){
				if((PF_L3[i] + random) >= PF_L3_MAX[i])
				{
					PF_L3[i] = PF_L3_MAX[i];
				}else{
					PF_L3[i] += random;
				}
				
			}else{
				//Do nothing
			}
			
			//Calculate Active Power
			kVA_L3[i]= kW_L3[i]/(PF_L3[i]);
			//Calculate Reactive Power
			kVAR_L3[i] = kVA_L3[i] - kW_L3[i];
			//Calculate System Current
			A_L3[i] = 1000*kVA_L3[i]/V_L3[i];
			//Calculate Active Energy
			kWh_L3_diff[i] = kW_L3[i]/60;
			kWh_L3[i] += kWh_L3_diff[i];
			if(kWh_L3[i] == Double.MAX_VALUE || kWh_L3[i] < 0){
				kWh_L3[i] = 0;
			}
			//Calculate Reactive Energy
			kVARh_L3_diff[i] = kVAR_L3[i]/60;
			kVARh_L3[i] += kVARh_L3_diff[i];
			if(kVARh_L3[i] == Double.MAX_VALUE || kVARh_L3[i] < 0){
				kVARh_L3[i] = 0;
			}
			//Calculate Apparent Energy
			kVAh_L3_diff[i] = kVA_L3[i]/60;
			kVAh_L3[i] += kVAh_L3_diff[i];
			if(kVAh_L3[i] == Double.MAX_VALUE || kVAh_L3[i] < 0){
				kVAh_L3[i] = 0;
			}
			
			
			////System
			//Calculate Active Energy
			kWh_System_diff[i] = kWh_L1_diff[i] + kWh_L2_diff[i] + kWh_L3_diff[i];
			kWh_System[i] += kWh_System_diff[i];
			if(kWh_System[i] == Double.MAX_VALUE || kWh_System[i] < 0){
				kWh_System[i] = 0;
			}
			kW_System[i] = kW_L1[i] + kW_L2[i] + kW_L3[i];
			//Calculate Reactive Energy
			kVARh_System_diff[i] = kVARh_L1_diff[i] + kVARh_L2_diff[i] + kVARh_L3_diff[i];
			kVARh_System[i] += kVARh_System_diff[i];
			if(kVARh_System[i] == Double.MAX_VALUE || kVARh_System[i] < 0){
				kVARh_System[i] = 0;
			}
			kVAR_System[i] = kVAR_L1[i] + kVAR_L2[i] + kVAR_L3[i];
			//Calculate Apparent Energy
			kVAh_System_diff[i] = kVAh_L1_diff[i] + kVAh_L2_diff[i] + kVAh_L3_diff[i];
			kVAh_System[i] += kVAh_System_diff[i];
			if(kVAh_System[i] == Double.MAX_VALUE || kVAh_System[i] < 0){
				kVAh_System[i] = 0;
			}
			kVA_System[i] = kVA_L1[i] + kVA_L2[i] +kVA_L3[i];
			//Calculate Power Factor
			if(kVA_System[i] != 0){
				PF_System[i] = kW_System[i]/kVA_System[i];
			}else{
				PF_System[i] = 1;
			}
			//Calculate Current
			A_System[i] = A_L1[i] + A_L2[i] + A_L3[i];
			//Calculate Voltage
			if(A_System[i] != 0){
				V_System[i] = 1000*kVA_System[i]/A_System[i];
			}else{
				V_System[i] = V_L1[i];
			}
			//Calculate Neutral Current
			A_Neutral[i] = Math.abs(A_L1[i]*A_L1[i] + A_L2[i]*A_L2[i] + A_L3[i]*A_L3[i] - A_L1[i]*A_L2[i] - A_L2[i]*A_L3[i] - A_L3[i]*A_L1[i]);
		}
		saveData();
	}

	private String createDataSendToServer() {
		if (getStatus() != GKProtocol.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		for (int ch = 0; ch < CHANNEL_NUM; ch++) {
			// System
			data.append("|DEVICEID=" + getId() + "-" + ch + "-0");
			data.append(";TIMESTAMP=" + timestamp);
			data.append(";Active Energy Reading=" + vformat.format(kWh_System[ch] * Scalar_Energy[ch]) + ",kWh");
			data.append(";Reactive Energy Reading=" + vformat.format(kVARh_System[ch] * Scalar_Energy[ch]) + ",kVARh");
			data.append(";Apparent Energy Reading=" + vformat.format(kVAh_System[ch] * Scalar_Energy[ch]) + ",kVAh");
			data.append(";Active Energy=" + vformat.format(kWh_System_diff[ch] * Scalar_Energy[ch] * 1000.0) + ",Wh");
			data.append(";Reactive Energy=" + vformat.format(kVARh_System_diff[ch] * Scalar_Energy[ch] * 1000.0) + ",VARh");
			data.append(";Apparent Energy=" + vformat.format(kVAh_System_diff[ch] * Scalar_Energy[ch] * 1000.0) + ",VAh");
			data.append(";Current Neutral=" + vformat.format(A_Neutral[ch] * Scalar_A[ch]) + ",A");
			data.append(";Frequency=" + vformat.format(Hz_System[ch] * Scalar_Hz[ch]) + ",Hz");
			data.append(";Active Power=" + vformat.format((Math.abs(kW_L1[ch]) + Math.abs(kW_L2[ch]) + Math.abs(kW_L3[ch])) * Scalar_Power[ch])
					+ ",kW");
			data.append(";Reactive Power="
					+ vformat.format((Math.abs(kVAR_L1[ch]) + Math.abs(kVAR_L2[ch]) + Math.abs(kVAR_L3[ch])) * Scalar_Power[ch]) + ",kVAR");
			data.append(";Apparent Power=" + vformat.format((kVA_L1[ch] + kVA_L2[ch] + kVA_L3[ch]) * Scalar_Power[ch]) + ",kVA");
			data.append(";Power Factor=" + vformat.format(PF_System[ch] * Scalar_PF[ch]) + ",None");
			data.append(";Voltage=" + vformat.format(V_L1[ch] * Scalar_V[ch]) + ",V");
			data.append(";Current=" + vformat.format((Math.abs(A_L1[ch]) + Math.abs(A_L2[ch]) + Math.abs(A_L3[ch])) * Scalar_A[ch]) + ",A");
			data.append(";Peak Demand="
					+ vformat.format((Math.abs(kW_Demand_L1[ch]) + Math.abs(kW_Demand_L2[ch]) + Math.abs(kW_Demand_L3[ch])) * Scalar_Power[ch])
					+ ",kW");
			data.append(";Voltage L1-L2=" + vformat.format(Math.abs(V_L1_L2[ch] * Scalar_V[ch])) + ",V");
			data.append(";Voltage L2-L3=" + vformat.format(Math.abs(V_L2_L3[ch] * Scalar_V[ch])) + ",V");
			data.append(";Voltage L1-L3=" + vformat.format(Math.abs(V_L1_L3[ch] * Scalar_V[ch])) + ",V");

			// Phase L1
			data.append("|DEVICEID=" + getId() + "-" + ch + "-1");
			data.append(";TIMESTAMP=" + timestamp);
			data.append(";Active Energy Reading=" + vformat.format((kWh_L1[ch]) * Scalar_Energy[ch]) + ",kWh");
			data.append(";Reactive Energy Reading=" + vformat.format((kVARh_L1[ch]) * Scalar_Energy[ch]) + ",kVARh");
			data.append(";Apparent Energy Reading=" + vformat.format(kVAh_L1[ch] * Scalar_Energy[ch]) + ",kVAh");
			data.append(";Active Energy=" + vformat.format((kWh_L1_diff[ch]) * Scalar_Energy[ch] * 1000.0) + ",Wh");
			data.append(";Reactive Energy=" + vformat.format((kVARh_L1_diff[ch]) * Scalar_Energy[ch] * 1000.0) + ",VARh");
			data.append(";Apparent Energy=" + vformat.format(kVAh_L1_diff[ch] * Scalar_Energy[ch] * 1000.0) + ",VAh");
			data.append(";Active Power=" + vformat.format(Math.abs(kW_L1[ch]) * Scalar_Power[ch]) + ",kW");
			data.append(";Reactive Power=" + vformat.format(Math.abs(kVAR_L1[ch]) * Scalar_Power[ch]) + ",kVAR");
			data.append(";Apparent Power=" + vformat.format(kVA_L1[ch] * Scalar_Power[ch]) + ",kVA");
			data.append(";Power Factor=" + vformat.format(PF_L1[ch] * Scalar_PF[ch]) + ",None");
			data.append(";Voltage=" + vformat.format(V_L1[ch] * Scalar_V[ch]) + ",V");
			data.append(";Current=" + vformat.format(Math.abs(A_L1[ch]) * Scalar_A[ch]) + ",A");
			data.append(";Frequency=" + vformat.format(Hz_System[ch] * Scalar_Hz[ch]) + ",Hz");
			data.append(";Peak Demand=" + vformat.format(Math.abs(kW_Demand_L1[ch]) * Scalar_Power[ch]) + ",kW");

			// Phase L2
			data.append("|DEVICEID=" + getId() + "-" + ch + "-2");
			data.append(";TIMESTAMP=" + timestamp);
			data.append(";Active Energy Reading=" + vformat.format((kWh_L2[ch]) * Scalar_Energy[ch]) + ",kWh");
			data.append(";Reactive Energy Reading=" + vformat.format((kVARh_L2[ch]) * Scalar_Energy[ch]) + ",kVARh");
			data.append(";Apparent Energy Reading=" + vformat.format(kVAh_L2[ch] * Scalar_Energy[ch]) + ",kVAh");
			data.append(";Active Energy=" + vformat.format((kWh_L2_diff[ch]) * Scalar_Energy[ch] * 1000.0) + ",Wh");
			data.append(";Reactive Energy=" + vformat.format((kVARh_L2_diff[ch]) * Scalar_Energy[ch] * 1000.0) + ",VARh");
			data.append(";Apparent Energy=" + vformat.format((kVAh_L2_diff[ch]) * Scalar_Energy[ch] * 1000.0) + ",VAh");
			data.append(";Active Power=" + vformat.format(Math.abs(kW_L2[ch]) * Scalar_Power[ch]) + ",kW");
			data.append(";Reactive Power=" + vformat.format(Math.abs(kVAR_L2[ch]) * Scalar_Power[ch]) + ",kVAR");
			data.append(";Apparent Power=" + vformat.format(kVA_L2[ch] * Scalar_Power[ch]) + ",kVA");
			data.append(";Power Factor=" + vformat.format(PF_L2[ch] * Scalar_PF[ch]) + ",None");
			data.append(";Voltage=" + vformat.format(V_L2[ch] * Scalar_V[ch]) + ",V");
			data.append(";Current=" + vformat.format(Math.abs(A_L2[ch]) * Scalar_A[ch]) + ",A");
			data.append(";Frequency=" + vformat.format(Hz_System[ch] * Scalar_Hz[ch]) + ",Hz");
			data.append(";Peak Demand=" + vformat.format(Math.abs(kW_Demand_L2[ch]) * Scalar_Power[ch]) + ",kW");

			// Phase L3
			data.append("|DEVICEID=" + getId() + "-" + ch + "-3");
			data.append(";TIMESTAMP=" + timestamp);
			data.append(";Active Energy Reading=" + vformat.format((kWh_L3[ch]) * Scalar_Energy[ch]) + ",kWh");
			data.append(";Reactive Energy Reading=" + vformat.format((kVARh_L3[ch]) * Scalar_Energy[ch]) + ",kVARh");
			data.append(";Apparent Energy Reading=" + vformat.format(kVAh_L3[ch] * Scalar_Energy[ch]) + ",kVAh");
			data.append(";Active Energy=" + vformat.format((kWh_L3_diff[ch]) * Scalar_Energy[ch] * 1000.0) + ",Wh");
			data.append(";Reactive Energy=" + vformat.format((kVARh_L3_diff[ch]) * Scalar_Energy[ch] * 1000.0) + ",VARh");
			data.append(";Apparent Energy=" + vformat.format(kVAh_L3_diff[ch] * Scalar_Energy[ch] * 1000.0) + ",VAh");
			data.append(";Active Power=" + vformat.format(Math.abs(kW_L3[ch]) * Scalar_Power[ch]) + ",kW");
			data.append(";Reactive Power=" + vformat.format(Math.abs(kVAR_L3[ch]) * Scalar_Power[ch]) + ",kVAR");
			data.append(";Apparent Power=" + vformat.format(kVA_L3[ch] * Scalar_Power[ch]) + ",kVA");
			data.append(";Power Factor=" + vformat.format(PF_L3[ch] * Scalar_PF[ch]) + ",None");
			data.append(";Voltage=" + vformat.format(V_L3[ch] * Scalar_V[ch]) + ",V");
			data.append(";Current=" + vformat.format(Math.abs(A_L3[ch]) * Scalar_A[ch]) + ",A");
			data.append(";Frequency=" + vformat.format(Hz_System[ch] * Scalar_Hz[ch]) + ",Hz");
			data.append(";Peak Demand=" + vformat.format(Math.abs(kW_Demand_L3[ch]) * Scalar_Power[ch]) + ",kW");
		}
		return data.toString();
	}
	
	public void setL1SimData(int chan, Double max,Double min,Double delta,Double scale){
	    if(max != null){
	    	KW_L1_MAX[chan] = max;
	    }if(min != null){
	    	KW_L1_MIN[chan] = min;
	    }
	    if(KW_L1_MIN[chan] > KW_L1_MAX[chan]){
	    	double tmp = KW_L1_MAX[chan];
	    	KW_L1_MAX[chan] = KW_L1_MIN[chan];
	    	KW_L1_MIN[chan] = tmp;
	    }
	    
	    if(scale != null)
	    {
	    	KW_L1_DELTA_SCALE[chan] = scale;
	    }
	    if(delta != null){
	    	KW_L1_DELTA[chan] = delta;
	    } else {
	    	KW_L1_DELTA[chan] = (KW_L1_MAX[chan] - KW_L1_MIN[chan]) * KW_L1_DELTA_SCALE[chan];
	    }
	    
	    kW_L1[chan] = KW_L1_MIN[chan];

	}
	
	public void setL2SimData(int chan, Double max,Double min,Double delta,Double scale){
	    if(max != null){
	    	KW_L2_MAX[chan] = max;
	    }if(min != null){
	    	KW_L2_MIN[chan] = min;
	    }
	    if(KW_L2_MIN[chan] > KW_L2_MAX[chan]){
	    	double tmp = KW_L2_MAX[chan];
	    	KW_L2_MAX[chan] = KW_L2_MIN[chan];
	    	KW_L2_MIN[chan] = tmp;
	    }
	    
	    if(scale != null)
	    {
	    	KW_L2_DELTA_SCALE[chan] = scale;
	    }
	    if(delta != null){
	    	KW_L2_DELTA[chan] = delta;
	    } else {
	    	KW_L2_DELTA[chan] = (KW_L2_MAX[chan] - KW_L2_MIN[chan]) * KW_L2_DELTA_SCALE[chan];
	    }
	    
	    kW_L2[chan] = KW_L2_MIN[chan];

	}
	
	public void setL3SimData(int chan, Double max,Double min,Double delta,Double scale){
	    if(max != null){
	    	KW_L3_MAX[chan] = max;
	    }if(min != null){
	    	KW_L3_MIN[chan] = min;
	    }
	    if(KW_L3_MIN[chan] > KW_L3_MAX[chan]){
	    	double tmp = KW_L3_MAX[chan];
	    	KW_L3_MAX[chan] = KW_L3_MIN[chan];
	    	KW_L3_MIN[chan] = tmp;
	    }
	    
	    if(scale != null)
	    {
	    	KW_L3_DELTA_SCALE[chan] = scale;
	    }
	    if(delta != null){
	    	KW_L3_DELTA[chan] = delta;
	    } else {
	    	KW_L3_DELTA[chan] = (KW_L3_MAX[chan] - KW_L3_MIN[chan]) * KW_L3_DELTA_SCALE[chan];
	    }
	    
	    kW_L3[chan] = KW_L3_MIN[chan];

	}
    public boolean saveData() {
    	mLogger.info("save data");
    	Properties _props = new Properties();
    	try {
    		for (int i = 0; i < CHANNEL_NUM; i++) {
	    		_props.setProperty("kWh_System_ch" + i,String.valueOf(kWh_System[i]));
	    		_props.setProperty("kVARh_System_ch" + i,String.valueOf(kVARh_System[i]));
	    		_props.setProperty("kVAh_System_ch" + i,String.valueOf(kVAh_System[i]));
	    		
	    		_props.setProperty("kWh_L1_ch" + i,String.valueOf(kWh_L1[i]));
	    		_props.setProperty("kVARh_L1_ch" + i,String.valueOf(kVARh_L1[i]));
	    		_props.setProperty("kVAh_L1_ch" + i,String.valueOf(kVAh_L1[i]));
	    		
	    		_props.setProperty("kWh_L2_ch" + i,String.valueOf(kWh_L2[i]));
	    		_props.setProperty("kVARh_L2_ch" + i,String.valueOf(kVARh_L2[i]));
	    		_props.setProperty("kVAh_L2_ch" + i,String.valueOf(kVAh_L2[i]));
	    		
	    		_props.setProperty("kWh_L3_ch" + i,String.valueOf(kWh_L3[i]));
	    		_props.setProperty("kVARh_L3_ch" + i,String.valueOf(kVARh_L3[i]));
	    		_props.setProperty("kVAh_L3_ch" + i,String.valueOf(kVAh_L3[i]));
    		}
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
			for (int i = 0; i < CHANNEL_NUM; i++) {
				try {
					kWh_System[i] = Double.parseDouble(_props.getProperty("kWh_System_ch" + i));
				} catch (NumberFormatException ex) {
					//mLogger.error("Wrong volume data,use default " );
				}
				try {
					kVARh_System[i] = Double.parseDouble(_props.getProperty("kVARh_System_ch" + i));
				} catch (NumberFormatException ex) {
					//mLogger.error("Wrong volume data,use default " );
				}
				try {
					kVAh_System[i] = Double.parseDouble(_props.getProperty("kVAh_System_ch" + i));
				} catch (NumberFormatException ex) {
					//mLogger.error("Wrong volume data,use default " );
				}
				
				try {
					kWh_L1[i] = Double.parseDouble(_props.getProperty("kWh_L1_ch" + i));
				} catch (NumberFormatException ex) {
					//mLogger.error("Wrong volume data,use default " );
				}
				try {
					kVARh_L1[i] = Double.parseDouble(_props.getProperty("kVARh_L1_ch" + i));
				} catch (NumberFormatException ex) {
					//mLogger.error("Wrong volume data,use default " );
				}
				try {
					kVAh_L1[i] = Double.parseDouble(_props.getProperty("kVAh_L1_ch" + i));
				} catch (NumberFormatException ex) {
					//mLogger.error("Wrong volume data,use default " );
				}
				
				try {
					kWh_L2[i] = Double.parseDouble(_props.getProperty("kWh_L2_ch" + i));
				} catch (NumberFormatException ex) {
					//mLogger.error("Wrong volume data,use default " );
				}
				try {
					kVARh_L2[i] = Double.parseDouble(_props.getProperty("kVARh_L2_ch" + i));
				} catch (NumberFormatException ex) {
					//mLogger.error("Wrong volume data,use default " );
				}
				try {
					kVAh_L2[i] = Double.parseDouble(_props.getProperty("kVAh_L2_ch" + i));
				} catch (NumberFormatException ex) {
					//mLogger.error("Wrong volume data,use default " );
				}
				
				try {
					kWh_L3[i] = Double.parseDouble(_props.getProperty("kWh_L3_ch" + i));
				} catch (NumberFormatException ex) {
					//mLogger.error("Wrong volume data,use default " );
				}
				try {
					kVARh_L3[i] = Double.parseDouble(_props.getProperty("kVARh_L3_ch" + i));
				} catch (NumberFormatException ex) {
					//mLogger.error("Wrong volume data,use default " );
				}
				try {
					kVAh_L3[i] = Double.parseDouble(_props.getProperty("kVAh_L3_ch" + i));
				} catch (NumberFormatException ex) {
					//mLogger.error("Wrong volume data,use default " );
				}
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
