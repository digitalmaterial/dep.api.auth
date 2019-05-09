package com.mtn.dep;

/**
 * 
 * An Object used to generate the Authorization and X-Amz-Date headers that should form part of the
 * REST request(s) sent to the DEP platform endpoint(s). The object also contains the JSON body
 * that forms part of the REST request, as well as the query string, for convenience. This ensures that
 * you will have the correctly formated body and query string that should form part of the REST call to
 * the DEP platform endpoint(s).
 * 
 * @author Andrew Howes
 * @since 1.8
 * @version 1.1.0
 */
public interface Authentication {

	/**
	 * Creates a DEPAuthenticationHeaders object with the generated authorization string as well as a date string
	 * which represents the X-Amz-Date header. These headers should form part of the REST request to the DEP platform.
	 * 
	 * @return DEPAuthenticationHeaders object with the generated 
	 * authorization string and the X-Amz-Date.
	 */
	AuthenticationHeaders createAuthenticationHeaders();
	
	/**
	 * This method returns the JSON body as text. It serves as a convenience method, containing the JSON body
	 * that should form part of the request for which the headers are generated.
	 * 
	 * @return The JSON body as text.
	 */
	String getJsonBody();
	
	/**
	 * To retrieve the query parameters, make use of the "getQueryString(boolean shouldEncode)" method from the
	 * QueryStringProcessor object.
	 * 
	 * @return The request query string as a string, alphabetically formatted by key (or value if the two keys are equal).
	 */
	@Deprecated
	String getRequestQueryString();
}
