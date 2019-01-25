package com.mtn.dep;

/**
 * 
 * DTO containing the result of the generated authorization string as well as the X-Amz-Date to be used
 * as part of the REST call to the DEP platform endpoint(s).
 * 
 * @author Andrew Howes
 * @since 1.8
 * @version 1.0.0
 */
public interface AuthenticationHeaders {

	/**
	 * This method returns the generated authorization string that should form part of 
	 * the headers when making the REST call to the DEP platform.
	 * 
	 * @return The generated authorization string
	 */
	String getAuthorizationString();
	
	/**
	 * The method returns the X-Amz-Date header as a correctly formatted string that
	 * should form part of the headers when making the REST call to the DEP platform.
	 * 
	 * @return The generated X-Amz-Date header as text.
	 */
	String getXAmzDate();
}
