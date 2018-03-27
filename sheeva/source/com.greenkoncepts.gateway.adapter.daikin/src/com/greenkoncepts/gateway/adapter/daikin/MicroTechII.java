package com.greenkoncepts.gateway.adapter.daikin;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.FuncUtil;

public class MicroTechII extends DaikinDevice {

	public static int MBREG_DATA_COIL = 2;
	public static int MBREG_NUM_COIL = 31;
	
	public static int MBREG_DATA_SEG1 = 1;//40002;
	public static int MBREG_NUM_SEG1 = 100;
	
	public static int MBREG_DATA_SEG2 = 101;//40102;
	public static int MBREG_NUM_SEG2 = 76;
	
	public static int MBREG_DATA_SEG3 = 231;//40232;
	public static int MBREG_NUM_SEG3 = 24;
	
	public static int[] posChillerEnableInput = {0,0x01};//2;0
	public static int[] posRunEnabled = {0,0x02};//3;1
	public static int[] posAlarmDigitalOutput = {0,0x04};//4;2
	public static int[] posChillerLocal_Remote = {0,0x10};//6;4
	public static int[] posChillerLimited = {0,0x20};//7;5
	public static int[] posEvaporatorFlowSwitchStatus = {0,0x40};//8;6
	public static int[] posCondenserFlowSwitchStatus = {0,0x80};//9;7
	public static int[] posClearAlarms = {1,0x04};//12;2
	public static int[] posPumpSelect = {2,0x04};//20;2
	public static int[] posEvaporatorWaterPumpStatus = {3,0x10};//30;4
	public static int[] posCondenserWaterPumpStatus = {3,0x40};//32;6

	
	public static int posCoolSetpoint =  0; //40002;
	public static int posActiveSetpoint = 1;// 40003;
	public static int posCapacityLimitSetpoint = 2;//40004;
	public static int posEvaporatorEnteringWaterTemperature = 3;//40005;
	public static int posHeatSetpoint = 4;//40006;
	public static int posEvaporatorLeavingWaterTemperatureForUnit = 5;//40007;
	public static int posCondenserEnteringWaterTemperature = 6;//40008;
	public static int posCondenserLeavingWaterTemperature = 7;//40009;
	public static int posActualCapacity = 9;//40011;
	public static int posEvaporatorLeavingWaterTemperatureForCompressorSelect = 13;//40015;
	public static int posCompressorSuctionLineTemperatureCompressorSelect = 14;//40016;
	public static int posEvaporatorSaturatedRefrigerantTemperatureCompressorSelect = 15;//40017;
	public static int posEvaporatorRefrigerantPressureCompressorSelect = 16;//40018;
	public static int posEvaporatorWaterFlowRate = 17;//40019;
	public static int posCompressorDischargeTemperatureCompressorSelect = 18;//40020;
	public static int posCondenserSaturatedRefrigerantTemperatureCompressorSelect = 19;//40021;
	public static int posCondenserRefrigerantPressureCompressorSelect = 20;//40022;
	public static int posHeatRecoveryEnteringWaterTemperature = 21;//40023;
	public static int posHeatRecoveryLeavingWaterTemperature = 22;//40024;
	public static int posCondenserWaterFlowRate = 23;//40025;
	public static int posCompressorPercentRLACompressorSelect = 24;//40026;
	public static int posCompressorCurrentCompressorSelect = 25;//40027;
	public static int posCompressorPowerCompressorSelect  = 26;//40028;
	public static int posCompressorVoltageCompressorSelect  = 28;//40030;
	public static int posOilFeedPressureCompressorSelect = 31;//40033;
	public static int posOilSumpPressureCompressorSelect  = 32;//40034;
	public static int posOilFeedTemperatureCompressorSelect = 33;//40035;
	public static int posOilSumpTemperatureCompressorSelect = 34;//40036;
	public static int posLiquidLineRefrigerantTemperatureCompressorSelect = 35;//40037;
	public static int posLiquidLineRefrigerantPressure = 37;//40039;
	public static int posOutdoorAirTemperature = 38;//40040;
	public static int posCapacityLimitOutput = 41;//40043;
	public static int posIceSetpoint = 49;//40051;
	public static int posCavityTemperatureCompressor1 = 84;//40086;
	public static int posInverterTemperatureCompressor1 = 85;//40087;
	public static int posEvaporatorLeavingWaterTemperatureForCompressor2 = 86;//40088;
	public static int posCompressorSuctionLineTemperatureCompressor2  = 87;//40089;
	public static int posEvaporatorSaturatedRefrigerantTemperatureCompressor2  = 88;//40090;
	public static int posEvaporatorRefrigerantPressureCompressor2 = 89;//40091;
	public static int posCompressorDischargeTemperatureCompressor2 = 90;//40092;
	public static int posCondenserSaturatedRefrigerantTemperatureCompressor2 = 91;//40093;
	public static int posCondenserRefrigerantPressureCompressor2 = 92;//40094;
	public static int posCompressorPercentRLACompressor2 = 93;//40095;
	public static int posCompressorCurrentCompressor2 = 94;//40096;
	public static int posCompressorPowerCompressor2 = 95;//40097;
	public static int posCompressorVoltageCompressor2 = 97;//40099;
	public static int posOilFeedPressureCompressor2 = 99;//40101;
	
	
	public static int posOilSumpPressureCompressor2 = 0;//40102;
	public static int posOilFeedTemperatureCompressor2 = 1;//40103;
	public static int posOilSumpTemperatureCompressor2 = 2;//40104;
	public static int posLiquidLineRefrigerantTemperatureCompressor2  = 3;//40105;
	public static int posCavityTemperatureCompressor2 = 4;//40106;
	public static int posInverterTemperatureCompressor2 = 5;//40107;
	public static int posCompressor2ActiveCapacityLimit = 6;//40108;
	public static int posCompressorPercentRLACompressor3 = 7;//40109;
	public static int posCompressorCurrentCompressor3 = 8;//40110;
	public static int posCompressorPowerCompressor3 = 9;//40111;
	public static int posCompressorVoltageCompressor3 = 11;//40113;
	public static int posCompressorPercentRLACompressor4 = 12;//40114;
	public static int posCompressorCurrentCompressor4 = 13;//40115;
	public static int posCompressorPowerCompressor4 = 14;//40116;
	public static int posCompressorVoltageCompressor4 = 16;//40118;
	public static int posCompressorPercentRLACompressor5 = 17;//40119;
	public static int posCompressorCurrentCompressor5 = 18;//40120;
	public static int posCompressorPowerCompressor5 = 19;//40121;
	public static int posCompressorVoltageCompressor5 = 21;//40123;
	public static int posCompressorPercentRLACompressor6 = 22;//40124;
	public static int posCompressorCurrentCompressor6  = 23;//40125;
	public static int posCompressorPowerCompressor6 = 24;//40126;
	public static int posCompressorVoltageCompressor6 = 26;//40128;
	public static int posActiveAlarms130 = 28;//40130;
	public static int posActiveAlarms131 = 29;//40131;
	public static int posActiveAlarms132 = 30;//40132;
	public static int posActiveAlarms133 = 31;//40133;
	public static int posActiveAlarms134 = 32;//40134;
	public static int posActiveAlarms135 = 33;//40135;
	public static int posActiveAlarms136 = 34;//40136;
	public static int posActiveAlarms137 = 35;//40137;
	public static int posActiveAlarms138 = 36;//40138;
	public static int posActiveAlarms139 = 37;//40139;
	public static int posActiveAlarms140 = 38;//40140;
	public static int posActiveAlarms141 = 39;//40141;
	public static int posActiveAlarms142 = 40;//40142;
	public static int posActiveAlarms143 = 41;//40143;
	public static int posActiveAlarms144 = 42;//40144;
	public static int posActiveAlarms145 = 43;//40145;
	public static int posChillerModeSetpoint = 44;//40146;
	public static int posChillerStatus = 45;//40147;
	public static int posChillerModeOutput = 46;//40148;
	public static int posCompressorSelect  = 59;//40161;
	public static int posCompressorStartsCompressorSelect = 72;//40174;
	public static int posCompressorRunHoursCompressorSelect = 73;//40175;
	public static int posEvaporatorPumpRunHoursPumpSelect = 74;//40176;
	public static int posCondenserPumpRunHoursPumpSelect = 75;//40177;
	
	
	public static int posEvaporatorPumpRunHoursPump2 = 0;//40232;
	public static int posCondenserPumpRunHoursPump2 = 1;//40233;
	public static int posCompressorStartsCompressor2  = 3;//40235;
	public static int posCompressorRunHoursCompressor2 = 4;//40236;
	public static int posCompressorStartsCompressor3 = 6;//40238;
	public static int posCompressorRunHoursCompressor3 = 7;//40239;
	public static int posCompressorStartsCompressor4 = 9;//40241;
	public static int posCompressorRunHoursCompressor4 = 10;//40242;
	public static int posChillerPower = 13;//40245;
	public static int posMaximumRPMCompressor1 = 14;//40246;
	public static int posActualRPMCompressor1 = 15;//40247;
	public static int posMinimumRPMCompressor1 = 16;//40248;
	public static int posIGVPercentageOpenCompressor1  = 17;//40249;
	public static int posMaximumRPMCompressor2 = 18;//40250;
	public static int posActualRPMCompressor2 = 19;//40251;
	public static int posMinimumRPMCompressor2  = 20;//40252;
	public static int posIGVPercentageOpenCompressor2 = 21;//40253;
	public static int posDesignRPMCompressor1 = 22;//40254;
	public static int posDesignRPMCompressor2 = 23;//40255;
	
