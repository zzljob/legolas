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

import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.mime.ResponseBody;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.Response;

/** Network that uses {@link HttpURLConnection} for communication. */
public class UrlConnectionHttpSender implements HttpSender {
	private static LegolasLog log = LegolasLog.getClazz(UrlConnectionHttpSender.class);

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
		log.v("execute");
		HttpURLConnection connection = openConnection(request);
		prepareRequest(connection, request);
		return readResponse(request, connection);
	}

	protected HttpURLConnection openConnection(Request request) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(request.getUrl()).openConnection();
		connection.setConnectTimeout(connectTimeout);
		connection.setReadTimeout(readTimeout);
		log.v(String.format("openConnection, ConnectTimeout:%s s, ReadTimeout:%s s", connectTimeout / 1000, readTimeout / 1000));
		return connection;
	}
	
	protected void prepareRequest(HttpURLConnection connection, Request request) throws IOException {
		log.v(String.format("prepareRequest, setRequestMethod:%s", request.getMethod()));
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

		connection.setDoInput(true);

		Map<String, String> headers = request.getHeaders();
		for (String key : headers.keySet()) {
			log.v(String.format("addRequestProperty, %s=>%s", key, headers.get(key)));
			connection.addRequestProperty(key, headers.get(key));
		}

		RequestBody body = request.getBody();
		if (body != null) {
			connection.setDoOutput(true);
			connection.addRequestProperty(RequestBody.Content_Type, body.mimeType());
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
		log.v(String.format("readResponse"));
		int status = connection.getResponseCode();
		String reason = connection.getResponseMessage();

		Map<String, String> headers = new HashMap<String, String>();
		for (Map.Entry<String, List<String>> field : connection.getHeaderFields().entrySet()) {
			String name = field.getKey();
			for (String value : field.getValue()) {
				headers.put(name, value);
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
		ResponseBody responseBody = new StreamResponseBody(mimeType, length, stream);
		return new Response(request.getUuid(), status, reason, headers, responseBody);
	}

	private static class StreamResponseBody implements ResponseBody {
		private final String mimeType;
		private final long length;
		private final InputStream stream;

		private StreamResponseBody(String mimeType, long length, InputStream stream) {
			this.mimeType = mimeType;
			this.length = length;
			this.stream = stream;
		}

		@Override
		public String mimeType() {
			return mimeType;
		}

		@Override
		public long length() {
			return length;
		}

		@Override
		public InputStream read() throws IOException {
			return stream;
		}
	}
}
