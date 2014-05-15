package com.yepstudio.legolas.exception;

import com.yepstudio.legolas.LegolasException;

public class NetworkException extends LegolasException {

	private static final long serialVersionUID = -749730287973068769L;

	public NetworkException() {
		super();
	}

	public NetworkException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NetworkException(String message, Throwable cause) {
		super(message, cause);
	}

	public NetworkException(String message) {
		super(message);
	}

	public NetworkException(Throwable cause) {
		super(cause);
	}

}
