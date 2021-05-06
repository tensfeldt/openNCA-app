package com.pfizer.pgrd.equip.services.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ServiceCaller {
	private static String GET = "GET", POST = "POST", PUT = "PUT", DELETE = "DELETE";
	private static CookieManager cookieManager;

	public ServiceCaller() throws ServiceCallerException {
		if (cookieManager == null) {
			cookieManager = new CookieManager();
			CookieHandler.setDefault(cookieManager);
		}
	}

	private HttpURLConnection createConnection(String uri, String method, Map<String, String> headers,
			Map<String, String> parameters) throws IOException {
		// Create connection...
		String uriPlusParams = appendParameters(uri, parameters);
		URL url = new URL(uriPlusParams);
		System.out.println("createConnection url : "+url);
		System.out.println("method : "+method);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);
		if (headers != null) {
			System.out.println("Headers : ");
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				System.out.println(entry.getKey()+":"+entry.getValue());
				connection.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}
		return connection;
	}

	private HttpURLConnection createConnection(String uri, String method, Map<String, String> headers)
			throws IOException {
		// Create connection...
		URL url = new URL(uri);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			connection.setRequestProperty(entry.getKey(), entry.getValue());
		}
		return connection;
	}

	public Future<ServiceResponse> getAsync(String uri, Map<String, String> headers) {
		return this.getAsync(uri, headers, null);
	}

	public Future<ServiceResponse> getAsync(String uri, Map<String, String> headers, Map<String, String> parameters) {
		return this.callAsync(GET, uri, headers, parameters, null);
	}

	public ServiceResponse get(String uri, Map<String, String> headers) throws ServiceCallerException {
		return get(uri, headers, null);
	}

	public ServiceResponse get(String uri, Map<String, String> headers, Map<String, String> parameters)
			throws ServiceCallerException {
		ServiceResponse response = null;
		try {
			// Create connection...
			HttpURLConnection connection = createConnection(uri, GET, headers, parameters);

			// Return response...
			response = getResponse(connection);
		} catch (Exception ex) {
			throw new ServiceCallerException(ex, response);
		}

		return response;
	}

	public Future<ServiceResponse> putAsync(String uri, Map<String, String> headers, String putData) {
		return this.putAsync(uri, headers, null, putData);
	}

	public Future<ServiceResponse> putAsync(String uri, Map<String, String> headers, Map<String, String> parameters,
			String putData) {
		return this.callAsync(PUT, uri, headers, parameters, putData);
	}

	public ServiceResponse put(String uri, Map<String, String> headers, String putData) throws ServiceCallerException {
		return put(uri, headers, null, putData);
	}

	public ServiceResponse put(String uri, Map<String, String> headers, Map<String, String> parameters, String putData)
			throws ServiceCallerException {
		ServiceResponse response = null;
		HttpURLConnection connection = null;
		DataOutputStream out = null;
		try {
			// Create connection...
			connection = createConnection(uri, PUT, headers, parameters);
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setFixedLengthStreamingMode(putData.getBytes().length);

			out = new DataOutputStream(connection.getOutputStream());
			out.write(putData.getBytes());
			out.flush();
			out.close();

			// Return response...
			response = getResponse(connection);
		} catch (Exception ex) {
			throw new ServiceCallerException(ex, response);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception ex2) {
				}
			}
		}

		return response;
	}

	public Future<ServiceResponse> postAsync(String uri, Map<String, String> headers, String postData) {
		return this.postAsync(uri, headers, null, postData);
	}

	public Future<ServiceResponse> postAsync(String uri, Map<String, String> headers, Map<String, String> parameters,
			String postData) {
		return this.callAsync(POST, uri, headers, parameters, postData);
	}

	public ServiceResponse post(String uri, Map<String, String> headers, String postData)
			throws ServiceCallerException {
		return post(uri, headers, null, postData);
	}

	public ServiceResponse post(String uri, Map<String, String> headers, Map<String, String> parameters,
			String postData) throws ServiceCallerException {
		ServiceResponse response = null;
		try {
			// Create connection...
			HttpURLConnection connection = createConnection(uri, POST, headers, parameters);
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			try (OutputStream os = connection.getOutputStream()) {
				os.write(postData.getBytes());
				os.flush();
				// Return response...
				response = getResponse(connection);
			}
		} catch (Exception ex) {
			throw new ServiceCallerException(ex, response);
		}

		return response;
	}

	public Future<ServiceResponse> deleteAsync(String uri, Map<String, String> headers) {
		return this.deleteAsync(uri, headers, null);
	}

	public Future<ServiceResponse> deleteAsync(String uri, Map<String, String> headers,
			Map<String, String> parameters) {
		return this.callAsync(DELETE, uri, headers, parameters, null);
	}

	public ServiceResponse delete(String uri, Map<String, String> headers) throws ServiceCallerException {
		return delete(uri, headers, null);
	}

	public ServiceResponse delete(String uri, Map<String, String> headers, Map<String, String> parameters)
			throws ServiceCallerException {
		ServiceResponse response = null;
		try {
			// Create connection...
			HttpURLConnection connection = createConnection(uri, DELETE, headers, parameters);

			// Return response...
			response = getResponse(connection);
		} catch (Exception ex) {
			throw new ServiceCallerException(ex, response);
		}

		return response;
	}

	/**
	 * Performs an asynchronous HTTP call using the provided criteria. Returns a {@link Future}<{@link ServiceResponse}> object.
	 * 
	 * @param method
	 * @param uri
	 * @param headers
	 * @param parameters
	 * @param body
	 * @param handler
	 */
	private Future<ServiceResponse> callAsync(String method, String uri, Map<String, String> headers,
			Map<String, String> parameters, String body) {
		AsyncRequest asyncClient = new AsyncRequest();
		asyncClient.setBody(body);
		asyncClient.setHeaders(headers);
		asyncClient.setMethod(method);
		asyncClient.setParameters(parameters);
		asyncClient.setServiceCaller(this);
		asyncClient.setUri(uri);

		ExecutorService executor = Executors.newSingleThreadExecutor();
		return executor.submit(asyncClient);
	}

	private String appendParameters(String uri, Map<String, String> parameters) throws UnsupportedEncodingException {
		String uriPlusParams = uri;
		if (parameters != null) {
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				if (sb.length() == 0) {
					sb.append('?');
				} else {
					sb.append('&');
				}
				sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
				sb.append('=');
				sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
			}
			uriPlusParams += sb.toString();
		}
		return uriPlusParams;
	}

	private ServiceResponse getResponse(HttpURLConnection connection) throws IOException {
		ServiceResponse response = null;
		if(connection != null) {
			int responseCode = connection.getResponseCode();
			java.io.InputStream is = null;
			java.io.InputStream es = null;
			
			if(responseCode < 400) {
				is = connection.getInputStream();
			}
			else {
				es = connection.getErrorStream();
			}
			
			response = new ServiceResponse(connection, responseCode, is, es);
		}
		
		return response;
	}

}

class AsyncRequest implements Callable<ServiceResponse> {
	private ServiceCaller serviceCaller;
	private String uri;
	private String method;
	private Map<String, String> headers;
	private Map<String, String> parameters;
	private String body;

	@Override
	public ServiceResponse call() throws Exception {
		ServiceResponse response = null;
		if (this.uri != null && this.method != null && this.serviceCaller != null) {
			if (this.method.equalsIgnoreCase("get")) {
				response = this.serviceCaller.get(this.uri, this.headers, this.parameters);
			} else if (this.method.equalsIgnoreCase("post")) {
				response = this.serviceCaller.post(this.uri, this.headers, this.parameters, this.body);
			} else if (this.method.equalsIgnoreCase("put")) {
				response = this.serviceCaller.put(this.uri, this.headers, this.parameters, this.body);
			} else if (this.method.equalsIgnoreCase("delete")) {
				response = this.serviceCaller.delete(this.uri, this.headers, this.parameters);
			}
		}

		return response;
	}

	public ServiceCaller getServiceCaller() {
		return this.serviceCaller;
	}

	public void setServiceCaller(ServiceCaller serviceCaller) {
		this.serviceCaller = serviceCaller;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
}
