package com.yepstudio.legolas;

import com.yepstudio.legolas.request.AsyncRequest;

/**
 * Response投递
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月8日
 * @version 2.0，2014年4月30日
 */
public interface ResponseDelivery {

	public void postAsyncRequest(AsyncRequest wrapper);
	
	public void postAsyncResponse(AsyncRequest wrapper);

	public void postAsyncError(AsyncRequest wrapper, LegolasException exception);

}
