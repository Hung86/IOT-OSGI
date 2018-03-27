package com.greenkoncepts.gateway.control.rest.server;

import java.util.ArrayList;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greekoncepts.gateway.api.schedule.Schedule;
import com.greenkoncepts.gateway.api.adapter.Adapter;
import com.greenkoncepts.gateway.api.task.ITaskExecute;

public class RestServiceTracker implements ServiceTrackerCustomizer {
	private static RestServiceTracker serviceTracker;
	
    protected ArrayList<Adapter> adapterServices;
	protected ServiceTracker adapterTracker;
	
    protected ITaskExecute taskExecuteService;
    protected ServiceTracker taskTracker;
    
	protected BundleContext context;

	protected Schedule schService;
	protected ServiceTracker schServiceTracker;

	private Logger mLogger = LoggerFactory.getLogger("RestServiceTracker");
	
	private RestServiceTracker() {
		adapterServices = new ArrayList<Adapter>(); 
		adapterTracker = null;
		context = null;
	}
	
	public void activate( BundleContext bundleContext )
	{
		context = bundleContext;
		adapterTracker = new ServiceTracker(bundleContext, Adapter.class, (ServiceTrackerCustomizer) this);
		if(adapterTracker != null){
			adapterTracker.open();
		}
		
		schServiceTracker = new ServiceTracker(bundleContext, Schedule.class, (ServiceTrackerCustomizer) this);
		if(schServiceTracker != null){
			schServiceTracker.open();
		}

		taskTracker = new ServiceTracker(bundleContext, ITaskExecute.class, (ServiceTrackerCustomizer) this);
		if(taskTracker != null){
			taskTracker.open();
		}

	}
	
	public void deactivate()
	{
		if(adapterTracker != null){
			adapterTracker.close();
		}

		if(schServiceTracker != null){
			schServiceTracker.close();
		}

		if(taskTracker != null){
			taskTracker.close();
		}
		
	}
	
	public Adapter getAdapterService(String name)
	{
	    for (int i = 0; i < adapterServices.size(); i++)
	    {
	    	Adapter adapter = adapterServices.get(i);
	        if (name.equals(adapter.getClass().getSimpleName()))
	        {
	            return adapter;
	        }
	    } 
	    return null;
	}
	
	
	private int getAdapterIndex(String name)
	{
	    for (int i = 0; i < adapterServices.size(); i++)
	    {
	    	Adapter adapter = adapterServices.get(i);
	        if (name.equals(adapter.getClass().getSimpleName()))
	        {
	            return i;
	        }
	    } 
	    return -1;
	}
	
	public ArrayList<Adapter> getAdapterServices() {
		return adapterServices;
	}
	
	public Schedule getScheduleService() {
		return schService;
	}

	public ITaskExecute getTaskExecuteService() {
		return taskExecuteService;
	}
	
	@Override
	public Object addingService(ServiceReference arg0) {
		if(context.getService(arg0) instanceof Adapter){
			Adapter service = (Adapter) context.getService(arg0);
			if(!adapterServices.contains(service)){
				int index = getAdapterIndex(service.getClass().getSimpleName());
				//only add if array does not have same adapter name
				if(index == -1){
					adapterServices.add(service);
					mLogger.info("...add Adapter: "+service.getClass().getName());
				}
			}
		} else if (context.getService(arg0) instanceof Schedule) {
			schService = (Schedule) context.getService(arg0);
			mLogger.info("...add Schedule service: "+ schService.getClass().getName());
		} else if (context.getService(arg0) instanceof ITaskExecute) {
			taskExecuteService = (ITaskExecute) context.getService(arg0);
			mLogger.info("...add ITaskExecute: "+taskExecuteService.getClass().getName());
		}
		return arg0;
	}

	@Override
	public void modifiedService(ServiceReference arg0, Object arg1) {
		if(context.getService(arg0) instanceof Adapter){
			Adapter service = (Adapter) context.getService(arg0);
			if(adapterServices.contains(service)){
				int index = getAdapterIndex(service.getClass().getSimpleName());
				//only remove if array has same adapter name
				if(index != -1){
					adapterServices.set(index,service);
					mLogger.info("...modified Adapter: "+service.getClass().getName());
				}
			}
		} else if (context.getService(arg0) instanceof Schedule) {
			schService = (Schedule) context.getService(arg0);
			mLogger.info("...modified Schedule service: "+ schService.getClass().getName());
		} else if (context.getService(arg0) instanceof ITaskExecute) {
			taskExecuteService = (ITaskExecute) context.getService(arg0);
			mLogger.info("...modified ITaskExecute: "+taskExecuteService.getClass().getName());
		}
		
	}

	@Override
	public void removedService(ServiceReference arg0, Object arg1) {
		if(context.getService(arg0) instanceof Adapter){
			Adapter service = (Adapter) context.getService(arg0);
			if(adapterServices.contains(service)){
				int index = getAdapterIndex(service.getClass().getSimpleName());
				//only remove if array has same adapter name
				if(index != -1){
					adapterServices.remove(service);
					mLogger.info("...remove Adapter: "+service.getClass().getName());
				}
			}
			context.ungetService(arg0);
		} else if (context.getService(arg0) instanceof Schedule) {
			mLogger.info("...remove Schedule service: "+ schService.getClass().getName());
			schService = null;
		} else if (context.getService(arg0) instanceof ITaskExecute) {
			mLogger.info("...remove ITaskExecute: "+taskExecuteService.getClass().getName());
			taskExecuteService = null;
		}
	}
	
	public static RestServiceTracker getInstance() {
		if (serviceTracker == null) {
			serviceTracker = new RestServiceTracker();
		}
		return serviceTracker;
	}
}
