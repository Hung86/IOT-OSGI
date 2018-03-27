package com.greenkoncepts.gateway.schedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greenkoncepts.gateway.api.adapter.Adapter;

public class SchedulerRunnable implements Runnable {
	private SchedulerObject schedulerObject;
	private List<Adapter> adapterList;
	private Logger mLogger = LoggerFactory.getLogger(getClass().getName());
	private List<Integer> dayOfWeek = new ArrayList<Integer>();
	private Date startTime = null;
	private Date endTime = null;

	@Override
	public void run() {
		mLogger.info("Run Scheduler 1 id : " + schedulerObject.getScheduleId());
		List<Node> nodeList = schedulerObject.getNodeList();
		Calendar calendar = Calendar.getInstance();
		Long currTime = calendar.getTimeInMillis();
		
		//Check condition for running scheduler
		if ((startTime != null) && (currTime < startTime.getTime())) {
			return;
		}
		
		if ((endTime != null) && (currTime > endTime.getTime())) {
			return;
		}
		boolean isOK = false;
		for (Integer i : dayOfWeek) {
			if (calendar.get(Calendar.DAY_OF_WEEK) == i) {
				isOK = true;
				break;
			}
		}
		
		if (!isOK) {
			return;
		}
		
 		mLogger.info("Run Scheduler 2 id : " + schedulerObject.getScheduleId());
		for (Node node : nodeList) {
			Map<Integer, String> data = new HashMap<Integer, String>();
			data.put(Integer.valueOf(node.getChannelId()), schedulerObject.isRelayStatus() ? "1" : "0");
			for (Adapter adapter : adapterList) {
				if(adapter.setNodeValue(node.getCategory(), node.getDeviceId(), data)) {
					break;
				}
			}
		}
	}

	public SchedulerObject getSchedulerObject() {
		return schedulerObject;
	}

	public void setSchedulerObject(SchedulerObject schedulerObject) {
		this.schedulerObject = schedulerObject;
		
		String[] strSplit = schedulerObject.getDayOfWeek().trim().split(";");
		if (strSplit.length > 0) {
			int day = 0;
			for (int i = 0; i < strSplit.length ; i++) {
				switch(Integer.valueOf(strSplit[i])) {
					case 0:
						day = Calendar.MONDAY;
						break;
					case 1:
						day = Calendar.TUESDAY;
						break;
					case 2:
						day = Calendar.WEDNESDAY;
						break;
					case 3:
						day = Calendar.THURSDAY;
						break;
					case 4:
						day = Calendar.FRIDAY;
						break;
					case 5:
						day = Calendar.SATURDAY;
						break;
					case 6:
						day = Calendar.SUNDAY;	
				}
				dayOfWeek.add(day);
			}
		}
		
		startTime = schedulerObject.getStartDate();
		endTime = schedulerObject.getEndDate();
		
	}

	public List<Adapter> getAdapterList() {
		return adapterList;
	}

	public void setAdapterList(List<Adapter> adapterList) {
		this.adapterList = adapterList;
	}

	
	
	

}
