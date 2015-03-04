package com.yepstudio.legolas.listener;

import java.lang.reflect.Type;

import com.yepstudio.legolas.internal.TypesHelper;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.mime.ResponseBody;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.request.AsyncRequest;
import com.yepstudio.legolas.response.Response;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2014年11月6日
 * @version 1.0，2014年11月6日
 *
 */
public class LegolasListenerWrapper {

	private final Type responseType;
	private final Type errorType;
	private final LegolasListener<?, ?> listener;
	private Object responseValue;
	private boolean responseFromMemoryCache = false;
	private Object errorValue;

	public LegolasListenerWrapper(LegolasListener<?, ?> listener, Type responseType, Type errorType) {
		super();
		this.listener = listener;
		this.responseType = responseType;
		this.errorType = errorType;
	}

	public LegolasListener getListener() {
		return listener;
	}

	public Type getResponseType() {
		return responseType;
	}

	public Type getErrorType() {
		return errorType;
	}

	public Object getResponseValue() {
		return responseValue;
	}

	public void setResponseValue(Object resultValue) {
		this.responseValue = resultValue;
	}

	public void setMemoryCacheResponseValue(Object resultValue) {
		this.responseValue = resultValue;
		this.responseFromMemoryCache = true;
	}
	
	public boolean isShouldIgnoreResponse() {
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
	
	public boolean isShouldCacheResponseInMemory() {
		if (listener == null) {
			return false;
		}
		if (responseFromMemoryCache || responseType == null || responseValue == null) {
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

	public Object getErrorValue() {
		return errorValue;
	}

	public void setErrorValue(Object errorValue) {
		this.errorValue = errorValue;
	}

	public boolean isResponseFromMemoryCache() {
		return responseFromMemoryCache;
	}

}
