package com.mtn.dep.service;

/**
 * 
 * Thrown when one of the mandatory fields do not form part of the instantiation
 * of the DEPAuthentication Object.
 * 
 * @author Andrew Howes
 * @since 1.8
 * @version 1.0.0
 */
public class DEPValidationException extends Exception {

	private static final long serialVersionUID = -6811310406283501737L;

	/**
	 * Constructs a new DEPValidatinException with the given message. This exception could be the
	 * result of mandatory fields not being provided, where needed.
	 *  
	 * @param exception The message that describes the exception being thrown.
	 */
	public DEPValidationException(String exception) {
		super(exception);
	}
}
