package com.yepstudio.legolas.request;

import java.lang.reflect.Type;
import java.util.Map;

import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.LegolasOptions;
import com.yepstudio.legolas.mime.RequestBody;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2015年1月13日
 * @version 1.0，2015年1月13日
 *
 */
public final class SyncRequest extends BasicRequest {

	// 同步的请求参数
	private final Type resultType;
	private final Type errorType;
	private Object resultValue;
	private Object errorValue;

	public SyncRequest(String url, String method, String description,
			Map<String, String> headers, RequestBody body,
			LegolasOptions options, Converter converter,
			Type resultType, Type errorType) {
		super(url, method, description, headers, body, options, converter);
		this.resultType = resultType;
		this.errorType = errorType;
	}

	public synchronized boolean isSyncResultValueHasValue() {
		return resultValue != null;
	}

	public Object getResultValue() {
		return resultValue;
	}

	public void setResultValue(Object resultValue) {
		this.resultValue = resultValue;
	}

	public Object getErrorValue() {
		return errorValue;
	}

	public void setErrorValue(Object errorValue) {
		this.errorValue = errorValue;
	}

	public Type getResultType() {
		return resultType;
	}

	public Type getErrorType() {
		return errorType;
	}

}
