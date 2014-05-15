package com.yepstudio.legolas;

import com.yepstudio.legolas.exception.HttpException;
import com.yepstudio.legolas.exception.NetworkException;
import com.yepstudio.legolas.exception.ServiceException;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.Response;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年5月15日
 * @version 2.0, 2014年5月15日
 *
 */
public interface ResponseParser {
	
	public Response doParse(Request request, Response response) throws NetworkException, HttpException, ServiceException;
	
}
