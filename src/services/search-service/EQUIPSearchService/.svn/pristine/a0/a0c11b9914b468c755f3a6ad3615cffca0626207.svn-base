package com.pfizer.equip.service.client;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

/**
 * Encapsulates responses from ServiceClient.
 * 
 * @author HeinemanWP
 *
 */
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
		InputStream is = inputStream != null ? inputStream : errorStream;
		if (is == null) {
			return "";
		}
		try(ByteArrayOutputStream result = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[8192];
			int length;
			while ((length = is.read(buffer)) != -1) {
			    result.write(buffer, 0, length);
			}
			// StandardCharsets.UTF_8.name() > JDK 7
			return result.toString(StandardCharsets.UTF_8.name());	 
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
