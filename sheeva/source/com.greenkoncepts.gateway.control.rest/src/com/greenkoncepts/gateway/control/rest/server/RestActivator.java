package com.greenkoncepts.gateway.control.rest.server;

import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.greenkoncepts.ligting.control.cloud.mqtt.MqttBridge;

public class RestActivator implements BundleActivator,ServiceListener,HttpContext  {
	private static BundleContext context;
	private ServiceReference sRef;
	private HttpService httpService;
//	private RestBridge restBridge;
//	private MqttBridge mqttBridge;
	private RestApiController restController;
	private RestServiceTracker restServiceTracker;
	private Logger mLogger = LoggerFactory.getLogger("RestActivator");


	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		RestActivator.context = bundleContext;
		restServiceTracker = RestServiceTracker.getInstance();
		restServiceTracker.activate(bundleContext);
		context.addServiceListener(this, "(objectClass="+HttpService.class.getName()+")");
		if(sRef == null){
			sRef = context.getServiceReference(HttpService.class.getName());
		}
		if(sRef != null){
			registerServlet();
		}
//		restBridge = new RestBridge();
//		restBridge.start(bundleContext);
//		mqttBridge = new MqttBridge();
//		mqttBridge.start(bundleContext);
		//controllerResource = ControllerResource.getInstance();
		//controllerResource.start(context);
		//restController = new RestApiController();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		freeResources();
		
//		restBridge.stop(bundleContext);
//		mqttBridge.stop(bundleContext);
		//controllerResource.stop();
		
		context.removeServiceListener(this);
		RestActivator.context = null;
	}

	@Override
	public String getMimeType(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getResource(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean handleSecurity(HttpServletRequest arg0,
			HttpServletResponse arg1) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void serviceChanged(ServiceEvent event) {
		switch (event.getType()){
        case ServiceEvent.REGISTERED:            	
            try {
				registerServlet();
			} catch (Exception e) {
				mLogger.error("Exception", e);
			}
            break;

        case ServiceEvent.UNREGISTERING:            	
        	freeResources();
            break;
    }
	}
	
	private void registerServlet() throws Exception{
		if (sRef == null){
			sRef = context.getServiceReference(HttpService.class.getName());
        }
	 
        if (sRef != null){
        	httpService = (HttpService)context.getService(sRef);
    		//RESTApiServlet servlet = new RESTApiServlet(context);
    		//httpService.registerServlet("/RestApi", servlet, null, this);
    		//httpService.registerResources("/RestApi", "/RestApi", this);
    		Dictionary<String, String> params = new Hashtable<String, String>();
    		params.put("resteasy.scan", "false");
    		params.put("javax.ws.rs.Application", RestAppDispatcher.class.getName());
    		
    		//System.out.print("Dispatcher:"+bridge);
    		httpService.registerServlet("/control",  new HttpServletDispatcher(), params, null);
        }
		
	}
	
	private void freeResources(){
		if(httpService == null){
			return;
		}
		try{
			httpService.unregister("/control");
		}catch(Exception e){
			mLogger.error("Exception", e);
		}finally{
			context.ungetService(sRef);
			sRef = null;
			httpService = null;
		}
	}

}
