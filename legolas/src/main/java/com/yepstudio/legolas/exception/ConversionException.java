package com.yepstudio.legolas.exception;

import java.lang.reflect.Type;

import com.yepstudio.legolas.response.Response;

public class ConversionException extends ServiceException {

	private static final long serialVersionUID = -5439925901304555188L;

	private final Type result;

	public ConversionException(String uuid, Response response, Type result, Throwable cause) {
		this(uuid, response, result, "", cause);
	}

	public ConversionException(String uuid, Response response, Type result, String message, Throwable cause) {
		super(uuid, response, message, cause);
		this.result = result;
	}

	public Type getResult() {
		return result;
	}

}
