//package com.greenkoncepts.gateway.adapter.monnit;
//
//import java.text.DecimalFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Hashtable;
//import java.util.List;
//import java.util.Map;
//
//import com.greenkoncepts.gateway.protocol.GKProtocol;
//
//public class Hoestar extends MonnitDevice {
//	
//	private int gatewayid;
//	private String measurement;
//	private String unit;
//	private String messageDate;
//	private double value;
//	public DecimalFormat vformat = new DecimalFormat("#########0.0000");
//	public Hoestar(int id) {
//		super(id, GKProtocol.DEVICE_MONNIT_HOESTAR);
//	}
//
//	@Override
//	public String getDeviceData() {
//		return createDataToServer();
//	}
//
//	@Override
//	public List<Map<String, String>> getRealTimeData(int dataIdx,
//			boolean isRefesh) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public boolean setDataSensors(Object dataJson) {
//		mLogger.info("dataJson = " + dataJson);
//		if (dataJson != null) {
//			Map<String, Object> data = (Map<String, Object>) dataJson; 
//			
//			Map<String, String> gatewaymsg = (Map<String, String>) data.get("gatewayMessage");
//			mLogger.info("gatewaymsg = " + gatewaymsg);
//			if (gatewaymsg != null) {
//				if ( gatewayid == Integer.valueOf(gatewaymsg.get("gatewayID"))) {
//					List<Map<String, String>> sensormsg = (List<Map<String, String>>) data.get("sensorMessages");
//					mLogger.info("sensormsg = " + sensormsg);
//					if (sensormsg != null) {
//						boolean isFound = false;
//						for (Map<String, String> sensor : sensormsg) {
//							if (Integer.parseInt(sensor.get("sensorID")) == deviceid) {
//								isFound = true;
//								value = Double.parseDouble(sensor.get("dataValue"));
//								messageDate = sensor.get("messageDate");
//								mLogger.info("value = " + value + " - messageDate = " + messageDate);
//								break;
//							}
//						}
//						if (isFound) {
//							return true;
//						}
//					}
//				}
//			}
//		}
//		return false;
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public void setDeviceAttributes(Object data) {
//		Map<String, String> attr = (Map<String, String>) data;
//		gatewayid = Integer.parseInt(attr.get("gateway_id"));
//		measurement = attr.get("measurement");
//		unit = attr.get("unit");
//		value = 0;
//	}
//
//	public String createDataToServer() {
//		try {
//			if ((messageDate != null) && (messageDate.equals(""))) {
//				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//				Date parsedDate = dateFormat.parse(messageDate);
//				timestamp = parsedDate.getTime();
//			} else {
//				timestamp = System.currentTimeMillis();
//			}
//			StringBuffer data = new StringBuffer();
//			data.append("|DEVICEID=" + getId() + "-0-0");
//			data.append(";TIMESTAMP=" + timestamp);
//			data.append(";" + measurement + "=" + vformat.format(value) + "," + unit);
//			return data.toString();
//		} catch (ParseException e) {
//			mLogger.error("ParseException", e);
//		} catch (Exception e) {
//			mLogger.error("Exception", e);
//		}
//		
//		return "";
//	}
//}
//
//abstract class HoestarSensor {
//	public String _gatewayid = "-1";
//	public String _category = "-1";
//	public String _sensorid = "-1";
//	public String _messageDate = "";
//	public long _timestamp = -1;
//	public boolean _hasValue = false;
//
//
//	public DecimalFormat vformat = new DecimalFormat("#########0.0000");
//	abstract public String createDataToServer() throws Exception;
//	abstract public void value(List<Map<String, String>> sensormsg);
//	public boolean isValidGateway(String gatewayid) {
//		if (_gatewayid.equals(gatewayid)) {
//			return true;
//		}
//		return false;
//	}
//}
//
//class Temperature extends HoestarSensor {
//	private double value = -1;
//
//	@Override
//	public String createDataToServer() throws Exception {
//		try {
//			if (!_hasValue) {
//				return "|DEVICEID=" + _category + "-" + _sensorid + ";ERROR=Communication timeout";
//			}
//			
//			if ((_messageDate != null) && (_messageDate.equals(""))) {
//				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//				Date parsedDate = dateFormat.parse(_messageDate);
//				_timestamp = parsedDate.getTime();
//			} else {
//				_timestamp = System.currentTimeMillis();
//			}
//			
//			StringBuffer data = new StringBuffer();
//			data.append("|DEVICEID=" + _category + "-" + _sensorid + "-0-0");
//			data.append(";TIMESTAMP=" + _timestamp);
//			data.append(";Temperature=" + vformat.format(value) + ",C");
//			return data.toString();
//		}  catch (Exception e) {
//			throw e;
//		}
//	}
//
//	@Override
//	public void value(List<Map<String, String>> sensormsg) {
//		_hasValue = false;
//		for (Map<String, String> sensor : sensormsg) {
//			if (_sensorid.equals(sensor.get("sensorID"))) {
//				_hasValue = true;
//				value = Double.parseDouble(sensor.get("dataValue"));
//				_messageDate = sensor.get("messageDate");
//				break;
//			}
//		}
//	}
//	
//}
//
//class Vibration extends HoestarSensor {
//	private double X_Axis_Speed = 0;
//	private double Y_Axis_Speed = 0;
//	private double Z_Axis_Speed = 0;
//	private double X_Axis_Frequency = 0;
//	private double Y_Axis_Frequency = 0;
//	private double Z_Axis_Frequency = 0;
//	private double Duty_Cycle = 0;
//	@Override
//	public String createDataToServer() throws Exception {
//		try {
//			if (!_hasValue) {
//				return "|DEVICEID=" + _category + "-" + _sensorid + ";ERROR=Communication timeout";
//			}
//			
//			if ((_messageDate != null) && (_messageDate.equals(""))) {
//				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//				Date parsedDate = dateFormat.parse(_messageDate);
//				_timestamp = parsedDate.getTime();
//			} else {
//				_timestamp = System.currentTimeMillis();
//			}
//			
//			StringBuffer data = new StringBuffer();
//			data.append("|DEVICEID=" + _category + "-" + _sensorid + "-0-0");
//			data.append(";TIMESTAMP=" + _timestamp);
//			data.append(";X-Axis Speed=" + vformat.format(X_Axis_Speed) + ",mm/s");
//			data.append(";Y-Axis Speed=" + vformat.format(Y_Axis_Speed) + ",mm/s");
//			data.append(";Z-Axis Speed=" + vformat.format(Z_Axis_Speed) + ",mm/s");
//			data.append(";X-Axis Frequency=" + vformat.format(X_Axis_Frequency) + ",Hz");
//			data.append(";Y-Axis Frequency=" + vformat.format(Y_Axis_Frequency) + ",Hz");
//			data.append(";Z-Axis Frequency=" + vformat.format(Z_Axis_Frequency) + ",Hz");
//			data.append(";Duty Cycle=" + vformat.format(Duty_Cycle) + ",%");
//			return data.toString();
//		}  catch (Exception e) {
//			throw e;
//		}
//	}
//
//	@Override
//	public void value(List<Map<String, String>> sensormsg) {
//		_hasValue = false;
//		for (Map<String, String> sensor : sensormsg) {
//			if (_sensorid.equals(sensor.get("sensorID"))) {
//				_hasValue = true;
//				String dataValue[] = sensor.get("dataValue").split("|");
//				_messageDate = sensor.get("messageDate");
//				X_Axis_Speed = Double.parseDouble(dataValue[0]);
//				Y_Axis_Speed = Double.parseDouble(dataValue[1]);
//				Z_Axis_Speed = Double.parseDouble(dataValue[2]);
//				X_Axis_Frequency = Double.parseDouble(dataValue[3]);
//				Y_Axis_Frequency = Double.parseDouble(dataValue[4]);
//				Z_Axis_Frequency = Double.parseDouble(dataValue[5]);
//				Duty_Cycle = Double.parseDouble(dataValue[6]);
//				break;
//			}
//		}
//	}
//	
//}
