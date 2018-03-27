package com.greenkoncepts.gateway.control.rest.server;

import java.util.ArrayList;
import java.util.List;

public class AccessManager {
	private static AccessManager instance;
 
	private List<Long> key;
 
 
	private AccessManager() {
		key = new ArrayList<Long>();
	}
 
	public static AccessManager getInstance() {
		if (instance == null)
				instance = new AccessManager();
	  return instance;
	}
 
	public boolean addKey(Long key){
		return this.key.add(key);
	}
 
	public boolean removeKey(Long key){
		return this.key.remove(key);
	}
 
	public boolean isKeyValid(Long key){
		if(this.key.indexOf(key) != -1){
			return true;
		}
		return false;
	}
 
	public boolean validate(String user,String password){
		if((user.equals("gkadmin") && (password.equals("greenkoncepts")))){
			return true;
		}
		return false;
	}
}
