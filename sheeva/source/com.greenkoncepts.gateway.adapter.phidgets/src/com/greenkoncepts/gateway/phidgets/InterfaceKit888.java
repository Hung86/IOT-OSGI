package com.greenkoncepts.gateway.phidgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greenkoncepts.gateway.api.adapter.IDevice;
import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;
import com.phidgets.event.ErrorEvent;
import com.phidgets.event.ErrorListener;

public class InterfaceKit888 extends PhidgetsDevice{
	
	//Interface kit
	InterfaceKitPhidget ik;
	int ikSerialID;
	
	static int MOTION_TOLERANCE = 30;
//	int maxMotion = 0;
//	int minMotion = 500;
//	boolean occupantStatus = false;
//	int motionTriggerCount = 0;
	static int MOTION_TRIGGER_TOLERANCE = 6;
//	int occupantCount = 0;
	List<List<Integer>> motionValues;//Accumulated motion values
	
	List<Integer> thresholds;
	List<Integer> trigger_mins;
	List<Integer> eventCounts;
	List<Integer> maxMotions;
	List<Integer> minMotions;
	List<Boolean> occupantStatuses;	
	
	private String[][] webconsoleData = new String[8][5];
	
	private ScheduledExecutorService scheduledExecutorService;
	
	private Logger mLogger ;
	
	public InterfaceKit888(String category, int addr, final List<String> inputs) {
		super(category, addr,inputs);
		mLogger = LoggerFactory.getLogger("InterfaceKit888 " + addr);
		ikSerialID = addr;
		motionValues = new ArrayList<List<Integer>>();
		eventCounts = new ArrayList<Integer>();
		maxMotions = new ArrayList<Integer>();
		minMotions = new ArrayList<Integer>();;
		occupantStatuses = new ArrayList<Boolean>();
//		thresholds = new ArrayList<Integer>();
//		trigger_mins = new ArrayList<Integer>();
		for(int i = 0;i < 8;i++){
			if(inputs.get(i).equals("1111")){
				motionValues.add(new ArrayList<Integer>() );
				eventCounts.add(0);
				maxMotions.add(0);
				minMotions.add(0);
				occupantStatuses.add(false);
			}else{
				motionValues.add(null);
				eventCounts.add(null);
				maxMotions.add(null);
				minMotions.add(null);
				occupantStatuses.add(null);
			}
		}
		try {
			ik = new InterfaceKitPhidget();
			
			ik.addAttachListener(new AttachListener() {
				public void attached(AttachEvent ae) {
					mLogger.info("attachment of " + ae);
					try {
			            InterfaceKitPhidget attached = (InterfaceKitPhidget)ae.getSource();
			            mLogger.info("Device Name " +  attached.getDeviceName());
			            mLogger.info("Serial Number " +  attached.getSerialNumber());
			            mLogger.info("Device Version " + attached.getDeviceVersion());
			            mLogger.info("Input Count " + attached.getInputCount());
			            mLogger.info("Output Count " + attached.getOutputCount());
			            mLogger.info("Sensor Count " + attached.getSensorCount());
			            //ik.open(attached.getSerialNumber());
					}catch (Exception e){
						mLogger.error("Exception", e);
					}
				}
			});
			ik.addDetachListener(new DetachListener() {
				public void detached(DetachEvent ae) {
					mLogger.info("detachment of " + ae);
				}
			});
			ik.addErrorListener(new ErrorListener() {
				public void error(ErrorEvent ee) {
					mLogger.error("ErrorEvent "+ee);
				}
			});

			ik.open(ikSerialID);
		} catch (PhidgetException e) {
			mLogger.error("PhidgetException", e);
		}
		scheduledExecutorService = Executors.newScheduledThreadPool(3);
		try{
			scheduledExecutorService.scheduleAtFixedRate(new Runnable(){
				@Override
				public void run() {
					try {
						if(ik.isAttached()){
							//mLogger.info("1 second tick");
							for(int i = 0;i < 8; i++){
								if(input_types.get(i).equals(SENSOR_MOTION_1111)){
									//Handle Motion
									//int currentMotion = ik.getSensorValue(i);
									int currentDeltaMotion = Math.abs(500 - ik.getSensorValue(i));
									//temporary= Math.abs(currentDeltaMotion - lastDeltaMotion) ;
									if(currentDeltaMotion >= thresholds.get(i)){
										//iOListeners.onReceivedReport("|DEVICEID="+DEVICE_TYPE+"-"+ikSerialID+"-2-0;TIMESTAMP="+System.currentTimeMillis()+";Motion Switch=" + currentMotion + ",None");
										synchronized(motionValues){
											if(currentDeltaMotion > maxMotions.get(i)){
												//maxMotion = currentDeltaMotion;
												maxMotions.set(i, currentDeltaMotion);
											}
											if(currentDeltaMotion < minMotions.get(i)){
												//minMotion = currentDeltaMotion;
												minMotions.set(i, currentDeltaMotion);
											}
											motionValues.get(i).add(currentDeltaMotion);
											
										}
										//occupantCount++;
										eventCounts.set(i, eventCounts.get(i) +1);
										//occupantStatus = true;
										mLogger.info("Motion sensor index="+i+" has delta greater than threshold sensitivity with raw value="+currentDeltaMotion+",max value="+maxMotions.get(i)+",events count="+eventCounts.get(i));
										
									}else{
										mLogger.info("Motion sensor index="+i+" has delta less than threshold sensitivity with raw value="+currentDeltaMotion);
									}
								}
							}
								
						}else{
							mLogger.info("Wait for Interfacekit attachment...");
							ik.waitForAttachment(1000);
						}
					} catch (PhidgetException e) {
						mLogger.error("PhidgetException", e);
					}
				}		
			}
			, 0, 1, TimeUnit.SECONDS);
		}catch(RejectedExecutionException e){
			//log(Logger.TYPE_ERROR, "scheduledExecutorService:"+e.toString());
		}catch(NullPointerException e){
			//log(Logger.TYPE_ERROR, "scheduledExecutorService:"+e.toString());
		}catch(IllegalArgumentException e){
			//log(Logger.TYPE_ERROR, "scheduledExecutorService:"+e.toString());
		}
	}
	
