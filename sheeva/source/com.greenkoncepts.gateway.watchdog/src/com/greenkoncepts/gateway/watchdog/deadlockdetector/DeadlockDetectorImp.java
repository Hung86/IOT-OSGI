package com.greenkoncepts.gateway.watchdog.deadlockdetector;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greenkoncepts.gateway.util.Util;
import com.greenkoncepts.gateway.watchdog.DeadlockDetector;

public class DeadlockDetectorImp implements DeadlockDetector{
	private int WATCHDOG_DELAY = 0;
	private int WATCHDOG_PERIOD = 40;
	private boolean hasPrintedAllStacks = false;
	private long deadlockTimeInMS = 5 * 60 * 1000; 
	private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	private Hashtable<String, TimeUpdated> detectedObjects = new Hashtable<String, TimeUpdated>();
	private Logger mLogger = LoggerFactory.getLogger("DeadlockDetectorImp");

	protected void activator(){
		scheduledExecutorService.scheduleAtFixedRate(new Runnable(){
			@Override
			public void run() {
				checkDeadlock();
			}		
		}
		, WATCHDOG_DELAY, WATCHDOG_PERIOD, TimeUnit.SECONDS);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	protected void deactivator(){
		try {
			scheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS);
			scheduledExecutorService.shutdownNow();
		} catch (InterruptedException e) {
			e.printStackTrace();
			try {
				scheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS);
				scheduledExecutorService.shutdownNow();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	synchronized public void registerDeadlockDetector(String className) {
		detectedObjects.put(className, new TimeUpdated());
	}
	
	synchronized public void unRegisterDeadlockDetector(String className) {
		detectedObjects.remove(className);
	}
	
	synchronized public void pingDeadlock(String className, int queryingTime) {
		TimeUpdated info = detectedObjects.get(className);
		if (info != null) {
			info.previousTimeUpdated = info.currentTimeUpdated;
			info.currentTimeUpdated = System.currentTimeMillis();
			mLogger.info( className + " still alive ... !");
		} else {
			if (queryingTime >= 300) {
				deadlockTimeInMS = 2 * queryingTime * 1000;
			}
			detectedObjects.put(className, new TimeUpdated());
			mLogger.info( className + " register to DeadlockDetector ... !");
		}
	}
	
	synchronized public void checkDeadlock() {
		long currentTime = System.currentTimeMillis();
		long gapTime = 0l;
		for (String key : detectedObjects.keySet()) {
			if (detectedObjects.get(key).previousTimeUpdated == detectedObjects.get(key).currentTimeUpdated) {
				gapTime = currentTime - detectedObjects.get(key).currentTimeUpdated;
				if (gapTime > deadlockTimeInMS) {
					//ERROR ! System need reboot now ... class has deadlock time is  
					mLogger.error( " ERROR ERROR ! JVM will be rebooted soon, " + key + " has deadlock time is " + gapTime + " ms");
					printAllStackTraces();
					Util.RebootJVM();
				} else if (gapTime > (deadlockTimeInMS - 120000)) {
					// WARNING ! class has deadlock time is 
					mLogger.warn( " WARNING WARNING ! " + key + " has deadlock time is " + gapTime + " ms");
				}
			} else {
				detectedObjects.get(key).previousTimeUpdated = detectedObjects.get(key).currentTimeUpdated;
			}
		}
	}
	
	private void printAllStackTraces() {
		if (!hasPrintedAllStacks) {
			StringBuffer stackTrace = new StringBuffer();
			Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
			for (Thread key : stacks.keySet()) {
				stackTrace.append("Thread "+ key.getId() + " [" + key.getName() + "," + key.getPriority()  + "]: " + key.getState().toString() + "\n");
				StackTraceElement[] element = stacks.get(key);
				for (int i = 0 ; i < element.length; i++ ) {
					stackTrace.append(element[i].toString() + "\n");
				}
				
				stackTrace.append("\n");
			}
			hasPrintedAllStacks = true;
			mLogger.warn( stackTrace.toString());
		}
	}
	
	class TimeUpdated {
		public long previousTimeUpdated;
		public long currentTimeUpdated;
		
		TimeUpdated() {
			currentTimeUpdated = System.currentTimeMillis();
			previousTimeUpdated = 0;
		}
	}

}
