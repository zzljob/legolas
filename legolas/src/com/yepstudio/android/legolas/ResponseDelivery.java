package com.yepstudio.android.legolas;

import com.yepstudio.android.legolas.error.LegolasError;
import com.yepstudio.android.legolas.http.Request;

/**
 * Response投递
 * @author zzljob@gmail.com
 * @createDate 2014年1月8日
 */
public interface ResponseDelivery {
	
	/**
	 * Submit a request from the network and delivers it.
	 */
	public void submitRequest(Request request);
	
	/**
	 * Parses a response from the network or cache and delivers it.
	 */
	public void postResponse(Request request, Object result);

	/**
	 * Parses a response from the network or cache and delivers it. The provided
	 * Runnable will be executed after delivery.
	 */
	public void postResponse(Request request, Object result, Runnable runnable);

	/**
	 * Posts an error for the given request.
	 */
	public void postError(Request request, LegolasError error);
}
