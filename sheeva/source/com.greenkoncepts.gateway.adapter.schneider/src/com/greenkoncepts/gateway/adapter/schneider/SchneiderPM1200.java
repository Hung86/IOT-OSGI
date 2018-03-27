package com.greenkoncepts.gateway.adapter.schneider;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.FuncUtil;

public class SchneiderPM1200 extends SchneiderDevice {
	static public final int MBREG_DATA_START1 = 3900;// 3901-3980
	static public final int MBREG_DATA_NUM1 = 82;

	static public final int MBREG_DATA_START2 = 3880;// 3881-3888
	static public final int MBREG_DATA_NUM2 = 10;
	
	
	static public int posFrequency = OFFSET_DATA + 2 + 2 * 14;// 3915
	static public int posCurrentA = OFFSET_DATA + 2 + 2 * 28;// 3929
	static public int posCurrentB = OFFSET_DATA + 2 + 2 * 42;// 3943
	static public int posCurrentC = OFFSET_DATA + 2 + 2 * 56;// 3957
	static public int posCurrentAVG = OFFSET_DATA + 2 + 2 * 12;// 3913

	static public int posVoltageAB = OFFSET_DATA + 2 + 2 * 24;// 3925
	static public int posVoltageBC = OFFSET_DATA + 2 + 2 * 38;// 3939
	static public int posVoltageCA = OFFSET_DATA + 2 + 2 * 52;// 3953

	static public int posVoltageAN = OFFSET_DATA + 2 + 2 * 26;// 3927
	static public int posVoltageBN = OFFSET_DATA + 2 + 2 * 40;// 3941
	static public int posVoltageCN = OFFSET_DATA + 2 + 2 * 54;// 3955
	static public int posVoltageAVG = OFFSET_DATA + 2 + 2 * 10;// 3911

	static public int posActivePowerA = OFFSET_DATA + 2 + 2 * 18;// 3919
	static public int posActivePowerB = OFFSET_DATA + 2 + 2 * 32;// 3933
	static public int posActivePowerC = OFFSET_DATA + 2 + 2 * 46;// 3947
	static public int posActivePowerTotal = OFFSET_DATA + 2 + 2 * 2;// 3903

	static public int posReactivePowerA = OFFSET_DATA + 2 + 2 * 20;// 3921
	static public int posReactivePowerB = OFFSET_DATA + 2 + 2 * 34;// 3935
	static public int posReactivePowerC = OFFSET_DATA + 2 + 2 * 48;// 3949
	static public int posReactivePowerTotal = OFFSET_DATA + 2 + 2 * 4;// 3905

	static public int posApparentPowerA = OFFSET_DATA + 2 + 2 * 16;// 3917
	static public int posApparentPowerB = OFFSET_DATA + 2 + 2 * 30;// 3931
	static public int posApparentPowerC = OFFSET_DATA + 2 + 2 * 44;// 3945
	static public int posApparentPowerTotal = OFFSET_DATA + 2 + 2 * 0;// 3901

	static public int posForwardActiveEnergyTotal = OFFSET_DATA + 2 + 2 * 60;// 3961
	static public int posForwardReactiveInductiveEnergyTotal = OFFSET_DATA + 2 + 2 * 62;// 3963
	static public int posForwardReactiveCapacitiveEnergyTotal = OFFSET_DATA + 2 + 2 * 64;// 3965
	static public int posForwardApparentEnergyTotal = OFFSET_DATA + 2 + 2 * 58;// 3959
	
	static public int posReverseActiveEnergyTotal = OFFSET_DATA + 2 + 2 * 68;// 3969
	static public int posReserveReactiveInductiveEnergyTotal = OFFSET_DATA + 2 + 2 * 70;// 3971
	static public int posReverseReactiveCapacitiveEnergyTotal = OFFSET_DATA + 2 + 2 * 72;// 3973
	static public int posReverseApparentEnergyTotal = OFFSET_DATA + 2 + 2 * 66;// 3967

