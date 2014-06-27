package com.yepstudio.legolas.internal;

import com.yepstudio.legolas.LegolasException;
import com.yepstudio.legolas.Profiler;
import com.yepstudio.legolas.ProfilerDelivery;
import com.yepstudio.legolas.request.RequestWrapper;
import com.yepstudio.legolas.response.Response;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年5月15日
 * @version 2.0, 2014年5月15日
 *
 */
public class SimpleProfilerDelivery implements ProfilerDelivery {

	private final Profiler profiler;

	public SimpleProfilerDelivery(Profiler<?> profiler) {
		super();
		this.profiler = profiler;
	}

	@Override
	public void postBeforeCall(RequestWrapper wrapper) {
		if (profiler == null) {
			return;
		}
		wrapper.setStartTime(System.currentTimeMillis());
		wrapper.setBeforeCallData(profiler.beforeCall(wrapper.getRequest()));
	}

	@Override
	public void postAfterCall(RequestWrapper wrapper, Response response, LegolasException exception) {
		if (profiler == null) {
			return;
		}
		profiler.afterCall(response, exception, wrapper.getStartTime(), wrapper.getBeforeCallData());
	}

	@Override
	public void postCancelCall(RequestWrapper wrapper) {
		if (profiler == null) {
			return;
		}
		profiler.cancelCall(wrapper.getBeforeCallData());
	}

}
