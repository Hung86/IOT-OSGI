package com.greenkoncepts.gateway.adapter.kam;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.util.ModbusUtil;

public class PFM_DPM_v2 extends KamDevice {
	public static int MBREG_DATA_START = 0x300;
	public static int MBREG_DATA_NUM = 0x62;

	private int phase1current = 0;
	private int phase2current = 0;
	private int phase3current = 0;
	private int neutralcurrent = 0;
	private int phasetophasevoltageu12 = 0;
	private int phasetophasevoltageu23 = 0;
	private int phasetophasevoltageu31 = 0;
	private int phasetoneutralvoltagephase1 = 0;
	private int phasetoneutralvoltagephase2 = 0;
	private int phasetoneutralvoltagephase3 = 0;
	private int frequency = 0;
	private int activepower = 0;
	private int reactivepower = 0;
	private int apparentpower = 0;
	private int powerfactor = 0;
	private int activepowerphase1 = 0;
	private int activepowerphase2 = 0;
	private int activepowerphase3 = 0;
	private int reactivepowerphase1 = 0;
	private int reactivepowerphase2 = 0;
	private int reactivepowerphase3 = 0;
	private int apparentpowerphase1 = 0;
	private int apparentpowerphase2 = 0;
	private int apparentpowerphase3 = 0;
	private int powerfactorphase1 = 0;
	private int powerfactorphase2 = 0;
	private int powerfactorphase3 = 0;
	private int averagevaluei1 = 0;
	private int averagevaluei2 = 0;
	private int averagevaluei3 = 0;
	private int averagevalueactivepowerplus = 0;
	private int averagevalueactivepowerminus = 0;
	private int averagevaluereactivepowerplus = 0;
	private int averagevaluereactivepowerminus = 0;
	private int averagevalueapparentpower = 0;
	private int maximumvaluei1 = 0;
	private int maximumvaluei2 = 0;
	private int maximumvaluei3 = 0;
	private int maximumvalueactivepowerplus = 0;
	private int maximumvalueactivepowerminus = 0;
	private int maximumvaluereactivepowerplus = 0;
	private int maximumvaluereactivepowerminus = 0;
	private int maximumvalueapparentpower = 0;

	private int activeenergyplus = 0;
	private int reactiveenergyplus = 0;
	private int apparentenergy = 0;

	private int preactiveenergyplus = 0;
	private int prereactiveenergyplus = 0;
	private int preapparentenergy = 0;

	private int kWh_delta_System = 0;
	private int kVARh_delta_System = 0;
	private int kVAh_delta_System = 0;

	public PFM_DPM_v2(String category, int addr) {
		super(category, addr);
	}

