package com.pfizer.pgrd.equip.services.authorization.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.services.client.BaseClient;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.client.ServiceResponse;

public class AuthorizationServiceClient extends BaseClient {
	private static final Gson GSON = new Gson();

	public AuthorizationServiceClient() throws ServiceCallerException {
		this(null, 0, null);
	}

	public AuthorizationServiceClient(String host, int port, String systemId) throws ServiceCallerException {
		super();
		this.setHost(host);
		this.setPort(port);
		this.setSystemId(systemId);
	}

	public static final AuthorizationServiceClient getClient(String host, int port, String systemId)
			throws ServiceCallerException {
		return new AuthorizationServiceClient(host, port, systemId);
	}

	/**
	 * Returns a {@link List} of {@link AuthorizationRequestBody} objects created
	 * from the provided {@link Dataframe} objects.
	 * 
	 * @param dataframes
	 * @return {@link List}<{@link AuthorizationRequestBody}>
	 */
	public static final List<AuthorizationRequestBody> createAuthRequest(List<Dataframe> dataframes) {
		List<AuthorizationRequestBody> list = new ArrayList<>();
		for (Dataframe df : dataframes) {
			AuthorizationRequestBody body = AuthorizationServiceClient.createAuthRequest(df, false);
			if (body != null) {
				list.add(body);
			}
		}

		return list;
	}

	/**
	 * Returns an {@link AuthorizationRequestBody} object created from the provided
	 * {@link Dataframe} object.
	 * 
	 * @param dataframe
	 * @param useEquipId
	 * @return {@link AuthorizationRequestBody}
	 */
	public static final AuthorizationRequestBody createAuthRequest(Dataframe dataframe, boolean useEquipId) {
		AuthorizationRequestBody body = null;
		if (dataframe != null) {
			body = new AuthorizationRequestBody();
			body.setDataBlindingStatus(dataframe.getDataBlindingStatus());
			body.setDataframeType(dataframe.getDataframeType());
			body.setPromotionStatus(dataframe.getPromotionStatus());
			body.setRestrictionStatus(dataframe.getRestrictionStatus());
			body.setstudyIds(dataframe.getStudyIds());

			if (useEquipId) {
				body.setDataframeId(dataframe.getEquipId());
			} else {
				body.setDataframeId(dataframe.getId());
			}
		}

		return body;
	}

	public AuthorizationResponseBody getDataframePrivileges(String user, Dataframe dataframe)
			throws ServiceCallerException {
		AuthorizationRequestBody body = AuthorizationServiceClient.createAuthRequest(dataframe, true);
		String json = new Gson().toJson(body);
		return this.getDataframePrivileges(user, json);
	}

	public AuthorizationResponseBody getDataframePrivileges(String user, List<Dataframe> dataframes)
			throws ServiceCallerException {
		List<AuthorizationRequestBody> body = AuthorizationServiceClient.createAuthRequest(dataframes);
		String json = new Gson().toJson(body);
		return this.getMultipleDataframePrivileges(user, json);
	}

	public AuthorizationResponseBody getDataframePrivileges(String user, String authorizationRequestBody)
			throws ServiceCallerException {
		return this.getDataframePrivileges(user, authorizationRequestBody, false);
	}

	public AuthorizationResponseBody getMultipleDataframePrivileges(String user, String authorizationRequestBody)
			throws ServiceCallerException {
		return this.getDataframePrivileges(user, authorizationRequestBody, true);
	}

	public AuthorizationResponseBody getDataframePrivileges(String user, String authorizationRequestBody,
			boolean isMultiple) throws ServiceCallerException {
		String url = this.getBaseURI() + "/dataframe/check-access";
		if (isMultiple) {
			url += "es";
		}

		this.setUser(user);
		// comment by mh
		ServiceResponse sr = this.post(url, authorizationRequestBody);
		AuthorizationResponseBody rb = null;
		String json = sr.getResponseAsString();
		if (sr.getCode() == 200) {
			rb = GSON.fromJson(json, AuthorizationResponseBody.class);
		} else {
			throw new ServiceCallerException("Authorization Service", sr.getCode(), json);
		}

		return rb;
	}

