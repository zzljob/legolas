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
public class SimpleProfiler implements Profiler<Void> {
	
	private static Logger logger = LoggerFactory.getLogger(SimpleProfiler.class);

	@Override
	public Void beforeCall(Request request) {
		logger.debug("beforeCall....");
		return null;
	}

	@Override
	public void afterCall(Request request, Response response, LegolasException exception, Void before) {
		logger.debug("afterCall....Ready[{}ms], Spend[{}ms]", request.getReadyTime(), request.getSpendTime());
	}

	@Override
	public void cancelCall(Request request, Response response, Void beforeCallData) {
		logger.debug("cancelCall....");
	}

}
