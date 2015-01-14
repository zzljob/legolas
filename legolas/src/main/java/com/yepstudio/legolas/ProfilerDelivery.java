package com.yepstudio.legolas;

import com.yepstudio.legolas.request.BasicRequest;
import com.yepstudio.legolas.response.Response;

/**
 * 分析调度器
 * 
 * @author zzljob@gmail.com
 * @create 2014年5月15日
 * @version 2.0, 2014年5月15日
 *
 */
public interface ProfilerDelivery {

	public void postRequestStart(BasicRequest wrapper);

	public void postRequestEnd(BasicRequest wrapper, Response response, LegolasException exception);

	public void postRequestCancel(BasicRequest wrapper, Response response);

	public void enableProfiler(boolean enable);

}
