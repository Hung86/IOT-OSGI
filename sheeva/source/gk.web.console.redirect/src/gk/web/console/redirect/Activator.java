package gk.web.console.redirect;

import java.io.IOException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

public class Activator implements BundleActivator, ServiceListener, HttpContext {

	private BundleContext bc;
	private ServiceReference sRef;
	private HttpService httpService;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bc) throws Exception {
		this.bc = bc;
		bc.addServiceListener(this, "(objectClass=" + HttpService.class.getName() + ")");
		if (this.sRef == null) {
			this.sRef = bc.getServiceReference(HttpService.class.getName());
		}
		if (this.sRef != null) {
			registerServlet();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bc) throws Exception {
		freeResources();
		bc.removeServiceListener(this);
	}
	
	public void serviceChanged(ServiceEvent event)
	{
		ServiceReference sR = event.getServiceReference();
		if ((1 == event.getType()) && (this.sRef == null))
		{
			this.sRef = sR;
		    try
		    {
		    	registerServlet();
		    }
		    catch (Exception e)
		    {
		    	//log.warning("Could not registerServlet!", e);
		    }
		}
		else if ((4 == event.getType()) && (sR.equals(this.sRef)))
		{
			freeResources();
		}
	}
	
	private void registerServlet() throws Exception
	{
	  this.httpService = ((HttpService)this.bc.getService(this.sRef));
	  
	
	  Servlet servlet = new Servlet(this.bc);
	  this.httpService.registerServlet("/", servlet, null, this);
	  //this.httpService.registerResources("/res", "/res", this);
	}
	
	private final void freeResources()
	{
	  if (this.httpService == null) {
	    return;
	  }
	  try
	  {
	    this.httpService.unregister("/");
	    //this.httpService.unregister("/res");
	  }
	  catch (Exception e)
	  {
	//    log.warning("Could not free the servlet!", e);
	  }
	  finally
	  {
	    this.bc.ungetService(this.sRef);
	    this.sRef = null;
	    this.httpService = null;
	  }
	}
	
	public String getMimeType(String name)
	{
	  return name.endsWith(".css") ? "text/css" : null;
	}
	
	public URL getResource(String name)
	{
	  return getClass().getResource(name);
	}
	
	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response)
	  throws IOException
	{
	  return true;
	}

}
