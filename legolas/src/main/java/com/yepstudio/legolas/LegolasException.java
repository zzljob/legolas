package com.yepstudio.legolas;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.yepstudio.legolas.exception.HttpException;
import com.yepstudio.legolas.exception.NetworkException;
import com.yepstudio.legolas.exception.ServiceException;
import com.yepstudio.legolas.response.Response;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年6月5日
 * @version 2.0, 2014年6月5日
 * 
 */
public class LegolasException extends Exception {

	private static final long serialVersionUID = 5851274589793445523L;
	private final String uuid;
	private final Response response;

	public LegolasException(String uuid, Response response) {
		super();
		this.uuid = uuid;
		this.response = response;
	}

	public LegolasException(String uuid, Response response, String message, Throwable cause) {
		super(message, cause);
		this.uuid = uuid;
		this.response = response;
	}

	public LegolasException(String uuid, Response response, String message) {
		super(message);
		this.uuid = uuid;
		this.response = response;
	}

	public LegolasException(String uuid, Response response, Throwable cause) {
		super(cause);
		this.uuid = uuid;
		this.response = response;
	}

	public String getUuid() {
		return uuid;
	}
	
	public Response getResponse() {
		return response;
	}
	
	public String getResponseText() {
		if (response == null) {
			return null;
		}
		try {
			String charset = response.parseCharset("UTF-8");
			return new String(Response.streamToBytes(response.getBody().read()), charset);
		} catch (UnsupportedEncodingException e) {
			
		} catch (IOException e) {
			
		}
		return "";
	}

	public boolean isNetworkException() {
		return this instanceof NetworkException;
	}

	public boolean isHttpException() {
		return this instanceof HttpException;
	}

	public boolean isServiceException() {
		return this instanceof ServiceException;
	}

}
