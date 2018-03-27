package com.greenkoncepts.gateway.control.rest.server;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greekoncepts.gateway.api.schedule.Schedule;
import com.greenkoncepts.gateway.api.adapter.Adapter;
import com.greenkoncepts.gateway.api.task.ITaskExecute;
@Path("/control")
public class RestApiController {
	static private RestApiController restApiController;
	private static final String AUTHORIZATION_PROPERTY = "Authorization";
	private static final String AUTHENTICATION_SCHEME = "Basic";
	private Logger mLogger = LoggerFactory.getLogger("RestApiController");
	protected RestApiController() {
		
	}
	@GET
	@Path("/login")
	//@Consumes("application/x-www-form-urlencoded")
	@Consumes("application/json")
	@Produces("application/json")
	public Response login(@QueryParam("user") String user,
			@QueryParam("pass") String password,@HeaderParam("content-type") String contentType) {
		mLogger.info("[login] with user = " + user + " - pass= " + password);
		if (user == null || password == null) {
			return Response.serverError().build();
		}
		System.out.println("content type :" + contentType);
		
		AccessManager mgr = AccessManager.getInstance();
		Map<String, String> results = new HashMap<String, String>();
		results.put("user", user);
		if(mgr.validate(user, password)){
			long currentTime = System.currentTimeMillis();
			mgr.addKey(currentTime);
			results.put("key", String.valueOf(currentTime));
		} else {
			results.put("error", "user is invalid");
		}
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonResults;
		try {
			jsonResults = objectMapper.writeValueAsString(results);
		} catch (JsonProcessingException e) {
			jsonResults = "";
			mLogger.error("JsonProcessingException", e);
		}
		return Response.ok(jsonResults).build();
	}
	
	@GET
	@Path("/logout")
	//@Consumes("application/x-www-form-urlencoded")
	@Consumes("application/json")
	@Produces("application/json")
	public Response logout(@QueryParam("key") String key) {
		mLogger.info("[logout] with key = " + key);
	
		AccessManager mgr = AccessManager.getInstance();
		Map<String, String> results = new HashMap<String, String>();
		
		if(mgr.isKeyValid(Long.valueOf(key))){
			mgr.removeKey(Long.valueOf(key));
			results.put("key", key);
		} else {
			results.put("error", "key is invalid");
		}
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonResults;
		try {
			jsonResults = objectMapper.writeValueAsString(results);
		} catch (JsonProcessingException e) {
			jsonResults = "";
			mLogger.error("JsonProcessingException", e);
		}
		return Response.ok(jsonResults).build();
	}
	
	@POST
	@Path("/sensors")
	@Consumes("application/json")
	@Produces("application/json")
	public Response setDataSensor(String jSon) {
		mLogger.debug("[setDataSensor]  jSon = " + jSon);
		Map<String, String> results = new HashMap<String, String>();
		RestServiceTracker tracker = RestServiceTracker.getInstance();
		ITaskExecute taskExecute = tracker.getTaskExecuteService();
		XDKTask task = new XDKTask(tracker, jSon);
		if (taskExecute != null) {
			taskExecute.addTask(task);
		}

		ObjectMapper objectMapper = new ObjectMapper();
		String jsonResults;
		try {
			results.put("success", "TRUE" );
			jsonResults = objectMapper.writeValueAsString(results);
		} catch (JsonProcessingException e) {
			jsonResults = "";
			mLogger.error("JsonProcessingException", e);
		}
		
		return Response.ok(jsonResults).build();
		
	}
	
	@POST
	@Path("/sensor/monnit")
	@Consumes("application/json")
	@Produces("application/json")
	public Response setMonnitDataSensor(String jSon) {
		mLogger.debug("[setDataSensor]  jSon = " + jSon);
		Map<String, String> results = new HashMap<String, String>();
		RestServiceTracker tracker = RestServiceTracker.getInstance();
		Adapter foundAdapter = tracker.getAdapterService("MonnitAdapter");
		if (foundAdapter == null) {
			results.put("error", "Adapter is invalid");
		} else {
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				@SuppressWarnings("unchecked")
				Map<String, String> oiMap = objectMapper.readValue(jSon, Map.class);
				boolean successes = foundAdapter.setNodeValue("", "", oiMap);
				results.put("success", successes ? "TRUE" : "FALSE");
			} catch (JsonParseException e) {
				results.put("error", "JsonParseException");
				mLogger.error("JsonParseException", e);
			} catch (JsonMappingException e) {
				results.put("error", "JsonMappingException");
				mLogger.error("JsonMappingException", e);
			} catch (IOException e) {
				results.put("error", "IOException");
				mLogger.error("IOException", e);
			} catch (Exception e) {
				results.put("error", "Exception");
				mLogger.error("Exception", e);
			}
		}
		
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonResults;
		try {
			jsonResults = objectMapper.writeValueAsString(results);
		} catch (JsonProcessingException e) {
			jsonResults = "";
			mLogger.error("JsonProcessingException", e);
		}
		