	public void setThresholds(List<Integer> input){
		thresholds = input;
	}

	public void setTriggerMin(List<Integer> input){
		trigger_mins = input;
	}
	
	public void stop(){
		if(ik != null){
			try {
				ik.close();
			} catch (PhidgetException e) {
				mLogger.error("PhidgetException", e);
			}
		}
	}
	
	@Override
	public String getDeviceData() {
		mLogger.info("getDeviceData()");
		try {
			if(!ik.isAttached() ){
				mLogger.error("getDeviceData() device is not attached");
				errorCount++;
				return "|DEVICEID="+getId() + ";ERROR=Communication timeout";
			}
		} catch (PhidgetException e1) {
			mLogger.error("getDeviceData() has error" + e1.getDescription());
			errorCount++;
			return "|DEVICEID="+getId() + ";ERROR=Communication timeout";
		}
		StringBuffer data = new StringBuffer();
		
		for(int i = 0;i < 8;i++){
			if(input_types.get(i).equals(SENSOR_MOTION_1111)){
				mLogger.info("motion sensor at channel "+i);
				synchronized(motionValues){
					if(motionValues.get(i).isEmpty()){
						int currentDeltaMotion = 0;
						try {
							currentDeltaMotion = Math.abs(500 - ik.getSensorValue(i));
						} catch (PhidgetException e) {
							mLogger.error("getDeviceData() Exception:" , e);
						}
						//int temporary= Math.abs(currentDeltaMotion - lastDeltaMotion) ;
						int motion = 0;
						if(currentDeltaMotion >= thresholds.get(i)){
							motion = currentDeltaMotion;
							mLogger.info("Motion sensor index="+i+" has delta greater than threshold sensitivity with raw value="+currentDeltaMotion+",max value="+maxMotions.get(i)+",events count="+eventCounts.get(i));
//							maxMotion = currentDeltaMotion;
							maxMotions.set(i, currentDeltaMotion);
//							occupantCount++;
							eventCounts.set(i, eventCounts.get(i) +1);
						}else{
//							maxMotion = 0;
							maxMotions.set(i, 0);
//							occupantCount = 0;
							eventCounts.set(i, eventCounts.get(i) +1);
						}
						if(eventCounts.get(i) >= trigger_mins.get(i))
							//occupantStatus = true;
							occupantStatuses.set(i, true);
						else
							//occupantStatus = false;
							occupantStatuses.set(i,false);
//						iOListeners.onReceivedReport("|DEVICEID="+DEVICE_TYPE+"-"+ikSerialID+"-2-0;TIMESTAMP="+System.currentTimeMillis()+ ";Raw Sensor Value=" +  motion + ",None"+";Max Sensor Value="+  maxMotion + ",None"+";Occupant Status="+(occupantStatus?"1":"0")+",None"+";Event Count=0,None");
						if(occupantStatuses.get(i)){
							data.append("|DEVICEID="+getId()+"-"+i+"-0"+";TIMESTAMP="+System.currentTimeMillis()+ ";Raw Sensor Value=" +  motion + ",None"+";Max Sensor Value="+  maxMotions.get(i) + ",None"+";Occupant Status="+(occupantStatuses.get(i)?"1":"0")+",None"+";Event Count="+eventCounts.get(i)+",None");
						}else{
							data.append("|DEVICEID="+getId()+"-"+i+"-0"+";TIMESTAMP="+System.currentTimeMillis()+ ";Raw Sensor Value=" +  motion + ",None"+";Max Sensor Value="+  maxMotions.get(i) + ",None"+";Event Count="+eventCounts.get(i)+",None");
						}
						webconsoleData[i][0] = String.valueOf(motion);
						webconsoleData[i][1] = String.valueOf(maxMotions.get(i));
						webconsoleData[i][2] = "Motion Sensor";
						webconsoleData[i][3] = "None";
						webconsoleData[i][4] = String.valueOf(eventCounts.get(i));
					}else if(motionValues.get(i).size() == 1){
						//occupantCount = 1;
						eventCounts.set(i, 1);
						if(eventCounts.get(i) >= trigger_mins.get(i))
							//occupantStatus = true;
							occupantStatuses.set(i, true);
						else
							//occupantStatus = false;
							occupantStatuses.set(i, false);
//						iOListeners.onReceivedReport("|DEVICEID="+DEVICE_TYPE+"-"+ikSerialID+"-2-0;TIMESTAMP="+System.currentTimeMillis()+ ";Raw Sensor Value=" +  motionValues.get(0) + ",None"+";Max Sensor Value="+  motionValues.get(0) + ",None"+";Occupant Status="+(occupantStatus?"1":"0")+",None"+";Event Count=0,None");
						if(occupantStatuses.get(i)){
							data.append("|DEVICEID="+getId()+"-"+i+"-0"+";TIMESTAMP="+System.currentTimeMillis()+ ";Raw Sensor Value=" +  motionValues.get(i).get(0) + ",None"+";Max Sensor Value="+  motionValues.get(i).get(0) + ",None"+";Occupant Status="+(occupantStatuses.get(i)?"1":"0")+",None"+";Event Count="+eventCounts.get(i)+",None");
						}else{
							data.append("|DEVICEID="+getId()+"-"+i+"-0"+";TIMESTAMP="+System.currentTimeMillis()+ ";Raw Sensor Value=" +  motionValues.get(i).get(0) + ",None"+";Max Sensor Value="+  motionValues.get(i).get(0) + ",None"+";Event Count="+eventCounts.get(i)+",None");
						}
						webconsoleData[i][0] = String.valueOf(motionValues.get(i).get(0));
						webconsoleData[i][1] = String.valueOf(motionValues.get(i).get(0));
						webconsoleData[i][2] = "Motion Sensor";
						webconsoleData[i][3] = "None";
						webconsoleData[i][4] = String.valueOf(1);		
					}else{
						long total = 0;
						int j;
						for(j = 0; j < motionValues.get(i).size();j++){
							total += motionValues.get(i).get(j);
						}
						int average = (int) (total/motionValues.get(i).size());
						if(eventCounts.get(i) >= trigger_mins.get(i))
							//occupantStatus = true;
							occupantStatuses.set(i, true);
						else
							//occupantStatus = false;
							occupantStatuses.set(i, false);
//						iOListeners.onReceivedReport("|DEVICEID="+DEVICE_TYPE+"-"+ikSerialID+"-2-0;TIMESTAMP="+System.currentTimeMillis()+ ";Raw Sensor Value=" +  average + ",None"+";Max Sensor Value="+  maxMotion + ",None"+";Occupant Status="+(occupantStatus?"1":"0")+",None"+";Event Count=0,None");
						if(occupantStatuses.get(i)){
							data.append("|DEVICEID="+getId()+"-"+i+"-0"+";TIMESTAMP="+System.currentTimeMillis()+ ";Raw Sensor Value=" +  average + ",None"+";Max Sensor Value="+  maxMotions.get(i) + ",None"+";Occupant Status="+(occupantStatuses.get(i)?"1":"0")+",None"+";Event Count="+eventCounts.get(i)+",None");
						}else{
							data.append("|DEVICEID="+getId()+"-"+i+"-0"+";TIMESTAMP="+System.currentTimeMillis()+ ";Raw Sensor Value=" +  average + ",None"+";Max Sensor Value="+  maxMotions.get(i) + ",None"+";Event Count="+eventCounts.get(i)+",None");
						}
						webconsoleData[i][0] = String.valueOf(average);
						webconsoleData[i][1] = String.valueOf(maxMotions.get(i));
						webconsoleData[i][2] = "Motion Sensor";
						webconsoleData[i][3] = "None";
						webconsoleData[i][4] = String.valueOf(eventCounts.get(i));
					}
					motionValues.get(i).clear();
					//maxMotion = 0;
					//minMotion = 500;
					//occupantStatus = false;
					//occupantCount = 0;
					maxMotions.set(i, 0);
					minMotions.set(i, 500);
					occupantStatuses.set(i, false);
					eventCounts.set(i, 0);
				}
			}else if(input_types.get(i).equals(SENSOR_TEMP_1124)){
				mLogger.info("temperature sensor at channel "+i);
				try {
					int rawTemp = ik.getSensorValue(i);
					data.append("|DEVICEID="+getId()+"-"+i+"-0"+";TIMESTAMP="+System.currentTimeMillis()+";Temperature=" + vformat.format((rawTemp*0.2222)-61.111) + ",C");
					webconsoleData[i][0] = String.valueOf(rawTemp);
					webconsoleData[i][1] = vformat.format((rawTemp*0.2222)-61.111);
					webconsoleData[i][2] = "Temperature Sensor";
					webconsoleData[i][3] = "C";
					webconsoleData[i][4] = String.valueOf(0);
				} catch (PhidgetException e) {
					mLogger.error("getDeviceData() Exception:", e);
				}
			}else if(input_types.get(i).equals(SENSOR_PRECISION_LUX_1127)){
				mLogger.info("lux sensor 1127 at channel "+i);
				try {
					int lux = ik.getSensorValue(i);
					data.append("|DEVICEID="+getId()+"-"+i+"-0"+";TIMESTAMP="+System.currentTimeMillis()+";Lux=" + lux + ",lux");
					webconsoleData[i][0] = String.valueOf(lux);
					webconsoleData[i][1] = String.valueOf(lux);
					webconsoleData[i][2] = "Lux Sensor";
					webconsoleData[i][3] = "lux";
					webconsoleData[i][4] = String.valueOf(0);
				} catch (PhidgetException e) {
					mLogger.error("getDeviceData() Exception:",e);
				}
			}else if(input_types.get(i).equals(DUMMY_DEVICE)){
				data.append("|DEVICEID="+getId()+"-"+i+"-0"+";TIMESTAMP="+System.currentTimeMillis()+";Status=1,none");
			}
		}
		mLogger.info("getDeviceData() end");
		return data.toString();
		
	}

