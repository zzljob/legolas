package com.yepstudio.legolas;

import com.yepstudio.legolas.request.OnRequestListener;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.OnResponseListener;

/**
 * Response投递
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月8日
 * @version 2.0，2014年4月30日
 */
public interface ResponseDelivery {

	public void submitRequest(OnRequestListener onRequestListener, Request request);

	public void postResponse(OnResponseListener<?> onResponseListener, Request request, Object result);

	public void postResponse(OnResponseListener<?> onResponseListener, Request request, Object result, Runnable runnable);

	public void postError(OnErrorListener onErrorListener, Request request, LegolasException error);
}
