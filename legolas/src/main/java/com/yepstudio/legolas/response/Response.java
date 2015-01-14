package com.yepstudio.legolas.response;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.protocol.HTTP;

import com.yepstudio.legolas.mime.ResponseBody;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月14日
 * @version 2.0，2014年4月30日
 */
public final class Response {
	
	/** The HTTP status code. */
	private final int statusCode;

	/** The HTTP Message. */
	private final String message;

	/** Response headers. */
	private final Map<String, String> headers;
	
	/** Response Body. */
	private final ResponseBody responseBody;
	
	private final boolean fromMemoryCache;
	private final boolean fromDiskCache;

	public Response(int statusCode, String message, Map<String, String> headers) {
		this(statusCode, message, headers, null, false, false);
	}
	
	public Response(int statusCode, String message, Map<String, String> headers, ResponseBody responseBody) {
		this(statusCode, message, headers, responseBody, false, false);
	}
	
	public Response(int statusCode, String message, Map<String, String> headers, ResponseBody responseBody, boolean fromMemoryCache, boolean fromDiskCache) {
		super();
		this.statusCode = statusCode;
		this.message = message;
		this.headers = headers;
		this.responseBody = responseBody;
		this.fromMemoryCache = fromMemoryCache;
		this.fromDiskCache = fromDiskCache;
		//log.v(String.format("statusCode:%s, message:%s, headers:%s, responseBody:%s", statusCode, message, headers, responseBody));
	}

	public int getStatus() {
		return statusCode;
	}

	public String getMessage() {
		return message;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public ResponseBody getBody() {
		return responseBody;
	}
	
	public String parseCharset(String defaultCharset) {
		return parseCharset(this.headers, defaultCharset);
	}
	
	public String parseCharset() {
		return this.parseCharset(HTTP.DEFAULT_CONTENT_CHARSET);
	}
	
	public boolean isFromCache() {
		return fromMemoryCache || fromDiskCache;
	}

	public boolean isFromMemoryCache() {
		return fromMemoryCache;
	}

	public boolean isFromDiskCache() {
		return fromDiskCache;
	}
	
	private static final Pattern CHARSET = Pattern.compile("\\Wcharset=([^\\s;]+)", CASE_INSENSITIVE);
	
	public static String parseCharset(Map<String, String> headers, String defaultCharset) {
		String contentType = headers.get(HTTP.CONTENT_TYPE);
		if (contentType != null) {
			String[] params = contentType.split(";");
			for (int i = 1; i < params.length; i++) {
				String[] pair = params[i].trim().split("=");
				if (pair.length == 2) {
					if (pair[0].equals("charset")) {
						return pair[1];
					}
				}
			}
		}

		return defaultCharset;
	}

	public static String parseCharset(String mimeType, String defaultCharset) {
		if (mimeType == null || mimeType.trim().length() < 1) {
			return defaultCharset;
		}
		Matcher match = CHARSET.matcher(mimeType);
		if (match.find()) {
			return match.group(1).replaceAll("[\"\\\\]", "");
		}
		return defaultCharset;
	}

	private static final int BUFFER_SIZE = 0x1000;
	public static byte[] streamToBytes(InputStream stream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (stream != null) {
			byte[] buf = new byte[BUFFER_SIZE];
			int r;
			while ((r = stream.read(buf)) != -1) {
				baos.write(buf, 0, r);
			}
		}
		return baos.toByteArray();
	}

}