	@Override
	public String getDeviceData() {
		byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
		if (decodingData(0, data, DATA_MODE)) {
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
			byte[] data = modbus.readHoldingRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
			decodingData(0, data, DATA_MODE);
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			Map<String, String> item = new Hashtable<String, String>();
			item.put("System_Active_Energy_Reading", vformat.format(activeenergyplus));
			item.put("System_Active_Power", vformat.format((short) activepower / 100.0));
			item.put("System_Reactive_Energy_Reading", vformat.format(reactiveenergyplus));
			item.put("System_Reactive_Power", vformat.format((short) reactivepower / 100.0));
			item.put("System_Apparent_Energy_Reading", vformat.format(apparentenergy));
			item.put("System_Apparent_Power", vformat.format((short) apparentpower / 100.0));
			item.put("System_Power_Factor", vformat.format((short) powerfactor / 1000.0));
			item.put("System_Current", vformat.format((phase1current + phase2current + phase3current) / 1000.0));
			item.put("System_Voltage", vformat.format(phasetoneutralvoltagephase1 / 100.0));
			item.put("System_Voltage_L1-L2", vformat.format(phasetophasevoltageu12 / 100.0));
			item.put("System_Voltage_L2-L3", vformat.format(phasetophasevoltageu23 / 100.0));
			item.put("System_Voltage_L1-L3", vformat.format(phasetophasevoltageu31 / 100.0));
			item.put("System_Frequency", vformat.format(frequency / 100.0));
			item.put("System_Peak_Demand", vformat.format(maximumvalueactivepowerplus / 100.0));

			item.put("Phase1_Active_Power", vformat.format((short) activepowerphase1 / 100.0));
			item.put("Phase1_Reactive_Power", vformat.format((short) reactivepowerphase1 / 100.0));
			item.put("Phase1_Apparent_Power", vformat.format((short) apparentpowerphase1 / 100.0));
			item.put("Phase1_Power_Factor", vformat.format((short) powerfactorphase1 / 1000.0));
			item.put("Phase1_Current", vformat.format(phase1current / 1000.0));
			item.put("Phase1_Voltage", vformat.format(phasetoneutralvoltagephase1 / 100.0));

			item.put("Phase2_Active_Power", vformat.format((short) activepowerphase2 / 100.0));
			item.put("Phase2_Reactive_Power", vformat.format((short) reactivepowerphase2 / 100.0));
			item.put("Phase2_Apparent_Power", vformat.format((short) apparentpowerphase2 / 100.0));
			item.put("Phase2_Power_Factor", vformat.format((short) powerfactorphase2 / 1000.0));
			item.put("Phase2_Current", vformat.format(phase2current / 1000.0));
			item.put("Phase2_Voltage", vformat.format(phasetoneutralvoltagephase2 / 100.0));

			item.put("Phase3_Active_Power", vformat.format((short) activepowerphase3 / 100.0));
			item.put("Phase3_Reactive_Power", vformat.format((short) reactivepowerphase3 / 100.0));
			item.put("Phase3_Apparent_Power", vformat.format((short) apparentpowerphase3 / 100.0));
			item.put("Phase3_Power_Factor", vformat.format((short) powerfactorphase3 / 1000.0));
			item.put("Phase3_Current", vformat.format(phase3current / 1000.0));
			item.put("Phase3_Voltage", vformat.format(phasetoneutralvoltagephase3 / 100.0));

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
			phase1current = ModbusUtil.registersBEToInt(data, (768 - 768) * 2 + OFFSET_DATA);
			phase2current = ModbusUtil.registersBEToInt(data, (770 - 768) * 2 + OFFSET_DATA);
			phase3current = ModbusUtil.registersBEToInt(data, (772 - 768) * 2 + OFFSET_DATA);
			neutralcurrent = ModbusUtil.registersBEToInt(data, (774 - 768) * 2 + OFFSET_DATA);
			phasetophasevoltageu12 = ModbusUtil.registersBEToInt(data, (776 - 768) * 2 + OFFSET_DATA);
			phasetophasevoltageu23 = ModbusUtil.registersBEToInt(data, (778 - 768) * 2 + OFFSET_DATA);
			phasetophasevoltageu31 = ModbusUtil.registersBEToInt(data, (780 - 768) * 2 + OFFSET_DATA);
			phasetoneutralvoltagephase1 = ModbusUtil.registersBEToInt(data, (782 - 768) * 2 + OFFSET_DATA);
			phasetoneutralvoltagephase2 = ModbusUtil.registersBEToInt(data, (784 - 768) * 2 + OFFSET_DATA);
			phasetoneutralvoltagephase3 = ModbusUtil.registersBEToInt(data, (786 - 768) * 2 + OFFSET_DATA);
			frequency = ModbusUtil.registersBEToInt(data, (788 - 768) * 2 + OFFSET_DATA);
			activepower = ModbusUtil.registersBEToInt(data, (790 - 768) * 2 + OFFSET_DATA);
			reactivepower = ModbusUtil.registersBEToInt(data, (792 - 768) * 2 + OFFSET_DATA);
			apparentpower = ModbusUtil.registersBEToInt(data, (794 - 768) * 2 + OFFSET_DATA);
			powerfactor = ModbusUtil.registersBEToInt(data, (796 - 768) * 2 + OFFSET_DATA);
			activepowerphase1 = ModbusUtil.registersBEToInt(data, (798 - 768) * 2 + OFFSET_DATA);
			activepowerphase2 = ModbusUtil.registersBEToInt(data, (800 - 768) * 2 + OFFSET_DATA);
			activepowerphase3 = ModbusUtil.registersBEToInt(data, (802 - 768) * 2 + OFFSET_DATA);
			reactivepowerphase1 = ModbusUtil.registersBEToInt(data, (804 - 768) * 2 + OFFSET_DATA);
			reactivepowerphase2 = ModbusUtil.registersBEToInt(data, (806 - 768) * 2 + OFFSET_DATA);
			reactivepowerphase3 = ModbusUtil.registersBEToInt(data, (808 - 768) * 2 + OFFSET_DATA);
			apparentpowerphase1 = ModbusUtil.registersBEToInt(data, (810 - 768) * 2 + OFFSET_DATA);
			apparentpowerphase2 = ModbusUtil.registersBEToInt(data, (812 - 768) * 2 + OFFSET_DATA);
			apparentpowerphase3 = ModbusUtil.registersBEToInt(data, (814 - 768) * 2 + OFFSET_DATA);
			powerfactorphase1 = ModbusUtil.registersBEToInt(data, (816 - 768) * 2 + OFFSET_DATA);
			powerfactorphase2 = ModbusUtil.registersBEToInt(data, (818 - 768) * 2 + OFFSET_DATA);
			powerfactorphase3 = ModbusUtil.registersBEToInt(data, (820 - 768) * 2 + OFFSET_DATA);
			averagevaluei1 = ModbusUtil.registersBEToInt(data, (822 - 768) * 2 + OFFSET_DATA);
			averagevaluei2 = ModbusUtil.registersBEToInt(data, (824 - 768) * 2 + OFFSET_DATA);
			averagevaluei3 = ModbusUtil.registersBEToInt(data, (826 - 768) * 2 + OFFSET_DATA);
			averagevalueactivepowerplus = ModbusUtil.registersBEToInt(data, (828 - 768) * 2 + OFFSET_DATA);
			averagevalueactivepowerminus = ModbusUtil.registersBEToInt(data, (830 - 768) * 2 + OFFSET_DATA);
			averagevaluereactivepowerplus = ModbusUtil.registersBEToInt(data, (832 - 768) * 2 + OFFSET_DATA);
			averagevaluereactivepowerminus = ModbusUtil.registersBEToInt(data, (834 - 768) * 2 + OFFSET_DATA);
			averagevalueapparentpower = ModbusUtil.registersBEToInt(data, (836 - 768) * 2 + OFFSET_DATA);
			maximumvaluei1 = ModbusUtil.registersBEToInt(data, (838 - 768) * 2 + OFFSET_DATA);
			maximumvaluei2 = ModbusUtil.registersBEToInt(data, (840 - 768) * 2 + OFFSET_DATA);
			maximumvaluei3 = ModbusUtil.registersBEToInt(data, (842 - 768) * 2 + OFFSET_DATA);
			maximumvalueactivepowerplus = ModbusUtil.registersBEToInt(data, (844 - 768) * 2 + OFFSET_DATA);
			maximumvalueactivepowerminus = ModbusUtil.registersBEToInt(data, (846 - 768) * 2 + OFFSET_DATA);
			maximumvaluereactivepowerplus = ModbusUtil.registersBEToInt(data, (848 - 768) * 2 + OFFSET_DATA);
			maximumvaluereactivepowerminus = ModbusUtil.registersBEToInt(data, (850 - 768) * 2 + OFFSET_DATA);
			maximumvalueapparentpower = ModbusUtil.registersBEToInt(data, (852 - 768) * 2 + OFFSET_DATA);

			activeenergyplus = ModbusUtil.registersBEToInt(data, (856 - 768) * 2 + OFFSET_DATA);
			reactiveenergyplus = ModbusUtil.registersBEToInt(data, (858 - 768) * 2 + OFFSET_DATA);
			apparentenergy = ModbusUtil.registersBEToInt(data, (860 - 768) * 2 + OFFSET_DATA);
			return true;
		}
		if (mode == CONFIG_MODE) {

			return true;
		}

		return true;
	}

