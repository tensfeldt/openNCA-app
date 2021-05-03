package com.pfizer.pgrd.equip.services.authorization.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AuthorizationAccessGroups {
	private String response;
	private Map<String, Boolean> permissionsInfo;
	private List<User> users;
	
	//JAXB requires a constructor
	public AuthorizationAccessGroups() {}
			
	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public Map<String, Boolean> getPermissionsInfo() {
		return permissionsInfo;
	}

	public void setPermissionsInfo(Map<String, Boolean> permissionsInfo) {
		this.permissionsInfo = permissionsInfo;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}


}

