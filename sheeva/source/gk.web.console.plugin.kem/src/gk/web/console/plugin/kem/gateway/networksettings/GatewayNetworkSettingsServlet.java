package gk.web.console.plugin.kem.gateway.networksettings;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.webconsole.SimpleWebConsolePlugin;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenkoncepts.gateway.util.Util;


public class GatewayNetworkSettingsServlet extends SimpleWebConsolePlugin {

	    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		private static final String CATEGORY = "KEM";//Change Category
	    private static final String LABEL = "networksettings"; //$NON-NLS-1$
	    private static final String TITLE = "Network Settings"; //$NON-NLS-1$
	    private static final String RESOURCE_DIR = "res"; //$NON-NLS-1$
	    private static final String CSS[] = { "/" + LABEL + "/template/networksettings/gateway_networksettings.css"};
	    private static final String TEMPLATE = "/template/networksettings/gateway_networksettings.html";
	    
	    private static final Pattern IP_V4_PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	    //private static final Pattern REMOTE_HOST_PATTERN = Pattern.compile("^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([\\da-z\\.-]+)\\.([a-z\\.]{2,6})$");

	    // templates
	    
	    public static final int NO_CHANGE=0x1;
	    public static final int CHANGE_MODE=0x2;
	    public static final int CHANGE_ADDRESS=0x4;
	    public static final int CHANGE_NTP=0x8;


		private String OS;
	    /** Default constructor */
		public GatewayNetworkSettingsServlet()
	    {
	        super(LABEL, TITLE, CATEGORY, CSS);	        
	        OS = System.getProperty("os.name").toLowerCase();
			if ((OS.indexOf("win") >= 0)) {
				OS = "win";
			} else if ((OS.indexOf("mac") >= 0)) {
				OS = "mac";
			} else if ((OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 )) {
				OS = "linux";
				try {
					Util.setPermissions();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ((OS.indexOf("sunos") >= 0)) {
				OS = "sola";
			} else {
				OS = "";
			}
	    }

	    /**
	     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	     */
	    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	        throws IOException
	    {
	    	
	        String network_update = request.getParameter("network");
	        System.out.println("---network_update : " + network_update);
	        if (OS.equals("win")) {
	        	System.out.println("Changing Network doesn't support for Gateway running window !" );

	        	response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
	        	return;
	        }
	        
		if (network_update != null) {
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, String> networkMap = objectMapper.readValue(network_update, Map.class);
			
			int flag = checkNetworkChange(networkMap);
			
			if ((flag & NO_CHANGE) == NO_CHANGE) {
				System.out.println("Network Setting doesn't change any things");
				return;
			}
			
			String mode = networkMap.get("mode");

			if ((flag & CHANGE_MODE) == CHANGE_MODE) {
				if (!Util.configNetwork(mode)) {
					response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "Can not change mode to " + mode);
					return;
				}
				System.out.println("Network changed to " + mode);
			}
			
			if ((flag & CHANGE_ADDRESS) == CHANGE_ADDRESS) {
				if (mode.equals("static")) {
					if (validateAddress(networkMap)) {
						if(!Util.changeStaticSettings(networkMap)) {
							response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "Can not change settings of Static Ip");
						}
						System.out.println("Network changed Static IP settings ");
					} else {
						response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "Wrong address format");
					}
				}
			}
			
			if (!Util.restartNetwork()) {
				response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "Fail to restart networking");
			}
			
