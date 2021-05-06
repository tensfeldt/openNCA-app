package com.pfizer.pgrd.equip.dataframe.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;

public class DataframeServiceClient {
	private String username;
	private String surrogatedUser;
	private boolean isExternalUser = false;
	private String server;
	private int port;
	private boolean isHttps;
	private String systemId = "nca";
	
	public boolean isExternalUser() {
		return isExternalUser;
	}

	public void setExternalUser(boolean isExternalUser) {
		this.isExternalUser = isExternalUser;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public boolean isHttps() {
		return isHttps;
	}

	public void setHttps(boolean isHttps) {
		this.isHttps = isHttps;
	}
	
	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public Dataframe getDataframe(String id) throws IOException {
		Dataframe d = null;
		if(id != null) {
			List<String> ids = new ArrayList<>();
			ids.add(id);
			
			List<Dataframe> list = this.getDataframe(ids);
			if(list.size() == 1) {
				d = list.get(0);
			}
		}
		
		return d;
	}
	
	public List<Dataframe> getDataframe(List<String> ids) throws IOException {
		List<Dataframe> dataframes = new ArrayList<>();
		if(ids != null) {
			String json = this.toJson(ids);
			String path = "dataframes/list";
			String r = this.postAsString(path, json);
			
			Dataframe[] dfs = this.parseJson(r, Dataframe[].class);
			for(Dataframe df : dfs) {
				dataframes.add(df);
			}
		}
		
		return dataframes;
	}
	
	public Assembly getAssembly(String id) throws IOException {
		List<String> ids = new ArrayList<>();
		ids.add(id);
		
		List<Assembly> list = this.getAssembly(ids);
		Assembly a = null;
		if(list.size() == 1) {
			a = list.get(0);
		}
		
		return a;
	}
	
	public List<Assembly> getAssembly(List<String> ids) throws IOException {
		List<Assembly> assemblies = new ArrayList<>();
		if(ids != null) {
			String json = this.toJson(ids);
			String path = "assemblies/list";
			String r = this.postAsString(path, json);
			
			Assembly[] as = this.parseJson(r, Assembly[].class);
			for(Assembly a : as) {
				assemblies.add(a);
			}
		}
		
		return assemblies;
	}
	
	public EquipObject getItem(String id) {
		return null;
	}
	
	public List<EquipObject> getItemByEquipId(String equipId) {
		return new ArrayList<>();
	}
	
	public List<Assembly> getReportingEventByEquipId(String equipid) throws IOException {
		List<Assembly> assemblies = new ArrayList<>();
		if(equipid != null) {
			String path = "assemblies/equipId/" + equipid;
			String r = this.getAsString(path);
			
			Assembly[] as = this.parseJson(r, Assembly[].class);
			for(Assembly a : as) {
				assemblies.add(a);
			}
		}
		
		return assemblies;
	}
	
	public ReportingEventItem getReportingEventItem(String id) throws IOException {
		List<String> ids = new ArrayList<>();
		ids.add(id);
		
		List<ReportingEventItem> items = this.getReportingEventItem(ids);
		ReportingEventItem item = null;
		if(items.size() == 1) {
			item = items.get(0);
		}
		
		return item;
	}
	
	public List<ReportingEventItem> getReportingEventItem(List<String> ids) throws IOException {
		List<ReportingEventItem> items = new ArrayList<>();
		if(ids != null) {
			String json = this.toJson(ids);
			String path = "reportingeventitems/list";
			String r = this.postAsString(path, json);
			
			ReportingEventItem[] reis = this.parseJson(r, ReportingEventItem[].class);
			for(ReportingEventItem rei : reis) {
				items.add(rei);
			}
		}
		
		return items;
	}
	
	public List<ReportingEventItem> getReportingEventItemByDataframeId(String dataframeId) throws IOException {
		List<ReportingEventItem> items = new ArrayList<>();
		if(dataframeId != null) {
			String path = "dataframes/" + dataframeId + "/reportingEventItems";
			String r = this.getAsString(path);
			
			ReportingEventItem[] reis = this.parseJson(r, ReportingEventItem[].class);
			for(ReportingEventItem rei : reis) {
				items.add(rei);
			}
		}
		
		return items;
	}
	
	public byte[] getDatasetDataByComplexId(String complexId) throws IOException {
		byte[] bytes = new byte[0];
		if(complexId != null) {
			String path = "dataframes/data/" + complexId;
			bytes = this.get(path);
		}
		
		return bytes;
	}
	
	private <T> T parseJson(String json, Class<T> classOfT) {
		T t = null;
		Gson gson = new Gson();
		t = gson.fromJson(json, classOfT);
		
		return t;
	}
	
	private String toJson(Object o) {
		GsonBuilder gb = new GsonBuilder();
		gb.setPrettyPrinting();
		Gson gson = gb.create();
		
		return gson.toJson(o);
	}
	
	private byte[] get(String path) throws IOException {
		return this.call(path, "GET", null);
	}
	
	private String getAsString(String path) throws IOException {
		byte[] bytes = this.get(path);
		return new String(bytes);
	}
	
	private String postAsString(String path, String json) throws IOException {
		byte[] bytes = this.post(path, json);
		return new String(bytes);
	}
	
	private byte[] post(String path, String json) throws IOException {
		return this.call(path, "POST", json);
	}
	
	private byte[] call(String path, String method, String body) throws IOException {
		byte[] bytes = null;
		String http = "http";
		if(this.isHttps) {
			http += "s";
		}
		
		String uri = http + "://" + this.server + ":" + this.port + "/EQuIPDataframeService/" + this.systemId + "/" + path;
		URL url = new URL(uri);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);
		if(this.username != null) {
			connection.setRequestProperty("IAMPFIZERUSERCN", this.username);
		}
		if(this.surrogatedUser != null) {
			connection.setRequestProperty("External-User", this.surrogatedUser);
		}
		
		if(method.equalsIgnoreCase("POST")) {
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			
			try (OutputStream os = connection.getOutputStream()) {
				os.write(body.getBytes());
				os.flush();
				// Return response...
				bytes = getResponse(connection);
			}
		}
		else {
			bytes = getResponse(connection);
		}
		
		return bytes;
	}
	
	private byte[] getResponse(HttpURLConnection connection) throws IOException {
		InputStream is = connection.getInputStream();
		int bufferSize = 65536;
		
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
	
	private String getResponseAsString(HttpURLConnection connection) throws IOException {
		byte[] bytes = this.getResponse(connection);
		return new String(bytes);
	}

	public String getSurrogatedUser() {
		return surrogatedUser;
	}

	public void setSurrogatedUser(String surrogatedUser) {
		this.surrogatedUser = surrogatedUser;
	}
}
