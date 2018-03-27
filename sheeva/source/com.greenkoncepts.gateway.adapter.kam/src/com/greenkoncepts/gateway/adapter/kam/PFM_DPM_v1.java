package com.greenkoncepts.gateway.adapter.kam;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.FuncUtil;

public class PFM_DPM_v1 extends KamDevice {
	public static int MBREG_DATA_START1 = 0x500;
	public static int MBREG_DATA_NUM1 = 0xc6;
	
	public static int MBREG_DATA_START2 = 0x730;
	public static int MBREG_DATA_NUM2 = 0x42;
	
	
	public static short posPhase1Current = 0x0;
	public static short posPhase2Current = 0x6;
	public static short posPhase3Current = 0xc;
	public static short posNeutralCurrent = 0x12;
	public static short posPhaseVoltage12 = 0x2a;
	public static short posPhaseVoltage23 = 0x30;
	public static short posPhaseVoltage31 = 0x36;
	public static short posPhaseVoltage1 = 0x3c;
	public static short posPhaseVoltage2 = 0x42;
	public static short posPhaseVoltage3 = 0x48;
	public static short posFrequency = 0x4e;
	public static short posRealPowerPhase1 = 0x52;
	public static short posRealPowerPhase2 = 0x58;
	public static short posRealPowerPhase3 = 0x5e;
	public static short posReactivePowerPhase1 = 0x64;
	public static short posReactivePowerPhase2 = 0x6a;
	public static short posReactivePowerPhase3 = 0x70;
	public static short posApparentPowerPhase1 = 0x76;
	public static short posApparentPowerPhase2 = 0x7c;
	public static short posApparentPowerPhase3 = 0x82;
	public static short posTotalRealPower = 0x88;
	public static short posTotalReactivePower = 0x8e;
	public static short posTotalApparentPower = 0x94;
	public static short posPowerFactorPhase1 = 0xb8;
	public static short posPowerFactorPhase2 = 0xbc;
	public static short posPowerFactorPhase3 = 0xc0;
	public static short posTotalPowerFactor = 0xc4;
	
	public static short posPositiveMaxRealPower =0x0;
	public static short posNegativeMaxRealPower = 0x6;
	public static short posPositiveRealEnergy =0x1e;
	public static short posNegativeRealEnergy = 0x26;
	public static short posPositiveReactiveEnergy =0x2e;
	public static short posNegativeReactiveEnergy = 0x36;
	public static short posApparentEnergy = 0x3e;
	
	private long phase1Current = 0;
	private long phase2Current = 0;
	private long phase3Current = 0;
	private long neutralCurrent = 0;
	private long phaseVoltage12 = 0;
	private long phaseVoltage23 = 0;
	private long phaseVoltage31 = 0;
	private long phaseVoltage1 = 0;
	private long phaseVoltage2 = 0;
	private long phaseVoltage3 = 0;
	private long frequency = 0;
	private long realPowerPhase1 = 0;
	private long realPowerPhase2 = 0;
	private long realPowerPhase3 = 0;
	private long reactivePowerPhase1 = 0;
	private long reactivePowerPhase2 = 0;
	private long reactivePowerPhase3 = 0;
	private long apparentPowerPhase1 = 0;
	private long apparentPowerPhase2 = 0;
	private long apparentPowerPhase3 = 0;
	private long totalRealPower = 0;
	private long totalReactivePower = 0;
	private long totalApparentPower = 0;
	private long powerFactorPhase1 = 0;
	private long powerFactorPhase2 = 0;
	private long powerFactorPhase3 = 0;
	private long totalPowerFactor = 0;
	

	private long positiveMaxRealPowerReading = 0;
	private long negativeMaxRealPowerReading = 0;
	private long positiveRealEnergyReading =0;
	private long negativeRealEnergyReading  = 0;
	private long positiveReactiveEnergyReading  =0;
	private long negativeReactiveEnergyReading  = 0;
	private long apparentEnergyReading  = 0;

