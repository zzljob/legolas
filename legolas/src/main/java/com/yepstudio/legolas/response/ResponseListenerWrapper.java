package com.yepstudio.legolas.response;

import java.lang.reflect.Type;

import com.yepstudio.legolas.internal.TypesHelper;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.mime.ResponseBody;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.request.AsyncRequest;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2014年11月6日
 * @version 1.0，2014年11月6日
 *
 */
public class ResponseListenerWrapper {

	private final Type responseType;
	private final OnResponseListener listener;
	private Object responseValue;
	private boolean fromMemoryCache = false;

	public ResponseListenerWrapper(OnResponseListener listener, Type result) {
		super();
		this.responseType = result;
		this.listener = listener;
	}

	public OnResponseListener getListener() {
		return listener;
	}

	public Type getResponseType() {
		return responseType;
	}

	public Object getResponseValue() {
		return responseValue;
	}

	public void setResponseValue(Object value) {
		this.responseValue = value;
		this.fromMemoryCache = false;
	}
	
	public void setMemoryCacheValue(Object value) {
		this.responseValue = value;
		this.fromMemoryCache = true;
	}

	public boolean isFromMemoryCache() {
		return fromMemoryCache;
	}

	public boolean isShouldIgnore() {
		if (responseType == null 
				|| Void.class == responseType
				|| void.class == responseType) {
			return true;
		}
		if (listener == null) {
			return true;
		}
		return false;
	}
	
	public boolean isShouldCacheInMemory() {
		if (listener == null) {
			return false;
		}
		if (fromMemoryCache || responseType == null || responseValue == null) {
			return false;
		}
		Class<?> clazz = TypesHelper.getRawType(responseType);
		if (Void.class.equals(clazz)
				|| void.class.equals(clazz)
				|| Response.class.equals(clazz) 
				|| Request.class.equals(clazz)
				|| AsyncRequest.class.equals(clazz)
				|| RequestBody.class.isAssignableFrom(clazz) 
				|| ResponseBody.class.isAssignableFrom(clazz)) {
			return false;
		}
		return true;
	}

}
