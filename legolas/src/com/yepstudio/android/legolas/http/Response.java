package com.yepstudio.android.legolas.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.http.protocol.HTTP;

import com.yepstudio.android.legolas.error.LegolasError;
import com.yepstudio.android.legolas.http.mime.ByteArrayBody;
import com.yepstudio.android.legolas.http.mime.ResponseBody;
import com.yepstudio.android.legolas.log.LegolasLog;

/**
 * 
 * @author zhangzl@fund123.cn
 * @createDate 2014年1月14日
 */
public class Response {
	
	private static LegolasLog log = LegolasLog.getClazz(Response.class);

	/** The HTTP status code. */
	private final int statusCode;

	/** The HTTP Message. */
	private final String message;

	/** Response headers. */
	private final Map<String, String> headers;
	
	/** Response Body. */
	private final ResponseBody responseBody;

	public Response(int statusCode, String message, Map<String, String> headers) {
		this(statusCode, message, headers, null);
	}
	
	public Response(int statusCode, String message, Map<String, String> headers, ResponseBody responseBody) {
		super();
		this.statusCode = statusCode;
		this.message = message;
		this.headers = headers;
		this.responseBody = responseBody;
		log.v(String.format("statusCode:%s, message:%s, headers:%s, responseBody:%s", statusCode, message, headers, responseBody));
	}

	public String parseCharset() {
		return parseCharset(this.headers);
	}
	
	public static String parseCharset(Map<String, String> headers) {
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

		return HTTP.DEFAULT_CONTENT_CHARSET;
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
	
	/**
	 * T是要转换成的类型，从Response=>T<br/>
	 * 转换是通过Converter接口转换的<br/>
	 * 会根据T的Type的类型去查找Converter接口，如果没找到就会调用默认的Converter=>JSON
	 * @author zzljob@gmail.com
	 * @createDate 2014年1月8日
	 */
	public interface OnResponseListener<T> {

		public void onResponse(T response);
		
	}

	public interface OnErrorListener {

		public void onError(LegolasError error);
		
	}
	
	private static final int BUFFER_SIZE = 0x1000;
	
	public static Response readBodyToBytesIfNecessary(Response response) throws IOException {
		ResponseBody body = response.getBody();
		if (body == null || body instanceof ByteArrayBody) {
			return response;
		}

		String bodyMime = body.mimeType();
		InputStream is = body.read();
		try {
			byte[] bodyBytes = streamToBytes(is);
			body = new ByteArrayBody(bodyMime, bodyBytes);

			return new Response(response.getStatus(), response.getMessage(), response.getHeaders(), body);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

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
