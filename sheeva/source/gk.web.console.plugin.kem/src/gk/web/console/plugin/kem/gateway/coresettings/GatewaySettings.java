package gk.web.console.plugin.kem.gateway.coresettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import com.greenkoncepts.gateway.util.ConfigFileFormat;
import com.greenkoncepts.gateway.util.Util;

public class GatewaySettings
{
  private Properties bridgeProps;
  private File bridgeConfigFile;
  private Properties loggerProps;
  private File loggerConfigFile;
  private Properties protocolProps;
  private File protocolConfigFile;
  private String bridgeProtocol ;
  private final String socketBridge = "SocketBridge.prop";
  private final String mqttBridge = "MqttClientWaitBridge.prop";
  private final String fileLogger = "FileLogger.prop";
  private final String bridgeMaster = "BridgeMasterImp.prop";
	
  private String macAddress ;
  
  public GatewaySettings(){
	  initialize();
  }
  
  public GatewaySettings(String protocol){
	  initialize2(protocol);
  }
  
  private void initialize() { // use load settings from Files
	  //check bridgeMaster file
	  bridgeConfigFile = new File(bridgeMaster);
	  bridgeProps = Util.getPropertiesFile(bridgeConfigFile);
	  bridgeProtocol = getCurrentProtocol(Integer.parseInt(bridgeProps.getProperty("bridgeMode")));
	  
	  if (bridgeProtocol.equals("socket")) {
		  protocolConfigFile = new File(socketBridge);
	  } else if (bridgeProtocol.equals("mqtt")) {
		  protocolConfigFile = new File(mqttBridge);
	  }
	  protocolProps = Util.getPropertiesFile(protocolConfigFile);
	  //check GatewayID is blank or not
	  if(protocolProps.getProperty("gatewayId").equals("")){
		  //get Macaddress from device
		  macAddress = Util.getMac();
		  if(macAddress == null || macAddress.equals("") )
		  {
			  try {
				  macAddress = Util.getMacByScript();
			  } catch (IOException e) {
				  // TODO Auto-generated catch block
				  e.printStackTrace();
			  }
		  }
		  //set to properties
		  protocolProps.setProperty("gatewayId", macAddress);
	  }
	  
	  //check file logger
	  loggerConfigFile = new File(fileLogger);
	  loggerProps = Util.getPropertiesFile(loggerConfigFile);
  }
  
  private void initialize2(String protocol) { // use to write Settings to File
	//check bridgeMaster file
	  bridgeConfigFile = new File(bridgeMaster);
	  bridgeProps = Util.getPropertiesFile(bridgeConfigFile);
	  
	  if (protocol.equals("socket")) {
		  protocolConfigFile = new File(socketBridge);
		  bridgeProps.setProperty("bridgeMode", "0");
	  } else if (protocol.equals("mqtt")) {
		  protocolConfigFile = new File(mqttBridge);
		  bridgeProps.setProperty("bridgeMode", "1");
	  }
	  bridgeProtocol = protocol;
	  protocolProps = Util.getPropertiesFile(protocolConfigFile);
	  //check GatewayID is blank or not
	  if(protocolProps.getProperty("gatewayId").equals("")){
		  //get Macaddress from device
		  macAddress = Util.getMac();
		  if(macAddress == null || macAddress.equals("") )
		  {
			  try {
				  macAddress = Util.getMacByScript();
			  } catch (IOException e) {
				  // TODO Auto-generated catch block
				  e.printStackTrace();
			  }
		  }
		  //set to properties
		  protocolProps.setProperty("gatewayId", macAddress);
	  }
	  
	  //check file logger
	  loggerConfigFile = new File(fileLogger);
	  loggerProps = Util.getPropertiesFile(loggerConfigFile);
	  

  }

  public Properties toProperties()
  {
    Properties gwSettingsProps = new Properties();
    gwSettingsProps.putAll(protocolProps);    
    gwSettingsProps.putAll(loggerProps);
    gwSettingsProps.putAll(bridgeProps);
    return gwSettingsProps;
  }
  
  public ArrayList<String> validate(File errorMessages, File verifier) 
  {
    GatewaySettingsValidator validator = new GatewaySettingsValidator(protocolProps);
    validator.setCheckingValidate(errorMessages, verifier);
    return validator.validate();
  }
  
  public void update(Properties props)
  {
    for (String key : this.protocolProps.stringPropertyNames())
    {
      String value = props.getProperty(key);
      if (value != null) {
         this.protocolProps.setProperty(key, value);
      }
    }
    if (props.getProperty("debug") != null) {
        this.loggerProps.setProperty("debug", props.getProperty("debug"));
    }
    if (props.getProperty("enableLogServer") != null) {
        this.loggerProps.setProperty("enableLogServer", props.getProperty("enableLogServer"));
    }
    if (props.getProperty("remoteLogServer") != null) { // in some cases , this field is null
        this.loggerProps.setProperty("remoteLogServer", props.getProperty("remoteLogServer"));
    } else {
        props.setProperty("remoteLogServer", this.loggerProps.getProperty("remoteLogServer"));
    }
    if (props.getProperty("bridgeMode") != null) {
        this.bridgeProps.setProperty("bridgeMode", props.getProperty("bridgeMode"));
    }
  }
  	
	public long getLastModified() {
		return protocolConfigFile.lastModified();
	}
	
	public String getCurrentProtocol(int mode) {
		if (mode == 0) {
			return "socket";
		} else {
			return "mqtt";
		}
	}
	
	public String getCurrentBridgeMode() {
		return bridgeProps.getProperty("bridgeMode");
	}
	public void save()
	{
		ConfigFileFormat writeFormat = new ConfigFileFormat();
		writeFormat.setConfigFile(protocolConfigFile, protocolProps); // Socket and MQTT 
		writeFormat.writeGatewaySettingsToFile(bridgeProtocol, "#Configuration file for " + protocolConfigFile.getName());
		writeFormat.setConfigFile(loggerConfigFile, loggerProps); // Logger 
		writeFormat.writeGatewaySettingsToFile("logger", "#Configuration file for " + loggerConfigFile.getName());
		writeFormat.setConfigFile(bridgeConfigFile, bridgeProps); // Bridge Master 
		writeFormat.writeGatewaySettingsToFile("", "#Configuration file for " + bridgeConfigFile.getName());
	}
}