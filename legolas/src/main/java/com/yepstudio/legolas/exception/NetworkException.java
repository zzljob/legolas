package com.yepstudio.legolas.exception;

import com.yepstudio.legolas.LegolasException;

/**
 * 网络发生错误时抛出的异常，大部分是IOException
 * 
 * @author zzljob@gmail.com
 * @create 2014年6月5日
 * @version 2.0, 2014年6月5日
 * 
 */
public class NetworkException extends LegolasException {

	private static final long serialVersionUID = -749730287973068769L;

	public NetworkException(String uuid, String message, Throwable cause) {
		super(uuid, null, message, cause);
	}

	public NetworkException(String uuid, String message) {
		super(uuid, null, message);
	}

	public NetworkException(String uuid, Throwable cause) {
		super(uuid, null, cause);
	}

	public NetworkException(String uuid) {
		super(uuid, null);
	}

}
