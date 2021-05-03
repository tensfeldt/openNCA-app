package com.pfizer.pgrd.equip.services.computeservice.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.pfizer.pgrd.equip.services.client.BaseClient;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.client.ServiceResponse;
import com.pfizer.pgrd.equip.services.computeservice.dto.ComputeParameters;
import com.pfizer.pgrd.equip.services.computeservice.dto.ComputeResult;

public class ComputeServiceClient extends BaseClient {
	private static final Gson GSON = new Gson();
	private Map<String,String> headers; 
	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	
	public ComputeServiceClient() throws ServiceCallerException { this(null, 0); }
	
	public ComputeServiceClient(String host, int port) throws ServiceCallerException {
		super();
		this.setHost(host);
		this.setPort(port);
	}
	
	public String getVersion() throws ServiceCallerException {
		String uri = super.getBaseURI() + "/EQUIPComputeService/version";
		ServiceResponse sr = this.get(uri);
		return sr.getResponseAsString();
	}
	
	public ComputeResult compute(ComputeParameters params) throws ServiceCallerException {
		return this.compute(params, false);
	}
	
	public ComputeResult computeVirtual(ComputeParameters params) throws ServiceCallerException {
		return this.compute(params, true);
	}
	
	public ComputeResult compute(ComputeParameters params, boolean isVirtual) throws ServiceCallerException {
		ComputeResult result = null;
		if(params != null) {
			String uri = this.getBaseURI() + "/compute";
			if(isVirtual) {
				uri += "?virtual=true";
			}
			
			String json = GSON.toJson(params);
			System.out.println(json);
			System.out.println("-------headers to compute");
			System.out.println(GSON.toJson(this.getHeaders()));
			ServiceResponse sr = this.post(uri, json,this.getHeaders());
			json = sr.getResponseAsString();
			if(json != null && sr.getCode() == 200) {
				result = GSON.fromJson(json, ComputeResult.class);
				result.setUri(uri);
			}  else {
				throw new ServiceCallerException(json, sr.getCode());
			}
		}
		
		return result;
	}
	
	public void computeAsync(ComputeParameters params) throws Exception {
		throw new Exception("Not Implemented");
	}
	
	public List<String> getAsyncList() {
		List<String> list = new ArrayList<>();
		return list;
	}
	
	@Override
	protected String getBaseURI() {
		return super.getBaseURI() + "/EQUIPComputeService/" + this.getSystemId();
	}
}