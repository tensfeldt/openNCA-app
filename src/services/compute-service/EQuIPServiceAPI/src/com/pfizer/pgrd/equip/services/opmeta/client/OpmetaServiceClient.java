package com.pfizer.pgrd.equip.services.opmeta.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.pfizer.pgrd.equip.services.client.BaseClient;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.client.ServiceResponse;

public class OpmetaServiceClient extends BaseClient {
	private static final String APPLICATION_JSON = "application/json";

	public OpmetaServiceClient() throws ServiceCallerException {
		this(null, 0);
	}

	public OpmetaServiceClient(String host, int port) throws ServiceCallerException {
		super();
		this.setHost(host);
		this.setPort(port);
	}
	
	public List<AssignedUser> getAssignedUsers(String programCode, String study) throws ServiceCallerException {
		List<AssignedUser> users = new ArrayList<>();
		if(programCode != null && study != null) {
			String base = this.getBaseURI() + "/opmeta/nodes/programs/" + programCode + "/protocols/" + study;
			
			String pkaUri = base + "/assigned-pka-users";
			users.addAll(this.getUsers(pkaUri, "PK Analyst"));
			
			String cagUri = base + "/assigned-cag-users";
			users.addAll(this.getUsers(cagUri, "CAG"));
		}
		
		return users;
	}
	
	private List<AssignedUser> getUsers(String uri, String roleName) throws ServiceCallerException {
		List<AssignedUser> users = new ArrayList<>();
		if(uri != null) {
			Gson gson = new Gson();
			ServiceResponse sr = this.get(uri);
			if(sr != null && sr.getCode() > 199 && sr.getCode() < 300) {
				String json = sr.getResponseAsString();
				AssignedUserResult asr = gson.fromJson(json, AssignedUserResult.class);
				users = asr.users;
				for(AssignedUser user : users) {
					user.setRole(roleName);
				}
			}
		}
		
		return users;
	}
	
	/**
	 * Returns the {@link Program} object from the OPMeta service whose program code matches the one provided.
	 * Returns {@code null} if no object is found.
	 * @param programCode
	 * @return {@link Program}
	 * @throws ServiceCallerException
	 */
	public Program getProgram(String programCode) throws ServiceCallerException {
		Program p = null;
		if(programCode != null) {
			List<Program> list = this.getProgram(new ArrayList<>(Arrays.asList(programCode)));
			if(list.size() == 1) {
				p = list.get(0);
			}
		}
		
		return p;
	}
	
	/**
	 * Returns all {@link Program} objects from the OPMeta service whose program code matches any of the ones provided.
	 * @param studyIds
	 * @return {@link List}<{@link Program}>
	 * @throws ServiceCallerException
	 */
	public List<Program> getProgram(List<String> programCodes) throws ServiceCallerException {
		List<Program> all = this.getAllPrograms();
		List<Program> returnList = new ArrayList<>();
		for(Program p : all) {
			String code = null;
			for(String programCode : programCodes) {
				if(p.getProgramCode().equalsIgnoreCase(programCode)) {
					code = programCode;
					break;
				}
			}
			
			if(code != null) {
				returnList.add(p);
				programCodes.remove(code);
				
				if(programCodes.size() < 1) {
					break;
				}
			}
		}
		
		return returnList;
	}
	
	/**
	 * Returns all {@link Program} objects from the OPMeta service.
	 * @return {@link List}<{@link Program}>
	 * @throws ServiceCallerException
	 */
	public List<Program> getAllPrograms() throws ServiceCallerException {
		List<Program> programs = new ArrayList<>();
		String url = this.getBaseURI() + "/opmeta/nodes/programs";
		
		ServiceResponse sr = this.get(url);
		if (sr.getCode() == 200) {
			String json = sr.getResponseAsString();
			Gson gson = new Gson();
			OpmetaResponseBody orb = gson.fromJson(json, OpmetaResponseBody.class);
			programs = orb.getPrograms();
		}
		else {
			throw new ServiceCallerException("opmeta", sr.getCode(), url);
		}
		
		return programs;
	}
	
