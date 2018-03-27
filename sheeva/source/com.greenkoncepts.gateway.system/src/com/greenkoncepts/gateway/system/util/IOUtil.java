/**
 * 
 */
package com.greenkoncepts.gateway.system.util;

/**
 * @author Phu
 *
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IOUtil 
{
	private static final Logger s_logger = LoggerFactory.getLogger(IOUtil.class);

	/**
	 * Reads a resource fully and returns it as a string.
	 * 
	 * @param ctx
	 * @param resourceName
	 * @return
	 * @throws IOException
	 */
	public static String readResource(String resourceName) 
		throws IOException
	{
		URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(resourceName);
		if (resourceUrl == null) {
			return null;
		}
		return readResource(resourceUrl);
	}

	/**
	 * Reads a resource fully and returns it as a string.
	 * 
	 * @param ctx
	 * @param resourceName
	 * @return
	 * @throws IOException
	 */
	public static String readResource(BundleContext ctx, String resourceName) 
		throws IOException
	{
		URL resourceUrl = ctx.getBundle().getResource(resourceName);
		if (resourceUrl == null) {
			return null;
		}
		
		String resource = readResource(resourceUrl);		
		return resource;
	}
	
	/**
	 * Reads a resource fully and returns it as a string.
	 * 
	 * @param ctx
	 * @param resourceName
	 * @return
	 * @throws IOException
	 */
	public static String readResource(Bundle bundle, String resourceName) 
		throws IOException
	{
		URL resourceUrl = bundle.getResource(resourceName);
		if (resourceUrl == null) {
			return null;
		}
		
		String resource = readResource(resourceUrl);		
		return resource;
	}

	/**
	 * Reads a resource fully and returns it as a string.
	 * 
	 * @param ctx
	 * @param resourceName
	 * @return
	 * @throws IOException
	 */
	public static String readResource(URL resourceUrl) 
		throws IOException
	{
		String result = null;
		BufferedReader resourceBr = null;
		InputStreamReader resourceIsr = null;
		try {
		
			resourceIsr = new InputStreamReader(resourceUrl.openStream());
			resourceBr  = new BufferedReader(resourceIsr);
			
			int iRead = 0;
			char[] buffer = new char[1024];
			StringWriter sw = new StringWriter();
			while ((iRead = resourceBr.read(buffer)) != -1) {
				sw.write(buffer, 0, iRead);
			}
			result = sw.toString();
		}
		finally {
			if (resourceBr != null) {
				try { resourceBr.close(); }
				catch (IOException e)  {
					s_logger.warn("Error closing reader", e);
				}
			}
			if (resourceIsr != null) {
				try { resourceIsr.close(); }
				catch (IOException e)  {
					s_logger.warn("Error closing reader", e);
				}
			}
		}
		return result;		
	}
}