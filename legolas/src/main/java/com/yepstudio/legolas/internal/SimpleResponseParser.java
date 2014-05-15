package com.yepstudio.legolas.internal;

import java.io.IOException;

import org.apache.http.HttpStatus;

import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.ResponseParser;
import com.yepstudio.legolas.exception.HttpException;
import com.yepstudio.legolas.exception.NetworkException;
import com.yepstudio.legolas.exception.ServiceException;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.Response;

/**
 * Response解析器，将Response解析到用户需要返回的类
 * @author zzljob@gmail.com
 * @create 2014年1月8日
 * @version 2014年4月23日
 */
public class SimpleResponseParser implements ResponseParser {
	private static LegolasLog log = LegolasLog.getClazz(SimpleResponseParser.class);
	
	@Override
	public Response doParse(Request request, Response response) throws NetworkException, HttpException, ServiceException {
		int status = response.getStatus();
		try {
			// 介绍HTTP状态码
			// http://zh.wikipedia.org/wiki/HTTP%E7%8A%B6%E6%80%81%E7%A0%81
			if (status >= 200 && status < 300) { // 2XX == successful request
				if (status == 200) {
					return successfullRequest(response);
				}
			} else if (status >= 300 && status < 400) { // 3xx重定向
				if (status == HttpStatus.SC_NOT_MODIFIED) {
					//读取缓存
					return cachedRequest(request, response);
				} else {
					throw new HttpException("just http status code 200, 304 be Supported");
				}
			} else if (status >= 400 && status < 500) { // 4xx请求错误
				throw new HttpException();
			} else if (status >= 500 && status < 600) { // 5xx服务器错误
				throw new ServiceException();
			}
		} catch (IOException e) {
			throw new NetworkException(e);
		}
		return null;
	}
	
	private Response successfullRequest(Response response) throws IOException {
		log.v("successfullRequest");
		return response;
	}
	
	private Response cachedRequest(Request request, Response response) throws IOException {
		log.v("cachedRequest");
		return response;
	}

}
