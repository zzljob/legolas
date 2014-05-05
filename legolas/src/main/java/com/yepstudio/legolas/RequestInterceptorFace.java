package com.yepstudio.legolas;

import java.util.Map;

public interface RequestInterceptorFace {

	public String getRequestUrl();

	public String getRequestMethod();

	public int getRequestType();

	public Map<String, Object> getHeaders();

	public Map<String, Object> getPathParams();

	public Map<String, Object> getQueryParams();

	public Map<String, Object> getFieldParams();

	public Map<String, Object> getPartParams();

	public Object getBodyParams();

	public void addHeader(String name, Object value);

	public void addPathParam(String name, Object value);

	public void addQueryParam(String name, Object value);

	public void addFieldParam(String name, Object value);

	public void addPartParam(String name, Object value);

}