	/**
	 * Returns a {@link Protocol} object that has at least one alias matching the one provided. Returns {@link null} if no protocol could be found.
	 * @param alias
	 * @return {@link Protocol}
	 * @throws ServiceCallerException
	 */
	public Protocol getProtocolByAlias(String alias) throws ServiceCallerException {
		Map<String, Protocol> map = this.getProtocolByAlias(Arrays.asList(alias));
		return map.get(alias);
	}
	
	/**
	 * Returns a {@link Map} object, whose keys are the aliases provided, of {@link Protocol} objects whose aliases match any of the 
	 * ones provided.
	 * @param aliases
	 * @return {@link Map}<{@link String}, {@link Protocol}>
	 * @throws ServiceCallerException
	 */
	public Map<String, Protocol> getProtocolByAlias(List<String> aliases) throws ServiceCallerException {
		Map<String, Protocol> map = new HashMap<>();
		boolean done = false;
		if(aliases != null) {
			List<Program> allPrograms = this.getAllPrograms();
			for(Program program : allPrograms) {
				List<Protocol> protocols = program.getProtocols();
				if(protocols != null) {
					for(Protocol protocol : protocols) {
						List<ProtocolAlias> pas = protocol.getProtocolAliases();
						if(aliases != null) {
							String alias = null;
							for(ProtocolAlias a : pas) {
								for(String al : aliases) {
									if(a.getStudyAlias().equalsIgnoreCase(al)) {
										alias = al;
										break;
									}
								}
								
								if(alias != null) {
									map.put(alias, protocol);
									aliases.remove(alias);
									if(aliases.size() < 1) {
										done = true;
									}
									
									break;
								}
							}
						}
						
						if(done) {
							break;
						}
					}
				}
				
				if(done) {
					break;
				}
			}
		}
		
		return map;
	}
	
	public boolean isStudyBlinded(String studyId) throws ServiceCallerException {
		if(studyId != null) {
			String[] parts = studyId.split(":");
			if(parts.length == 2) {
				return this.isStudyBlinded(parts[0], parts[1]);
			}
			else {
				throw new IllegalArgumentException("The value provided is not a valid study ID. Study IDs must be in the form <programCode>:<protocol>.");
			}
		}
		
		return false;
	}
	
	public boolean isStudyBlinded(String program, String protocol) throws ServiceCallerException {
		return this.isStudyBlinded(null, program, protocol);
	}
	
	public boolean isStudyBlinded(String user, String program, String protocol) throws ServiceCallerException {
		boolean returnBoolean = false;
		if(user != null) {
			this.setUser(user);
		}
		
		String url = this.getBaseURI() + "/opmeta/nodes/programs/" + program + "/protocols/" + protocol;
		ServiceResponse sr = this.get(url);
		String json = sr.getResponseAsString();

		Gson gson = new Gson();
		OpmetaResponseBody orb = gson.fromJson(json, OpmetaResponseBody.class);
		String studyBlindingStatus = orb.getPrograms().get(0).getProtocols().get(0).getStudyBlindingStatus();
		returnBoolean = studyBlindingStatus.equalsIgnoreCase("blinded");
		return returnBoolean;
	}
	
	public List<String> getStudyAliases(String studyId) throws ServiceCallerException {
		return this.getStudyAliases(null, studyId);
	}
	
	public List<String> getStudyAliases(String user, String studyId) throws ServiceCallerException {
		if(user != null) {
			this.setUser(user);
		}
		
		String url = this.getBaseURI() + "/opmeta/nodes/programs/" + studyId.substring(0, 4) + "/protocols/" + studyId;
		try {
			ServiceResponse sr = this.get(url);
			String json = sr.getResponseAsString();

			// if sr is nothing will need to search opmeta to get the actual program id
			if (sr.getCode() == 500) {
				return getProtocolAliasList(getAliasListWithoutProgram(studyId));
			}
			else {
				Gson gson = new Gson();
				OpmetaResponseBody orb = gson.fromJson(json, OpmetaResponseBody.class);
				List<ProtocolAlias> aliases = orb.getPrograms().get(0).getProtocols().get(0).getProtocolAliases();
				return getProtocolAliasList(aliases);
			}
		} catch (Exception e) {
			String str = "GET: " + url + " failed: " + e.getMessage();
			throw new IllegalStateException(str);
		}
	}
	
