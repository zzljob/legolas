package com.yepstudio.android.legolas.http;

import java.lang.reflect.Type;

import com.yepstudio.android.legolas.error.LegolasError;

/**
 * 请求执行器
 * 
 * @author zzljob@gmail.com
 * @createDate 2013年12月27日
 */
public interface RequestExecutor {

	/**
	 * 异步执行
	 * @param request
	 */
	public void asyncRequest(Request request);

	/**
	 * 同步执行
	 * @param request
	 * @param type
	 * @return
	 */
	public Object syncRequest(Request request, Type type) throws LegolasError;

}
