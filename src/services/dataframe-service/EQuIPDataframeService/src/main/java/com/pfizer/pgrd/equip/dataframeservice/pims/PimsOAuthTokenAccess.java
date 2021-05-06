package com.pfizer.pgrd.equip.dataframeservice.pims;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframeservice.application.Props;

public class PimsOAuthTokenAccess {
	private static Logger log = LoggerFactory.getLogger(PimsWrapper.class);
	
	private final static String OATH_ENDPOINT = Props.getPIMSOAuthEndpoint();
	private final static String BASE64_CREDENTIALS = Base64.encodeBase64String((Props.getOAuthUser() + ":" + Props.getOAuthPassword()).getBytes());
	
	/**
	 * This method makes a call to PingFederate to retrieve an oAuth2 access_token.
	 * 
	 * @return accessToken
	 * @throws Exception
	 */
	public final static String getAccessToken() throws Exception {
		String accessToken = null;
		HttpURLConnection con  = null;
		
		try {
			URL url = new URL(OATH_ENDPOINT);
		    con = (HttpURLConnection) url.openConnection();
		    con.setDoOutput(true);
		    con.setRequestMethod("POST");
		    con.setRequestProperty("Authorization", "Basic " + BASE64_CREDENTIALS); // set Authorization Basic for PingFederate call
		    con.setRequestProperty("Content-Type", "application/json");
	
		    Integer responseCode = con.getResponseCode();
	        if (responseCode == 200) {
	        	String responseBody = getResponseBody(con.getInputStream());
	        	log.info("Success calling oAuth server. HTTP Status: " + responseCode + ". Response Body: " + responseBody);
	        	accessToken = getAccessTokenFromResponseBody(responseBody);
	        } else {
	        	log.info("Error calling oAuth server. HTTP Status: " + responseCode + ". Response Body: " + getResponseBody(con.getErrorStream()));
	        	throw new RuntimeException("Error calling oAuth server");
		    }
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
		
		return accessToken;
	}
	
	/**
	 * This method extracts the response body from the 
	 * InputStream, converts it to String and returns it.
	 * 
	 * @param response InputStream
	 * @return responseBody
	 * @throws Exception
	 */
	private static final String getResponseBody(InputStream is) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder content = new StringBuilder();
		
		try {
		    String line;

		    while ((line = br.readLine()) != null) {
		    	content.append(line);
		    }
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (Exception e) {
				log.info("Error closing BufferedReader in getResponseBody()");
			}
		}
		
	    return content.toString();
	}
	
	/**
	 * This method parses the string response in order to retrieve the
	 * JSON fields and returns access_token.
	 * 
	 * @param responseBody
	 * @return
	 */
	private static final String getAccessTokenFromResponseBody(String responseBody) {
	    Map<String, String> responseTokens = new HashMap<String, String>();
	    
	    for (String token : responseBody.replace("{", "").replace("}", "").replace("\"", "").split(",")) { // remove {} and ""
	    	String[] keyValue = token.split(":"); // split on key:value 
	        responseTokens.put(keyValue[0], keyValue[1]); //put key:value pairs on map
	    }
	    
	    return responseTokens.get("access_token"); // return value for key access_token
	}	

}