	/**
	 * Returns the study ID associated with the provided alias.
	 * @param studyAlias the alias
	 * @return {@link String}
	 */
	public String getStudyIdByAlias(String studyAlias) {
		return this.getStudyIdByAlias(null, studyAlias);
	}

	// returns the studyId matching a given aliase. The endpoint will return a 500
	// error
	// if it finds multiple matching studyIds as this should not happen in the data.
	// So we
	// are assuming only 1 study id comes back
	/**
	 * Returns the study ID associated with the provided alias.
	 * @param user the user ID of the calling user
	 * @param studyAlias the alias
	 * @return {@link String}
	 */
	public String getStudyIdByAlias(String user, String studyAlias) {
		String studyIdValue = null;
		if(user != null) {
			this.setUser(user);
		}

		// GET /equip-services/nca/opmeta/aliases/B1521002
		String url = this.getBaseURI() + "/opmeta/aliases/" + studyAlias;
		try {
			ServiceResponse sr = this.get(url);
			String json = sr.getResponseAsString();

			Gson gson = new Gson();
			OpmetaSimpleResponseBody orb = gson.fromJson(json, OpmetaSimpleResponseBody.class);
			studyIdValue = orb.getStudyId();
		} catch (Exception e) {
			String str = "GET: " + url + " failed: " + e.getMessage();
			throw new IllegalStateException(str);
		}
		
		return studyIdValue;
	}
	
	public String updateProtocolModifiedDateAsync(String user, String studyId) {
		return updateProtocolModifiedDate(user, studyId, true);
	}
	
	public String updateProtocolModifiedDate(String user, String studyId) {
		return updateProtocolModifiedDate(user, studyId, false);
	}
	
	public String updateProtocolModifiedDate(String user, String studyId, boolean isAsync) {
		String program = null;
		String protocol = null;
		this.setUser(user);
		String response = "";
		
		// study id is form  B152:B1521002
		if( studyId != null) {
			String results[] = studyId.split(":");
			if( results.length > 1) {
				program = results[0];
				protocol = results[1];
			}
			// try to split if form is B5121002?
		}
		// PUT /equip-services/nca/opmeta/nodes/programs/B346/protocols/B3461031
		if( program != null && protocol != null ) {
			String url = this.getBaseURI() + "/opmeta/nodes/programs/" + program + "/protocols/" + protocol + "/master";
			try {
				if(!isAsync) {
					ServiceResponse sr = this.put(url, "{}");
					response = sr.getResponseAsString();
				}
				else {
					this.putAsync(url, "{}");
					return "Launched";
				}
			} catch (Exception e) {
				String str = "PUT:" + url + " failed: " + e.getMessage();
				throw new IllegalStateException(str, e);
			}
		}
		else {
			response = "invalid input study id" + studyId;
		}
		return response;
	}	

	@Override
	protected String getBaseURI() {
		return super.getBaseURI() + "/equip-services/" + this.getSystemId();
	}

	public List<ProtocolAlias> getAliasListWithoutProgram(String studyId) {
		String url = this.getBaseURI() + "/opmeta/nodes/programs/";
		try {
			ServiceResponse sr = this.get(url);
			String json = sr.getResponseAsString();
			Gson gson = new Gson();
			OpmetaResponseBody orb = gson.fromJson(json, OpmetaResponseBody.class);
			List<Program> programs = orb.getPrograms();
			for (Program program : programs) {
				List<Protocol> protocols = program.getProtocols();
				for (Protocol protocol : protocols) {
					List<ProtocolAlias> aliases = protocol.getProtocolAliases();
					for (ProtocolAlias alias : aliases) {
						if (alias.getStudyAlias().equals(studyId)) {
							// return all the aliases for this protocol
							return protocol.getProtocolAliases();
						}
					}
				}
			}
		}
		catch (Exception e) {
			String str = "GET: " + url + " failed: " + e.getMessage();
			throw new IllegalStateException(str);
		}
		return null;
	}

	private List<String> getProtocolAliasList(List<ProtocolAlias> aliases) {
		List<String> aliasList = new ArrayList<>();
		for (ProtocolAlias alias : aliases) {
			aliasList.add(alias.getStudyAlias());
		}
		return aliasList;
	}
}

class AssignedUserResult {
	public String response;
	public List<AssignedUser> users = new ArrayList<>();
}
