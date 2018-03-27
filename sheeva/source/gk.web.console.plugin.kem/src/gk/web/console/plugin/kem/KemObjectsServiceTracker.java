package gk.web.console.plugin.kem;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.greenkoncepts.gateway.api.adapter.AModbusAdapter;
import com.greenkoncepts.gateway.api.adapter.Adapter;
import com.greenkoncepts.gateway.api.bridge.BridgeMaster;

public class KemObjectsServiceTracker implements ServiceTrackerCustomizer {
    protected ArrayList<Adapter> adapterServices;
	protected ServiceTracker adapterTracker;
	protected BridgeMaster bridgeMasterService;
	protected ServiceTracker bridgeMasterTracker;
	protected BundleContext context;
	
	public KemObjectsServiceTracker() {
		adapterServices = new ArrayList<Adapter>(); 
		adapterTracker = null;
		bridgeMasterService = null;
		bridgeMasterTracker = null;
		context = null;
	}
	
	public void activate( BundleContext bundleContext )
	{
		context = bundleContext;
		adapterTracker = new ServiceTracker(bundleContext, Adapter.class.getName(), (ServiceTrackerCustomizer) this);
		if(adapterTracker != null){
			adapterTracker.open();
		}

		bridgeMasterTracker = new ServiceTracker(bundleContext, BridgeMaster.class.getName(), (ServiceTrackerCustomizer) this);
		if(bridgeMasterTracker != null){
			bridgeMasterTracker.open();
		}
	}
	
	public void deactivate()
	{
		if(adapterTracker != null){
			adapterTracker.close();
		}
		
		if(bridgeMasterTracker != null){
			bridgeMasterTracker.close();
		}
	}
	
    /**
     * Check Serial Error
     * @return serial errors
     */
    public int getSerialError(){
    	int error = 0;
    	for (int i = 0; i < adapterServices.size(); i++)
	    {
	    	Adapter adapter = adapterServices.get(i);
	        error += adapter.getCommunicationErrorIn24hrs();
	    } 
	    return error;
    	
    }
    
    public boolean setDeviceConfig(String adapter, String deviceAddr, String data) {
    	
    	return true;
    }
    