	// data
	private boolean boolChillerEnableInput = false;
	private boolean boolRunEnabled = false;
	private boolean boolAlarmDigitalOutput = false;
	private boolean boolChillerLocal_Remote = false;
	private boolean boolChillerLimited = false;
	private boolean boolEvaporatorFlowSwitchStatus = false;
	private boolean boolCondenserFlowSwitchStatus = false;
	private boolean boolClearAlarms = false;
	private boolean boolPumpSelect = false;
	private boolean boolEvaporatorWaterPumpStatus = false;
	private boolean boolCondenserWaterPumpStatus = false;

	// command 1
	private int intCoolSetpoint =  0; //40002;
	private int intActiveSetpoint = 0;// 40003;
	private int intCapacityLimitSetpoint = 0;//40004;
	private int intEvaporatorEnteringWaterTemperature = 0;//40005;
	private int intHeatSetpoint = 0;//40006;
	private int intEvaporatorLeavingWaterTemperatureForUnit = 0;//40007;
	private int intCondenserEnteringWaterTemperature = 0;//40008;
	private int intCondenserLeavingWaterTemperature = 0;//40009;
	private int intActualCapacity = 0;//40011;
	private int intEvaporatorLeavingWaterTemperatureForCompressorSelect = 0;//40015;
	private int intCompressorSuctionLineTemperatureCompressorSelect = 0;//40016;
	private int intEvaporatorSaturatedRefrigerantTemperatureCompressorSelect = 0;//40017;
	private int intEvaporatorRefrigerantPressureCompressorSelect = 0;//40018;
	private int intEvaporatorWaterFlowRate = 0;//40019;
	private int intCompressorDischargeTemperatureCompressorSelect = 0;//40020;
	private int intCondenserSaturatedRefrigerantTemperatureCompressorSelect = 0;//40021;
	private int intCondenserRefrigerantPressureCompressorSelect = 0;//40022;
	private int intHeatRecoveryEnteringWaterTemperature = 0;//40023;
	private int intHeatRecoveryLeavingWaterTemperature = 0;//40024;
	private int intCondenserWaterFlowRate = 0;//40025;
	private int intCompressorPercentRLACompressorSelect = 0;//40026;
	private int intCompressorCurrentCompressorSelect = 0;//40027;
	private int intCompressorPowerCompressorSelect  = 0;//40028;
	private int intCompressorVoltageCompressorSelect  = 0;//40030;
	private int intOilFeedPressureCompressorSelect = 0;//40033;
	private int intOilSumpPressureCompressorSelect  = 0;//40034;
	private int intOilFeedTemperatureCompressorSelect = 0;//40035;
	private int intOilSumpTemperatureCompressorSelect = 0;//40036;
	private int intLiquidLineRefrigerantTemperatureCompressorSelect = 0;//40037;
	private int intLiquidLineRefrigerantPressure = 0;//40039;
	private int intOutdoorAirTemperature = 0;//40040;
	private int intCapacityLimitOutput = 0;//40043;
	private int intIceSetpoint = 0;//40051;
	private int intCavityTemperatureCompressor1 = 0;//40086;
	private int intInverterTemperatureCompressor1 = 0;//40087;
	private int intEvaporatorLeavingWaterTemperatureForCompressor2 = 0;//40088;
	private int intCompressorSuctionLineTemperatureCompressor2  = 0;//40089;
	private int intEvaporatorSaturatedRefrigerantTemperatureCompressor2  = 0;//40090;
	private int intEvaporatorRefrigerantPressureCompressor2 = 0;//40091;
	private int intCompressorDischargeTemperatureCompressor2 = 0;//40092;
	private int intCondenserSaturatedRefrigerantTemperatureCompressor2 = 0;//40093;
	private int intCondenserRefrigerantPressureCompressor2 = 0;//40094;
	private int intCompressorPercentRLACompressor2 = 0;//40095;
	private int intCompressorCurrentCompressor2 = 0;//40096;
	private int intCompressorPowerCompressor2 = 0;//40097;
	private int intCompressorVoltageCompressor2 = 0;//40099;
	private int intOilFeedPressureCompressor2 = 0;//40101;
	
	// command 2
	private int intOilSumpPressureCompressor2 = 0;//40102;
	private int intOilFeedTemperatureCompressor2 = 0;//40103;
	private int intOilSumpTemperatureCompressor2 = 0;//40104;
	private int intLiquidLineRefrigerantTemperatureCompressor2  = 0;//40105;
	private int intCavityTemperatureCompressor2 = 0;//40106;
	private int intInverterTemperatureCompressor2 = 0;//40107;
	private int intCompressor2ActiveCapacityLimit = 0;//40108;
	private int intCompressorPercentRLACompressor3 = 0;//40109;
	private int intCompressorCurrentCompressor3 = 0;//40110;
	private int intCompressorPowerCompressor3 = 0;//40111;
	private int intCompressorVoltageCompressor3 = 0;//40113;
	private int intCompressorPercentRLACompressor4 = 0;//40114;
	private int intCompressorCurrentCompressor4 = 0;//40115;
	private int intCompressorPowerCompressor4 = 0;//40116;
	private int intCompressorVoltageCompressor4 = 0;//40118;
	private int intCompressorPercentRLACompressor5 = 0;//40119;
	private int intCompressorCurrentCompressor5 = 0;//40120;
	private int intCompressorPowerCompressor5 = 0;//40121;
	private int intCompressorVoltageCompressor5 = 0;//40123;
	private int intCompressorPercentRLACompressor6 = 0;//40124;
	private int intCompressorCurrentCompressor6  = 0;//40125;
	private int intCompressorPowerCompressor6 = 0;//40126;
	private int intCompressorVoltageCompressor6 = 0;//40128;
	private int intActiveAlarms130 = 0;//40130;
	private int intActiveAlarms131 = 0;//40131;
	private int intActiveAlarms132 = 0;//40132;
	private int intActiveAlarms133 = 0;//40133;
	private int intActiveAlarms134 = 0;//40134;
	private int intActiveAlarms135 = 0;//40135;
	private int intActiveAlarms136 = 0;//40136;
	private int intActiveAlarms137 = 0;//40137;
	private int intActiveAlarms138 = 0;//40138;
	private int intActiveAlarms139 = 0;//40139;
	private int intActiveAlarms140 = 0;//40140;
	private int intActiveAlarms141 = 0;//40141;
	private int intActiveAlarms142 = 0;//40142;
	private int intActiveAlarms143 = 0;//40143;
	private int intActiveAlarms144 = 0;//40144;
	private int intActiveAlarms145 = 0;//40145;
	private int intChillerModeSetpoint = 0;//40146;
	private int intChillerStatus = 0;//40147;
	private int intChillerModeOutput = 0;//40148;
	private int intCompressorSelect  = 0;//40161;
	private int intCompressorStartsCompressorSelect = 0;//40174;
	private int intCompressorRunHoursCompressorSelect = 0;//40175;
	private int intEvaporatorPumpRunHoursPumpSelect = 0;//40176;
	private int intCondenserPumpRunHoursPumpSelect = 0;//40177;
	
