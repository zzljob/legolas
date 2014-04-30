package com.yepstudio.legolas;

import com.yepstudio.legolas.request.RequestWrapper;

/**
 * 请求执行器
 * 
 * @author zzljob@gmail.com
 * @create 2013年12月27日
 * @version 2.0，2014年4月23日
 */
public interface RequestExecutor {

	/**
	 * 异步执行
	 * 
	 * @param request
	 */
	public void asyncRequest(RequestWrapper wrapper);

	/**
	 * 同步执行
	 * 
	 * @param request
	 * @param type
	 * @return
	 */
	public Object syncRequest(RequestWrapper wrapper) throws LegolasError;

}
