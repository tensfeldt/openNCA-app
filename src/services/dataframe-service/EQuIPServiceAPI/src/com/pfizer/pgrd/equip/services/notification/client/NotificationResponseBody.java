package com.pfizer.pgrd.equip.services.notification.client;


import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NotificationResponseBody {
	//there are other properties sent in the response body - we are just checking that it is ok
	//possible to use others later
	
	private String response;

	public String getResponse() {
		return response;
	}


}




