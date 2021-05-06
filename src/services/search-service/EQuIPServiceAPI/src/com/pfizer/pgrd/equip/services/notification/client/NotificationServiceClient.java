package com.pfizer.pgrd.equip.services.notification.client;

import com.google.gson.Gson;
import com.pfizer.pgrd.equip.services.client.BaseClient;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.client.ServiceResponse;

public class NotificationServiceClient extends BaseClient {
	private static final String APPLICATION_JSON = "application/json";
	private static final Gson GSON = new Gson();

	public NotificationServiceClient() throws ServiceCallerException {
		this(null, 0, null);
	}

	public NotificationServiceClient(String host, int port, String systemId) throws ServiceCallerException {
		super();
		this.setHost(host);
		this.setPort(port);
		this.setSystemId(systemId);
	}

	public static final NotificationServiceClient getClient(String host, int port, String systemId)
			throws ServiceCallerException {
		return new NotificationServiceClient(host, port, systemId);

	}


	public boolean postNotification(String user, NotificationRequestBody notifRequestBody) {
		this.setUser(user);

		String bodyJson = new Gson().toJson(notifRequestBody);

		
		//http://equip-services-dev.pfizer.com/equip-services/event/publish
			String url = this.getBaseURI() +  "/event/publish/";

		try {
			ServiceResponse sr = this.post(url, bodyJson);
			String json = sr.getResponseAsString();
			NotificationResponseBody nrb = GSON.fromJson(json, NotificationResponseBody.class);
			String response = nrb.getResponse();
			return response.equalsIgnoreCase("ok");
		} catch (Exception ex) {
			return false;
		}
	}
	
	@Override
	protected String getBaseURI() {
		return super.getBaseURI() + "/equip-services/" + this.getSystemId();
	}

}