			if ((flag & CHANGE_NTP) == CHANGE_NTP) {
				System.out.println("Network changed NTP settings ");
				Util.setNtpServer(networkMap.get("ntp1"), networkMap.get("ntp2"));
			}	

		}
	}

	    /**
	     * @see org.apache.felix.webconsole.AbstractWebConsolePlugin#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	     */
	    protected void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException
	    {
	        String id = request.getParameter("id");

	        if(id != null){
	        	if(id.equals("network")){
		        	final PrintWriter out = response.getWriter();
					System.out.println("Do Get:Network");
					response.setContentType("application/json"); //$NON-NLS-1$
	                response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
					renderNetwork(out);
				}
	        	return;
	        }
	        super.doGet(request, response);
	    }

	    /**
	     * @see org.apache.felix.webconsole.AbstractWebConsolePlugin#renderContent(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	     */
	    protected void renderContent(HttpServletRequest request, HttpServletResponse response)
	        throws IOException
	    {
	        response.getWriter().print(readTemplateFile(TEMPLATE));

	    }

	private void renderNetwork(final PrintWriter pw) throws IOException
	{
		JSONObject networkJson = new JSONObject(getNetworkSettings());
		pw.write(networkJson.toString());
	}
	
	private Map<String, String> getNetworkSettings() {
		Map<String, String> network = new HashMap<String, String>();
		String mode = "dhcp";
		String ip = "";
		String subnet = "";
		String gateway = "";
		String dns1 = "";
		String dns2 = "";
		String ntp1 = "";
		String ntp2 = "";
		try {
			mode = Util.findNetworkMode();
			ip = Util.findLocalIP();
			subnet = Util.findSubnet();
			gateway =Util.findGateway();
			if (OS.equals("linux")) {
				ArrayList<String> dnsArray = Util.findDns();
				if (!dnsArray.isEmpty()) {
					dns1 = dnsArray.get(0);
					if (dnsArray.size() > 1) {
						dns2 = dnsArray.get(1);
					}
				}
				
				ArrayList<String> ntpArray = Util.findNtpServer();
				if (!ntpArray.isEmpty()) {
					ntp1 = ntpArray.get(0);
					if (ntpArray.size() > 1) {
						ntp2 = ntpArray.get(1);
					}
				}
			}
		}  catch (Exception e) {
			e.printStackTrace();
		}
		network.put("mode", mode);
		network.put("ip", ip);
		network.put("subnet", subnet);
		network.put("gateway", gateway);
		network.put("dns1", dns1);
		network.put("dns2", dns2);
		network.put("ntp1", ntp1);
		network.put("ntp2", ntp2);
		
		System.out.println("-----get network = " + network);
		return network;
	}
	
	private int checkNetworkChange(Map<String, String> newNetworkSettings) {
		Map<String, String> currentNetworkSettings = getNetworkSettings();
		int flag = 0;
		if (!newNetworkSettings.get("mode").equals(currentNetworkSettings.get("mode"))) {
			flag = flag | CHANGE_MODE;
			if ("static".equals(newNetworkSettings.get("mode"))) {
				flag = flag | CHANGE_ADDRESS;
			}
		} else if (!newNetworkSettings.get("ip").equals(currentNetworkSettings.get("ip")) ||
				!newNetworkSettings.get("subnet").equals(currentNetworkSettings.get("subnet")) ||
				!newNetworkSettings.get("dns1").equals(currentNetworkSettings.get("dns1")) ||
				!newNetworkSettings.get("dns2").equals(currentNetworkSettings.get("dns2"))) {
			flag = flag | CHANGE_ADDRESS;
		}
		
		if (!newNetworkSettings.get("ntp1").equals(currentNetworkSettings.get("ntp1")) ||
				!newNetworkSettings.get("ntp2").equals(currentNetworkSettings.get("ntp2"))) {
			flag = flag | CHANGE_NTP;
		}

		
		return flag;
	}
	
	private boolean validateAddress (Map<String, String> networkMap) {
		Matcher match = IP_V4_PATTERN.matcher(networkMap.get("ip"));
		if (!match.matches()) {
			return false;
		}
		match = IP_V4_PATTERN.matcher(networkMap.get("subnet"));
		if (!match.matches()) {
			return false;
		}
		match = IP_V4_PATTERN.matcher(networkMap.get("gateway"));
		if (!match.matches()) {
			return false;
		}
		
		return true;
	}
}
