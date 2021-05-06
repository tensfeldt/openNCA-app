package com.pfizer.pgrd.equip.services.authorization.client;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AuthorizationPrivilegesResponse {
	private String response;
	private List<String> privileges;
	
	//JAXB requires a constructor
	public AuthorizationPrivilegesResponse() {}
			
	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public List<String> getPrivileges() {
		return privileges;
	}

	public void setPrivileges(List<String> privileges) {
		this.privileges = privileges;
	}


}

