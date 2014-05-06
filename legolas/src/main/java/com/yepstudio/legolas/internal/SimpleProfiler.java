package com.yepstudio.legolas.internal;

import com.yepstudio.legolas.LegolasLog;
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
public class SimpleProfiler implements Profiler<Request> {
	private static LegolasLog log = LegolasLog.getClazz(SimpleProfiler.class);

	@Override
	public Request beforeCall(Request request) {
		log.d("beforeCall, request:[" + request.getUuid() + "]");
		return request;
	}

	@Override
	public void afterCall(Response response, long startTime, long elapsedTime, Request request) {
		log.d(String.format("afterCall, success:[%s], request:[%s], spend:[%s ms]", response != null, request.getUuid(), elapsedTime - startTime));
	}

}