	static public int posPowerFactorA = OFFSET_DATA + 2 + 2 * 22;// 3923
	static public int posPowerFactorB = OFFSET_DATA + 2 + 2 * 36;// 3937
	static public int posPowerFactorC = OFFSET_DATA + 2 + 2 * 50;// 3951
	static public int posPowerFactorTotal = OFFSET_DATA + 2 + 2 * 6;// 3907
	
	
	static public int posLoadPhaseA = OFFSET_DATA + 2 + 2 * 2;// 3883
	static public int posLoadPhaseB = OFFSET_DATA + 2 + 2 * 4;// 3885
	static public int posLoadPhaseC = OFFSET_DATA + 2 + 2 * 6;// 3887
	static public int posLoadPhaseTotal = OFFSET_DATA + 2 + 2 * 0;// 3881
	
	static public int posMaximumDemand = OFFSET_DATA + 2 + 2 * 78;// 3979
	
	
	private double doubleFrequency = 0;// 3915
	private double doubleCurrentA = 0;// 3929
	private double doubleCurrentB = 0;// 3943
	private double doubleCurrentC = 0;// 3957
	private double doubleCurrentAVG = 0;// 3913

	private double doubleVoltageAB = 0;// 3925
	private double doubleVoltageBC = 0;// 3939
	private double doubleVoltageCA = 0;// 3953

	private double doubleVoltageAN = 0;// 3927
	private double doubleVoltageBN = 0;// 3941
	private double doubleVoltageCN = 0;// 3955
	private double doubleVoltageAVG = 0;// 3911

	private double doubleActivePowerA = 0;// 3919
	private double doubleActivePowerB = 0;// 3933
	private double doubleActivePowerC = 0;// 3947
	private double doubleActivePowerTotal = 0;// 3903

	private double doubleReactivePowerA = 0;// 3921
	private double doubleReactivePowerB = 0;// 3935
	private double doubleReactivePowerC = 0;// 3949
	private double doubleReactivePowerTotal = 0;// 3905

	private double doubleApparentPowerA = 0;// 3917
	private double doubleApparentPowerB = 0;// 3931
	private double doubleApparentPowerC = 0;// 3945
	private double doubleApparentPowerTotal = 0;// 3901

	private double doubleForwardActiveEnergyTotal = 0;// 3961
	private double doubleForwardReactiveInductiveEnergyTotal = 0;// 3963
	private double doubleForwardReactiveCapacitiveEnergyTotal = 0;// 3965
	private double doubleForwardApparentEnergyTotal = 0;// 3959
		
	private double doubleReverseActiveEnergyTotal = 0;// 3969
	private double doubleReserveReactiveInductiveEnergyTotal = 0;// 3971
	private double doubleReverseReactiveCapacitiveEnergyTotal = 0;// 3973
	private double doubleReverseApparentEnergyTotal = 0;// 3967

	private double doublePowerFactorA = 0;// 3923
	private double doublePowerFactorB = 0;// 3937
	private double doublePowerFactorC = 0;// 3951
	private double doublePowerFactorTotal = 0;// 3907
		
		
	private double doubleLoadPhaseA = 0;// 3883
	private double doubleLoadPhaseB = 0;// 3885
	private double doubleLoadPhaseC = 0;// 3887
	private double doubleLoadPhaseTotal = 0;// 3881
		
	private double doubleMaximumDemand = 0;// 3979
	
	private double doublePreActiveEnergyTotal = 0;
	private double doublePreReactiveEnergyTotal = 0;
	private double doublePreApparentEnergyTotal = 0;
	
	private double doubleActiveEnergyTotal = 0;
	private double doubleReactiveEnergyTotal = 0;
	private double doubleApparentEnergyTotal = 0;
	
