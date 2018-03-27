package com.greenkoncepts.gateway.util;

import java.io.IOException;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class ProcessUtil 
{
	//private static final Logger s_logger = LoggerFactory.getLogger(ProcessUtil.class);
	
	
	public static Process exec(String command)
		throws IOException
	{
		Runtime runtime = Runtime.getRuntime();
		return runtime.exec(command);
	}

	
	public static Process exec(String command, String[] envp)
		throws IOException
	{
		Runtime runtime = Runtime.getRuntime();
		return runtime.exec(command, envp);
	}

	
	public static Process exec(String[] cmdarray)
		throws IOException
	{
		Runtime runtime = Runtime.getRuntime();
		return runtime.exec(cmdarray);
	}

	
	public static Process exec(String[] cmdarray, String[] envp)
		throws IOException
	{
		Runtime runtime = Runtime.getRuntime();
		return runtime.exec(cmdarray, envp);
	}
	
	
	public static void close(Process proc) {
		if (proc == null) {
			return;
		}
		
		try {
			proc.getInputStream().close();
		}
		catch (Exception e) {
			//s_logger.warn("Exception in proc.getInputStream().close()", e);
		}
		
		try {
			proc.getOutputStream().close();
		}
		catch (Exception e) {
			//s_logger.warn("Exception in proc.getOutputStream().close()", e);
		}

		try {
			proc.getErrorStream().close();;
		}
		catch (Exception e) {
			//s_logger.warn("Exception in proc.getErrorStream().close()", e);
		}
	}
	
	public static void destroy(Process proc) 
	{
		if (proc == null) {
			return;
		}
		
		ProcessUtil.close(proc);

		try {
			proc.destroy();
		}
		catch (Exception e) {
			//s_logger.warn("Exception in proc.destroy()", e);
		}		
	}
}
