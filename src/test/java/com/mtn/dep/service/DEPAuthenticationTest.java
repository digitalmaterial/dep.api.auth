package com.mtn.dep.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TreeMap;

import org.junit.Test;

import com.mtn.dep.Authentication;
import com.mtn.dep.AuthenticationHeaders;

public class DEPAuthenticationTest {
	
	private final String hostName = "api.dep.mtn.co.za";
	private final String accessKey = "TESTKEY";
	private final String accessSecret = "TESTSECRET";
	
	private final String result = "AWS4-HMAC-SHA256 Credential=TESTKEY/20181022/eu-west-1/execute-api/aws4_request, SignedHeaders=host;x-amz-date, Signature=b398ee70496cf3a43cc571431ea833090d58f20a0b1439109775d1e59c4106ca";	
	private final ZonedDateTime timestamp = ZonedDateTime.of(LocalDate.of(2018, 10, 22), LocalTime.of(12, 59, 51, 428), ZoneId.of("UTC"));
	

	@Test
	public void testThatAutorizationStringIsCorrectForDELETE() {
		try {
			Authentication auth = DEPAuthentication
				.builder()
					.accessKey(accessKey)
					.accessSecret(accessSecret)
					.httpMethod(HttpMethod.DELETE)
					.requestPath("/subscription/50273440")
					.timestamp(timestamp)
					.hostName(hostName)
				.build();
			
			assertEquals(result, auth.createAuthenticationHeaders().getAuthorizationString());
		} catch (DEPValidationException e) {
			fail("The DEPAuthentication object could not be built.");
		}
						
	}
	
	@Test
	public void testThatAutorizationStringIsCorrectForPOST() {
		String result = "AWS4-HMAC-SHA256 Credential=TESTKEY/20181022/eu-west-1/execute-api/aws4_request, SignedHeaders=host;x-amz-date, Signature=e28d5a3de0973c0cd7cd5ce86a87157897f118ea85791f540819d0201771cd3b";
		
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		builder.append("\"");
		builder.append("one");
		builder.append("\"");
		builder.append(":");
		builder.append("\"");
		builder.append("one");
		builder.append("\"");
		builder.append("}");
		
		try {
			Authentication auth = DEPAuthentication
				.builder()
					.accessKey(accessKey)
					.accessSecret(accessSecret)
					.httpMethod(HttpMethod.POST)
					.requestPath("/subscription")
					.timestamp(timestamp)
					.body(builder.toString())
					.hostName(hostName)
				.build();
			
			assertEquals(result, auth.createAuthenticationHeaders().getAuthorizationString());
		} catch (DEPValidationException e) {
			fail("The DEPAuthentication object could not be built.");
		}
						
	}
	
	@Test
	public void testThatAccessKeyIsMandatory() {
		try {
			DEPAuthentication.builder().accessSecret(accessSecret).httpMethod(HttpMethod.DELETE)
					.requestPath("/subscription/50273440").timestamp(timestamp).build();
			
			fail("No exception were thrown for mandatory field accessKey.");
		} catch (DEPValidationException e) {
			assertEquals("No accessKey provided. The accessKey is a mandatory field and cannot be null.", e.getMessage());
		}
	}
	
	@Test
	public void testThatAccessSecretIsMandatory() {
		try {
			DEPAuthentication.builder().accessKey(accessKey).httpMethod(HttpMethod.DELETE)
					.requestPath("/subscription/50273440").timestamp(timestamp).build();
			
			fail("No exception were thrown for mandatory field accessSecret.");
		} catch (DEPValidationException e) {
			assertEquals("No accessSecret provided. The accessSecret is a mandatory field and cannot be null.", e.getMessage());
		}
						
	}
	
	@Test
	public void testThatHttpMethodIsMandatory() {
		try {
			DEPAuthentication.builder().accessKey(accessKey).accessSecret(accessSecret)
					.requestPath("/subscription/50273440").timestamp(timestamp).build();
			
			fail("No exception were thrown for mandatory field httpMethod.");
		} catch (DEPValidationException e) {
			assertEquals("No httpMethod provided. The httpMethod is a mandatory field and cannot be null.", e.getMessage());
		}
						
	}
	