	private double doubleActiveEnergyTotalConsume = 0;
	private double doubleReactiveEnergyTotalConsume = 0;
	private double doubleApparentEnergyTotalConsume = 0;
	
	
	public SchneiderPM1200(int addr, String category) {
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
			if (decodingData(0, data, DATA_MODE)) {
				data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START2, MBREG_DATA_NUM2);
				decodingData(1, data, DATA_MODE);
			}
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			item.put("act_energy", vformat.format(doubleActiveEnergyTotal));
			item.put("rea_energy", vformat.format(doubleReactiveEnergyTotal));
			item.put("app_energy", vformat.format(doubleApparentEnergyTotal));

			item.put("act_power", vformat.format(doubleActivePowerTotal));
			item.put("act_power_l1", vformat.format(doubleActivePowerA));
			item.put("act_power_l2", vformat.format(doubleActivePowerB));
			item.put("act_power_l3", vformat.format(doubleActivePowerC));

			item.put("rea_power", vformat.format(doubleReactivePowerTotal));
			item.put("rea_power_l1", vformat.format(doubleReactivePowerA));
			item.put("rea_power_l2", vformat.format(doubleReactivePowerB));
			item.put("rea_power_l3", vformat.format(doubleReactivePowerC));

			item.put("app_power", vformat.format(doubleApparentPowerTotal));
			item.put("app_power_l1", vformat.format(doubleApparentPowerA));
			item.put("app_power_l2", vformat.format(doubleApparentPowerB));
			item.put("app_power_l3", vformat.format(doubleApparentPowerC));

			item.put("voltage", vformat.format(doubleVoltageAVG));
			item.put("voltage_l1", vformat.format(doubleVoltageAN));
			item.put("voltage_l2", vformat.format(doubleVoltageBN));
			item.put("voltage_l3", vformat.format(doubleVoltageCN));

			item.put("current", vformat.format(doubleCurrentAVG));
			item.put("current_l1", vformat.format(doubleCurrentA));
			item.put("current_l2", vformat.format(doubleCurrentB));
			item.put("current_l3", vformat.format(doubleCurrentC));

			item.put("v_l1_l2", vformat.format(doubleVoltageAB));
			item.put("v_l2_l3", vformat.format(doubleVoltageBC));
			item.put("v_l3_l1", vformat.format(doubleVoltageCA));

			item.put("pow_factor", vformat.format(doublePowerFactorTotal));
			item.put("pow_factor_l1", vformat.format(doublePowerFactorA));
			item.put("pow_factor_l2", vformat.format(doublePowerFactorB));
			item.put("pow_factor_l3", vformat.format(doublePowerFactorC));
			item.put("frequency", vformat.format(doubleFrequency));
			
			item.put("load_phase", vformat.format(doubleLoadPhaseTotal));
			item.put("load_phase_l1", vformat.format(doubleLoadPhaseA));
			item.put("load_phase_l2", vformat.format(doubleLoadPhaseB));
			item.put("load_phase_l3", vformat.format(doubleLoadPhaseC));
			
			item.put("peak_demand", vformat.format(doubleMaximumDemand));
			
			
			
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
				doubleFrequency = FuncUtil.registersToIEEE754FloatHighFirst(data, posFrequency);

				doubleCurrentA = FuncUtil.registersToIEEE754FloatHighFirst(data, posCurrentA);
				doubleCurrentB = FuncUtil.registersToIEEE754FloatHighFirst(data, posCurrentB);
				doubleCurrentC = FuncUtil.registersToIEEE754FloatHighFirst(data, posCurrentC);
				doubleCurrentAVG = FuncUtil.registersToIEEE754FloatHighFirst(data, posCurrentAVG);

				doubleVoltageAB = FuncUtil.registersToIEEE754FloatHighFirst(data, posVoltageAB);
				doubleVoltageBC = FuncUtil.registersToIEEE754FloatHighFirst(data, posVoltageBC);
				doubleVoltageCA = FuncUtil.registersToIEEE754FloatHighFirst(data, posVoltageCA);

