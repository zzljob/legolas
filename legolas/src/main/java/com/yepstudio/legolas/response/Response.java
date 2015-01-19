package com.yepstudio.legolas.response;

import java.util.Map;

import org.apache.http.protocol.HTTP;

import com.yepstudio.legolas.Legolas;
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
	
	private boolean fromMemoryCache;
	private boolean fromDiskCache;

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
		Legolas.getLog().v(String.format("statusCode:%s, message:%s, headers:%s, responseBody:%s", statusCode, message, headers, responseBody));
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
	
	public static String parseCharset(Map<String, String> headers, String defaultCharset) {
		String contentType = headers.get(HTTP.CONTENT_TYPE);
		if (contentType != null) {
			String[] params = contentType.split(";");
			for (int i = 1; i < params.length; i++) {
				String[] pair = params[i].trim().split("=");
				if (pair.length == 2) {
					if (pair[0].equalsIgnoreCase("charset")) {
						return pair[1];
					}
				}
			}
		}

		return defaultCharset;
	}

	public void setFromMemoryCache(boolean fromMemoryCache) {
		this.fromMemoryCache = fromMemoryCache;
	}

	public void setFromDiskCache(boolean fromDiskCache) {
		this.fromDiskCache = fromDiskCache;
	}

}
