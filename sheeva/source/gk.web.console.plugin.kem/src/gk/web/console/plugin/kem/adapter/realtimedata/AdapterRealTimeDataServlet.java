package gk.web.console.plugin.kem.adapter.realtimedata;

import gk.web.console.plugin.kem.KemObjectsServiceTracker;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.webconsole.SimpleWebConsolePlugin;
import org.osgi.framework.BundleContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenkoncepts.gateway.api.adapter.ABacnetAdapter;
import com.greenkoncepts.gateway.api.adapter.AModbusAdapter;
import com.greenkoncepts.gateway.api.adapter.Adapter;

public class AdapterRealTimeDataServlet extends SimpleWebConsolePlugin {

	private static final long serialVersionUID = -8796894563844834716L;

	private static final String CATEGORY = "KEM";// Change Category
	private static final String LABEL = "realtimedata"; //$NON-NLS-1$
	private static final String TITLE = "Real Time Data"; //$NON-NLS-1$
	private static final String CSS[] = {}; // yes, it's correct! //$NON-NLS-1$

	private final String REALTIMEDATA_TEMPLATE = "/template/realtimedata/real_time_data_entry.html";// External header html file
	private boolean debug = true;

	private KemObjectsServiceTracker ASTHandler;

	public AdapterRealTimeDataServlet(KemObjectsServiceTracker ASTHandler_) {
		super(LABEL, TITLE, CATEGORY, CSS);
		// TODO Auto-generated constructor stub
		ASTHandler = ASTHandler_;
	}

	@Override
	public void activate(BundleContext bundleContext)
	{
		super.activate(bundleContext);
		log("WebConsolePlugin starts");
	}

	/**
	 * @see org.apache.felix.webconsole.SimpleWebConsolePlugin#deactivate()
	 */
	public void deactivate()
	{
		super.deactivate();
		log("WebConsolePlugin stops");
	}

