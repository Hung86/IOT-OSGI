package com.greenkoncepts.gateway.adapter.brainchild;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.greenkoncepts.gateway.api.bridge.BridgeMaster;
import com.greenkoncepts.gateway.util.FuncUtil;

public class Brainchild16DIState extends BrainchildDevice {

	public static int MBREG_DATA_START = 1;
	public static int MBREG_DATA_NUM = 1;
	
	public static int CHANNEL_NUM = 16;
	
	public static int DIBit[] = {0x0001, 0x0002, 0x0004, 0x0008,
								 0x0010, 0x0020, 0x0040, 0x0080,
								 0x0100, 0x0200, 0x0400, 0x0800,
								 0x1000, 0x2000, 0x4000, 0x8000};
	
	private int preState[] =   {0,0,0,0,
								0,0,0,0,
								0,0,0,0,
								0,0,0,0};
	
	private int currentState[] = {0,0,0,0,
								  0,0,0,0,
								  0,0,0,0,
								  0,0,0,0};
	
	private int registerValue = 0;
	private boolean hasChange[] = new boolean[CHANNEL_NUM];
	private String names[] = new String[CHANNEL_NUM];
	private int values[] = new int[CHANNEL_NUM];
	
	public Brainchild16DIState(int addr, String category, BridgeMaster bm) {
		super(category, addr);
		for (int i = 0; i < CHANNEL_NUM; i++) {
			values[i] = 0;
			hasChange[i] = false;
		}
		
	}

	@Override
	public void setDeviceAttributes(List<Map<String, String>> attr) {
		for (int ch = 0; ch < CHANNEL_NUM && ch < attr.size(); ch++) {
			names[ch] = attr.get(ch).get("name");
			if ((names[ch] == null) || (names[ch].equals(""))) {
				mLogger.warn("[DI 16] Device " + modbusid + " Channel " + ch
						+ " is not defined, IGNORE this channel");
				continue;
			}
		}
	}
	

	@Override
	public String getDeviceStateData() {
		byte[] data = modbus.readInputRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
		if (decodingData(0, data, DATA_MODE)) {
			calculateDecodedData();
		}
		return createDataSendToServer(true);
	}
	
	@Override
	public String getDeviceData() {
		return createDataSendToServer(false);
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
			byte[] data = modbus.readInputRegisters(modbusid, MBREG_DATA_START, MBREG_DATA_NUM);
			decodingData(0, data, DATA_MODE);
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			for (int i = 0; i < CHANNEL_NUM; i++) {
				if ((names[i] == null) || (names[i].equals(""))) {
					continue;
				}
				Map<String, String> item = new Hashtable<String, String>();
				item.put("data_" + i,  String.valueOf(values[i]));
				item.put("name_" + i, names[i]);
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

	private boolean decodingData(int idx, byte[] data, int mode) {
		if (data == null) {
			errorCount++;
			return false;
		}

		errorCount = 0;
		if (mode == DATA_MODE) {
			registerValue = FuncUtil.RegisterBigEndian.registerToUnsignedShort(data, OFFSET_DATA);
			return true;
		}

		if (mode == CONFIG_MODE) {

		}

		return true;
	}

	private void calculateDecodedData() {
		for (int i = 0; i < CHANNEL_NUM; i++) {
			if ((names[i] == null) || (names[i].equals(""))) {
				continue;
			}
			if((currentState[i] = (registerValue & DIBit[i])) == 0) {
				values[i] = 0;
			} else {
				values[i] = 1;
			}
			
			if (currentState[i] != preState[i]) {
				preState[i] = currentState[i];
				if (!hasChange[i]) {
					hasChange[i] = true;
				}
			}
		}
	}

	private String createDataSendToServer(boolean isStateCalling) {
		StringBuffer data = new StringBuffer();
		timestamp = System.currentTimeMillis();
		if (isStateCalling) {
			for (int ch = 0; ch < CHANNEL_NUM; ch++) {
				if (hasChange[ch]) {
					hasChange[ch] = false;
					if ((names[ch] == null) || (names[ch].equals(""))) {
						continue;
					}
					data.append("|DEVICEID=" + getId() + "-" + ch + "-0");
					data.append(";TIMESTAMP=" + timestamp);
					data.append(";" + names[ch] + "=" + String.valueOf(values[ch]) + ",None");
				}
			}
		} else {
			if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
				return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
			}
			
			for (int ch = 0; ch < CHANNEL_NUM; ch++) {
				if ((names[ch] == null) || (names[ch].equals(""))) {
					continue;
				}
				data.append("|DEVICEID=" + getId() + "-" + ch + "-0");
				data.append(";TIMESTAMP=" + timestamp);
				data.append(";" + names[ch] + "=" + String.valueOf(values[ch]) + ",None");
			}
		}
		return data.toString();
	}

}
