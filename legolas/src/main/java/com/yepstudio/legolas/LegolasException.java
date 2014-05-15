package com.yepstudio.legolas;

public class LegolasException extends Exception {

	private static final long serialVersionUID = 5851274589793445523L;

	public LegolasException() {
		super();
	}

	public LegolasException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public LegolasException(String message, Throwable cause) {
		super(message, cause);
	}

	public LegolasException(String message) {
		super(message);
	}

	public LegolasException(Throwable cause) {
		super(cause);
	}

}
