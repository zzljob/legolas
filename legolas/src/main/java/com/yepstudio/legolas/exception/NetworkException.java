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

	public NetworkException() {
		super();
	}

	public NetworkException(String message) {
		super(message);
	}

	public NetworkException(Throwable cause) {
		super(cause);
	}
	
	public NetworkException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public NetworkException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
