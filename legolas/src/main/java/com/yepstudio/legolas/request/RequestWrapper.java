package com.yepstudio.legolas.request;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.yepstudio.legolas.description.RequestDescription;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.OnResponseListener;

public class RequestWrapper {
	private final Request request;
	private final List<OnRequestListener> onRequestListeners;
	private final Map<Type, OnResponseListener<?>> onResponseListeners;
	private final List<OnErrorListener> onErrorListeners;
	//方法返回的类型
	private final Type result;
	
	public RequestWrapper(Request request,
			Type result,
			List<OnRequestListener> onRequestListeners,
			Map<Type, OnResponseListener<?>> onResponseListeners,
			List<OnErrorListener> onErrorListeners) {
		super();
		this.request = request;
		this.result = result;
		this.onRequestListeners = onRequestListeners;
		this.onResponseListeners = onResponseListeners;
		this.onErrorListeners = onErrorListeners;
	}

	public Request getRequest() {
		return request;
	}

	public List<OnRequestListener> getOnRequestListeners() {
		return onRequestListeners;
	}

	public Map<Type, OnResponseListener<?>> getOnResponseListeners() {
		return onResponseListeners;
	}

	public List<OnErrorListener> getOnErrorListeners() {
		return onErrorListeners;
	}

	public Type getResult() {
		return result;
	}
	
	public boolean isSynchronou() {
		return RequestDescription.hasSynchronousReturnType(result);
	}
}
