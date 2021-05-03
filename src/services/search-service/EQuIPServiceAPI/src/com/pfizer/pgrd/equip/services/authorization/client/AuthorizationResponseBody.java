package com.pfizer.pgrd.equip.services.authorization.client;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AuthorizationResponseBody {
	private String response;
	private Map<String, Boolean> permissionsInfo;
	private List<String> users;
	
	//JAXB requires a constructor
	public AuthorizationResponseBody() {}
			
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

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}


}

