package gk.web.console.plugin.kem.device.settings;

import gk.web.console.plugin.kem.KemObjectsServiceTracker;
import gk.web.console.plugin.kem.KemValidation;
import gk.web.console.plugin.kem.Network;
import gk.web.console.plugin.kem.adapter.settings.AdapterSettingsServlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.felix.webconsole.SimpleWebConsolePlugin;
import org.json.JSONException;
import org.json.JSONWriter;
import org.osgi.framework.BundleContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenkoncepts.gateway.api.adapter.ABacnetAdapter;
import com.greenkoncepts.gateway.api.adapter.AHttpAdapter;
import com.greenkoncepts.gateway.api.adapter.Adapter;
import com.greenkoncepts.gateway.util.Util;
import com.greenkoncepts.gateway.util.UtilLog;

public class DeviceSettingsServlet extends SimpleWebConsolePlugin {
	private static final long serialVersionUID = 1L;
	private static final String CATEGORY = "KEM"; //$NON-NLS-1$
	private static final String TITLE = "Device Settings"; //$NON-NLS-1$
	private static final String DEVICESETTINGS = "devicesettings";
	// private static final String CSS[] = { "/" + LABEL + "/" + RESOURCE_DIR +
	// "/style.css"};
	private final static String LABEL = DEVICESETTINGS;


	private static final String CSS[] = { "/" + DEVICESETTINGS + "/template/devicesettings/style.css" };; // yes, it's correct! //$NON-NLS-1$
	
	private KemObjectsServiceTracker ASTHandler;
	private UtilLog logger = UtilLog.getInstance(AdapterSettingsServlet.class
			.getSimpleName());
	private int paging = 20;

	private int totalPages = 0;
	private Object syncObject = new Object();
	private List<Map<String, String>> configXDKCache = new ArrayList<Map<String,String>>();


	public DeviceSettingsServlet(KemObjectsServiceTracker ASTHandler_) {
		super(LABEL, TITLE, CATEGORY, CSS);
		ASTHandler = ASTHandler_;

	}

	/**
	 * @see org.apache.felix.webconsole.AbstractWebConsolePlugin#activate(org.osgi.framework.BundleContext)
	 */
	public void activate(BundleContext bundleContext) {
		super.activate(bundleContext);
		
	}

