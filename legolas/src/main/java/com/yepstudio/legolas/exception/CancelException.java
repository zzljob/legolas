package com.yepstudio.legolas.exception;

import com.yepstudio.legolas.LegolasException;
import com.yepstudio.legolas.response.Response;

public class CancelException extends LegolasException {

	private static final long serialVersionUID = 5444373136509533161L;

	private Response response;

	public CancelException() {
		super();
	}

	public CancelException(String message) {
		super(message);
	}

	public CancelException(Throwable cause) {
		super(cause);
	}

	public CancelException(String message, Throwable cause) {
		super(message, cause);
	}

	public CancelException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}
}
