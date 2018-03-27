package com.greenkoncepts.gateway.adapter.monnit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.IDevice;

public class Temperature extends MonnitDevice {
	private double value = -1;
	
	public Temperature(int id, String category, String gwid) {
		super(id, category);
		gatewayid = gwid;
	}

	@Override
	public String getDeviceData() {
		if (getStatus() != IDevice.DEVICE_STATUS_ONLINE) {
			return "|DEVICEID=" + getId() + ";ERROR=Communication timeout";
		}
		
		
		if ((messageDate != null) && (messageDate.equals(""))) {
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date parsedDate = dateFormat.parse(messageDate);
				timestamp = parsedDate.getTime();
			} catch (ParseException e) {
				mLogger.info("ParseException", e);
				timestamp = System.currentTimeMillis();
			}
			
		} else {
			timestamp = System.currentTimeMillis();
		}
		
		StringBuffer data = new StringBuffer();
		data.append("|DEVICEID=" + getId() + "-0-0");
		data.append(";TIMESTAMP=" + timestamp);
		data.append(";Temperature=" + vformat.format(value) + ",C");
		return data.toString();
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx,
			boolean isRefesh) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean setDataSensors(Object dataJson) {
		if (dataJson != null) {
			Map<String, Object> data = (Map<String, Object>) dataJson; 
			
			Map<String, String> gatewaymsg = (Map<String, String>) data.get("gatewayMessage");
			mLogger.info("gatewaymsg = " + gatewaymsg);
			
			if (gatewaymsg != null) {
				if ( gatewayid.equals(gatewaymsg.get("gatewayID"))) {
					List<Map<String, String>> sensormsg = (List<Map<String, String>>) data.get("sensorMessages");
					mLogger.info("sensormsg = " + sensormsg);
					if (sensormsg != null) {
						for (Map<String, String> sensor : sensormsg) {
							if (Integer.parseInt(sensor.get("sensorID")) == deviceid) {
								value = Double.parseDouble(sensor.get("dataValue"));
								messageDate = sensor.get("messageDate");
								hasValue = true;
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public void setDeviceAttributes(Object data) {
		// TODO Auto-generated method stub
		
	}

}