	/**
	 * @see org.apache.felix.webconsole.SimpleWebConsolePlugin#deactivate()
	 */
	public void deactivate() {
	
		super.deactivate();
		logger.Log("WebConsolePlugin stops");
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String id = request.getParameter("id");
		String isChannel = request.getParameter("channel");
		ObjectMapper mapper = new ObjectMapper();
		String getAllData = request.getParameter("get_all_data");
		String scan = request.getParameter("scan");
		String action = request.getParameter("action");
		if ("true".equals(scan)) {
			String adapterName = request.getParameter("adapterName");
			Adapter adapter = ASTHandler.getAdapterService(adapterName);
			String address = request.getParameter("addressId");
			String instanceId = request.getParameter("instanceId");
			if (adapter.getAdapterType() == Adapter.BACNET_TYPE
					|| adapter.getAdapterType() == Adapter.OPCUA_TYPE) {
				((ABacnetAdapter) adapter).scanDeviceObjectIdentifier(address,
						instanceId);
			}

			final PrintWriter out = response.getWriter();
			response.setContentType("application/json"); //$NON-NLS-1$
			response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
			out.print("");
			out.flush();
			return;

		}
		if ("true".equals(getAllData)) {
			final PrintWriter out = response.getWriter();
			response.setContentType("application/json"); //$NON-NLS-1$
			response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
			String adapterName = request.getParameter("adapterName");
			Adapter adapter = ASTHandler.getAdapterService(adapterName);
			String address = request.getParameter("addressId");
			String instanceId = request.getParameter("instanceId");
			String indexPage = request.getParameter("indexPage");
			List<Map<String, String>> list = new ArrayList<Map<String, String>>();
			if (adapter.getAdapterType() == Adapter.BACNET_TYPE
					|| adapter.getAdapterType() == Adapter.OPCUA_TYPE) {
				list = ((ABacnetAdapter) adapter).getNodeScanningPage(address,
						instanceId, indexPage, 0,"");
			}

			String renderJson = mapper.writeValueAsString(list);
			out.print(renderJson);
			out.flush();
			return;
		}

		if ("true".equals(isChannel)) {
			final PrintWriter out = response.getWriter();
			response.setContentType("application/json"); //$NON-NLS-1$
			response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
			String adapterName = request.getParameter("adapterName");
			Adapter adapter = ASTHandler.getAdapterService(adapterName);
			String address = request.getParameter("addressId");
			String instanceId = request.getParameter("instanceId");
			String indexPage = request.getParameter("indexPage");
			String tab = request.getParameter("tab");
			List<Map<String, String>> list = new ArrayList<Map<String, String>>();
			Map<String, Object> result = new HashMap<String, Object>();
			Integer recordNum = Integer.parseInt(request
					.getParameter("record_num"));
			String type = request.getParameter("type");
			synchronized (syncObject) {
				paging = recordNum;
				if ("real_node".equals(tab)) {
					totalPages = ((ABacnetAdapter) adapter).numOfNode(
							address, instanceId, ABacnetAdapter.SCANNING_NODE,type);
					Double pages = Math.ceil((Double.valueOf(totalPages) / paging));
					String isCreateNew = request.getParameter("isCreateNew");
					if("true".equals(isCreateNew)){
						indexPage = String.valueOf(pages.intValue());
					}
					list = ((ABacnetAdapter) adapter).getRealNodeReadingPage(address,
							instanceId, indexPage, paging);
					
					
				

					
					result.put("total", pages);
					result.put("device_attributes", list);
				} else if ("virtual_node".equals(tab)) {
					list = ((ABacnetAdapter) adapter).getVirtualNodeReadingPage(
							address, instanceId, indexPage, paging);
					int totalPagesVitural = ((ABacnetAdapter) adapter)
							.numOfNode(address, instanceId, ABacnetAdapter.VIRTUAL_READING_NODE,"");
					result.put("total_vitural",
							Math.ceil(Double.valueOf(totalPagesVitural) / 20));
					result.put("device_attributes_vitural", list);
				}

				String renderJson = mapper.writeValueAsString(result);
				out.print(renderJson);
				out.flush();
			}
			return;
		}
		if("true".equals(request.getParameter("scanning"))){
			final PrintWriter out = response.getWriter();
			response.setContentType("application/json"); //$NON-NLS-1$
			response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
			String adapterName = request.getParameter("adapterName");
			Adapter adapter = ASTHandler.getAdapterService(adapterName);
			String address = request.getParameter("addressId");
			String instanceId = request.getParameter("instanceId");
			String indexPage = request.getParameter("indexPage");
			Integer recordNum = Integer.parseInt(request
					.getParameter("record_num"));
			String type = request.getParameter("type");
			
			
			paging = recordNum;
			List<Map<String, String>> list = new ArrayList<Map<String, String>>();
			list = ((ABacnetAdapter) adapter).getNodeScanningPage(address,
					instanceId, indexPage, paging,type);
			if (indexPage.equals("1")) {
				totalPages = ((ABacnetAdapter) adapter).numOfNode(
						address, instanceId, ABacnetAdapter.SCANNING_NODE,type);
			}
			
			double pages = Math.ceil((Double.valueOf(totalPages) / paging));
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("total", pages);
			result.put("device_attributes", list);
			String renderJson = mapper.writeValueAsString(result);
			out.print(renderJson);
			out.flush();
			return;
		}
		if("true".equals(request.getParameter("writing"))){
			final PrintWriter out = response.getWriter();
			response.setContentType("application/json"); //$NON-NLS-1$
			response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
			String adapterName = request.getParameter("adapterName");
			Adapter adapter = ASTHandler.getAdapterService(adapterName);
			String address = request.getParameter("addressId");
			String instanceId = request.getParameter("instanceId");
			String indexPage = request.getParameter("indexPage");
			Integer recordNum = Integer.parseInt(request
					.getParameter("record_num"));
			paging = recordNum;
			String type = request.getParameter("type");
			List<Map<String, String>> list = new ArrayList<Map<String, String>>();
			list = ((ABacnetAdapter) adapter).getNodeWrittingPage(address,
					instanceId, indexPage, paging);
			if (indexPage.equals("1")) {
				totalPages = ((ABacnetAdapter) adapter).numOfNode(
						address, instanceId, ABacnetAdapter.WRITTING_NODE,type);
			}
			
			double pages = Math.ceil((Double.valueOf(totalPages) / paging));
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("total", pages);
			result.put("device_attributes", list);
			String renderJson = mapper.writeValueAsString(result);
			out.print(renderJson);
			out.flush();
			return;
		}
		if ("adapters".equals(id)) {
			try {
				logger.Log("Do Get:Adapters");
				final PrintWriter out = response.getWriter();
				response.setContentType("application/json"); //$NON-NLS-1$
				response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
				ArrayList<Adapter> allAdapter = ASTHandler.getAdapterServices();
				Map<String, Object> allResults = new HashMap<String, Object>();
				
				Map<String, String> gwSettings = ASTHandler.getBridgeSettingsBy("socket");
				String gwid = "";
				if (gwSettings.containsKey("customer_id")) {
					gwid = gwSettings.get("customer_id");
				}
				
				if (gwSettings.containsKey("gateway_id")) {
					gwid = gwid + "-" + gwSettings.get("gateway_id");
				}
				
				allResults.put("gwid", gwid);
				
				Map<String, Object> result = new HashMap<String, Object>();
				for (Adapter adapter : allAdapter) {
					String adapterName = adapter.getClass().getSimpleName();
					List<Map<String, String>> deviceList = adapter
							.getDeviceList();
					if (deviceList == null || deviceList.size() == 0) {
						continue;
					}
					result.put(adapterName, deviceList);
				}
				
				allResults.put("gwdata", result);
				
				String jsonString = mapper.writeValueAsString(allResults);
				out.print(jsonString);
				out.flush();
				return;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if("exportingXDK".equals(request.getParameter("action"))){
			String adapterName = request.getParameter("adapter");
			String deviceid = request.getParameter("deviceid");
			if (KemValidation.isValidatedAdapterName(adapterName)) {
				AHttpAdapter adapter = (AHttpAdapter) ASTHandler.getAdapterService(adapterName);
				//List<Map<String, String>> configXDK = adapter.getDeviceConfigurations(deviceid);
				   response.setContentType("text/plain");
			        response.setHeader("Content-Disposition", "attachment;filename=wifi.ini");
			        OutputStream os = response.getOutputStream();
			        System.out.println("---------- configXDK : " + configXDKCache + " - deviceid = " + deviceid);
			        
			        Map<String, String> prepareXDK = new HashMap<String, String>();
			        for (Map<String, String> map  : configXDKCache) {
			        	String fieldName = map.get("name");
			        	String fieldValue = map.get("value");
			        	if ("netmode".equals(fieldName)) {
			        		prepareXDK.put("dhcpEnable", (fieldValue.equals("dhcp") ? "1" : "0"));
			        		continue;
			        	}
			        	
			        	if ("ssidname".equals(fieldName)) {
			        		prepareXDK.put("ssidname", fieldValue);
			        		continue;
			        	}
			        	
			        	if ("wifipass".equals(fieldName)) {
			        		prepareXDK.put("wifipass", fieldValue);
			        		continue;
			        	}
			        	
			        	if ("gatewayip".equals(fieldName)) {
			        		prepareXDK.put("gatewayURL", fieldValue);
			        		continue;
			        	}
			        	
			        	if ("gatewayport".equals(fieldName)) {
			        		prepareXDK.put("gatewayPort", fieldValue);
			        		continue;
			        	}
			        	
			        	if ("sendinterval".equals(fieldName)) {
			        		prepareXDK.put("sendinterval", fieldValue);
			        		continue;
			        	}
			        	
			        	if ("staticip".equals(fieldName)) {
			        		String byteIP[] = fieldValue.split("\\.");
			        		if (byteIP.length == 4) {
			        			prepareXDK.put("ipv4.byte3", byteIP[0]);
			        			prepareXDK.put("ipv4.byte2", byteIP[1]);
			        			prepareXDK.put("ipv4.byte1", byteIP[2]);
			        			prepareXDK.put("ipv4.byte0", byteIP[3]);
			        		}
			        		continue;
			        	}
			        	
			        	if ("dns".equals(fieldName)) {
			        		String byteDns[] = fieldValue.split("\\.");
			        		if (byteDns.length == 4) {
			        			prepareXDK.put("ipv4DnsServer.byte3", byteDns[0]);
			        			prepareXDK.put("ipv4DnsServer.byte2", byteDns[1]);
			        			prepareXDK.put("ipv4DnsServer.byte1", byteDns[2]);
			        			prepareXDK.put("ipv4DnsServer.byte0", byteDns[3]);
			        		}
			        		continue;
			        	}
			        	
			        	
			        	if ("gateway".equals(fieldName)) {
			        		String byteGateway[] = fieldValue.split("\\.");
			        		if (byteGateway.length == 4) {
			        			prepareXDK.put("ipv4Gateway.byte3", byteGateway[0]);
			        			prepareXDK.put("ipv4Gateway.byte2", byteGateway[1]);
			        			prepareXDK.put("ipv4Gateway.byte1", byteGateway[2]);
			        			prepareXDK.put("ipv4Gateway.byte0", byteGateway[3]);
			        		}
			        		continue;
			        	}
			        	
			        	if ("subnet".equals(fieldName)) {
			        		String byteSubnet[] = fieldValue.split("\\.");
			        		if (byteSubnet.length == 4) {
			        			prepareXDK.put("ipv4Mask.byte3", byteSubnet[0]);
			        			prepareXDK.put("ipv4Mask.byte2", byteSubnet[1]);
			        			prepareXDK.put("ipv4Mask.byte1", byteSubnet[2]);
			        			prepareXDK.put("ipv4Mask.byte0", byteSubnet[3]);
			        		}
			        		continue;
			        	}
			        	
			        	if ("msgmode".equals(fieldName)) {
			        		if (fieldValue.equals("mqtt")) {
			        			Map<String, String> gwSettings = ASTHandler.getBridgeSettingsBy("socket");
			    				if (gwSettings.containsKey("gateway_id")) {
				        			prepareXDK.put("gwid", gwSettings.get("gateway_id"));
				        			System.out.println("-----------GWID 1 = " + gwSettings.get("gateway_id"));
			    				}
			        		}
			        		continue;
			        	}
			        				        
			        }

			        String timeStamp = String.valueOf(System.currentTimeMillis()); 
			        byte[] tag = ("[wifi]").getBytes();
			        byte[] keyTime = Network.en_XORCrypt("key", "greenkoncepts").getBytes();;
			        byte[] time = Network.en_XORCrypt(timeStamp, "greenkoncepts").getBytes();;
			        os.write(tag);
			        os.write("\n".getBytes());
			        os.write(keyTime);
			        os.write("=".getBytes());
			        os.write(time);
			        os.write("\n".getBytes());
			      
			        for (String key : prepareXDK.keySet()) {
			        	byte[] key_b = Network.en_XORCrypt(key, timeStamp).getBytes();;
			        	byte[] value_b = Network.en_XORCrypt(prepareXDK.get(key), timeStamp).getBytes();;
			        	 os.write(key_b);
					     os.write("=".getBytes());
					     os.write(value_b);
					     os.write("\n".getBytes());
			        }
			        
			        os.write(Network.en_XORCrypt("deviceId", timeStamp).getBytes());
			        os.write("=".getBytes());
		        	os.write(Network.en_XORCrypt(deviceid, timeStamp).getBytes());
		        	os.write("\n".getBytes());
			        os.flush();
			        os.close();
			}
		}else if ("html_page".equals(request.getParameter("action"))) {
		
		} else if ("validation_show".equals(request.getParameter("action"))) {
			final PrintWriter out = response.getWriter();
			response.setContentType("application/json"); //$NON-NLS-1$
			response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
			String address = request.getParameter("device_address");
			String deviceIntanceId = request.getParameter("device_instanceid");
			String adapterName = request.getParameter("adapter_name");
			Adapter adapter = ASTHandler.getAdapterService(adapterName);
			List<Map<String, String>> list = ((ABacnetAdapter) adapter)
					.getValidationRule(deviceIntanceId, address);

			String jsonString = mapper.writeValueAsString(list);
			out.print(jsonString);
			out.flush();
			return;
		} else if ("edit_validation".equals(request.getParameter("action"))) {
			final PrintWriter out = response.getWriter();
			response.setContentType("application/json"); //$NON-NLS-1$
			response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
			String address = request.getParameter("device_address");
			String deviceIntanceId = request.getParameter("device_instanceid");
			String validationId = request.getParameter("validation_id");
			String adapterName = request.getParameter("adapter_name");
			Adapter adapter = ASTHandler.getAdapterService(adapterName);
			Map<String, String> validationRule = ((ABacnetAdapter) adapter)
					.getValidationRuleById(deviceIntanceId, address,
							validationId);

			String jsonString = mapper.writeValueAsString(validationRule);
			out.print(jsonString);
			out.flush();
			return;

		} else if ("delete_validation".equals(request.getParameter("action"))) {
			String validationId = request.getParameter("validation_id");
			String adapterName = request.getParameter("adapter_name");
			Adapter adapter = ASTHandler.getAdapterService(adapterName);
			((ABacnetAdapter) adapter).deleteValidation(validationId);
			return;
		}else if("template".equals(action)){
			response.setContentType("application/html"); //$NON-NLS-1$
			response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
			final PrintWriter out = response.getWriter();
			String adapterName = request.getParameter("adapterName");
			String category = request.getParameter("category");
			String template = "";
			if(adapterName.equals("AquametroAdapter")){
				template = readTemplateFile("/template/devicesettings/aquametro.html");
				out.print(template);
				return ;
			}
			if(adapterName.equals("BacnetAdapter")){
				template = readTemplateFile("/template/devicesettings/bacnet_device_settings.html");
				out.print(template);
				return ;
			}
			if(adapterName.equals("BaylanAdapter")){
				template = readTemplateFile("/template/devicesettings/baylan.html");
				out.print(template);
				return ;
			}
			if(adapterName.equals("BrainchildAdapter")){
				if(category.equals("3000")){
					template = readTemplateFile("/template/devicesettings/brainchild_3000.html");
					out.print(template);
					return ;
				}
				if(category.equals("3001")){
					template = readTemplateFile("/template/devicesettings/brainchild_3001.html");
					out.print(template);
					return ;
				}
				if(category.equals("3002")){
					template = readTemplateFile("/template/devicesettings/brainchild_3002.html");
					out.print(template);
					return ;
				}
				if(category.equals("3003")){
					template = readTemplateFile("/template/devicesettings/brainchild_3003.html");
					out.print(template);
					return ;
				}
			}
			if(adapterName.equals("IneproAdapter")){
				template = readTemplateFile("/template/devicesettings/inepro.html");
				out.print(template);
				return ;
			}
			if(adapterName.equals("BoschAdapter")){
				template = readTemplateFile("/template/devicesettings/bosch_2050.html");
				out.print(template);
				return ;
			}
			if(adapterName.equals("ContrecAdapter")){
				template = readTemplateFile("/template/devicesettings/contrec.html");
				out.print(template);
				return ;
			}
			if(adapterName.equals("DentAdapter")){
				if(category.equals("1003")){
					template = readTemplateFile("/template/devicesettings/dent_1003.html");
					out.print(template);
					return ;
				}
				if(category.equals("1006")){
					template = readTemplateFile("/template/devicesettings/dent_1006.html");
					out.print(template);
					return ;
				}
				
			}
			if(adapterName.equals("EnergetixPowerMeterAdapter")){
				if(category.equals("1002")){
					template = readTemplateFile("/template/devicesettings/energetixpowermeter_1002.html");
					out.print(template);
					return ;
				}
				if(category.equals("1009")){
					template = readTemplateFile("/template/devicesettings/energetixpowermeter_1009.html");
					out.print(template);
					return ;
				}
				if(category.equals("1011")){
					template = readTemplateFile("/template/devicesettings/energetixpowermeter_1011.html");
					out.print(template);
					return ;
				}
				if(category.equals("1012")){
					template = readTemplateFile("/template/devicesettings/energetixpowermeter_1012.html");
					out.print(template);
					return ;
				}
				if(category.equals("1016")){
					template = readTemplateFile("/template/devicesettings/energetixpowermeter_1016.html");
					out.print(template);
					return ;
				}
				
			}
			if(adapterName.equals("EnergetixSensorAdapter")){
				if(category.equals("2000")){
					template = readTemplateFile("/template/devicesettings/energetixsensor_2000.html");
					out.print(template);
					return ;
				}
				if(category.equals("2001")){
					template = readTemplateFile("/template/devicesettings/energetixsensor_2001.html");
					out.print(template);
					return ;
				}
				if(category.equals("2002")){
					template = readTemplateFile("/template/devicesettings/energetixsensor_2002.html");
					out.print(template);
					return ;
				}
				if(category.equals("2003")){
					template = readTemplateFile("/template/devicesettings/energetixsensor_2003.html");
					out.print(template);
					return ;
				}
				if(category.equals("2004")){
					template = readTemplateFile("/template/devicesettings/energetixsensor_2004.html");
					out.print(template);
					return ;
				}
				
			}
			if(adapterName.equals("EntesAdapter")){
				template = readTemplateFile("/template/devicesettings/entes.html");
				out.print(template);
				return ;
			}
			if(adapterName.equals("EpowerAdapter")){
				template = readTemplateFile("/template/devicesettings/epower.html");
				out.print(template);
				return ;
			}
			if(adapterName.equals("GeAquatransAdapter")){
				template = readTemplateFile("/template/devicesettings/geaquatrans.html");
				out.print(template);
				return ;
			}
			if(adapterName.equals("IneproAdapter")){
				template = readTemplateFile("/template/devicesettings/inepro.html");
				out.print(template);
				return ;
			}
			if(adapterName.equals("JanitzaAdapter")){
				template = readTemplateFile("/template/devicesettings/janitza.html");
				out.print(template);
				return ;
			}
			if(adapterName.equals("ModbusConverterAdapter")){
				if(category.equals("7030")){
					template = readTemplateFile("/template/devicesettings/modbusconverter_7030.html");
					out.print(template);
					return ;
				}
				if(category.equals("7031")){
					template = readTemplateFile("/template/devicesettings/modbusconverter_7031.html");
					out.print(template);
					return ;
				}
				if(category.equals("7032")){
					template = readTemplateFile("/template/devicesettings/modbusconverter_7032.html");
					out.print(template);
					return ;
				}
			}
			if(adapterName.equals("PhidgetsAdapter")){
				template = readTemplateFile("/template/devicesettings/phidgets.html");
				out.print(template);
				return ;
			}
			if(adapterName.equals("RielloAdapter")){
				template = readTemplateFile("/template/devicesettings/riello.html");
				out.print(template);
				return ;
			}
			if(adapterName.equals("SchneiderAdapter")){
				if(category.equals("1021")){
					template = readTemplateFile("/template/devicesettings/schneider_1021.html");
					out.print(template);
					return ;
				}
				if(category.equals("1022")){
					template = readTemplateFile("/template/devicesettings/schneider_1022.html");
					out.print(template);
					return ;
				}
			}
			if(adapterName.equals("SitelabAdapter")){
				template = readTemplateFile("/template/devicesettings/sitelab.html");
				out.print(template);
				return ;
			}
			if(adapterName.equals("SocomecAdapter")){
				template = readTemplateFile("/template/devicesettings/socomec.html");
				out.print(template);
				return ;
			}
			if(adapterName.equals("OpcuaAdapter")){
				template = readTemplateFile("/template/devicesettings/opc_ua.html");
				out.print(template);
				return ;
			}
				
			if(adapterName.equals("CircuitControllerAdapter")){
				if(category.equals("2102")){
					template = readTemplateFile("/template/devicesettings/circuitcontroller_2102.html");
					out.print(template);
					return ;
				}
				if(category.equals("2103")){
					template = readTemplateFile("/template/devicesettings/circuitcontroller_2103.html");
					out.print(template);
					return ;
				}
			}
			
			if(adapterName.equals("EmersonAdapter")){
				/*template = readTemplateFile("/template/devicesettings/opc_ua.html");
				out.print(template);*/
				return ;
			}
			if(adapterName.equals("SiemensAdapter")){
				if(category.equals("5003")){
					template = readTemplateFile("/template/devicesettings/siemens_5003.html");
					out.print(template);
					return ;
				}
			}
			return ;
		}
		super.doGet(request, response);
		
	}

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String actionPost = FilenameUtils.getBaseName(request.getPathInfo());
		logger.Log("Do Post:" + actionPost);
		if ("readingconfiguration".equals(actionPost)) {
			String adapterName = request.getParameter("adapterName");
			String addressId = request.getParameter("addressId");
			boolean force = request.getParameter("force").equals("1");
			Map<Integer, String> configTable = ASTHandler
					.getDeviceConfiguration(adapterName, addressId, force);
			System.out.println(configTable);
			try {
				final JSONWriter jw = new JSONWriter(response.getWriter());
				jw.object();
				if (configTable != null) {
					for (Integer key : configTable.keySet()) {
						jw.key(key.toString());
						jw.value(configTable.get(key));
					}
				}
				jw.endObject();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if ("writtingconfiguration".equals(actionPost)) {
			try {
				String adapterName = request.getParameter("adapterName");
				String addressId = request.getParameter("addressId");
				String wroteData = request.getParameter("wroteData");
				ObjectMapper mapper = new ObjectMapper();
				Map<Integer, String> map = mapper.readValue(wroteData,
						new TypeReference<Map<Integer, String>>() {
						});
				if (map == null || map.size() == 0) {
					return;
				}

				List<Integer> registersWritten = new ArrayList<Integer>();
				registersWritten = ASTHandler.writeDeviceCongiguration(
						adapterName, addressId, map);
				Map<String, String> result = new HashMap<String, String>();
				if (registersWritten != null && registersWritten.size() > 0) {
					for (Integer item : map.keySet()) {
						result.put(item.toString(), "1");
					}
				} else {
					for (Integer item : map.keySet()) {
						result.put(item.toString(), "0");
					}
				}

				final PrintWriter out = response.getWriter();
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				String jsonString = mapper.writeValueAsString(result);
				out.print(jsonString);
				out.flush();
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("readingsettings".equals(actionPost)) {
			String adapterName = request.getParameter("adapterName");
			String addressId = request.getParameter("addressId");
			Adapter adapter = ASTHandler.getAdapterService(adapterName);
			List<Map<String, String>> list = adapter.getDeviceAttributes(null, addressId);

			ObjectMapper mapper = new ObjectMapper();
			final PrintWriter out = response.getWriter();
			response.setContentType("application/json"); //$NON-NLS-1$
			response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
			String jsonString = mapper.writeValueAsString(list);
			out.print(jsonString);
			out.flush();
			return;

		}else if ("readingsettingXDK".equals(actionPost)) {
			String adapterName = request.getParameter("adapterName");
			String addressId = request.getParameter("addressId");
			Adapter adapter = ASTHandler.getAdapterService(adapterName);
			List<Map<String, String>> list = ((AHttpAdapter) adapter).getDeviceConfigurations(addressId);

			if (list.isEmpty()) {
				Map<String, String>  item = new HashMap<String, String>();
				item.put("name", "ssidname");
				item.put("value", "");
				item.put("data_point", "");
				list.add(item);
				
				item = new HashMap<String, String>();
				item.put("name", "wifipass");
				item.put("value", "");
				item.put("data_point", "");
				list.add(item);
				
				item = new HashMap<String, String>();
				item.put("name", "netmode");
				item.put("value", "dhcp");
				item.put("data_point", "");
				list.add(item);
				
				item = new HashMap<String, String>();
				item.put("name", "staticip");
				item.put("value", "0.0.0.0");
				item.put("data_point", "");
				list.add(item);
				
				item = new HashMap<String, String>();
				item.put("name", "subnet");
				item.put("value", Util.findSubnet());
				item.put("data_point", "");
				list.add(item);
				
				item = new HashMap<String, String>();
				item.put("name", "gateway");
				item.put("value", Util.findGateway());
				item.put("data_point", "");
				list.add(item);
				
				item = new HashMap<String, String>();
				item.put("name", "dns");
				item.put("value", "8.8.8.8");
				item.put("data_point", "");
				list.add(item);
				
				item = new HashMap<String, String>();
				item.put("name", "msgmode");
				item.put("value", "mqtt");
				item.put("data_point", "");
				list.add(item);
				
				item = new HashMap<String, String>();
				item.put("name", "gatewayip");
				item.put("value", Util.findLocalIP());
				item.put("data_point", "");
				list.add(item);
				
				item = new HashMap<String, String>();
				item.put("name", "gatewayport");
				item.put("value", "80");
				item.put("data_point", "");
				list.add(item);
				
				item = new HashMap<String, String>();
				item.put("name", "sendinterval");
				item.put("value", "60");
				item.put("data_point", "");
				list.add(item);
			}
			
			ObjectMapper mapper = new ObjectMapper();
			final PrintWriter out = response.getWriter();
			response.setContentType("application/json"); //$NON-NLS-1$
			response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
			String jsonString = mapper.writeValueAsString(list);
			out.print(jsonString);
			out.flush();
			return;

		} else if ("savesettings".equals(actionPost)) {
			String adapterName = request.getParameter("adapterName");
			String addressId = request.getParameter("addressId");
			String data = request.getParameter("data");

			ObjectMapper mapper = new ObjectMapper();
			List<Map<String, String>> list = mapper.readValue(data, List.class);
			Adapter adapter = ASTHandler.getAdapterService(adapterName);
		
			if ((list.size()> 0) &&(list.get(0).get("data_point") == null || list.get(0).get("data_point").equals("")) && !adapterName.equals("ModbusConverterAdapter") ) {
				adapter.insertDeviceAttributes(null, addressId, list);
			} else {

				adapter.updateDeviceAttributes(null, addressId, list);
			}
			
			if (adapterName.equals("BoschAdapter")) {
				ASTHandler.restartBundle(adapterName);
			}
			
			return;
		} else if ("saveConfigXDK".equals(actionPost)) {
			String adapterName = request.getParameter("adapterName");
			String addressId = request.getParameter("addressId");
			String data = request.getParameter("data");

			ObjectMapper mapper = new ObjectMapper();
			List<Map<String, String>> list = mapper.readValue(data, List.class);
			Adapter adapter = ASTHandler.getAdapterService(adapterName);
		
			if ((list.size()> 0) &&(list.get(1).get("data_point") == null || list.get(1).get("data_point").equals("")) ) {
				((AHttpAdapter) adapter).insertDeviceConfigurations(addressId, list);
			} else {

				((AHttpAdapter) adapter).updateDeviceConfigurations(addressId, list);
			}

			return;
		} else if ("writeChannel".equals(actionPost)) {
			String update = request.getParameter("update");
			String adapterName = request.getParameter("adapterName");
			String deviceInstanceId = request.getParameter("deviceInstance");
			String address = request.getParameter("address");
			Adapter adapter = ASTHandler.getAdapterService(adapterName);
			String delete = request.getParameter("delete");

			ObjectMapper objectMapper = new ObjectMapper();
			if (delete != null && !"[]".equals(delete)) {
				List<String> deletesDataPoints = objectMapper.readValue(delete,
						List.class);
				((ABacnetAdapter) adapter).deleteDeviceAttribute(address,
						deviceInstanceId, deletesDataPoints);
			}

			List<Map<String, String>> attributes = objectMapper.readValue(
					update, List.class);
			for (Map<String, String> item : attributes) {
				if (item.get("data_point") != null) {
					
					((ABacnetAdapter) adapter).updateDeviceAttributes(address,
							deviceInstanceId, attributes);
				
					return;
				}
				boolean unique = ((ABacnetAdapter) adapter).checkUniqueObjectIdentifier(
						attributes.get(0).get("object_identifier"),deviceInstanceId, address);
				if(unique == true){
					Map<String,Object> result = new HashMap<String, Object>();
					List<String> list = new ArrayList<String>();
					list.add("Duplicate Object Identifier");
					result.put("errors", list);
					ObjectMapper mapper = new ObjectMapper();
					final PrintWriter out = response.getWriter();
					response.setContentType("application/json"); //$NON-NLS-1$
					response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
					String jsonString = mapper.writeValueAsString(result);
					out.print(jsonString);
					out.flush();
					return ;
				}
				((ABacnetAdapter) adapter).insertDeviceAttributes(address,
						deviceInstanceId, attributes);
				return;
			}
		} else if ("writeScanning".equals(actionPost)){
			String update = request.getParameter("update");
			String adapterName = request.getParameter("adapterName");
			String deviceInstanceId = request.getParameter("deviceInstance");
			String address = request.getParameter("address");
			Adapter adapter = ASTHandler.getAdapterService(adapterName);
			String delete = request.getParameter("delete");

			ObjectMapper objectMapper = new ObjectMapper();
			if (delete != null && !"[]".equals(delete)) {
				List<String> deletesDataPoints = objectMapper.readValue(delete,
						List.class);
				((ABacnetAdapter) adapter).deleteDeviceAttribute(address,
						deviceInstanceId, deletesDataPoints);
			}

			List<Map<String, String>> attributes = objectMapper.readValue(
					update, List.class);
			for (Map<String, String> item : attributes) {
				if (item.get("data_point") != null) {
					
					((ABacnetAdapter) adapter).updateDeviceAttributesScanning(address,
							deviceInstanceId, attributes);
				
					return;
				}
				((ABacnetAdapter) adapter).insertDeviceAttributes(address,
						deviceInstanceId, attributes);
				return;
			}
		}
		else if ("writeConfiguration".equals(actionPost)){
			String update = request.getParameter("update");
			String adapterName = request.getParameter("adapterName");
			String deviceInstanceId = request.getParameter("deviceInstance");
			String address = request.getParameter("address");
			Adapter adapter = ASTHandler.getAdapterService(adapterName);
			String delete = request.getParameter("delete");

			ObjectMapper objectMapper = new ObjectMapper();
			if (delete != null && !"[]".equals(delete)) {
				List<String> deletesDataPoints = objectMapper.readValue(delete,
						List.class);
				((ABacnetAdapter) adapter).deleteDeviceAttribute(address,
						deviceInstanceId, deletesDataPoints);
			}

			List<Map<String, String>> attributes = objectMapper.readValue(
					update, List.class);
			for (Map<String, String> item : attributes) {
				if (item.get("data_point") != null) {
					
					((ABacnetAdapter) adapter).updateDeviceAttributesConfiguration(address,
							deviceInstanceId, attributes);
				
					return;
				}
				((ABacnetAdapter) adapter).insertDeviceAttributes(address,
						deviceInstanceId, attributes);
				return;
			}
		}
		
		else if ("validation".equals(actionPost)) {
			String address = request.getParameter("device_address");
			String expressions = request.getParameter("expressions");
			String actionResult = request.getParameter("action_result");
			String adapterName = request.getParameter("adapter_name");
			String dataPoint = request.getParameter("data_point");
			String deviceIntanceId = request.getParameter("device_instanceid");
			String validationId = request.getParameter("validation_id");
			Adapter adapter = ASTHandler.getAdapterService(adapterName);
			if (!"".equals(validationId)) {
				((ABacnetAdapter) adapter).updateValidationRule(expressions,
						actionResult, validationId);
				return;
			}
			((ABacnetAdapter) adapter).insertValidationRule(deviceIntanceId,
					address, expressions, actionResult, dataPoint);

		} else if("importingXDK".equals(actionPost)){
			InputStream fileContent = null;
			System.out.println("-------------------------------------------importingXDK");
			try {
				List<FileItem> items = new ServletFileUpload(
						new DiskFileItemFactory()).parseRequest(request);
				if (items != null && items.size() > 0) {
					String fileName = FilenameUtils.getName(items.get(0).getName());
					if(fileName.endsWith("ini")){
						Map<String, String> importingData = new Hashtable<String, String>();
						fileContent = items.get(0).getInputStream();
						 InputStreamReader ipsr = new InputStreamReader(fileContent);
						BufferedReader br = new BufferedReader(ipsr);
						String line;
						
						int idx = 0;
						String key = "";
						String name = "";
						String value = "";
			            while ((line = br.readLine()) != null){
			            	String[] str = line.split("=");
			            	if(idx == 0) {
			            		System.out.println("===import starts with tag = " + line);
			            	} else if (idx == 1) {
			            		key=new String(Network.de_XORCrypt(str[1], "greenkoncepts"));	
			            		System.out.println("===import starts with key = " + key);
			            	} else {
			            		name=new String(Network.de_XORCrypt(str[0],key));	
			            		value=new String(Network.de_XORCrypt(str[1],key));	
			            		importingData.put(name, value);
			            	}
			               idx++;
			            }
						System.out.println("---------importingData = " + importingData);
						List<Map<String, String>> dbList = new ArrayList<Map<String,String>>();
						Map<String, String> temp;
						String ssid = (importingData.containsKey("ssidname") ? importingData.get("ssidname") : "") ;
						temp = new Hashtable<String, String>();
						temp.put("name", "ssidname");
						temp.put("value", ssid);
						dbList.add(temp);
						
						String pass = (importingData.containsKey("wifipass") ? importingData.get("wifipass") : "") ;
						temp = new Hashtable<String, String>();
						temp.put("name", "wifipass");
						temp.put("value", pass);
						dbList.add(temp);
						
						String netmode = (importingData.containsKey("dhcpEnable") ? (importingData.get("dhcpEnable").equals("1") ? "dhcp" : "static") : "") ;
						temp = new Hashtable<String, String>();
						temp.put("name", "netmode");
						temp.put("value", netmode);
						dbList.add(temp);
						
						String staticip = (importingData.containsKey("ipv4.byte3") ? importingData.get("ipv4.byte3") : "0") + "." +
											(importingData.containsKey("ipv4.byte2") ? importingData.get("ipv4.byte2") : "0") + "." +
											(importingData.containsKey("ipv4.byte1") ? importingData.get("ipv4.byte1") : "0") + "." +
											(importingData.containsKey("ipv4.byte0") ? importingData.get("ipv4.byte0") : "0") ;
						temp = new Hashtable<String, String>();
						temp.put("name", "staticip");
						temp.put("value", staticip);
						dbList.add(temp);
						
						String subnet = (importingData.containsKey("ipv4Mask.byte3") ? importingData.get("ipv4Mask.byte3") : "0") + "." +
								(importingData.containsKey("ipv4Mask.byte2") ? importingData.get("ipv4Mask.byte2") : "0") + "." +
								(importingData.containsKey("ipv4Mask.byte1") ? importingData.get("ipv4Mask.byte1") : "0") + "." +
								(importingData.containsKey("ipv4Mask.byte0") ? importingData.get("ipv4Mask.byte0") : "0") ;
						temp = new Hashtable<String, String>();
						temp.put("name", "subnet");
						temp.put("value", subnet);
						dbList.add(temp);
						
						String gateway = (importingData.containsKey("ipv4Gateway.byte3") ? importingData.get("ipv4Gateway.byte3") : "0") + "." +
										(importingData.containsKey("ipv4Gateway.byte2") ? importingData.get("ipv4Gateway.byte2") : "0") + "." +
										(importingData.containsKey("ipv4Gateway.byte1") ? importingData.get("ipv4Gateway.byte1") : "0") + "." +
										(importingData.containsKey("ipv4Gateway.byte0") ? importingData.get("ipv4Gateway.byte0") : "0") ;
						temp = new Hashtable<String, String>();
						temp.put("name", "gateway");
						temp.put("value", gateway);
						dbList.add(temp);
						
						String dns = (importingData.containsKey("ipv4DnsServer.byte3") ? importingData.get("ipv4DnsServer.byte3") : "0") + "." +
								(importingData.containsKey("ipv4DnsServer.byte2") ? importingData.get("ipv4DnsServer.byte2") : "0") + "." +
								(importingData.containsKey("ipv4DnsServer.byte1") ? importingData.get("ipv4DnsServer.byte1") : "0") + "." +
								(importingData.containsKey("ipv4DnsServer.byte0") ? importingData.get("ipv4DnsServer.byte0") : "0") ;
						temp = new Hashtable<String, String>();
						temp.put("name", "dns");
						temp.put("value", dns);
						dbList.add(temp);
				
						String msgmode = (importingData.containsKey("gwid") ? "mqtt" : "http");
						temp = new Hashtable<String, String>();
						temp.put("name", "msgmode");
						temp.put("value", msgmode);
						dbList.add(temp);
						
	        			System.out.println("-----------GWID = " + msgmode);

						String gatewayip = (importingData.containsKey("gatewayURL") ? importingData.get("gatewayURL") : "0.0.0.0") ;
						temp = new Hashtable<String, String>();
						temp.put("name", "gatewayip");
						temp.put("value", gatewayip);
						dbList.add(temp);
						
						String gatewayport = (importingData.containsKey("gatewayPort") ? importingData.get("gatewayPort") : "") ;
						temp = new Hashtable<String, String>();
						temp.put("name", "gatewayport");
						temp.put("value", gatewayport);
						dbList.add(temp);
						
						String sendinterval = (importingData.containsKey("sendinterval") ? importingData.get("sendinterval") : "60") ;
						temp = new Hashtable<String, String>();
						temp.put("name", "sendinterval");
						temp.put("value", sendinterval);
						dbList.add(temp);
						
						Adapter adapter = ASTHandler.getAdapterService("BoschAdapter");
						if (adapter != null) {
							((AHttpAdapter) adapter).importDeviceData(importingData.get("deviceId"), dbList);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (fileContent != null) {
					fileContent.close();
				}
			}
			response.sendRedirect("/system/console/devicesettings?");
		} else if("exportingXDK".equals(actionPost)){
			String data = request.getParameter("data");

			ObjectMapper mapper = new ObjectMapper();
			List<Map<String, String>> list = mapper.readValue(data, List.class);
			configXDKCache = list;
		}
	}

	@Override
	protected void renderContent(HttpServletRequest arg0,
			HttpServletResponse response) throws ServletException, IOException {
		String template = readTemplateFile("/template/devicesettings/device_settings_entry.html");
		response.getWriter().print(template);
	}

}
