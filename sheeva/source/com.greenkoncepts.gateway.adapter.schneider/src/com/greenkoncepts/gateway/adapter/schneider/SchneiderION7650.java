package com.greenkoncepts.gateway.adapter.schneider;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.FuncUtil;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class SchneiderION7650 extends SchneiderDevice {
	static public final int MBREG_DATA_START = 40150-40001;//40150-40265
	static public final int MBREG_DATA_NUM = 116;

	//config
	static public final int MBREG_CONFIG_START1 = 44001;
	static public final int MBREG_CONFIG_NUM1 = 6;
	static public final int posPMVoltsMode = 0;//44001
	static public final int posPMI1Polarity = 1;//44002
	static public final int posPMI2Polarity = 2;//44003
	static public final int posPMI3Polarity = 3;//44004
	static public final int posPMPhaseOrder = 4;//44005
	static public final int posPMPhaseLbls = 5;//44006
	
	static public final int MBREG_CONFIG_START2 = 44391;
	static public final int MBREG_CONFIG_NUM2 = 2;
	static public final int posCM1CommMode =0;//44391
	static public final int posCM1BaudRate =1;//44392	
	
	static public final int MBREG_CONFIG_START3 = 44586;
	static public final int MBREG_CONFIG_NUM3 = 9;
	static public final int posPMI4Polarity =0;//44586
	static public final int posPMV1Polarity =1;//44587
	static public final int posPMV2Polarity =2;//44588
	static public final int posPMV3Polarity =3;//44589
	static public final int posCM2BaudRate =4;//44590
	static public final int posCM3BaudRate =5;//44591
	static public final int posCM1Protocol =6;//44592
	static public final int posCM2Protocol =7;//44593
	static public final int posCM3Protocol =8;//44594	
	
	static public final int MBREG_CONFIG_START4 = 45043;
	static public final int MBREG_CONFIG_NUM4 = 2;
	static public final int posPMV4Polarity = 0;//45043
	static public final int posPMI5Polarity = 1;//45044
	
	static public final int MBREG_CONFIG_START5 = 45460;
	static public final int MBREG_CONFIG_NUM5 = 2;
	static public final int posCM4BaudRate = 0;//45460
	static public final int posCM4Protocol = 1;//45461
	
	static public final int MBREG_CONFIG_START6 = 46001;
	static public final int MBREG_CONFIG_NUM6 = 12;
	static public final int posPMPTPrimary = 0;//46001 to 46002
	static public final int posPMPTSecondary = 2;//46003 to 46004
	static public final int posPMCTPrimary = 4;//46005 to 46006
	static public final int posPMCTSecondary = 6;//46007 to 46008
	static public final int posPMI4CTPrimary = 8;//46009 to 46010
	static public final int posPMI4CTSecondary = 10;//46011 to 46012
	
	static public final int MBREG_CONFIG_START7 = 46977;
	static public final int MBREG_CONFIG_NUM7 = 4;
	static public final int posCM1RTSDelay = 0;// 46977 to 46978
	static public final int posCM1UnitID = 2;// 46979 to 46980
	
	static public final int MBREG_CONFIG_START8 = 47125;
	static public final int MBREG_CONFIG_NUM8 = 8;
	static public final int posCM2RTSDelay = 0;//47125 to 47126
	static public final int posCM3RTSDelay = 2;//47127 to 47128
	static public final int posCM2UnitID = 4;//47129 to 47130
	static public final int posCM3UnitID = 6;//47131 to 47132
	
	static public final int MBREG_CONFIG_START9 = 48903;
	static public final int MBREG_CONFIG_NUM9 = 8;
	static public final int posPMV4PTPrimary = 0;//48903 to 48904
	static public final int posPMV4PTSecondary = 2;//48905 to 48906
	static public final int posPMI5CTPrimary = 4;//48907 to 48908
	static public final int posPMI5CTSecondary = 6;//48909 to 48910
	
	// data

	private double scaleFactorI = 0.1;
	private double scaleFactorV = 1;
	private double scaleFactorHz = 0.1;
	private double scaleFactorF = 0.01;
	private double scaleFactorW = 1;
	private double scaleFactorE = 1;


	private int intActiveEnergyRecTotal = 0;
	private int intReactiveEnergyRecTotal = 0;
	private int prevIntActiveEnergyDelTotal = 0;
	private int intActiveEnergyDelTotal = 0;
	private int prevIntApparentEnergyTotal = 0;
	private int intApparentEnergyTotal = 0;
	private int prevIntReactiveEnergyDelTotal = 0;
	private int intReactiveEnergyDelTotal = 0;
	private int intPowerFactorA = 0;
	private int intPowerFactorB = 0;
	private int intPowerFactorC = 0;
	private int intPowerFactorTotal = 0;
	private int intFrequency = 0;
	private int intCurrentA = 0;
	private int intCurrentB = 0;
	private int intCurrentC = 0;
	private int intCurrentAVG = 0;
	private long intVoltageAB = 0;
	private long intVoltageBC = 0;
	private long intVoltageCA = 0;
	private long intVoltageAN = 0;
	private long intVoltageBN = 0;
	private long intVoltageCN = 0;
	private long intVoltageAVG = 0;
	private int intActivePowerA = 0;
	private int intActivePowerB = 0;
	private int intActivePowerC = 0;
	private int intActivePowerTotal = 0;
	private int intReactivePowerA = 0;
	private int intReactivePowerB = 0;
	private int intReactivePowerC = 0;
	private int intReactivePowerTotal = 0;
	private int intApparentPowerA = 0;
	private int intApparentPowerB = 0;
	private int intApparentPowerC = 0;
	private int intApparentPowerTotal = 0;
	
	private double doubleActiveEnergyRecTotal = 0;
	private double doubleReactiveEnergyRecTotal = 0;
	private double doubleActiveEnergyDelTotal = 0;
	private double doubleActiveEnergyDelTotalConsume = 0;
	private double doubleApparentEnergyTotal = 0;
	private double doubleApparentEnergyTotalConsume = 0;
	private double doubleReactiveEnergyDelTotal = 0;
	private double doubleReactiveEnergyDelTotalConsume = 0;
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

	
	

	static public int posFrequency = OFFSET_DATA  + 2 * 9;//40159
	static public int posCurrentA = OFFSET_DATA  + 2 * 0;//40150
	static public int posCurrentB = OFFSET_DATA  + 2 * 1;//40151
	static public int posCurrentC = OFFSET_DATA  + 2 * 2;// 40152
	static public int posCurrentAVG = OFFSET_DATA  + 2 * 5;//40155
	
	static public int posVoltageAB = OFFSET_DATA  + 2 * 28;//40178
	static public int posVoltageBC = OFFSET_DATA  + 2 * 30;//40180
	static public int posVoltageCA = OFFSET_DATA  + 2 * 32;//40182
	
	static public int posVoltageAN = OFFSET_DATA  + 2 * 16;//40166
	static public int posVoltageBN = OFFSET_DATA  + 2 * 18;//40168
	static public int posVoltageCN = OFFSET_DATA  + 2 * 20;//40170
	static public int posVoltageAVG = OFFSET_DATA  + 2 * 22;//40172
	
	static public int posActivePowerA = OFFSET_DATA  + 2 * 48;//40198
	static public int posActivePowerB = OFFSET_DATA  + 2 * 50;//40200
	static public int posActivePowerC = OFFSET_DATA  + 2 * 52;//40202
	static public int posActivePowerTotal = OFFSET_DATA  + 2 * 54;//40204
	
	static public int posReactivePowerA = OFFSET_DATA  + 2 * 58;//40208
	static public int posReactivePowerB = OFFSET_DATA  + 2 * 60;//40210
	static public int posReactivePowerC = OFFSET_DATA  + 2 * 62;//40212
	static public int posReactivePowerTotal = OFFSET_DATA  + 2 * 64;//40214

	static public int posApparentPowerA = OFFSET_DATA  + 2 * 68;//40218
	static public int posApparentPowerB = OFFSET_DATA  + 2 * 70;//40220
	static public int posApparentPowerC = OFFSET_DATA  + 2 * 72;//40222
	static public int posApparentPowerTotal = OFFSET_DATA  + 2 * 74;//40224
	
	static public int posActiveEnergyDelTotal = OFFSET_DATA  + 2 * 80;//40230
	static public int posActiveEnergyRecTotal = OFFSET_DATA  + 2 * 82;//40232
	static public int posReactiveEnergyDelTotal = OFFSET_DATA  + 2 * 84;//40234
	static public int posReactiveEnergyRecTotal = OFFSET_DATA  + 2 * 86;//40236
	static public int posApparentEnergyTotal = OFFSET_DATA  + 2 * 88;//40238

	
	static public int posPowerFactorA = OFFSET_DATA  + 2 * 112;//40262
	static public int posPowerFactorB = OFFSET_DATA  + 2 * 113;//40263
	static public int posPowerFactorC = OFFSET_DATA  + 2 * 114;//40264
	static public int posPowerFactorTotal = OFFSET_DATA  + 2 * 115;//40265

	public SchneiderION7650(int addr, String category) {
		super(category, addr);
	}

	@Override
	public String getDeviceData() {
		byte[]data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
		if (decodingData(0, data, DATA_MODE)) {
			calculateDecodedData();
		}
		return createDataSendToServer();
	}

	@Override
	public Map<Integer, String> getDeviceConfig() {
		device_config.clear();
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START1, MBREG_CONFIG_NUM1);
		if (!decodingData(1, data, CONFIG_MODE)) {
			mLogger.info("---- can not get settings at address = " + MBREG_CONFIG_START1);
		}
		
		data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START2, MBREG_CONFIG_NUM2);
		if (!decodingData(2, data, CONFIG_MODE)) {
			mLogger.info("---- can not get settings at address = " + MBREG_CONFIG_START2);
		}
		
		data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START3, MBREG_CONFIG_NUM3);
		if (!decodingData(3, data, CONFIG_MODE)) {
			mLogger.info("---- can not get settings at address = " + MBREG_CONFIG_START3);
		}
		
		data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START4, MBREG_CONFIG_NUM4);
		if (!decodingData(4, data, CONFIG_MODE)) {
			mLogger.info("---- can not get settings at address = " + MBREG_CONFIG_START4);
		}
		
		data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START5, MBREG_CONFIG_NUM5);
		if (!decodingData(5, data, CONFIG_MODE)) {
			mLogger.info("---- can not get settings at address = " + MBREG_CONFIG_START5);
		}
		
		data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START6, MBREG_CONFIG_NUM6);
		if (!decodingData(6, data, CONFIG_MODE)) {
			mLogger.info("---- can not get settings at address = " + MBREG_CONFIG_START6);
		}
		
		data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START7, MBREG_CONFIG_NUM7);
		if (!decodingData(7, data, CONFIG_MODE)) {
			mLogger.info("---- can not get settings at address = " + MBREG_CONFIG_START7);
		}
		
		data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START8, MBREG_CONFIG_NUM8);
		if (!decodingData(8, data, CONFIG_MODE)) {
			mLogger.info("---- can not get settings at address = " + MBREG_CONFIG_START8);
		}
		
		data = modbus.readHoldingRegisters(modbusid, MBREG_CONFIG_START9, MBREG_CONFIG_NUM9);
		if (!decodingData(9, data, CONFIG_MODE)) {
			mLogger.info("---- can not get settings at address = " + MBREG_CONFIG_START9);
		}
		
		return device_config;
	}

	@Override
	public List<Integer> setDeviceConfig(Map<Integer, String> config) {
		List<Integer> registers = new ArrayList<Integer>();
		if ((config != null) && (!config.isEmpty())) {
			byte[] results = null;
			byte[] writeBytes = null;
			for (Integer n : config.keySet()) {
				if (n < MBREG_CONFIG_START6) {
					short data = Short.parseShort(config.get(n));
					writeBytes = FuncUtil.shortToRegister(data);
				}  else {
					int data = Integer.parseInt(config.get(n));
					writeBytes = FuncUtil.intToRegisters(data);
				}
				results = modbus.writeMultipleRegisters(modbusid, n, writeBytes);
				if (results != null) {
					registers.add(n);
				}
			}
		}
		return registers;
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
			decodingData(0, data, DATA_MODE);
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			item.put("act_energy", vformat.format(doubleActiveEnergyDelTotal));
			item.put("rea_energy", vformat.format(doubleReactiveEnergyDelTotal));
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
			intFrequency = ModbusUtil.registerToShort(data, posFrequency);
			
			intCurrentA = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posCurrentA);
			intCurrentB = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posCurrentB);
			intCurrentC = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posCurrentC);
			intCurrentAVG = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, posCurrentAVG);
			
			intVoltageAB = FuncUtil.RegisterBigEndian.registersToUint(data, posVoltageAB);
			intVoltageBC = FuncUtil.RegisterBigEndian.registersToUint(data, posVoltageBC);
			intVoltageCA = FuncUtil.RegisterBigEndian.registersToUint(data, posVoltageCA);
			
			intVoltageAN = FuncUtil.RegisterBigEndian.registersToUint(data, posVoltageAN);
			intVoltageBN = FuncUtil.RegisterBigEndian.registersToUint(data, posVoltageBN);
			intVoltageCN = FuncUtil.RegisterBigEndian.registersToUint(data, posVoltageCN);
			intVoltageAVG = FuncUtil.RegisterBigEndian.registersToUint(data, posVoltageAVG);
			
			intActivePowerA = FuncUtil.RegisterBigEndian.registersToInt(data, posActivePowerA);
			intActivePowerB = FuncUtil.RegisterBigEndian.registersToInt(data, posActivePowerB);
			intActivePowerC = FuncUtil.RegisterBigEndian.registersToInt(data, posActivePowerC);
			intActivePowerTotal = FuncUtil.RegisterBigEndian.registersToInt(data, posActivePowerTotal);
			
			intApparentPowerA = FuncUtil.RegisterBigEndian.registersToInt(data, posApparentPowerA);
			intApparentPowerB = FuncUtil.RegisterBigEndian.registersToInt(data, posApparentPowerB);
			intApparentPowerC = FuncUtil.RegisterBigEndian.registersToInt(data, posApparentPowerC);
			intApparentPowerTotal = FuncUtil.RegisterBigEndian.registersToInt(data, posApparentPowerTotal);
			
			intReactivePowerA = FuncUtil.RegisterBigEndian.registersToInt(data, posReactivePowerA);
			intReactivePowerB = FuncUtil.RegisterBigEndian.registersToInt(data, posReactivePowerB);
			intReactivePowerC = FuncUtil.RegisterBigEndian.registersToInt(data, posReactivePowerC);
			intReactivePowerTotal = FuncUtil.RegisterBigEndian.registersToInt(data, posReactivePowerTotal);

			intActiveEnergyDelTotal = FuncUtil.RegisterBigEndian.registersToInt(data, posActiveEnergyDelTotal);
			intActiveEnergyRecTotal = FuncUtil.RegisterBigEndian.registersToInt(data, posActiveEnergyRecTotal);
			intReactiveEnergyDelTotal = FuncUtil.RegisterBigEndian.registersToInt(data, posReactiveEnergyDelTotal);
			intReactiveEnergyRecTotal = FuncUtil.RegisterBigEndian.registersToInt(data, posReactiveEnergyRecTotal);
			intApparentEnergyTotal = FuncUtil.RegisterBigEndian.registersToInt(data, posApparentEnergyTotal);

			
			intPowerFactorA = FuncUtil.RegisterBigEndian.registerToShort(data, posPowerFactorA);
			intPowerFactorB = FuncUtil.RegisterBigEndian.registerToShort(data, posPowerFactorB);
			intPowerFactorC = FuncUtil.RegisterBigEndian.registerToShort(data, posPowerFactorC);
			intPowerFactorTotal = FuncUtil.RegisterBigEndian.registerToShort(data, posPowerFactorTotal);
			return true;
		}

		if (mode == CONFIG_MODE) {
			if (idx == 1) {
				device_config.put(MBREG_CONFIG_START2 + posPMVoltsMode, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posPMVoltsMode)));
				device_config.put(MBREG_CONFIG_START2 + posPMI1Polarity, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posPMI1Polarity)));
				device_config.put(MBREG_CONFIG_START2 + posPMI2Polarity, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posPMI2Polarity)));
				device_config.put(MBREG_CONFIG_START2 + posPMI3Polarity, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posPMI3Polarity)));
				device_config.put(MBREG_CONFIG_START2 + posPMPhaseOrder, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posPMPhaseOrder)));
				device_config.put(MBREG_CONFIG_START2 + posPMPhaseLbls, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posPMPhaseLbls)));
			} else if (idx == 2) {
				device_config.put(MBREG_CONFIG_START1 + posCM1CommMode, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCM1CommMode)));
				device_config.put(MBREG_CONFIG_START1 + posCM1BaudRate, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCM1BaudRate)));
			} else if (idx == 3) {
				device_config.put(MBREG_CONFIG_START2 + posPMI4Polarity, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posPMI4Polarity)));
				device_config.put(MBREG_CONFIG_START2 + posPMV1Polarity, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posPMV1Polarity)));
				device_config.put(MBREG_CONFIG_START2 + posPMV2Polarity, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posPMV2Polarity)));
				device_config.put(MBREG_CONFIG_START2 + posPMV3Polarity, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posPMV3Polarity)));
				device_config.put(MBREG_CONFIG_START2 + posCM2BaudRate, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCM2BaudRate)));
				device_config.put(MBREG_CONFIG_START2 + posCM3BaudRate, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCM3BaudRate)));
				device_config.put(MBREG_CONFIG_START2 + posCM1Protocol, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCM1Protocol)));
				device_config.put(MBREG_CONFIG_START2 + posCM2Protocol, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCM2Protocol)));
				device_config.put(MBREG_CONFIG_START2 + posCM3Protocol, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCM3Protocol)));

			} else if (idx == 4) {
				device_config.put(MBREG_CONFIG_START2 + posPMV4Polarity, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posPMV4Polarity)));
				device_config.put(MBREG_CONFIG_START2 + posPMI5Polarity, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posPMI5Polarity)));
			} else if (idx == 5) {
				device_config.put(MBREG_CONFIG_START2 + posCM4BaudRate, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCM4BaudRate)));
				device_config.put(MBREG_CONFIG_START2 + posCM4Protocol, String.valueOf(FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCM4Protocol)));
			} else if (idx == 6) {
				device_config.put(MBREG_CONFIG_START2 + posPMPTPrimary, String.valueOf(FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posPMPTPrimary)));
				device_config.put(MBREG_CONFIG_START2 + posPMPTSecondary, String.valueOf(FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posPMPTSecondary)));
				device_config.put(MBREG_CONFIG_START2 + posPMCTPrimary, String.valueOf(FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posPMCTPrimary)));
				device_config.put(MBREG_CONFIG_START2 + posPMCTSecondary, String.valueOf(FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posPMCTSecondary)));
				device_config.put(MBREG_CONFIG_START2 + posPMI4CTPrimary, String.valueOf(FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posPMI4CTPrimary)));
				device_config.put(MBREG_CONFIG_START2 + posPMI4CTSecondary, String.valueOf(FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posPMI4CTSecondary)));

			} else if (idx == 7) {				
				device_config.put(MBREG_CONFIG_START2 + posCM1RTSDelay, String.valueOf(FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posCM1RTSDelay)));
				device_config.put(MBREG_CONFIG_START2 + posCM1UnitID, String.valueOf(FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posCM1UnitID)));
			} else if (idx == 8) {
				device_config.put(MBREG_CONFIG_START2 + posCM2RTSDelay, String.valueOf(FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posCM2RTSDelay)));
				device_config.put(MBREG_CONFIG_START2 + posCM3RTSDelay, String.valueOf(FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posCM3RTSDelay)));
				device_config.put(MBREG_CONFIG_START2 + posCM2UnitID, String.valueOf(FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posCM2UnitID)));
				device_config.put(MBREG_CONFIG_START2 + posCM3UnitID, String.valueOf(FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posCM3UnitID)));
			} else if (idx == 9) {
				device_config.put(MBREG_CONFIG_START2 + posPMV4PTPrimary, String.valueOf(FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posPMV4PTPrimary)));
				device_config.put(MBREG_CONFIG_START2 + posPMV4PTSecondary, String.valueOf(FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posPMV4PTSecondary)));
				device_config.put(MBREG_CONFIG_START2 + posPMI5CTPrimary, String.valueOf(FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posPMI5CTPrimary)));
				device_config.put(MBREG_CONFIG_START2 + posPMI5CTSecondary, String.valueOf(FuncUtil.RegisterBigEndian.registersToInt(data, OFFSET_DATA + 2*posPMI5CTSecondary)));

			}
			return true;
		}

		return false;
	}

	private void calculateDecodedData() {
		if (prevIntActiveEnergyDelTotal == 0) {
			doubleActiveEnergyDelTotalConsume = 0;
		} else if (intActiveEnergyDelTotal >= prevIntActiveEnergyDelTotal) {
			doubleActiveEnergyDelTotalConsume = (intActiveEnergyDelTotal - prevIntActiveEnergyDelTotal) * scaleFactorE * 1000;
		} else {
			doubleActiveEnergyDelTotalConsume = 0;
		}
		prevIntActiveEnergyDelTotal = intActiveEnergyDelTotal;

		if (prevIntApparentEnergyTotal == 0) {
			doubleApparentEnergyTotalConsume = 0;
		} else if (intApparentEnergyTotal >= prevIntApparentEnergyTotal) {
			doubleApparentEnergyTotalConsume = (intApparentEnergyTotal - prevIntApparentEnergyTotal) * scaleFactorE * 1000;
		} else {
			doubleApparentEnergyTotalConsume = 0;	
		}
		prevIntApparentEnergyTotal = intApparentEnergyTotal;

		if (prevIntReactiveEnergyDelTotal == 0) {
			doubleReactiveEnergyDelTotalConsume = 0;
		} else if (intReactiveEnergyDelTotal >= prevIntReactiveEnergyDelTotal) {
			doubleReactiveEnergyDelTotalConsume = (intReactiveEnergyDelTotal - prevIntReactiveEnergyDelTotal) * scaleFactorE * 1000;
		} else {
			doubleReactiveEnergyDelTotalConsume = 0;
		}
		prevIntReactiveEnergyDelTotal = intReactiveEnergyDelTotal;

		
		doubleActiveEnergyRecTotal = intActiveEnergyRecTotal * scaleFactorE;
		doubleReactiveEnergyRecTotal = intReactiveEnergyRecTotal * scaleFactorE;
		doubleActiveEnergyDelTotal = intActiveEnergyDelTotal * scaleFactorE;
		doubleApparentEnergyTotal = intApparentEnergyTotal * scaleFactorE;
		doubleReactiveEnergyDelTotal = intReactiveEnergyDelTotal * scaleFactorE;
		doublePowerFactorA = Math.abs(intPowerFactorA) * scaleFactorF;
		doublePowerFactorB = Math.abs(intPowerFactorB) * scaleFactorF;
		doublePowerFactorC = Math.abs(intPowerFactorC) * scaleFactorF;
		doublePowerFactorTotal = Math.abs(intPowerFactorTotal) * scaleFactorF;
		doubleFrequency = intFrequency  * scaleFactorHz;
		doubleCurrentA = intCurrentA * scaleFactorI;
		doubleCurrentB = intCurrentB * scaleFactorI;
		doubleCurrentC = intCurrentC * scaleFactorI;
		doubleCurrentAVG = intCurrentAVG * scaleFactorI;
		doubleVoltageAB = intVoltageAB * scaleFactorV;
		doubleVoltageBC = intVoltageBC * scaleFactorV;
		doubleVoltageCA = intVoltageCA * scaleFactorV;
		doubleVoltageAN = intVoltageAN * scaleFactorV;
		doubleVoltageBN = intVoltageBN * scaleFactorV;
		doubleVoltageCN = intVoltageCN * scaleFactorV;
		doubleVoltageAVG = intVoltageAVG * scaleFactorV;
		doubleActivePowerA = intActivePowerA * scaleFactorW;
		doubleActivePowerB = intActivePowerB * scaleFactorW;
		doubleActivePowerC = intActivePowerC * scaleFactorW;
		doubleActivePowerTotal = intActivePowerTotal * scaleFactorW;
		doubleReactivePowerA = intReactivePowerA * scaleFactorW;
		doubleReactivePowerB = intReactivePowerB * scaleFactorW;
		doubleReactivePowerC = intReactivePowerC * scaleFactorW;
		doubleReactivePowerTotal = intReactivePowerTotal * scaleFactorW;
		doubleApparentPowerA = intApparentPowerA * scaleFactorW;
		doubleApparentPowerB = intApparentPowerB * scaleFactorW;
		doubleApparentPowerC = intApparentPowerC * scaleFactorW;
		doubleApparentPowerTotal = intApparentPowerTotal * scaleFactorW;
		
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
		data.append(";Active Energy Reading=" + vformat.format(doubleActiveEnergyDelTotal) + ",kWh");
		data.append(";Active Energy=" + vformat.format(doubleActiveEnergyDelTotalConsume) + ",Wh");
		data.append(";Apparent Energy Reading=" + vformat.format(doubleApparentEnergyTotal) + ",kVAh");
		data.append(";Apparent Energy=" + vformat.format(doubleApparentEnergyTotalConsume) + ",VAh");
		data.append(";Reactive Energy Reading=" + vformat.format(doubleReactiveEnergyDelTotal) + ",kVARh");
		data.append(";Reactive Energy=" + vformat.format(doubleReactiveEnergyDelTotalConsume) + ",VARh");
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
		mLogger.debug("====doubleActiveEnergyRecTotal = " + doubleActiveEnergyRecTotal);
		mLogger.debug("====doubleReactiveEnergyRecTotal = " + doubleReactiveEnergyRecTotal);
		mLogger.debug("====doubleActiveEnergyDelTotal = " + doubleActiveEnergyDelTotal);
		mLogger.debug("====doubleApparentEnergyTotal = " + doubleApparentEnergyTotal);
		mLogger.debug("====doubleReactiveEnergyDelTotal = " + doubleReactiveEnergyDelTotal);
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
