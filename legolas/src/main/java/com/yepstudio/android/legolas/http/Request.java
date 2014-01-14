package com.yepstudio.android.legolas.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import com.yepstudio.android.legolas.http.mime.ByteArrayBody;
import com.yepstudio.android.legolas.http.mime.RequestBody;
import com.yepstudio.android.legolas.log.LegolasLog;

/**
 * 
 * @author zzljob@gmail.com
 * @createDate 2014年1月9日
 */
public class Request {
	private static LegolasLog log = LegolasLog.getClazz(Request.class);
	
    private final String method;
    private final String url;
    private final Map<String, String> headers;
    private final RequestBody body;
    
    private Request.RequestListener requestListener; 
	private Response.Listener<?> responseListener;
	private Response.ErrorListener errorListener;
	private Type result; 
    
	public interface RequestListener {

		public void onRequest();
		
	}
    
	public Request(String method, String url, Map<String, String> headers) {
		this(method, url, headers, null);
	}
	
	public Request(String method, String url, Map<String, String> headers, RequestBody body) {
		super();
		this.method = method;
		this.url = url;
		this.headers = headers;
		this.body = body;
		log.v(String.format("new Request, method:%s, url:%s, headers:%s, body:%s", method, url, headers, body));
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

	public Request.RequestListener getRequestListener() {
		return requestListener;
	}

	public void setRequestListener(Request.RequestListener requestListener) {
		this.requestListener = requestListener;
	}

	public Response.Listener<?> getResponseListener() {
		return responseListener;
	}

	public void setResponseListener(Response.Listener<?> responseListener) {
		this.responseListener = responseListener;
	}

	public Response.ErrorListener getErrorListener() {
		return errorListener;
	}

	public void setErrorListener(Response.ErrorListener errorListener) {
		this.errorListener = errorListener;
	}
	
	public static Request readBodyToBytesIfNecessary(Request request) throws IOException {
		RequestBody body = request.getBody();
		if (body == null || body instanceof ByteArrayBody) {
			return request;
		}

		String bodyMime = body.mimeType();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		body.writeTo(baos);
		body = new ByteArrayBody(bodyMime, baos.toByteArray());

		return new Request(request.getMethod(), request.getUrl(), request.getHeaders(), body);
	}

	public Type getResult() {
		return result;
	}

	public void setResult(Type result) {
		this.result = result;
	}

}
