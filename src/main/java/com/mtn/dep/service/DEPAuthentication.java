package com.mtn.dep.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;

import com.mtn.dep.Authentication;
import com.mtn.dep.AuthenticationHeaders;

import lombok.Builder;

/**
 * 
 * An implementation of the DEP platform authentication mechanism that takes care of the
 * Authorization header and X-Amz-Date header generation.
 * 
 * @author Andrew Howes
 * @since 1.8
 * @version 1.0.0
 */
public final class DEPAuthentication implements Authentication {
	
	private String accessKey;
	private String accessSecret;
	private String requestPath;
	private String hostName;
	private String body;
	private TreeMap<String, Object> queryString;
	private HttpMethod httpMethod;
	private ZonedDateTime timestamp;
	
	private final String signingAlgorithm = "AWS4-HMAC-SHA256";
	private final String signedHeaders = "host;x-amz-date";
	private final String zoneId = "GMT";
	private final String dateTimeFormat = "yyyyMMdd'T'HHmmss'Z'";
	private final String dateOnlyFormat = "yyyyMMdd";

	@Override
	public AuthenticationHeaders createAuthenticationHeaders() {
		
		LocalDate requestDate = getTimestampForZoneId(timestamp, zoneId).toLocalDate();
		LocalDateTime requestTimeStamp = getTimestampForZoneId(timestamp, zoneId).toLocalDateTime();
		String queryString = getRequestQueryString();
		
		AuthenticationService manager = buildAuthenticationManager(requestDate, requestTimeStamp, queryString, body);
		String authorizationString = getAuthorizationString(manager);
		
		return buildDEPAuthenticationHeaders(authorizationString);
	}
	
	@Override
	public String getJsonBody() {
		return body;
	}

	@Override
	public String getRequestQueryString() {
		String queryString = null;
		
		if (this.queryString != null) {
			queryString = "";

			for (String key : this.queryString.keySet()) {
				queryString += key + "=" + this.queryString.get(key) + "&";
			}
			
			queryString = queryString.substring(0, queryString.lastIndexOf("&"));
		}
		
		return queryString;
	}
	
	/**
	 * Exposes the fields that forms part of the builder pattern. Most of these fields
	 * are mandatory and will result in a DEPValidationException being thrown if not present
	 * when instantiating the object. If one of POST, PUT or PATCH endpoints are being called
	 * one or both the body and queryString should be provided or a DEPValidationException will
	 * be thrown.
	 * 
	 * @param accessKey (Mandatory) - The provided access key.
	 * @param accessSecret (Mandatory) - The provided access secret.
	 * @param requestPath (Mandatory) - The request path of the endpoint being called.
	 * @param queryString (Optional) - Any query string data that should form part of the call to the endpoint.
	 * @param httpMethod (Mandatory) - The type of call could be one of POST, PUT, DELETE, GET, PATCH.
	 * @param body (Optional) - Normally used as part of the POST, PUT, PATCH.
	 * @param timestamp (Mandatory) - Usually passed as "ZonedDateTime.now()" with optional ZoneId.
	 * @param hostName (Mandatory) - The provided host name.
	 * @throws DEPValidationException Thrown if one or more of the mandatory fields are not specified.
	 */
	@Builder
	private DEPAuthentication(String accessKey, String accessSecret, String requestPath, TreeMap<String, Object> queryString,
			HttpMethod httpMethod, String body, ZonedDateTime timestamp, String hostName) throws DEPValidationException {
		this.accessKey = accessKey;
		this.accessSecret = accessSecret;
		this.requestPath = requestPath;
		this.queryString = queryString;
		this.httpMethod = httpMethod;
		this.body = body;
		this.timestamp = timestamp;
		this.hostName = hostName;
		
		validateMandatoryFields();
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
}
