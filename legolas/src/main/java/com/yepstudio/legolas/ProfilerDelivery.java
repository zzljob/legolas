package com.yepstudio.legolas;

import com.yepstudio.legolas.request.RequestWrapper;
import com.yepstudio.legolas.response.Response;

/**
 * 分析调度器
 * @author zzljob@gmail.com
 * @create 2014年5月15日
 * @version 2.0, 2014年5月15日
 *
 */
public interface ProfilerDelivery {

	public void postBeforeCall(RequestWrapper wrapper);

	public void postAfterCall(RequestWrapper wrapper, Response response, LegolasException exception);

	public void postCancelCall(RequestWrapper wrapper);

}
