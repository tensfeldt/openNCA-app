package com.pfizer.modeshape.api.client;

import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.pfizer.equip.service.client.ServiceCaller;
import com.pfizer.equip.service.client.ServiceCallerException;
import com.pfizer.equip.service.client.ServiceResponse;

/**
 * Client for Modeshape's RESTful API
 * 
 * @author HeinemanWP
 *
 */
public class ModeshapeClient {
	private String baseUri;
	private String authorization;
	private ServiceCaller sc;
	
	public ModeshapeClient(String server, String username, String password) throws ServiceCallerException {
		baseUri = server + "/modeshape-rest/";
		authorization = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
		sc = new ServiceCaller();
	}

	public String createNode(String repository, String workspace, String path, String data) throws ModeshapeClientException {
		String uri = baseUri + repository + "/" + workspace + "/items/" + path;
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json;charset=UTF-8");
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		String postData = data;
		try {
			ServiceResponse sr = sc.post(uri, headers, parameters, postData);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ModeshapeClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ModeshapeClientException(ex);
		}
	}
	
	public String executeQuery(String repository, String workspace, String query, int offset, int limit) throws ModeshapeClientException {
		String uri = baseUri + repository + "/" + workspace + "/query";
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		headers.put("Content-Type", "application/jcr+sql2");
		headers.put("Accept", "application/json;charset=UTF-8");
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		parameters.put("offset", Integer.toString(offset));
		parameters.put("limit", Integer.toString(limit));
		String postData = query;
		try {
			ServiceResponse sr = sc.post(uri, headers, parameters, postData);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ModeshapeClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ModeshapeClientException(ex);
		}
	}
	
	public String retrieveNodeById(String repository, String workspace, String id) throws ModeshapeClientException {
		String uri = baseUri + repository + "/" + workspace + "/nodes/" + id;
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		// headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json;charset=UTF-8");
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		try {
			ServiceResponse sr = sc.get(uri, headers, parameters);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ModeshapeClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ModeshapeClientException(ex);
		}
	}
	
	public String retrieveNodeByPath(String repository, String workspace, String path) throws ModeshapeClientException {
		String uri = baseUri + repository + "/" + workspace + "/items" + path;
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json;charset=UTF-8");
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		try {
			ServiceResponse sr = sc.get(uri, headers, parameters);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ModeshapeClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ModeshapeClientException(ex);
		}
	}
	
	public String retrieveNodeByUri(String uri) throws ModeshapeClientException {
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		// headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json;charset=UTF-8");
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		try {
			ServiceResponse sr = sc.get(uri, headers, parameters);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ModeshapeClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ModeshapeClientException(ex);
		}
	}
	
	public String retrieveNodeOrProperty(String repository, String workspace, String path) throws ModeshapeClientException {
		String uri = baseUri + repository + "/" + workspace + "/items/" + path;
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		// headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json;charset=UTF-8");
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		try {
			ServiceResponse sr = sc.get(uri, headers, parameters);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ModeshapeClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ModeshapeClientException(ex);
		}
	}
	
	public String updateNodeOrProperty(String repository, String workspace, String path, String data) throws ModeshapeClientException {
		String uri = String.format("%s%s/%s/items/%s", baseUri, repository, workspace, path);
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json;charset=UTF-8");
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		String putData = data;
		try {
			ServiceResponse sr = sc.put(uri, headers, parameters, putData);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ModeshapeClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ModeshapeClientException(ex);
		}
	}

	public String updateNodeOrPropertyById(String repository, String workspace, String id, String data) throws ModeshapeClientException {
		String uri = String.format("%s%s/%s/nodes/%s", baseUri, repository, workspace, id);
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json;charset=UTF-8");
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		String putData = data;
		try {
			ServiceResponse sr = sc.put(uri, headers, parameters, putData);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ModeshapeClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ModeshapeClientException(ex);
		}
	}
	
	public InputStream retrieveBinary(String repository, String workspace, String path) throws ModeshapeClientException {
		return retrieveBinary(repository, workspace, path, null, null);
	}
	
	public InputStream retrieveBinary(String repository, String workspace, String path, String mimeType, String contentDisposition) throws ModeshapeClientException {
		String uri = baseUri + repository + "/" + workspace + "/binary/" + path;
		return retrieveBinaryFromUri(uri, mimeType, contentDisposition);
	}
	
	public InputStream retrieveBinaryFromUri(String uri) throws ModeshapeClientException {
		return retrieveBinaryFromUri(uri, null, null);
	}
	
	public InputStream retrieveBinaryFromUri(String uri, String mimeType, String contentDisposition) throws ModeshapeClientException {
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		headers.put("Accept", "application/json;charset=UTF-8");
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		if ((mimeType != null) && !mimeType.isEmpty()) {
			parameters.put("mimeType", mimeType);
		}
		if ((contentDisposition != null) && !contentDisposition.isEmpty()) {
			parameters.put("contentDisposition", contentDisposition);
		}
		try {
			ServiceResponse sr = sc.get(uri, headers, parameters);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getInputStream();
			} else {
				throw new ModeshapeClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ModeshapeClientException(ex);
		}
	}

	public String deleteNodeByPath(String repository, String workspace, String path) throws ModeshapeClientException {
		String uri = String.format("%s%s/%s/items/%s", baseUri, repository, workspace, path);
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json;charset=UTF-8");
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		try {
			ServiceResponse sr = sc.delete(uri, headers, parameters);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ModeshapeClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ModeshapeClientException(ex);
		}
	}

	public String deleteNodeById(String repository, String workspace, String id) throws ModeshapeClientException {
		String uri = String.format("%s%s/%s/nodes/%s", baseUri, repository, workspace, id);
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json;charset=UTF-8");
		headers.put("Accept-Charset", "utf-8");
		Map<String, String> parameters = new HashMap<>();
		try {
			ServiceResponse sr = sc.delete(uri, headers, parameters);
			int statusCode = sr.getCode();
			if ((statusCode >= 200) && (statusCode < 300)) {
				return sr.getResponseAsString();
			} else {
				throw new ModeshapeClientException(sr.getResponseAsString());
			}
		} catch (Exception ex) {
			throw new ModeshapeClientException(ex);
		}
	}

}
