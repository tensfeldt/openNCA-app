package com.pfizer.equip.service.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class ServiceResponse {
	private final HttpURLConnection connection;
	private final int code;
	private final InputStream inputStream;
	private final InputStream errorStream;
	
	public ServiceResponse(HttpURLConnection connection, int code, InputStream inputStream, InputStream errorStream) {
		this.connection = connection;
		this.code = code;
		this.inputStream = inputStream;
		this.errorStream = errorStream;
	}
	
	public int getCode() {
		return code;
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}
	
	public InputStream getErrorStream() {
		return errorStream;
	}
	
	public String getResponseAsString() throws ServiceCallerException {
		try {
			InputStream is = inputStream != null ? inputStream : errorStream;
			if (is == null) {
				return "";
			}
			try (BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
				 String inputLine;
				 StringBuilder response = new StringBuilder();
				 while ((inputLine = r.readLine()) != null) {
					 response.append(inputLine);
				 }
				 return response.toString();
			}
		} catch(Exception ex) {
			throw new ServiceCallerException(ex);
		} finally {
			disconnect();
		}
	}
	
	public void disconnect() {
		connection.disconnect();
	}
	
}