		return Response.ok(jsonResults).build();
		
	}
	
	
	@POST
	@Path("/setbacnet")
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	public Response setBacnetProperties( @FormParam("adapter") String adapter, @FormParam("deviceid") String id, @FormParam("data") String data, @HeaderParam("Authorization") String authorization) {
		System.out.println("[setBacnetProperties]  adapter = " + adapter + ", id = "+id+", data = " + data + ", authorization = " + authorization);
		final String encodedUserPassword = authorization.replaceFirst(AUTHENTICATION_SCHEME + " ", "");
		//Decode username and password
		
        String usernameAndPassword = "";
		try {
			usernameAndPassword = new String(Base64.decode(encodedUserPassword.getBytes()));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		};

        //Split username and password tokens
        final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
        final String username = tokenizer.nextToken();
        final String password = tokenizer.nextToken();
        
        System.out.println("sheeave username = " + username + " , password = " + password);
        
		AccessManager mgr = AccessManager.getInstance();
		Map<String, String> results = new HashMap<String, String>();
		results.put("user", username);

		if(!mgr.validate(username, password)){
			results.put("error", "username/password is invalid");
		} else {
			RestServiceTracker tracker = RestServiceTracker.getInstance();
			Adapter foundAdapter = tracker.getAdapterService(adapter);
			if (foundAdapter == null) {
				results.put("error", "Adapter is invalid");
			} else {
				ObjectMapper objectMapper = new ObjectMapper();
				try {
					@SuppressWarnings("unchecked")
					Map<String, String> oiMap = objectMapper.readValue(data, Map.class);
					boolean successes = foundAdapter.setNodeValue("10000", id, oiMap);
					results.put("success", successes ? "TRUE" : "FALSE");
				} catch (JsonParseException e) {
					results.put("error", "JsonParseException");
					mLogger.error("JsonParseException", e);
				} catch (JsonMappingException e) {
					results.put("error", "JsonMappingException");
					mLogger.error("JsonMappingException", e);
				} catch (IOException e) {
					results.put("error", "IOException");
					mLogger.error("IOException", e);
				} catch (Exception e) {
					results.put("error", "Exception");
					mLogger.error("Exception", e);
				}
			}
		}
		
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonResults;
		try {
			jsonResults = objectMapper.writeValueAsString(results);
		} catch (JsonProcessingException e) {
			jsonResults = "";
			mLogger.error("JsonProcessingException", e);
		}
		
		return Response.ok(jsonResults).build();
		
	}
	
	@POST
	@Path("/scheduler/add")
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	public Response addSchedulers(@FormParam("schedulers") String jsonSchedulers, @HeaderParam("Authorization") String authorization) {
		System.out.println("[addSchedulers]  schedulers = " + jsonSchedulers + ", authorization = " + authorization);
		Map<String, String> results = new HashMap<String, String>();
		int errorCode = 200;
		if (authorization == null) {
			results.put("error", "No authorization");
			errorCode = 401;
		} else {
			try {
				String encodedUserPassword = authorization.replaceFirst(AUTHENTICATION_SCHEME + " ", "");
				String usernameAndPassword = new String(Base64.decode(encodedUserPassword.getBytes()));
				StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
				String username = tokenizer.nextToken();
				String password = tokenizer.nextToken();

				System.out.println("Rest api addSchedulers username = " + username + " , password = " + password);

				AccessManager mgr = AccessManager.getInstance();

				results.put("user", username);

				if (!mgr.validate(username, password)) {
					errorCode = 401;
					results.put("error", "username/password is invalid");
				} else {
					RestServiceTracker tracker = RestServiceTracker.getInstance();
					Schedule scheduleService = tracker.getScheduleService();
					if (scheduleService == null) {
						errorCode = 500;
						results.put("error", "Schedule Service is invalid");
					} else {
						Map<Object, Object> returnRes = scheduleService.addSchedulerArrayFromJson(jsonSchedulers);
						for (Object key : returnRes.keySet()) {
							results.put((String) key, (String) returnRes.get(key));
						}
						
						String success = results.get("success");
						if ((success == null) || (success.equals("False"))) {
							errorCode = 500;
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				errorCode = 500;
				results.put("error", e.getMessage());
			}

		}

		ObjectMapper objectMapper = new ObjectMapper();
		String jsonResults;
		try {
			jsonResults = objectMapper.writeValueAsString(results);
		} catch (JsonProcessingException e) {
			jsonResults = "";
			mLogger.error("JsonProcessingException", e);
		}

		return Response.status(errorCode).entity(jsonResults).build();
	}

	@POST
	@Path("/scheduler/update")
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	public Response updateSchedulers(@FormParam("schedulers") String jsonSchedulers, @HeaderParam("Authorization") String authorization) {
		System.out.println("[updateSchedulers]  schedulers = " + jsonSchedulers + ", authorization = " + authorization);
		Map<String, String> results = new HashMap<String, String>();
		int errorCode = 200;
		if (authorization == null) {
			results.put("error", "No authorization");
			errorCode = 401;
		} else {
			try {
				String encodedUserPassword = authorization.replaceFirst(AUTHENTICATION_SCHEME + " ", "");
				String usernameAndPassword = new String(Base64.decode(encodedUserPassword.getBytes()));
				StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
				String username = tokenizer.nextToken();
				String password = tokenizer.nextToken();

				System.out.println("Rest api updateSchedulers username = " + username + " , password = " + password);

				AccessManager mgr = AccessManager.getInstance();

				results.put("user", username);

				if (!mgr.validate(username, password)) {
					errorCode = 401;
					results.put("error", "username/password is invalid");
				} else {
					RestServiceTracker tracker = RestServiceTracker.getInstance();
					Schedule scheduleService = tracker.getScheduleService();
					if (scheduleService == null) {
						errorCode = 500;
						results.put("error", "Schedule Service is invalid");
					} else {
						Map<Object, Object> returnRes = scheduleService.updateSchedulerArrayFromJson(jsonSchedulers);
						for (Object key : returnRes.keySet()) {
							results.put((String) key, (String) returnRes.get(key));
						}

						String success = results.get("success");
						if ((success == null) || (success.equals("False"))) {
							errorCode = 500;
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				errorCode = 500;
				results.put("error", e.getMessage());
			}

		}
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonResults;
		try {
			jsonResults = objectMapper.writeValueAsString(results);
		} catch (JsonProcessingException e) {
			jsonResults = "";
			mLogger.error("JsonProcessingException", e);
		}

		return Response.status(errorCode).entity(jsonResults).build();
	}
	
	@POST
	@Path("/scheduler/delete")
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	public Response deleteSchedulers(@FormParam("schedulers") String jsonSchedulers, @HeaderParam("Authorization") String authorization) {
		System.out.println("[deleteSchedulers]  schedulerIds = " + jsonSchedulers + ", authorization = " + authorization);
		Map<String, String> results = new HashMap<String, String>();
		int errorCode = 200;
		if (authorization == null) {
			results.put("error", "No authorization");
			errorCode = 401;
		} else {
			try {
				String encodedUserPassword = authorization.replaceFirst(AUTHENTICATION_SCHEME + " ", "");
				String usernameAndPassword = new String(Base64.decode(encodedUserPassword.getBytes()));
				StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
				String username = tokenizer.nextToken();
				String password = tokenizer.nextToken();

				System.out.println("Rest api deleteSchedulers username = " + username + " , password = " + password);

				AccessManager mgr = AccessManager.getInstance();

				results.put("user", username);

				if (!mgr.validate(username, password)) {
					errorCode = 401;
					results.put("error", "username/password is invalid");
				} else {
					RestServiceTracker tracker = RestServiceTracker.getInstance();
					Schedule scheduleService = tracker.getScheduleService();
					if (scheduleService == null) {
						errorCode = 500;
						results.put("error", "Schedule Service is invalid");
					} else {
						Map<Object, Object> returnRes = scheduleService.deleteSchedulerArrayFromJson(jsonSchedulers);
						for (Object key : returnRes.keySet()) {
							results.put((String) key, (String) returnRes.get(key));
						}

						String success = results.get("success");
						if ((success == null) || (success.equals("False"))) {
							errorCode = 500;
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				errorCode = 500;
				results.put("error", e.getMessage());
			}

		}
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonResults;
		try {
			jsonResults = objectMapper.writeValueAsString(results);
		} catch (JsonProcessingException e) {
			jsonResults = "";
			mLogger.error("JsonProcessingException", e);
		}

		return Response.status(errorCode).entity(jsonResults).build();
	}

	static public RestApiController getInstance() {
		if (restApiController == null) {
			restApiController = new RestApiController();
		}
		return restApiController;
	}
}
