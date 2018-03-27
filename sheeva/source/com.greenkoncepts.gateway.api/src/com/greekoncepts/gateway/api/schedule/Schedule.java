package com.greekoncepts.gateway.api.schedule;

import java.util.List;
import java.util.Map;

public interface Schedule {
	public Map<Object, Object> addSchedulerFromJson(String jsonSchedulers);
	public Map<Object, Object> addSchedulerArrayFromJson(String jsonSchedulers);
	public Map<Object, Object> updateSchedulerFromJson(String jsonSchedulers);
	public Map<Object, Object> updateSchedulerArrayFromJson(String jsonSchedulers);
	public Map<Object, Object> deleteSchedulerFromJson(String jsonSchedulerId);
	public Map<Object, Object> deleteSchedulerArrayFromJson(String jsonSchedulerIds);
	public List<Object> getAllSchedulers();
}