	/**
	 * @see org.apache.felix.webconsole.AbstractWebConsolePlugin#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		String id = null;
		id = request.getParameter("id");
		if (id != null) {
			String returnedData = "";
			final PrintWriter out = response.getWriter();
			response.setContentType("application/json"); //$NON-NLS-1$
			response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
			if (id.equals("adapters"))
			{
				log("Do Get:Adapters");
				returnedData = parserToJSONData(out, "getAdapters", null);
			} else if (id.equals("data")) {
				log("Do Get:Data");
				returnedData = parserToJSONData(out, "getAdapterData", request);
			}
			else if (id.equals("read_data")) {
				log("Do Get:Read_Data");
				returnedData = parserToJSONData(out, "getRealTimeData", request);
			}
			out.print(returnedData);
			out.flush();
			return;
		} else if ("html".equals(request.getParameter("action"))) //$NON-NLS-1$
		{
			response.setContentType("application/html"); //$NON-NLS-1$
			response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
			String adapter = request.getParameter("adapter");
			String device = request.getParameter("device");
			String page_template = "/template/realtimedata/devices/" + adapter + "_" + device + ".html";
			log("html_page device request: " + page_template);
			response.getWriter().print(readTemplateFile(page_template));
			return;

		}

		super.doGet(request, response);
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException
	{
		final String action = request.getParameter("action");
		PrintWriter out = response.getWriter();
		ObjectMapper objectMapper = new ObjectMapper();

		if ("set_mode".equals(action))
		{
			int mode = -1;
			try {
				mode = Integer.parseInt(request.getParameter("mode"));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			if (mode != -1) {
				String adaptername = request.getParameter("adapter");
				if (adaptername != null) {
					// Find adapter on list
					Adapter adapter = ASTHandler.getAdapterService(adaptername);
					Map<String, Integer> map = new HashMap<String, Integer>();
					// if adapter exists in list
					if (adapter != null) {
						log("set mode:" + mode);
						adapter.setMode(mode);
						response.setContentType("application/json");
						response.setCharacterEncoding("UTF-8");
						map.put("mode", adapter.getMode());
						// response.setContentType("text/plain");
					}
					out.print(objectMapper.writeValueAsString(map));
					out.flush();
				}
			}
		} else if ("node_num".equals(action)) {
			String adapterName = request.getParameter("adapter");
			String address = request.getParameter("address");
			String instanceId = request.getParameter("id");
			String nodeType = request.getParameter("node_type");
			Adapter adapter = ASTHandler.getAdapterService(adapterName);
			Map<String, Object> webconsoleData = new HashMap<String, Object>();
			int nodeNum = 0;
			if (adapter != null) {
				if (adapter.getAdapterType() == Adapter.BACNET_TYPE) {
					ABacnetAdapter bacnet = (ABacnetAdapter) adapter;
					if ("real_node".equals(nodeType)) {
						bacnet.setNodeType(ABacnetAdapter.USED_REAL_READING_NODE);
					} else if ("virtual_node".equals(nodeType)) {
						bacnet.setNodeType(ABacnetAdapter.USED_VIRTUAL_READING_NODE);
					}
					nodeNum = bacnet.numOfNode(address, instanceId, bacnet.getNodeType(),"");
				}
			}
			webconsoleData.put("node_num", nodeNum);
			out.print(objectMapper.writeValueAsString(webconsoleData));
			out.flush();
		}
		else if ("read_data".equals(action)) {
			String adapterName = request.getParameter("adapter");
			String address = request.getParameter("address");
			String instanceId = request.getParameter("id");
			String channel = request.getParameter("channel");
			String len = request.getParameter("length");
			String mode = request.getParameter("mode");
			Adapter adapter = ASTHandler.getAdapterService(adapterName);
			Map<String, Object> webconsoleData = new HashMap<String, Object>();
			if (adapter != null) {
				boolean ok = true;
				if (adapter.getAdapterType() == Adapter.MODBUS_TYPE) {
					AModbusAdapter modbusAdapter = (AModbusAdapter) adapter;
					if (modbusAdapter.getModbusProtocolStatus() != 0) {
						log("Do Get:Read_Data, can not find serial port or internet");
						webconsoleData.put("status", -4);
						// need improvement in the future
						ok = false;
					}
				}
				if (ok) {
					if (Integer.parseInt(mode) == adapter.getMode()) {
						try {
							List<Map<String, String>> realTimeData;

							realTimeData = adapter.getRealTimeData(address, instanceId, channel, len);
							
							if (realTimeData.isEmpty()) {
								log("Do Get:Read_Data, no data available");
								webconsoleData.put("status", -1);
							} else {
								log("Do Get:Read_Data, read data for mode = " + mode + " - current mode = " + adapter.getMode());
								webconsoleData.put("status", 0);
								webconsoleData.put("data", realTimeData);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						// request and coordinate adapter don't have same mode
						log("Do Get:Read_Data, incorrect mode");
						webconsoleData.put("status", -2);
						webconsoleData.put("mode", String.valueOf(adapter.getMode()));
					}
				}
			} else {
				log("Do Get:Read_Data, no adapter available");
				webconsoleData.put("status", -3);
			}

			out.print(objectMapper.writeValueAsString(webconsoleData));
			out.flush();
		}
	}

	private String parserToJSONData(final PrintWriter pw, String action, HttpServletRequest request) throws IOException
	{
		if (action == null) {
			log("action is null");
			return "";
		}
		try
		{
			ArrayList<Adapter> adapterServices = ASTHandler.getAdapterServices();
			ObjectMapper mapper = new ObjectMapper();
			if (action.equals("getAdapters")) {
				List<String> adapters = new ArrayList<String>();
				for (int i = 0; i < adapterServices.size(); i++) {
					adapters.add(adapterServices.get(i).getClass().getSimpleName());
				}
				return mapper.writeValueAsString(adapters);
			} else if (action.equals("getAdapterData")) {
				String adapterName = request.getParameter("adapter");
				Adapter adapter = ASTHandler.getAdapterService(adapterName);
				Map<String, Object> adapterSettings = new HashMap<String, Object>();
				adapterSettings.put("mode", adapter.getMode());
				adapterSettings.put("devices", adapter.getDeviceList());
				return mapper.writeValueAsString(adapterSettings);

			} else if (action.equals("getRealTimeData")) {
				// String adapterName = request.getParameter("adapter");
				// String address = request.getParameter("address");
				// String category = request.getParameter("category");
				// String channel = request.getParameter("channel");
				// String mode = request.getParameter("mode");
				// Adapter adapter = ASTHandler.getAdapterService(adapterName);
				// if (adapter == null)
				// {
				// log("Do Get:Read_Data, no adapter available");
				// jw.object();
				//        			jw.key("status"); //$NON-NLS-1$
				// jw.value(-3);
				// jw.endObject();
				// return;
				// }
				//
				// if (!adapter.isExistedSerialPort()) {
				// log("Do Get:Read_Data, can not find serial port");
				// jw.object();
				//        			jw.key("status"); //$NON-NLS-1$
				// jw.value(-4);
				//        			jw.key("serial_port"); //$NON-NLS-1$
				// jw.value(adapter.getSerialPort());
				// jw.endObject();
				// return;
				// }
				//
				// if(Integer.parseInt(mode) == adapter.getMode()){
				// Hashtable<String,String> realTimeData = adapter.getData(Integer.parseInt(address), Integer.parseInt(channel));
				// if (realTimeData == null) {
				// log("Do Get:Read_Data, no data available");
				// jw.object();
				//	        			jw.key("status"); //$NON-NLS-1$
				// jw.value(-1);
				// jw.endObject();
				// return;
				// }
				// log("Do Get:Read_Data, read data for mode = " + mode + " - current mode = " + adapter.getMode());
				// jw.object();
				//        			jw.key("status"); //$NON-NLS-1$
				// jw.value(0);
				// jw.key("read_data");
				// jw.array();
				// for (String key : realTimeData.keySet()) {
				// jw.object();
				// jw.key("id");
				// jw.value(key);
				// jw.key("value");
				// jw.value(realTimeData.get(key));
				// jw.endObject();
				// }
				// jw.endArray();
				// jw.endObject();
				// }else{
				// //request and coordinate adapter don't have same mode
				// log("Do Get:Read_Data, incorrect mode");
				// jw.object();
				//                	jw.key("status"); //$NON-NLS-1$
				// jw.value(-2);
				//                	jw.key("mode"); //$NON-NLS-1$
				// jw.value(adapter.getMode());
				// jw.endObject();
				// }

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	protected void renderContent(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().print(readTemplateFile(REALTIMEDATA_TEMPLATE));
	}

	public void log(String text) {
		if (debug) {
			System.out.println(this.getClass().getSimpleName() + "---" + text);
		}
	}

}
