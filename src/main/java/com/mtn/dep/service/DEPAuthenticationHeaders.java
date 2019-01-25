package com.mtn.dep.service;

import com.mtn.dep.AuthenticationHeaders;

import lombok.Builder;
import lombok.Getter;

/**
 * 
 * A DTO containing the Authorization and X-Amz-Date header values needed to make
 * a REST request to the DEP platform endpoint(s).
 * 
 * @author Andrew Howes
 * @since 1.8
 * @version 1.0.0
 */
@Builder
@Getter
public final class DEPAuthenticationHeaders implements AuthenticationHeaders {
	
	private String authorizationString;
	private String xAmzDate;
}
