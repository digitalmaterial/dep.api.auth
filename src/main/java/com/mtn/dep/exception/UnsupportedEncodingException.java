package com.mtn.dep.exception;

/**
 * 
 * Thrown when the character encoding is not supported
 * 
 * @author Andrew Howes
 * @since 1.8
 * @version 1.1.0
 */
public class UnsupportedEncodingException extends RuntimeException {

	private static final long serialVersionUID = 5573406800191383906L;

	public UnsupportedEncodingException() {
		super("Unable to encode query string: The character encoding is not supported.");
	}
}
