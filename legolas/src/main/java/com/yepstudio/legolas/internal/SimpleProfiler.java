package com.yepstudio.legolas.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yepstudio.legolas.LegolasException;
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
	
	private static Logger logger = LoggerFactory.getLogger(SimpleProfiler.class);

	@Override
	public Request beforeCall(Request request) {
		logger.debug("beforeCall....");
		return request;
	}

	@Override
	public void afterCall(Response response, LegolasException exception, Request request) {
		logger.debug("afterCall....Ready[{}ms], Spend[{}ms]", request.getReadyTime(), request.getSpendTime());
	}

	@Override
	public void cancelCall(Request beforeCallData) {
		logger.debug("cancelCall....");
	}

}