	// command 3
	private int intEvaporatorPumpRunHoursPump2 = 0;//40232;
	private int intCondenserPumpRunHoursPump2 = 0;//40233;
	private int intCompressorStartsCompressor2  = 0;//40235;
	private int intCompressorRunHoursCompressor2 = 0;//40236;
	private int intCompressorStartsCompressor3 = 0;//40238;
	private int intCompressorRunHoursCompressor3 = 0;//40239;
	private int intCompressorStartsCompressor4 = 0;//40241;
	private int intCompressorRunHoursCompressor4 = 0;//40242;
	private int intChillerPower = 0;//40245;
	private int intMaximumRPMCompressor1 = 0;//40246;
	private int intActualRPMCompressor1 = 0;//40247;
	private int intMinimumRPMCompressor1 = 0;//40248;
	private int intIGVPercentageOpenCompressor1  = 0;//40249;
	private int intMaximumRPMCompressor2 = 0;//40250;
	private int intActualRPMCompressor2 = 0;//40251;
	private int intMinimumRPMCompressor2  = 0;//40252;
	private int intIGVPercentageOpenCompressor2 = 0;//40253;
	private int intDesignRPMCompressor1 = 0;//40254;
	private int intDesignRPMCompressor2 = 0;//40255;
	
	
	private double calActiveSetpoint = 0;
	private double calCavityTemperatureCompressor1 = 0;
	private double calCavityTemperatureCompressor2 = 0;
	private double calCompressorDischargeTemperatureCompressorSelect = 0;
	private double calCompressorDischargeTemperatureCompressor2 = 0;
	private double calCompressorSuctionLineTemperatureCompressorSelect = 0;
	private double calCompressorSuctionLineTemperatureCompressor2 = 0;
	private double calCondenserEnteringWaterTemperature = 0;
	private double calCondenserLeavingWaterTemperature =  0;
	private double calCondenserSaturatedRefrigerantTemperatureCompressorSelect =  0;
	private double calCondenserSaturatedRefrigerantTemperatureCompressor2 = 0;
	private double calCoolSetpoint = 0;
	private double calEvaporatorEnteringWaterTemperature = 0;
	private double calEvaporatorLeavingWaterTemperatureForUnit = 0;
	private double calEvaporatorLeavingWaterTemperatureForCompressorSelect = 0;
	private double calEvaporatorLeavingWaterTemperatureForCompressor2 = 0;
	private double calEvaporatorSaturatedRefrigerantTemperatureCompressorSelect = 0;
	private double calEvaporatorSaturatedRefrigerantTemperatureCompressor2 = 0;
	private double calHeatRecoveryEnteringWaterTemperature = 0;
	private double calHeatRecoveryLeavingWaterTemperature = 0;
	private double calHeatSetpoint =  0;
	private double calIceSetpoint =  0;
	private double calInverterTemperatureCompressor1 =  0;
	private double calInverterTemperatureCompressor2 = 0;
	private double calLiquidLineRefrigerantTemperatureCompressorSelect =  0;
	private double calLiquidLineRefrigerantTemperatureCompressor2 = 0;
	private double calOilFeedTemperatureCompressorSelect = 0;
	private double calOilFeedTemperatureCompressor2 = 0;
	private double calOilSumpTemperatureCompressorSelect = 0;
	private double calOilSumpTemperatureCompressor2 = 0;
	private double calOutdoorAirTemperature = 0;
			
	private double calCompressorRunHoursCompressorSelect = 0;
	private double calCompressorRunHoursCompressor2 = 0;
	private double calCompressorRunHoursCompressor3 = 0;
	private double calCompressorRunHoursCompressor4 = 0;
	private double calCondenserWaterFlowRate = 0;
	private double calEvaporatorWaterFlowRate = 0;
	
	private int calChillerEnableInput = 0;
	private int calRunEnabled = 0;
	private int calAlarmDigitalOutput = 0;
	private int calChillerLocal_Remote = 0;
	private int calChillerLimited = 0;
	private int calEvaporatorFlowSwitchStatus = 0;
	private int calCondenserFlowSwitchStatus = 0;
	private int calClearAlarms = 0;
	private int calPumpSelect = 0;
	private int calEvaporatorWaterPumpStatus = 0;
	private int calCondenserWaterPumpStatus = 0;
	
	public MicroTechII(String category, int addr) {
		super(category, addr);
	}

	@Override
	public String getDeviceData() {
		//get coil registers
		boolean ok = false;
		byte[] data = modbus.readCoilRegisters(modbusid, MBREG_DATA_COIL, MBREG_NUM_COIL);
		decodingData(0, data, DATA_MODE);
		
		data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_SEG1, MBREG_NUM_SEG1);
		ok = decodingData(1, data, DATA_MODE);
		
