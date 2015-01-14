package com.yepstudio.legolas;

import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.Response;

/**
 * 性能分析的接口，运行在非主线程
 * @author zzljob@gmail.com
 * @create 2014年4月24日
 * @version 2.0, 2014年4月24日
 *
 * @param <T>
 */
public interface Profiler<T> {
	
	/**
	 * 请求前调用的
	 * @param request
	 * @return
	 */
	public T beforeCall(Request request);

	/**
	 * 请求完成后调用的
	 * @param response 如果为空则表示请求失败了
	 * @param startTime 请求开始的时间
	 * @param elapsedTime 请求结束的时间
	 * @param beforeCallData 请求前调用返回的对象
	 */
	public void afterCall(Response response, LegolasException exception, T beforeCallData);
	
	/**
	 * 请求被取消时被调用
	 * @param beforeCallData
	 */
	public void cancelCall(T beforeCallData);

}
