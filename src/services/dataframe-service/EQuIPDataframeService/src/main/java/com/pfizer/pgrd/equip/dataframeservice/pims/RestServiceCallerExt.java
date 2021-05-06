package com.pfizer.pgrd.equip.dataframeservice.pims;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.ClientInfo;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;

import com.pfizer.pgrd.rest.client.RestClientException;
import com.pfizer.pgrd.rest.client.RestServiceCaller;
import com.pfizer.pgrd.rest.client.RestServiceResult;

//This class is temporarily needed until it can be moved into the RestClientFramework jar file.
public class RestServiceCallerExt extends RestServiceCaller {
	private static final Log lOGGER = LogFactory.getLog(RestServiceCallerExt.class);

	private static final List<Preference<MediaType>> ACCEPTED_MEDIA_TYPES = new ArrayList<Preference<MediaType>>();;

	//the user name and password are encoded in the token so no need for Restlet User object
	private static ClientResource getClientResource(String token, String uri) {
		if(RestServiceCallerExt.ACCEPTED_MEDIA_TYPES.size() == 0 ){
			RestServiceCallerExt.ACCEPTED_MEDIA_TYPES.add(new Preference<MediaType>(MediaType.APPLICATION_ALL_XML));
		}
		ClientResource cr = new ClientResource(uri);

		if (token != null && token.trim().length() > 0) {
			ChallengeResponse chResp = new ChallengeResponse(ChallengeScheme.HTTP_OAUTH_BEARER);
			chResp.setRawValue(token);
			cr.setChallengeResponse(chResp);
		}

		if (ACCEPTED_MEDIA_TYPES.isEmpty() == false) {
			ClientInfo ci = cr.getClientInfo();
			ci.setAcceptedMediaTypes(ACCEPTED_MEDIA_TYPES);
		}
		return cr;
	}

	public static RestServiceResult get(String token, String uri) throws RestClientException {
		ClientResource cr = getClientResource(token, uri);

		lOGGER.info("GET: " + cr.getReference().getPath() + " for user " + token);

		try {
			cr.get();
		} 
		catch (ResourceException e) {
			String str = "GET: " + cr.getReference().getPath() + " failed: " + e.getMessage();
			lOGGER.error( str, e);
			if (e.getMessage().equalsIgnoreCase("Not Found")){
				return null;  //this will allow teh code to continue and try another uri
			}
			else throw new IllegalStateException(str);
		}

		RestServiceResult svcRes = new RestServiceResult(cr.getResponse());

		return svcRes;
	}

}
