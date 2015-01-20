package com.yepstudio.legolas;

import java.util.Map;

import com.yepstudio.legolas.mime.RequestBody;

public interface RequestInterceptorFace {

	/**
	 * 被解析过的URL，也就是请求时候的URL，但是不带Query参数
	 * @return
	 */
	public String getRequestUrl();

	public String getEncode();

	public String getRequestMethod();

	public RequestType getRequestType();

	public RequestBody getBody();

	public Map<String, String> getHeaders();

	public Map<String, String> getQuerys();

}
