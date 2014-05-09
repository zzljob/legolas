package com.yepstudio.legolas.request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.mime.ByteArrayBody;
import com.yepstudio.legolas.mime.RequestBody;

/**
 * 
 * @author zzljob@gmail.com
 * @createDate 2014年1月9日
 */
public class Request {

	private static LegolasLog log = LegolasLog.getClazz(Request.class);

	private final String uuid;
	private final String description;
	private final String url;
	private final String method;
	private final Map<String, String> headers;
	private final RequestBody body;

	public Request(String description, String method, String url, Map<String, String> headers, RequestBody body) {
		super();
		uuid = UUID.randomUUID().toString();
		this.description = description;
		this.method = method;
		this.url = url;
		this.headers = headers;
		this.body = body;
		log.i(String.format("new Request:[%s], method:%s, url:%s, headers:%s, body:%s", uuid, method, url, headers, body));
	}

	public String getMethod() {
		return method;
	}

	public String getUrl() {
		return url;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public RequestBody getBody() {
		return body;
	}

	@Deprecated
	public static Request readBodyToBytesIfNecessary(Request request) throws IOException {
		RequestBody body = request.getBody();
		if (body == null || body instanceof ByteArrayBody) {
			return request;
		}

		String bodyMime = body.mimeType();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		body.writeTo(baos);
		body = new ByteArrayBody(bodyMime, baos.toByteArray());

		return new Request("", request.getMethod(), request.getUrl(), request.getHeaders(), body);
	}

	public String getDescription() {
		return description;
	}

	public String getUuid() {
		return uuid;
	}

}
