package com.yepstudio.legolas.internal;

import java.util.List;
import java.util.Map;

import com.yepstudio.legolas.RequestInterceptor;
import com.yepstudio.legolas.annotation.Headers;
import com.yepstudio.legolas.annotation.Interceptors;
import com.yepstudio.legolas.annotation.Item;

public class ParseHelper {
	

	/**
	 * 从@Headers 解析出header
	 * 
	 * @param headerMap
	 * @param headers
	 */
	public static void parseHeaders(Map<String, String> headerMap, Headers headers) {
		if (headerMap == null || headers == null) {
			return;
		}
		Item[] headerArray = headers.value();
		if (headerArray != null) {
			for (Item headerStr : headerArray) {
				headerMap.put(headerStr.key(), headerStr.value());
			}
		}
	}
	
	public static void parseInterceptors(List<RequestInterceptor> list, Interceptors interceptors) {
		if (list == null || interceptors == null) {
			return;
		}
		for (Class<? extends RequestInterceptor> clazz : interceptors.value()) {
			RequestInterceptor interceptor = null;
			try {
				interceptor = clazz.newInstance();
			} catch (InstantiationException e) {
				throw new IllegalArgumentException("RequestInterceptor can be newInstance ");
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("RequestInterceptor can be newInstance ");
			}
			list.add(interceptor);
		}
	}

}
