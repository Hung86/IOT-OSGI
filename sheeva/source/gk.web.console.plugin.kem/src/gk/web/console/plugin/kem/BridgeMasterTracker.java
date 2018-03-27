package gk.web.console.plugin.kem;

import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.greenkoncepts.gateway.api.bridge.BridgeMaster;

public class BridgeMasterTracker implements ServiceTrackerCustomizer {
    protected BridgeMaster bridgeMasterService;
	protected ServiceTracker bridgeMasterTracker;
	
	BridgeMasterTracker() {
		
	}
	@Override
	public Object addingService(ServiceReference arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void modifiedService(ServiceReference arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removedService(ServiceReference arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

}
