package com.greenkoncepts.gateway.adapter.bosch;

import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class XDK extends BoschDevice {
	private BoschAdapter ownAdapter = null;
	private Map<String, XDKSensor> sensorDevices = new Hashtable<String, XDKSensor>();
	private Map<String, XDKButton> buttonDevices = new Hashtable<String, XDKButton>();
	private StringBuilder rapidDataCache = new StringBuilder();
	private int sendInterval = -1;
	
	private long lastUpdatedTime = System.currentTimeMillis();
	public XDK(int id, String category, int interval, BoschAdapter adapter) {
		super(id, category);
		ownAdapter = adapter;
		sendInterval = interval;
		System.out.println("------------------------create XDK : sendInterval = " + sendInterval);
	}
	
	@Override
	public String getDeviceData() {
		timestamp = System.currentTimeMillis();
		if (Math.abs(timestamp - lastUpdatedTime) > 2*sendInterval*1000) {
			errorCount++;
			return "";
		}
		
		errorCount = 0;
		
		StringBuilder sb = new StringBuilder();
		for (String key : sensorDevices.keySet()) {
			XDKSensor sensor = sensorDevices.get(key);
			sb.append(sensor.createDataToServer(getId(), timestamp));
		}
		
		if (buttonDevices.containsKey("button")) {
			XDKButton button = buttonDevices.get("button");
			sb.append(button.createDataToServer(getId(), timestamp));
		}
		return sb.toString();
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		real_time_data.clear();
		for (String key : sensorDevices.keySet()) {
			real_time_data.add(sensorDevices.get(key).getWebConsoleValue());
		}
		
		if (buttonDevices.containsKey("button")) {
			real_time_data.add(buttonDevices.get("button").getWebConsoleValue());
		}
		return real_time_data;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean setDataSensors(Object dataJson) {
		if (dataJson != null) {
			Map<String, Object> data = (Map<String, Object>) dataJson; 
			int id = (Integer) data.get("id");
			if (deviceid == id) {
				lastUpdatedTime = System.currentTimeMillis();
				rapidDataCache.setLength(0);
				timestamp = System.currentTimeMillis();
				String returnVal = null;
				Map<String, Number> msg = (Map<String, Number>) data.get("msg");
				for (String key : sensorDevices.keySet()) {
					XDKSensor sensor = sensorDevices.get(key);
					try {
						returnVal = sensor.value(msg, getId(), timestamp);
						if (returnVal != null) {
							rapidDataCache.append(returnVal);
						}
					} catch (Exception e) {
						mLogger.error("Exception", e);
					}
				}
				
				if (buttonDevices.containsKey("button")) {
					returnVal = buttonDevices.get("button").value(msg, getId(), timestamp);
					if (returnVal != null) {
						rapidDataCache.append(returnVal);
					}
				}
				
				if (rapidDataCache.length() > 0) {
					if ((ownAdapter.getBridgeMaster() != null) && (ownAdapter.getBridgeMaster().getSendingInterval() != 1)){
						ownAdapter.getBridgeMaster().adapterSendDeviceState(ownAdapter.getClass().getName(), rapidDataCache.toString());
					}
				}
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setDeviceAttributes(Object data) {
		if (data != null) {
			sensorDevices.put("bma280", new BMA280());
			sensorDevices.put("bmg160", new BMG160());
			sensorDevices.put("bmi160", new BMI160());
			sensorDevices.put("bmm150", new BMM150());
			sensorDevices.put("bme280", new BME280());
			sensorDevices.put("max44009", new MAX44009());
			sensorDevices.put("aku340", new AKU340());
			buttonDevices.put("button", new XDKButton());
			
			List<Map<String, String>> attrs = (List<Map<String, String>>) data;
			for (Map<String, String> sensor : attrs) {
				String sensor_name = sensor.get("sensor");
				double offset = Double.valueOf(sensor.get("offset"));
				double delta = Math.abs(Double.valueOf(sensor.get("delta")));
				XDKSensor xdkSensor = sensorDevices.get(sensor_name.split("_")[0]);
				xdkSensor.offset.put(sensor_name, offset);
				xdkSensor.delta.put(sensor_name, delta);

			}
			
			for (String name : sensorDevices.keySet()) {
				XDKSensor xdkSensor = sensorDevices.get(name);
				mLogger.info("-------------[setDeviceAttributes] <xdkSensor.offset> = " + xdkSensor.offset + " - <xdkSensor.delta> = " + xdkSensor.delta);
				xdkSensor.refreshSetings();
			}
		}
	}
}

abstract class XDKSensor {
	public DecimalFormat vformat = new DecimalFormat("#########0.0000");
	public Map<String, Double> offset = new Hashtable<String, Double>();
	public Map<String, Double> delta = new Hashtable<String, Double>();
	public StringBuilder rapidDataCacheXDK = new StringBuilder();
	public long timestampXDK = 0;
	abstract public String createDataToServer(String prefix_id, long timestamp);
	abstract public String value(Map<String, Number> data, String prefix_id, long timestamp) ;
	abstract public Map<String, String> getWebConsoleValue() ;
	abstract public void refreshSetings() ;
}

class BMA280 extends XDKSensor {
	public static int CH = 0;
	public double x = 0;
	public double y = 0;
	public double z = 0;
	boolean able_x , able_y, able_z;
	double offset_x, offset_y, offset_z, delta_x, delta_y, delta_z;
	
	public void refreshSetings() {
		offset_x = (offset.containsKey("bma280_x") ? offset.get("bma280_x") * 1000 : 0);
		offset_y = (offset.containsKey("bma280_y") ? offset.get("bma280_y") * 1000 : 0);
		offset_z = (offset.containsKey("bma280_z") ? offset.get("bma280_z") * 1000 : 0);
		
		delta_x =  (delta.containsKey("bma280_x") ? delta.get("bma280_x") : 0);
		delta_y =  (delta.containsKey("bma280_y") ? delta.get("bma280_y") : 0);
		delta_z =  (delta.containsKey("bma280_z") ? delta.get("bma280_z") : 0);
	}
	
	public String createDataToServer(String prefix_id, long timestamp) {		
		StringBuilder data = new StringBuilder();
		data.append("|DEVICEID=" + prefix_id + "-0-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";AccX=" + vformat.format(x) + ",g");
		data.append(";AccY=" + vformat.format(y) + ",g");
		data.append(";AccZ=" + vformat.format(z) + ",g");
		return data.toString();
	}
	
	@Override
	public String value(Map<String, Number> data, String prefix_id, long timestamp) {
		timestampXDK = data.get("Timestamp[ms]").longValue();
		
		rapidDataCacheXDK.setLength(0);
		
		double temp = 0;
		temp = (((double) data.get("bma280_x[mg]").intValue()) + offset_x)/1000;
		if (Math.abs(temp - x) >= delta_x) {
			rapidDataCacheXDK.append(";AccX=" + vformat.format(temp) + ",g");
		} 
		x = temp;
		
		temp = (((double) data.get("bma280_y[mg]").intValue()) + offset_y)/1000;
		if (Math.abs(temp - y) >= delta_y) {
			rapidDataCacheXDK.append(";AccY=" + vformat.format(temp) + ",g");
		} 
		y = temp;
		
		temp = (((double) data.get("bma280_z[mg]").intValue()) + offset_z)/1000;
		if (Math.abs(temp - z) >= delta_z) {
			rapidDataCacheXDK.append(";AccZ=" + vformat.format(temp) + ",g");
		} 
		z = temp;
		
		if (rapidDataCacheXDK.length() > 0) {
			return "|DEVICEID=" + prefix_id + "-0-0" + ";TIMESTAMP=" + timestamp + rapidDataCacheXDK.toString();
		}
		return null;
	}

	@Override
	public Map<String, String> getWebConsoleValue() {
		Map<String, String> data = new Hashtable<String, String>();
		data.put("bma280_time", String.valueOf(timestampXDK));
		data.put("bma280_x", vformat.format(x));
		data.put("bma280_y", vformat.format(y));
		data.put("bma280_z", vformat.format(z));
		return data;
	}
	
};

class BMG160 extends XDKSensor {
	public static int CH = 1;
	public double x = 0;
	public double y = 0;
	public double z = 0;
	
	boolean able_x , able_y, able_z;
	double offset_x, offset_y, offset_z, delta_x, delta_y, delta_z;
	
	public void refreshSetings() {
		offset_x = (offset.containsKey("bmg160_x") ? offset.get("bmg160_x") * 1000 : 0);
		offset_y = (offset.containsKey("bmg160_y") ? offset.get("bmg160_y") * 1000 : 0);
		offset_z = (offset.containsKey("bma280_z") ? offset.get("bma280_z") * 1000 : 0);
		
		delta_x =  (delta.containsKey("bmg160_x") ? delta.get("bmg160_x") : 0);
		delta_y =  (delta.containsKey("bmg160_y") ? delta.get("bmg160_y") : 0);
		delta_z =  (delta.containsKey("bmg160_z") ? delta.get("bmg160_z") : 0);
	}
	
	public String createDataToServer(String prefix_id, long timestamp) {
		StringBuilder data = new StringBuilder();
		data.append("|DEVICEID=" + prefix_id + "-1-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";GyroX=" + vformat.format(x) + ",deg");
		data.append(";GyroY=" + vformat.format(y) + ",deg");
		data.append(";GyroZ=" + vformat.format(z) + ",deg");
		return data.toString();
	}
	
	@Override
	public String value(Map<String, Number> data, String prefix_id, long timestamp) {
		timestampXDK = data.get("Timestamp[ms]").longValue();
		rapidDataCacheXDK.setLength(0);
		double temp = 0;
		
		temp = (((double) data.get("bmg160_x[mDeg]").intValue()) + offset_x)/1000;
		if (Math.abs(temp - x) >= delta_x) {
			rapidDataCacheXDK.append(";GyroX=" + vformat.format(temp) + ",deg");
		}
		x = temp;
		
		temp = (((double) data.get("bmg160_y[mDeg]").intValue()) + offset_y)/1000;
		if (Math.abs(temp - y) >= delta_y) {
			rapidDataCacheXDK.append(";GyroY=" + vformat.format(temp) + ",deg");
		}
		y = temp;
		
		temp = (((double) data.get("bmg160_z[mDeg]").intValue()) + offset_z)/1000;
		if (Math.abs(temp - z) >= delta_z) {
			rapidDataCacheXDK.append(";GyroZ=" + vformat.format(temp) + ",deg");
		} 
		z = temp;
		
		if (rapidDataCacheXDK.length() > 0) {
			return "|DEVICEID=" + prefix_id + "-1-0" + ";TIMESTAMP=" + timestamp + rapidDataCacheXDK.toString();
		}
		return null;
	}
	
	@Override
	public Map<String, String> getWebConsoleValue() {
		Map<String, String> data = new Hashtable<String, String>();
		data.put("bmg160_time", String.valueOf(timestampXDK));
		data.put("bmg160_x", vformat.format(x));
		data.put("bmg160_y", vformat.format(y));
		data.put("bmg160_z", vformat.format(z));
		return data;
	}
};

class BMI160 extends XDKSensor {
	public static int CH = 2;
	public double a_x = 0;
	public double a_y = 0;
	public double a_z = 0;
	public double g_x = 0;
	public double g_y = 0;
	public double g_z = 0;
	
	boolean able_ax , able_ay, able_az, able_gx , able_gy, able_gz;
	double offset_ax, offset_ay, offset_az, delta_ax, delta_ay, delta_az;
	double offset_gx, offset_gy, offset_gz, delta_gx, delta_gy, delta_gz;
	
	public void refreshSetings() {
		offset_ax = (offset.containsKey("bmi160_a_x") ? offset.get("bmi160_a_x") * 1000 : 0);
		offset_ay = (offset.containsKey("bmi160_a_y") ? offset.get("bmi160_a_y") * 1000 : 0);
		offset_az = (offset.containsKey("bmi160_a_z") ? offset.get("bmi160_a_z") * 1000 : 0);
		
		delta_ax =  (delta.containsKey("bmi160_a_x") ? delta.get("bmi160_a_x") : 0);
		delta_ay =  (delta.containsKey("bmi160_a_y") ? delta.get("bmi160_a_y") : 0);
		delta_az =  (delta.containsKey("bmi160_a_z") ? delta.get("bmi160_a_z") : 0);
		
		offset_gx = (offset.containsKey("bmi160_g_x") ? offset.get("bmi160_g_x") * 1000 : 0);
		offset_gy = (offset.containsKey("bmi160_g_y") ? offset.get("bmi160_g_y") * 1000 : 0);
		offset_gz = (offset.containsKey("bmi160_g_z") ? offset.get("bmi160_g_z") * 1000 : 0);
		
		delta_gx =  (delta.containsKey("bmi160_g_x") ? delta.get("bmi160_g_x") : 0);
		delta_gy =  (delta.containsKey("bmi160_g_y") ? delta.get("bmi160_g_y") : 0);
		delta_gz =  (delta.containsKey("bmi160_g_z") ? delta.get("bmi160_g_z") : 0);
	}
	
	public String createDataToServer(String prefix_id, long timestamp) {
		StringBuilder data = new StringBuilder();
		data.append("|DEVICEID=" + prefix_id + "-2-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";AccX=" + vformat.format(a_x) + ",g");
		data.append(";AccY=" + vformat.format(a_y) + ",g");
		data.append(";AccZ=" + vformat.format(a_z) + ",g");
		data.append(";GyroX=" + vformat.format(g_x) + ",deg");
		data.append(";GyroY=" + vformat.format(g_y) + ",deg");
		data.append(";GyroZ=" + vformat.format(g_z) + ",deg");
		return data.toString();
	}
	
	@Override
	public String value(Map<String, Number> data, String prefix_id, long timestamp) {
		timestampXDK = data.get("Timestamp[ms]").longValue();
		rapidDataCacheXDK.setLength(0);
		double temp = 0;
		
		temp = (((double) data.get("bmi160_a_x[mg]").intValue()) + offset_ax)/1000;
		if (Math.abs(temp - a_x) >= delta_ax) {
			rapidDataCacheXDK.append(";AccX=" + vformat.format(temp) + ",g");
		} 
		a_x = temp;
		
		temp = (((double) data.get("bmi160_a_y[mg]").intValue()) + offset_ay)/1000;
		if (Math.abs(temp - a_y) >= delta_ay) {
			rapidDataCacheXDK.append(";AccY=" + vformat.format(temp) + ",g");
		}
		a_y = temp;
		
		temp = (((double) data.get("bmi160_a_z[mg]").intValue()) + offset_az)/1000;
		if (Math.abs(temp - a_z) >= delta_az) {
			rapidDataCacheXDK.append(";AccZ=" + vformat.format(temp) + ",g");
		}
		a_z = temp;
		
		temp = (((double) data.get("bmi160_g_x[mDeg]").intValue()) + offset_gx)/1000;
		if (Math.abs(temp - g_x) >= delta_gx) {
			rapidDataCacheXDK.append(";GyroX=" + vformat.format(temp) + ",deg");
		}
		g_x = temp;
		
		temp = (((double) data.get("bmi160_g_y[mDeg]").intValue()) + offset_gy)/1000;
		if (Math.abs(temp - g_y) >= delta_gy) {
			rapidDataCacheXDK.append(";GyroY=" + vformat.format(temp) + ",deg");
		}
		g_y = temp;
		
		temp = (((double) data.get("bmi160_g_z[mDeg]").intValue()) + offset_gz)/1000;
		if (Math.abs(temp - g_z) >= delta_gz) {
			rapidDataCacheXDK.append(";GyroZ=" + vformat.format(temp) + ",deg");
		}
		g_z = temp;
		
		if (rapidDataCacheXDK.length() > 0) {
			return "|DEVICEID=" + prefix_id + "-2-0" + ";TIMESTAMP=" + timestamp + rapidDataCacheXDK.toString();
		}
		return null;
	}
	@Override
	public Map<String, String> getWebConsoleValue() {
		Map<String, String> data = new Hashtable<String, String>();
		data.put("bmi160_time", String.valueOf(timestampXDK));
		data.put("bmi160_a_x", vformat.format(a_x));
		data.put("bmi160_a_y", vformat.format(a_y));
		data.put("bmi160_a_z", vformat.format(a_z));
		data.put("bmi160_g_x", vformat.format(g_x));
		data.put("bmi160_g_y", vformat.format(g_y));
		data.put("bmi160_g_z", vformat.format(g_z));
		return data;
	}
}

class BMM150 extends XDKSensor {
	public static int CH = 3;
	public double x = 0;
	public double y = 0;
	public double z = 0;
	
	boolean able_x , able_y, able_z;
	double offset_x, offset_y, offset_z, delta_x, delta_y, delta_z;
	
	public void refreshSetings() {
		offset_x = (offset.containsKey("bmm150_x") ? offset.get("bmm150_x") : 0);
		offset_y = (offset.containsKey("bmm150_y") ? offset.get("bmm150_y") : 0);
		offset_z = (offset.containsKey("bmm150_z") ? offset.get("bmm150_z") : 0);
		
		delta_x =  (delta.containsKey("bmm150_x") ? delta.get("bmm150_x") : 0);
		delta_y =  (delta.containsKey("bmm150_y") ? delta.get("bmm150_y") : 0);
		delta_z =  (delta.containsKey("bmm150_z") ? delta.get("bmm150_z") : 0);
	}
	public String createDataToServer(String prefix_id, long timestamp) {
		StringBuilder data = new StringBuilder();
		data.append("|DEVICEID=" + prefix_id + "-3-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";MagX=" + vformat.format(x) + ",uT");
		data.append(";MagY=" + vformat.format(y) + ",uT");
		data.append(";MagZ=" + vformat.format(z) + ",uT");
		return data.toString();
	}
	
	@Override
	public String value(Map<String, Number> data, String prefix_id, long timestamp) {
		timestampXDK = data.get("Timestamp[ms]").longValue();
		rapidDataCacheXDK.setLength(0);
		double temp = 0;
		
		temp = (((double) data.get("bmm150_x[microT]").intValue()) + offset_x);
		if (Math.abs(temp - x) >= delta_x) {
			rapidDataCacheXDK.append(";MagX=" + vformat.format(temp) + ",uT");
		} 
		x = temp;
		
		temp = (((double) data.get("bmm150_y[microT]").intValue()) + offset_y);
		if (Math.abs(temp - y) >= delta_y) {
			rapidDataCacheXDK.append(";MagY=" + vformat.format(temp) + ",uT");
		}
		y = temp;
		
		temp = (((double) data.get("bmm150_z[microT]").intValue()) + offset_z);
		if (Math.abs(temp - z) >= delta_z) {
			rapidDataCacheXDK.append(";MagZ=" + vformat.format(temp) + ",uT");
		}
		z = temp;
		
		if (rapidDataCacheXDK.length() > 0) {
			return "|DEVICEID=" + prefix_id + "-3-0" + ";TIMESTAMP=" + timestamp + rapidDataCacheXDK.toString();
		}
		return null;
	}

	@Override
	public Map<String, String> getWebConsoleValue() {
		Map<String, String> data = new Hashtable<String, String>();
		data.put("bmm150_time", String.valueOf(timestampXDK));
		data.put("bmm150_x", vformat.format(x));
		data.put("bmm150_y", vformat.format(y));
		data.put("bmm150_z", vformat.format(z));
		return data;
	}
};

class BME280 extends XDKSensor {
	public static int CH = 4;
	public double hum   = 0;
	public double press = 0;
	public double temp  = 0;
	
	boolean able_hum , able_press, able_temp;
	double offset_hum, offset_press, offset_temp, delta_hum, delta_press, delta_temp;
	
	public void refreshSetings() {
		offset_hum = (offset.containsKey("bme280_hum") ? offset.get("bme280_hum") : 0);
		offset_press = (offset.containsKey("bme280_press") ? offset.get("bme280_press") * 1000 : 0);
		offset_temp = (offset.containsKey("bme280_temp") ? offset.get("bme280_temp") * 1000 : 0);
		
		delta_hum = (delta.containsKey("bme280_hum") ? delta.get("bme280_hum") : 0);
		delta_press = (delta.containsKey("bme280_press") ? delta.get("bme280_press") : 0);
		delta_temp = (delta.containsKey("bme280_temp") ? delta.get("bme280_temp") : 0);
	}
	
	public String createDataToServer(String prefix_id, long timestamp) {
		
		StringBuilder data = new StringBuilder();
		data.append("|DEVICEID=" + prefix_id + "-4-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Humidity=" + vformat.format(hum) + ",%");
		data.append(";Atmospheric Pressure=" + vformat.format(press) + ",kPa");
		data.append(";Temperature=" + vformat.format(temp) + ",C");
		return data.toString();
	}
	
	@Override
	public String value(Map<String, Number> data, String prefix_id, long timestamp)  {
		timestampXDK = data.get("Timestamp[ms]").longValue();
		rapidDataCacheXDK.setLength(0);
		double tempVal = 0;
		
		tempVal = (((double) data.get("bme280_hum[rh]").intValue()) + offset_hum);
		if (Math.abs(tempVal - hum) >= delta_hum) {
			rapidDataCacheXDK.append(";Humidity=" + vformat.format(tempVal) + ",%");
		} 
		hum = tempVal;
		
		tempVal = (((double) data.get("bme280_press[Pa]").intValue()) + offset_press)/1000;
		if (Math.abs(tempVal - press) >= delta_press) {
			rapidDataCacheXDK.append(";Atmospheric Pressure=" + vformat.format(tempVal) + ",kPa");
		} 
		press = tempVal;
		
		tempVal = (((double) data.get("bme280_temp[mDeg]").intValue()) + offset_temp)/1000;
		if (Math.abs(tempVal - temp) >= delta_temp) {
			rapidDataCacheXDK.append(";Temperature=" + vformat.format(tempVal) + ",C");
		} 
		temp = tempVal;
		
		if (rapidDataCacheXDK.length() > 0) {
			return "|DEVICEID=" + prefix_id + "-4-0" + ";TIMESTAMP=" + timestamp + rapidDataCacheXDK.toString();
		}
		return null;
		
	}

	@Override
	public Map<String, String> getWebConsoleValue() {
		Map<String, String> data = new Hashtable<String, String>();
		data.put("bme280_time", String.valueOf(timestampXDK));
		data.put("bme280_hum", vformat.format(hum));
		data.put("bme280_press", vformat.format(press));
		data.put("bme280_temp", vformat.format(temp));
		return data;
	}
};

class MAX44009 extends XDKSensor {
	public static int CH = 5;
	public double lux = 0;
	
	boolean able_lux ;
	double offset_lux, delta_lux;
	
	public void refreshSetings() {
		offset_lux = (offset.containsKey("max44009_bright") ? offset.get("max44009_bright") * 1000 : 0);
		delta_lux = (delta.containsKey("max44009_bright") ? delta.get("max44009_bright") : 0);
	}
	
	public String createDataToServer(String prefix_id, long timestamp) {		
		StringBuilder data = new StringBuilder();
		data.append("|DEVICEID=" + prefix_id + "-5-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Lux=" + vformat.format(lux) + ",lux");
		return data.toString();
	}
	
	@Override
	public String value(Map<String, Number> data, String prefix_id, long timestamp)  {
		timestampXDK = data.get("Timestamp[ms]").longValue();
		rapidDataCacheXDK.setLength(0);
		double temp = (((double) data.get("max44009_bright[mLux]").intValue()) + offset_lux)/1000;
		if (Math.abs(temp - lux) >= delta_lux) {
			rapidDataCacheXDK.append(";Lux=" + vformat.format(temp) + ",lux");
		} 
		lux = temp;
		
		if (rapidDataCacheXDK.length() > 0) {
			return "|DEVICEID=" + prefix_id + "-5-0" + ";TIMESTAMP=" + timestamp + rapidDataCacheXDK.toString();
		}
		return null;
	}

	@Override
	public Map<String, String> getWebConsoleValue() {
		Map<String, String> data = new Hashtable<String, String>();
		data.put("max44009_time", String.valueOf(timestampXDK));
		data.put("max44009_bright", vformat.format(lux));
		return data;
	}
}

class AKU340 extends XDKSensor {
	public static int CH = 6;
	public double sound = 0;
	
	boolean able_sound ;
	double offset_sound, delta_sound;
	
	public void refreshSetings() {
		offset_sound = (offset.containsKey("aku340_sound") ? offset.get("aku340_sound") : 0);
		delta_sound = (delta.containsKey("aku340_sound") ? delta.get("aku340_sound") : 0);
	}
	public String createDataToServer(String prefix_id, long timestamp) {
		StringBuilder data = new StringBuilder();
		data.append("|DEVICEID=" + prefix_id + "-6-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Noise=" + vformat.format(sound) + ",dBA");
		return data.toString();
	}
	
	@Override
	public String value(Map<String, Number> data, String prefix_id, long timestamp) {
		timestampXDK = data.get("Timestamp[ms]").longValue();
		rapidDataCacheXDK.setLength(0);
		double temp = data.get("aku340_sound[dB]").doubleValue() + offset_sound;
		if (Math.abs(temp - sound) >= delta_sound) {
			rapidDataCacheXDK.append(";Noise=" + vformat.format(temp) + ",dBA");
		} 
		sound = temp;
		
		if (rapidDataCacheXDK.length() > 0) {
			return "|DEVICEID=" + prefix_id + "-6-0" + ";TIMESTAMP=" + timestamp + rapidDataCacheXDK.toString();
		}
		return null;
	}

	@Override
	public Map<String, String> getWebConsoleValue() {
		Map<String, String> data = new Hashtable<String, String>();
		data.put("aku340_time", String.valueOf(timestampXDK));
		data.put("aku340_sound", vformat.format(sound));
		return data;
	}
}

class XDKButton {
	public StringBuilder rapidDataCacheXDK = new StringBuilder();
	public long timestamp = 0;
	public static int CH = 7;
	public int button1 = 0;
	public int button2 = 0;
	public String createDataToServer(String prefix_id, long timestamp) {
		StringBuilder data = new StringBuilder();
		data.append("|DEVICEID=" + prefix_id + "-7-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Relay Status=" + button1 + ",None");
		data.append("|DEVICEID=" + prefix_id + "-7-1");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Relay Status=" + button2 + ",None");
		return data.toString();
	}
	
	public String value(Map<String, Number> data, String prefix_id, long timestamp) {
		timestamp = data.get("Timestamp[ms]").longValue();
		rapidDataCacheXDK.setLength(0);
		
		int temp =  data.get("button1").intValue();
		if (temp != button1) {
			rapidDataCacheXDK.append("|DEVICEID=" + prefix_id + "-7-0");
			rapidDataCacheXDK.append(";TIMESTAMP=" + timestamp);
			rapidDataCacheXDK.append(";Relay Status=" + temp + ",None");
		}
		button1 = temp;
		
		temp = data.get("button2").intValue();
		if (temp != button2) {
			rapidDataCacheXDK.append("|DEVICEID=" + prefix_id + "-7-1");
			rapidDataCacheXDK.append(";TIMESTAMP=" + timestamp);
			rapidDataCacheXDK.append(";Relay Status=" + temp + ",None");
		}
		button2 = temp;
		
		if (rapidDataCacheXDK.length() > 0) {
			return rapidDataCacheXDK.toString();
		}
		return null;
		
	}

	public Map<String, String> getWebConsoleValue() {
		Map<String, String> data = new Hashtable<String, String>();
		data.put("XDKButton_time", String.valueOf(timestamp));
		data.put("button1", String.valueOf(button1));
		data.put("button2", String.valueOf(button2));
		return data;
	}
}
