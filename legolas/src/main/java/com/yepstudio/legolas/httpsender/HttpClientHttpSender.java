package com.yepstudio.legolas.httpsender;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

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

import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.mime.ByteArrayResponseBody;
import com.yepstudio.legolas.mime.FileResponseBody;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.mime.ResponseBody;
import com.yepstudio.legolas.mime.StreamResponseBody;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.Response;

public class HttpClientHttpSender implements HttpSender {
	
	private static HttpClient createDefaultClient(int connectTimeout, int readTimeout) {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, connectTimeout);
		HttpConnectionParams.setSoTimeout(params, readTimeout);
		return new DefaultHttpClient(params);
	}

	private final HttpClient client;

	public HttpClientHttpSender() {
		this(createDefaultClient(15 * 1000, 20 * 1000));
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
		return parseResponse(request, apacheResponse);
	}

	/** Execute the specified {@code request} using the provided {@code client}. */
	protected HttpResponse execute(HttpClient client, HttpUriRequest request) throws IOException {
		return client.execute(request);
	}

	static HttpUriRequest createRequest(Request request) {
		return new GenericHttpRequest(request);
	}

	static Response parseResponse(Request request, HttpResponse response) throws IOException {
		StatusLine statusLine = response.getStatusLine();
		int status = statusLine.getStatusCode();
		String reason = statusLine.getReasonPhrase();

		Map<String, String> headers = new HashMap<String, String>();
		String contentType = "application/octet-stream";
		for (org.apache.http.Header header : response.getAllHeaders()) {
			if (header == null) {
				continue;
			}
			String name = header.getName();
			String value = header.getValue();
			if (ResponseBody.Content_Type.equalsIgnoreCase(name)) {
				contentType = value;
			}
			headers.put(name, value);
		}

		HttpEntity entity = response.getEntity();
		if (entity != null) {
			long length = entity.getContentLength();
			InputStream stream;
			stream = entity.getContent();
			String encoding = null;
			org.apache.http.Header contentEncodingHeader = entity.getContentEncoding();
			if (contentEncodingHeader != null) {
				encoding = contentEncodingHeader.getValue();
			}
			
			if (stream == null) {
				return new Response(status, reason, headers);
			}
			
			InputStream inputStream;
			if (isGzipEncoding(encoding)) {
				inputStream = new GZIPInputStream(stream);
			} else if (isDeflateEncoding(encoding)) {
				inputStream = new InflaterInputStream(stream);
			} else {
				inputStream = stream;
			}
			ResponseBody responseBody = new StreamResponseBody(contentType, length, inputStream);
			
			long max_length = ByteArrayResponseBody.MAX_LIMIT_SIZE;
			if (0 <= responseBody.length() && responseBody.length() < max_length) {
				responseBody = ByteArrayResponseBody.build(responseBody);
			} else {
				File file = File.createTempFile("legolas_", "_temp");
				responseBody = FileResponseBody.build(responseBody, file);
			}
			
			stream.close();
			inputStream.close();
			entity.consumeContent();
			return new Response(status, reason, headers, responseBody);
		}

		return new Response(status, reason, headers);
	}
	
	static boolean isDeflateEncoding(String encoding) {
		if ("deflate".equalsIgnoreCase(encoding)) {
			return true;
		}
		if (encoding == null || "".equalsIgnoreCase(encoding.trim())) {
			return false;
		} else {
			return encoding.indexOf("deflate") > -1;
		}
	}
	
	static boolean isGzipEncoding(String encoding) {
		if ("gzip".equalsIgnoreCase(encoding)) {
			return true;
		}
		if (encoding == null || "".equalsIgnoreCase(encoding.trim())) {
			return false;
		} else {
			return encoding.indexOf("gzip") > -1;
		}
	}

	private static class GenericHttpRequest extends HttpEntityEnclosingRequestBase {
		private final String method;

		GenericHttpRequest(Request request) {
			super();
			method = request.getMethod();
			setURI(URI.create(request.getUrl()));

			// Add all headers.
			Map<String, String> headers = request.getHeaders();
			headers.put("Accept-Encoding", "gzip,deflate");
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
