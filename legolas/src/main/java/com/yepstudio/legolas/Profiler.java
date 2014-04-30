package com.yepstudio.legolas;

import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.Response;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年4月24日
 * @version 2.0, 2014年4月24日
 *
 * @param <T>
 */
public interface Profiler<T> {
	
	public T beforeCall(Request request);

	public void afterCall(Response response, long startTime, long elapsedTime, T beforeCallData);

}
