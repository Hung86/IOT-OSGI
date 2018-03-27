package com.greenkoncepts.gateway.schedule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greekoncepts.gateway.api.schedule.Schedule;
import com.greenkoncepts.gateway.api.adapter.Adapter;
import com.greenkoncepts.gateway.api.database.DbService;

public class ScheduledExecutionThread implements Schedule {
	public static ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(1);
	public static Map<Integer, ScheduledFuture<?>> scheduledFutureMap = new Hashtable<Integer, ScheduledFuture<?>>();
	public static Map<Integer, SchedulerObject> runningSchedulerObjects = new Hashtable<Integer, SchedulerObject>();
	
	protected Logger mLogger = LoggerFactory.getLogger(getClass().getSimpleName());
	private ExecutiveDatabaseImp dbExecute ;
	private DbService dbService  = null;
	private List<Adapter> adapterList = new ArrayList<Adapter>();
	protected void activator() {
		if (dbService != null) {
			 mLogger.error("activator : database instance is READY now ");
			 dbExecute = new ExecutiveDatabaseImp(dbService);
			 dbExecute.createSchedulerObjectTable();
			 dbExecute.createNodeObjectTable();
			 scheduledThreadPool.setRemoveOnCancelPolicy(true);
			 startSchedueler();
		} else {
			mLogger.error("activator : database instance is NULL now ");
		}
	}

	protected void deactivator() {
		mLogger.info("deactivator : scheduledThreadPool is shutdown");
		scheduledThreadPool.shutdown();
	}
	
