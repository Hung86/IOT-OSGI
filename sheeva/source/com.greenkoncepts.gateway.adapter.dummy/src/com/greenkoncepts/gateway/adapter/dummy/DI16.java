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

public class DI16 extends DummyDevice {

	public static int CHANNEL_NUM = 16;
	private double counter_reading[] = new double[CHANNEL_NUM];
	private double counter[] = new double[CHANNEL_NUM];
	private double flowRate[] = new double[CHANNEL_NUM];
	
	private String names[] = new String[CHANNEL_NUM];
	private float ratios[] = new float[CHANNEL_NUM];
	private String units[] = new String[CHANNEL_NUM];
	private String flowRateName[] = new String[CHANNEL_NUM];
	private float maxs[] = new float[CHANNEL_NUM];
	private float mins[] = new float[CHANNEL_NUM];
	
	private String _savefile;
	
	public DI16(int addr, String cat) {
		super(addr, cat);
		for (int i = 0; i < CHANNEL_NUM; i++) {
			counter_reading[i] = 0;
			counter[i] = 0;
		}
		_savefile = "Dummy_Data_DI16_" + addr;
		loadData();
	}
	
	public void setDeviceAttributes(List<Map<String, String>> attr) {
		if (attr.isEmpty()) {
			for (int i = 0; i < CHANNEL_NUM; i++) {
				names[i] = "Water Volume";
				units[i] = "cu m";
				ratios[i] = 1;
				flowRateName[i] = "Flow Rate"; 
				mins[i] = 0;
			    maxs[i] = 1;
			    mLogger.warn("[DI 16] Device " + modbusid + " Channel " + i
						+ " is set to default setting");
			}
			return;
		}

		for (int ch = 0; ch < CHANNEL_NUM && ch < attr.size(); ch++) {
			names[ch] = attr.get(ch).get("name");
			units[ch] = attr.get(ch).get("unit");
			if ((names[ch] == null) || (names[ch].equals(""))) {
				mLogger.warn("[DI 16] Device " + modbusid + " Channel " + ch
						+ " is not defined, IGNORE this channel");
				continue;
			}

			if ("cu m".equals(units[ch])) {
				flowRateName[ch] = "Flow Rate";
				mins[ch] = 0;
				maxs[ch] = 1;
			} else {
				// for liter unit
				flowRateName[ch] = "Diesel Flow Rate";
				mins[ch] = 0;
				maxs[ch] = 10;
			}

			try {
				ratios[ch] = Float.parseFloat(attr.get(ch).get("ratio"));
			} catch (NumberFormatException e) {
				ratios[ch] = 1;
				mLogger.error("[DI 16] Device " + modbusid + " Channel " + ch
						+ " uses default ratio = 1");
			}

		}

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
			for (int i = 0; i < CHANNEL_NUM; i++) {
				if ((names[i] == null) || (names[i].equals(""))) {
					continue;
				}
				Map<String, String> item = new Hashtable<String, String>();
				item.put("data_" + i, vformat.format(counter_reading[i]));
				item.put("flow_" + i, vformat.format(flowRate[i]));
				item.put("name_" + i, names[i]);
				item.put("unit_" + i, units[i]);
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
		for (int i = 0; i < CHANNEL_NUM; i++) {
			if ((names[i] == null) || (names[i].equals(""))) {
				continue;
			}
			
			double randomInt =  (mins[i] + (Math.random() * (maxs[i] - mins[i])));
		    counter_reading[i] += randomInt*ratios[i];
			counter[i] = randomInt*ratios[i];
			flowRate[i] = counter[i];
			if ("Flow Rate".equals(flowRateName)) {
				flowRate[i] = flowRate[i] * 1000;
			}
			flowRate[i] = flowRate[i]/60;
		}
		saveData();
	}

	private String createDataSendToServer() {
		if (getStatus() != GKProtocol.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		StringBuffer data = new StringBuffer();
		timestamp = System.currentTimeMillis();
		for (int ch = 0; ch < CHANNEL_NUM; ch++) {
			if ((names[ch] == null) || (names[ch].equals(""))) {
				continue;
			}
			data.append("|DEVICEID=" + getId() + "-" + ch + "-0");
			data.append(";TIMESTAMP=" + timestamp);
			data.append(";" + names[ch] + " Reading=" + vformat.format(counter_reading[ch]) + "," + units[ch]);
			data.append(";" + names[ch] + "=" + vformat.format(counter[ch]) + "," + units[ch]);
			data.append(";" + flowRateName[ch] + "=" + vformat.format((flowRate[ch])) + ",l/s");
		}
		return data.toString();
	}
	
	public boolean saveData() {
		mLogger.info("save data");
		Properties _props = new Properties();
		try {
			for (int i = 0; i < CHANNEL_NUM; i++) {
				_props.setProperty("volume_ch" + i, String.valueOf(counter_reading[i]));
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
			FileInputStream in = new FileInputStream(_savefile);
			_props.load(in);
			in.close();
			for (int i = 0; i < CHANNEL_NUM; i++) {
				try {
					counter_reading[i] = Double.parseDouble(_props.getProperty("volume_ch" + i));
				} catch (NumberFormatException ex) {
					
				}
			}
			return true;
		} catch (FileNotFoundException e) {
			mLogger.warn("Can't find save file: " + _savefile);

		} catch (IOException e) {
			mLogger.error("Read failure for file: " + _savefile);
		}
		return false;
	}

}