package com.mtn.dep.service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.digest.DigestUtils;

import lombok.Builder;

/**
 * 
 * An implementation of the Amazon AWS authentication mechanism, used to make REST calls
 * to the DEP platform endpoint(s).
 * 
 * @author Andrew Howes
 * @since 1.8
 * @version 1.0.0
 */
final class AuthenticationService {
	
	private String accessSecret;
	private String httpMethod;
	private String requestPath;
	private String queryString = "";
	private LocalDate requestDate;
	private LocalDateTime timestamp;
	private String body = "";
	private DateTimeFormatter requestDateFormat;
	private DateTimeFormatter timestampFormat;
	private String hostName;
	
	private final String algorithmSuit = "AWS4-HMAC-SHA256";
	private final String hashingAlgorithm = "HmacSHA256";
	private final String region = "eu-west-1";
	private final String service = "execute-api";
	private final String secretPrefix = "AWS4";
	private final String requestTerminationString = "aws4_request";
	private final String amzDate = "x-amz-date:";
	private final String hostAmzDate = "host;x-amz-date";
	
	public String generateSignature() {
		String result = null;

		try {
			byte[] step2 = createSigningKey();
			String step3 = createSigningString();
			
			result = createSignature(step2, step3);
		} catch (InvalidKeyException e) {
		} catch (NoSuchAlgorithmException e) {
		}
		
		return result;
	}
	
	public String getCredentialScope() {
		return createCredentialScope();
	}
	
	@Builder
	private AuthenticationService(String accessSecret, String httpMethod, String requestPath,
			String queryString, LocalDate requestDate, LocalDateTime timestamp, String body,
			DateTimeFormatter requestDateFormat, DateTimeFormatter timestampFormat, String hostName) {
		this.accessSecret = accessSecret;
		this.httpMethod = httpMethod;
		this.requestPath = requestPath;
		
		if (queryString != null) {
			this.queryString = queryString;
		}
		
		this.requestDate = requestDate;
		this.timestamp = timestamp;
		
		if (body != null) {
			this.body = body;
		}
		
		this.requestDateFormat = requestDateFormat;
		this.timestampFormat = timestampFormat;
		this.hostName = hostName;
	}
	
	private String createCredentialScope() {
		String credentialScope = null;
		
		credentialScope = requestDate.format(requestDateFormat).toString();
		credentialScope += "/" + region;
		credentialScope += "/" + service;
		credentialScope += "/" + requestTerminationString;
		
		return credentialScope;
	}
	
	
	private byte[] createSigningKey() throws NoSuchAlgorithmException, InvalidKeyException {
		List<String> signingValues = new ArrayList<>();
		
		signingValues.add(requestDate.format(requestDateFormat));
		signingValues.add(region);
		signingValues.add(service);
		signingValues.add(requestTerminationString);
		
		return createSigningKey(createUtf8EncodedSecret(), signingValues, 0);
	}
	
	private byte[] createUtf8EncodedSecret() {
		String aws4 = secretPrefix + accessSecret;
		
		return aws4.getBytes(StandardCharsets.UTF_8);
	}
	
	private byte[] createSigningKey(byte[] key, List<String> value, int index) throws NoSuchAlgorithmException, InvalidKeyException {
		if (index >= value.size()) {
			return key;
		}
		
		Mac hMac = Mac.getInstance(hashingAlgorithm);
		SecretKey secret = new SecretKeySpec(key, hashingAlgorithm);
		
		hMac.init(secret);
		
		byte[] processedMac = hMac.doFinal(value.get(index).getBytes(StandardCharsets.UTF_8));
		byte[] result = createSigningKey(processedMac, value, ++index);
		
		return result;
	}
	
	private String createSigningSubString() {
		String result = "";
		
		List<String> dataToSign = new ArrayList<>();
		
		dataToSign.add(httpMethod);
		dataToSign.add(requestPath);
		dataToSign.add(queryString);
		dataToSign.add(hostName);
		dataToSign.add(amzDate + timestamp.format(timestampFormat));
		dataToSign.add(null); // This is needed to satisfy the requirement of having a blank line
		dataToSign.add(hostAmzDate);
		
		for (String data : dataToSign) {
			if (data != null) {
				result += data + System.lineSeparator();
			} else {
				result += System.lineSeparator();
			}
		}

		result += DigestUtils.sha256Hex(body.getBytes(StandardCharsets.UTF_8));
		
		return result;
	}
	
	private String createSigningString() {
		String result = algorithmSuit 
				+ System.lineSeparator()
				+ timestamp.format(timestampFormat)
				+ System.lineSeparator()
				+ createCredentialScope()
				+ System.lineSeparator()
				+ DigestUtils.sha256Hex(createSigningSubString().getBytes(StandardCharsets.UTF_8));
		
		return result;
	}
	
	private String createSignature(byte[] key, String value) throws InvalidKeyException, NoSuchAlgorithmException {
		List<String> signingValues = new ArrayList<>();
		
		signingValues.add(value);
		
		byte[] result = createSigningKey(key, signingValues, 0);
		
		String signature = convertBytesToHex(result);
		
		return signature;
	}
	
	private static String convertBytesToHex(byte[] hash) {
	    StringBuffer hexString = new StringBuffer();

	    for (int i = 0; i < hash.length; i++) {
		    String hex = Integer.toHexString(0xff & hash[i]);
		
		    if(hex.length() == 1) {
		    	hexString.append('0');
		    }
		    
	        hexString.append(hex);
	    }
	    
	    return hexString.toString();
	}
}
