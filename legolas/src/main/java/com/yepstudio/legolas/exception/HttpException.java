package com.yepstudio.legolas.exception;

import com.yepstudio.legolas.LegolasException;

public class HttpException extends LegolasException {

	private static final long serialVersionUID = 4223041631699763099L;

	public HttpException(String uuid, String message, Throwable cause) {
		super(uuid, message, cause);
	}

	public HttpException(String uuid, String message) {
		super(uuid, message);
	}

	public HttpException(String uuid, Throwable cause) {
		super(uuid, cause);
	}

	public HttpException(String uuid) {
		super(uuid);
	}

}
