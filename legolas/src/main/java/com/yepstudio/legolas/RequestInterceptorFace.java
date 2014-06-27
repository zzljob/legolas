package com.yepstudio.legolas;

import java.util.Map;

public interface RequestInterceptorFace {

	/**
	 * 获取请求的URL
	 * @param original 是否原始的请求地址，原始的地址就是Api的地址+Request的地址
	 * @return
	 */
	public String getRequestUrl(boolean original);

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

	/**
	 * 不包括在RequestInterceptor里边设置的
	 * @return
	 */
	public Map<String, Object> getHeaders();

	/**
	 * 不包括在RequestInterceptor里边设置的
	 * @return
	 */
	public Map<String, Object> getPathParams();

	/**
	 * 不包括在RequestInterceptor里边设置的
	 * @return
	 */
	public Map<String, Object> getQueryParams();

	/**
	 * 不包括在RequestInterceptor里边设置的
	 * @return
	 */
	public Map<String, Object> getFieldParams();

	/**
	 * 不包括在RequestInterceptor里边设置的
	 * @return
	 */
	public Map<String, Object> getPartParams();

	public Object getBodyParams();
	
	public Converter getConverter();

	public void addHeader(String name, Object value);

	public void addPathParam(String name, Object value);

	public void addQueryParam(String name, Object value);

	public void addFieldParam(String name, Object value);

	public void addPartParam(String name, Object value);
}
