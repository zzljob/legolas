package com.yepstudio.legolas;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年6月5日
 * @version 2.0, 2014年6月5日
 * 
 */
public class LegolasException extends Exception {

	private static final long serialVersionUID = 5851274589793445523L;

	public LegolasException() {
		super();
	}

	public LegolasException(String message) {
		super(message);
	}

	public LegolasException(Throwable cause) {
		super(cause);
	}
	
	public LegolasException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public LegolasException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
