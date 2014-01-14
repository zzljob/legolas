package com.yepstudio.android.legolas.handler;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import com.yepstudio.android.legolas.description.ApiDescription;
import com.yepstudio.android.legolas.description.RequestDescription;
import com.yepstudio.android.legolas.http.Request;
import com.yepstudio.android.legolas.http.RequestBuilder;
import com.yepstudio.android.legolas.http.RequestExecutor;
import com.yepstudio.android.legolas.http.Server;
import com.yepstudio.android.legolas.http.mime.ByteArrayBody;
import com.yepstudio.android.legolas.http.mime.RequestBody;
import com.yepstudio.android.legolas.log.LegolasLog;

/**
 * API接口的代理拦截类，用来拦截所有方法，并将参数网络请求的参数都解析出来
 * @author zzljob@gmail.com
 * @createDate 2013年12月27日
 */
public class ProxyHandler implements InvocationHandler {
	
	private static LegolasLog log = LegolasLog.getClazz(ProxyHandler.class);

	private final ApiDescription apiDescription;
	private final Server server;
	private final RequestExecutor executor;
	
	public ProxyHandler(ApiDescription apiDescription, Server server, RequestExecutor executor) {
		super();
		if (apiDescription == null) {
			log.e("apiContext can not be null");
			throw new RuntimeException("apiContext can not be null");
		}
		this.apiDescription = apiDescription;
		this.server = server;
		this.executor = executor;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Map<Method, RequestDescription> requestMap = apiDescription.getRequestMap();
		RequestDescription description = requestMap.get(method);
		//如果没有找到该方法的请求描述RequestDescription 则直接返回类型的默认值
		if(description == null) {
			log.e("have not @Request, is not http Request.");
			throw new RuntimeException("have not @Request, is not http Request.");
		}
		description.initCache();
		
		RequestBuilder builder = new RequestBuilder(apiDescription, method);
		if (server != null) {
			builder.setApiUrl(server.getUrl());
		}
		builder.setArguments(args);
		final Request request = builder.build();
		
		logRequest("HTTP", request);
		
		if (description.isSynchronous()) {
			return executor.syncRequest(request, description.getResponseType());
		}
		
		executor.doRequest(request);
		return null;
	}
	
	private void logRequest(String name, Request request) throws IOException {
		log.d(String.format("---> %s %s %s", name, request.getMethod(), request.getUrl()));

		Map<String, String> headers = request.getHeaders();
		for (String key : headers.keySet()) {
			log.d("header: " + key + "=>" + headers.get(key));
		}
		long bodySize = 0;
		RequestBody body = request.getBody();
		if (body != null) {
			bodySize = body.length();
			String bodyMime = body.mimeType();

			if (bodyMime != null) {
				log.d(RequestBody.Content_Type + " : " + bodyMime);
			}
			if (bodySize != -1) {
				log.d(RequestBody.Content_Length + " : " + bodySize);
			}

			if (!(body instanceof ByteArrayBody)) {
				// Read the entire response body to we can log it and replace
				// the original response
				request = Request.readBodyToBytesIfNecessary(request);
				body = request.getBody();
			}

			byte[] bodyBytes = ((ByteArrayBody) body).getBytes();
			bodySize = bodyBytes.length;
			String bodyCharset = "UTF-8";
			log.d(new String(bodyBytes, bodyCharset));

		}
		log.d(String.format("---> END %s (%s-byte body)", name, bodySize));
	}
	
}
