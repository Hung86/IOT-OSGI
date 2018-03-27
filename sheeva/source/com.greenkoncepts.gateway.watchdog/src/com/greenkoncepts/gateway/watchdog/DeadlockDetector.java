package com.greenkoncepts.gateway.watchdog;

public interface DeadlockDetector {
	public void pingDeadlock(String className, int queryingTime);
}
