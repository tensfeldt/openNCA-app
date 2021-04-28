package com.pfizer.equip.searchservice.util;

/**
 * Name of class says it all. Convenience class.
 * 
 * @author HeinemanWP
 *
 */
public class HTTPStatusCodes {
	private HTTPStatusCodes() {}
	
	/**
	 * The request has succeeded. The information returned with the response is dependent on the method used in the request, for example:
	 * <br><br>
	 * GET an entity corresponding to the requested resource is sent in the response;
	 * <br><br>
	 * HEAD the entity-header fields corresponding to the requested resource are sent in the response without any message-body;
	 * <br><br>
	 * POST an entity describing or containing the result of the action;
	 * <br><br>
	 * TRACE an entity containing the request message as received by the end server.
	 */
	public static final int OK = 200;
	
	/**
	 * The request has been fulfilled and resulted in a new resource being created. The newly created resource can be referenced by the URI(s) 
	 * returned in the entity of the response, with the most specific URI for the resource given by a Location header field. The response SHOULD 
	 * include an entity containing a list of resource characteristics and location(s) from which the user or user agent can choose the one most 
	 * appropriate. The entity format is specified by the media type given in the Content-Type header field. The origin server MUST create the 
	 * resource before returning the 201 status code. If the action cannot be carried out immediately, the server SHOULD respond with 202 (Accepted) 
	 * response instead.
	 * <br><br>
	 * A 201 response MAY contain an ETag response header field indicating the current value of the entity tag for the requested variant just created
	 */
	public static final int OK_CREATED = 201;
	
	/**
	 * The server has fulfilled the request but does not need to return an entity-body, and might want to return updated metainformation. The 
	 * response MAY include new or updated metainformation in the form of entity-headers, which if present SHOULD be associated with the requested 
	 * variant.
	 * <br><br>
	 * If the client is a user agent, it SHOULD NOT change its document view from that which caused the request to be sent. This response is 
	 * primarily intended to allow input for actions to take place without causing a change to the user agent's active document view, although 
	 * any new or updated metainformation SHOULD be applied to the document currently in the user agent's active view.
	 * <br><br>
	 * The 204 response MUST NOT include a message-body, and thus is always terminated by the first empty line after the header fields.
	 */
	public static final int OK_NO_CONTENT = 204;
	
	/**
	 * The request could not be understood by the server due to malformed syntax. The client SHOULD NOT repeat the request without modifications.
	 */
	public static final int BAD_REQUEST = 400;
	
	/**
	 * The request requires user authentication. The response MUST include a WWW-Authenticate header field (section 14.47) containing a challenge 
	 * applicable to the requested resource. The client MAY repeat the request with a suitable Authorization header field (section 14.8). If the 
	 * request already included Authorization credentials, then the 401 response indicates that authorization has been refused for those credentials. 
	 * If the 401 response contains the same challenge as the prior response, and the user agent has already attempted authentication at least once, 
	 * then the user SHOULD be presented the entity that was given in the response, since that entity might include relevant diagnostic information. 
	 * HTTP access authentication is explained in "HTTP Authentication: Basic and Digest Access Authentication"
	 */
	public static final int UNAUTHORIZED = 401;
	
	/**
	 * The server understood the request, but is refusing to fulfill it. Authorization will not help and the request SHOULD NOT be repeated. 
	 * If the request method was not HEAD and the server wishes to make public why the request has not been fulfilled, it SHOULD describe the 
	 * reason for the refusal in the entity. If the server does not wish to make this information available to the client, the status code 
	 * 404 (Not Found) can be used instead.
	 */
	public static final int FORBIDDEN = 403;
	
	/**
	 * The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent. 
	 * The 410 (Gone) status code SHOULD be used if the server knows, through some internally configurable mechanism, that an old resource is 
	 * permanently unavailable and has no forwarding address. This status code is commonly used when the server does not wish to reveal exactly 
	 * why the request has been refused, or when no other response is applicable.
	 */
	public static final int NOT_FOUND = 404;
	
	/**
	 * The method specified in the Request-Line is not allowed for the resource identified by the Request-URI. The response MUST include an Allow 
	 * header containing a list of valid methods for the requested resource.
	 */
	public static final int METHOD_NOT_ALLOWED = 405;
	
	/**
	 * The request could not be completed due to a conflict with the current state of the resource. This code is only allowed in situations where it 
	 * is expected that the user might be able to resolve the conflict and resubmit the request. The response body SHOULD include enough information 
	 * for the user to recognize the source of the conflict. Ideally, the response entity would include enough information for the user or user 
	 * agent to fix the problem; however, that might not be possible and is not required.
	 * <br><br>
	 * Conflicts are most likely to occur in response to a PUT request. For example, if versioning were being used and the entity being PUT 
	 * included changes to a resource which conflict with those made by an earlier (third-party) request, the server might use the 409 response 
	 * to indicate that it can't complete the request. In this case, the response entity would likely contain a list of the differences between 
	 * the two versions in a format defined by the response Content-Type.
	 */
	public static final int CONFLICT = 409;
	
	/**
	 * The server encountered an unexpected condition which prevented it from fulfilling the request.
	 */
	public static final int INTERNAL_SERVER_ERROR = 500;
	
	/**
	 * The server does not support the functionality required to fulfill the request. This is the appropriate response when the server does 
	 * not recognize the request method and is not capable of supporting it for any resource.
	 */
	public static final int NOT_IMPLEMENTED = 501;
}
