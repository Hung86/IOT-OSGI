package com.greenkoncepts.gateway.adapter.schneider;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.FuncUtil;

public class SchneiderPM2200DPM extends SchneiderDevice {
	static public final int MBREG_DATA_START1 = 2999;// 403000-403110
	static public final int MBREG_DATA_NUM1 = 112;

	static public final int MBREG_DATA_START2 = 3211;// 403212-403244
	static public final int MBREG_DATA_NUM2 = 36;

	static public int posFrequency = OFFSET_DATA + 2 * 110;// 403110
	static public int posCurrentA = OFFSET_DATA + 2 * 0;// 403000
	static public int posCurrentB = OFFSET_DATA + 2 * 2;// 403002
	static public int posCurrentC = OFFSET_DATA + 2 * 4;// 403004
	static public int posCurrentAVG = OFFSET_DATA + 2 * 10;// 403010

	static public int posVoltageAB = OFFSET_DATA + 2 * 20;// 403020
	static public int posVoltageBC = OFFSET_DATA + 2 * 22;// 403022
	static public int posVoltageCA = OFFSET_DATA + 2 * 24;// 403024

	static public int posVoltageAN = OFFSET_DATA + 2 * 28;// 403028
	static public int posVoltageBN = OFFSET_DATA + 2 * 30;// 403030
	static public int posVoltageCN = OFFSET_DATA + 2 * 32;// 403032
	static public int posVoltageAVG = OFFSET_DATA + 2 * 36;// 403036

	static public int posActivePowerA = OFFSET_DATA + 2 * 54;// 403054
	static public int posActivePowerB = OFFSET_DATA + 2 * 56;// 403056
	static public int posActivePowerC = OFFSET_DATA + 2 * 58;// 403058
	static public int posActivePowerTotal = OFFSET_DATA + 2 * 60;// 403060

	static public int posReactivePowerA = OFFSET_DATA + 2 * 62;// 403062
	static public int posReactivePowerB = OFFSET_DATA + 2 * 64;// 403064
	static public int posReactivePowerC = OFFSET_DATA + 2 * 66;// 403066
	static public int posReactivePowerTotal = OFFSET_DATA + 2 * 68;// 403068

	static public int posApparentPowerA = OFFSET_DATA + 2 * 70;// 403070
	static public int posApparentPowerB = OFFSET_DATA + 2 * 72;// 403072
	static public int posApparentPowerC = OFFSET_DATA + 2 * 74;// 403074
	static public int posApparentPowerTotal = OFFSET_DATA + 2 * 76;// 403076

	static public int posActiveEnergyTotal = OFFSET_DATA + 2 * 0;// 403212
	static public int posReactiveEnergyTotal = OFFSET_DATA + 2 * 16;// 403228
	static public int posApparentEnergyTotal = OFFSET_DATA + 2 * 32;// 403244

	static public int posPowerFactorA = OFFSET_DATA + 2 * 78;// 403078
	static public int posPowerFactorB = OFFSET_DATA + 2 * 80;// 403080
	static public int posPowerFactorC = OFFSET_DATA + 2 * 82;// 403082
	static public int posPowerFactorTotal = OFFSET_DATA + 2 * 84;// 403084

	private double doublePreActiveEnergyTotal = 0;
	private double doubleActiveEnergyTotal = 0;
	private double doubleActiveEnergyDelTotalConsume = 0;

	private double doublePreApparentEnergyTotal = 0;
	private double doubleApparentEnergyTotal = 0;
	private double doubleApparentEnergyTotalConsume = 0;

	private double doublePreReactiveEnergyTotal = 0;
	private double doubleReactiveEnergyTotal = 0;
	private double doubleReactiveEnergyTotalConsume = 0;

	private double doublePowerFactorA = 0;
	private double doublePowerFactorB = 0;
	private double doublePowerFactorC = 0;
	private double doublePowerFactorTotal = 0;
	private double doubleFrequency = 0;
	private double doubleCurrentA = 0;
	private double doubleCurrentB = 0;
	private double doubleCurrentC = 0;
	private double doubleCurrentAVG = 0;
	private double doubleVoltageAB = 0;
	private double doubleVoltageBC = 0;
	private double doubleVoltageCA = 0;
	private double doubleVoltageAN = 0;
	private double doubleVoltageBN = 0;
	private double doubleVoltageCN = 0;
	private double doubleVoltageAVG = 0;
	private double doubleActivePowerA = 0;
	private double doubleActivePowerB = 0;
	private double doubleActivePowerC = 0;
	private double doubleActivePowerTotal = 0;
	private double doubleReactivePowerA = 0;
	private double doubleReactivePowerB = 0;
	private double doubleReactivePowerC = 0;
	private double doubleReactivePowerTotal = 0;
	private double doubleApparentPowerA = 0;
	private double doubleApparentPowerB = 0;
	private double doubleApparentPowerC = 0;
	private double doubleApparentPowerTotal = 0;

