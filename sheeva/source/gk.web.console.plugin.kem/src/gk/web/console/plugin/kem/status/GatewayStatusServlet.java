package gk.web.console.plugin.kem.status;


import gk.web.console.plugin.kem.KemObjectsServiceTracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.webconsole.SimpleWebConsolePlugin;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.osgi.framework.BundleContext;

import com.greenkoncepts.gateway.util.Util;

public class GatewayStatusServlet extends SimpleWebConsolePlugin {

	private static final long serialVersionUID = 1L;
	private static final String CATEGORY = "KEM";//Change Category
    private static final String LABEL = "gatewaystatus"; //$NON-NLS-1$
    private static final String TITLE = "Gateway Status"; //$NON-NLS-1$
    private static final String CSS[] = {  }; // yes, it's correct! //$NON-NLS-1$
   // private static final String RES = "/" + LABEL + "/res/"; //$NON-NLS-1$ //$NON-NLS-2$
    private final static String NAND_STORAGE = "buffer/";
    private final static String SD_STORAGE 	= "buffer_SD/";
        
  //  private static final long startDate = System.currentTimeMillis();

    // from BaseWebConsolePlugin
    private DecimalFormat decimal = new DecimalFormat("#####0.00");


    // templates
    private final String TPL_VM_MAIN = "/template/gatewaystatus/gateway_status.html";
   // private final String TPL_VM_STOP;
    private final String TPL_VM_RESTART = "/template/gatewaystatus/status_restart.html" ;
    private final String TPL_OS_REBOOT = "/template/gatewaystatus/status_reboot_os.html";

  //  private final DecimalFormat vformat = new DecimalFormat("##0.0000");
    
	private KemObjectsServiceTracker ASTHandler;
    
    private final String OS;
    
    //private final boolean debug = true;
    
	public GatewayStatusServlet(KemObjectsServiceTracker ASTHandler_) {
		super(LABEL, TITLE, CATEGORY, CSS);
		ASTHandler = ASTHandler_;
		// load templates
        String os = System.getProperty("os.name").toLowerCase();
        
 		if ((os.indexOf("win") >= 0)) {
 			//log("This is Windows");
 			OS = "win";
 		} else if ((os.indexOf("mac") >= 0)) {
 			//log("This is Mac");
 			OS = "mac";
 		} else if ((os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0 )) {
 			//log("This is Unix or Linux");
 			OS = "linux";
 		} else if ((os.indexOf("sunos") >= 0)) {
 			//log("This is Solaris");
 			OS = "sola";
 		} else {
 			//log("Your OS is not support!!");
 			OS = "";
 		}
	}
    
	public void activate( BundleContext bundleContext )
	{
		super.activate( bundleContext );
		log("GatewayStatusServlet starts on " + OS);
	}
	
