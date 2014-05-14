package com.yepstudio.legolas.internal;

import java.io.IOException;
import java.lang.reflect.Type;

import com.yepstudio.legolas.ConversionException;
import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.LegolasError;
import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.ResponseParser;
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
	public Object doParse(Converter converter, Request request, Response response, Type type) throws LegolasError {
		log.v("doParse => " + type);
		if (type == null) {
			return null;
		}
		int status = response.getStatus();
		try {
			// 介绍HTTP状态码
			// http://zh.wikipedia.org/wiki/HTTP%E7%8A%B6%E6%80%81%E7%A0%81
			if (status >= 200 && status < 300) { // 2XX == successful request
				if (status == 200) {
					return successfullRequest(response, type, converter);
				}
			} else if (status >= 300 && status < 400) { // 3xx重定向
				if (status == 304) {
					//读取缓存
					return cachedRequest(request, response, type);
				} else {
					throw LegolasError.httpError(request.getUrl(), response, null, type);
				}
			} else if (status >= 400 && status < 500) { // 4xx请求错误
				throw LegolasError.httpError(request.getUrl(), response, null, type);
			} else if (status >= 500 && status < 600) { // 5xx服务器错误
				throw LegolasError.httpError(request.getUrl(), response, null, type);
			}
		} catch (IOException e) {
			throw LegolasError.networkError(request.getUrl(), e);
		} catch (ConversionException e) {
			throw LegolasError.conversionError(request.getUrl(), response, converter, type, e);
		} catch (Throwable e) {
			throw LegolasError.unexpectedError(request.getUrl(), e);
		}
		return null;
	}
	
	private Object successfullRequest(Response response, Type type, Converter converter) throws IOException, ConversionException {
		log.v("successfullRequest");
		if (type.equals(Response.class)) {
			return Response.readBodyToBytesIfNecessary(response);
		}
		return converter.fromBody(response.getBody(), type);
	}
	
	private Object cachedRequest(Request request, Response response, Type type) {
		return null;
	}

}