	private void calculateDecodedData() {
		if (kWh_delta_System == -1)
			kWh_delta_System = 0;
		// device reboot
		else if (activeenergyplus <= preactiveenergyplus)
			kWh_delta_System = 0;
		else
			kWh_delta_System = activeenergyplus - preactiveenergyplus;
		preactiveenergyplus = activeenergyplus;

		if (kVARh_delta_System == -1)
			kVARh_delta_System = 0;
		// device reboot
		else if (reactiveenergyplus <= prereactiveenergyplus)
			kVARh_delta_System = 0;
		else
			kVARh_delta_System = reactiveenergyplus - prereactiveenergyplus;
		prereactiveenergyplus = reactiveenergyplus;

		if (kVAh_delta_System == -1)
			kVAh_delta_System = 0;
		// device reboot
		else if (apparentenergy <= preapparentenergy)
			kVAh_delta_System = 0;
		else
			kVAh_delta_System = apparentenergy - preapparentenergy;
		preapparentenergy = apparentenergy;

	}

	private String createDataSendToServer() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		timestamp = System.currentTimeMillis();
		StringBuffer data = new StringBuffer();
		data.append("|DEVICEID=" + getId() + "-0-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Energy Reading=" + vformat.format(activeenergyplus) + ",kWh");
		data.append(";Active Energy=" + vformat.format(kWh_delta_System * 1000.0) + ",Wh");
		data.append(";Active Power=" + vformat.format((short) activepower / 100.0) + ",kW");
		data.append(";Reactive Energy Reading=" + vformat.format(reactiveenergyplus) + ",kVARh");
		data.append(";Reactive Energy=" + vformat.format(kVARh_delta_System * 1000.0) + ",VARh");
		data.append(";Reactive Power=" + vformat.format((short) reactivepower / 100.0) + ",kVAR");
		data.append(";Apparent Energy Reading=" + vformat.format(apparentenergy) + ",kVAh");
		data.append(";Apparent Energy=" + vformat.format(kVAh_delta_System * 1000.0) + ",VAh");
		data.append(";Apparent Power=" + vformat.format((short) apparentpower / 100.0) + ",kVA");
		data.append(";Power Factor=" + vformat.format((short) powerfactor / 1000.0) + ",None");
		data.append(";Current=" + vformat.format((phase1current + phase2current + phase3current) / 1000.0) + ",A");
		data.append(";Voltage=" + vformat.format(phasetoneutralvoltagephase1 / 100.0) + ",V");
		data.append(";Voltage L1-L2=" + vformat.format(phasetophasevoltageu12 / 100.0) + ",V");
		data.append(";Voltage L2-L3=" + vformat.format(phasetophasevoltageu23 / 100.0) + ",V");
		data.append(";Voltage L1-L3=" + vformat.format(phasetophasevoltageu31 / 100.0) + ",V");
		data.append(";Frequency=" + vformat.format(frequency / 100.0) + ",Hz");
		data.append(";Peak Demand=" + vformat.format(maximumvalueactivepowerplus / 100.0) + ",kW");

