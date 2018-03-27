package com.greenkoncepts.gateway.util;

public class UtilLog {
	private static UtilLog instance;
	
	private static String className ;

	private static boolean isDebug = true ;
	private UtilLog() {
	}

	public static synchronized UtilLog getInstance(String className) {
		if (instance == null){
			instance = new UtilLog();
			UtilLog.className = className ;
		}

		return instance;
	}

	public void Log( String text) {
		if (isDebug) {
			System.out.println(className +"---"+ text);
		}
	}

}
