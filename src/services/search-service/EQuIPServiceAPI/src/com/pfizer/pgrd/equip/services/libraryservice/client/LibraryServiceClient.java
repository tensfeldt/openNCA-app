package com.pfizer.pgrd.equip.services.libraryservice.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.pfizer.pgrd.equip.services.client.BaseClient;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.client.ServiceCallerUtils;
import com.pfizer.pgrd.equip.services.client.ServiceResponse;
import com.pfizer.pgrd.equip.services.libraryservice.dto.LibraryResponse;
import com.pfizer.pgrd.equip.services.libraryservice.dto.Script;

public class LibraryServiceClient extends BaseClient {
	private static final Gson GSON = new Gson();
	
	public LibraryServiceClient() throws ServiceCallerException { this(null, 0); }
	
	public LibraryServiceClient(String host, int port) throws ServiceCallerException {
		super();
		this.setHost(host);
		this.setPort(port);
	}
	
	/**
	 * Returns a {@link LibraryResponse} containing information on the script matching the provided {@code scriptName} found in the 
	 * {@code /global/system-scripts} directory.
	 * @param scriptName the name of the script
	 * @return {@link LibraryResponse} or {@code null} if no script is found with matching the provided name
	 * @throws ServiceCallerException 
	 */
	public LibraryResponse getGlobalSystemScriptByName(String scriptName) throws ServiceCallerException {
		return this.getScriptByName("/global/system-scripts", scriptName);
	}
	
	/**
	 * Returns a {@link List} of {@link LibraryResponse} objects containing information on the script matching any of the provided script names found in the 
	 * {@code /global/system-scripts} directory.
	 * @param scriptNames
	 * @return {@link List}<{@link LibraryResponse}>
	 * @throws ServiceCallerException
	 */
	public List<LibraryResponse> getGlobalSystemScriptByName(List<String> scriptNames)  throws ServiceCallerException {
		List<LibraryResponse> list = new ArrayList<>();
		for(String name : scriptNames) {
			if(name != null) {
				name = name.trim();
				LibraryResponse lr = this.getGlobalSystemScriptByName(name);
				if(lr != null) {
					list.add(lr);
				}
			}
		}
		
		return list;
	}
	
	/**
	 * Returns a {@link LibraryResponse} containing information on the script matching the provided {@code scriptName} found in the 
	 * provided directory.
	 * @param directory the directory containing the script
	 * @param scriptName the name of the script
	 * @return {@link LibraryResponse} or {@code null} if no script is found with matching the provided name in the provided directory
	 * @throws ServiceCallerException 
	 */
	public LibraryResponse getScriptByName(String directory, String scriptName) throws ServiceCallerException {
		LibraryResponse response = null;
		if(directory != null && !directory.isEmpty() && scriptName != null && !scriptName.isEmpty()) {
			String uri = this.getBaseURI() + directory + "/" + scriptName;
			ServiceResponse sr = this.get(uri);
			String json = sr.getResponseAsString();
			response = GSON.fromJson(json, LibraryResponse.class);
		}
		
		return response;
	}
	
	/**
	 * Returns a {@link LibraryResponse} containing information on the script matching the provided {@code id}.
	 * @param id the id of the script
	 * @return {@link LibraryResponse} or {@code null} if no script is found with matching the provided id.
	 * @throws ServiceCallerException 
	 */
	public LibraryResponse getScriptById(String id) throws ServiceCallerException {
		LibraryResponse response = null;
		if(id != null && !id.isEmpty()) {
			String uri = this.getBaseURI();
			uri = uri.substring(0, uri.indexOf("/library"));
			uri += "/id/" + id;
			ServiceResponse sr = this.get(uri);
			String json = sr.getResponseAsString();
			response = GSON.fromJson(json, LibraryResponse.class);
		}
		return response;
	}
	
	/**
	 * Returns the contents of a script as a byte array for the script matching the provide {@code id}
	 * @param id
	 * @return
	 * @throws ServiceCallerException
	 * @throws IOException
	 */
	public byte[] getItemContent(String id) throws ServiceCallerException, IOException {
		if(id != null && !id.isEmpty()) {
			String uri = this.getBaseURI();
			uri = uri.substring(0, uri.indexOf("/current/library"));
			uri += "/content/current/id/" + id;
			ServiceResponse sr = this.get(uri);
			if (sr.getCode() >= 200 && sr.getCode() < 300) {
				return ServiceCallerUtils.getResponseDataAsByteArray(sr);
			} else {
				String response = sr.getResponseAsString();
				String message = response.isEmpty() ? "" : " - " + response;
				throw new ServiceCallerException("Library Service", sr.getCode(), message);
			}
		}
		return null;
	}
	
	@Override
	protected String getBaseURI() {
		return super.getBaseURI() + "/equip-services/" + this.getSystemId() + "/librarian/artifact/current/library";
	}
}