		data.append("|DEVICEID=" + getId() + "-1-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format((short) activepowerphase1 / 100.0) + ",kW");
		data.append(";Reactive Power=" + vformat.format((short) reactivepowerphase1 / 100.0) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format((short) apparentpowerphase1 / 100.0) + ",kVA");
		data.append(";Power Factor=" + vformat.format((short) powerfactorphase1 / 1000.0) + ",None");
		data.append(";Current=" + vformat.format(phase1current / 1000.0) + ",A");
		data.append(";Voltage=" + vformat.format(phasetoneutralvoltagephase1 / 100.0) + ",V");

		data.append("|DEVICEID=" + getId() + "-2-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format((short) activepowerphase2 / 100.0) + ",kW");
		data.append(";Reactive Power=" + vformat.format((short) reactivepowerphase2 / 100.0) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format((short) apparentpowerphase2 / 100.0) + ",kVA");
		data.append(";Power Factor=" + vformat.format((short) powerfactorphase2 / 1000.0) + ",None");
		data.append(";Current=" + vformat.format(phase2current / 1000.0) + ",A");
		data.append(";Voltage=" + vformat.format(phasetoneutralvoltagephase2 / 100.0) + ",V");

		data.append("|DEVICEID=" + getId() + "-3-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Active Power=" + vformat.format((short) activepowerphase3 / 100.0) + ",kW");
		data.append(";Reactive Power=" + vformat.format((short) reactivepowerphase3 / 100.0) + ",kVAR");
		data.append(";Apparent Power=" + vformat.format((short) apparentpowerphase3 / 100.0) + ",kVA");
		data.append(";Power Factor=" + vformat.format((short) powerfactorphase3 / 1000.0) + ",None");
		data.append(";Current=" + vformat.format(phase3current / 1000.0) + ",A");
		data.append(";Voltage=" + vformat.format(phasetoneutralvoltagephase3 / 100.0) + ",V");

		return data.toString();
	}

}