	public void deactivate()
	{
		super.deactivate();
		log("GatewayStatusServlet stop");
	}
	
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
    IOException
{
    String action = request.getParameter( "action"); //$NON-NLS-1$
    if ( "grabage_collector".equals( action ) ) //$NON-NLS-1$
    {
        System.gc();
    }
    else if ( "reboot".equals( action )  )
    {

//    	final String shutdown_type = request.getParameter( PARAM_SHUTDOWN_TYPE );
        // whether to stop or restart the framework
        final String rebootType = request.getParameter( "reboot_type" ) ;
        log("reboot type"+request.getParameter( "reboot_type" ));

        // simply terminate VM in case of shutdown :-)
        //final Bundle systemBundle = getBundleContext().getBundle( 0 );
        Thread t = new Thread( "Stopper" )
        {
            public void run()
            {
                try
                {
                    Thread.sleep( 2000L );
                }
                catch ( InterruptedException ie )
                {
                    // ignore
                }

                log( "Shutting down server now! : reboot " + rebootType);

                if ( "jvm".equalsIgnoreCase(rebootType) )
				{
                	if(OS.equals("linux")){
                		Util.RebootJVM();
                	}
				} else if ("gateway".equalsIgnoreCase(rebootType)) {
					//Reboot gateway
					if(OS.equals("linux")){
						//log(Logger.TYPE_INFO,"Reboot JVM");
						try{
							Runtime r = Runtime.getRuntime();
							Process p = r.exec("sudo reboot");
							p.waitFor();
							BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
							String line = "";
					
							while ((line = b.readLine()) != null) {
								//log(Logger.TYPE_INFO, line);
							  log(line);
							}
							DateFormat dateformat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SSS");
							String d = dateformat.format(new Date());
							System.out.print("Reboot JVM at:"+d+",after ");
						}catch(IOException e){
							System.out.println("Fail to reboot JVM");
						}catch(InterruptedException e){
							System.out.println("Fail to reboot JVM");
						}catch(Exception e){
							System.out.println("Fail to reboot JVM");
						}
					}
				}
            }
        };
        t.start();
        request.setAttribute( "rebooting", rebootType );
    }

    // render the response without redirecting
    super.doGet(request, response);
	}
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		String id = request.getParameter("id");
		if (id != null) {
			if (id.equals("gateway_status")) {
				final PrintWriter out = response.getWriter();
				Map<String, Long> bridgeStatus = ASTHandler.getBridgeStatus();
				Map<String, String> gwStatus = new Hashtable<String, String>();
				gwStatus.put("internet_error", bridgeStatus.get("bridgeErrorPer24Hours") + " Error(s) in 24 hours");
				gwStatus.put("serial_error", ASTHandler.getSerialError() + " Error(s) in 24 hours");
				gwStatus.put("read_data_in_last_minute",String.valueOf(bridgeStatus.get("readDataInLastMinute")) + " bytes");
				gwStatus.put("total_reading_data",String.valueOf(bridgeStatus.get("totalReadingData")) + " bytes");
				gwStatus.put("sent_data_in_last_minute",String.valueOf(bridgeStatus.get("sentDataInLastMinute")) + " bytes");
				gwStatus.put("total_sent_data",String.valueOf(bridgeStatus.get("totalSentData"))+ " bytes");
				
				if (checkInternetConnectivity()) {
					gwStatus.put("internet_status", "Good");
				} else {
					gwStatus.put("internet_status", "Bad");
				}
				Map<String, String> infoHW = Util.getInfoHardware("all");
				// Physical Hardware Status
				double freeMem = 0;
				double totalMem = 0;
				double usedMem = 0;
				double memUsage = 0;
				double cpuUsage = 0.0f;

				if (infoHW != null) {
					totalMem = Util.getDoubleValueOf(infoHW.get("total_mem")) / 1000;
					usedMem = Util.getDoubleValueOf(infoHW.get("used_mem")) / 1000;
					memUsage = (usedMem / totalMem) * 100;
					cpuUsage = Util.getDoubleValueOf(infoHW.get("usr_cpu")) + Util.getDoubleValueOf(infoHW.get("sys_cpu"));
				}
				gwStatus.put("cpu_usage", decimal.format(cpuUsage));
				gwStatus.put("mem_usage", decimal.format(memUsage));
				gwStatus.put("used_mem", decimal.format(usedMem));
				gwStatus.put("total_mem", decimal.format(totalMem));

				// JVM Memory Status
				freeMem = Runtime.getRuntime().freeMemory() / 1000000;
				totalMem = Runtime.getRuntime().totalMemory() / 1000000;
				usedMem = totalMem - freeMem;
				memUsage = (usedMem / totalMem) * 100;

				gwStatus.put("jvm_mem_usage", decimal.format(memUsage));
				gwStatus.put("used_jvm_mem", decimal.format(usedMem));
				gwStatus.put("total_jvm_mem", decimal.format(totalMem));

				double total = 0.0f;
				double used = 0.0f;
				double percentage = 0.0f;
				
				// NAND FLASH
				File classpathRoot_NAND = new File(NAND_STORAGE);
				if (classpathRoot_NAND.exists()) {
            		used = (folderSize(classpathRoot_NAND)/1024.0)/1024.0;
					total =  used + ASTHandler.getUsableStorage(NAND_STORAGE);
					if (used == 0) {
						percentage = 0;
					} else {
						percentage = (used / total) * 100;
					}
				}


				log("total internal store: " + total + " - used internal store: " + used);
				gwStatus.put("internal_buffer_usage", decimal.format(percentage));
				gwStatus.put("in_cap_used", decimal.format(used));
				gwStatus.put("in_cap_total", decimal.format(total));

				// SD Card
				total = 0;
				used = 0;
				percentage = 0;
				File classpathRoot_SD = new File(SD_STORAGE);
				if (classpathRoot_NAND.exists()) {
            		used = (folderSize(classpathRoot_SD)/1024.0)/1024.0;
					total =  used + ASTHandler.getUsableStorage(SD_STORAGE);
					if (used == 0) {
						percentage = 0;
					} else {
						percentage = (used / total) * 100;
					}
				}


				log("total external store: " + total + " - used external store: " + used);
				gwStatus.put("external_buffer_usage", decimal.format(percentage));
				gwStatus.put("ex_cap_used", decimal.format(used));
				gwStatus.put("ex_cap_total", decimal.format(total));

				RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

				DateFormat formatDate = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, request.getLocale());
				gwStatus.put("gateway_time_label", formatDate.format(new Date(System.currentTimeMillis())));
				gwStatus.put("jvm_last_started", formatDate.format(new Date(runtimeBean.getStartTime())));
				gwStatus.put("jvm_up_time", formatPeriod(runtimeBean.getUptime()));
				
				gwStatus.put("start_time_reading",formatDate.format(bridgeStatus.get("startTimeReading")));
				gwStatus.put("start_time_sending",formatDate.format(bridgeStatus.get("startTimeSending")));

				JSONObject gwJson = new JSONObject(gwStatus);
				out.print(gwJson.toString());

			} else if (id.equals("internet_status"))
			{
				final PrintWriter out = response.getWriter();
				log("Do Get:internet status");
				response.setContentType("application/json"); //$NON-NLS-1$
				response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
				final JSONWriter jw = new JSONWriter(out);
				try
				{
					jw.object();
					jw.key("internet_status"); //$NON-NLS-1$
					if (checkInternetConnectivity()) {
						jw.value("Good");
					} else {
						jw.value("Bad");
					}

					jw.endObject();
				} catch (JSONException je)
				{
					throw new IOException(je.toString());
				}
			} else if (id.equals("internet_error"))
			{
				final PrintWriter out = response.getWriter();
				log("Do Get:internet error");
				response.setContentType("application/json"); //$NON-NLS-1$
				response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
				final JSONWriter jw = new JSONWriter(out);
				try
				{
					jw.object();
//					jw.key("internet_error"); //$NON-NLS-1$
//					jw.value(ASTHandler.getInternetError());
					jw.endObject();
				} catch (JSONException je)
				{
					throw new IOException(je.toString());
				}
			} else if (id.equals("serial_error"))
			{
				final PrintWriter out = response.getWriter();
				log("Do Get:serial error");
				response.setContentType("application/json"); //$NON-NLS-1$
				response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
				final JSONWriter jw = new JSONWriter(out);
				try
				{
					jw.object();
					jw.key("serial_error"); //$NON-NLS-1$
					jw.value(ASTHandler.getSerialError());
					jw.endObject();
				} catch (JSONException je)
				{
					throw new IOException(je.toString());
				}
			} else if (id.equals("gateway_time"))
			{
				final PrintWriter out = response.getWriter();
				log("Do Get:gateway time");
				response.setContentType("application/json"); //$NON-NLS-1$
				response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
				final JSONWriter jw = new JSONWriter(out);
				try
				{
					jw.object();
					jw.key("time"); //$NON-NLS-1$
					DateFormat format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, request.getLocale());
					jw.value(format.format(System.currentTimeMillis()));
					jw.endObject();
				} catch (JSONException je)
				{
					throw new IOException(je.toString());
				}
			} else if (id.equals("sync_clock"))
			{
				log("Do Get:synchronize gateway time");
				if (OS.equals("linux")) {
					if(!Util.syncClock()) {
						log("Can not synchronize time");
					}
				} else {
					log("Synchronize only run on Linux OS");
				}
				final PrintWriter out = response.getWriter();
				response.setContentType("application/json"); //$NON-NLS-1$
				response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
				final JSONWriter jw = new JSONWriter(out);
				try
				{
					jw.object();
					jw.key("time"); //$NON-NLS-1$
					DateFormat format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, request.getLocale());
					jw.value(format.format(System.currentTimeMillis()));
					jw.endObject();
				} catch (JSONException je)
				{
					throw new IOException(je.toString());
				}
			} else if (id.equals("cpu_usage"))
			{
				final PrintWriter out = response.getWriter();
				log("Do Get:cpu usage");
				response.setContentType("application/json"); //$NON-NLS-1$
				response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
				Hashtable<String, String> infoHW = Util.getInfoHardware("cpu");
				final JSONWriter jw = new JSONWriter(out);
				try
				{
					jw.object();
					double value = 0.0f;
					if (infoHW != null) {
						value = Util.getDoubleValueOf(infoHW.get("usr_cpu")) + Util.getDoubleValueOf(infoHW.get("sys_cpu"));
					}
					jw.key("cpu_usage");
					jw.value(decimal.format(value));
					jw.endObject();
				} catch (JSONException je)
				{
					throw new IOException(je.toString());
				}
			} else if (id.equals("mem_usage"))
			{
				final PrintWriter out = response.getWriter();
				log("Do Get:memory usage");
				response.setContentType("application/json"); //$NON-NLS-1$
				response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
				Hashtable<String, String> infoHW = Util.getInfoHardware("mem");
				final JSONWriter jw = new JSONWriter(out);
				try
				{
					double usedMem = Util.getDoubleValueOf(infoHW.get("used_mem")) / 1000;
					double totalMem = Util.getDoubleValueOf(infoHW.get("total_mem")) / 1000;
					double memUsage = 0;
					if (infoHW != null) {
						memUsage = (usedMem / totalMem) * 100;
					}
					jw.object();
					jw.key("used_mem");
					jw.value(decimal.format(usedMem));
					jw.key("mem_usage");
					jw.value(decimal.format(memUsage));
					jw.endObject();
				} catch (JSONException je)
				{
					throw new IOException(je.toString());
				}
			} else if (id.equals("clear_internal_store"))
			{
				final PrintWriter out = response.getWriter();
				log("Do Get:clear internal store");
				clearBuffer("internal");
				response.setContentType("application/json"); //$NON-NLS-1$
				response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
				ASTHandler.clearBuffer("internal");
				log("Do Get:clear internal store 1");
				final JSONWriter jw = new JSONWriter(out);
				try
				{
					jw.object();
					double total = 0.0f;
					double used = 0.0f;
					double percentage = 0.0f;
					// NAND FLASH
					File classpathRoot_NAND = new File("buffer");
					if (classpathRoot_NAND.exists()) {
						total = 150 * 500000 * 0.000001;
						used = folderSize(classpathRoot_NAND) * 0.000001;
						percentage = (used / total) * 100;
					}
					jw.key("internal_buffer_usage");
					jw.value(decimal.format(percentage));

					jw.key("in_cap_used");
					jw.value(decimal.format(total));

					jw.key("in_cap_total");
					jw.value(decimal.format(total));

					log("Do Get:clear internal store 2 : percentage = " + percentage + " - total = " + total);
					// SD Card
					total = 0;
					used = 0;
					percentage = 0;
					File classpathRoot_SD = new File("buffer_SD");
					if (classpathRoot_SD.exists()) {
						if (classpathRoot_SD.getTotalSpace() != classpathRoot_NAND.getTotalSpace()) {
							if (Util.checkExternalStoreAlive("buffer_SD")) {
								total = classpathRoot_SD.getTotalSpace() * 0.000001;
								used = folderSize(classpathRoot_SD) * 0.000001;
								percentage = (used / total) * 100;
							} else {
								log("Input/Output Error happened on external store");
							}
						}
					}

					jw.key("external_buffer_usage");
					jw.value(decimal.format(percentage));

					jw.key("ex_cap_used");
					jw.value(decimal.format(used));

					jw.key("ex_cap_total");
					jw.value(decimal.format(total));
					log("Do Get:clear internal store EXIT");
					jw.endObject();
				} catch (JSONException je)
				{
					log("Do Get:clear internal store end : EXCEPTION	");
					throw new IOException(je.toString());
				}
			} else if (id.equals("clear_external_store"))
			{
				final PrintWriter out = response.getWriter();
				log("Do Get:clear external store");
				clearBuffer("internal");
				response.setContentType("application/json"); //$NON-NLS-1$
				response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
				ASTHandler.clearBuffer("external");
				final JSONWriter jw = new JSONWriter(out);
				try
				{
					jw.object();
					double total = 0.0f;
					double used = 0.0f;
					double percentage = 0.0f;
					// NAND FLASH
					File classpathRoot_NAND = new File("buffer");
					// SD Card
					total = 0;
					used = 0;
					percentage = 0;
					File classpathRoot_SD = new File("buffer_SD");
					if (classpathRoot_SD.exists()) {
						if (classpathRoot_SD.getTotalSpace() != classpathRoot_NAND.getTotalSpace()) {
							if (Util.checkExternalStoreAlive("buffer_SD")) {
								total = classpathRoot_SD.getTotalSpace() * 0.000001;
								used = folderSize(classpathRoot_SD) * 0.000001;
								percentage = (used / total) * 100;
							} else {
								log("Input/Output Error happened on external store");
							}
						}
					}

					jw.key("external_buffer_usage");
					jw.value(decimal.format(percentage));

					jw.key("ex_cap_used");
					jw.value(decimal.format(used));

					jw.key("ex_cap_total");
					jw.value(decimal.format(total));
					jw.endObject();
				} catch (JSONException je)
				{
					throw new IOException(je.toString());
				}
			}
			return;
		} else if (request.getParameter("action") != null ){
			if("clear_data_read".equals(request.getParameter("action"))){
				
				ASTHandler.updateGatewayStatus(0L, null);
			}
			if("clear_data_sent".equals(request.getParameter("action"))){
				ASTHandler.updateGatewayStatus(null, 0L);
			}
			return ;
		}
		super.doGet(request, response);
	}
    
	@Override
    protected void renderContent( HttpServletRequest request, HttpServletResponse response ) throws IOException
    {
        String body = null;
        log("--------------------------------------- log 1");
        if ( request.getAttribute( "rebooting" ) != null )
        {
            String reboot_action = request.getAttribute( "rebooting" ).toString();
            if ( reboot_action.equalsIgnoreCase("jvm"))
            {
                body = readTemplateFile(TPL_VM_RESTART);
            }
            else if (reboot_action.equalsIgnoreCase("gateway"))
            {
                //body = TPL_VM_STOP;
            	body = readTemplateFile(TPL_OS_REBOOT);
            }
            response.getWriter().print( body );
            request.removeAttribute("rebooting");
            return;
        }
        body = readTemplateFile(TPL_VM_MAIN);
//        log("--------------------------------------- log 1");
//        DefaultVariableResolver vars = ( ( DefaultVariableResolver ) WebConsoleUtil.getVariableResolver( request ) );
//
//        log("--------------------------------------- log 1");
//
//        boolean isRestaring = request.getParameter( "restarting" ) != null;
//        String retartType = request.getParameter( "restart_type" );
//        if ( retartType == null )
//        	retartType = "";
//        
//        vars.put( "restarting", (isRestaring ? "1" : "0"));
//        vars.put( "restart_type", retartType);
//        
//        vars.put( "internet_error", ASTHandler.getInternetError() + " Error(s) in 24 hours");
//        vars.put( "serial_error", ASTHandler.getSerialError() + " Error(s) in 24 hours");
//        
//        if(checkInternetConnectivity()){
//        	vars.put( "internet_status", "Good");
//        }else{
//        	vars.put( "internet_status", "Bad");
//        }
//        log("--------------------------------------- log 1");
//        Hashtable<String, String> infoHW = Util.getInfoHardware("all");
//        // Physical Hardware Status
//        float freeMem = 0;
//        float totalMem = 0;
//        float usedMem = 0;
//        float memUsage = 0;
//        float cpuUsage =0.0f;
//        
//        if (infoHW != null) {
//        	totalMem = Float.parseFloat(infoHW.get("total_mem"))/1000;
//        	usedMem = Float.parseFloat(infoHW.get("used_mem"))/1000;
//        	memUsage = (usedMem/totalMem)*100;
//        	cpuUsage = Float.parseFloat(infoHW.get("usr_cpu")) + Float.parseFloat(infoHW.get("sys_cpu"));
//        }
//        vars.put( "cpu_usage", decimal.format(cpuUsage));
//        vars.put( "mem_usage", decimal.format(memUsage));
//        vars.put( "used_mem", decimal.format(usedMem));
//        vars.put( "total_mem", decimal.format(totalMem));
//        log("--------------------------------------- log 1");
//        // JVM Memory Status
//        freeMem = Runtime.getRuntime().freeMemory() / 1000000;
//        totalMem = Runtime.getRuntime().totalMemory() / 1000000;
//        usedMem = totalMem - freeMem;
//        memUsage = (usedMem/totalMem)*100;
//        
//        vars.put( "jvm_mem_usage", decimal.format(memUsage));
//        vars.put( "used_jvm_mem", decimal.format(usedMem));
//        vars.put( "total_jvm_mem", decimal.format(totalMem));
//        
//        double total = 0.0f;
//        double used = 0.0f;
//        double percentage = 0.0f;
//        // NAND FLASH
//        File classpathRoot_NAND = new File("buffer");
//        if (classpathRoot_NAND.exists()) {
//        	total = 150*500000*0.000001;
//        	used = folderSize(classpathRoot_NAND)*0.000001;
//        	percentage = (used/total)*100;
//        }
//        log("--------------------------------------- log 1");
//    	log("total internal store: " + total + " - used internal store: " + used);
//    	vars.put( "internal_buffer_usage", decimal.format(percentage));
//    	vars.put( "in_cap_used", decimal.format(used));
//    	vars.put( "in_cap_total", decimal.format(total));
//    	        
//        // SD Card
//        total = 0;
//        used = 0;
//        percentage = 0;
//        File classpathRoot_SD = new File("buffer_SD");
//        if (classpathRoot_SD.exists()) {
//            if (classpathRoot_SD.getTotalSpace() != classpathRoot_NAND.getTotalSpace()) {
//            	if (Util.checkExternalStoreAlive("buffer_SD")) {
//            		total = classpathRoot_SD.getTotalSpace()*0.000001;
//            		used = folderSize(classpathRoot_SD)*0.000001;
//                	percentage = (used/total)*100;
//            	} else {
//            		log("Input/Output Error happened on external store");
//            	}
//            }
//        }
//
//    	log("total external store: " + total + " - used external store: " + used);
//    	vars.put( "external_buffer_usage", decimal.format(percentage));
//    	vars.put( "ex_cap_used", decimal.format(used));
//    	vars.put( "ex_cap_total", decimal.format(total));
//    	
//        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
//        
//        DateFormat formatDate = DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG, request.getLocale()); 
//        vars.put( "gateway_time_label", formatDate.format( new Date(System.currentTimeMillis())));
//        vars.put( "jvm_last_started", formatDate.format( new Date( runtimeBean.getStartTime())));
//        vars.put( "jvm_up_time", formatPeriod(runtimeBean.getUptime()));

        response.getWriter().print( body );
        
//    	response.getWriter().print(TEMPLATE);
    }
	
    private static final String formatPeriod( final long period )
    {
        final Long msecs = new Long( period % 1000 );
        final Long secs = new Long( period / 1000 % 60 );
        final Long mins = new Long( period / 1000 / 60 % 60 );
        final Long hours = new Long( period / 1000 / 60 / 60 % 24 );
        final Long days = new Long( period / 1000 / 60 / 60 / 24 );
        return MessageFormat.format(
            "{0,number} days {1,number,00}:{2,number,00}:{3,number,00}.{4,number,000}",
            new Object[]
                { days, hours, mins, secs, msecs } );
    }
    
    
    private void clearBuffer(String store){
    	ASTHandler.clearBuffer(store);
    }
    
    public long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }
    
    /**
     * Check Internet connectivity
     * @return true: if it is good
     * 		   false: if it is bad
     */
    boolean checkInternetConnectivity(){
    	try {
    		URL url = new URL("http://www.google.com");
    		HttpURLConnection con = (HttpURLConnection) url.openConnection();
    		con.connect();
    		if (con.getResponseCode() == 200){
    			return true;
    		}
    	} catch (Exception exception) {
    		log("No Connection");
    	}
		return false;
    	
    }
	
	public void log(String text){
		System.out.println(this.getClass().getSimpleName()+"---"+text);
	}

}
