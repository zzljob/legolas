package com.yepstudio.legolas.internal;

import com.yepstudio.legolas.Profiler;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.Response;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年4月24日
 * @version 2.0, 2014年4月24日
 *
 */
public class SimpleProfiler implements Profiler<Object> {

	@Override
	public Object beforeCall(Request request) {
		return null;
	}

	@Override
	public void afterCall(Response response, long startTime, long elapsedTime, Object beforeCallData) {
		
	}

}