	@Test
	public void testThatRequestPathIsMandatory() {
		try {
			DEPAuthentication.builder().accessKey(accessKey).accessSecret(accessSecret).httpMethod(HttpMethod.DELETE)
					.timestamp(timestamp).build();
			
			fail("No exception were thrown for mandatory field requestPath.");
		} catch (DEPValidationException e) {			
			assertEquals("No requestPath provided. The requestPath is a mandatory field and cannot be null.", e.getMessage());
		}
						
	}
	
	@Test
	public void testThatTimestampIsMandatory() {
		try {
			DEPAuthentication.builder().accessKey(accessKey).accessSecret(accessSecret).httpMethod(HttpMethod.DELETE)
					.requestPath("/subscription/50273440").build();
			
			fail("No exception were thrown for mandatory field timestamp.");
		} catch (DEPValidationException e) {
			assertEquals("No timestamp provided. The timestamp is a mandatory field and cannot be null.", e.getMessage());
		}
						
	}
	
	@Test
	public void testThatASortedRequestQueryStringIsReturned() {
		TreeMap<String, Object> map = new TreeMap<>();
		
		map.put("z", "4");
		map.put("a", 1);
		map.put("t", "3");
		map.put("d", 2L);

		try {
			Authentication auth = DEPAuthentication
				.builder()
					.accessKey(accessKey)
					.accessSecret(accessSecret)
					.httpMethod(HttpMethod.DELETE)
					.requestPath("/subscription/50273440")
					.timestamp(timestamp)
					.queryString(map)
					.hostName(hostName)
				.build();
			
			 assertEquals("a=1&d=2&t=3&z=4", auth.getRequestQueryString());
		} catch (DEPValidationException e) {
			fail("The DEPAuthentication object could not be built.");
		}
	}
	
	@Test
	public void testThatJsonBodyOrQueryStringMustBeProvidedIfHttpMethodIsPOST() {
		try {
			DEPAuthentication
				.builder()
					.accessKey(accessKey)
					.accessSecret(accessSecret)
					.httpMethod(HttpMethod.POST)
					.requestPath("/subscription/50273440")
					.timestamp(timestamp)
					.hostName(hostName)
				.build();
			
			fail("No exception were thrown when HttpMethod is POST and no Json body or query string were provided.");
		} catch (DEPValidationException e) {
			assertEquals("A JSON body or query string should be provided for HttpMethod types: POST, PUT, PATCH.", e.getMessage());
		}
						
	}
	
	@Test
	public void testThatJsonBodyOrQueryStringMustBeProvidedIfHttpMethodIsPUT() {
		try {
			DEPAuthentication
				.builder()
					.accessKey(accessKey)
					.accessSecret(accessSecret)
					.httpMethod(HttpMethod.PUT)
					.requestPath("/subscription/50273440")
					.timestamp(timestamp)
					.hostName(hostName)
				.build();
			
			fail("No exception were thrown when HttpMethod is PUT and no Json body or query string were provided.");
		} catch (DEPValidationException e) {
			assertEquals("A JSON body or query string should be provided for HttpMethod types: POST, PUT, PATCH.", e.getMessage());
		}
						
	}
	
	@Test
	public void testThatJsonBodyOrQueryStringMustBeProvidedIfHttpMethodIsPATCH() {
		try {
			DEPAuthentication
				.builder()
					.accessKey(accessKey)
					.accessSecret(accessSecret)
					.httpMethod(HttpMethod.PATCH)
					.requestPath("/subscription/50273440")
					.timestamp(timestamp)
					.hostName(hostName)
				.build();
			
			fail("No exception were thrown when HttpMethod is PATCH and no Json body or query string were provided.");
		} catch (DEPValidationException e) {
			assertEquals("A JSON body or query string should be provided for HttpMethod types: POST, PUT, PATCH.", e.getMessage());
		}
						
	}
	
