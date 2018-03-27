package com.greenkoncepts.gateway.adapter.dummy;

public class GKProtocol {
	public final static int MSG_MAX_SIZE = 100*1024;	// 100k

	public final static String DEVICE_UNKNOW = "0";

	// Electricity Meter series
	public final static String DEVICE_ES60 = "1000";	// HUABANG_DDS228
	public final static String DEVICE_ET100 = "1001";	// HUABANG_DTS866
	public final static String DEVICE_EM100 = "1002"; 	// ECOSAIL CM-S16E
	public final static String DEVICE_PM100_3 = "1003";  // DENT_POWERSCOUT_3
	public final static String DEVICE_PM100_18 = "1004"; 	// DENT_POWERSCOUT_18
	public final static String DEVICE_PQ100 = "1005";	// J&D POWER QUALITY METER
	public final static String DEVICE_PM100_3p = "1006"; 	// DENT_POWERSCOUT_3+

	public final static String DEVICE_EM101 = "1009"; 	// ECOSAIL SCPM-S16
	public final static String DEVICE_PFMDPM = "1010";	// PFM-DPM METER
	public final static String DEVICE_EM200 = "1011"; 	// ECOSAIL SCPM-M12
	public final static String DEVICE_PM200 = "1012";	// ECOSAIL SCPM-T12
	public final static String DEVICE_MPR46S = "1013";	//Entes MPR46S
	public final static String DEVICE_EM300_6 = "1016";	//ECOSAIL SCPM-S6
	public final static String DEVICE_ISAST_MULTI_GEM18 = "1017";	//iSAST Multi-GEM18

	public final static String DEVICE_JANITZA_UMG96S = "1014"; //Janitza UMG96S

	public final static String DEVICE_SCHNEIDER_PM710 ="1021"; // Schneider Power Meter PM710
	public final static String DEVICE_SCHNEIDER_IEM3X00_SERIES ="1022";
	public final static String DEVICE_SCHNEIDER_ION7650 ="1023";
	public final static String DEVICE_SCHNEIDER_PM2200 ="1024";
	public final static String DEVICE_SCHNEIDER_PM1200 ="1026";



	public final static String DEVICE_INEPRO_PRO1250D = "1025"; // Dmmetering INEPRO 1250D

	public final static String DEVICE_SOCOMEC_COUNTISE44 = "1060"; // Socomec
	
	public final static String DEVICE_EPOWER_ECM770 = "1070";


	// Sensor series
	public final static String DEVICE_TSA01 = "2000"; 	// 16 channel Temperature and Humidity sensor
	public final static String DEVICE_TST01 = "2001"; 	// 16 channel Temperature sensor
	public final static String DEVICE_TSA02 = "2002"; 	// 1 channel Temperature and Humidity sensor
	public final static String DEVICE_TST02 = "2003"; 	// 1 channel Temperature sensor
	public final static String DEVICE_LS01 = "2004"; 	// 1 channel Lux sensor
	
	public final static String DEVICE_BOSCH_XDK = "2050"; 	// Bosch XDK
	public final static String DEVICE_MONNIT_TEMPERATURE = "2051"; 	// Monnit
	public final static String DEVICE_MONNIT_VIBRATION = "2052"; 	// Monnit

	public final static String DEVICE_PHIDGETS_IK888 = "2010";
	// IO series
	public final static String DEVICE_DI_16 = "3000"; 	// 16 channel digital input
	public final static String DEVICE_DI_16_STATE = "3002"; 	// 16 channel digital input to check 1/0 of input state
	public final static String DEVICE_AII_8 = "3001"; 	// 8 channel digital input
	public final static String DEVICE_DAIO_8 = "3003"; 	// 8 channel digital input
	// Device control module
	public final static String DEVICE_DCM01 = "4000"; 	// DCM device

	// BTU meter
	public final static String DEVICE_AKEC03P = "5000"; // AKE BTU Meter
	public final static String DEVICE_BTU_CONTREC_MODEL212 = "5001";
	public final static String DEVICE_SIEMENS_SITRANS_FUE950 = "5002";
	public final static String DEVICE_SIEMENS_SITRANS_MAG6000 = "5003";
	
	public final static String DEVICE_AQUAMETRO_AMTRONMAG = "5010";// Aquametro
	public final static String DEVICE_AQUAMETRO_CALECST2 = "5011";// CalecstII


	// Water quality meter series
	public final static String DEVICE_WATER_QUALITY_NGEE_ANN_PLC = "6003";

	// Water meter series
	public final static String DEVICE_WATER_BAYLAN_MODBUS_MODULE = "7001";
	public final static String DEVICE_WATER_PFLOWCA20 = "7020";
	public final static String DEVICE_WATER_GEAQUATRANS_AT600 = "7021";
	public final static String DEVICE_WATER_SITELAB_SL1168 = "7026";
	public final static String DEVICE_WATER_VALTECHNIK = "7030"; // using Mbus converter
	public final static String DEVICE_WATER_HD67029M_485_20 = "7031"; // using Mbus converter
	
	public final static String DEVICE_HART2MODBUS_HCS = "7032"; //  convert Hart to modbus RTU
	
	public final static String DEVICE_DAIKIN_MIRCOTECH2 = "7050";

	public final static String DEVICE_EMERSON_SMART_WIRELESS_GATEWAY = "7100";
	public final static String DEVICE_EMERSON_LIEBERTCRV_CRAC = "7101";
	public final static String DEVICE_EMERSON_LIEBERTPEX_CRAC = "7102";
	public final static String DEVICE_EMERSON_LIEBERTNXR_UPS = "7103";
	public final static String DEVICE_APC_SYMMETRA_UPS = "7104";

	// Uninterruptible Power Supply (UPS)
	public final static String DEVICE_MULTICOM_30X = "8001";

	// Legacy
	public final static String DEVICE_VERIS_E50 = "9001";	// Legacy model

	// Bacnet Device
	public final static String DEVICE_BACNET_TRANE = "10000"; // Trane BAS
	
	// Rest Device
	public final static String DEVICE_REST_API_MSE = "11000"; // MSE


	public final static int DEVICE_STATUS_UNKNOWN = 0;
	public final static int DEVICE_STATUS_ERROR = 1;
	public final static int DEVICE_STATUS_ONLINE = 2;
	public final static int DEVICE_STATUS_OFFLINE = 3;

}
