package com.yepstudio.legolas.exception;

import com.yepstudio.legolas.LegolasException;

public class ConversionException extends LegolasException {

	private static final long serialVersionUID = -5439925901304555188L;

	public ConversionException() {
		super();
	}

	public ConversionException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ConversionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConversionException(String message) {
		super(message);
	}

	public ConversionException(Throwable cause) {
		super(cause);
	}
	
}