	@Test
	public void testThatURLEncodingIsAppliedToQueryString() {
		TreeMap<String, Object> map = new TreeMap<>();
		
		map.put("(", ")");
		map.put("=", ",");
		map.put("a", "a");
		map.put("A", 2L);

		try {
			Authentication auth = DEPAuthentication
				.builder()
					.accessKey(accessKey)
					.accessSecret(accessSecret)
					.httpMethod(HttpMethod.DELETE)
					.requestPath("/subscription/50273440")
					.timestamp(timestamp)
					.queryString(map)
					.shouldURLEncode(true)
					.hostName(hostName)
				.build();
			
			 assertEquals("%28=%29&%3D=%2C&A=2&a=a", auth.getRequestQueryString());
		} catch (DEPValidationException e) {
			fail("The DEPAuthentication object could not be built.");
		}
	}
	
	@Test
	public void testThatURLEncodingIsNOTAppliedToQueryString() {
		TreeMap<String, Object> map = new TreeMap<>();
		
		map.put("(", ")");
		map.put("=", ",");
		map.put("a", "a");
		map.put("A", 2L);
		
		try {
			Authentication auth = DEPAuthentication
				.builder()
					.accessKey(accessKey)
					.accessSecret(accessSecret)
					.httpMethod(HttpMethod.DELETE)
					.requestPath("/subscription/50273440")
					.timestamp(timestamp)
					.queryString(map)
					.hostName(hostName)
				.build();
			
			 assertEquals("(=)&==,&A=2&a=a", auth.getRequestQueryString());
		} catch (DEPValidationException e) {
			fail("The DEPAuthentication object could not be built.");
		}
	}
	
	@Test
	public void testThatACallWithQueryParametersSuccessfullyGenerateAuthHeader() {
		QueryStringProcessor queryStringProcessor = new QueryStringProcessor();
		
		queryStringProcessor.addQueryParameter("expand", "subscription(status=2,page=1,svc_id=1)");

		try {
			Authentication auth = DEPAuthentication
				.builder()
					.accessKey(accessKey)
					.accessSecret(accessSecret)
					.httpMethod(HttpMethod.GET)
					.requestPath("/service/1")
					.timestamp(ZonedDateTime.of(LocalDate.of(2019, 04, 16), LocalTime.of(9, 10, 10, 123), ZoneId.of("UTC")))
					.queryStringProcessor(queryStringProcessor)
					.hostName(hostName)
				.build();
			
			AuthenticationHeaders headers = auth.createAuthenticationHeaders();
			
			assertEquals("AWS4-HMAC-SHA256 Credential=TESTKEY/20190416/eu-west-1/execute-api/aws4_request, SignedHeaders=host;x-amz-date, Signature=5344a4e4d60b2a0c86fce8dea98ffb40e74cddbf69f3b5e47c5b094b8a284334", headers.getAuthorizationString());
			assertEquals("20190416T091010Z", headers.getXAmzDate());
			
		} catch (DEPValidationException e) {
			fail("The DEPAuthentication object could not be built.");
		}
	}
	
	@Test
	public void testThatSortingIsCorrectWhenOneValueIsEncoded() {
		QueryStringProcessor queryStringProcessor = new QueryStringProcessor();
		
		queryStringProcessor.addQueryParameter("A", "-");
		queryStringProcessor.addQueryParameter("A", ":");

		try {
			Authentication auth = DEPAuthentication
				.builder()
					.accessKey(accessKey)
					.accessSecret(accessSecret)
					.httpMethod(HttpMethod.GET)
					.requestPath("/service/1")
					.timestamp(ZonedDateTime.of(LocalDate.of(2019, 04, 16), LocalTime.of(10, 3, 10, 123), ZoneId.of("UTC")))
					.queryStringProcessor(queryStringProcessor)
					.hostName(hostName)
				.build();
			
			AuthenticationHeaders headers = auth.createAuthenticationHeaders();
			
			assertEquals("AWS4-HMAC-SHA256 Credential=TESTKEY/20190416/eu-west-1/execute-api/aws4_request, SignedHeaders=host;x-amz-date, Signature=5bab220e8024d65bb55f462f94d2e61d36655495f98b909a5aa7692e41b49d57", headers.getAuthorizationString());
			assertEquals("20190416T100310Z", headers.getXAmzDate());
			
		} catch (DEPValidationException e) {
			fail("The DEPAuthentication object could not be built.");
		}
	}
}