				doubleVoltageAN = FuncUtil.registersToIEEE754FloatHighFirst(data, posVoltageAN);
				doubleVoltageBN = FuncUtil.registersToIEEE754FloatHighFirst(data, posVoltageBN);
				doubleVoltageCN = FuncUtil.registersToIEEE754FloatHighFirst(data, posVoltageCN);
				doubleVoltageAVG = FuncUtil.registersToIEEE754FloatHighFirst(data, posVoltageAVG);

				doubleActivePowerA = FuncUtil.registersToIEEE754FloatHighFirst(data, posActivePowerA);
				doubleActivePowerB = FuncUtil.registersToIEEE754FloatHighFirst(data, posActivePowerB);
				doubleActivePowerC = FuncUtil.registersToIEEE754FloatHighFirst(data, posActivePowerC);
				doubleActivePowerTotal = FuncUtil.registersToIEEE754FloatHighFirst(data, posActivePowerTotal);

				doubleApparentPowerA = FuncUtil.registersToIEEE754FloatHighFirst(data, posApparentPowerA);
				doubleApparentPowerB = FuncUtil.registersToIEEE754FloatHighFirst(data, posApparentPowerB);
				doubleApparentPowerC = FuncUtil.registersToIEEE754FloatHighFirst(data, posApparentPowerC);
				doubleApparentPowerTotal = FuncUtil.registersToIEEE754FloatHighFirst(data, posApparentPowerTotal);

				doubleReactivePowerA = FuncUtil.registersToIEEE754FloatHighFirst(data, posReactivePowerA);
				doubleReactivePowerB = FuncUtil.registersToIEEE754FloatHighFirst(data, posReactivePowerB);
				doubleReactivePowerC = FuncUtil.registersToIEEE754FloatHighFirst(data, posReactivePowerC);
				doubleReactivePowerTotal = FuncUtil.registersToIEEE754FloatHighFirst(data, posReactivePowerTotal);

				doublePowerFactorA = FuncUtil.registersToIEEE754FloatHighFirst(data, posPowerFactorA);
				doublePowerFactorB = FuncUtil.registersToIEEE754FloatHighFirst(data, posPowerFactorB);
				doublePowerFactorC = FuncUtil.registersToIEEE754FloatHighFirst(data, posPowerFactorC);
				doublePowerFactorTotal = FuncUtil.registersToIEEE754FloatHighFirst(data, posPowerFactorTotal);
				
				doubleForwardActiveEnergyTotal = Math.abs(FuncUtil.registersToIEEE754FloatHighFirst(data, posForwardActiveEnergyTotal));
				doubleForwardReactiveInductiveEnergyTotal = Math.abs(FuncUtil.registersToIEEE754FloatHighFirst(data, posForwardReactiveInductiveEnergyTotal));
				doubleForwardReactiveCapacitiveEnergyTotal = Math.abs(FuncUtil.registersToIEEE754FloatHighFirst(data, posForwardReactiveCapacitiveEnergyTotal));
				doubleForwardApparentEnergyTotal = Math.abs(FuncUtil.registersToIEEE754FloatHighFirst(data, posForwardApparentEnergyTotal));
					
				doubleReverseActiveEnergyTotal = Math.abs(FuncUtil.registersToIEEE754FloatHighFirst(data, posReverseActiveEnergyTotal));
				doubleReserveReactiveInductiveEnergyTotal = Math.abs(FuncUtil.registersToIEEE754FloatHighFirst(data, posReserveReactiveInductiveEnergyTotal));
				doubleReverseReactiveCapacitiveEnergyTotal = Math.abs(FuncUtil.registersToIEEE754FloatHighFirst(data, posReverseReactiveCapacitiveEnergyTotal));
				doubleReverseApparentEnergyTotal = Math.abs(FuncUtil.registersToIEEE754FloatHighFirst(data, posReverseApparentEnergyTotal));
				
