package com.yepstudio.legolas;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年5月28日
 * @version 2.0, 2014年5月28日
 * 
 */
public interface RemoteEndpoint extends Endpoint {

	/**
	 * 已经解析过的URL
	 * 
	 * @return
	 */
	String getRemoteUrl();

	String getHost();

}