package com.greenkoncepts.gateway.adapter.energetix.powermeter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.FuncUtil;

public class ISASTMultiGEM18 extends EcosailDevice {
	public final static int MBREG_DATA_START_GENERAL  = 8;
	
	public final static int MBREG_DATA_START_FEEDER12  = 230;
	public final static int MBREG_DATA_START_FEEDER34  = 350;
	public final static int MBREG_DATA_START_FEEDER56  = 470;
	public final static int MBREG_DATA_START_ENERGY  = 8022;
	
	public static final int MBREG_DATA_NUM_FEEDER = 120;		
	public static final int MBREG_DATA_NUM_ENERGY = 108;
	
	final static int CHANNEL_NUM = 6;
	
	private double temperature_1 = 0;
	private double temperature_2 = 0;
	private double frequency = 0;
	private StringBuilder data = new StringBuilder();
	

	private Feeder[] feeder ;
	public ISASTMultiGEM18(int addr, String category) {
		super(category, addr);
		// TODO Auto-generated constructor stub
		feeder = new Feeder[CHANNEL_NUM];
		for (int i = 0; i < CHANNEL_NUM; i++) {
			feeder[i] = new Feeder(i);
		}
	}

	@Override
	public String getDeviceData() {
		byte[] data = modbus.readInputRegisters(modbusid, MBREG_DATA_START_GENERAL, 3);
		decodingData(0, data, DATA_MODE);
		
		data = modbus.readInputRegisters(modbusid, MBREG_DATA_START_FEEDER12, MBREG_DATA_NUM_FEEDER);
		decodingData(1, data, DATA_MODE);
		
		data = modbus.readInputRegisters(modbusid, MBREG_DATA_START_FEEDER34, MBREG_DATA_NUM_FEEDER);
		decodingData(2, data, DATA_MODE);
		
		data = modbus.readInputRegisters(modbusid, MBREG_DATA_START_FEEDER56, MBREG_DATA_NUM_FEEDER);
		decodingData(3, data, DATA_MODE);
		
		data = modbus.readInputRegisters(modbusid, MBREG_DATA_START_ENERGY, MBREG_DATA_NUM_ENERGY);
		decodingData(4, data, DATA_MODE);
		
		
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
			byte[] data = modbus.readInputRegisters(modbusid, MBREG_DATA_START_ENERGY, MBREG_DATA_NUM_ENERGY);
			decodingData(4, data, DATA_MODE);
			
			if ((dataIdx == 0) || (dataIdx == 1)) {
				data = modbus.readInputRegisters(modbusid, MBREG_DATA_START_FEEDER12, MBREG_DATA_NUM_FEEDER);
				decodingData(1, data, DATA_MODE);
			} else if ((dataIdx == 2) || (dataIdx == 3)) {
				data = modbus.readInputRegisters(modbusid, MBREG_DATA_START_FEEDER34, MBREG_DATA_NUM_FEEDER);
				decodingData(2, data, DATA_MODE);
			} else if ((dataIdx == 4) || (dataIdx == 5)) {
				data = modbus.readInputRegisters(modbusid, MBREG_DATA_START_FEEDER56, MBREG_DATA_NUM_FEEDER);
				decodingData(3, data, DATA_MODE);
			}
			
		}
		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			for (int j = 0; j < CHANNEL_NUM; j++) {
				real_time_data.add(feeder[j].getRealTimeData());
			}
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
				temperature_1 = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA) * ((double) 0.1);
				temperature_2 = FuncUtil.RegisterBigEndian.registerToShort(data, OFFSET_DATA + 2) * ((double) 0.1);
				frequency = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA + 4) * ((double) 0.01);
			} else if (idx == 1) {
				feeder[0].parseCommonValue(data, 0);
				feeder[1].parseCommonValue(data, 120);
			} else if (idx == 2) {
				feeder[2].parseCommonValue(data, 0);
				feeder[3].parseCommonValue(data, 120);
			} else if (idx == 3) {
				feeder[4].parseCommonValue(data, 0);
				feeder[5].parseCommonValue(data, 120);
			} else if (idx == 4) {
				feeder[0].parseEnergyValue(data, 0);
				feeder[1].parseEnergyValue(data, 36);
				feeder[2].parseEnergyValue(data, 72);
				feeder[3].parseEnergyValue(data, 108);
				feeder[4].parseEnergyValue(data, 144);
				feeder[5].parseEnergyValue(data, 180);
				
			} 
		}
		return true;

	}
	
	private String createDataSendToServer() {
		data.setLength(0);
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		
		timestamp  = System.currentTimeMillis();
		for (int i = 0; i < CHANNEL_NUM; i++) {
			data.append(feeder[i].getFeederStringValue());
		}
		return data.toString();
	}
	
	class Feeder {
		public static final int Offset_L1_Voltage = 2*0;
		public static final int Offset_L1_Current = 2*2;
		public static final int Offset_L1_ActivePower = 2*4;
		public static final int Offset_L1_ReactivePower = 2*6;
		public static final int Offset_L1_ApparentPower = 2*8;
		public static final int Offset_L1_Angle = 2*11;
		public static final int Offset_L1_PowerFactor = 2*12;
		
		public static final int Offset_L2_Voltage = 2*14;
		public static final int Offset_L2_Current = 2*16;
		public static final int Offset_L2_ActivePower = 2*18;
		public static final int Offset_L2_ReactivePower = 2*20;
		public static final int Offset_L2_ApparentPower = 2*22;
		public static final int Offset_L2_Angle = 2*25;
		public static final int Offset_L2_PowerFactor = 2*26;
		
		public static final int Offset_L3_Voltage = 2*28;
		public static final int Offset_L3_Current = 2*30;
		public static final int Offset_L3_ActivePower = 2*32;
		public static final int Offset_L3_ReactivePower = 2*34;
		public static final int Offset_L3_ApparentPower = 2*36;
		public static final int Offset_L3_Angle = 2*39;
		public static final int Offset_L3_PowerFactor = 2*40;
		
		public static final int Offset_Sys_Current = 2*42;
		public static final int Offset_Sys_ActivePower = 2*44;
		public static final int Offset_Sys_ReactivePower = 2*46;
		public static final int Offset_Sys_ApparentPower = 2*48;
		public static final int Offset_Sys_PowerFactor = 2*51;
		
		public static final int Offset_Cumulated_Active_Energy = 2*0;
		public static final int Offset_Cumulated_Reactive_Energy = 2*6;
		public static final int Offset_Cumulated_Apparent_Energy = 2*12;
		
		
		private int channel = 0;
		
		private double data_L1_Voltage = 0;
		private double data_L1_Current = 0;
		private double data_L1_ActivePower = 0;
		private double data_L1_ReactivePower = 0;
		private double data_L1_ApparentPower = 0;
		private double data_L1_Angle = 0;
		private double data_L1_PowerFactor = 0;
				
		private double data_L2_Voltage = 0;
		private double data_L2_Current = 0;
		private double data_L2_ActivePower = 0;
		private double data_L2_ReactivePower = 0;
		private double data_L2_ApparentPower = 0;
		private double data_L2_Angle = 0;
		private double data_L2_PowerFactor = 0;
				
		private double data_L3_Voltage = 0;
		private double data_L3_Current = 0;
		private double data_L3_ActivePower = 0;
		private double data_L3_ReactivePower = 0;
		private double data_L3_ApparentPower = 0;
		private double data_L3_Angle = 0;
		private double data_L3_PowerFactor = 0;
				
		private double data_Sys_Current = 0;
		private double data_Sys_ActivePower = 0;
		private double data_Sys_ReactivePower = 0;
		private double data_Sys_ApparentPower = 0;
		private double data_Sys_PowerFactor = 0;
				
		private double data_Cumulated_Active_Energy = 0;
		private double data_Cumulated_Reactive_Energy = 0;
		private double data_Cumulated_Apparent_Energy = 0;
		
		private double data_Previous_Active_Energy = 0;
		private double data_Previous_Reactive_Energy = 0;
		private double data_Previous_Apparent_Energy = 0;
		
		private double data_Consumed_Active_Energy = 0;
		private double data_Consumed_Reactive_Energy = 0;
		private double data_Consumed_Apparent_Energy = 0;
		
		private StringBuilder dataFeeder = new StringBuilder();
		
		public Feeder( int channel) {
			this.channel = channel;
		}
		
		public boolean parseCommonValue(byte[] rawData, int offset) {
			
			data_L1_Voltage = FuncUtil.RegisterBigEndian.registersToUint(rawData, offset + OFFSET_DATA + Offset_L1_Voltage) * ((double) 0.01);
			data_L1_Current = FuncUtil.RegisterBigEndian.registersToUint(rawData, offset + OFFSET_DATA + Offset_L1_Current) * ((double) 0.01);
			data_L1_ActivePower = FuncUtil.RegisterBigEndian.registersToInt(rawData, offset + OFFSET_DATA + Offset_L1_ActivePower);
			data_L1_ReactivePower = FuncUtil.RegisterBigEndian.registersToInt(rawData, offset + OFFSET_DATA + Offset_L1_ReactivePower);
			data_L1_ApparentPower = FuncUtil.RegisterBigEndian.registersToUint(rawData, offset + OFFSET_DATA + Offset_L1_ApparentPower);
			data_L1_Angle = FuncUtil.RegisterBigEndian.registerToUnsignedShort(rawData, offset + OFFSET_DATA + Offset_L1_Angle) * ((double) 0.01);
			data_L1_PowerFactor = FuncUtil.RegisterBigEndian.registerToShort(rawData, offset + OFFSET_DATA + Offset_L1_PowerFactor) * ((double) 0.01);
							
			data_L2_Voltage = FuncUtil.RegisterBigEndian.registersToUint(rawData, offset + OFFSET_DATA + Offset_L2_Voltage) * ((double) 0.01);
			data_L2_Current = FuncUtil.RegisterBigEndian.registersToUint(rawData, offset + OFFSET_DATA + Offset_L2_Current) * ((double) 0.01);
			data_L2_ActivePower = FuncUtil.RegisterBigEndian.registersToInt(rawData, offset + OFFSET_DATA + Offset_L2_ActivePower);
			data_L2_ReactivePower = FuncUtil.RegisterBigEndian.registersToInt(rawData, offset + OFFSET_DATA + Offset_L2_ReactivePower);
			data_L2_ApparentPower = FuncUtil.RegisterBigEndian.registersToUint(rawData, offset + OFFSET_DATA + Offset_L2_ApparentPower);
			data_L2_Angle = FuncUtil.RegisterBigEndian.registerToUnsignedShort(rawData, offset + OFFSET_DATA + Offset_L2_Angle) * ((double) 0.01);
			data_L2_PowerFactor =  FuncUtil.RegisterBigEndian.registerToShort(rawData, offset + OFFSET_DATA + Offset_L2_PowerFactor) * ((double) 0.01);
							
			data_L3_Voltage = FuncUtil.RegisterBigEndian.registersToUint(rawData, offset + OFFSET_DATA + Offset_L3_Voltage) * ((double) 0.01);
			data_L3_Current = FuncUtil.RegisterBigEndian.registersToUint(rawData, offset + OFFSET_DATA + Offset_L3_Current) * ((double) 0.01);
			data_L3_ActivePower = FuncUtil.RegisterBigEndian.registersToInt(rawData, offset + OFFSET_DATA + Offset_L3_ActivePower);
			data_L3_ReactivePower = FuncUtil.RegisterBigEndian.registersToInt(rawData, offset + OFFSET_DATA + Offset_L3_ReactivePower);
			data_L3_ApparentPower = FuncUtil.RegisterBigEndian.registersToUint(rawData, offset + OFFSET_DATA + Offset_L3_ApparentPower);
			data_L3_Angle = FuncUtil.RegisterBigEndian.registerToUnsignedShort(rawData, offset + OFFSET_DATA + Offset_L3_Angle) * ((double) 0.01);
			data_L3_PowerFactor =  FuncUtil.RegisterBigEndian.registerToShort(rawData, offset + OFFSET_DATA + Offset_L3_PowerFactor) * ((double) 0.01);
							
			data_Sys_Current = FuncUtil.RegisterBigEndian.registersToUint(rawData, offset + OFFSET_DATA + Offset_Sys_Current) * ((double) 0.01);
			data_Sys_ActivePower = FuncUtil.RegisterBigEndian.registersToInt(rawData, offset + OFFSET_DATA + Offset_Sys_ActivePower);
			data_Sys_ReactivePower = FuncUtil.RegisterBigEndian.registersToInt(rawData, offset + OFFSET_DATA + Offset_Sys_ReactivePower);
			data_Sys_ApparentPower = FuncUtil.RegisterBigEndian.registersToUint(rawData, offset + OFFSET_DATA + Offset_Sys_ApparentPower);
			data_Sys_PowerFactor = FuncUtil.RegisterBigEndian.registerToShort(rawData, offset + OFFSET_DATA + Offset_Sys_PowerFactor) * ((double) 0.01);
			
			return true;
		}
		
		public boolean parseEnergyValue(byte[] rawData, int offset) {
			
			data_Cumulated_Active_Energy = FuncUtil.RegisterBigEndian.registersToUint(rawData, offset + OFFSET_DATA + Offset_Cumulated_Active_Energy) * ((double) 0.1);
			data_Cumulated_Reactive_Energy = FuncUtil.RegisterBigEndian.registersToUint(rawData, offset + OFFSET_DATA + Offset_Cumulated_Reactive_Energy) * ((double) 0.1);
			data_Cumulated_Apparent_Energy = FuncUtil.RegisterBigEndian.registersToUint(rawData, offset + OFFSET_DATA + Offset_Cumulated_Apparent_Energy) * ((double) 0.1);
			
			if (data_Previous_Active_Energy == 0) {
				data_Consumed_Active_Energy = 0;
			} else if (data_Previous_Active_Energy > data_Cumulated_Active_Energy) {
				data_Consumed_Active_Energy = 0;
			} else {
				data_Consumed_Active_Energy = 1000 * (data_Cumulated_Active_Energy - data_Previous_Active_Energy);
			}
			data_Previous_Active_Energy= data_Cumulated_Active_Energy;
			
			if (data_Previous_Reactive_Energy == 0) {
				data_Consumed_Reactive_Energy = 0;
			} else if (data_Previous_Reactive_Energy > data_Cumulated_Reactive_Energy) {
				data_Consumed_Reactive_Energy = 0;
			} else {
				data_Consumed_Reactive_Energy = 1000 * (data_Cumulated_Reactive_Energy - data_Previous_Reactive_Energy);
			}
			data_Previous_Reactive_Energy = data_Cumulated_Reactive_Energy;
			
			if (data_Previous_Apparent_Energy == 0) {
				data_Consumed_Apparent_Energy = 0;
			} else if (data_Previous_Apparent_Energy > data_Cumulated_Apparent_Energy) {
				data_Consumed_Apparent_Energy = 0;
			} else {
				data_Consumed_Apparent_Energy = 1000 * (data_Cumulated_Apparent_Energy - data_Previous_Apparent_Energy);
			}
			data_Previous_Apparent_Energy = data_Cumulated_Apparent_Energy;
			
			return true;
		}
		
		public Map<String, String> getRealTimeData() {
			Map<String, String> item = new HashMap<String, String>();
			
			String chan = "ch" + channel + "_";
			item.put(chan + "Sys_Active_Energy_Reading" ,vformat.format(data_Cumulated_Active_Energy));
			item.put(chan + "Sys_Reactive_Energy_Reading" ,vformat.format(data_Cumulated_Reactive_Energy));
			item.put(chan + "Sys_Apparent_Energy_Reading" ,vformat.format(data_Cumulated_Apparent_Energy));
			item.put(chan + "Sys_Active_Power" ,vformat.format(Math.abs(data_Sys_ActivePower/1000)));
			item.put(chan + "Sys_Reactive_Power", vformat.format(Math.abs(data_Sys_ReactivePower/1000)));
			item.put(chan + "Sys_Apparent_Power" ,vformat.format(data_Sys_ApparentPower/1000));
			item.put(chan + "Sys_Power_Factor" ,vformat.format(data_Sys_PowerFactor));
			item.put(chan + "Sys_Voltage" ,vformat.format(data_L1_Voltage));
			item.put(chan + "Sys_Current" ,vformat.format(Math.abs(data_Sys_Current)));
			item.put(chan + "Sys_Temperature1" ,vformat.format(temperature_1));
			item.put(chan + "Sys_Temperature2" ,vformat.format(temperature_2));
			item.put(chan + "Sys_Frequency" ,vformat.format(frequency));
						
						// Phase L1
			item.put(chan + "L1_Active_Power" ,vformat.format(Math.abs(data_L1_ActivePower/1000)));
			item.put(chan + "L1_Reactive_Power" ,vformat.format(Math.abs(data_L1_ReactivePower/1000)));
			item.put(chan + "L1_Apparent_Power" ,vformat.format(data_L1_ApparentPower/1000));
			item.put(chan + "L1_Power_Factor" ,vformat.format(data_L1_PowerFactor));
			item.put(chan + "L1_Voltage" ,vformat.format(data_L1_Voltage));
			item.put(chan + "L1_Current" ,vformat.format(data_L1_Current));
			item.put(chan + "L1_Frequency" ,vformat.format(frequency));

						// Phase L2
			item.put(chan + "L2_Active_Power" ,vformat.format(Math.abs(data_L2_ActivePower/1000)));
			item.put(chan + "L2_Reactive_Power" ,vformat.format(Math.abs(data_L2_ReactivePower/1000)));
			item.put(chan + "L2_Apparent_Power" ,vformat.format(data_L2_ApparentPower/1000));
			item.put(chan + "L2_Power_Factor" ,vformat.format(data_L2_PowerFactor));
			item.put(chan + "L2_Voltage" ,vformat.format(data_L2_Voltage));
			item.put(chan + "L2_Current" ,vformat.format(data_L2_Current));
			item.put(chan + "L2_Frequency" ,vformat.format(frequency));

						// Phase L3
			item.put(chan + "L3_Active_Power" ,vformat.format(Math.abs(data_L3_ActivePower/1000)));
			item.put(chan + "L3_Reactive_Power" ,vformat.format(Math.abs(data_L3_ReactivePower/1000)));
			item.put(chan + "L3_Apparent_Power" ,vformat.format(data_L3_ApparentPower/1000));
			item.put(chan + "L3_Power_Factor" ,vformat.format(data_L3_PowerFactor));
			item.put(chan + "L3_Voltage" ,vformat.format(data_L3_Voltage));
			item.put(chan + "L3_Current" ,vformat.format(data_L3_Current));
			item.put(chan + "L3_Frequency" ,vformat.format(frequency));
			return item;
		}
		
		public String getFeederStringValue() {
			dataFeeder.setLength(0);
			// System
			dataFeeder.append("|DEVICEID=" + getId() + "-" + channel + "-0");
			dataFeeder.append(";TIMESTAMP=" + timestamp);
			dataFeeder.append(";Active Energy Reading=" + vformat.format(data_Cumulated_Active_Energy) + ",kWh");
			dataFeeder.append(";Reactive Energy Reading=" + vformat.format(data_Cumulated_Reactive_Energy) + ",kVARh");
			dataFeeder.append(";Apparent Energy Reading=" + vformat.format(data_Cumulated_Apparent_Energy) + ",kVAh");
			dataFeeder.append(";Active Energy=" + vformat.format(data_Consumed_Active_Energy) + ",Wh");
			dataFeeder.append(";Reactive Energy=" + vformat.format(data_Consumed_Reactive_Energy) + ",VARh");
			dataFeeder.append(";Apparent Energy=" + vformat.format(data_Consumed_Apparent_Energy) + ",VAh");
			dataFeeder.append(";Active Power=" + vformat.format(Math.abs(data_Sys_ActivePower/1000)) + ",kW");
			dataFeeder.append(";Reactive Power="+ vformat.format(Math.abs(data_Sys_ReactivePower/1000)) + ",kVAR");
			dataFeeder.append(";Apparent Power=" + vformat.format(data_Sys_ApparentPower/1000) + ",kVA");
			dataFeeder.append(";Power Factor=" + vformat.format(data_Sys_PowerFactor) + ",None");
			dataFeeder.append(";Voltage=" + vformat.format(data_L1_Voltage) + ",V");
			dataFeeder.append(";Current=" + vformat.format(Math.abs(data_Sys_Current)) + ",A");
			dataFeeder.append(";Temperature 1=" + vformat.format(temperature_1) + ",C");
			dataFeeder.append(";Temperature 2=" + vformat.format(temperature_2) + ",C");
			dataFeeder.append(";Frequency=" + vformat.format(frequency) + ",Hz");
			
			// Phase L1
			dataFeeder.append("|DEVICEID=" + getId() + "-" + channel + "-1");
			dataFeeder.append(";TIMESTAMP=" + timestamp);
			dataFeeder.append(";Active Power=" + vformat.format(Math.abs(data_L1_ActivePower/1000)) + ",kW");
			dataFeeder.append(";Reactive Power=" + vformat.format(Math.abs(data_L1_ReactivePower/1000)) + ",kVAR");
			dataFeeder.append(";Apparent Power=" + vformat.format(data_L1_ApparentPower/1000) + ",kVA");
			dataFeeder.append(";Power Factor=" + vformat.format(data_L1_PowerFactor) + ",None");
			dataFeeder.append(";Voltage=" + vformat.format(data_L1_Voltage) + ",V");
			dataFeeder.append(";Current=" + vformat.format(data_L1_Current) + ",A");
			dataFeeder.append(";Frequency=" + vformat.format(frequency) + ",Hz");

			// Phase L2
			dataFeeder.append("|DEVICEID=" + getId() + "-" + channel + "-2");
			dataFeeder.append(";TIMESTAMP=" + timestamp);
			dataFeeder.append(";Active Power=" + vformat.format(Math.abs(data_L2_ActivePower/1000)) + ",kW");
			dataFeeder.append(";Reactive Power=" + vformat.format(Math.abs(data_L2_ReactivePower/1000)) + ",kVAR");
			dataFeeder.append(";Apparent Power=" + vformat.format(data_L2_ApparentPower/1000) + ",kVA");
			dataFeeder.append(";Power Factor=" + vformat.format(data_L2_PowerFactor) + ",None");
			dataFeeder.append(";Voltage=" + vformat.format(data_L2_Voltage) + ",V");
			dataFeeder.append(";Current=" + vformat.format(data_L2_Current) + ",A");
			dataFeeder.append(";Frequency=" + vformat.format(frequency) + ",Hz");

			// Phase L3
			dataFeeder.append("|DEVICEID=" + getId() + "-" + channel + "-3");
			dataFeeder.append(";TIMESTAMP=" + timestamp);
			dataFeeder.append(";Active Power=" + vformat.format(Math.abs(data_L3_ActivePower/1000)) + ",kW");
			dataFeeder.append(";Reactive Power=" + vformat.format(Math.abs(data_L3_ReactivePower/1000)) + ",kVAR");
			dataFeeder.append(";Apparent Power=" + vformat.format(data_L3_ApparentPower/1000) + ",kVA");
			dataFeeder.append(";Power Factor=" + vformat.format(data_L3_PowerFactor) + ",None");
			dataFeeder.append(";Voltage=" + vformat.format(data_L3_Voltage) + ",V");
			dataFeeder.append(";Current=" + vformat.format(data_L3_Current) + ",A");
			dataFeeder.append(";Frequency=" + vformat.format(frequency) + ",Hz");
			
			return dataFeeder.toString();
		}
	}

}