	public List<String> getPrivileges(String username) {
		List<String> privs = new ArrayList<>();
		if (username != null) {
			String url = this.getBaseURI() + "/users/" + username + "/privileges";

			try {
				ServiceResponse sr = this.get(url);
				String json = sr.getResponseAsString();
				AuthorizationPrivilegesResponse apr = GSON.fromJson(json, AuthorizationPrivilegesResponse.class);
				privs = apr.getPrivileges();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return privs;
	}

	public boolean checkAuthorization(List<String> privileges, String username) throws ServiceCallerException {
		// get the user privileges and make sure all the listed values are included
		this.setUser(username);
		// get list of privileges
		// if null then allow it
		if (privileges.isEmpty()) {
			return true;
		}
		// http://equip-services-dev:8080/equip-services/nca/users/gavanr01/privileges

		String url = this.getBaseURI() + "/users/" + username + "/privileges";

		ServiceResponse sr = this.get(url);
		String json = sr.getResponseAsString();
		AuthorizationPrivilegesResponse apr = GSON.fromJson(json, AuthorizationPrivilegesResponse.class);
		List<String> privlist = apr.getPrivileges();
		boolean isOk = false;
		
		String self = "_SELF";
		List<String> handledSelfs = new ArrayList<>();
		if (privlist != null) {
			// now check to see if the necessary privileges are on the list
			for (String privilege : privileges) {
				String selfPriv = privilege;
				if(!selfPriv.endsWith(self)) {
					selfPriv += self;
				}
				
				if(handledSelfs.contains(privilege)) {
					continue;
				}
				
				handledSelfs.add(selfPriv);
				if (privlist.contains(privilege) || privlist.contains(selfPriv)) {
					isOk = true;
				} else if (privilege.equalsIgnoreCase("RESTORE_USER_ENTITY") && privlist.contains("RESTORE_ANY_ENTITY")) {
					// special case - if user is updating their own node then the "any entity" is
					// also acceptable
					isOk = true;
				} else {
					isOk = false;
				}
				
				if (!isOk) {
					break;
				}
			}
		} else {
			isOk = true;
		}
		
		return isOk;
	}

	public String getAccessGroupName(String protocolId, String userId) throws ServiceCallerException {
		// http://{{hostname}}:8080/equip-services/nca/entities/PROTOCOL/B1521005/access
		String url = this.getBaseURI() + "/entities/PROTOCOL/" + protocolId + "/access";

		this.setUser(userId);
		String groupName = "";
		ServiceResponse sr = this.get(url);
		String json = sr.getResponseAsString();
		AuthorizationBlindingAccess aba = GSON.fromJson(json, AuthorizationBlindingAccess.class);

		if (aba.getGroupDetails() == null)
			return "Not Found";
		List<GroupDetail> details = aba.getGroupDetails();
		if (details.isEmpty())
			return "Not Found";

		groupName = details.get(0).getGroupName();

		return groupName;
	}

	public List<User> getAccessGroupMembers(String groupName, String userId) throws ServiceCallerException {
		// http://{{hostname}}:8080/equip-services/global/groups/B1521005_UNBLINDED/users
		this.setUser(userId);

		String url = super.getBaseURI() + "/equip-services/global/groups/" + groupName + "/users";

		ServiceResponse sr = this.get(url);
		String json = sr.getResponseAsString();

		AuthorizationAccessGroups aag = GSON.fromJson(json, AuthorizationAccessGroups.class);
		return aag.getUsers();
	}

	public boolean copyDataframeGroupAccess(String equipId, String userId, String authorizationRequestBody) {
		// http://{{hostname}}:8080/equip-services/nca/dataframe/copy-access?targetEntityId=<equipID
		// of dataframeID2>
		String url = this.getBaseURI() + "/dataframe/copy-access?targetEntityId=" + equipId;

		this.setUser(userId);

		try {
			ServiceResponse sr = this.post(url, authorizationRequestBody);
			sr.getResponseAsString();
		} catch (Exception ex) {
			return false;
		}

		return true;
	}

	public boolean copyDataframeGroupAccess(Map<String, String> links, String userId) {
		long transactionId = 0;
		List<CopyGroupAccessLink> request = new ArrayList<>();
		for (Entry<String, String> e : links.entrySet()) {
			CopyGroupAccessLink link = new CopyGroupAccessLink(e.getKey(), e.getValue(), ++transactionId + "");
			request.add(link);
		}

		GsonBuilder gb = new GsonBuilder();
		gb.setPrettyPrinting();
		Gson gson = gb.create();
		String json = gson.toJson(request);

		boolean success = false;
		String uri = this.getBaseURI() + "/dataframe/copy-access-multiple";
		try {
			ServiceResponse sr = this.post(uri, json);
			if (sr.getCode() == 200) {
				success = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return success;
	}

	@Override
	protected String getBaseURI() {
		return super.getBaseURI() + "/equip-services/" + this.getSystemId();
	}
}

class CopyGroupAccessLink {
	String parentId;
	String targetId;
	String transactionId;

	public CopyGroupAccessLink() {
		this(null, null, null);
	}

	public CopyGroupAccessLink(String parentId, String targetId, String transactionId) {
		this.parentId = parentId;
		this.targetId = targetId;
		this.transactionId = transactionId;
	}
}