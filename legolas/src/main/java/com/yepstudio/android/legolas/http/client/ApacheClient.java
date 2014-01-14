package com.yepstudio.android.legolas.http.client;

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

import com.yepstudio.android.legolas.http.Request;
import com.yepstudio.android.legolas.http.Response;
import com.yepstudio.android.legolas.http.mime.ByteArrayBody;
import com.yepstudio.android.legolas.http.mime.RequestBody;
import com.yepstudio.android.legolas.http.mime.ResponseBody;

/** A {@link Client} which uses an implementation of Apache's {@link HttpClient}. */
public class ApacheClient implements Client {
	
	private static HttpClient createDefaultClient() {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 15 * 1000);
		HttpConnectionParams.setSoTimeout(params, 20 * 1000);
		return new DefaultHttpClient(params);
	}

	private final HttpClient client;

	/** Creates an instance backed by {@link DefaultHttpClient}. */
	public ApacheClient() {
		this(createDefaultClient());
	}

	public ApacheClient(HttpClient client) {
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
		final RequestBody typedOutput;

		EntityRequestBody(RequestBody typedOutput) {
			this.typedOutput = typedOutput;
			setContentType(typedOutput.mimeType());
		}

		@Override
		public boolean isRepeatable() {
			return true;
		}

		@Override
		public long getContentLength() {
			return typedOutput.length();
		}

		@Override
		public InputStream getContent() throws IOException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			typedOutput.writeTo(out);
			return new ByteArrayInputStream(out.toByteArray());
		}

		@Override
		public void writeTo(OutputStream out) throws IOException {
			typedOutput.writeTo(out);
		}

		@Override
		public boolean isStreaming() {
			return false;
		}
	}
}