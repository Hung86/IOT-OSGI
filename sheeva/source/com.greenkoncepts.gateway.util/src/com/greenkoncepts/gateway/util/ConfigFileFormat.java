package com.greenkoncepts.gateway.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import javax.servlet.ServletOutputStream;

public class ConfigFileFormat {
	private File _configFile;
	private Properties _props;

	public ConfigFileFormat() {
	}

	public ConfigFileFormat(File configFile, Properties props) {
		this();
		_configFile = configFile;
		_props = props;
	}

	public void setConfigFile(File configFile, Properties props) {
		_configFile = configFile;
		_props = props;
	}

	public void writeAdapterSettingsToFile(String comment) {
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		
		try {
			ArrayList<String> _commonLines = new ArrayList<String>();
			ArrayList<String> _deviceLines = new ArrayList<String>();
			ArrayList<String> _datapoint = new ArrayList<String>();

			_commonLines.add("serial_port");
			_commonLines.add("serial_baudrate");
			_commonLines.add("serial_stopbit");
			_commonLines.add("serial_parity");
			_commonLines.add("port");
			_commonLines.add("auto_mode");
			_commonLines.add("query_timeout");
			_commonLines.add("device_num");

			Properties prop = (Properties) _props.clone();
			int channelNum = 0;
			int subchannelNum = 0;
			int datapointNum = 0;
			int deviceNum = Integer.parseInt(prop.getProperty("device_num", "0"));
			for (int i = 0; i < deviceNum; i++) {
				_deviceLines.add("device_" + i + "_address");
				_deviceLines.add("device_" + i + "_id");
				_deviceLines.add("device_" + i + "_delegateid");
				_deviceLines.add("device_" + i + "_category");
				_deviceLines.add("device_" + i + "_version");
				_deviceLines.add("device_" + i + "_channel");
				channelNum = Integer.parseInt(prop.getProperty("device_" + i + "_channel", "0"));
				for (int k = 0; k < channelNum; k++) {
					_deviceLines.add("device_" + i + "_" + k + "_subchannel");
					subchannelNum = Integer.parseInt(prop.getProperty("device_" + i + "_" + k + "_subchannel", "1"));
					for (int m = 0; m < subchannelNum; m++) {
						_deviceLines.add("device_" + i + "_" + k + "_" + m + "_datapoint");
						datapointNum = Integer.parseInt(prop.getProperty("device_" + i + "_" + k + "_" + m + "_datapoint", "1"));
						for (int n = 0; n < datapointNum; n++) {
							String prefix = "device_" + i + "_" + k + "_" + m + "_" + n + "_";
							for (String key : prop.stringPropertyNames()) {
								if (key.startsWith(prefix)) {
									_datapoint.add(key);
								}
							}
							Collections.sort(_datapoint);
						}
						_deviceLines.addAll(_datapoint);
						_datapoint.clear();
					}
				}
				
				if (channelNum == 0) {
					String pattern = "device_" + i + "_\\d*_\\d*_\\d*_\\w*";
					for (String key : prop.stringPropertyNames()) {
						if (key.matches(pattern)) {
							_datapoint.add(key);
						}
					}
					if (!_datapoint.isEmpty()) {
						Collections.sort(_datapoint);
						_deviceLines.addAll(_datapoint);
						_datapoint.clear();
					}
				}

			}
			fos = new FileOutputStream(_configFile);
			osw = new OutputStreamWriter(fos);
			String propKey = null;
			String propValue = null;
			osw.write(comment + "\r\n");
			osw.write("# " + new SimpleDateFormat("MMMM d, yyyy 'at' h:mm:ss a").format(System.currentTimeMillis()).toString() + "\r\n");
			for (int i = 0; i < _commonLines.size(); i++) {
				propKey = _commonLines.get(i);
				propValue = prop.getProperty(propKey);
				if (propValue != null) {
					osw.write(propKey + "=" + propValue + "\r\n");
					prop.remove(propKey);
				}
			}
			osw.write("\r\n");
			osw.write("#Device Settings ...\r\n");

			for (int k = 0; k < _deviceLines.size(); k++) {
				propKey = _deviceLines.get(k);
				propValue = prop.getProperty(propKey);
				if (propValue != null) {
					osw.write(propKey + "=" + propValue + "\r\n");
					prop.remove(propKey);
				}
			}

			osw.write("\r\n");

			osw.write("#Somethings else ...\r\n");
			for (String key : prop.stringPropertyNames()) {
				propValue = prop.getProperty(key);
				if (propValue != null) {
					osw.write(key + "=" + propValue + "\r\n");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (osw != null) {
				try {
					osw.close();
				} catch (IOException e) {
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public void writeGatewaySettingsToFile(String mode, String comment) {
		ArrayList<String> formatList = new ArrayList<String>();
		if (mode.equals("socket")) {
			formatList.add("protocolVersion");
			formatList.add("customerId");
			formatList.add("gatewayId");
			formatList.add("socketErrMax");
			formatList.add("uartErrMax");
			formatList.add("checkPeriod");
			formatList.add("remoteHost");
			formatList.add("remotePort");
			formatList.add("bufferSendLimit");
		} else if (mode.equals("mqtt")) {
			formatList.add("customerId");
			formatList.add("gatewayId");
			formatList.add("uartErrMax");
			formatList.add("checkPeriod");
			formatList.add("checkPeriod_internet");
			formatList.add("mqtt_topic");
			formatList.add("mqtt_subtopic");
			formatList.add("mqtt_qos");
			formatList.add("mqtt_clientid");
			formatList.add("mqtt_ssl");
			formatList.add("mqtt_keepaliveinterval");
			formatList.add("mqtt_connectiontimeout");
			formatList.add("mqtt_cleansession");
			formatList.add("remoteHost");
			formatList.add("remotePort");
			formatList.add("bufferSendLimit");
			formatList.add("saverecord");
		} else if (mode.equals("logger")) {
			formatList.add("debug");
			formatList.add("enableLogServer");
			formatList.add("remoteLogServer");
		}
		Properties prop = (Properties) _props.clone();
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		try {
			fos = new FileOutputStream(_configFile);
			osw = new OutputStreamWriter(fos);

			osw.write(comment + "\r\n");
			osw.write("# " + new SimpleDateFormat("MMMM d, yyyy 'at' h:mm:ss a").format(System.currentTimeMillis()).toString() + "\r\n");
			String name = null;
			String propValue = null;
			for (int i = 0; i < formatList.size(); i++) {
				name = formatList.get(i);
				propValue = prop.getProperty(name);
				if (propValue != null) {
					osw.write(name + "=" + propValue + "\r\n");
					prop.remove(name);
				}
			}
			for (String key : prop.stringPropertyNames()) {
				propValue = prop.getProperty(key);
				if (propValue != null) {
					osw.write(key + "=" + propValue + "\r\n");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (osw != null) {
				try {
					osw.close();
				} catch (IOException e) {
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public void exportToOutputStream(String mode, Properties props, ServletOutputStream ost, String comment) throws IOException {
		String content = comment;
		String name = null;
		String propValue = null;
		if (mode.equals("GatewaySettings")) {
			ArrayList<String> formatList = new ArrayList<String>();
			formatList.add("bridgeMode");
			if (props.getProperty("bridgeMode").equals("socket")) {
				formatList.add("protocolVersion");
				formatList.add("customerId");
				formatList.add("gatewayId");
				formatList.add("socketErrMax");
				formatList.add("uartErrMax");
				formatList.add("checkPeriod");
				formatList.add("remoteHost");
				formatList.add("remotePort");
				// formatList.add("bufferSendLimit");//hide
				// formatList.add("bufferCapacityDays");
				// formatList.add("loopCountMax");
			} else if (props.getProperty("bridgeMode").equals("mqtt")) {
				formatList.add("customerId");
				formatList.add("gatewayId");
				formatList.add("uartErrMax");
				formatList.add("checkPeriod");
				// formatList.add("checkPeriod_internet");
				formatList.add("mqtt_topic");
				formatList.add("mqtt_subtopic");
				formatList.add("mqtt_qos");
				formatList.add("mqtt_clientid");
				formatList.add("mqtt_ssl");
				// formatList.add("mqtt_keepaliveinterval");
				// formatList.add("mqtt_connectiontimeout");
				// formatList.add("mqtt_cleansession");//hide
				formatList.add("remoteHost");
				formatList.add("remotePort");
				// formatList.add("bufferSendLimit");//hide
				// formatList.add("bufferFile");
				// formatList.add("saverecord");//hide
				// formatList.add("loopCountMax");
			}
			formatList.add("debug");
			formatList.add("enableLogServer");
			formatList.add("remoteLogServer");

			ost.write(content.getBytes());
			for (int i = 0; i < formatList.size(); i++) {
				name = formatList.get(i);
				propValue = props.getProperty(name);
				if (propValue != null) {
					content = name + "=" + propValue + "\r\n";
					ost.write(content.getBytes());
				}
			}
		} else if (mode.equals("AdapterSettings")) {
			ArrayList<String> _commonLines = new ArrayList<String>();
			ArrayList<String> _deviceLines = new ArrayList<String>();
			ArrayList<String> _datapoint = new ArrayList<String>();

			_commonLines.add("serial_port");
			_commonLines.add("serial_baudrate");
			_commonLines.add("serial_stopbit");
			_commonLines.add("serial_parity");
			_commonLines.add("port");
			_commonLines.add("auto_mode");
			_commonLines.add("query_timeout");
			_commonLines.add("device_num");

			Properties prop = (Properties) props.clone();
			int channelNum = 0;
			int subchannelNum = 0;
			int datapointNum = 0;
			int deviceNum = Integer.parseInt(prop.getProperty("device_num", "0"));
			for (int i = 0; i < deviceNum; i++) {
				_deviceLines.add("device_" + i + "_address");
				_deviceLines.add("device_" + i + "_id");
				_deviceLines.add("device_" + i + "_delegateid");
				_deviceLines.add("device_" + i + "_category");
				_deviceLines.add("device_" + i + "_version");
				_deviceLines.add("device_" + i + "_channel");
				channelNum = Integer.parseInt(prop.getProperty("device_" + i + "_channel", "0"));
				for (int k = 0; k < channelNum; k++) {
					_deviceLines.add("device_" + i + "_" + k + "_subchannel");
					subchannelNum = Integer.parseInt(prop.getProperty("device_" + i + "_" + k + "_subchannel", "1"));
					for (int m = 0; m < subchannelNum; m++) {
						_deviceLines.add("device_" + i + "_" + k + "_" + m + "_datapoint");
						datapointNum = Integer.parseInt(prop.getProperty("device_" + i + "_" + k + "_" + m + "_datapoint", "1"));
						for (int n = 0; n < datapointNum; n++) {
							String prefix = "device_" + i + "_" + k + "_" + m + "_" + n + "_";
							for (String key : prop.stringPropertyNames()) {
								if (key.startsWith(prefix)) {
									_datapoint.add(key);
								}
							}
							Collections.sort(_datapoint);
						}
						_deviceLines.addAll(_datapoint);
						_datapoint.clear();
					}
				}
				
				if (channelNum == 0) {
					String pattern = "device_" + i + "_\\d*_\\d*_\\d*_\\w*";
					for (String key : prop.stringPropertyNames()) {
						if (key.matches(pattern)) {
							_datapoint.add(key);
						}
					}
					if (!_datapoint.isEmpty()) {
						Collections.sort(_datapoint);
						_deviceLines.addAll(_datapoint);
						_datapoint.clear();
					}
				}
			}
			
			ost.write(content.getBytes());
			for (int i = 0; i < _commonLines.size(); i++) {
				name = _commonLines.get(i);
				propValue = prop.getProperty(name);
				if (propValue != null) {
					content = name + "=" + propValue + "\r\n";
					ost.write(content.getBytes());
					prop.remove(name);
				}
			}
			ost.write("\r\n".getBytes());

			for (int k = 0; k < _deviceLines.size(); k++) {
				name = _deviceLines.get(k);
				propValue = prop.getProperty(name);
				if (propValue != null) {
					content = name + "=" + propValue + "\r\n";
					ost.write(content.getBytes());
					prop.remove(name);
				}
			}

			ost.write("\r\n".getBytes());
			
			for (String key : prop.stringPropertyNames()) {
				propValue = prop.getProperty(key);
				if (propValue != null) {
					content = key + "=" + propValue + "\r\n";
					ost.write(content.getBytes());
				}
			}
		}
	}

}
