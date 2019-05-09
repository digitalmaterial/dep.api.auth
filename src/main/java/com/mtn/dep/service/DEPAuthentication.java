package com.mtn.dep.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;

import com.mtn.dep.Authentication;
import com.mtn.dep.AuthenticationHeaders;

/**
 * 
 * An implementation of the DEP platform authentication mechanism that takes care of the
 * Authorization header and X-Amz-Date header generation.
 * 
 * @author Andrew Howes
 * @since 1.8
 * @version 1.1.0
 */
public final class DEPAuthentication implements Authentication {
	
	private String accessKey;
	private String accessSecret;
	private String requestPath;
	private String hostName;
	private String body;
	
	@Deprecated
	private TreeMap<String, Object> queryString;
	
	@Deprecated
	private boolean shouldURLEncode;
	
	private HttpMethod httpMethod;
	
	private ZonedDateTime timestamp;
	
	private QueryStringProcessor queryStringProcessor;
	
	private final String signingAlgorithm = "AWS4-HMAC-SHA256";
	private final String signedHeaders = "host;x-amz-date";
	private final String zoneId = "GMT";
	private final String dateTimeFormat = "yyyyMMdd'T'HHmmss'Z'";
	private final String dateOnlyFormat = "yyyyMMdd";
	
	/**
	 * An implementation of the builder pattern. Ensure that you supply all
	 * mandatory fields to avoid a DEPValidationException
	 * 
	 * @return An instance of DEPAuthenticationBuilder.
	 */
	public static DEPAuthenticationBuilder builder() {
		return new DEPAuthenticationBuilder();
	}

	@Override
	public AuthenticationHeaders createAuthenticationHeaders() {
		
		LocalDate requestDate = getTimestampForZoneId(timestamp, zoneId).toLocalDate();
		LocalDateTime requestTimeStamp = getTimestampForZoneId(timestamp, zoneId).toLocalDateTime();
		String queryString = getQueryString();
		
		AuthenticationService manager = buildAuthenticationManager(requestDate, requestTimeStamp, queryString, body);
		String authorizationString = getAuthorizationString(manager);
		
		return buildDEPAuthenticationHeaders(authorizationString);
	}
	
	@Override
	public String getJsonBody() {
		return body;
	}
	
	@Override
	@Deprecated
	public String getRequestQueryString() {
		if (queryStringProcessor != null) {
			return queryStringProcessor.getQueryString(shouldURLEncode);
		}
		
		return null;
	}
	
	private DEPAuthentication() {}
	
	private String getQueryString() {
		if (queryStringProcessor != null) {
			return queryStringProcessor.getQueryString(true);
		}
		
		return null;
	}
	
	private void createQueryString(TreeMap<String, Object> queryString, boolean shouldURLEncode) {
		this.queryStringProcessor = new QueryStringProcessor();
		
		for (String key : queryString.keySet()) {
			this.queryStringProcessor.addQueryParameter(key, "" + queryString.get(key));
		}
	}

	private void validateMandatoryFields() throws DEPValidationException {
		if (accessKey == null) {
			throw new DEPValidationException("No accessKey provided. The accessKey is a mandatory field and cannot be null.");
		}
		if (accessSecret == null) {
			throw new DEPValidationException("No accessSecret provided. The accessSecret is a mandatory field and cannot be null.");
		}
		if (requestPath == null) {
			throw new DEPValidationException("No requestPath provided. The requestPath is a mandatory field and cannot be null.");
		}
		if (httpMethod == null) {
			throw new DEPValidationException("No httpMethod provided. The httpMethod is a mandatory field and cannot be null.");
		}
		if (timestamp == null) {
			throw new DEPValidationException("No timestamp provided. The timestamp is a mandatory field and cannot be null.");
		}
		if (hostName == null || hostName.isEmpty()) {
			throw new DEPValidationException("No hostName provided. The hostName is a mandatory field and cannot be null.");
		}
		if ((httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT || httpMethod == HttpMethod.PATCH) && (body == null && queryString == null)) {
			throw new DEPValidationException("A JSON body or query string should be provided for HttpMethod types: POST, PUT, PATCH.");
		}
	}
	
	private DEPAuthenticationHeaders buildDEPAuthenticationHeaders(String authorizationString) {
		return DEPAuthenticationHeaders
				.builder()
					.authorizationString(authorizationString)
					.xAmzDate(getTimestampAsGMTString())
				.build();
	}

	private AuthenticationService buildAuthenticationManager(LocalDate requestDate, LocalDateTime requestTimeStamp, String queryString,
			String body) {
		return AuthenticationService
		.builder()
			.httpMethod(httpMethod.name())
			.requestPath(requestPath)
			.queryString(queryString)
			.body(body)
			.accessSecret(accessSecret)
			.requestDate(requestDate)
			.requestDateFormat(DateTimeFormatter.ofPattern(dateOnlyFormat))
			.timestamp(requestTimeStamp)
			.timestampFormat(DateTimeFormatter.ofPattern(dateTimeFormat))
			.hostName(hostName)
		.build();
	}