	private double doublePhase1Current = 0;
	private double doublePhase2Current = 0;
	private double doublePhase3Current = 0;
	private double doubleNeutralCurrent = 0;
	private double doublePhaseVoltage12 = 0;
	private double doublePhaseVoltage23 = 0;
	private double doublePhaseVoltage31 = 0;
	private double doublePhaseVoltage1 = 0;
	private double doublePhaseVoltage2 = 0;
	private double doublePhaseVoltage3 = 0;
	private double doubleFrequency = 0;
	private double doubleRealPowerPhase1 = 0;
	private double doubleRealPowerPhase2 = 0;
	private double doubleRealPowerPhase3 = 0;
	private double doubleReactivePowerPhase1 = 0;
	private double doubleReactivePowerPhase2 = 0;
	private double doubleReactivePowerPhase3 = 0;
	private double doubleApparentPowerPhase1 = 0;
	private double doubleApparentPowerPhase2 = 0;
	private double doubleApparentPowerPhase3 = 0;
	private double doubleTotalRealPower = 0;
	private double doubleTotalReactivePower = 0;
	private double doubleTotalApparentPower = 0;
	private double doublePowerFactorPhase1 = 0;
	private double doublePowerFactorPhase2 = 0;
	private double doublePowerFactorPhase3 = 0;
	private double doubleTotalPowerFactor = 0;
	
	private double doubleRealEnergy =0;
	private double doubleReactiveEnergy = 0;
	private double doubleApparentEnergy = 0;
	
	private double doubleRealEnergyReading =0;
	private double doubleReactiveEnergyReading  =0;
	private double doubleApparentEnergyReading  = 0;
	
	private double doublePreRealEnergyReading  =0;
	private double doublePreReactiveEnergyReading  =0;
	private double doublePreApparentEnergyReading  = 0;
	
	private double doublePeakDemand = 0;

	public PFM_DPM_v1(String category, int addr) {
		super(category, addr);
	}

