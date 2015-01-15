package com.yepstudio.legolas.request;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.Legolas;
import com.yepstudio.legolas.LegolasOptions;
import com.yepstudio.legolas.listener.LegolasListenerWrapper;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.ResponseListenerWrapper;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年12月18日
 * @version 1.0，2014年12月18日
 *
 */
public final class AsyncRequest extends BasicRequest  {

	// 异步的请求参数
	private final List<OnRequestListener> onRequestListeners;
	private final List<ResponseListenerWrapper> onResponseListeners;
	private final List<OnErrorListener> onErrorListeners;
	private final List<LegolasListenerWrapper> onLegolasListeners;
	protected final AtomicBoolean retry = new AtomicBoolean(false);
	
	public AsyncRequest(String url,
			String method, 
			String description,
			Map<String, String> headers,
			RequestBody body,
			LegolasOptions options,
			Converter converter, 
			List<OnRequestListener> onRequestListeners,
			List<ResponseListenerWrapper> onResponseListeners,
			List<OnErrorListener> onErrorListeners,
			List<LegolasListenerWrapper> onLegolasListeners) {
		
		super(url, method, description, headers, body, options, converter);
		
		this.onRequestListeners = onRequestListeners;
		this.onResponseListeners = onResponseListeners;
		this.onErrorListeners = onErrorListeners;
		this.onLegolasListeners = onLegolasListeners;
	}

	public List<LegolasListenerWrapper> getOnLegolasListeners() {
		return onLegolasListeners;
	}

	public List<ResponseListenerWrapper> getOnResponseListeners() {
		return onResponseListeners;
	}

	public List<OnRequestListener> getOnRequestListeners() {
		return onRequestListeners;
	}

	public List<OnErrorListener> getOnErrorListeners() {
		return onErrorListeners;
	}

	public synchronized boolean isAllResponseListenerHasValue() {
		if (onResponseListeners != null) {
			for (ResponseListenerWrapper wrapper : onResponseListeners) {
				if (wrapper == null || wrapper.getListener() == null) {
					continue;
				}
				if (wrapper.getResponseValue() == null) {
					return false;
				}
			}
		}
		if (onLegolasListeners != null) {
			for (LegolasListenerWrapper wrapper : onLegolasListeners) {
				if (wrapper == null || wrapper.getListener() == null) {
					continue;
				}
				if (wrapper.getResponseValue() == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	public synchronized boolean isRetry() {
		return retry.get();
	}
	
	public synchronized boolean retry() {
		if (!isFinished()) {
			return false;
		}
		retry.set(true);
		cancel.set(false);
		startRequestTime = null;
		finishRequestTime = null;
		finishTime = null;
		allFinished.set(false);
		profilerExpansion = null;
		Legolas.getInstance().asyncRequest(this);
		return true;
	}

}
