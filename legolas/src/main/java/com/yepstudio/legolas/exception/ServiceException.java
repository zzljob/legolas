package com.yepstudio.legolas.exception;

import com.yepstudio.legolas.LegolasException;

public class ServiceException extends LegolasException {

	private static final long serialVersionUID = -6649149773387034472L;

	public ServiceException(String uuid, String message, Throwable cause) {
		super(uuid, message, cause);
	}

	public ServiceException(String uuid, String message) {
		super(uuid, message);
	}

	public ServiceException(String uuid, Throwable cause) {
		super(uuid, cause);
	}

	public ServiceException(String uuid) {
		super(uuid);
	}


}