				doubleMaximumDemand = FuncUtil.registersToIEEE754FloatHighFirst(data, posMaximumDemand);
				
			} else if (idx == 1) {
				doubleLoadPhaseA = FuncUtil.registersToIEEE754FloatHighFirst(data, posLoadPhaseA);
				doubleLoadPhaseB = FuncUtil.registersToIEEE754FloatHighFirst(data, posLoadPhaseB);
				doubleLoadPhaseC = FuncUtil.registersToIEEE754FloatHighFirst(data, posLoadPhaseC);
				doubleLoadPhaseTotal = FuncUtil.registersToIEEE754FloatHighFirst(data, posLoadPhaseTotal);
			}
			return true;

		}

		if (mode == CONFIG_MODE) {
		}

		return false;
	}

	private void calculateDecodedData() {
		
		doubleActiveEnergyTotal = (doubleForwardActiveEnergyTotal/1000 + doubleReverseActiveEnergyTotal/1000);
		doubleReactiveEnergyTotal = doubleForwardReactiveCapacitiveEnergyTotal/1000 + doubleForwardReactiveInductiveEnergyTotal/1000 + doubleReserveReactiveInductiveEnergyTotal/1000 + doubleReverseReactiveCapacitiveEnergyTotal/1000;
		doubleApparentEnergyTotal = doubleForwardApparentEnergyTotal/1000 + doubleReverseApparentEnergyTotal/1000 ;
		
		doubleActivePowerA = doubleActivePowerA/1000;
		doubleActivePowerB = doubleActivePowerB/1000;
		doubleActivePowerC = doubleActivePowerC/1000;
		doubleActivePowerTotal = doubleActivePowerTotal/1000;

		doubleReactivePowerA = doubleReactivePowerA/1000;
		doubleReactivePowerB = doubleReactivePowerB/1000;
		doubleReactivePowerC = doubleReactivePowerC/1000;
		doubleReactivePowerTotal = doubleReactivePowerTotal/1000;

		doubleApparentPowerA = doubleApparentPowerA/1000;
		doubleApparentPowerB = doubleApparentPowerB/1000;
		doubleApparentPowerC = doubleApparentPowerC/1000;
		doubleApparentPowerTotal = doubleApparentPowerTotal/1000;
		
		doubleMaximumDemand = doubleMaximumDemand/1000;
				
		if (doublePreActiveEnergyTotal == 0) {
			doubleActiveEnergyTotalConsume = 0;
		} else if (doubleActiveEnergyTotal >= doubleActiveEnergyTotalConsume) {
			doubleActiveEnergyTotalConsume = (doubleActiveEnergyTotal - doublePreActiveEnergyTotal) * 1000;
		} else {
			doubleActiveEnergyTotalConsume = 0;
		}
		doublePreActiveEnergyTotal = doubleActiveEnergyTotal;

		if (doublePreApparentEnergyTotal == 0) {
			doubleApparentEnergyTotalConsume = 0;
		} else if (doubleApparentEnergyTotal >= doublePreApparentEnergyTotal) {
			doubleApparentEnergyTotalConsume = (doubleApparentEnergyTotal - doublePreApparentEnergyTotal) * 1000;
		} else {
			doubleApparentEnergyTotalConsume = 0;
		}
		doublePreApparentEnergyTotal = doubleApparentEnergyTotal;

		if (doublePreReactiveEnergyTotal == 0) {
			doubleReactiveEnergyTotalConsume = 0;
		} else if (doubleReactiveEnergyTotal >= doublePreReactiveEnergyTotal) {
			doubleReactiveEnergyTotalConsume = (doubleReactiveEnergyTotal - doublePreReactiveEnergyTotal) * 1000;
		} else {
			doubleReactiveEnergyTotalConsume = 0;
		}
		doublePreReactiveEnergyTotal = doubleReactiveEnergyTotal;

		infoDebug();
	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		data.append("|DEVICEID=" + getId() + "-0-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + vformat.format(doubleActiveEnergyTotal) + ",kWh");
		data.append(";Active Energy=" + vformat.format(doubleActiveEnergyTotalConsume) + ",Wh");
		data.append(";Apparent Energy Reading=" + vformat.format(doubleApparentEnergyTotal) + ",kVAh");
		data.append(";Apparent Energy=" + vformat.format(doubleApparentEnergyTotalConsume) + ",VAh");
		data.append(";Reactive Energy Reading=" + vformat.format(doubleReactiveEnergyTotal) + ",kVARh");
		data.append(";Reactive Energy=" + vformat.format(doubleReactiveEnergyTotalConsume) + ",VARh");
		data.append(";Active Power=" + vformat.format(doubleActivePowerTotal) + ",kW");
		data.append(";Reactive Power=" + vformat.format(doubleReactivePowerTotal) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(doubleApparentPowerTotal) + ",kVA");
		data.append(";Power Factor=" + vformat.format(doublePowerFactorTotal) + ",None");
		data.append(";Current=" + vformat.format(doubleCurrentAVG) + ",A");
		data.append(";Voltage=" + vformat.format(doubleVoltageAVG) + ",V");
		data.append(";Voltage L1-L2=" + vformat.format(doubleVoltageAB) + ",V");
		data.append(";Voltage L2-L3=" + vformat.format(doubleVoltageBC) + ",V");
		data.append(";Voltage L3-L1=" + vformat.format(doubleVoltageCA) + ",V");
		data.append(";Frequency=" + vformat.format(doubleFrequency) + ",Hz");
		data.append(";Peak Demand=" + vformat.format(Math.abs(doubleMaximumDemand))+ ",kW");
		data.append(";Load Phase=" + vformat.format(doubleLoadPhaseTotal) + ",%");

		data.append("|DEVICEID=" + getId() + "-1-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(doubleActivePowerA) + ",kW");
		data.append(";Reactive Power=" + vformat.format(doubleReactivePowerA) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(doubleApparentPowerA) + ",kVA");
		data.append(";Power Factor=" + vformat.format(doublePowerFactorA) + ",None");
		data.append(";Current=" + vformat.format(doubleCurrentA) + ",A");
		data.append(";Voltage=" + vformat.format(doubleVoltageAN) + ",V");
		data.append(";Load Phase=" + vformat.format(doubleLoadPhaseA) + ",%");

		data.append("|DEVICEID=" + getId() + "-2-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(doubleActivePowerB) + ",kW");
		data.append(";Reactive Power=" + vformat.format(doubleReactivePowerB) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(doubleApparentPowerB) + ",kVA");
		data.append(";Power Factor=" + vformat.format(doublePowerFactorB) + ",None");
		data.append(";Current=" + vformat.format(doubleCurrentB) + ",A");
		data.append(";Voltage=" + vformat.format(doubleVoltageBN) + ",V");
		data.append(";Load Phase=" + vformat.format(doubleLoadPhaseB) + ",%");

		data.append("|DEVICEID=" + getId() + "-3-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(doubleActivePowerC) + ",kW");
		data.append(";Reactive Power=" + vformat.format(doubleReactivePowerC) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(doubleApparentPowerC) + ",kVA");
		data.append(";Power Factor=" + vformat.format(doublePowerFactorC) + ",None");
		data.append(";Current=" + vformat.format(doubleCurrentC) + ",A");
		data.append(";Voltage=" + vformat.format(doubleVoltageCN) + ",V");
		data.append(";Load Phase=" + vformat.format(doubleLoadPhaseC) + ",%");
		return data.toString();
	}

	private void infoDebug() {
		mLogger.debug("=== Device Address = " + modbusid);
		mLogger.debug("====doubleForwardActiveEnergyTotal = " + doubleForwardActiveEnergyTotal);
		mLogger.debug("====doubleForwardReactiveInductiveEnergyTotal = " + doubleForwardReactiveInductiveEnergyTotal);
		mLogger.debug("====doubleForwardReactiveCapacitiveEnergyTotal = " + doubleForwardReactiveCapacitiveEnergyTotal);
		mLogger.debug("====doubleForwardApparentEnergyTotal = " + doubleForwardApparentEnergyTotal);
		
		mLogger.debug("====doubleReverseActiveEnergyTotal = " + doubleReverseActiveEnergyTotal);
		mLogger.debug("====doubleReverseReactiveInductiveEnergyTotal = " + doubleReserveReactiveInductiveEnergyTotal);
		mLogger.debug("====doubleReverseReactiveCapacitiveEnergyTotal = " + doubleReverseReactiveCapacitiveEnergyTotal);
		mLogger.debug("====doubleReverseApparentEnergyTotal = " + doubleReverseApparentEnergyTotal);
		
		mLogger.debug("====doublePowerFactorA = " + doublePowerFactorA);
		mLogger.debug("====doublePowerFactorB = " + doublePowerFactorB);
		mLogger.debug("====doublePowerFactorC = " + doublePowerFactorC);
		mLogger.debug("====doublePowerFactorTotal = " + doublePowerFactorTotal);
		
		mLogger.debug("====doubleFrequency = " + doubleFrequency);
		mLogger.debug("====doubleCurrentA = " + doubleCurrentA);
		mLogger.debug("====doubleCurrentB = " + doubleCurrentB);
		mLogger.debug("====doubleCurrentC = " + doubleCurrentC);
		mLogger.debug("====doubleCurrentAVG = " + doubleCurrentAVG);
		
		mLogger.debug("====doubleVoltageAB = " + doubleVoltageAB);
		mLogger.debug("====doubleVoltageBC = " + doubleVoltageBC);
		mLogger.debug("====doubleVoltageCA = " + doubleVoltageCA);
		mLogger.debug("====doubleVoltageAN = " + doubleVoltageAN);
		mLogger.debug("====doubleVoltageBN = " + doubleVoltageBN);
		mLogger.debug("====doubleVoltageCN = " + doubleVoltageCN);
		mLogger.debug("====doubleVoltageAVG = " + doubleVoltageAVG);
		
		mLogger.debug("====doubleActivePowerA = " + doubleActivePowerA);
		mLogger.debug("====doubleActivePowerB = " + doubleActivePowerB);
		mLogger.debug("====doubleActivePowerC = " + doubleActivePowerC);
		mLogger.debug("====doubleActivePowerTotal = " + doubleActivePowerTotal);
		mLogger.debug("====doubleReactivePowerA = " + doubleReactivePowerA);
		mLogger.debug("====doubleReactivePowerB = " + doubleReactivePowerB);
		mLogger.debug("====doubleReactivePowerC = " + doubleReactivePowerC);
		mLogger.debug("====doubleReactivePowerTotal = " + doubleReactivePowerTotal);
		mLogger.debug("====doubleApparentPowerA = " + doubleApparentPowerA);
		mLogger.debug("====doubleApparentPowerB = " + doubleApparentPowerB);
		mLogger.debug("====doubleApparentPowerC = " + doubleApparentPowerC);
		mLogger.debug("====doubleApparentPowerTotal = " + doubleApparentPowerTotal);
		
		mLogger.debug("====doubleLoadPhaseTotal = " + doubleLoadPhaseTotal);
		mLogger.debug("====doubleLoadPhaseA = " + doubleLoadPhaseA);
		mLogger.debug("====doubleLoadPhaseB = " + doubleLoadPhaseB);
		mLogger.debug("====doubleLoadPhaseC = " + doubleLoadPhaseC);
		
		mLogger.debug("====doubleMaximumDemand = " + doubleMaximumDemand);
		
		mLogger.debug("=======================================================================");
	}
}
