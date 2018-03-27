package gk.web.console.plugin.kem.adapter.settings;

import gk.web.console.plugin.kem.KemObjectsServiceTracker;
import gk.web.console.plugin.kem.KemValidation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.felix.webconsole.SimpleWebConsolePlugin;
import org.json.JSONException;
import org.json.JSONWriter;
import org.osgi.framework.BundleContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.greenkoncepts.gateway.api.adapter.ABacnetAdapter;
import com.greenkoncepts.gateway.api.adapter.Adapter;
import com.greenkoncepts.gateway.util.UtilLog;

public class AdapterSettingsServlet extends SimpleWebConsolePlugin {
	private static final long serialVersionUID = 1L;
	private static final String CATEGORY = "KEM"; //$NON-NLS-1$
	private static final String TITLE = "Adapter Settings"; //$NON-NLS-1$
	private static final String ADAPTERSETTINGS = "adaptersettings";
	// private static final String CSS[] = { "/" + LABEL + "/" + RESOURCE_DIR +
	// "/style.css"};
	private final static String LABEL = ADAPTERSETTINGS;

	private final String ADAPTER_TEMPLATE = "/template/adaptersettings/adapter_settings_entry.html";
	private final String MODBUS_TEMPLATE = "/template/adaptersettings/modbus.html";
	private final String BACNET_TEMPLATE = "/template/adaptersettings/bacnet.html";
	private final String OPC_TEMPLATE = "/template/adaptersettings/opcua.html";
	private final String DUMMY_TEMPLATE = "/template/adaptersettings/dummy.html";
	private static final String CSS[] = { "/" + ADAPTERSETTINGS + "/template/adaptersettings/style.css" }; // yes, it's correct! //$NON-NLS-1$
	private KemObjectsServiceTracker ASTHandler;
	private String curAdapter;
	private UtilLog logger = UtilLog.getInstance(AdapterSettingsServlet.class.getSimpleName());

	public AdapterSettingsServlet(KemObjectsServiceTracker ASTHandler_) {
		super(LABEL, TITLE, CATEGORY, CSS);
		ASTHandler = ASTHandler_;
		curAdapter = null;
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
		log("WebConsolePlugin stops");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String id = request.getParameter("id");
		if (id != null) {
			String returnedData = "";
			final PrintWriter out = response.getWriter();
			if (id.equals("adapters")) {
				logger.Log("Do Get:Adapters");
				response.setContentType("application/json"); //$NON-NLS-1$
				response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
				returnedData = parserToJSONData(out, "getAdapters", request);
			} else if (id.equals("settings")) {
				logger.Log("Do Get:Settings");
				response.setContentType("application/json"); //$NON-NLS-1$
				response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
				returnedData = parserToJSONData(out, "getAdapterSettings", request);
			} else if (id.equals("scanning")) {
				logger.Log("Do Get:Scanning");
				response.setContentType("application/json"); //$NON-NLS-1$
				response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
				returnedData = parserToJSONData(out, "getScannedDeviceList", request);
			}
			out.print(returnedData);
			out.flush();
			return;
		}
		
		String pageAdapter = request.getParameter("html");
		System.out.println("----------------------pageAdapter = " + pageAdapter);

		if (pageAdapter != null) {
			final PrintWriter out = response.getWriter();
			Adapter adapter = ASTHandler.getAdapterService(pageAdapter);
			if (adapter != null) {
				if (adapter.getAdapterType() == Adapter.MODBUS_TYPE) {
					out.print(readTemplateFile(MODBUS_TEMPLATE));
				} else if (adapter.getAdapterType() == Adapter.BACNET_TYPE) {
					out.print(readTemplateFile(BACNET_TEMPLATE));
				} else if (adapter.getAdapterType() == Adapter.OPCUA_TYPE) {
					out.print(readTemplateFile(OPC_TEMPLATE));
				} else {
					out.print(readTemplateFile(DUMMY_TEMPLATE));
				}
			}
			out.flush();
			return;

		}
		else if ("exporting".equals(request.getParameter("action"))) {

			String adapterName = request.getParameter("adapter");
			if (KemValidation.isValidatedAdapterName(adapterName)) {
				Adapter adapter = ASTHandler.getAdapterService(adapterName);
				List<Object> data = adapter.getExportData();
				Map<String, String> adapterSettings = (Map<String, String>) data.get(0);
				List<Map<String, String>> deviceList = (List<Map<String, String>>) data.get(1);
				List<Map<String, String>> deviceAttributes = (List<Map<String, String>>) data.get(2);
				response.setContentType("text/csv");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + adapterName + " .json\"");

				try
				{
					OutputStream outputStream = response.getOutputStream();
					ObjectMapper mapper = new ObjectMapper();
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("adapter", adapterSettings);
					map.put("deviceList", deviceList);
					map.put("deviceAttribute", deviceAttributes);

					mapper.enable(SerializationFeature.INDENT_OUTPUT);
					String result = mapper.writeValueAsString(map);
					outputStream.write(result.getBytes());
					outputStream.flush();
					outputStream.close();
				} catch (Exception e)
				{
					System.out.println(e.toString());
				}
			}

		}

