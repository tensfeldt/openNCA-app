package com.pfizer.pgrd.modeshape.rest;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pfizer.pgrd.equip.dataframeservice.dto.CommentDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.MetadatumDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.dto.NTFile;
import com.pfizer.pgrd.equip.dataframeservice.dto.NTResource;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipCreated;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.exceptions.UnableToPersistException;
import com.pfizer.pgrd.modeshape.rest.query.JCRQueryResultSet;

public class ModeShapeClient {
	private static Logger log = LoggerFactory.getLogger(ModeShapeClient.class);		
	private static Gson GSON;
	
	private static int bufferSize = 65536;
	private String repositoryName;
	private String workspaceName;
	private String contextName;
	private String host;
	private Integer port;
	private boolean isHttps;

	private String username;
	private String password;
	
	// +-------------+
	// |   GETTING   |
	// +-------------+
	
	public ModeShapeClient() { }
	
	public ModeShapeClient(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public ModeShapeClient clone() {
		ModeShapeClient client = new ModeShapeClient();
		client.setContextName(this.getContextName());
		client.setHost(this.getHost());
		client.setHttps(this.isHttps());
		client.setPassword(this.getPassword());
		client.setPort(this.getPort());
		client.setRepositoryName(this.getRepositoryName());
		client.setUsername(this.getUsername());
		client.setWorkspaceName(this.getWorkspaceName());
		
		return client;
	}
	
	public ModeShapeNode getNode(String nodeId) {
		return this.getNode(ModeShapeNode.class, nodeId, true);
	}
	
	public ModeShapeNode getNode(String nodeId, boolean includeChildren) {
		return this.getNode(ModeShapeNode.class, nodeId, includeChildren);
	}
	
	public <T extends ModeShapeNode> T getNode(Class<T> c, String nodeId) {
		return this.getNode(c, nodeId, true);
	}
	
	public <T extends ModeShapeNode> T getNode(Class<T> c, String nodeId, boolean includeChildren) {
		T node = null;
		
		String json = this.getNodeJson(nodeId, includeChildren);
		if(json != null) {
			node = ModeShapeNode.unmarshal(json, c);
		}
		
		return node;
	}
	
	public ModeShapeNode getNodeByPath(String path, boolean includeChildren) {
		return this.getNodeByPath(ModeShapeNode.class, path, includeChildren);
	}
	
	public <T extends ModeShapeNode> T getNodeByPath(Class<T> c, String path, boolean includeChildren) {
		return this.getNodeByPath(c, path, includeChildren, false);
	}
	
	public ModeShapeNode getNodeByRelativePath(String relPath, boolean includeChildren) {
		return this.getNodeByRelativePath(ModeShapeNode.class, relPath, includeChildren);
	}
	
	public <T extends ModeShapeNode> T getNodeByRelativePath(Class<T> c, String path, boolean includeChildren) {
		return this.getNodeByPath(c, path, includeChildren, true);
	}
	
	private <T extends ModeShapeNode> T getNodeByPath(Class<T> c, String path, boolean includeChildren, boolean isRelPath) {
		T node = null;
		if(path != null) {
			String uri = path;
			if(isRelPath) {
				uri = this.getBaseUri() + "/items/" + path;
			}
			
			if(includeChildren) {
				uri += "?depth=-1";
			}
			
			String json = this.get(uri);
			if(json != null) {
				node = ModeShapeNode.unmarshal(json, c);
			}
		}
		
		return node;
	}
	
	public String getNodeJson(String nodeId) {
		return this.getNodeJson(nodeId, true);
	}
	
	public String getNodeJson(String nodeId, boolean includeChildren) {
		String json = null;
		if(nodeId != null) {
			String uri = nodeId;
			if(!nodeId.contains("http:") & !nodeId.contains("https:")) {
				uri = this.getBaseUri() + "/nodes/" + nodeId.trim();
			}
			
			if(includeChildren) {
				uri += "?depth=-1";
			}
			
			json = this.get(uri);
		}
		
		return json;
	}
	
	private String get(String uri) {
		String response = null;
		if (uri != null) {
			HttpURLConnection connection = null;
			try {
				connection = buildConnection(uri);
				connection.setRequestMethod("GET");
				
				response = readResponseString(connection);
				//System.out.println(response);
			} catch (ModeShapeAPIException  msae) {
				log.error("", msae);
				if( msae.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND ) {
					// just return null on 404
					//throw new RuntimeException("Resource not found");					
				}
				else {
					throw new RuntimeException("Issue in persistence layer upon retrieval");					
				}
			} catch (Exception e) {
				log.error("", e);
				throw new RuntimeException("Issue in persistence layer upon retrieval");
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		}
		
		return response;
	}
	
	public NTFile getComplexData(String complexDataId) {
		NTFile cd = null;
		if (complexDataId != null) {
			cd = this.getNode(NTFile.class, complexDataId);
		}
		
		return cd;
	}
	
	// +-------------+
	// |   POSTING   |
	// +-------------+
	
	public <T extends ModeShapeNode> T postNode(T node) throws ModeShapeAPIException {
		return this.postNode(node, false);
	}
	
	public <T extends ModeShapeNode> T postNode(T node, boolean asChild) throws ModeShapeAPIException {
		String path = this.getBaseUri() + "/items";
		return this.postNode(node, path, asChild);
	}
	
	public <T extends ModeShapeNode> T createFolderAndPostNode(T node, String folderNameToCreate) throws ModeShapeAPIException {
		return this.createFolderAndPostNode(node, folderNameToCreate, true);
	}
	
	public <T extends ModeShapeNode> T createFolderAndPostNode(T node, String folderNameToCreate, boolean asChild) throws ModeShapeAPIException {
		String folder = createFolderIfItDoesNotAlreadyExist(this.getBaseUri() + "/items", folderNameToCreate);
		String path = folder;
		if(asChild) {
			path += node.generateNodeName();
		}
		
		return this.postNode(node, path, asChild);
	}

	public String createFolderIfItDoesNotAlreadyExist(String path, String folderName) throws ModeShapeAPIException {
		path += "/" + folderName + "/";

		ModeShapeNode folder = getNodeByPath(path, false);
		
		if(folder == null) {
			folder = new ModeShapeNode();
			folder.setNodeName(folderName);
			postNode(folder, path, true);
		}
		
		return path;
	}
	
	public <T extends ModeShapeNode> T postNode(T node, String path) throws ModeShapeAPIException {
		return this.postNode(node, path, false);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ModeShapeNode> T postNode(T node, String path, boolean asChild) throws ModeShapeAPIException {
		T rNode = null;
		if(node != null && path != null) {
			if(node instanceof EquipCreated) {
				EquipCreated ec = (EquipCreated)node;
				if(ec.getCreated() == null) {
					ec.setCreated(new Date());
				}
			}
			
			String json = node.marshal(true, !asChild);
			try {
			json = this.postNode(json, path);
			if(json != null) {
				// Unfortunately, if the node is not a child, ModeShape will return the newly created 
				// node as a JSON array...
				if(!asChild) {
					Class c = node.getClass();
					T[] a = (T[])Array.newInstance(c, 0);
					a = ModeShapeNode.unmarshal(json, a);
					rNode = a[0];
				}
				else {
					rNode = (T) ModeShapeNode.unmarshal(json, node.getClass());
				}
			}
			}
			catch (ModeShapeAPIException maie) {
				this.delete(path);
				throw (maie);
			}

		}
		
		// In order for the JCR history to be correct, we have to make a no-op update call to each comment and metadata item.
		if(rNode != null) {
			if(rNode instanceof CommentDTO || rNode instanceof MetadatumDTO) {
				boolean deleted = false;
				if(rNode instanceof CommentDTO) {
					deleted = ((CommentDTO) rNode).isDeleted();
				}
				else {
					deleted = ((MetadatumDTO) rNode).isDeleted();
				}
				
				PropertiesPayload pp = new PropertiesPayload();
				pp.addProperty(ModeShapeNode.CommonProperties.DELETE_FLAG, deleted);
				this.updateNode(rNode.getJcrId(), pp, false);
			}
			
			List<ModeShapeNode> commentsMetadata = new ArrayList<>();
			for(ModeShapeNode child : rNode.getChildren()) {
				String nodeName = child.getNodeName();
				if(nodeName.startsWith(CommentDTO.PRIMARY_TYPE) || nodeName.startsWith("equip:metadatum") || nodeName.startsWith(MetadatumDTO.PRIMARY_TYPE)) {
					commentsMetadata.add(child);
				}
			}
			
			int nodesPerThread = 3;
			List<Updater> threads = new ArrayList<>();
			Updater currentUpdater = null;
			for(int i = nodesPerThread; i < commentsMetadata.size(); i++) {
				if(i % nodesPerThread == 0) {
					currentUpdater = new Updater();
					currentUpdater.client = this.clone();
					
					threads.add(currentUpdater);
				}
				
				currentUpdater.nodes.add(commentsMetadata.get(i));
			}
			
			for(Updater thread : threads) {
				thread.start();
			}
			
			for(int i = 0; i < nodesPerThread && i < commentsMetadata.size(); i++) {
				ModeShapeNode child = commentsMetadata.get(i);
				PropertiesPayload pp = new PropertiesPayload();
				pp.addProperty(ModeShapeNode.CommonProperties.DELETE_FLAG, false);
				
				try {
					this.updateNode(child.getJcrId(), pp, false);
				}
				catch(Exception e) { e.printStackTrace(); }
			}
			
			for(Updater thread : threads) {
				try {
					thread.join();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return rNode;
	}
	
	private class Updater extends Thread {
		public List<ModeShapeNode> nodes = new ArrayList<>();
		public ModeShapeClient client = null;
		
		@Override
		public void run() {
			for(ModeShapeNode child : this.nodes) {
				PropertiesPayload pp = new PropertiesPayload();
				pp.addProperty(ModeShapeNode.CommonProperties.DELETE_FLAG, false);
				
				try {
					client.updateNode(child.getJcrId(), pp, false);
				}
				catch(Exception e) { e.printStackTrace(); }
			}
		}
	}
	
	public String postNode(String json) throws ModeShapeAPIException {
		String path = this.getBaseUri() + "/items";
		return this.postNode(json, path);
	}
	
	public String postNode(String json, String path) throws ModeShapeAPIException {
		if(json != null && path != null) {
			byte[] data = json.getBytes();
			json = this.post(path, data, HTTPContentTypes.JSON);
		}
		
		return json;
	}
	
	public List<ModeShapeNode> postNode(List<ModeShapeNode> nodes, String path) throws ModeShapeAPIException {
		String[] paths = path.split("/items/");
		String basePath = paths[0];
		String childPath = paths[1];
		Map<String, JsonElement> map = new HashMap<>();
		for(ModeShapeNode node : nodes) {
			map.put(childPath + "/" + node.generateNodeName(), this.nodeToElement(node, true, false));
		}
		
		ModeShapeClient.initJson();
		String json = GSON.toJson(map);
		json = this.postNode(json, basePath);
		
		return new ArrayList<>();
	}
	
	public List<ModeShapeNode> postNode(Map<String, ModeShapeNode> nodes, String basePath) throws ModeShapeAPIException {
		Map<String, JsonElement> map = new HashMap<>();
		for(Entry<String, ModeShapeNode> e : nodes.entrySet()) {
			String path = e.getKey();
			ModeShapeNode node = e.getValue();
			map.put(path + "/" + node.generateNodeName(), this.nodeToElement(node, true, false));
		}
		
		ModeShapeClient.initJson();
		String json = GSON.toJson(map);
		json = this.postNode(json, basePath);
		
		return new ArrayList<>();
	}
	
	private static final void initJson() {
		if(GSON == null) {
			GsonBuilder gb = new GsonBuilder();
			gb.setPrettyPrinting();
			GSON = gb.create();
		}
	}
	
	private JsonElement nodeToElement(ModeShapeNode node, boolean includeChildren, boolean includeWrapper) {
		ModeShapeClient.initJson();
		
		JsonElement element = null;
		if(node != null) {
			List<ModeShapeNode> tempChildren = node.getChildren();
			node.setChildren(null);
			element = GSON.toJsonTree(node);
			node.setChildren(tempChildren);
			
			if (includeChildren && !node.getChildren().isEmpty()) {
				JsonArray array = new JsonArray();
				for (ModeShapeNode child : node.getChildren()) {
					JsonElement childNode = this.nodeToElement(child, includeChildren, true);
					array.add(childNode);
				}
				
				element.getAsJsonObject().add("children", array);
			}
			
			if (includeWrapper) {
				JsonObject wrapper = new JsonObject();
				String wrapperName = node.getNodeName();
				if(wrapperName == null) {
					wrapperName = node.generateNodeName();
				}
				
				wrapper.add(wrapperName, element);
				element = wrapper;
			}
		}
		
		return element;
	}
	
	// +--------------+
	// |   DELETING   |
	// +--------------+
	//this is only used to delete corrupt nodes on creation
	public String delete(String uri) throws ModeShapeAPIException {
		String response = null;
		if (uri != null) {
			HttpURLConnection connection = null;
			try {
				connection = buildConnection(uri);
				connection.setRequestMethod("DELETE");
				
				response = readResponseString(connection);
			} catch (ModeShapeAPIException maie ) {
				throw maie;
			} 
			catch (Exception e) {
				log.error("", e);
				throw new RuntimeException("Issue in persistence layer upon storage");
			}finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		}
		
		return response;
	}
	
	// +--------------+
	// |   UPDATING   |
	// +--------------+
	public String updateNode(ModeShapeNode node) throws ModeShapeAPIException {
		String json = null;
		if(node != null && node.getJcrId() != null) {
			json = this.updateNode(node, node.getJcrId());
		}
		
		return json;
	}
	
	public String updateNode(ModeShapeNode node, String nodeId) throws ModeShapeAPIException {
		return updateNode( node, nodeId, false);
	}

	public String updateNode(ModeShapeNode node, String nodeId, boolean includeChildren) throws ModeShapeAPIException {
		String json = null;
		if(node != null && nodeId != null) {
			json = node.marshal(includeChildren, false);
			json = this.updateNode(json, nodeId);
		}
		
		return json;
	}
	
	public String updateNode(String json, String nodeId) throws ModeShapeAPIException {
		String r = null;
		if(json != null && nodeId != null) {
			String path = this.getBaseUri() + "/nodes/" + nodeId.trim();
			byte[] data = json.getBytes();
			
			r = this.put(path, data, HTTPContentTypes.JSON);
		}
		
		return r;
	}
	
	public String updateNode(String nodeId, PropertiesPayload props) throws ModeShapeAPIException {
		return this.updateNode(nodeId, props, true);
	}
	
	public String updateNode(String nodeId, PropertiesPayload props, boolean updateModifiedProps) throws ModeShapeAPIException {
		String r = null;
		if(nodeId != null && props != null) {
			String json = props.marshal();
			String path = this.getBaseUri() + "/nodes/" + nodeId.trim();
			byte[] data = json.getBytes();
			
			r = this.put(path, data, HTTPContentTypes.JSON);
		}
		
		return r;
	}
	
	/**
	 * Returns a {@code byte[]} containing the binary contained by the node matching
	 * the provided ID.
	 * 
	 * @param nodeId
	 * @return {@code byte[]}
	 */
	public byte[] getBinary(String nodeId) {
		Object[] vals = this.getBinaryWithHeaders(nodeId);
		if(vals != null) {
			return (byte[]) vals[1];
		}
		
		return null;
	}
	
	public Object[] getBinaryWithHeaders(String nodeId) {
		Object[] content = null;
		if (nodeId != null) {
			NTResource node = this.getNode(NTResource.class, nodeId);
			if (node != null) {
				String uri = node.getJcrData();
				content = this.downloadWithHeaders(uri);
			}
		}

		return content;
	}

	/**
	 * Uploads the provided {@code byte[]} to the specified path. Returns
	 * {@code true} if successful, {@code false} otherwise.
	 * 
	 * @param uri
	 *            the destination
	 * @param binary
	 *            the binary
	 * @return {@code boolean} success
	 * @throws ModeShapeAPIException 
	 */
	public boolean uploadBinary(String nodeId, String propertyName, byte[] binary) throws ModeShapeAPIException {
		boolean successful = false;
		if (binary != null && propertyName != null) {
			ModeShapeNode node = getNode(nodeId);
			if (node != null) {
				String self = node.getSelf();
				String rootUri = getBaseUri() + "/items/";

				int startIndex = rootUri.length();
				int endIndex = self.length();
				String path = self.substring(startIndex, endIndex);
				
				String uploadUri = getBaseUri() + "/upload/" + path + "/" + propertyName;
				String json = this.post(uploadUri, binary, null);
				if (json != null) {
					successful = true;
				}

				//System.out.println(uploadUri);
			}
		}

		return successful;
	}
	
	/**
	 * Returns a {@code byte[]} containing binary data returned from a GET request
	 * from the provided URI.
	 * 
	 * @param uri
	 * @return {@code byte[]}
	 */
	private byte[] download(String uri) {
		Object[] vals = this.downloadWithHeaders(uri);
		if(vals != null) {
			return (byte[]) vals[1];
		}
		
		return null;
	}
	
	private Object[] downloadWithHeaders(String uri) {
		Object[] values = null;
		if (uri != null) {
			HttpURLConnection connection = null;
			try {
				connection = buildConnection(uri);
				connection.setRequestMethod("GET");
				values = this.readResponseWithHeaders(connection);
				//System.out.println(content);
			} catch (Exception e) {
				log.error("", e);
				throw new RuntimeException("Issue in persistence layer upon download");
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		}
		
		return values;
	}

	/**
	 * Returns the response from a POST request to the provided URI, uploading the
	 * provided JSON.
	 * 
	 * @param uri
	 *            the URI
	 * @param json
	 *            the JSON
	 * @return {@link String} the JSON response
	 * @throws ModeShapeAPIException 
	 */
	private String post(String uri, String json) throws ModeShapeAPIException {
		String response = null;
		if (json != null) {
			System.out.println("Payload: " + json);
			response = this.post(uri, json.getBytes(), HTTPContentTypes.JSON);
		}

		return response;
	}
	
	/**
	 * Returns the response from a PUT request to the provided URI, uploading the
	 * provided JSON. Need to be PUT to increment modeshape versioning
	 * 
	 * @param uri
	 *            the URI
	 * @param json
	 *            the JSON
	 * @return {@link String} the JSON response
	 * @throws ModeShapeAPIException 
	 */
	private String put(String uri, String json) throws ModeShapeAPIException {
		String response = null;
		if (json != null) {
			response = this.put(uri, json.getBytes(), HTTPContentTypes.JSON);
		}

		return response;
	}

	/**
	 * Returns the response from a POST request to the provided URI, uploading the
	 * provided bytes.
	 * 
	 * @param uri
	 *            the URI
	 * @param data
	 *            the bytes
	 * @return {@link String} the JSON response
	 * @throws ModeShapeAPIException 
	 */
	private String post(String uri, byte[] data, String contentType) throws ModeShapeAPIException {
		String response = null;
		DataOutputStream out = null;
		
		if (uri != null && data != null) {
			HttpURLConnection connection = null;
			try {
				connection = buildConnection(uri);
				connection.setDoOutput(true); // automatically makes this a POST request
				connection.setRequestMethod("POST");
				connection.setFixedLengthStreamingMode(data.length);
				if (contentType != null) {
					connection.setRequestProperty(HTTPHeaders.CONTENT_TYPE, contentType);
				}

				out = new DataOutputStream(connection.getOutputStream());
				out.write(data);
				out.flush();
				out.close();

				response = this.readResponseString(connection);
				//System.out.println( response );
			}
			catch(ModeShapeAPIException maie) {
				maie.setModeShapeErrorMessage("Node was not able to be created");
				throw maie;
			}
			catch (Exception e) {
				log.error("", e);
				throw new UnableToPersistException(e.getMessage());
			} finally {
				try{
					if (out != null) {
						out.close();
					}
				}
				catch( Exception ex ){
					log.error("", ex);
				}
				try{
					if (connection != null) {
						connection.disconnect();
					}
				}
				catch( Exception ex ){
					log.error("", ex);
				}
			}
		}

		return response;
	}

	private String put(String uri, byte[] data, String contentType) throws ModeShapeAPIException {
		String response = null;
		if (uri != null && data != null) {
			HttpURLConnection connection = null;
			try {
				connection = buildConnection(uri);
				connection.setDoOutput(true); // automatically makes this a PUT request
				connection.setRequestMethod("PUT");
				connection.setFixedLengthStreamingMode(data.length);
				if (contentType != null) {
					connection.setRequestProperty(HTTPHeaders.CONTENT_TYPE, contentType);
				}

				DataOutputStream out = new DataOutputStream(connection.getOutputStream());
				out.write(data);
				out.flush();
				out.close();

				response = this.readResponseString(connection);
			} 
			catch(ModeShapeAPIException maie) {
				throw maie;
			}
			catch (Exception e) {
				log.error("", e);
				throw new RuntimeException("Issue in persistence layer upon storage");
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		}

		return response;
	}
	
	/**
	 * Returns the response from the provided connection.
	 * 
	 * @param connection
	 * @return {@link String}
	 * @throws ModeShapeAPIException 
	 */
	private String readResponseString(HttpURLConnection connection) throws ModeShapeAPIException {
		String response = null;
		if (connection != null) {
			byte[] b = this.readResponse(connection);
			if (b != null) {
				response = new String(b);
			}
		}

		return response;
	}

	/**
	 * Reads the response from the provided {@link HttpURLConnection} into a
	 * {@link String}.
	 * 
	 * @param connection
	 *            the connection
	 * @return {@link String} the response
	 * @throws ModeShapeAPIException 
	 */
	private byte[] readResponse(HttpURLConnection connection) throws ModeShapeAPIException {
		Object[] vals = this.readResponseWithHeaders(connection);
		if(vals != null) {
			return (byte[]) vals[1];
		}
		
		return null;
	}
	
	/**
	 * Reads the response from the provided {@link HttpURLConnection} into a
	 * {@link String}.
	 * 
	 * @param connection
	 *            the connection
	 * @return {@link String} the response
	 * @throws ModeShapeAPIException 
	 */
	private Object[] readResponseWithHeaders(HttpURLConnection connection) throws ModeShapeAPIException {
		Object[] values = null;
		byte[] response = null;
		if (connection != null) {
			InputStream responseStream = null;
			try {
				//System.out.println("Getting response from " + connection.getRequestMethod() + ": " + connection.getURL());
				responseStream = connection.getInputStream();
				Map<String, List<String>> headers = connection.getHeaderFields();
				response = this.getResponseDataAsByteArray(responseStream);
				
				values = new Object[] {headers, response};
			} catch (IOException ioe) {
				ModeShapeAPIException maie = this.createException(connection);
				log.error("",ioe);
				throw maie;
				//System.err.println(maie);
			}
			catch (Exception e) {
				log.error("", e);
				throw new RuntimeException("Issue in persistence layer upon response");
			}
			finally {
				try {
					if (responseStream != null) {
						responseStream.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return values;
	}
	
	private ModeShapeAPIException createException(HttpURLConnection connection) {
		ModeShapeAPIException maie = null;
		if(connection != null) {
			maie = new ModeShapeAPIException();
			maie.setHttpMethod(connection.getRequestMethod());
			maie.setUri(connection.getURL().toString());
			
			try {
				maie.setStatusCode(connection.getResponseCode());
				InputStream errorStream = connection.getErrorStream();
				if(errorStream != null) {
					String errorMessage = this.getResponseDataAsString(errorStream);
					maie.setModeShapeErrorMessage(errorMessage);
				}
			}
			catch(Exception ex) { 
				//intentionally left blank
			}
		}
		
		return maie;
	}
	

	/**
	 * Returns a {@link String} object created from the data within the provided
	 * {@link InputStream} object.
	 * 
	 * @param is
	 *            the InputStream
	 * @return {@link String}
	 * @throws IOException
	 */
	private String getResponseDataAsString(InputStream is) throws IOException {
		byte[] bytes = getResponseDataAsByteArray(is);
		return new String(bytes);
	}

	/**
	 * Returns the data within the provided {@link InputStream} as a {@code byte[]}.
	 * 
	 * @param is
	 *            the InputStream
	 * @return {@code byte[]} the data
	 * @throws IOException
	 */
	private byte[] getResponseDataAsByteArray(InputStream is) throws IOException {
		// Looks complex (and it is), but it is designed to handle very large input
		// streams.
		List<byte[]> buffers = new ArrayList<>();
		int count = 0;
		ByteBuffer bb = ByteBuffer.allocate(bufferSize);
		do {
			bb.clear();
			int offset = 0;
			int size = bufferSize;
			do {
				count = is.read(bb.array(), offset, size - offset);
				if (count > 0) {
					offset += count;
				}
			}
			while ((count > 0) && (offset < size));

			if (offset > 0) {
				byte[] buffer = new byte[offset];
				bb.get(buffer);
				buffers.add(buffer);
			}
		}
		while (count > -1);

		int arraySize = 0;
		for (byte[] buffer : buffers) {
			arraySize += buffer.length;
		}

		ByteBuffer ob = ByteBuffer.allocate(arraySize);
		for (byte[] buffer : buffers) {
			ob.put(buffer, 0, buffer.length);
		}

		return ob.array();
	}
	
	public JCRQueryResultSet query(String sql) throws ModeShapeAPIException {
		JCRQueryResultSet resultSet = null;
		if(sql != null) {
			//System.out.println("Query:" + sql);
			
			String uri = this.getBaseUri() + "/query";
			byte[] d = sql.getBytes();
			String json = this.post(uri, d, HTTPContentTypes.JCR_SQL);
			
			if(json != null) {
				resultSet = JCRQueryResultSet.unmarshal(json);
			}
		}
		
		return resultSet;
	}
	
	/**
	 * Returns an {@link HttpURLConnection} object pre-configured with default
	 * headers.
	 * 
	 * @param uri
	 *            the URL for the connection
	 * @return {@link HttpURLConnection}
	 * @throws IOException
	 */
	private HttpURLConnection buildConnection(String uri) throws IOException {
		HttpURLConnection connection = null;
		if (uri != null) {
			URL url = new URL(uri);
			connection = (HttpURLConnection) url.openConnection();
			String b64 = this.toBase64(this.username + ":" + this.password);
			connection.setRequestProperty(HTTPHeaders.AUTHORIZATION, "Basic " + b64);
			connection.setRequestProperty(HTTPHeaders.ACCEPT, HTTPContentTypes.JSON);
		}

		return connection;
	}
	
	/**
	 * Returns the provided {@link String} as a base64 representation.
	 * 
	 * @param s
	 * @return
	 */
	private String toBase64(String s) {
		String b64 = Base64.getEncoder().encodeToString(s.getBytes());
		return b64;
	}
	
	/**
	 * Returns the base URI for all ModeShape REST API calls in the format
	 * http&#60;s>://&#60;host>:&#60;port>/&#60;context>/&#60;repository>/&#60;workspace>.
	 * 
	 * @return {@link String} the base URI
	 */
	public String getBaseUri() {
		String baseUri = "http";
		if (this.isHttps) {
			baseUri += "s";
		}

		baseUri += "://" + this.host;

		if (this.port != null) {
			baseUri += ":" + this.port;
		}

		baseUri += "/" + this.contextName;
		baseUri += "/" + this.repositoryName;
		baseUri += "/" + this.workspaceName;

		return baseUri;
	}

	public String getRepositoryName() {
		return repositoryName;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public String getWorkspaceName() {
		return workspaceName;
	}

	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

	public String getContextName() {
		return contextName;
	}

	public void setContextName(String contextName) {
		this.contextName = contextName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public boolean isHttps() {
		return isHttps;
	}

	public void setHttps(boolean isHttps) {
		this.isHttps = isHttps;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}