    public Map<Integer, String> getDeviceConfiguration(String adapter, String deviceAddr, boolean force) {
    	Adapter adapterService = getAdapterService(adapter);
    	Map<Integer, String> result = null;
    	if (adapterService != null) {
    		try {
    	
    			if (adapterService.getAdapterType() == Adapter.MODBUS_TYPE) {
    				adapterService.setMode(Adapter.CONFIGURATION_MODE);
    				result = ((AModbusAdapter)adapterService).getConfig(deviceAddr);
    				adapterService.setMode(Adapter.STORED_DATA_MODE);
    				return result;
    			}
    		} catch (NumberFormatException e) {
    			System.out.println("KemObjectsServiceTracker.getDeviceConfiguration : deviceAddr is not string of integer number");
    		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}

    	return result;
    }
    
    public List<Integer> writeDeviceCongiguration(String adapter,  String deviceAddr, Map <Integer, String> hashConfig) {
    	Adapter adapterService = getAdapterService(adapter);
    	List<Integer> result = null;
    	if (adapterService != null) {
    		try {
    			if (adapterService.getAdapterType() == Adapter.MODBUS_TYPE) {
    				adapterService.setMode(Adapter.CONFIGURATION_MODE);
    				result  = ((AModbusAdapter)adapterService).setConfig(deviceAddr, hashConfig);
    				adapterService.setMode(Adapter.STORED_DATA_MODE);
    				return result;
    			}
    		} catch (NumberFormatException e) {
    			System.out.println("KemObjectsServiceTracker.writeDeviceCongiguration : deviceAddr is not string of integer number");
    		} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	return result;
    }
    /**
     * Check Internet Error
     * @return serial errors
     */
    public Map<String, Long> getBridgeStatus(){
    	if(bridgeMasterService != null){
    		return bridgeMasterService.getBridgeStatus();
    	}
	    return new Hashtable<String, Long>();
    	
    }

	public Map<String, String> getGatewayStatus() {
		if (bridgeMasterService != null) {
			return bridgeMasterService.getGatewayStatus();
		}
		return null;
	}

	public Map<String, String> getCurrentBridgeSettings() {
		if (bridgeMasterService != null) {
			return bridgeMasterService.getCurrentBridgeSettings();
		}
		return new Hashtable<String, String>();
	}

	public Map<String, String> getBridgeSettingsBy(String mode) {
		if (bridgeMasterService != null) {
			return bridgeMasterService.getBridgeSettingsBy(mode);
		}
		return new Hashtable<String, String>();
	}

	public boolean updateBridgeSettings(Map<String, String> settings) {
		if (bridgeMasterService != null) {
			settings.put("last_modified", String.valueOf(System.currentTimeMillis()));
			return bridgeMasterService.updateBridgeSettings(settings);
		}
		return false;
	}
    
    public void clearBuffer(String store){
    	if(bridgeMasterService != null){
    		bridgeMasterService.clearBufferStorage(store);
    	}
    	
    }
    
	public boolean updateGatewayStatus(Long totalReadData, Long totalSentData){
		if(bridgeMasterService != null){
			return bridgeMasterService.updateGatewayStatus(totalReadData, totalSentData);
		}
		return false;
	}
    
    public long getUsableStorage(String store){
    	if(bridgeMasterService != null){
    		return bridgeMasterService.getUsableStorage(store);
    	}
    	return 0;
    }
    
	public String getAdapterVersion(String adapter) {
		Bundle[] bundles = context.getBundles();
		String adapterVersion = "Unknown";
		for (Bundle bundle : bundles) {
			String simpleName = FilenameUtils.getExtension(bundle.getSymbolicName());
			if((!simpleName.isEmpty()) && (!simpleName.equals("adapter")) 
					&& (adapter.toLowerCase().contains(simpleName))){
				adapterVersion = bundle.getVersion().toString() ;
				break;
			}
		}
		
		return adapterVersion;
	}
	
	public String getFullBundleName(String adapter) {
		Bundle[] bundles = context.getBundles();
		String bundleName = "Unknown";
		for (Bundle bundle : bundles) {
			String simpleName = FilenameUtils.getExtension(bundle.getSymbolicName());
			if((bundle.getSymbolicName().startsWith("com.greenkoncepts.gateway.adapter.")) && (adapter.toLowerCase().contains(simpleName))){
				bundleName = bundle.getSymbolicName() + "_" + bundle.getVersion().toString();
				break;
			}
		}
		System.out.println("===> bundleName = " + bundleName);
		return bundleName;
	}
	
	public boolean uninstallBundle(String adapter) {
		Bundle[] bundles = context.getBundles();
		for (Bundle bundle : bundles) {
			String simpleName = FilenameUtils.getExtension(bundle.getSymbolicName());
			if((bundle.getSymbolicName().startsWith("com.greenkoncepts.gateway.adapter.")) && (adapter.toLowerCase().contains(simpleName))){
				try {
					bundle.stop();
					Thread.sleep(1000);
					bundle.uninstall();
					return true;
				} catch (BundleException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
		return false;
	}
	
	public boolean restartBundle(String adapter) {
		Bundle[] bundles = context.getBundles();
		for (Bundle bundle : bundles) {
			String simpleName = FilenameUtils.getExtension(bundle.getSymbolicName());
			if((bundle.getSymbolicName().startsWith("com.greenkoncepts.gateway.adapter.")) && (adapter.toLowerCase().contains(simpleName))){
				try {
					bundle.stop();
					Thread.sleep(500);
					bundle.start();
					System.out.println("===> bundleName = " + adapter + " has just restarted !");
					return true;
				} catch (BundleException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
		return false;
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
	
	public BridgeMaster getBridgeMasterService() {
		return bridgeMasterService;
	}
	
	
	public void log(String text){
		System.out.println(this.getClass().getSimpleName()+"---"+text);
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
					log("Kem Service ...add Adapter: "+service.getClass().getName());
				}
			}
		} else if(context.getService(arg0)instanceof BridgeMaster){
			bridgeMasterService = (BridgeMaster) context.getService(arg0);
			log("Kem Service ...add BridgeMaster: "+bridgeMasterService.getClass().getName());
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
					log("Kem Service ...modified Adapter: "+service.getClass().getName());
				}
			}
		} else if(context.getService(arg0)instanceof BridgeMaster){
			bridgeMasterService = (BridgeMaster) context.getService(arg0);
			log("Kem Service ...modified BridgeMaster: "+bridgeMasterService.getClass().getName());

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
					log("Kem Service ...remove Adapter: "+service.getClass().getName());
				}
			}
			context.ungetService(arg0);
		} else if(context.getService(arg0)instanceof BridgeMaster){
			log("Kem Service ...remove BridgeMaster: "+bridgeMasterService.getClass().getName());
			bridgeMasterService = null;
			context.ungetService(arg0);
		}
	}
}
