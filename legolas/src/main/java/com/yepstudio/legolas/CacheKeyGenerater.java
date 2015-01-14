package com.yepstudio.legolas;

import java.lang.reflect.Type;

import com.yepstudio.legolas.request.Request;

public interface CacheKeyGenerater {

	public String generateKey(Request request);

	public String generateKey(Request request, Type type);

}
