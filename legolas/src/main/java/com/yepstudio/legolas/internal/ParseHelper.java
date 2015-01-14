package com.yepstudio.legolas.internal;

import java.util.List;

import com.yepstudio.legolas.RequestInterceptor;
import com.yepstudio.legolas.annotation.Interceptors;

public class ParseHelper {
	
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
