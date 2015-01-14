package com.yepstudio.legolas.exception;

import com.yepstudio.legolas.LegolasException;
import com.yepstudio.legolas.response.Response;

/**
 * Http请求完好，但是数据有错误，比如约定好的数据不能为空，但是返回的数据为空了，约定了日期的格式，但是格式却错了
 * 
 * @author zzljob@gmail.com
 * @create 2014年6月5日
 * @version 2.0, 2014年6月5日
 * 
 */
public class ResponseException extends LegolasException {

	private static final long serialVersionUID = -6649149773387034472L;
	private Response response;

	public ResponseException() {
		super();
	}

	public ResponseException(String message) {
		super(message);
	}

	public ResponseException(Throwable cause) {
		super(cause);
	}
	
	public ResponseException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ResponseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}
}
