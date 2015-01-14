package com.yepstudio.legolas.exception;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年10月27日
 * @version 1.0, 2014年10月27日
 *
 */
public class LegolasConfigureError extends Error {
	
	private static final long serialVersionUID = -6956763473546770944L;

	public LegolasConfigureError() {
		super();
	}

	public LegolasConfigureError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public LegolasConfigureError(String message, Throwable cause) {
		super(message, cause);
	}

	public LegolasConfigureError(String message) {
		super(message);
	}

	public LegolasConfigureError(Throwable cause) {
		super(cause);
	}


}
