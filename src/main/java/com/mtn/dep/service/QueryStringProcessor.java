package com.mtn.dep.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class provides the ability to create a sorted query string based
 * on the key/value pairs provided. Optionally, the query string can be
 * URL encoded. <i>Note:</i> both key and value will be URL encoded. The
 * query string will be sorted by key. Since duplicate keys are supported,
 * sorting will be done on value for such scenarios. The query string will 
 * always be returned in sorted order.
 * 
 * <b><i>Note</i>: Duplicate key/value pairs are not supported. Only duplicate keys with
 * different values are supported by this class.</b>
 * 
 * @author andrewhowes
 * @since 1.8
 * @version 1.1.0
 */
public class QueryStringProcessor {

	private Map<String, String> sortedQueryParameters = new TreeMap<>();
	
	private Map<String, String> unsortedQueryParameters = new LinkedHashMap<>();
	
	private String characterEncoding = StandardCharsets.UTF_8.toString();
	
	/**
	 * Associates the specified value with the given key, to be used as part of the query string.
	 * 
	 * @param key key with which the value is associated.
	 * @param value value associated to the specified name.
	 */
	public void addQueryParameter(String key, String value) {
		try {
			String encodedKey = encode(key) + "=" + encode(value);
			String unencodedValue = key + "=" + value;
			
			sortedQueryParameters.put(encodedKey, unencodedValue);
			unsortedQueryParameters.put(encodedKey, unencodedValue);
		} catch (UnsupportedEncodingException e) {
			throw new com.mtn.dep.exception.UnsupportedEncodingException();
		}
	}
	
	/**
	 * A convenience method returning a sorted query string. Sorting is done by (1) key and (2) value (if two keys are equal).
	 * If "shouldEncode" is set to "true", the query string will be returned as StandardCharsets.UTF_8 encoded.
	 * @return A sorted query string.
	 */
	public String getQueryString(boolean shouldEncode) {
		return getQueryString(shouldEncode, sortedQueryParameters);
	}
	
	/**
	 * A convenience method returning an unsorted query string. The original order of insertion are preserved.
	 * If "shouldEncode" is set to "true", the query string will be returned as StandardCharsets.UTF_8 encoded.
	 * @return An unsorted query string.
	 */
	public String getUnsortedQueryString(boolean shouldEncode) {
		return getQueryString(shouldEncode, unsortedQueryParameters);
	}

	private String getQueryString(boolean shouldEncode, Map<String, String> map) {
		if (shouldEncode) {
			return computeQueryString(map.keySet());
		} else {
			return computeQueryString(map.values());
		}
	}
	
	private String encode(String text) throws UnsupportedEncodingException {
		String result = null;
		
		if (shouldEncode(text)) {
			result = URLEncoder.encode(text, characterEncoding).replaceAll("\\+", "%20");
		} else {
			result = text;
		}
		
		return result;
	}

	private boolean shouldEncode(String text) {
		String unreservedCharacters = "[\\.\\-\\_\\~]";

		return !text.matches(unreservedCharacters);
	}
	
	private String computeQueryString(Collection<String> collection) {
		StringBuilder result = new StringBuilder();
		
		for (String queryString : collection) {
			result.append(queryString).append("&");
		}

		result.deleteCharAt(result.lastIndexOf("&"));
		
		return result.toString();
	}
}