	@Override
	public List<Map<String, String>> getRealTimeData(int dataIdx, boolean isRefesh) {
		if (isRefesh) {
				try {
					if(!ik.isAttached() ){ 
						mLogger.error("getData() device is not attached");
						errorCount++;
						return null;
					}
				} catch (PhidgetException e1) {
					mLogger.error("getData() has error "+e1.getMessage());
					errorCount++;
					return null;
				}

				for(int i = 0;i < 8;i++){
					if(input_types.get(i).equals(SENSOR_MOTION_1111)){
						mLogger.info("motion sensor at channel "+i);
						int currentDeltaMotion = 0;
						try {
							currentDeltaMotion = Math.abs(500 - ik.getSensorValue(i));
							if(currentDeltaMotion >= thresholds.get(i)){
								eventCounts.set(i, eventCounts.get(i) +1);
							}
							webconsoleData[i][0] = String.valueOf(currentDeltaMotion);
							webconsoleData[i][1] = String.valueOf(currentDeltaMotion);
							webconsoleData[i][2] = "Motion Sensor";
							webconsoleData[i][3] = "None";
							webconsoleData[i][4] = String.valueOf(eventCounts.get(i));
						} catch (PhidgetException e) {
							mLogger.error("PhidgetException", e);
						}
						
					}else if(input_types.get(i).equals(SENSOR_TEMP_1124)){
						mLogger.info("temperature sensor at channel "+i);
						try {
							int rawTemp = ik.getSensorValue(i);
							webconsoleData[i][0] = String.valueOf(rawTemp);
							webconsoleData[i][1] = vformat.format((rawTemp*0.2222)-61.111);
							webconsoleData[i][2] = "Temperature Sensor";
							webconsoleData[i][3] = "C";
							webconsoleData[i][4] = String.valueOf(0);
						} catch (PhidgetException e) {
							mLogger.error("PhidgetException", e);
						}
					}else if(input_types.get(i).equals(SENSOR_PRECISION_LUX_1127)){
						mLogger.info("lux sensor 1127 at channel "+i);
						try {
							int lux = ik.getSensorValue(i);
							webconsoleData[i][0] = String.valueOf(lux);
							webconsoleData[i][1] = String.valueOf(lux);
							webconsoleData[i][2] = "Lux Sensor";
							webconsoleData[i][3] = "lux";
							webconsoleData[i][4] = String.valueOf(0);
						} catch (PhidgetException e) {
							mLogger.error("PhidgetException", e);
						}
					}
				}
			
		}

		real_time_data.clear();
		if (getStatus() == IDevice.DEVICE_STATUS_ONLINE) {
			for (int j = 0; j < 8; j++) {
				Map<String, String> item = new HashMap<String, String>();
				item.put("rawdata_"+j, webconsoleData[j][0]);
				item.put("data_"+j, webconsoleData[j][1]);
				item.put("name_"+j, webconsoleData[j][2]);
				item.put("unit_"+j, webconsoleData[j][3]);
				item.put("event_"+j, webconsoleData[j][4]);
				real_time_data.add(item);
			}
		}
		return real_time_data;
	}

	
	
	public int getStatus() {
		try {
			if(!ik.isAttached() ) 
				return 3;
		} catch (PhidgetException e1) {
			return 3;
		}
		return 2;
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

}
