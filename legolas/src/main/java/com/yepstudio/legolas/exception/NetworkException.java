package com.yepstudio.legolas.exception;

import com.yepstudio.legolas.LegolasException;

public class NetworkException extends LegolasException {

	private static final long serialVersionUID = -749730287973068769L;

	public NetworkException(String uuid, String message, Throwable cause) {
		super(uuid, message, cause);
	}

	public NetworkException(String uuid, String message) {
		super(uuid, message);
	}

	public NetworkException(String uuid, Throwable cause) {
		super(uuid, cause);
	}

	public NetworkException(String uuid) {
		super(uuid);
	}

}
