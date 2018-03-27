package com.greenkoncepts.gateway.taskqueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.greenkoncepts.gateway.api.task.ITask;
public class TaskQueue {
	private BlockingQueue<ITask> queue = new LinkedBlockingQueue<ITask>(20);
	
	public void addQueue(ITask T) throws InterruptedException {
		if (queue.size() > 100) {
			queue.remove();
		}
			queue.put(T);
	}
	
	public ITask deQueue() throws InterruptedException {
		return queue.take();
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
 }