	public SchneiderPM2200DPM(int addr, String category) {
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
			} else if (idx == 1) {
				doubleActiveEnergyTotal = FuncUtil.RegisterBigEndian.registersToLong(data, posActiveEnergyTotal)/((double)1000.0);
				doubleReactiveEnergyTotal = FuncUtil.RegisterBigEndian.registersToLong(data, posReactiveEnergyTotal)/((double)1000.0);
				doubleApparentEnergyTotal = FuncUtil.RegisterBigEndian.registersToLong(data, posApparentEnergyTotal)/((double)1000.0);
			}
			return true;

		}

		if (mode == CONFIG_MODE) {
		}

		return false;
	}

	private void calculateDecodedData() {
		if (doublePreActiveEnergyTotal == 0) {
			doubleActiveEnergyDelTotalConsume = 0;
		} else if (doubleActiveEnergyTotal >= doublePreActiveEnergyTotal) {
			doubleActiveEnergyDelTotalConsume = (doubleActiveEnergyTotal - doublePreActiveEnergyTotal) * 1000;
		} else {
			doubleActiveEnergyDelTotalConsume = 0;
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
		
		if (doublePowerFactorA > 1) {
			doublePowerFactorA = 2 - doublePowerFactorA;
		} else if (doublePowerFactorA < -1) {
			doublePowerFactorA = -2 - doublePowerFactorA;
		}
		
		if (doublePowerFactorB > 1) {
			doublePowerFactorB = 2 - doublePowerFactorB;
		} else if (doublePowerFactorB < -1) {
			doublePowerFactorB = -2 - doublePowerFactorB;
		}
		
		if (doublePowerFactorC > 1) {
			doublePowerFactorC = 2 - doublePowerFactorC;
		} else if (doublePowerFactorC < -1) {
			doublePowerFactorC = -2 - doublePowerFactorC;
		}
		
		if (doublePowerFactorTotal > 1) {
			doublePowerFactorTotal = 2 - doublePowerFactorTotal;
		} else if (doublePowerFactorTotal < -1) {
			doublePowerFactorTotal = -2 - doublePowerFactorTotal;
		}

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
		data.append(";Active Energy=" + vformat.format(doubleActiveEnergyDelTotalConsume) + ",Wh");
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

		data.append("|DEVICEID=" + getId() + "-0-1");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(doubleActivePowerA) + ",kW");
		data.append(";Reactive Power=" + vformat.format(doubleReactivePowerA) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(doubleApparentPowerA) + ",kVA");
		data.append(";Power Factor=" + vformat.format(doublePowerFactorA) + ",None");
		data.append(";Current=" + vformat.format(doubleCurrentA) + ",A");
		data.append(";Voltage=" + vformat.format(doubleVoltageAN) + ",V");

		data.append("|DEVICEID=" + getId() + "-0-2");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(doubleActivePowerB) + ",kW");
		data.append(";Reactive Power=" + vformat.format(doubleReactivePowerB) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(doubleApparentPowerB) + ",kVA");
		data.append(";Power Factor=" + vformat.format(doublePowerFactorB) + ",None");
		data.append(";Current=" + vformat.format(doubleCurrentB) + ",A");
		data.append(";Voltage=" + vformat.format(doubleVoltageBN) + ",V");

		data.append("|DEVICEID=" + getId() + "-0-3");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format(doubleActivePowerC) + ",kW");
		data.append(";Reactive Power=" + vformat.format(doubleReactivePowerC) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format(doubleApparentPowerC) + ",kVA");
		data.append(";Power Factor=" + vformat.format(doublePowerFactorC) + ",None");
		data.append(";Current=" + vformat.format(doubleCurrentC) + ",A");
		data.append(";Voltage=" + vformat.format(doubleVoltageCN) + ",V");
		return data.toString();
	}

	private void infoDebug() {
		mLogger.debug("=== Device Address = " + modbusid);
		mLogger.debug("====doubleActiveEnergyTotal = " + doubleActiveEnergyTotal);
		mLogger.debug("====doubleReactiveEnergyTotal = " + doubleReactiveEnergyTotal);
		mLogger.debug("====doubleApparentEnergyTotal = " + doubleApparentEnergyTotal);
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
	}
}
