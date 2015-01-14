package com.yepstudio.legolas.exception;

import com.yepstudio.legolas.LegolasException;
import com.yepstudio.legolas.response.Response;

/**
 * Http错误，主要是HTTP status code 不是200,204,304所造成的错误.
 * 
 * @author zzljob@gmail.com
 * @create 2014年6月5日
 * @version 2.0, 2014年6月5日
 *
 */
public class HttpStatusException extends LegolasException {

	private static final long serialVersionUID = 4223041631699763099L;

	private final Response response;
	private Object errorValue;
	
	public HttpStatusException(Response response) {
		super();
		this.response = response;
	}
	
	public HttpStatusException(Response response, Throwable cause) {
		super(cause);
		this.response = response;
	}

	public HttpStatusException(Response response,String message) {
		super(message);
		this.response = response;
	}

	public HttpStatusException(Response response, String message, Throwable cause) {
		super(message, cause);
		this.response = response;
	}

	public Response getResponse() {
		return response;
	}

	public Object getErrorValue() {
		return errorValue;
	}

	public void setErrorValue(Object errorValue) {
		this.errorValue = errorValue;
	}

}
