package com.yepstudio.legolas.httpsender;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.Legolas;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.mime.ResponseBody;
import com.yepstudio.legolas.mime.StreamResponseBody;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.Response;

/** Network that uses {@link HttpURLConnection} for communication. */
public class UrlConnectionHttpSender implements HttpSender {

	private static final int CHUNK_SIZE = 4096; //4K
	private final Field methodField;
	private final int connectTimeout;
	private final int readTimeout;

	public UrlConnectionHttpSender() {
		this(15 * 1000, 20 * 1000);
	}
	
	public UrlConnectionHttpSender(int connectTimeout, int readTimeout) {
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		try {
			this.methodField = HttpURLConnection.class.getDeclaredField("method");
			this.methodField.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException();
		}
	}

	@Override
	public Response execute(Request request) throws IOException {
		Legolas.getLog().v("execute request : " + request.getUrl());
		HttpURLConnection connection = openConnection(request);
		prepareRequest(connection, request);
		return readResponse(request, connection);
	}

	protected HttpURLConnection openConnection(Request request) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(request.getUrl()).openConnection();
		connection.setConnectTimeout(connectTimeout);
		connection.setReadTimeout(readTimeout);
		Legolas.getLog().v(String.format("openConnection, ConnectTimeout:%s s, ReadTimeout:%s s", connectTimeout / 1000, readTimeout / 1000));
		return connection;
	}
	
	protected void prepareRequest(HttpURLConnection connection, Request request) throws IOException {
		Legolas.getLog().v(String.format("prepareRequest, setRequestMethod:%s", request.getMethod()));
		// HttpURLConnection artificially restricts request method
		try {
			connection.setRequestMethod(request.getMethod());
		} catch (ProtocolException e) {
			try {
				methodField.set(connection, request.getMethod());
			} catch (IllegalAccessException e1) {
				throw new RuntimeException();
			}
		}

		Legolas.getLog().v(String.format("----------------Request--------------"));
		
		connection.setDoInput(true);

		Map<String, String> headers = request.getHeaders();
		headers.put("Accept-Encoding", "gzip,deflate");
		for (String key : headers.keySet()) {
			Legolas.getLog().v(String.format("addRequestHeader: %s=>%s", key, headers.get(key)));
			connection.addRequestProperty(key, headers.get(key));
		}

		RequestBody body = request.getBody();
		if (body != null) {
			connection.setDoOutput(true);
			connection.addRequestProperty(RequestBody.Content_Type, body.mimeType());
			Legolas.getLog().v(String.format("addRequestHeader: Content-Type=>%s", body.mimeType()));
			long length = body.length();
			if (length != -1) {
				connection.setFixedLengthStreamingMode((int) length);
				connection.addRequestProperty(RequestBody.Content_Length, String.valueOf(length));
			} else {
				connection.setChunkedStreamingMode(CHUNK_SIZE);
			}
			body.writeTo(connection.getOutputStream());
		}
	}

	Response readResponse(Request request, HttpURLConnection connection) throws IOException {
		Legolas.getLog().v(String.format("------------------Response----------------------"));
		int status = connection.getResponseCode();
		String reason = connection.getResponseMessage();

		Map<String, String> headers = new HashMap<String, String>();
		for (Map.Entry<String, List<String>> field : connection.getHeaderFields().entrySet()) {
			String name = field.getKey();
			for (String value : field.getValue()) {
				headers.put(name, value);
				Legolas.getLog().v(String.format("header: %s=%s", name, value));
			}
		}

		String mimeType = connection.getContentType();
		int length = connection.getContentLength();
		InputStream stream;
		if (status >= 400) {
			stream = connection.getErrorStream();
		} else {
			stream = connection.getInputStream();
		}
		String encoding = connection.getContentEncoding();
		InputStream inputStream;
		if (isGzipEncoding(encoding)) {
			inputStream = new GZIPInputStream(stream);
		} else if (isDeflateEncoding(encoding)) {
			inputStream = new InflaterInputStream(stream);
		} else {
			inputStream = stream;
		}
		
		Legolas.getLog().v(String.format("status[%s] mimeType[%s], length[%s], stream[%s]", status, mimeType, length, inputStream));
		ResponseBody responseBody = new StreamResponseBody(mimeType, length, inputStream);
		return new Response(status, reason, headers, responseBody);
	}
	
	private boolean isDeflateEncoding(String encoding) {
		if ("deflate".equalsIgnoreCase(encoding)) {
			return true;
		}
		if (encoding == null || "".equalsIgnoreCase(encoding.trim())) {
			return false;
		} else {
			return encoding.indexOf("deflate") > -1;
		}
	}
	
	private boolean isGzipEncoding(String encoding) {
		if ("gzip".equalsIgnoreCase(encoding)) {
			return true;
		}
		if (encoding == null || "".equalsIgnoreCase(encoding.trim())) {
			return false;
		} else {
			return encoding.indexOf("gzip") > -1;
		}
	}
}
