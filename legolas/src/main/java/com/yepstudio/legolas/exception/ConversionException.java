package com.yepstudio.legolas.exception;

import com.yepstudio.legolas.LegolasException;

public class ConversionException extends LegolasException {

	private static final long serialVersionUID = -5439925901304555188L;

	public ConversionException(String uuid, String message, Throwable cause) {
		super(uuid, message, cause);
	}

	public ConversionException(String uuid, String message) {
		super(uuid, message);
	}

	public ConversionException(String uuid, Throwable cause) {
		super(uuid, cause);
	}

	public ConversionException(String uuid) {
		super(uuid);
	}


}
