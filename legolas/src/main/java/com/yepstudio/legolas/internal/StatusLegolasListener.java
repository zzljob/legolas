package com.yepstudio.legolas.internal;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import com.yepstudio.legolas.LegolasException;
import com.yepstudio.legolas.exception.CancelException;
import com.yepstudio.legolas.exception.ConversionException;
import com.yepstudio.legolas.exception.HttpStatusException;
import com.yepstudio.legolas.exception.NetworkException;
import com.yepstudio.legolas.exception.ResponseException;
import com.yepstudio.legolas.listener.LegolasListener;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.Response;

public class StatusLegolasListener<R, E> implements LegolasListener<R, E> {

	private CountDownLatch countDown = null;
	private AtomicBoolean success = new AtomicBoolean(false);
	private R successResponse = null;
	private E failResponse = null;
	private LegolasException exception = null;

	@Override
	public void onRequest(Request request) {
		countDown = new CountDownLatch(1);
	}
	
	@Override
	public void onResponse(Request request, R response) {
		countDown.countDown();
		success.set(true);
		successResponse = response;
	}

	@Override
	public void onError(Request request, LegolasException error, E response) {
		countDown.countDown();
		success.set(false);
		failResponse = response;
		exception = error;
	}

	public boolean isRequesting() {
		return countDown != null && countDown.getCount() > 0;
	}

	public boolean isSuccess() {
		return success.get();
	}
	
	public boolean isFailed() {
		return !isRequesting() && !success.get();
	}

	public R getSuccessResponse() {
		return successResponse;
	}

	public E getFailResponse() {
		return failResponse;
	}

	public LegolasException getException() {
		return exception;
	}
	
	public String getErrorMsg() {
		if (isRequesting() || isSuccess()) {
			return null;
		}
		if (exception != null) {
			if (exception instanceof CancelException) {
				return getErrorMsgForCancelException((CancelException) exception);
			} else if (exception instanceof HttpStatusException) {
				getErrorMsgForHttpStatusException((HttpStatusException) exception);
			} else if (exception instanceof NetworkException) {
				return getErrorMsgForNetworkException((NetworkException) exception);
			} else if (exception instanceof ConversionException) {
				return getErrorMsgForConversionException((ConversionException) exception);
			} else if (exception instanceof ResponseException) {
				return getErrorMsgForResponseException((ResponseException) exception);
			}
		}
		return getErrorMsgForUnkown();
	}

	protected String getErrorMsgForUnkown() {
		return "未知错误";
	}

	protected String getErrorMsgForCancelException(CancelException ce) {
		return "请求取消";
	}

	protected String getErrorMsgForResponseException(ResponseException ce) {
		return "请求成功，但返回的数据有错";
	}

	protected String getErrorMsgForConversionException(ConversionException ce) {
		return "请求成功，数据转换出错 => " + ce.getConversionType();
	}

	protected String getErrorMsgForNetworkException(NetworkException ne) {
		return "网络错误";
	}

	protected String getErrorMsgForHttpStatusException(HttpStatusException hse) {
		Response r = hse.getResponse();
		if (r == null) {
			return "Http响应状态不正确";
		} else {
			return String.format("Http响应状态不正确：%s(%s)", r.getMessage(), r.getStatus());
		}
	}

	public CountDownLatch getCountDownLatch() {
		return countDown;
	}
}
