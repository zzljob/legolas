package com.yepstudio.legolas.internal;

import com.yepstudio.legolas.LegolasException;
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
	public void afterCall(Response response, LegolasException exception, long startTime, Request request) {
		long elapsedTime = System.currentTimeMillis();
		log.d(String.format("afterCall, success:[%s], request:[%s], spend:[%s ms]", exception == null, request.getUuid(), elapsedTime - startTime));
	}

	@Override
	public void cancelCall(Request beforeCallData) {
		log.d(String.format("cancelCall : ", beforeCallData.getUuid()));
	}

}
