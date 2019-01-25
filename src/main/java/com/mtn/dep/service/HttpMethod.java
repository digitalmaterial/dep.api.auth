package com.mtn.dep.service;
/**
 * 
 * An enum used by the DEPAuthentication implementation to specify the type
 * of http method that should form part of the authorisation header string generation.
 * 
 * @author Andrew Howes
 * @since 1.8
 * @version 1.0.0
 */
public enum HttpMethod {
	POST,
	PUT,
	PATCH,
	GET,
	DELETE
}
