package com.yepstudio.legolas;

import java.util.Map;

public interface RequestInterceptorFace {

	/**
	 * 获取请求的URL，已经应用过Endpoint，但是Path参数没有替换的，不过包括Query参数
	 * @return
	 */
	public String getRequestUrl();

	/**
	 * 请求方式
	 * @return
	 */
	public String getRequestMethod();

	/**
	 * 请求方式：包含下面三个值
	 * <ul>
	 * <li>{@link com.yepstudio.legolas.description.RequestDescription.RequestType#SIMPLE}</li>
	 * <li>{@link com.yepstudio.legolas.description.RequestDescription.RequestType#MULTIPART}</li>
	 * <li>{@link com.yepstudio.legolas.description.RequestDescription.RequestType#FORM_URL_ENCODED}</li>
	 * </ul>
	 * @return
	 */
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
