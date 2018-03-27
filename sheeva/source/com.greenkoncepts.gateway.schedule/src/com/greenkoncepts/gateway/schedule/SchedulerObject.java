package com.greenkoncepts.gateway.schedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greenkoncepts.gateway.api.adapter.Adapter;

public class SchedulerObject {
	public static final boolean RELAY_ON = true;
	public static final boolean RELAY_OFF = false;
	
	private int scheduleId;
	private int groupId;
	private boolean relayStatus;
	private int aoValue;
	private int minute;
	private int hour;
	private String dayOfWeek;
	private Date startDate;
	private Date endDate;
	List<Node> nodeList = new ArrayList<Node>();
	private Logger mLogger = LoggerFactory.getLogger(getClass().getSimpleName());
	
	public SchedulerObject() {
		scheduleId = -1;
		groupId = -1;
		relayStatus = false;
		aoValue = -1;
		minute = -1;
		hour = -1;
		dayOfWeek = "";
		startDate = null;
		endDate = null;
	}
	 
	public ScheduledFuture<?> addScheduler(ScheduledThreadPoolExecutor scheduledThreadPool, List<Adapter> adapterList) {
		if (hasExpired()) {
			mLogger.error("[addScheduler] expired Scheduler Object :" + toString());
			return null;
		}
		
		Calendar startTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		startTime.set(Calendar.HOUR_OF_DAY, hour);
		startTime.set(Calendar.MINUTE, minute);
		long delay = startTime.getTimeInMillis() - System.currentTimeMillis();
		SchedulerRunnable command  = new SchedulerRunnable();
		command.setSchedulerObject(this);
		command.setAdapterList(adapterList);
		
		if  (delay < 0) {
			delay = 24*60*60*1000 + delay;
		}

		
		mLogger.info("addScheduler : scheduler id = " + scheduleId + ", delay = " + delay);
		return scheduledThreadPool.scheduleAtFixedRate(command, delay, 24*60*60*1000, TimeUnit.MILLISECONDS); 
	}
	
	public boolean hasExpired() {
		if ((endDate != null) && (endDate.getTime() < System.currentTimeMillis())) {
			return true;
		}
		
		if ((startDate != null) && (endDate != null) &&
				(startDate.getTime() > endDate.getTime())) {
			return true;
		}
		return false;
	}

	public int getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(int scheduleId) {
		this.scheduleId = scheduleId;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public boolean isRelayStatus() {
		return relayStatus;
	}

	public void setRelayStatus(boolean relayStatus) {
		this.relayStatus = relayStatus;
	}

	public int getAoValue() {
		return aoValue;
	}

	public void setAoValue(int aoValue) {
		this.aoValue = aoValue;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}
	
	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public List<Node> getNodeList() {
		return nodeList;
	}

	public void setNodeList(List<Node> nodeList) {
		this.nodeList = nodeList;
	}
	
	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("Scheduler : scheduleId = " + scheduleId + ", groupId = " + groupId + ", relayStatus = " + relayStatus + ", aoValue = " 
				+ aoValue + ", minute = " + minute + ", hour = " + hour + ", dayOfWeek = " + dayOfWeek 
				+ ", startDate = " + startDate + ", endDate = " + endDate + " (");
		if ((nodeList != null) && (nodeList.size() > 0)) {
			for (int i = 0; i < nodeList.size(); i++) {
				strBuilder.append("<" + nodeList.get(i).toString() + ">");
			}
		}
		strBuilder.append(")");
		return strBuilder.toString();
		
	}
	
	
}
