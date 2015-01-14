package com.yepstudio.legolas;

import com.yepstudio.legolas.request.AsyncRequest;
import com.yepstudio.legolas.request.SyncRequest;

/**
 * 请求引擎
 * 
 * @author zzljob@gmail.com
 * @create 2013年12月27日
 * @version 3.0，2014年11月4日
 */
public interface LegolasEngine {

	public void asyncRequest(AsyncRequest wrapper);

	public Object syncRequest(SyncRequest wrapper) throws LegolasException;

	public void denyCache(boolean denyCache);

	public void pause();

	public void resume();

	public void stop();

}