	public void startSchedueler() {
		List<SchedulerObject> schedulerList = dbExecute.getAllSchedulerObjects();
		for (SchedulerObject scheduler : schedulerList) {
			mLogger.info(scheduler.toString());
			ScheduledFuture<?> scheduledFuture = scheduler.addScheduler(scheduledThreadPool, adapterList);
			if (scheduledFuture != null) {
				scheduledFutureMap.put(scheduler.getScheduleId(), scheduledFuture);
				runningSchedulerObjects.put(scheduler.getScheduleId(), scheduler);
			}
		}
		scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				checkingSchedulerStatus();
			}
		}, 60*1000, 10*60*1000, TimeUnit.MILLISECONDS);
		//scheduledThreadPool.scheduleAtFixedRate(() -> checkingSchedulerStatus(), 60*1000, 10*60*1000, TimeUnit.MILLISECONDS);
	}

	public boolean addScheduler (SchedulerObject scheduler) {
		if (scheduler != null) {
			if (dbExecute.insertSchedulerObject(scheduler)) {
				ScheduledFuture<?> scheduledFuture = scheduler.addScheduler(scheduledThreadPool, adapterList);
				if (scheduledFuture != null) {
					scheduledFutureMap.put(scheduler.getScheduleId(), scheduledFuture);
					runningSchedulerObjects.put(scheduler.getScheduleId(), scheduler);
				}
				return true;
			} else {
				mLogger.info("addScheduler : can not insert scheduler id " + scheduler.getScheduleId());
			}
		}
		return false;
	}
	
	public void addSchedulers(List<SchedulerObject> schedulers) {
		for (SchedulerObject scheduler : schedulers) {
			addScheduler(scheduler);
		}
	}
	
	public boolean updateScheduler (SchedulerObject scheduler) {
		if (scheduler == null) {
			return false;
		}
		int scheduledId = scheduler.getScheduleId();
		ScheduledFuture<?> currScheduler = scheduledFutureMap.get(scheduledId);
		if (currScheduler != null) {
			if(dbExecute.updateSchedulerObject(scheduler)) {
				currScheduler.cancel(true);
				ScheduledFuture<?> scheduledFuture = scheduler.addScheduler(scheduledThreadPool, adapterList);
				if (scheduledFuture != null) {
					scheduledFutureMap.put(scheduler.getScheduleId(), scheduledFuture);
					runningSchedulerObjects.put(scheduler.getScheduleId(), scheduler);
				}
				return true;
			}
		} else {
			mLogger.info("updateScheduler : can not update scheduler id " + scheduler.getScheduleId());
		}
		return false;
	}
	
	public void updateSchedulers (List<SchedulerObject> schedulers) {
		if ((schedulers == null ) ||  schedulers.isEmpty()) {
			return ;
		}
		int groupId = schedulers.get(0).getGroupId();
		List<Integer> deletedSchedulers = new ArrayList<Integer>();
		for (Integer schId : runningSchedulerObjects.keySet()){
			SchedulerObject value = runningSchedulerObjects.get(schId);
			if (value.getGroupId() == groupId) {
				deletedSchedulers.add(schId);
			}
		}
		deleteSchedulers(deletedSchedulers);
		
		for (SchedulerObject scheduler : schedulers) {
			addScheduler(scheduler);
		}
	}
	
	public boolean deleteScheduler(int schedulerId) {
		if (schedulerId >= 0) {
			if (dbExecute.deleteSchedulerObject(schedulerId)) {
    			ScheduledFuture<?> currScheduler = scheduledFutureMap.get(schedulerId);
    			if (currScheduler != null) {
    				currScheduler.cancel(true);
    				runningSchedulerObjects.remove(schedulerId);
    				return true;
    			}
			}
		}
		return false;
	}
	
	public void deleteSchedulers (List<Integer> schedulerIds) {
		if (schedulerIds != null) {
			for(int i = 0; i < schedulerIds.size(); i++) {
				deleteScheduler(schedulerIds.get(i));
			}
		}
	}
	
	public boolean deleteAllSchedulers () {
		return true;
	}
	
	public void setDbService(DbService db) {
		dbService = db;
	}

	public void clearDbService(DbService db) {
		dbService = null;
	}
	
	
	// Method will be used by DS to set the Adapter service
	public void setAdapter(Adapter service) {
		synchronized(adapterList)
		{
			adapterList.add(service);
		}
	}

	// Method will be used by DS to unset the Adapter service
	public void unsetAdapter(Adapter service) {
		synchronized(adapterList)
		{
			if(adapterList.contains(service))
			{
				adapterList.remove(service);
			}
		}
	}

	@Override
	public Map<Object, Object> addSchedulerFromJson(String jsonSchedulers) {
		Map<Object, Object> result = new Hashtable<Object, Object>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			SchedulerObject scheduler = mapper.readValue(jsonSchedulers, SchedulerObject.class);
			if (addScheduler(scheduler)) {
				
			}
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public Map<Object, Object> addSchedulerArrayFromJson(String jsonSchedulers) {
		mLogger.info("[addSchedulerArrayFromJson] jsonSchedulers = " + jsonSchedulers);
		Map<Object, Object> result = new Hashtable<Object, Object>();
		ObjectMapper mapper = new ObjectMapper();
		result.put("success", "False");
		try {
			List<SchedulerObject> schedulerList = mapper.readValue(jsonSchedulers, new TypeReference<List<SchedulerObject>>() {});
			addSchedulers(schedulerList);
			result.put("success", "True");
		} catch (JsonGenerationException e) {
			mLogger.error("JsonGenerationException",e);
			result.put("error", "JsonGenerationException");
		} catch (JsonMappingException e) {
			mLogger.error("JsonMappingException",e);
			result.put("error", "JsonMappingException");
		} catch (IOException e) {
			mLogger.error("IOException",e);
			result.put("error", "IOException");
		} catch (Exception e) {
			mLogger.error("Exception",e);
			result.put("error", "Exception");
		}
		return result;
	}

	@Override
	public Map<Object, Object> updateSchedulerFromJson(String jsonSchedulers) {
		Map<Object, Object> result = new Hashtable<Object, Object>();
		ObjectMapper mapper = new ObjectMapper();
		result.put("success", "False");
		try {
			SchedulerObject scheduler = mapper.readValue(jsonSchedulers, SchedulerObject.class);
			if (updateScheduler(scheduler)) {
				result.put("success", "True");
			}
		} catch (JsonGenerationException e) {
			mLogger.error("JsonGenerationException",e);
			result.put("error", "JsonGenerationException");
		} catch (JsonMappingException e) {
			mLogger.error("JsonMappingException",e);
			result.put("error", "JsonMappingException");
		} catch (IOException e) {
			mLogger.error("IOException",e);
			result.put("error", "IOException");
		} catch (Exception e) {
			mLogger.error("Exception",e);
			result.put("error", "Exception");
		}
		return result;
	}

	@Override
	public Map<Object, Object> updateSchedulerArrayFromJson(String jsonSchedulers) {
		mLogger.info("[updateSchedulerArrayFromJson] jsonSchedulers = " + jsonSchedulers);
		Map<Object, Object> result = new Hashtable<Object, Object>();
		ObjectMapper mapper = new ObjectMapper();
		result.put("success", "False");
		try {
			List<SchedulerObject> schedulerList = mapper.readValue(jsonSchedulers, new TypeReference<List<SchedulerObject>>() {});
			updateSchedulers(schedulerList);
			result.put("success", "True");
		} catch (JsonGenerationException e) {
			mLogger.error("JsonGenerationException",e);
			result.put("error", "JsonGenerationException");
		} catch (JsonMappingException e) {
			mLogger.error("JsonMappingException",e);
			result.put("error", "JsonMappingException");
		} catch (IOException e) {
			mLogger.error("IOException",e);
			result.put("error", "IOException");
		} catch (Exception e) {
			mLogger.error("Exception",e);
			result.put("error", "Exception");
		}
		return result;
	}

	@Override
	public Map<Object, Object> deleteSchedulerFromJson(String jsonSchedulerId) {
		Map<Object, Object> result = new Hashtable<Object, Object>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			Integer schedulerId = mapper.readValue(jsonSchedulerId, Integer.class);
			deleteScheduler(schedulerId);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public Map<Object, Object> deleteSchedulerArrayFromJson(String jsonSchedulerIds) {
		mLogger.info("[deleteSchedulerArrayFromJson] jsonSchedulerIds = " + jsonSchedulerIds);
		Map<Object, Object> result = new Hashtable<Object, Object>();
		ObjectMapper mapper = new ObjectMapper();
		result.put("success", "False");
		try {
			List<Integer> schedulerIdList = mapper.readValue(jsonSchedulerIds, new TypeReference<List<Integer>>() {});
			deleteSchedulers(schedulerIdList);
			result.put("success", "True");
		} catch (JsonGenerationException e) {
			mLogger.error("JsonGenerationException",e);
			result.put("error", "JsonGenerationException");
		} catch (JsonMappingException e) {
			mLogger.error("JsonMappingException",e);
			result.put("error", "JsonMappingException");
		} catch (IOException e) {
			mLogger.error("IOException",e);
			result.put("error", "IOException");
		} catch (Exception e) {
			mLogger.error("Exception",e);
			result.put("error", "Exception");
		}
		return result;
	}

	@Override
	public List<Object> getAllSchedulers() {
		List<Object> results = new ArrayList<Object>();
		if(dbExecute != null) {
			results.addAll(dbExecute.getAllSchedulerObjects());
		}
		return results;
	}
	
	public void checkingSchedulerStatus() {
		mLogger.info("[checkingSchedulerStatus] Active Threads = " + scheduledThreadPool.getActiveCount() +" and Tasks in Queue = " + scheduledThreadPool.getQueue().size());
		for (Integer soid : runningSchedulerObjects.keySet()) {
			SchedulerObject SO = runningSchedulerObjects.get(soid);
			mLogger.info("[checkingSchedulerStatus] Active Scheduler : " + SO.toString());
			if (SO.hasExpired()) {
				runningSchedulerObjects.remove(soid);
				ScheduledFuture<?> currScheduler = scheduledFutureMap.get(soid);
				currScheduler.cancel(true);
			}
			
		}
	}

}
