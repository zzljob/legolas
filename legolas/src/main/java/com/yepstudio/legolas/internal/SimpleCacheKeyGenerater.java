package com.yepstudio.legolas.internal;

import java.lang.reflect.Type;

import com.yepstudio.legolas.CacheKeyGenerater;
import com.yepstudio.legolas.request.Request;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2015年1月14日
 * @version 1.0，2015年1月14日
 *
 */
public class SimpleCacheKeyGenerater implements CacheKeyGenerater {

	@Override
	public String generateKey(Request request) {
		return String.format("%s:%s", request.getMethod(), request.getUrl());
	}

	@Override
	public String generateKey(Request request, Type type) {
		return String.format("%s:%s#%s", generateKey(request), type);
	}

}
