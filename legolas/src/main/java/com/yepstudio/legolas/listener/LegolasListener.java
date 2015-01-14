package com.yepstudio.legolas.listener;

import com.yepstudio.legolas.LegolasException;
import com.yepstudio.legolas.request.Request;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2014年10月30日
 * @version 1.0，2014年10月30日
 *
 * @param <T>
 */
public interface LegolasListener<R, E> {

	public void onRequest(Request request);

	public void onResponse(Request request, R response);

	public void onError(Request request, LegolasException error, E response);

}