	@Override
	public String getDeviceData() {
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START1, MBREG_DATA_NUM1);
		if (decodingData(0, data, DATA_MODE)) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START2, MBREG_DATA_NUM2);
			if (decodingData(1, data, DATA_MODE)) {
				calculateDecodedData();
			}
		}
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
		if (isRefesh) {
			byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START1, MBREG_DATA_NUM1);
			if(decodingData(0, data, DATA_MODE)) {
				data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START2, MBREG_DATA_NUM2);
				if (decodingData(1, data, DATA_MODE)) {
					calculateDecodedData();
				}
			}
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			item.put("System_Active_Energy_Reading" ,  vformat.format(doubleRealEnergyReading));
			item.put("System_Active_Energy" ,  vformat.format(doubleRealEnergy));
			item.put("System_Active_Power" ,  vformat.format(doubleTotalRealPower));
			item.put("System_Reactive_Energy_Reading" ,  vformat.format(doubleReactiveEnergyReading));
			item.put("System_Reactive_Energy" ,  vformat.format(doubleReactiveEnergy));
			item.put("System_Reactive_Power" ,  vformat.format(doubleTotalReactivePower));
			item.put("System_Apparent_Energy_Reading" ,  vformat.format(doubleApparentEnergyReading));
			item.put("System_Apparent_Energy" ,  vformat.format(doubleApparentEnergy));
			item.put("System_Apparent_Power" ,  vformat.format(doubleTotalApparentPower));
			item.put("System_Power_Factor" ,  vformat.format(doubleTotalPowerFactor));
			item.put("System_Current" ,  vformat.format(doubleNeutralCurrent));
			item.put("System_Voltage" ,  vformat.format(doublePhaseVoltage1));
			item.put("System_Voltage_L1-L2" ,  vformat.format(doublePhaseVoltage12));
			item.put("System_Voltage_L2-L3" ,  vformat.format(doublePhaseVoltage23));
			item.put("System_Voltage_L1-L3" ,  vformat.format(doublePhaseVoltage31));
			item.put("System_Frequency" ,  vformat.format(doubleFrequency));
			item.put("System_Peak_Demand" ,  vformat.format(doublePeakDemand));

			item.put("Phase1_Active_Power" ,  vformat.format(doubleRealPowerPhase1));
			item.put("Phase1_Reactive_Power" ,  vformat.format(doubleReactivePowerPhase1));
			item.put("Phase1_Apparent_Power" ,  vformat.format(doubleApparentPowerPhase1));
			item.put("Phase1_Power_Factor" ,  vformat.format(doublePowerFactorPhase1));
			item.put("Phase1_Current" ,  vformat.format(doublePhase1Current));
			item.put("Phase1_Voltage" ,  vformat.format(doublePhaseVoltage1));

			item.put("Phase2_Active_Power" ,  vformat.format(doubleRealPowerPhase2));
			item.put("Phase2_Reactive_Power" ,  vformat.format(doubleReactivePowerPhase2));
			item.put("Phase2_Apparent_Power" ,  vformat.format(doubleApparentPowerPhase2));
			item.put("Phase2_Power_Factor" ,  vformat.format(doublePowerFactorPhase2));
			item.put("Phase2_Current" ,  vformat.format(doublePhase2Current));
			item.put("Phase2_Voltage" ,  vformat.format(doublePhaseVoltage2));

			item.put("Phase3_Active_Power" ,  vformat.format(doubleRealPowerPhase3));
			item.put("Phase3_Reactive_Power" ,  vformat.format(doubleReactivePowerPhase3));
			item.put("Phase3_Apparent_Power" ,  vformat.format(doubleApparentPowerPhase3));
			item.put("Phase3_Power_Factor" ,  vformat.format(doublePowerFactorPhase3));
			item.put("Phase3_Current" ,  vformat.format(doublePhase3Current));
			item.put("Phase3_Voltage" ,  vformat.format(doublePhaseVoltage3));

			real_time_data.add(item);

		}
		return real_time_data;
	}

	private boolean decodingData(int idx, byte[] data, int mode) {
		if (data == null) {
			errorCount++;
			return false;
		}

		errorCount = 0;
		if (mode == DATA_MODE) {
			if (idx == 0) {
				phase1Current = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posPhase1Current);
				phase2Current = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posPhase2Current);
				phase3Current = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posPhase3Current);
				neutralCurrent = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posNeutralCurrent);
				phaseVoltage12 = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posPhaseVoltage12);
				phaseVoltage23 = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posPhaseVoltage23);
				phaseVoltage31 = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posPhaseVoltage31);
				phaseVoltage1 = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posPhaseVoltage1);
				phaseVoltage2 = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posPhaseVoltage2);
				phaseVoltage3 = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posPhaseVoltage3);
				frequency = FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posFrequency);
				realPowerPhase1 = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posRealPowerPhase1);
				realPowerPhase2 = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posRealPowerPhase2);
				realPowerPhase3 = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posRealPowerPhase3);
				reactivePowerPhase1 = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posReactivePowerPhase1);
				reactivePowerPhase2 = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posReactivePowerPhase2);
				reactivePowerPhase3 = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posReactivePowerPhase3);
				apparentPowerPhase1 = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posApparentPowerPhase1);
				apparentPowerPhase2 = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posApparentPowerPhase2);
				apparentPowerPhase3 = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posApparentPowerPhase3);
				totalRealPower = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posTotalRealPower);
				totalReactivePower = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posTotalReactivePower);
				totalApparentPower = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posTotalApparentPower);
				powerFactorPhase1 = FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posPowerFactorPhase1);
				powerFactorPhase2 = FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posPowerFactorPhase2);
				powerFactorPhase3 = FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posPowerFactorPhase3);
				totalPowerFactor = FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posTotalPowerFactor);
			} else if (idx == 1) {
				positiveMaxRealPowerReading = apparentPowerPhase3 = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posPositiveMaxRealPower);
				negativeMaxRealPowerReading = apparentPowerPhase3 = FuncUtil.RegisterBigEndian.registersToInt6(data, OFFSET_DATA + 2*posNegativeMaxRealPower);
				positiveRealEnergyReading =FuncUtil.RegisterBigEndian.registersToLong(data, OFFSET_DATA + 2*posPositiveRealEnergy);
				negativeRealEnergyReading  = FuncUtil.RegisterBigEndian.registersToLong(data, OFFSET_DATA + 2*posNegativeRealEnergy);
				positiveReactiveEnergyReading  =FuncUtil.RegisterBigEndian.registersToLong(data, OFFSET_DATA + 2*posPositiveReactiveEnergy);
				negativeReactiveEnergyReading  = FuncUtil.RegisterBigEndian.registersToLong(data, OFFSET_DATA + 2*posNegativeReactiveEnergy);
				apparentEnergyReading  = FuncUtil.RegisterBigEndian.registersToLong(data, OFFSET_DATA + 2*posApparentEnergy);
			}
			return true;
		}
		if (mode == CONFIG_MODE) {

			return true;
		}

		return true;
	}
	
	private void calculateDecodedData() {
		doublePhase1Current = phase1Current * (double)0.0001;
		doublePhase2Current = phase2Current * (double)0.0001;
		doublePhase3Current = phase3Current * (double)0.0001;
		doubleNeutralCurrent = neutralCurrent * (double)0.0001;
		doublePhaseVoltage12 = phaseVoltage12 * (double)0.0001;
		doublePhaseVoltage23 = phaseVoltage23 * (double)0.0001;
		doublePhaseVoltage31 = phaseVoltage31 * (double)0.0001;
		doublePhaseVoltage1 = phaseVoltage1 * (double)0.0001;
		doublePhaseVoltage2 = phaseVoltage2 * (double)0.0001;
		doublePhaseVoltage3 = phaseVoltage3 * (double)0.0001;
		doubleFrequency = frequency * (double)0.0001;
		doubleRealPowerPhase1 = realPowerPhase1 * (double)0.0001;
		doubleRealPowerPhase2 = realPowerPhase2 * (double)0.0001;
		doubleRealPowerPhase3 = realPowerPhase3 * (double)0.0001;
		doubleReactivePowerPhase1 = reactivePowerPhase1 * (double)0.0001;
		doubleReactivePowerPhase2 = reactivePowerPhase2 * (double)0.0001;
		doubleReactivePowerPhase3 = reactivePowerPhase3 * (double)0.0001;
		doubleApparentPowerPhase1 = apparentPowerPhase1 * (double)0.0001;
		doubleApparentPowerPhase2 = apparentPowerPhase2 * (double)0.0001;
		doubleApparentPowerPhase3 = apparentPowerPhase3 * (double)0.0001;
		doubleTotalRealPower = totalRealPower * (double)0.0001;
		doubleTotalReactivePower = totalReactivePower * (double)0.0001;
		doubleTotalApparentPower = totalApparentPower * (double)0.0001;
		doublePowerFactorPhase1 = powerFactorPhase1 * (double)0.0001;
		doublePowerFactorPhase2 = powerFactorPhase2 * (double)0.0001;
		doublePowerFactorPhase3 = powerFactorPhase3 * (double)0.0001;
		doubleTotalPowerFactor = totalPowerFactor * (double)0.0001;
		
		doublePeakDemand = positiveMaxRealPowerReading + negativeMaxRealPowerReading;
		doubleRealEnergyReading = (positiveRealEnergyReading *(double)0.0000001) + (Math.abs(negativeRealEnergyReading) * (double) 0.0000001);
		doubleReactiveEnergyReading  = (positiveReactiveEnergyReading *(double)0.0000001) + (Math.abs(negativeReactiveEnergyReading) * (double) 0.0000001);
		doubleApparentEnergyReading  = apparentEnergyReading * (double)0.000001;
		
		if ((doublePreRealEnergyReading == 0) || (doublePreRealEnergyReading > doubleRealEnergyReading)) {
			doubleRealEnergy  = 0;
		} else {
			doubleRealEnergy = (doubleRealEnergyReading - doublePreRealEnergyReading) * 1000;
		}
		doublePreRealEnergyReading = doubleRealEnergyReading;
		
		if ((doublePreReactiveEnergyReading == 0) || (doublePreReactiveEnergyReading > doubleReactiveEnergyReading)) {
			doubleReactiveEnergy  = 0;
		} else {
			doubleReactiveEnergy = (doubleReactiveEnergyReading - doublePreReactiveEnergyReading) * 1000;
		}
		doublePreReactiveEnergyReading = doubleReactiveEnergyReading;
		
		
		if ((doublePreApparentEnergyReading == 0) || (doublePreApparentEnergyReading > doubleApparentEnergyReading)) {
			doubleApparentEnergy  = 0;
		} else {
			doubleApparentEnergy = (doubleApparentEnergyReading - doublePreApparentEnergyReading) * 1000;
		}
		doublePreApparentEnergyReading = doubleApparentEnergyReading;
		
		mLogger.debug("=====READING DATA FROM DEVICE ID " + modbusid);
		mLogger.debug("=====phase1Current: " + phase1Current);
		mLogger.debug("=====phase2Current: " + phase2Current);
		mLogger.debug("=====phase3Current: " + phase3Current);
		mLogger.debug("=====neutralCurrent: " + neutralCurrent);
		mLogger.debug("=====phaseVoltage12: " + phaseVoltage12);
		mLogger.debug("=====phaseVoltage23: " + phaseVoltage23);
		mLogger.debug("=====phaseVoltage31: " + phaseVoltage31);
		mLogger.debug("=====phaseVoltage1: " + phaseVoltage1);
		mLogger.debug("=====phaseVoltage2: " + phaseVoltage2);
		mLogger.debug("=====phaseVoltage3: " + phaseVoltage3);
		mLogger.debug("=====frequency: " + frequency);
		mLogger.debug("=====realPowerPhase1: " + realPowerPhase1);
		mLogger.debug("=====realPowerPhase2: " + realPowerPhase2);
		mLogger.debug("=====realPowerPhase3: " + realPowerPhase3);
		mLogger.debug("=====reactivePowerPhase1: " + reactivePowerPhase1);
		mLogger.debug("=====reactivePowerPhase2: " + reactivePowerPhase2);
		mLogger.debug("=====reactivePowerPhase3: " + reactivePowerPhase3);
		mLogger.debug("=====apparentPowerPhase1: " + apparentPowerPhase1);
		mLogger.debug("=====apparentPowerPhase2: " + apparentPowerPhase2);
		mLogger.debug("=====apparentPowerPhase3: " + apparentPowerPhase3);
		mLogger.debug("=====totalRealPower: " + totalRealPower);
		mLogger.debug("=====totalReactivePower: " + totalReactivePower);
		mLogger.debug("=====totalApparentPower: " + totalApparentPower);
		mLogger.debug("=====powerFactorPhase1: " + powerFactorPhase1);
		mLogger.debug("=====powerFactorPhase2: " + powerFactorPhase2);
		mLogger.debug("=====powerFactorPhase3: " + powerFactorPhase3);
		mLogger.debug("=====totalPowerFactor: " + totalPowerFactor);
			

		mLogger.debug("=====positiveMaxRealPowerReading: " + positiveMaxRealPowerReading);
		mLogger.debug("=====negativeMaxRealPowerReading: " + negativeMaxRealPowerReading);
		mLogger.debug("=====positiveRealEnergyReading: " + positiveRealEnergyReading);
		mLogger.debug("=====negativeRealEnergyReading: " + negativeRealEnergyReading);
		mLogger.debug("=====positiveReactiveEnergyReading: " + positiveReactiveEnergyReading);
		mLogger.debug("=====negativeReactiveEnergyReading: " + negativeReactiveEnergyReading);
		mLogger.debug("=====apparentEnergyReading: " + apparentEnergyReading);
		
	}
	
	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		data.append("|DEVICEID=" + getId() + "-0-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + vformat.format(doubleRealEnergyReading) + ",kWh");
		data.append(";Active Energy=" + vformat.format(doubleRealEnergy) + ",Wh");
		data.append(";Active Power=" + vformat.format(doubleTotalRealPower) + ",kW");
		data.append(";Reactive Energy Reading=" + vformat.format(doubleReactiveEnergyReading) + ",kVARh");
		data.append(";Reactive Energy=" + vformat.format(doubleReactiveEnergy) + ",VARh");
		data.append(";Reactive Power=" + vformat.format(doubleTotalReactivePower) + ",kVAR");
		data.append(";Apparent Energy Reading=" + vformat.format(doubleApparentEnergyReading) + ",kVAh");
		data.append(";Apparent Energy=" + vformat.format(doubleApparentEnergy) + ",VAh");
		data.append(";Apparent Power=" + vformat.format(doubleTotalApparentPower) + ",kVA");
		data.append(";Power Factor=" + vformat.format(doubleTotalPowerFactor) + ",None");
		data.append(";Current=" + vformat.format(doubleNeutralCurrent) + ",A");
		data.append(";Voltage=" + vformat.format(doublePhaseVoltage1) + ",V");
		data.append(";Voltage L1-L2=" + vformat.format(doublePhaseVoltage12) + ",V");
		data.append(";Voltage L2-L3=" + vformat.format(doublePhaseVoltage23) + ",V");
		data.append(";Voltage L1-L3=" + vformat.format(doublePhaseVoltage31) + ",V");
		data.append(";Frequency=" + vformat.format(doubleFrequency) + ",Hz");
		data.append(";Peak Demand=" + vformat.format(doublePeakDemand) + ",kW");

		data.append("|DEVICEID=" + getId() + "-1-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(doubleRealPowerPhase1) + ",kW");
		data.append(";Reactive Power=" + vformat.format(doubleReactivePowerPhase1) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(doubleApparentPowerPhase1) + ",kVA");
		data.append(";Power Factor=" + vformat.format(doublePowerFactorPhase1) + ",None");
		data.append(";Current=" + vformat.format(doublePhase1Current) + ",A");
		data.append(";Voltage=" + vformat.format(doublePhaseVoltage1) + ",V");

		data.append("|DEVICEID=" + getId() + "-2-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(doubleRealPowerPhase2) + ",kW");
		data.append(";Reactive Power=" + vformat.format(doubleReactivePowerPhase2) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(doubleApparentPowerPhase2) + ",kVA");
		data.append(";Power Factor=" + vformat.format(doublePowerFactorPhase2) + ",None");
		data.append(";Current=" + vformat.format(doublePhase2Current) + ",A");
		data.append(";Voltage=" + vformat.format(doublePhaseVoltage2) + ",V");

		data.append("|DEVICEID=" + getId() + "-3-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(doubleRealPowerPhase3) + ",kW");
		data.append(";Reactive Power=" + vformat.format(doubleReactivePowerPhase3) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(doubleApparentPowerPhase3) + ",kVA");
		data.append(";Power Factor=" + vformat.format(doublePowerFactorPhase3) + ",None");
		data.append(";Current=" + vformat.format(doublePhase3Current) + ",A");
		data.append(";Voltage=" + vformat.format(doublePhaseVoltage3) + ",V");

		return data.toString();
	}
}
