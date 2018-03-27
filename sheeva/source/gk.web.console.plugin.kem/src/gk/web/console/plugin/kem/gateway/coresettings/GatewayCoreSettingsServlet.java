package gk.web.console.plugin.kem.gateway.coresettings;

import gk.web.console.plugin.kem.KemObjectsServiceTracker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.json.JSONObject;
import org.osgi.framework.BundleContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class GatewayCoreSettingsServlet extends SimpleWebConsolePlugin {
	private static final long serialVersionUID = 1L;
	private static final String CATEGORY = "KEM";
	private static final String LABEL = "gatewaysettings";
	private static final String GATEWAYSETTINGS = LABEL;
	private static final String TITLE = "Gateway Settings";
	private static final String[] CSS = { "/" + GATEWAYSETTINGS + "/template/gatewaysettings/style.css" };
	private final String GATEWAY_TEMPLATE = "/template/gatewaysettings/gateway_settings_entry.html";
	KemObjectsServiceTracker ASTHandler;

	public GatewayCoreSettingsServlet(KemObjectsServiceTracker ASTHandler) {
		super(LABEL, TITLE, CATEGORY, CSS);
		this.ASTHandler = ASTHandler;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String id = null;
		id = request.getParameter("id");
		log("Do Get: : id = " + id);
		if (id != null) {
			if ("exporting".equals(id)) {
				Map<String, String> bridgeSettings = ASTHandler.getCurrentBridgeSettings();
				response.setContentType("application/json");
				response.setHeader("Content-Disposition", "attachment; filename=\"GatewaySettings.json\"");

				try
				{
					OutputStream outputStream = response.getOutputStream();
					ObjectMapper mapper = new ObjectMapper();
					mapper.enable(SerializationFeature.INDENT_OUTPUT);
					String result = mapper.writeValueAsString(bridgeSettings);
					outputStream.write(result.getBytes());
					outputStream.flush();
					outputStream.close();
				} catch (Exception e)
				{
					e.printStackTrace();
				}

			} else {
				final PrintWriter out = response.getWriter();
				response.setContentType("application/json"); //$NON-NLS-1$
				response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
				out.print(parserToJSONDataString(id));
				out.flush();
			}
			return;
		}

		super.doGet(request, response);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String actionPost = FilenameUtils.getBaseName(request.getPathInfo());
		log("Do Post:" + actionPost);
		if ("importing".equals(actionPost)) {
			try {
				List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
				for (FileItem item : items) {
					if("GatewaySettings.json".equals(item.getName())){
						InputStream fileContent = items.get(0).getInputStream();
						ObjectMapper mapper = new ObjectMapper();
						Map<String, String> settingMap = mapper.readValue(fileContent, Map.class);
						ASTHandler.updateBridgeSettings(settingMap);
					}
				}
			} catch (FileUploadException e) {
				throw new ServletException("Cannot parse multipart request.", e);
			}
			response.sendRedirect("/system/console/" + GATEWAYSETTINGS);
		} else {
			ObjectMapper objectMapper = new ObjectMapper();
			String bridge = request.getParameter("bridge");
			Map<String, String> bridgeMap = objectMapper.readValue(bridge, Map.class);
			System.out.println("--------bridgeMap = " + bridgeMap);
			try {
				JSONObject result = new JSONObject();
				if (ASTHandler.updateBridgeSettings(bridgeMap)) {
					//response.setContentType("application/json"); //$NON-NLS-1$
					//response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
					result.put("result", "success");

				} else {
					result.put("result", "fail");

				}
				response.getWriter().print(result);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	synchronized private String parserToJSONDataString(String id)
			throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> settings = new HashMap<String, String>();

		if ("all".equals(id)) {
			settings = ASTHandler.getCurrentBridgeSettings();
		} else {
			settings = ASTHandler.getBridgeSettingsBy(id);
		}
		if (!settings.isEmpty()) {
			DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss dd/MM/yyyy");
			long datetime = Long.parseLong(settings.get("last_modified"));
			if (datetime == 0) {
				datetime = System.currentTimeMillis();
			}

			settings.put("last_modified",
					"Last modified at " + dateFormat.format(datetime));
		}
		return mapper.writeValueAsString(settings);
	}

	@Override
	protected void renderContent(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().print(readTemplateFile(GATEWAY_TEMPLATE));

	}

	@Override
	public void activate(BundleContext bundleContext)
	{
		super.activate(bundleContext);
		// log("WebConsolePlugin starts");
	}

	/**
	 * @see org.apache.felix.webconsole.SimpleWebConsolePlugin#deactivate()
	 */
	public void deactivate()
	{
		super.deactivate();
		// log("WebConsolePlugin stops");
	}

	public void log(String text) {
		if (true) {
			System.out.println(this.getClass().getSimpleName() + "---" + text);
		}
	}
}
