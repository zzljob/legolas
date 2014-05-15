package com.yepstudio.legolas.internal.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.mime.ByteArrayBody;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.mime.ResponseBody;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.Response;

/** A {@link HttpSender} which uses an implementation of Apache's {@link HttpClient}. */
public class HttpClientHttpSender implements HttpSender {
	
	private static HttpClient createDefaultClient(int connectTimeout, int readTimeout) {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, connectTimeout);
		HttpConnectionParams.setSoTimeout(params, readTimeout);
		return new DefaultHttpClient(params);
	}

	private final HttpClient client;

	/** Creates an instance backed by {@link DefaultHttpClient}. */
	public HttpClientHttpSender() {
		this(createDefaultClient(12 * 1000, 20 * 1000));
	}
	
	public HttpClientHttpSender(int connectTimeout, int readTimeout) {
		this(createDefaultClient(connectTimeout, readTimeout));
	}

	public HttpClientHttpSender(HttpClient client) {
		this.client = client;
	}

	@Override
	public Response execute(Request request) throws IOException {
		HttpUriRequest apacheRequest = createRequest(request);
		HttpResponse apacheResponse = execute(client, apacheRequest);
		return parseResponse(apacheResponse);
	}

	/** Execute the specified {@code request} using the provided {@code client}. */
	protected HttpResponse execute(HttpClient client, HttpUriRequest request) throws IOException {
		return client.execute(request);
	}

	static HttpUriRequest createRequest(Request request) {
		return new GenericHttpRequest(request);
	}

	static Response parseResponse(HttpResponse response) throws IOException {
		StatusLine statusLine = response.getStatusLine();
		int status = statusLine.getStatusCode();
		String reason = statusLine.getReasonPhrase();

		Map<String, String> headers = new HashMap<String, String>();
		String contentType = "application/octet-stream";
		for (org.apache.http.Header header : response.getAllHeaders()) {
			String name = header.getName();
			String value = header.getValue();
			if (ResponseBody.Content_Type.equalsIgnoreCase(name)) {
				contentType = value;
			}
			headers.put(name, value);
		}

		ByteArrayBody body = null;
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			byte[] bytes = EntityUtils.toByteArray(entity);
			body = new ByteArrayBody(contentType, bytes);
		}

		return new Response(status, reason, headers, body);
	}

	private static class GenericHttpRequest extends HttpEntityEnclosingRequestBase {
		private final String method;

		GenericHttpRequest(Request request) {
			super();
			method = request.getMethod();
			setURI(URI.create(request.getUrl()));

			// Add all headers.
			Map<String, String> headers = request.getHeaders();
			for (String key : headers.keySet()) {
				addHeader(new BasicHeader(key, headers.get(key)));
			}

			// Add the content body, if any.
			RequestBody body = request.getBody();
			if (body != null) {
				setEntity(new EntityRequestBody(body));
			}
		}

		@Override
		public String getMethod() {
			return method;
		}
	}

	/** Container class for passing an entire {@link RequestBody} as an {@link HttpEntity}. */
	static class EntityRequestBody extends AbstractHttpEntity {
		final RequestBody requestBody;

		EntityRequestBody(RequestBody typedOutput) {
			this.requestBody = typedOutput;
			setContentType(typedOutput.mimeType());
		}

		@Override
		public boolean isRepeatable() {
			return true;
		}

		@Override
		public long getContentLength() {
			return requestBody.length();
		}

		@Override
		public InputStream getContent() throws IOException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			requestBody.writeTo(out);
			return new ByteArrayInputStream(out.toByteArray());
		}

		@Override
		public void writeTo(OutputStream out) throws IOException {
			requestBody.writeTo(out);
		}

		@Override
		public boolean isStreaming() {
			return false;
		}
	}
}
