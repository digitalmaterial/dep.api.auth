package com.mtn.dep.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class QueryStringProcessorTest {

	@Test
	public void testThatKeyAndValueAreEncoded() {
		QueryStringProcessor queryStringProcessor = new QueryStringProcessor();
		
		queryStringProcessor.addQueryParameter("=", "=");
		queryStringProcessor.addQueryParameter("a", " ");
		queryStringProcessor.addQueryParameter("1", ":");
		queryStringProcessor.addQueryParameter("expand", "subscription(status=2,page=1,svc_id=1)");
		
		assertEquals("%3D=%3D&1=%3A&a=%20&expand=subscription%28status%3D2%2Cpage%3D1%2Csvc_id%3D1%29", queryStringProcessor.getQueryString(true));
	}
	
	@Test
	public void testThatASpaceAreCorrectlyEncoded() {
		QueryStringProcessor queryStringProcessor = new QueryStringProcessor();
		
		queryStringProcessor.addQueryParameter(" ", " ");
		
		assertEquals("%20=%20", queryStringProcessor.getQueryString(true));
	}
	
	@Test
	public void testThatSortingIsByKey() {
		QueryStringProcessor queryStringProcessor = new QueryStringProcessor();
		
		queryStringProcessor.addQueryParameter("b", "2");
		queryStringProcessor.addQueryParameter("F", "1");
		
		assertEquals("F=1&b=2", queryStringProcessor.getQueryString(false));
	}
	
	@Test
	public void testThatDuplicateKeysAreAllowed() {
		QueryStringProcessor queryStringProcessor = new QueryStringProcessor();
		
		queryStringProcessor.addQueryParameter("key", "F");
		queryStringProcessor.addQueryParameter("key", "1");
		queryStringProcessor.addQueryParameter("key", "b");
		
		assertEquals("key=1&key=F&key=b", queryStringProcessor.getQueryString(false));
	}
	
	@Test
	public void testThatSortingIsByKeyAndValueOnDuplicateKey() {
		QueryStringProcessor queryStringProcessor = new QueryStringProcessor();
		
		queryStringProcessor.addQueryParameter("a", "b");
		queryStringProcessor.addQueryParameter("a", "F");
		
		assertEquals("a=F&a=b", queryStringProcessor.getQueryString(false));
	}
	
	@Test
	public void testThatDuplicateKeyAndValueAreNotSupported() {
		QueryStringProcessor queryStringProcessor = new QueryStringProcessor();
		
		queryStringProcessor.addQueryParameter("key", "value1");
		queryStringProcessor.addQueryParameter("key", "value1");
		
		assertEquals("key=value1", queryStringProcessor.getQueryString(false));
	}
	
	@Test
	public void testThatKeysAndValuesAreNotEncoded() {
		QueryStringProcessor queryStringProcessor = new QueryStringProcessor();
		
		queryStringProcessor.addQueryParameter("=", "=");
		
		assertEquals("===", queryStringProcessor.getQueryString(false));
	}
	
	@Test
	public void testThatEndcodedKeysAndValuesAreNotDoubleEncoded() {
		QueryStringProcessor queryStringProcessor = new QueryStringProcessor();
		
		queryStringProcessor.addQueryParameter("%3D", "%3D");
		
		assertEquals("%3D=%3D", queryStringProcessor.getQueryString(false));
	}
	
	@Test
	public void testThatUnreservedCharactersAreNotEncoded() {
		QueryStringProcessor queryStringProcessor = new QueryStringProcessor();
		
		queryStringProcessor.addQueryParameter("-", "1");
		queryStringProcessor.addQueryParameter("A", "=");
		queryStringProcessor.addQueryParameter("_", ".");
		queryStringProcessor.addQueryParameter("~", " ");
		
		assertEquals("-=1&A=%3D&_=.&~=%20", queryStringProcessor.getQueryString(true));
	}
	
	@Test
	public void testThatOrderRemainsForUnsortedQueryString() {
		QueryStringProcessor queryStringProcessor = new QueryStringProcessor();
		
		queryStringProcessor.addQueryParameter("B", "b");
		queryStringProcessor.addQueryParameter("A", "a");
		
		assertEquals("B=b&A=a", queryStringProcessor.getUnsortedQueryString(false));

	}
	
	@Test
	public void testThatStringIsEncodedForUnsortedList() {
		QueryStringProcessor queryStringProcessor = new QueryStringProcessor();
		
		queryStringProcessor.addQueryParameter("=", " ");
		
		assertEquals("%3D=%20", queryStringProcessor.getUnsortedQueryString(true));

	}
	
	@Test
	public void testThatUnreservedCharactersAreNotEncodedForUnsortedQueryString() {
		QueryStringProcessor queryStringProcessor = new QueryStringProcessor();
		
		queryStringProcessor.addQueryParameter("-", "1");
		queryStringProcessor.addQueryParameter("A", "=");
		queryStringProcessor.addQueryParameter("_", ".");
		queryStringProcessor.addQueryParameter("~", " ");
		
		assertEquals("-=1&A=%3D&_=.&~=%20", queryStringProcessor.getUnsortedQueryString(true));
	}
	
}
