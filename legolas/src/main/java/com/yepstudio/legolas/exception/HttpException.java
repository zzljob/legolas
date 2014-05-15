package com.yepstudio.legolas.exception;

import com.yepstudio.legolas.LegolasException;

public class HttpException extends LegolasException {

	private static final long serialVersionUID = 4223041631699763099L;

	public HttpException() {
		super();
	}

	public HttpException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public HttpException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpException(String message) {
		super(message);
	}

	public HttpException(Throwable cause) {
		super(cause);
	}

}
