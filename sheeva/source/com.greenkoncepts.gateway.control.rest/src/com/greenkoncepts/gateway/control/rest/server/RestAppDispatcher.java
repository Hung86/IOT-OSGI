package com.greenkoncepts.gateway.control.rest.server;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

public class RestAppDispatcher extends Application{
	private Set<Object> singletons = new HashSet<Object>();
	 
//	public RestApp(@Context Dispatcher dispatcher) {
//	  
//	}
	 
	public RestAppDispatcher(){
		super();
		singletons.add(RestApiController.getInstance());
	}
	 
	 
	@Override
	public Set<Object> getSingletons() {
		System.out.println("------------WTF SINGLRTONS");
		return singletons;
	}
}
