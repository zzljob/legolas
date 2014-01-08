package com.yepstudio.android.legolas.handler;

import com.yepstudio.android.legolas.http.Request;


/**
 * 认证接口
 * @author zzljob@gmail.com
 * @createDate 2013年12月27日
 */
public interface AuthHandler {

	/**
	 * 生成认证参数
	 * @param context
	 */
	public void doAuth(Request context);

}
