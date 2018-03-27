package gk.web.console.plugin.kem.file;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.webconsole.SimpleWebConsolePlugin;
import org.json.JSONException;
import org.json.JSONWriter;
import org.osgi.framework.BundleContext;


public class KEMLogServlet extends SimpleWebConsolePlugin
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5498354399053970088L;
	
	private static final String CATEGORY = "KEM";//Change Category
    private static final String LABEL = "logviewer"; //$NON-NLS-1$
    private static final String TITLE = "Log Viewer"; //$NON-NLS-1$
    private static final String CSS[] = {  }; // yes, it's correct! //$NON-NLS-1$
    private static final String RES = "/" + LABEL + "/res/"; //$NON-NLS-1$ //$NON-NLS-2$

    // templates
    private final String TEMPLATE = "/template/logviewer/logviewer.html";

    private final String OS;//Information about Operating System
    
    private final LogFileViewerRequestHandler handler;
    
    private final boolean debug = true;
    /**
     *
     */
    public KEMLogServlet()
    {
        super( LABEL, TITLE, CSS );

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
		handler = new LogFileViewerRequestHandler();
		handler.start();
    }

    /**
	* @see org.apache.felix.webconsole.AbstractWebConsolePlugin#activate(org.osgi.framework.BundleContext)
	*/
	@Override
	public void activate( BundleContext bundleContext )
	{
		super.activate( bundleContext );
		log("Log Servlet starts");
	}


	/**
	* @see org.apache.felix.webconsole.SimpleWebConsolePlugin#deactivate()
	*/
	public void deactivate()
	{
		handler.stop();
		super.deactivate();
		log("Log Servlet stops");
	}
    
    public String getCategory()
    {
        return CATEGORY;
    }


    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
        IOException
    {
        super.doPost( request, response );
    }
    
    
    /**
     * @see org.apache.felix.webconsole.AbstractWebConsolePlugin#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String id = null;

        id = request.getParameter("id");
        if(id != null){
        	if(id.equals("start")){
	        	final PrintWriter out = response.getWriter();
				log("Do Get: start log");
				response.setContentType("text/plain");
                response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
                final JSONWriter jw = new JSONWriter(out);
                try {
					jw.object();					// TODO Auto-generated catch block
					jw.key("logId");
					jw.value(handler.startLog());
					jw.endObject();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else if (id.equals("get")) {
	        	final PrintWriter out = response.getWriter();
				String line;
				int hashCode = Integer.parseInt(request.getParameter("log_id"));
				handler.updateStartTime(hashCode);
				StringBuffer logs = new StringBuffer();
    			while ((line = handler.getLog(hashCode)) != null) {
    				logs.insert(0, "<li>" + line + "</li>");
    			}
    			if (logs.length() == 0) {

    			} else {
    				out.println(logs);

    			}
			} else if (id.equals("stop")) {
				log("Do Get: stop log");
				int hashCode = Integer.parseInt(request.getParameter("log_id"));
				handler.stopLog(hashCode);
			}
        	return;
        }
        super.doGet(request, response);
    }
    

	@Override
	protected void renderContent(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().print(readTemplateFile(TEMPLATE));
	}
	

	
	private class LogFileViewerRequestHandler{
		Thread fileMonitorThread;
		FileMonitor fileMonitor;
		LogFileViewerRequestHandler(){
			fileMonitorThread = null;
		}
		
		public void start(){
			fileMonitor = new FileMonitor(FILE_NAME);
			fileMonitorThread = new Thread(fileMonitor, "FileMonitor(" + fileMonitor.getFileName() + ")");
			fileMonitorThread.start();
		}
		
		public void stop(){
			//Clear Buffer Map
			fileMonitorThread.interrupt();
		}
		
		public int startLog(){
			return fileMonitor.createNewObserver();
		}
		
		public void stopLog(int hashCode){
			fileMonitor.removeObserver(hashCode);
		}
		
		public String getLog(int hashCode){
			return fileMonitor.readLineFromObserver(hashCode);
		}
	
		public void updateStartTime(int hashCode){
			fileMonitor.updateStartTime(hashCode);
		}
		
		private static final String FILE_NAME = "logs";
		
	}
	public void log(String text){
		if(debug){
			System.out.println(this.getClass().getSimpleName()+"---"+text);
		}
	}
	
}