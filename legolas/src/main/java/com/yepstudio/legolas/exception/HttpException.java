package com.yepstudio.legolas.exception;

import com.yepstudio.legolas.LegolasException;
import com.yepstudio.legolas.response.Response;

/**
 * Http错误，主要是HTTP status code 不是200,204,304所造成的错误.
 * @author zzljob@gmail.com
 * @create 2014年6月5日
 * @version 2.0, 2014年6月5日
 *
 */
public class HttpException extends LegolasException {

	private static final long serialVersionUID = 4223041631699763099L;

	public HttpException(String uuid, String message, Response response) {
		super(uuid, response, message);
	}
	
}
