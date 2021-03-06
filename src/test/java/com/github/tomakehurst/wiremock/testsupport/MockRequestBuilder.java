/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.testsupport;

import com.github.tomakehurst.wiremock.http.*;
import org.jmock.Expectations;
import org.jmock.Mockery;

import java.util.List;

import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;

public class MockRequestBuilder {

	private final Mockery context;
	private String url = "/";
	private RequestMethod method = GET;
    private List<HttpHeader> individualHeaders = newArrayList();
	private String body = "";
	private boolean browserProxyRequest = false;
	
	private String mockName;
	
	public MockRequestBuilder(Mockery context) {
		this.context = context;
	}
	
	public MockRequestBuilder(Mockery context, String mockName) {
		this.mockName = mockName;
		this.context = context;
	}
	
	public static MockRequestBuilder aRequest(Mockery context) {
		return new MockRequestBuilder(context);
	}
	
	public static MockRequestBuilder aRequest(Mockery context, String mockName) {
		return new MockRequestBuilder(context, mockName);
	}

	public MockRequestBuilder withUrl(String url) {
		this.url = url;
		return this;
	}

	public MockRequestBuilder withMethod(RequestMethod method) {
		this.method = method;
		return this;
	}

	public MockRequestBuilder withHeader(String key, String value) {
        individualHeaders.add(new HttpHeader(key, value));
		return this;
	}
	
	public MockRequestBuilder withBody(String body) {
		this.body = body;
		return this;
	}
	
	public MockRequestBuilder asBrowserProxyRequest() {
		this.browserProxyRequest = true;
		return this;
	}
	
	public Request build() {
        final HttpHeaders headers = new HttpHeaders(individualHeaders);

		final Request request = mockName == null ? context.mock(Request.class) : context.mock(Request.class, mockName);
		context.checking(new Expectations() {{
			allowing(request).getUrl(); will(returnValue(url));
			allowing(request).getMethod(); will(returnValue(method));
			for (HttpHeader header: headers.all()) {
				allowing(request).containsHeader(header.key()); will(returnValue(true));
				allowing(request).getHeader(header.key()); will(returnValue(header.firstValue()));
			}

            for (HttpHeader header: headers.all()) {
                allowing(request).header(header.key()); will(returnValue(header));
                if (header.key().equals(ContentTypeHeader.KEY) && header.isPresent()) {
                    allowing(request).contentTypeHeader(); will(returnValue(new ContentTypeHeader(header.firstValue())));
                }
            }
            allowing(request).header(with(any(String.class))); will(returnValue(httpHeader("key", "value")));

            allowing(request).getHeaders(); will(returnValue(headers));
			allowing(request).getAllHeaderKeys(); will(returnValue(newLinkedHashSet(headers.keys())));
			allowing(request).containsHeader(with(any(String.class))); will(returnValue(false));
			allowing(request).getBodyAsString(); will(returnValue(body));
			allowing(request).getAbsoluteUrl(); will(returnValue("http://localhost:8080" + url));
			allowing(request).isBrowserProxyRequest(); will(returnValue(browserProxyRequest));
		}});
		
		return request;
	}
}
