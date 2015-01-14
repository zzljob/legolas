package com.yepstudio.legolas;

import java.util.Map;

import com.yepstudio.legolas.mime.RequestBody;

public interface RequestInterceptorFace {

	public String getRequestUrl();

	public String getEncode();

	public String getRequestMethod();

	public RequestType getRequestType();

	public RequestBody getBody();

	public Map<String, String> getHeaders();

	public Map<String, String> getQuerys();

}