		// For the first time
		String baseName = FilenameUtils.getBaseName(request.getPathInfo());
		System.out.println("----------------------baseName = " + baseName);
		if ((baseName.equalsIgnoreCase(ADAPTERSETTINGS)) && ((curAdapter =request.getParameter("adaptername")) == null)) {
			ArrayList<Adapter> adapterServices = ASTHandler.getAdapterServices();
			if (adapterServices.size() > 0) {
				curAdapter = adapterServices.get(0).getClass().getSimpleName();
			} else {
				curAdapter = "NULL";
			}
			response.sendRedirect("/system/console/" + ADAPTERSETTINGS + "?adaptername=" + curAdapter);
			return;
		}

		super.doGet(request, response);
	}

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String actionPost = FilenameUtils.getBaseName(request.getPathInfo());
		logger.Log("Do Post:" + actionPost);
		try {
			if ("update_adapter_settings".equals(actionPost)) {
				String adapter = request.getParameter("adapter");
				String general = request.getParameter("data");
				String jsonResult = "";
				if (adapter != null) {
					Adapter adapterObject = ASTHandler.getAdapterService(adapter);
					String result = "fail";
					List<String> errorsList = new ArrayList<String>();
					ObjectMapper objectMapper = new ObjectMapper();
					List<Map<String, Object>> dataList = objectMapper.readValue(general, List.class);
					System.out.println("===> update object list = " + dataList);
					if (adapterObject.updateDataObject(dataList)) {
						try {
							//ASTHandler.restartBundle(curAdapter);
						} catch (Exception e) {
							e.printStackTrace();
						}
						result = "success";
					}
					Map<String, Object> reValue = new HashMap<String, Object>();
					reValue.put("result", result);
					reValue.put("errors", errorsList);
					jsonResult = objectMapper.writeValueAsString(reValue);
				}
				response.getWriter().print(jsonResult);
				response.getWriter().flush();
			} else if ("bundle".equals(actionPost)) {
				String action = request.getParameter("action");
				try {
					if ("adding".equals(action)) {
						String bundleName = request.getParameter("bundle");
						if (KemValidation.isValidatedBundleName(bundleName)) {
							Process proc = Runtime.getRuntime().exec("sudo script/bundles.sh -add " + bundleName);
							proc.waitFor();
							final JSONWriter jw = new JSONWriter(response.getWriter());
							jw.object();
							jw.key("exitcode");
							jw.value(proc.exitValue());

							jw.key("errors");
							jw.array();
							String s = null;
							BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
							while ((s = stdError.readLine()) != null) {
								String[] errorStringArray = s.split(":");
								jw.object();
								jw.key("string");
								jw.value(errorStringArray[errorStringArray.length - 1]);
								jw.endObject();
							}
							jw.endArray();
							jw.endObject();
						}
					} else if ("deleting".equals(action)) {
						String adapterName = request.getParameter("adapter");
						if (KemValidation.isValidatedAdapterName(adapterName)) {
							String configFile = adapterName + ".prop";
							String bundleFile = ASTHandler.getFullBundleName(adapterName) + ".jar";
							if (KemValidation.isValidatedBundleName(bundleFile)) {
								ASTHandler.uninstallBundle(adapterName);
								Process proc = Runtime.getRuntime().exec(
										"sudo script/bundles.sh -delete " + bundleFile + " " + configFile);
								proc.waitFor();
							}
						}
					} else if ("restarting".equals(action)) {
						String adapterName = request.getParameter("adapter");
						try {
							ASTHandler.restartBundle(adapterName);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if ("save_devices_discovery".equals(actionPost)) {
				ObjectMapper objectMapper = new ObjectMapper();

				String adapter_name = request.getParameter("adapter");
				String device_list = request.getParameter("device_list");
				String ok = "success";
				if (adapter_name != null) {
					Adapter adapter = ASTHandler.getAdapterService(adapter_name);
					if (device_list != null) {
						List<Map<String, String>> deviceObjectList = objectMapper.readValue(device_list, List.class);
						if (!adapter.insertDeviceList(deviceObjectList)) {
							ok = "fail";
						}
					}
				}
				Map<String, Object> reValue = new HashMap<String, Object>();
				reValue.put("result", ok);
				reValue.put("errors", new ArrayList<String>());
				response.getWriter().print(objectMapper.writeValueAsString(reValue));
				response.getWriter().flush();
			} else if ("importing".equals(actionPost)) {
				InputStream fileContent = null;
				String adapterName = "";
				try {
					List<FileItem> items = new ServletFileUpload(
							new DiskFileItemFactory()).parseRequest(request);
					if (items != null && items.size() > 0) {
						String fileName = FilenameUtils.getName(items.get(0).getName());
						if(fileName.contains("json")){
							fileContent = items.get(0).getInputStream();
							ObjectMapper mapper = new ObjectMapper();
							Map<String, Object> jsonMap = mapper.readValue(fileContent, Map.class);
							Map<String, String> adapterSettings = (Map<String, String>) jsonMap.get("adapter");
							List<Map<String, String>> deviceList = (List<Map<String, String>>) jsonMap.get("deviceList");
							List<Map<String, String>> deviceAttribute = (List<Map<String, String>>) jsonMap.get("deviceAttribute");
							adapterName = adapterSettings.get("adapter_name"); /* adapterSettings.get("adapter_name"); */
							importDatabase(adapterName, adapterSettings, deviceList,
									deviceAttribute);
						}else if(fileName.contains("prop")){
							String oldFileName[] = fileName.split("_");
							adapterName = oldFileName[0].substring(0, oldFileName[0].indexOf("."));
							fileContent = items.get(0).getInputStream();
							Map<String, String> adapterSettings = new HashMap<String, String>();
							Properties prop = new Properties();
							prop.load(fileContent);
							String port = prop.getProperty("serial_port");
							if(null == port || port == ""){
								port = prop.getProperty("port");
							}
							
							adapterSettings.put("port", port );
							adapterSettings.put("query_timeout", prop.getProperty("query_timeout"));
							adapterSettings.put("protocol", "");
							adapterSettings.put("serial_port", prop.getProperty("serial_port"));
							adapterSettings.put("baudrate", prop.getProperty("serial_baudrate"));
							adapterSettings.put("stop_bit", prop.getProperty("serial_stopbit") == null ? "" : prop.getProperty("serial_stopbit"));
							adapterSettings.put("parity", prop.getProperty("serial_parity") == null ? "" : prop.getProperty("serial_parity"));
							adapterSettings.put("address", prop.getProperty(""));
							
							List<Map<String,String>> deviceAttribute = new ArrayList<Map<String,String>>();
							
						
							
							Integer deviceNumber = Integer.valueOf(prop.getProperty("device_num"));
							List<Map<String, String>> deviceList = new ArrayList<Map<String,String>>(Integer.valueOf(deviceNumber));
							for(int i = 0 ; i< deviceNumber ;i++){
								Map<String,String> map = new HashMap<String, String>();
								String address = prop.getProperty("device_"+i+"_address");
								if(address.contains(".")){
									map.put("device_address", prop.getProperty("device_"+i+"_address"));
									map.put("device_instanceid", prop.getProperty("device_"+i+"_id"));
								}else{
									map.put("device_address", "");
									map.put("device_instanceid", address);
								}
								map.put("device_name", "");
								map.put("device_category", prop.getProperty("device_"+i+"_category"));
								map.put("device_version",prop.getProperty("device_"+i+"_version"));
								map.put("device_network_number","0");
								map.put("device_network_address","-1");
								map.put("device_alternativeid", prop.getProperty("device_"+i+"_delegateid"));
								map.put("device_id", String.valueOf(i));
								
								deviceList.add(map);
								
								String patter = "^device_" + i + "_\\d*_\\d*";
								
								for(String key : prop.stringPropertyNames()){
									if (Pattern.matches(patter, key)) {
										Map<String,String> map2 = new HashMap<String, String>();
										String allAttribute = prop.getProperty(key);
										String attribute[] = allAttribute.split(":");
										if(adapterName.contains("Bacnet")){
											if( attribute[0].contains("vir")){
												map2.put("formula", attribute[1]);
												map2.put("measure_name", attribute[2]);
												map2.put("measure_unit", attribute[3]);
												map2.put("measure_ratio", "1");
											}else{
												map2.put("object_identifier", attribute[0]);
												map2.put("measure_name", attribute[1]);
												map2.put("measure_unit", attribute[2]);
												map2.put("type", "data");
												map2.put("measure_ratio", "1");
											}
											
											String channel = key.substring(findNthIndexOf(key, "_", 2)+1,findNthIndexOf(key, "_", 3));
											map2.put("channel", channel);
											map2.put("device_id", String.valueOf(i));
											
											if(attribute.length == 7){
												map2.put("compsumtion", "true");
											}
											
										}
										if(adapterName.contains("Brainchild")){
											map2.put("name", attribute[0]);
											map2.put("ratio", attribute[1]);
											map2.put("unit", attribute[2]);
											String deviceId = key.substring(findNthIndexOf(key, "_", 1)+1,findNthIndexOf(key, "_", 2));
											if(deviceId.equals(String.valueOf(i))){
												map2.put("device_id", deviceId);
											}
										}
										
										deviceAttribute.add(map2);
									}
								}
								Collections.sort(deviceAttribute, new Comparator <Map<String,String>> () {
									public int compare(Map<String,String> o1, Map<String,String> o2) {
											String chan1 = o1.get("channel");
											String chan2 = o2.get("channel");
											if ((chan1 != null) && (chan2 != null)) {
												if (Integer.parseInt(chan1) == Integer.parseInt(chan2)) {
													return 0;
												} else if (Integer.parseInt(chan1) < Integer.parseInt(chan2)) {
													return -1;
												} else {
													return 1;
												}
											}
								           return 0;
								        }
								});
							
								importDatabase(adapterName, adapterSettings, deviceList, deviceAttribute);
							}
							
							
							
						}

						if (KemValidation.isValidatedAdapterName(adapterName)) {
							response.sendRedirect("/system/console/adaptersettings?adaptername=" + adapterName);
						}

					}

				} catch (FileUploadException e) {
					e.printStackTrace();
				} finally {
					if (fileContent != null) {
						fileContent.close();
					}
				}
			} else if ("upload_file".equals(actionPost)) {
				try {
					List<FileItem> items = new ServletFileUpload(
							new DiskFileItemFactory()).parseRequest(request);
					if (items != null && items.size() > 0) {
						String fileName = FilenameUtils.getName(items.get(0).getName());
						String oldFileName[] = fileName.split("_");
						InputStream fileContent = null;
						OutputStream outputStream = null;
						try {
							fileContent = items.get(0).getInputStream();
							File file = new File("/home/gkadmin/apps/current/repos/plugins/" + fileName);
							File dir = new File("/home/gkadmin/apps/current/repos/plugins/");
							for (File f : dir.listFiles()) {
								if (f.getName().startsWith(oldFileName[0])) {
									f.delete();
									logger.Log("remove Adapter " + f.getName() + " out of repos");
									break;
								}
							}
							outputStream = new FileOutputStream(file);

							int read = 0;
							byte[] bytes = new byte[1024];

							while ((read = fileContent.read(bytes)) != -1) {
								outputStream.write(bytes, 0, read);
							}
						} catch (Exception e) {
							logger.Log("upload_file error ! error ! error !");
						} finally {
							if (fileContent != null) {
								fileContent.close();
							}
							
							if (outputStream != null){
								outputStream.close();
							}
						}
						response.sendRedirect("/system/console/adaptersettings");
					}

				} catch (FileUploadException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void importDatabase(String adapterName, Map<String, String> adapterSettings,
			List<Map<String, String>> deviceList,
			List<Map<String, String>> deviceAttribute) {
		Adapter adapter = ASTHandler.getAdapterService(adapterName);
		adapter.importData(adapterSettings, deviceList, deviceAttribute);

		
		if ("BacnetAdapter".equals(adapterName)) {
			for (Map<String, String> map : deviceList) {
				String deviceId = map.get("device_id");
				List<Map<String, String>> temp = new ArrayList<Map<String, String>>();
				for (Map<String, String> item : deviceAttribute) {
					if (item.get("device_id").equals(deviceId)) {
						temp.add(item);
					}
				}
				String address = map.get("device_address");
				String deviceInstanceId = map.get("device_instanceid");
				adapter.insertDeviceAttributes(address, deviceInstanceId, temp);
			}
		}
	}

	@Override
	protected void renderContent(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().print(readTemplateFile(ADAPTER_TEMPLATE));
	}


	private String parserToJSONData(final PrintWriter pw, String action, HttpServletRequest request) throws IOException {
		try {
			ArrayList<Adapter> adapterServices = ASTHandler.getAdapterServices();
			ObjectMapper mapper = new ObjectMapper();
			if (action.equals("getAdapters")) {
				Map<String, Object> adaptersData = new HashMap<String, Object>();
				adaptersData.put("size", String.valueOf(adapterServices.size()));
				adaptersData.put("curAdapter", curAdapter);

				Map<String, String> adapters = new LinkedHashMap<String, String>();
				for (int i = 0; i < adapterServices.size(); i++) {
					String name = adapterServices.get(i).getClass().getSimpleName();
					String version = ASTHandler.getAdapterVersion(name);
					adapters.put(name, version);
				}
				adaptersData.put("adapters", adapters);

				Map<String, String> bundles = new HashMap<String, String>();
				File repos = new File("/home/gkadmin/apps/current/repos/plugins");
				File[] bundleList = repos.listFiles();
				if (bundleList != null) {
					for (int j = 0; j < bundleList.length; j++) {
						if (bundleList[j].isFile()) {
							String[] bundleName = bundleList[j].getName().split("_");
							if (bundleName[0].startsWith("com.greenkoncepts.gateway.adapter.")) {
								String[] splitedBundleName = bundleName[0].split("\\.");
								// bundles.put(bundleList[j].getName(), splitedBundleName[splitedBundleName.length - 1]);
								bundles.put(splitedBundleName[splitedBundleName.length - 1], bundleList[j].getName());
							}
						}
					}
				}
				adaptersData.put("bundles", bundles);
				return mapper.writeValueAsString(adaptersData);

			} else if (action.equals("getAdapterSettings")) {
				String adapterName = request.getParameter("adapter");
				Adapter adapter = ASTHandler.getAdapterService(adapterName);
				Map<String, Object> adapterSettings = new HashMap<String, Object>();
				adapterSettings.put("settings", adapter.getAdapterSettings());
				adapterSettings.put("devices", adapter.getDeviceList());
				return mapper.writeValueAsString(adapterSettings);
			} else if (action.equals("getScannedDeviceList")) {
				Adapter adapter = ASTHandler.getAdapterService(curAdapter);
				if ((adapter.getAdapterType() == Adapter.BACNET_TYPE) || (adapter.getAdapterType() == Adapter.OPCUA_TYPE)) {
					String scanningStatus = request.getParameter("scanning");
					boolean scanning = ("1".equals(scanningStatus)) ? true : false;
					ABacnetAdapter bacnet = (ABacnetAdapter) adapter;
					List<Map<String, String>> deviceList = bacnet.scanDevice(scanning);
					return mapper.writeValueAsString(deviceList);
				}
			}
		} catch (Exception je) {
		}
		return "";
	}

	public static int findNthIndexOf(String str, String needle, int occurence)
			throws IndexOutOfBoundsException {
		int index = -1;
		Pattern p = Pattern.compile(needle, Pattern.MULTILINE);
		Matcher m = p.matcher(str);
		while (m.find()) {
			if (--occurence == 0) {
				index = m.start();
				break;
			}
		}
		if (index < 0)
			throw new IndexOutOfBoundsException();
		return index;
	}

}