	private String getAuthorizationString(AuthenticationService manager) {
		return signingAlgorithm + 
				" Credential=" + accessKey + 
				"/" + manager.getCredentialScope() + ", " + 
				"SignedHeaders=" + signedHeaders + ", " + 
				"Signature=" +	manager.generateSignature();
	}
	
	private String getTimestampAsGMTString() {
		return timestamp.withZoneSameInstant(ZoneId.of(zoneId)).format(DateTimeFormatter.ofPattern(dateTimeFormat));
	}
	
	private ZonedDateTime getTimestampForZoneId(ZonedDateTime timestamp, String zoneId) {
		return timestamp.withZoneSameInstant(ZoneId.of(zoneId));
	}
	
	
	
	public static class DEPAuthenticationBuilder {
		private DEPAuthentication instance;
		
		/**
		 * The DEPAuthenticationBuilder constructor is responsible
		 * for creating a new instance of the DEPAuthentication class.
		 */
		private DEPAuthenticationBuilder() {
			instance = new DEPAuthentication();
		}
		
		/**
		 * @param accessKey (Mandatory) - The provided access key.
		 * @return
		 */
		public DEPAuthenticationBuilder accessKey(String accessKey) {
			instance.accessKey = accessKey;
			
			return this;
		}
		
		/**
		 * @param accessSecret (Mandatory) - The provided access secret.
		 * @return
		 */
		public DEPAuthenticationBuilder accessSecret(String accessSecret) {
			instance.accessSecret = accessSecret;
			
			return this;
		}
		
		/**
		 * @param requestPath (Mandatory) - The request path of the endpoint being called.
		 * @return
		 */
		public DEPAuthenticationBuilder requestPath(String requestPath) {
			instance.requestPath = requestPath;
			
			return this;
		}
		
		/**
		 * @param hostName (Mandatory) - The provided host name.
		 * @return
		 */
		public DEPAuthenticationBuilder hostName(String hostName) {
			if (!hostName.startsWith("host:")) {
				hostName = "host:" + hostName;
			}
			
			instance.hostName = hostName;
			
			return this;
		}
		
		/**
		 * @param body (Optional) - Normally used as part of the POST, PUT, PATCH.
		 * @return
		 */
		public DEPAuthenticationBuilder body(String body) {
			instance.body = body;
			
			return this;
		}
		
		/**
		 * @deprecated This parameter is deprecated since version 1.0.1. Make use of QueryStringProcessor instead.
		 * @param queryString (Optional) - Any query string data that should form part of the call to the endpoint.
		 * @return
		 */
		@Deprecated
		public DEPAuthenticationBuilder queryString(TreeMap<String, Object> queryString) {
			instance.queryString = queryString;
			
			return this;
		}
		
		/**
		 * @deprecated This parameter is deprecated since version 1.0.1. Make use of QueryStringProcessor instead.
		 * @param shouldURLEncode (Optional) - If true the queryString will be URLEncoded.
		 * @return
		 */
		@Deprecated
		public DEPAuthenticationBuilder shouldURLEncode(boolean shouldURLEncode) {
			instance.shouldURLEncode = shouldURLEncode;
			
			return this;
		}
		
		/**
		 * @param httpMethod (Mandatory) - The type of call could be one of POST, PUT, DELETE, GET, PATCH.
		 * @return
		 */
		public DEPAuthenticationBuilder httpMethod(HttpMethod httpMethod) {
			instance.httpMethod = httpMethod;
			
			return this;
		}
		
		/**
		 * @param timestamp (Mandatory) - Usually passed as "ZonedDateTime.now()" with optional ZoneId.
		 * @return
		 */
		public DEPAuthenticationBuilder timestamp(ZonedDateTime timestamp) {
			instance.timestamp = timestamp;
			
			return this;
		}
		
		/**
		 * 
		 * @param queryStringProcessor
		 * @return
		 */
		public DEPAuthenticationBuilder queryStringProcessor(QueryStringProcessor queryStringProcessor) {
			instance.queryStringProcessor = queryStringProcessor;
			
			return this;
		}
		
		/**
		 * @return An instance of the DEPAuthentication class.
		 * @throws DEPValidationException Thrown if one or more of the mandatory fields are not specified.
		 */
		public DEPAuthentication build() throws DEPValidationException {
			instance.validateMandatoryFields();
			
			if (instance.queryStringProcessor == null && instance.queryString != null && !instance.queryString.isEmpty()) {
				instance.createQueryString(instance.queryString, instance.shouldURLEncode);
			}
			
			return instance;
		}
	}
}
