package com.greenkoncepts.gateway.taskqueue;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greenkoncepts.gateway.api.task.ITask;
import com.greenkoncepts.gateway.api.task.ITaskExecute;

public class TaskExecuteActivator implements ITaskExecute {
	static Logger mLogger = LoggerFactory.getLogger("TaskExecuteActivator");
	
	private ScheduledExecutorService processServerThread = Executors.newSingleThreadScheduledExecutor();
	private TaskQueue taskQueue = new TaskQueue();
	private boolean isRunning = false ;
	
	protected void activator(){
		isRunning = true;
		processServerThread.execute(new Runnable() {
			@Override
			public void run() {
				process();
			}
		});
		mLogger.info("TaskExecuteActivator service ... started !");
	}

	protected void deactivator(){
		isRunning = false;
		processServerThread.shutdown();
		mLogger.info("TaskExecuteActivator service ... stop !");
	}
	
	public void process() {
		while (isRunning) {
			try {
				ITask task = taskQueue.deQueue();
				task.run();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void addTask(ITask T) {
		try {
			taskQueue.addQueue(T);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