		if (ok) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_SEG2, MBREG_NUM_SEG2);
			ok = decodingData(2, data, DATA_MODE);
		}
		
		if (ok) {
			data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_SEG3, MBREG_NUM_SEG3);
			ok = decodingData(3, data, DATA_MODE);
		}
		
		if (ok) {
			calculateDecodedData(); 
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
			//get coil registers
			boolean ok = false;
			byte[] data = modbus.readCoilRegisters(modbusid, MBREG_DATA_COIL, MBREG_NUM_COIL);
			decodingData(0, data, DATA_MODE);
			
			data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_SEG1, MBREG_NUM_SEG1);
			ok = decodingData(1, data, DATA_MODE);
			
			if (ok) {
				data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_SEG2, MBREG_NUM_SEG2);
				ok = decodingData(2, data, DATA_MODE);
			}
			
			if (ok) {
				data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_SEG3, MBREG_NUM_SEG3);
				ok = decodingData(3, data, DATA_MODE);
			}
			
			if (ok) {
				calculateDecodedData(); 
			}
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			// data from System
			item.put("Active_Setpoint" , vformat.format(calActiveSetpoint)); 
			item.put("Actual_Capacity" , "" + intActualCapacity); 
			item.put("Actual_RPM_Compressor_1" , "" + intActualRPMCompressor1); 
			item.put("Actual_RPM_Compressor_2" , "" + intActualRPMCompressor2); 
			item.put("Capacity_Limit_Output" , "" + intCapacityLimitOutput); 
			item.put("Capacity_Limit_Setpoint" , "" + intCapacityLimitSetpoint); 
			item.put("Cavity_Temperature_Compressor_1" , vformat.format(calCavityTemperatureCompressor1)); 
			item.put("Cavity_Temperature_Compressor_2" , vformat.format(calCavityTemperatureCompressor2)); 
			item.put("Chiller_Enable_Input" , "" + calChillerEnableInput); 
			item.put("Chiller_Limited" , "" + calChillerLimited); 
			item.put("Chiller_Local_Remote" , "" + calChillerLocal_Remote); 
			item.put("Chiller_Mode_Output" , "" + intChillerModeOutput); 
			item.put("Chiller_Mode_Setpoint" , "" + intChillerModeSetpoint); 
			item.put("Compressor_2_Active_Capacity_Limit" , "" + intCompressor2ActiveCapacityLimit); 
			item.put("Chiller_Status" , "" + intChillerStatus); 
			item.put("Chiller_Power" , "" + intChillerPower); 
			item.put("Compressor_Current_Compressor_Select" , "" + intCompressorCurrentCompressorSelect); 
			item.put("Compressor_Current_Compressor_2" , "" + intCompressorCurrentCompressor2); 
			item.put("Compressor_Current_Compressor_3" , "" + intCompressorCurrentCompressor3); 
			item.put("Compressor_Current_Compressor_4" , "" + intCompressorCurrentCompressor4); 
			item.put("Compressor_Current_Compressor_5" , "" + intCompressorCurrentCompressor5); 
			item.put("Compressor_Current_Compressor_6" , "" + intCompressorCurrentCompressor6); 
			item.put("Compressor_Discharge_Temperature_Compressor_Select" , vformat.format(calCompressorDischargeTemperatureCompressorSelect)); 
			item.put("Compressor_Discharge_Temperature_Compressor_2" , vformat.format(calCompressorDischargeTemperatureCompressor2)); 
			item.put("Compressor_Percent_RLA_Compressor_Select" , "" + intCompressorPercentRLACompressorSelect); 
			item.put("Compressor_Percent_RLA_Compressor_2" , "" + intCompressorPercentRLACompressor2); 
			item.put("Compressor_Percent_RLA_Compressor_3" , "" + intCompressorPercentRLACompressor3); 
			item.put("Compressor_Percent_RLA_Compressor_4" , "" + intCompressorPercentRLACompressor4); 
			item.put("Compressor_Percent_RLA_Compressor_5" , "" + intCompressorPercentRLACompressor5); 
			item.put("Compressor_Percent_RLA_Compressor_6" , "" + intCompressorPercentRLACompressor6); 
			item.put("Compressor_Power_Compressor_Select" , "" + intCompressorPowerCompressorSelect); 
			item.put("Compressor_Power_Compressor_2" , "" + intCompressorPowerCompressor2); 
			item.put("Compressor_Power_Compressor_3" , "" + intCompressorPowerCompressor3); 
			item.put("Compressor_Power_Compressor_4" , "" + intCompressorPowerCompressor4); 
			item.put("Compressor_Power_Compressor_5" , "" + intCompressorPowerCompressor5); 
			item.put("Compressor_Power_Compressor_6" , "" + intCompressorPowerCompressor6); 
			item.put("Compressor_Run_Hours_Compressor_Select" , vformat.format(calCompressorRunHoursCompressorSelect)); 
			item.put("Compressor_Run_Hours_Compressor_2" , vformat.format(calCompressorRunHoursCompressor2)); 
			item.put("Compressor_Run_Hours_Compressor_3" , vformat.format(calCompressorRunHoursCompressor3)); 
			item.put("Compressor_Run_Hours_Compressor_4" , vformat.format(calCompressorRunHoursCompressor4)); 
			item.put("Compressor_Select" , "" + intCompressorSelect); 
			item.put("Compressor_Starts_Compressor_Select" , "" + intCompressorStartsCompressorSelect); 
			item.put("Compressor_Starts_Compressor_2" , "" + intCompressorStartsCompressor2); 
			item.put("Compressor_Starts_Compressor_3" , "" + intCompressorStartsCompressor3); 
			item.put("Compressor_Starts_Compressor_4" , "" + intCompressorStartsCompressor4); 
			item.put("Compressor_Suction_Line_Temperature_Compressor_Select" , vformat.format(calCompressorSuctionLineTemperatureCompressorSelect)); 
			item.put("Compressor_Suction_Line_Temperature_Compressor_2" , vformat.format(calCompressorSuctionLineTemperatureCompressor2)); 
			item.put("Compressor_Voltage_Compressor_Select" , "" + intCompressorVoltageCompressorSelect); 
			item.put("Compressor_Voltage_Compressor_2" , "" + intCompressorVoltageCompressor2); 
			item.put("Compressor_Voltage_Compressor_3" , "" + intCompressorVoltageCompressor3); 
			item.put("Compressor_Voltage_Compressor_4" , "" + intCompressorVoltageCompressor4); 
			item.put("Compressor_Voltage_Compressor_5" , "" + intCompressorVoltageCompressor5); 
			item.put("Compressor_Voltage_Compressor_6" , "" + intCompressorVoltageCompressor6); 
			item.put("Condenser_Entering_Water_Temperature" , vformat.format(calCondenserEnteringWaterTemperature)); 
			item.put("Condenser_Flow_Switch_Status" , "" + calCondenserFlowSwitchStatus); 
			item.put("Condenser_Leaving_Water_Temperature" , vformat.format(calCondenserLeavingWaterTemperature)); 
			item.put("Condenser_Pump_Run_Hours_Pump_Select" , "" + intCondenserPumpRunHoursPumpSelect); 
			item.put("Condenser_Pump_Run_Hours_Pump_2" , "" + intCondenserPumpRunHoursPump2); 
			item.put("Condenser_Refrigerant_Pressure_Compressor_Select" , "" + intCondenserRefrigerantPressureCompressorSelect); 
			item.put("Condenser_Refrigerant_Pressure_Compressor_2" , "" + intCondenserRefrigerantPressureCompressor2); 
			item.put("Condenser_Saturated_Refrigerant_Temperature_Compressor_Select" , vformat.format(calCondenserSaturatedRefrigerantTemperatureCompressorSelect)); 
			item.put("Condenser_Saturated_Refrigerant_Temperature_Compressor_2" , vformat.format(calCondenserSaturatedRefrigerantTemperatureCompressor2)); 
			item.put("Condenser_Water_Flow_Rate" , vformat.format(calCondenserWaterFlowRate)); 
			item.put("Design_RPM_Compressor_1" , "" + intDesignRPMCompressor1); 
			item.put("Design_RPM_Compressor_2" , "" + intDesignRPMCompressor2); 
			item.put("IGV_Percentage_Open_Compressor_1" , "" + intIGVPercentageOpenCompressor1); 
			item.put("IGV_Percentage_Open_Compressor_2" , "" + intIGVPercentageOpenCompressor2); 
			item.put("Inverter_Temperature_Compressor_1" , vformat.format(calInverterTemperatureCompressor1)); 
			item.put("Inverter_Temperature_Compressor_2" , vformat.format(calInverterTemperatureCompressor2)); 
			item.put("Maximum_RPM_Compressor_1" , "" + intMaximumRPMCompressor1); 
			item.put("Maximum_RPM_Compressor_2" , "" + intMaximumRPMCompressor2); 
			item.put("Minimum_RPM_Compressor_1" , "" + intMinimumRPMCompressor1); 
			item.put("Minimum_RPM_Compressor_2" , "" + intMinimumRPMCompressor2); 
			item.put("Condenser_Water_Pump_Status" , "" + calCondenserWaterPumpStatus); 
			item.put("Cool_Setpoint" , vformat.format(calCoolSetpoint)); 
			item.put("Evaporator_Entering_Water_Temperature" , vformat.format(calEvaporatorEnteringWaterTemperature)); 
			item.put("Evaporator_Flow_Switch_Status" , "" + calEvaporatorFlowSwitchStatus); 
			item.put("Evaporator_Leaving_Water_Temperature_For_Unit" , vformat.format(calEvaporatorLeavingWaterTemperatureForUnit)); 
			item.put("Evaporator_Leaving_Water_Temperature_For_Compressor_Select" , vformat.format(calEvaporatorLeavingWaterTemperatureForCompressorSelect)); 
			item.put("Evaporator_Leaving_Water_Temperature_For_Compressor_2" , vformat.format(calEvaporatorLeavingWaterTemperatureForCompressor2)); 
			item.put("Evaporator_Pump_Run_Hours_Pump_Select" , "" + intEvaporatorPumpRunHoursPumpSelect); 
			item.put("Evaporator_Pump_Run_Hours_Pump_2" , "" + intEvaporatorPumpRunHoursPump2); 
			item.put("Evaporator_Refrigerant_Pressure_Compressor_Select" , "" + intEvaporatorRefrigerantPressureCompressorSelect); 
			item.put("Evaporator_Refrigerant_Pressure_Compressor_2" , "" + intEvaporatorRefrigerantPressureCompressor2); 
			item.put("Evaporator_Saturated_Refrigerant_Temperature_Compressor_Select" , vformat.format(calEvaporatorSaturatedRefrigerantTemperatureCompressorSelect)); 
			item.put("Evaporator_Saturated_Refrigerant_Temperature_Compressor_2" , vformat.format(calEvaporatorSaturatedRefrigerantTemperatureCompressor2)); 
			item.put("Evaporator_Water_Flow_Rate" , vformat.format(calEvaporatorWaterFlowRate)); 
			item.put("Evaporator_Water_Pump_Status" , "" + calEvaporatorWaterPumpStatus); 
			item.put("Heat_Recovery_Entering_Water_Temperature" , vformat.format(calHeatRecoveryEnteringWaterTemperature)); 
			item.put("Heat_Recovery_Leaving_Water_Temperature" , vformat.format(calHeatRecoveryLeavingWaterTemperature)); 
			item.put("Heat_Setpoint" , vformat.format(calHeatSetpoint)); 
			item.put("Ice_Setpoint" , vformat.format(calIceSetpoint)); 
			item.put("Liquid_Line_Refrigerant_Pressure" , "" + intLiquidLineRefrigerantPressure); 
			item.put("Liquid_Line_Refrigerant_Temperature_Compressor_Select" , vformat.format(calLiquidLineRefrigerantTemperatureCompressorSelect)); 
			item.put("Liquid_Line_Refrigerant_Temperature_Compressor_2" , vformat.format(calLiquidLineRefrigerantTemperatureCompressor2)); 
			item.put("Oil_Feed_Pressure_Compressor_Select" , "" + intOilFeedPressureCompressorSelect); 
			item.put("Oil_Feed_Pressure_Compressor_2" , "" + intOilFeedPressureCompressor2); 
			item.put("Oil_Feed_Temperature_Compressor_Select" , vformat.format(calOilFeedTemperatureCompressorSelect)); 
			item.put("Oil_Feed_Temperature_Compressor_2" , vformat.format(calOilFeedTemperatureCompressor2)); 
			item.put("Oil_Sump_Pressure_Compressor_Select" , "" + intOilSumpPressureCompressorSelect); 
			item.put("Oil_Sump_Pressure_Compressor_2" , "" + intOilSumpPressureCompressor2); 
			item.put("Oil_Sump_Temperature_Compressor_Select" , vformat.format(calOilSumpTemperatureCompressorSelect)); 
			item.put("Oil_Sump_Temperature_Compressor_2" , vformat.format(calOilSumpTemperatureCompressor2)); 
			item.put("Outdoor_Air_Temperature" , vformat.format(calOutdoorAirTemperature)); 
			item.put("Pump_Select" , "" + calPumpSelect); 
			item.put("Run_Enabled" , "" + calRunEnabled); 
			item.put("Alarm_Digital_Output" , "" + calAlarmDigitalOutput); 
			item.put("Clear_Alarms" , "" + calClearAlarms);

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
				boolChillerEnableInput = FuncUtil.byteToCoil(data[OFFSET_DATA + posChillerEnableInput[0]], posChillerEnableInput[1]);
				boolRunEnabled = FuncUtil.byteToCoil(data[OFFSET_DATA + posRunEnabled[0]], posRunEnabled[1]);
				boolAlarmDigitalOutput = FuncUtil.byteToCoil(data[OFFSET_DATA + posAlarmDigitalOutput[0]], posAlarmDigitalOutput[1]);
				boolChillerLocal_Remote = FuncUtil.byteToCoil(data[OFFSET_DATA + posChillerLocal_Remote[0]], posChillerLocal_Remote[1]);
				boolChillerLimited = FuncUtil.byteToCoil(data[OFFSET_DATA + posChillerLimited[0]], posChillerLimited[1]);
				boolEvaporatorFlowSwitchStatus = FuncUtil.byteToCoil(data[OFFSET_DATA + posEvaporatorFlowSwitchStatus[0]], posEvaporatorFlowSwitchStatus[1]);
				boolCondenserFlowSwitchStatus = FuncUtil.byteToCoil(data[OFFSET_DATA + posCondenserFlowSwitchStatus[0]], posCondenserFlowSwitchStatus[1]);
				boolClearAlarms = FuncUtil.byteToCoil(data[OFFSET_DATA + posClearAlarms[0]], posClearAlarms[1]);
				boolPumpSelect = FuncUtil.byteToCoil(data[OFFSET_DATA + posPumpSelect[0]], posPumpSelect[1]);
				boolEvaporatorWaterPumpStatus = FuncUtil.byteToCoil(data[OFFSET_DATA + posEvaporatorWaterPumpStatus[0]], posEvaporatorWaterPumpStatus[1]);
				boolCondenserWaterPumpStatus = FuncUtil.byteToCoil(data[OFFSET_DATA + posCondenserWaterPumpStatus[0]], posCondenserWaterPumpStatus[1]);
				return true;
			}
			
			if (idx == 1) {
				intCoolSetpoint = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCoolSetpoint) ; //40002;
				intActiveSetpoint =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posActiveSetpoint) ;// 40003;
				intCapacityLimitSetpoint =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCapacityLimitSetpoint) ;//40004;
				intEvaporatorEnteringWaterTemperature =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posEvaporatorEnteringWaterTemperature) ;//40005;
				intHeatSetpoint =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posHeatSetpoint) ;//40006;
				intEvaporatorLeavingWaterTemperatureForUnit =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posEvaporatorLeavingWaterTemperatureForUnit) ;//40007;
				intCondenserEnteringWaterTemperature =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCondenserEnteringWaterTemperature) ;//40008;
				intCondenserLeavingWaterTemperature =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCondenserLeavingWaterTemperature) ;//40009;
				intActualCapacity =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActualCapacity) ;//40011;
				intEvaporatorLeavingWaterTemperatureForCompressorSelect =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posEvaporatorLeavingWaterTemperatureForCompressorSelect) ;//40015;
				intCompressorSuctionLineTemperatureCompressorSelect =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCompressorSuctionLineTemperatureCompressorSelect) ;//40016;
				intEvaporatorSaturatedRefrigerantTemperatureCompressorSelect =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posEvaporatorSaturatedRefrigerantTemperatureCompressorSelect) ;//40017;
				intEvaporatorRefrigerantPressureCompressorSelect =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posEvaporatorRefrigerantPressureCompressorSelect) ;//40018;
				intEvaporatorWaterFlowRate =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posEvaporatorWaterFlowRate) ;//40019;
				intCompressorDischargeTemperatureCompressorSelect =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCompressorDischargeTemperatureCompressorSelect) ;//40020;
				intCondenserSaturatedRefrigerantTemperatureCompressorSelect =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCondenserSaturatedRefrigerantTemperatureCompressorSelect) ;//40021;
				intCondenserRefrigerantPressureCompressorSelect =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCondenserRefrigerantPressureCompressorSelect) ;//40022;
				intHeatRecoveryEnteringWaterTemperature =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posHeatRecoveryEnteringWaterTemperature) ;//40023;
				intHeatRecoveryLeavingWaterTemperature =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posHeatRecoveryLeavingWaterTemperature) ;//40024;
				intCondenserWaterFlowRate =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCondenserWaterFlowRate) ;//40025;
				intCompressorPercentRLACompressorSelect =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorPercentRLACompressorSelect) ;//40026;
				intCompressorCurrentCompressorSelect =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorCurrentCompressorSelect) ;//40027;
				intCompressorPowerCompressorSelect  =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorPowerCompressorSelect) ;//40028;
				intCompressorVoltageCompressorSelect  =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorVoltageCompressorSelect) ;//40030;
				intOilFeedPressureCompressorSelect =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posOilFeedPressureCompressorSelect) ;//40033;
				intOilSumpPressureCompressorSelect  =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posOilSumpPressureCompressorSelect) ;//40034;
				intOilFeedTemperatureCompressorSelect =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posOilFeedTemperatureCompressorSelect) ;//40035;
				intOilSumpTemperatureCompressorSelect =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posOilSumpTemperatureCompressorSelect) ;//40036;
				intLiquidLineRefrigerantTemperatureCompressorSelect =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posLiquidLineRefrigerantTemperatureCompressorSelect) ;//40037;
				intLiquidLineRefrigerantPressure =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posLiquidLineRefrigerantPressure) ;//40039;
				intOutdoorAirTemperature =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posOutdoorAirTemperature) ;//40040;
				intCapacityLimitOutput =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCapacityLimitOutput) ;//40043;
				intIceSetpoint =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posIceSetpoint) ;//40051;
				intCavityTemperatureCompressor1 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCavityTemperatureCompressor1) ;//40086;
				intInverterTemperatureCompressor1 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posInverterTemperatureCompressor1) ;//40087;
				intEvaporatorLeavingWaterTemperatureForCompressor2 =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posEvaporatorLeavingWaterTemperatureForCompressor2) ;//40088;
				intCompressorSuctionLineTemperatureCompressor2  =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCompressorSuctionLineTemperatureCompressor2) ;//40089;
				intEvaporatorSaturatedRefrigerantTemperatureCompressor2  =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posEvaporatorSaturatedRefrigerantTemperatureCompressor2) ;//40090;
				intEvaporatorRefrigerantPressureCompressor2 =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posEvaporatorRefrigerantPressureCompressor2) ;//40091;
				intCompressorDischargeTemperatureCompressor2 =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCompressorDischargeTemperatureCompressor2) ;//40092;
				intCondenserSaturatedRefrigerantTemperatureCompressor2 =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCondenserSaturatedRefrigerantTemperatureCompressor2) ;//40093;
				intCondenserRefrigerantPressureCompressor2 =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posCondenserRefrigerantPressureCompressor2) ;//40094;
				intCompressorPercentRLACompressor2 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorPercentRLACompressor2) ;//40095;
				intCompressorCurrentCompressor2 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorCurrentCompressor2) ;//40096;
				intCompressorPowerCompressor2 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorPowerCompressor2) ;//40097;
				intCompressorVoltageCompressor2 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorVoltageCompressor2) ;//40099;
				intOilFeedPressureCompressor2 =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posOilFeedPressureCompressor2) ;//40101;
				return true;
			}
			
			
			if (idx == 2) {
				intOilSumpPressureCompressor2 =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posOilSumpPressureCompressor2) ;//40102;
				intOilFeedTemperatureCompressor2 =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posOilFeedTemperatureCompressor2) ;//40103;
				intOilSumpTemperatureCompressor2 =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posOilSumpTemperatureCompressor2) ;//40104;
				intLiquidLineRefrigerantTemperatureCompressor2  =FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2*posLiquidLineRefrigerantTemperatureCompressor2) ;//40105;
				intCavityTemperatureCompressor2 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCavityTemperatureCompressor2) ;//40106;
				intInverterTemperatureCompressor2 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posInverterTemperatureCompressor2) ;//40107;
				intCompressor2ActiveCapacityLimit =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressor2ActiveCapacityLimit) ;//40108;
				intCompressorPercentRLACompressor3 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorPercentRLACompressor3) ;//40109;
				intCompressorCurrentCompressor3 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorCurrentCompressor3) ;//40110;
				intCompressorPowerCompressor3 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorPowerCompressor3) ;//40111;
				intCompressorVoltageCompressor3 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorVoltageCompressor3) ;//40113;
				intCompressorPercentRLACompressor4 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorPercentRLACompressor4) ;//40114;
				intCompressorCurrentCompressor4 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorCurrentCompressor4) ;//40115;
				intCompressorPowerCompressor4 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorPowerCompressor4) ;//40116;
				intCompressorVoltageCompressor4 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorVoltageCompressor4) ;//40118;
				intCompressorPercentRLACompressor5 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorPercentRLACompressor5) ;//40119;
				intCompressorCurrentCompressor5 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorCurrentCompressor5) ;//40120;
				intCompressorPowerCompressor5 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorPowerCompressor5) ;//40121;
				intCompressorVoltageCompressor5 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorVoltageCompressor5) ;//40123;
				intCompressorPercentRLACompressor6 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorPercentRLACompressor6) ;//40124;
				intCompressorCurrentCompressor6  =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorCurrentCompressor6) ;//40125;
				intCompressorPowerCompressor6 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorPowerCompressor6) ;//40126;
				intCompressorVoltageCompressor6 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorVoltageCompressor6) ;//40128;
				intActiveAlarms130 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActiveAlarms130) ;//40130;
				intActiveAlarms131 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActiveAlarms131) ;//40131;
				intActiveAlarms132 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActiveAlarms132) ;//40132;
				intActiveAlarms133 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActiveAlarms133) ;//40133;
				intActiveAlarms134 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActiveAlarms134) ;//40134;
				intActiveAlarms135 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActiveAlarms135) ;//40135;
				intActiveAlarms136 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActiveAlarms136) ;//40136;
				intActiveAlarms137 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActiveAlarms137) ;//40137;
				intActiveAlarms138 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActiveAlarms138) ;//40138;
				intActiveAlarms139 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActiveAlarms139) ;//40139;
				intActiveAlarms140 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActiveAlarms140) ;//40140;
				intActiveAlarms141 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActiveAlarms141) ;//40141;
				intActiveAlarms142 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActiveAlarms142) ;//40142;
				intActiveAlarms143 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActiveAlarms143) ;//40143;
				intActiveAlarms144 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActiveAlarms144) ;//40144;
				intActiveAlarms145 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActiveAlarms145) ;//40145;
				intChillerModeSetpoint =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posChillerModeSetpoint) ;//40146;
				intChillerStatus =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posChillerStatus) ;//40147;
				intChillerModeOutput =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posChillerModeOutput) ;//40148;
				intCompressorSelect  =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorSelect) ;//40161;
				intCompressorStartsCompressorSelect =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorStartsCompressorSelect) ;//40174;
				intCompressorRunHoursCompressorSelect =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorRunHoursCompressorSelect) ;//40175;
				intEvaporatorPumpRunHoursPumpSelect =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posEvaporatorPumpRunHoursPumpSelect) ;//40176;
				intCondenserPumpRunHoursPumpSelect =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCondenserPumpRunHoursPumpSelect) ;//40177;
				return true;
			}
			
			if (idx == 3) {
				intEvaporatorPumpRunHoursPump2 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posEvaporatorPumpRunHoursPump2) ;//40232;
				intCondenserPumpRunHoursPump2 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCondenserPumpRunHoursPump2) ;//40233;
				intCompressorStartsCompressor2  =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorStartsCompressor2) ;//40235;
				intCompressorRunHoursCompressor2 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorRunHoursCompressor2) ;//40236;
				intCompressorStartsCompressor3 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorStartsCompressor3) ;//40238;
				intCompressorRunHoursCompressor3 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorRunHoursCompressor3) ;//40239;
				intCompressorStartsCompressor4 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorStartsCompressor4) ;//40241;
				intCompressorRunHoursCompressor4 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posCompressorRunHoursCompressor4) ;//40242;
				intChillerPower =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posChillerPower) ;//40245;
				intMaximumRPMCompressor1 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posMaximumRPMCompressor1) ;//40246;
				intActualRPMCompressor1 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActualRPMCompressor1) ;//40247;
				intMinimumRPMCompressor1 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posMinimumRPMCompressor1) ;//40248;
				intIGVPercentageOpenCompressor1  =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posIGVPercentageOpenCompressor1) ;//40249;
				intMaximumRPMCompressor2 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posMaximumRPMCompressor2) ;//40250;
				intActualRPMCompressor2 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posActualRPMCompressor2) ;//40251;
				intMinimumRPMCompressor2  =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posMinimumRPMCompressor2) ;//40252;
				intIGVPercentageOpenCompressor2 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posIGVPercentageOpenCompressor2) ;//40253;
				intDesignRPMCompressor1 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posDesignRPMCompressor1) ;//40254;
				intDesignRPMCompressor2 =FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 2*posDesignRPMCompressor2) ;//40255;
				return true;
			}
			return true;
		}
		if (mode == CONFIG_MODE) {

		}

		return true;
	}

	private void calculateDecodedData() {
		calActiveSetpoint = FuncUtil.Temperature.convertFahrenheitToCelsius(intActiveSetpoint/10);
		calCavityTemperatureCompressor1 = FuncUtil.Temperature.convertFahrenheitToCelsius(intCavityTemperatureCompressor1/10);
		calCavityTemperatureCompressor2 = FuncUtil.Temperature.convertFahrenheitToCelsius(intCavityTemperatureCompressor2/10);
		calCompressorDischargeTemperatureCompressorSelect = FuncUtil.Temperature.convertFahrenheitToCelsius(intCompressorDischargeTemperatureCompressorSelect/10);
		calCompressorDischargeTemperatureCompressor2 = FuncUtil.Temperature.convertFahrenheitToCelsius(intCompressorDischargeTemperatureCompressor2/10);
		calCompressorSuctionLineTemperatureCompressorSelect = FuncUtil.Temperature.convertFahrenheitToCelsius(intCompressorSuctionLineTemperatureCompressorSelect/10);
		calCompressorSuctionLineTemperatureCompressor2 = FuncUtil.Temperature.convertFahrenheitToCelsius(intCompressorSuctionLineTemperatureCompressor2/10);
		calCondenserEnteringWaterTemperature = FuncUtil.Temperature.convertFahrenheitToCelsius(intCondenserEnteringWaterTemperature/10);
		calCondenserLeavingWaterTemperature =  FuncUtil.Temperature.convertFahrenheitToCelsius(intCondenserLeavingWaterTemperature/10);
		calCondenserSaturatedRefrigerantTemperatureCompressorSelect =  FuncUtil.Temperature.convertFahrenheitToCelsius(intCondenserSaturatedRefrigerantTemperatureCompressorSelect/10);
		calCondenserSaturatedRefrigerantTemperatureCompressor2 =  FuncUtil.Temperature.convertFahrenheitToCelsius(intCondenserSaturatedRefrigerantTemperatureCompressor2/10);
		calCoolSetpoint =  FuncUtil.Temperature.convertFahrenheitToCelsius(intCoolSetpoint/10);
		calEvaporatorEnteringWaterTemperature =  FuncUtil.Temperature.convertFahrenheitToCelsius(intEvaporatorEnteringWaterTemperature/10);
		calEvaporatorLeavingWaterTemperatureForUnit =  FuncUtil.Temperature.convertFahrenheitToCelsius(intEvaporatorLeavingWaterTemperatureForUnit/10);
		calEvaporatorLeavingWaterTemperatureForCompressorSelect =  FuncUtil.Temperature.convertFahrenheitToCelsius(intEvaporatorLeavingWaterTemperatureForCompressorSelect/10);
		calEvaporatorLeavingWaterTemperatureForCompressor2 =  FuncUtil.Temperature.convertFahrenheitToCelsius(intEvaporatorLeavingWaterTemperatureForCompressor2/10);
		calEvaporatorSaturatedRefrigerantTemperatureCompressorSelect = FuncUtil.Temperature.convertFahrenheitToCelsius(intEvaporatorSaturatedRefrigerantTemperatureCompressorSelect/10);
		calEvaporatorSaturatedRefrigerantTemperatureCompressor2 = FuncUtil.Temperature.convertFahrenheitToCelsius(intEvaporatorSaturatedRefrigerantTemperatureCompressor2/10);
		calHeatRecoveryEnteringWaterTemperature = FuncUtil.Temperature.convertFahrenheitToCelsius(intHeatRecoveryEnteringWaterTemperature/10);
		calHeatRecoveryLeavingWaterTemperature = FuncUtil.Temperature.convertFahrenheitToCelsius(intHeatRecoveryLeavingWaterTemperature/10);
		calHeatSetpoint =  FuncUtil.Temperature.convertFahrenheitToCelsius(intHeatSetpoint/10);
		calIceSetpoint =  FuncUtil.Temperature.convertFahrenheitToCelsius(intIceSetpoint/10);
		calInverterTemperatureCompressor1 =  FuncUtil.Temperature.convertFahrenheitToCelsius(intInverterTemperatureCompressor1/10);
		calInverterTemperatureCompressor2 =  FuncUtil.Temperature.convertFahrenheitToCelsius(intInverterTemperatureCompressor2/10);
		calLiquidLineRefrigerantTemperatureCompressorSelect =  FuncUtil.Temperature.convertFahrenheitToCelsius(intLiquidLineRefrigerantTemperatureCompressorSelect/10);
		calLiquidLineRefrigerantTemperatureCompressor2 =  FuncUtil.Temperature.convertFahrenheitToCelsius(intLiquidLineRefrigerantTemperatureCompressor2/10);
		calOilFeedTemperatureCompressorSelect = FuncUtil.Temperature.convertFahrenheitToCelsius(intOilFeedTemperatureCompressorSelect/10);
		calOilFeedTemperatureCompressor2 = FuncUtil.Temperature.convertFahrenheitToCelsius(intOilFeedTemperatureCompressor2/10);
		calOilSumpTemperatureCompressorSelect = FuncUtil.Temperature.convertFahrenheitToCelsius(intOilSumpTemperatureCompressorSelect/10);
		calOilSumpTemperatureCompressor2 = FuncUtil.Temperature.convertFahrenheitToCelsius(intOilSumpTemperatureCompressor2/10);
		calOutdoorAirTemperature = FuncUtil.Temperature.convertFahrenheitToCelsius(intOutdoorAirTemperature/10);
				
		calCompressorRunHoursCompressorSelect = intCompressorRunHoursCompressorSelect/10.0;
		calCompressorRunHoursCompressor2 = intCompressorRunHoursCompressor2/10.0;
		calCompressorRunHoursCompressor3 = intCompressorRunHoursCompressor3/10.0;
		calCompressorRunHoursCompressor4 = intCompressorRunHoursCompressor4/10.0;
		calCondenserWaterFlowRate = intCondenserWaterFlowRate*0.063;
		calEvaporatorWaterFlowRate = intEvaporatorWaterFlowRate*0.063;
		
		calChillerEnableInput = (boolChillerEnableInput ? 1 : 0);
		calRunEnabled = (boolRunEnabled ? 1 : 0);
		calAlarmDigitalOutput = (boolAlarmDigitalOutput ? 1 : 0);
		calChillerLocal_Remote = (boolChillerLocal_Remote ? 1 : 0);
		calChillerLimited = (boolChillerLimited ? 1 : 0);
		calEvaporatorFlowSwitchStatus = (boolEvaporatorFlowSwitchStatus ? 1 : 0);
		calCondenserFlowSwitchStatus = (boolCondenserFlowSwitchStatus ? 1 : 0);
		calClearAlarms = (boolClearAlarms ? 1 : 0);
		calPumpSelect = (boolPumpSelect ? 1 : 0);
		calEvaporatorWaterPumpStatus = (boolEvaporatorWaterPumpStatus ? 1 : 0);
		calCondenserWaterPumpStatus = (boolCondenserWaterPumpStatus ? 1 : 0);
		
		intActualCapacity = intActualCapacity*10;
		intCapacityLimitOutput = intCapacityLimitOutput * 10;
		intCapacityLimitSetpoint = intCapacityLimitSetpoint*10;

		intCompressorPercentRLACompressorSelect = intCompressorPercentRLACompressorSelect*10;
		intCompressorPercentRLACompressor2 = intCompressorPercentRLACompressor2*10;
		intCompressorPercentRLACompressor3 = intCompressorPercentRLACompressor3*10;
		intCompressorPercentRLACompressor4 = intCompressorPercentRLACompressor4*10;
		intCompressorPercentRLACompressor5 = intCompressorPercentRLACompressor5*10;
		intCompressorPercentRLACompressor6 = intCompressorPercentRLACompressor6*10;

		intCondenserRefrigerantPressureCompressorSelect = intCondenserRefrigerantPressureCompressorSelect*10;
		intCondenserRefrigerantPressureCompressor2 = intCondenserRefrigerantPressureCompressor2*10;

		intEvaporatorRefrigerantPressureCompressorSelect = intEvaporatorRefrigerantPressureCompressorSelect*10;
		intEvaporatorRefrigerantPressureCompressor2 = intEvaporatorRefrigerantPressureCompressor2*10;

		intLiquidLineRefrigerantPressure = intLiquidLineRefrigerantPressure*10;

		intOilSumpPressureCompressorSelect = intOilSumpPressureCompressorSelect*10;
		intOilSumpPressureCompressor2 = intOilSumpPressureCompressor2*10;
	
	}
	
	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuilder data = new StringBuilder();
		// System
		data.append("|DEVICEID=" + getId() + "-0-0");
		data.append(";TIMESTAMP=" + timestamp);
		
		data.append(";Active Setpoint=" + vformat.format(calActiveSetpoint) + ",C"); 
		data.append(";Actual Capacity=" + intActualCapacity + ",%"); 
		data.append(";Actual RPM Compressor 1=" + intActualRPMCompressor1 + ",rpm"); 
		data.append(";Actual RPM Compressor 2=" + intActualRPMCompressor2 + ",rpm"); 
		data.append(";Capacity Limit Output=" + intCapacityLimitOutput + ",%"); 
		data.append(";Capacity Limit Setpoint=" + intCapacityLimitSetpoint + ",%"); 
		data.append(";Cavity Temperature Compressor 1=" + vformat.format(calCavityTemperatureCompressor1) + ",C"); 
		data.append(";Cavity Temperature Compressor 2=" + vformat.format(calCavityTemperatureCompressor2) + ",C"); 
		data.append(";Chiller Enable Input=" + calChillerEnableInput + ",None"); 
		data.append(";Chiller Limited=" + calChillerLimited + ",None"); 
		data.append(";Chiller Local/Remote=" + calChillerLocal_Remote + ",None"); 
		data.append(";Chiller Mode Output=" + intChillerModeOutput + ",None"); 
		data.append(";Chiller Mode Setpoint=" + intChillerModeSetpoint + ",None"); 
		data.append(";Compressor 2 Active Capacity Limit=" + intCompressor2ActiveCapacityLimit + ",%"); 
		data.append(";Chiller Status=" + intChillerStatus + ",None"); 
		data.append(";Chiller Power=" + intChillerPower + ",kW"); 
		data.append(";Compressor Current Compressor Select=" + intCompressorCurrentCompressorSelect + ",A"); 
		data.append(";Compressor Current Compressor 2=" + intCompressorCurrentCompressor2 + ",A"); 
		data.append(";Compressor Current Compressor 3=" + intCompressorCurrentCompressor3 + ",A"); 
		data.append(";Compressor Current Compressor 4=" + intCompressorCurrentCompressor4 + ",A"); 
		data.append(";Compressor Current Compressor 5=" + intCompressorCurrentCompressor5 + ",A"); 
		data.append(";Compressor Current Compressor 6=" + intCompressorCurrentCompressor6 + ",A"); 
		data.append(";Compressor Discharge Temperature Compressor Select=" + vformat.format(calCompressorDischargeTemperatureCompressorSelect) + ",C"); 
		data.append(";Compressor Discharge Temperature Compressor 2=" + vformat.format(calCompressorDischargeTemperatureCompressor2) + ",C"); 
		data.append(";Compressor Percent RLA Compressor Select=" + intCompressorPercentRLACompressorSelect + ",%"); 
		data.append(";Compressor Percent RLA Compressor 2=" + intCompressorPercentRLACompressor2 + ",%"); 
		data.append(";Compressor Percent RLA Compressor 3=" + intCompressorPercentRLACompressor3 + ",%"); 
		data.append(";Compressor Percent RLA Compressor 4=" + intCompressorPercentRLACompressor4 + ",%"); 
		data.append(";Compressor Percent RLA Compressor 5=" + intCompressorPercentRLACompressor5 + ",%"); 
		data.append(";Compressor Percent RLA Compressor 6=" + intCompressorPercentRLACompressor6 + ",%"); 
		data.append(";Compressor Power Compressor Select=" + intCompressorPowerCompressorSelect + ",kW"); 
		data.append(";Compressor Power Compressor 2=" + intCompressorPowerCompressor2 + ",kW"); 
		data.append(";Compressor Power Compressor 3=" + intCompressorPowerCompressor3 + ",kW"); 
		data.append(";Compressor Power Compressor 4=" + intCompressorPowerCompressor4 + ",kW"); 
		data.append(";Compressor Power Compressor 5=" + intCompressorPowerCompressor5 + ",kW"); 
		data.append(";Compressor Power Compressor 6=" + intCompressorPowerCompressor6 + ",kW"); 
		data.append(";Compressor Run Hours Compressor Select=" + vformat.format(calCompressorRunHoursCompressorSelect) + ",None"); 
		data.append(";Compressor Run Hours Compressor 2=" + vformat.format(calCompressorRunHoursCompressor2) + ",None"); 
		data.append(";Compressor Run Hours Compressor 3=" + vformat.format(calCompressorRunHoursCompressor3) + ",None"); 
		data.append(";Compressor Run Hours Compressor 4=" + vformat.format(calCompressorRunHoursCompressor4) + ",None"); 
		data.append(";Compressor Select=" + intCompressorSelect + ",None"); 
		data.append(";Compressor Starts Compressor Select=" + intCompressorStartsCompressorSelect + ",None"); 
		data.append(";Compressor Starts Compressor 2=" + intCompressorStartsCompressor2 + ",None"); 
		data.append(";Compressor Starts Compressor 3=" + intCompressorStartsCompressor3 + ",None"); 
		data.append(";Compressor Starts Compressor 4=" + intCompressorStartsCompressor4 + ",None"); 
		data.append(";Compressor Suction Line Temperature Compressor Select=" + vformat.format(calCompressorSuctionLineTemperatureCompressorSelect) + ",C"); 
		data.append(";Compressor Suction Line Temperature Compressor 2=" + vformat.format(calCompressorSuctionLineTemperatureCompressor2) + ",C"); 
		data.append(";Compressor Voltage Compressor Select=" + intCompressorVoltageCompressorSelect + ",V"); 
		data.append(";Compressor Voltage Compressor 2=" + intCompressorVoltageCompressor2 + ",V"); 
		data.append(";Compressor Voltage Compressor 3=" + intCompressorVoltageCompressor3 + ",V"); 
		data.append(";Compressor Voltage Compressor 4=" + intCompressorVoltageCompressor4 + ",V"); 
		data.append(";Compressor Voltage Compressor 5=" + intCompressorVoltageCompressor5 + ",V"); 
		data.append(";Compressor Voltage Compressor 6=" + intCompressorVoltageCompressor6 + ",V"); 
		data.append(";Condenser Entering Water Temperature=" + vformat.format(calCondenserEnteringWaterTemperature) + ",C"); 
		data.append(";Condenser Flow Switch Status=" + calCondenserFlowSwitchStatus + ",None"); 
		data.append(";Condenser Leaving Water Temperature=" + vformat.format(calCondenserLeavingWaterTemperature) + ",C"); 
		data.append(";Condenser Pump Run Hours Pump Select=" + intCondenserPumpRunHoursPumpSelect + ",None"); 
		data.append(";Condenser Pump Run Hours Pump 2=" + intCondenserPumpRunHoursPump2 + ",None"); 
		data.append(";Condenser Refrigerant Pressure Compressor Select=" + intCondenserRefrigerantPressureCompressorSelect + ",psi"); 
		data.append(";Condenser Refrigerant Pressure Compressor 2=" + intCondenserRefrigerantPressureCompressor2 + ",psi"); 
		data.append(";Condenser Saturated Refrigerant Temperature Compressor Select=" + vformat.format(calCondenserSaturatedRefrigerantTemperatureCompressorSelect) + ",C"); 
		data.append(";Condenser Saturated Refrigerant Temperature Compressor 2=" + vformat.format(calCondenserSaturatedRefrigerantTemperatureCompressor2) + ",C"); 
		data.append(";Condenser Water Flow Rate=" + vformat.format(calCondenserWaterFlowRate) + ",l/s"); 
		data.append(";Design RPM Compressor 1=" + intDesignRPMCompressor1 + ",rpm"); 
		data.append(";Design RPM Compressor 2=" + intDesignRPMCompressor2 + ",rpm"); 
		data.append(";IGV Percentage Open Compressor 1=" + intIGVPercentageOpenCompressor1 + ",%"); 
		data.append(";IGV Percentage Open Compressor 2=" + intIGVPercentageOpenCompressor2 + ",%"); 
		data.append(";Inverter Temperature Compressor 1=" + vformat.format(calInverterTemperatureCompressor1) + ",C"); 
		data.append(";Inverter Temperature Compressor 2=" + vformat.format(calInverterTemperatureCompressor2) + ",C"); 
		data.append(";Maximum RPM Compressor 1=" + intMaximumRPMCompressor1 + ",rpm"); 
		data.append(";Maximum RPM Compressor 2=" + intMaximumRPMCompressor2 + ",rpm"); 
		data.append(";Minimum RPM Compressor 1=" + intMinimumRPMCompressor1 + ",rpm"); 
		data.append(";Minimum RPM Compressor 2=" + intMinimumRPMCompressor2 + ",rpm"); 
		data.append(";Condenser Water Pump Status=" + calCondenserWaterPumpStatus + ",None"); 
		data.append(";Cool Setpoint=" + vformat.format(calCoolSetpoint) + ",C"); 
		data.append(";Evaporator Entering Water Temperature=" + vformat.format(calEvaporatorEnteringWaterTemperature) + ",C"); 
		data.append(";Evaporator Flow Switch Status=" + calEvaporatorFlowSwitchStatus + ",None"); 
		data.append(";Evaporator Leaving Water Temperature For Unit=" + vformat.format(calEvaporatorLeavingWaterTemperatureForUnit) + ",C"); 
		data.append(";Evaporator Leaving Water Temperature For Compressor Select=" + vformat.format(calEvaporatorLeavingWaterTemperatureForCompressorSelect) + ",C"); 
		data.append(";Evaporator Leaving Water Temperature For Compressor 2=" + vformat.format(calEvaporatorLeavingWaterTemperatureForCompressor2) + ",C"); 
		data.append(";Evaporator Pump Run Hours Pump Select=" + intEvaporatorPumpRunHoursPumpSelect + ",None"); 
		data.append(";Evaporator Pump Run Hours Pump 2=" + intEvaporatorPumpRunHoursPump2 + ",None"); 
		data.append(";Evaporator Refrigerant Pressure Compressor Select=" + intEvaporatorRefrigerantPressureCompressorSelect + ",psi"); 
		data.append(";Evaporator Refrigerant Pressure Compressor 2=" + intEvaporatorRefrigerantPressureCompressor2 + ",psi"); 
		data.append(";Evaporator Saturated Refrigerant Temperature Compressor Select=" + vformat.format(calEvaporatorSaturatedRefrigerantTemperatureCompressorSelect) + ",C"); 
		data.append(";Evaporator Saturated Refrigerant Temperature Compressor 2=" + vformat.format(calEvaporatorSaturatedRefrigerantTemperatureCompressor2) + ",C"); 
		data.append(";Evaporator Water Flow Rate=" + vformat.format(calEvaporatorWaterFlowRate) + ",l/s"); 
		data.append(";Evaporator Water Pump Status=" + calEvaporatorWaterPumpStatus + ",None"); 
		data.append(";Heat Recovery Entering Water Temperature=" + vformat.format(calHeatRecoveryEnteringWaterTemperature) + ",C"); 
		data.append(";Heat Recovery Leaving Water Temperature=" + vformat.format(calHeatRecoveryLeavingWaterTemperature) + ",C"); 
		data.append(";Heat Setpoint=" + vformat.format(calHeatSetpoint) + ",C"); 
		data.append(";Ice Setpoint=" + vformat.format(calIceSetpoint) + ",C"); 
		data.append(";Liquid Line Refrigerant Pressure=" + intLiquidLineRefrigerantPressure + ",psi"); 
		data.append(";Liquid Line Refrigerant Temperature Compressor Select=" + vformat.format(calLiquidLineRefrigerantTemperatureCompressorSelect) + ",C"); 
		data.append(";Liquid Line Refrigerant Temperature Compressor 2=" + vformat.format(calLiquidLineRefrigerantTemperatureCompressor2) + ",C"); 
		data.append(";Oil Feed Pressure Compressor Select=" + intOilFeedPressureCompressorSelect + ",psi"); 
		data.append(";Oil Feed Pressure Compressor 2=" + intOilFeedPressureCompressor2 + ",psi"); 
		data.append(";Oil Feed Temperature Compressor Select=" + vformat.format(calOilFeedTemperatureCompressorSelect) + ",C"); 
		data.append(";Oil Feed Temperature Compressor 2=" + vformat.format(calOilFeedTemperatureCompressor2) + ",C"); 
		data.append(";Oil Sump Pressure Compressor Select=" + intOilSumpPressureCompressorSelect + ",psi"); 
		data.append(";Oil Sump Pressure Compressor 2=" + intOilSumpPressureCompressor2 + ",psi"); 
		data.append(";Oil Sump Temperature Compressor Select=" + vformat.format(calOilSumpTemperatureCompressorSelect) + ",C"); 
		data.append(";Oil Sump Temperature Compressor 2=" + vformat.format(calOilSumpTemperatureCompressor2) + ",C"); 
		data.append(";Outdoor Air Temperature=" + vformat.format(calOutdoorAirTemperature) + ",C"); 
		data.append(";Pump Select=" + calPumpSelect + ",None"); 
		data.append(";Run Enabled=" + calRunEnabled + ",None"); 
		data.append(";Alarm Digital Output=" + calAlarmDigitalOutput + ",None"); 
		data.append(";Clear Alarms=" + calClearAlarms + ",None");
		return data.toString();
	}
	
}
