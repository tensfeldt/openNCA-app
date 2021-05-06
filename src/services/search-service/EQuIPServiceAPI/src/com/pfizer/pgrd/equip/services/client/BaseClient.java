package com.pfizer.pgrd.equip.services.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BaseClient extends ServiceCaller {
	private static final String GET = "get", POST = "post", PUT = "put";
	
	private String user;
	private int port;
	private String host;
	private String systemId = "nca";
	
	public BaseClient() throws ServiceCallerException {
		super();
	}
	
	public ServiceResponse get(String uri) throws ServiceCallerException {
		return this.call(uri, GET, null);
	}

	public Future<ServiceResponse> getAsync(String uri) throws ServiceCallerException {
		return this.callAsync(uri, GET, null);
	}
	//
	public ServiceResponse post(String uri, String data,Map<String,String> headers) throws ServiceCallerException {
		return this.call(uri, POST, data,headers);
	}
	public ServiceResponse post(String uri, String data) throws ServiceCallerException {
		return this.post(uri, data,null);
	}
	
	public Future<ServiceResponse> postAsync(String uri, String data) throws ServiceCallerException {
		return this.callAsync(uri, POST, data);
	}
	
	public ServiceResponse put(String uri, String data) throws ServiceCallerException {
		return this.call(uri, PUT, data);
	}
	
	public Future<ServiceResponse> putAsync(String uri, String data) throws ServiceCallerException {
		return this.callAsync(uri, PUT, data);
	}
	
	private Map<String, String> getHeaders(boolean hasData) {
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		if(this.getUser() != null) {
			headers.put("IAMPFIZERUSERCN", this.getUser());
		}
		if(hasData) {
			headers.put("Content-Type", "application/json");
		}
		
		return headers;
	}
	
	private Future<ServiceResponse> callAsync(String uri, String method, String data) {
		if(method != null) {
			Map<String, String> headers = this.getHeaders(data != null);
			if(method.equalsIgnoreCase(GET)) {
				return this.getAsync(uri, headers);
			}
			else if(method.equalsIgnoreCase(POST)) {
				return this.postAsync(uri, headers, data);
			}
			else if(method.equalsIgnoreCase(PUT)) {
				return this.putAsync(uri, headers, data);
			}
		}
		
		return null;
	}
	//
	private ServiceResponse call(String uri, String method, String data,Map<String,String> additionalheaders) throws ServiceCallerException {
		ServiceResponse response = null;
		try {
			Map<String, String> headers = this.getHeaders(data != null);
			if(additionalheaders != null) {
				for(Map.Entry<String, String> kvp : additionalheaders.entrySet()) {
					headers.put(kvp.getKey(), kvp.getValue());
				}
			}
			additionalheaders = headers;
			Gson gson = new Gson();
			System.out.println("Headers = "+gson.toJson(additionalheaders));
			if( method.equalsIgnoreCase(POST) ) {
				response = this.post(uri, additionalheaders, data);
			}
			else if( method.equalsIgnoreCase(PUT) ) {
				response = this.put(uri, additionalheaders, data);
			}
			else if( method.equalsIgnoreCase(GET) ) {
				response = this.get(uri, additionalheaders);
			}
		}
		catch(Exception e) {
			System.err.println("Error " + method + " " + uri + ": " + e.getMessage());
			throw e;
		}
		
		return response;
	}
	private ServiceResponse call(String uri, String method, String data) throws ServiceCallerException {
		return this.call(uri, method, data, null);
		
	}
	
	protected String getBaseURI() {
		return "http://" + this.host + ":" + this.port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}
	
	protected Gson getGson() {
		GsonBuilder gb = new GsonBuilder();
		gb.setPrettyPrinting();
		
		return gb.create();
	}


}
