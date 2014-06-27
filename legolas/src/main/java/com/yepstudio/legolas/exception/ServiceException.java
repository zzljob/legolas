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
public class ServiceException extends LegolasException {

	private static final long serialVersionUID = -6649149773387034472L;

	public ServiceException(String uuid, Response response, String message, Throwable cause) {
		super(uuid, response, message, cause);
	}
}
