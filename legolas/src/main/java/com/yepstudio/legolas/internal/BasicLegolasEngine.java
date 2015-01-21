package com.yepstudio.legolas.internal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.impl.cookie.DateUtils;

import com.yepstudio.legolas.CacheDispatcher;
import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.Legolas;
import com.yepstudio.legolas.LegolasEngine;
import com.yepstudio.legolas.LegolasException;
import com.yepstudio.legolas.LegolasOptions;
import com.yepstudio.legolas.LegolasOptions.CachePolicy;
import com.yepstudio.legolas.LegolasOptions.RecoveryPolicy;
import com.yepstudio.legolas.ProfilerDelivery;
import com.yepstudio.legolas.ResponseDelivery;
import com.yepstudio.legolas.cache.CacheEntry;
import com.yepstudio.legolas.exception.CancelException;
import com.yepstudio.legolas.exception.ConversionException;
import com.yepstudio.legolas.exception.HttpStatusException;
import com.yepstudio.legolas.exception.NetworkException;
import com.yepstudio.legolas.exception.ResponseException;
import com.yepstudio.legolas.listener.LegolasListenerWrapper;
import com.yepstudio.legolas.mime.ByteArrayResponseBody;
import com.yepstudio.legolas.mime.FileResponseBody;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.mime.ResponseBody;
import com.yepstudio.legolas.request.AsyncRequest;
import com.yepstudio.legolas.request.BasicRequest;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.request.SyncRequest;
import com.yepstudio.legolas.response.Response;
import com.yepstudio.legolas.response.ResponseListenerWrapper;

public class BasicLegolasEngine implements LegolasEngine {
	
	private final HttpSender httpSender;
	private final Executor taskExecutor;
	private final CacheDispatcher cacheDispatcher;
	private final ResponseDelivery responseDelivery;
	private final ProfilerDelivery profilerDelivery;
	private final AtomicBoolean pause = new AtomicBoolean(false);

	public BasicLegolasEngine(Executor executor, HttpSender httpSender, CacheDispatcher cacheDispatcher, ResponseDelivery responseDelivery, ProfilerDelivery profilerDelivery) {
		super();
		this.httpSender = httpSender;
		this.taskExecutor = executor;
		this.responseDelivery = responseDelivery;
		this.cacheDispatcher = cacheDispatcher;
		this.profilerDelivery = profilerDelivery;
	}

	@Override
	public void asyncRequest(final AsyncRequest wrapper) {
		Legolas.getLog().d("asyncRequest...");
		// 请求网络
		Callable<Void> requestCallable = new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				Legolas.getLog().d("requestCallable call");
				doAsyncRequest(wrapper);
				return null;
			}
		};
		FutureTask<Void> requestTask = new FutureTask<Void>(requestCallable);
		Legolas.getLog().d("taskExecutor execute requestTask");
		taskExecutor.execute(requestTask);
	}
	
	private Object getConvertObject(Converter converter, Request wrapper, Response response, Type type) throws ConversionException {
		Class<?> clazz = TypesHelper.getRawType(type);
		if (void.class.equals(clazz)) {
			return null;
		} else if (Void.class.equals(clazz)) {
			return null;
		} else if (Response.class.equals(clazz)) {
			return response;
		} else if (Request.class.equals(clazz)) {
			return wrapper;
		} else if (ResponseBody.class.isAssignableFrom(clazz)) {
			return response == null ? null : response.getBody();
		} else if (RequestBody.class.isAssignableFrom(clazz)) {
			return wrapper.getBody();
		} else {
			return converter.convert(response, type);
		}
	}
	
	private void doAsyncConvertGoodResponse(AsyncRequest wrapper, Response response) throws ConversionException {
		Converter converter = wrapper.getConverter();
		List<ResponseListenerWrapper> lrlList = wrapper.getOnResponseListeners();
		if (lrlList != null && !lrlList.isEmpty()) {
			for (ResponseListenerWrapper rlw : lrlList) {
				if (rlw == null || rlw.isShouldIgnore()) {
					continue;
				}
				rlw.setResponseValue(getConvertObject(converter, wrapper, response, rlw.getResponseType()));
			}
		}
		
		List<LegolasListenerWrapper> llList = wrapper.getOnLegolasListeners();
		if (llList != null && !llList.isEmpty()) {
			for (LegolasListenerWrapper llw : llList) {
				if (llw == null || llw.isShouldIgnoreResponse()) {
					continue;
				}
				llw.setResponseValue(getConvertObject(converter, wrapper, response, llw.getResponseType()));
			}
		}
	}

	private void doAsyncConvertBadResponse(AsyncRequest wrapper, Response response) throws ConversionException {
		Converter converter = wrapper.getConverter();
		List<LegolasListenerWrapper> llList = wrapper.getOnLegolasListeners();
		if (llList != null && !llList.isEmpty()) {
			for (LegolasListenerWrapper llw : llList) {
				if (llw == null || llw.isShouldIgnoreResponse()) {
					continue;
				}
				llw.setErrorValue(getConvertObject(converter, wrapper, response, llw.getErrorType()));
			}
		}
	}
	
	private Response processAsyncRequest(AsyncRequest wrapper) throws CancelException, ConversionException, ResponseException, HttpStatusException, NetworkException {
		Legolas.getLog().d("processAsyncRequest");
		CacheEntry<Response> cacheEntry = null;
		LegolasOptions options  = wrapper.getOptions();
		CachePolicy cachePolicy = options.getCachePolicy();
		Response response = null;
		
		checkCancelException(wrapper, response);
		
		//不需要请求，或者缓存了所有的Listener的结果，那就不需要请求了
		if (cacheDispatcher.getAsyncRequestConverterCache(wrapper)) {
			responseDelivery.postAsyncResponse(wrapper);
			return response;
		}
		
		checkCancelException(wrapper, response);
		
		cacheEntry = cacheDispatcher.getRequestCacheEntry(wrapper);
		
		boolean useCache = wrapper.isCacheResponseInMemory() || wrapper.isCacheResponseOnDisk();
		if (cacheEntry != null && cacheEntry.getData() != null && useCache) {
			response = cacheEntry.getData();
			if (CachePolicy.ALWAYS_USE_CACHE == cachePolicy) {
				// 处理response
				processAsyncResponse(wrapper, response);
				cacheDispatcher.updateAsyncRequestConverterCache(wrapper);
				return response;
			} 
			if (CachePolicy.SERVER_CACHE_CONTROL == cachePolicy && !cacheEntry.refreshNeeded()) {
				processAsyncResponse(wrapper, response);
				cacheDispatcher.updateAsyncRequestConverterCache(wrapper);
				return response;
			}
		}
		
		//先休眠一段时间
		if (options.getDelayBeforeRequest() > 0) {
			try {
				TimeUnit.MILLISECONDS.sleep(options.getDelayBeforeRequest());
			} catch (InterruptedException e) {
			}
		}
		
		checkCancelException(wrapper, response);
		
		response = sendHttpRequestOrRecovery(wrapper, options.getRecoveryPolicy(), cacheEntry);
		if (response == null) {
			throw new NetworkException("request failed and Recovery fail");
		}
		checkCancelException(wrapper, response);
		processAsyncResponse(wrapper, response);
		cacheDispatcher.updateRequestCache(wrapper, response);
		cacheDispatcher.updateAsyncRequestConverterCache(wrapper);
		return response;
	}
	
	private void doAsyncRequest(AsyncRequest request) {
		Legolas.getLog().d("doAsyncRequest");
		profilerDelivery.postRequestStart(request);
		responseDelivery.postAsyncRequest(request);
		Response response = null;
		LegolasException exception = null;
		
		if (request.isCancel()) {
			request.finish();
			profilerDelivery.postRequestCancel(request, response);
			return ;
		}
		
		try {
			response = processAsyncRequest(request);
		} catch (HttpStatusException e) {
			response = e.getResponse();
			exception = e;
		} catch (ConversionException e) {
			response = e.getResponse();
			exception = e;
		} catch (ResponseException e) {
			response = e.getResponse();
			exception = e;
		} catch (CancelException e) {
			if (request.isCancel()) {
				request.finish();
				profilerDelivery.postRequestCancel(request, response);
				return ;
			}
		} catch (LegolasException e) {
			exception = e;
		} catch (Throwable e) {
			exception = new LegolasException(e);
		}
		
		if (exception == null) {
			responseDelivery.postAsyncResponse(request);
		} else {
			responseDelivery.postAsyncError(request, exception);
		}
		profilerDelivery.postRequestEnd(request, response, exception);
		request.finish();
		return ;
	}
	
	private void processAsyncResponse(AsyncRequest wrapper, Response response) throws HttpStatusException, ConversionException {
		Legolas.getLog().d("processAsyncResponse");
		if (200 <= response.getStatus() && response.getStatus() < 300) {
			// 2xx请求认为是对的
			doAsyncConvertGoodResponse(wrapper, response);
		} else if (300 <= response.getStatus() && response.getStatus() < 400) {
			// 3xx请求是缓存跟跳转相关的，在上面会处理过，所以认为是对的
			doAsyncConvertGoodResponse(wrapper, response);
		} else if (400 <= response.getStatus() && response.getStatus() < 500) {
			// 4xx请求是服务端错误
			doAsyncConvertBadResponse(wrapper, response);
			throw new HttpStatusException(response);
		} else if (500 <= response.getStatus() && response.getStatus() < 600) {
			// 5xx请求是服务器内部错误
			doAsyncConvertBadResponse(wrapper, response);
			throw new HttpStatusException(response);
		} else {
			throw new HttpStatusException(response, "unknow http status");
		}
	}
	
	@Override
	public Object syncRequest(final SyncRequest wrapper) throws LegolasException {
		Legolas.getLog().d("syncRequest");
		// 请求网络
		Callable<Response> requestCallable = new Callable<Response>() {

			@Override
			public Response call() throws Exception {
				return doSyncRequest(wrapper);
			}
		};
		profilerDelivery.postRequestStart(wrapper);
		FutureTask<Response> requestTask = new FutureTask<Response>(requestCallable);
		taskExecutor.execute(requestTask);
		
		LegolasException exception = null;
		Response response = null;
		try {
			response = requestTask.get();
			profilerDelivery.postRequestEnd(wrapper, response, null);
			wrapper.finish();
			return wrapper.getResultValue();
		} catch (InterruptedException e) {
			// 网络线程被强制中断
			exception = new LegolasException("Request be Interrupted", e); 
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause == null || !(cause instanceof LegolasException)) {
				exception = new LegolasException("unknow Exception", e);
			} else {
				exception = (LegolasException) cause;
				if (cause instanceof HttpStatusException) {
					HttpStatusException tse = (HttpStatusException) cause; 
					response = tse.getResponse();
				} else if (cause instanceof ResponseException) {
					ResponseException re = (ResponseException) cause; 
					response = re.getResponse();
				} else if (cause instanceof NetworkException) {
					//网络错误就不处理
				}
			}
		} catch (Throwable e) {
			exception = new LegolasException("unknow Exception", e);
		} finally {
			System.out.println(exception);
		}
		profilerDelivery.postRequestEnd(wrapper, response, exception);
		wrapper.finish();
		throw exception;
	}
	
	public void addCacheHeaders(Request request, CacheEntry<Response> cacheEntry) {
		Map<String, String> headers = request.getHeaders();
		if (cacheEntry == null) {
			headers.put("Pragma", "no-cache");
			return ;
		}
		//If-None-Match
		if (cacheEntry.getEtag() != null) {
			headers.put("If-None-Match", cacheEntry.getEtag());
		}
		// 添加 If-Modified-Since
		if (cacheEntry.getServerModified() > 0) {
			Date refTime = new Date(cacheEntry.getServerModified());
			request.getHeaders().put("If-Modified-Since", DateUtils.formatDate(refTime));
		}
	}
	
	private Response getReadableResponse(Response response) throws IOException {
		if (response == null || response.getBody() == null) {
			return response;
		}
		ResponseBody body = response.getBody();
		if (body instanceof ByteArrayResponseBody) {
			return response;
		} else if (body instanceof FileResponseBody) {
			return response;
		} else {
			long max_length = ByteArrayResponseBody.MAX_LIMIT_SIZE;
			
			int statusCode = response.getStatus();
			String message = response.getMessage();
			Map<String, String> headers = response.getHeaders();
			ResponseBody responseBody = null;
			boolean fromMemoryCache = response.isFromMemoryCache();
			boolean fromDiskCache = response.isFromDiskCache();
			
			if (0 <= body.length() && body.length() < max_length) {
				responseBody = ByteArrayResponseBody.build(body);
			} else {
				File file = File.createTempFile("legolas_", "_temp");
				responseBody = FileResponseBody.build(body, file);
			}
			return new Response(statusCode, message, headers, responseBody, fromMemoryCache, fromDiskCache);
		}
	}
	
	private void checkCancelException(Request request, Response response) throws CancelException {
		if (request.isCancel()) {
			CancelException exception = new CancelException("Request be Cancel, response : " + response);
			exception.setResponse(response);
			throw exception;
		}
	}
	
	private Response sendHttpRequestOrRecovery(BasicRequest request, RecoveryPolicy recovery, CacheEntry<Response> cacheEntry) throws CancelException, NetworkException {
		StringBuilder log = new StringBuilder();
		
		//发送请求
		addCacheHeaders(request, cacheEntry);
		appendLogFOrRequest(log, request);
		
		Response response = null;
		
		Response cacheResponse = null;
		if (cacheEntry != null) {
			cacheResponse = cacheEntry.getData();
		}
		
		request.startRequest();
		try {
			checkCancelException(request, cacheResponse);
			
			Legolas.getLog().d("request:" + request.getUrl());
			response = httpSender.execute(request);
			appendLogForResponse(log, response, null);
			
			checkCancelException(request, cacheResponse);
			
			if (cacheResponse != null && cacheResponse.getBody() != null && response != null) {
				//处理304缓存请求请求
				if (304 == response.getStatus()) {
					//处理缓存
					//设置缓存的流
					ResponseBody body = cacheResponse.getBody();
					response = new Response(response.getStatus(), response.getMessage(), response.getHeaders(), body, false, false);
				} else if (400 <= response.getStatus()) {
					//服务内部错误5xx，服务不可用4xx都将被恢复
					if (RecoveryPolicy.HTTPSTATUS_ERROR == recovery || RecoveryPolicy.RESPONSE_NOT_EXPECTED == recovery) {
						Legolas.getLog().v(String.format("status[%s] recovery[%s], recovery to [%s]", response.getStatus(), recovery, cacheResponse));
						response = cacheResponse;
					}
				}
			}
			
			//开始转成字符或者文件的，使后面可以反复被读取
			response = getReadableResponse(response);
			if (response != null) {
				int status = response.getStatus();
				Legolas.getLog().v(String.format("status[%s] Body[%s], ", status, response.getBody()));
				Legolas.getLog().w("sendHttpRequest [success]");
			}
		} catch (IOException e) {
			Legolas.getLog().w("sendHttpRequest has IOException", e);
			//请求出错啦
			appendLogForResponse(log, null, e);
			
			NetworkException exception = new NetworkException("request failed", e);
			//执行恢复策略
			if (RecoveryPolicy.RESPONSE_NONE == recovery || RecoveryPolicy.RESPONSE_NOT_EXPECTED == recovery) {
				Legolas.getLog().w("RecoveryPolicy Recovery to [" + cacheResponse + "]");
				response = cacheResponse;
			} else if (RecoveryPolicy.NONE == recovery) {
				throw exception;
			}
		} finally {
			request.appendLog(log.toString());
			request.finishRequest();
		}
		return response;
	}
	
	private Response doSyncRequest(SyncRequest wrapper) throws NetworkException, ConversionException, HttpStatusException, LegolasException {
		Legolas.getLog().d("doSyncRequest");
		//不需要请求，直接返回解析的内容
		Class<?> clazz = TypesHelper.getRawType(wrapper.getResultType());
		if (Request.class.isAssignableFrom(clazz)) {
			wrapper.setResultValue(wrapper);
			return null;
		} else if (RequestBody.class.isAssignableFrom(clazz)) {
			wrapper.setResultValue(wrapper.getBody());
			return null;
		}
		
		LegolasOptions options = wrapper.getOptions();
		CachePolicy cachePolicy = options.getCachePolicy();
		CacheEntry<Response> cacheEntry = null; 
		
		//如果没有全面禁用缓存那就直接从缓存里边拿内容
		if (cacheDispatcher.getSyncRequestConverterCache(wrapper)) {
			Legolas.getLog().d("getSyncRequestConverterCache [true], finish Request");
			return null;
		}
		cacheEntry = cacheDispatcher.getRequestCacheEntry(wrapper);
		Legolas.getLog().d("getRequestCacheEntry [" + cacheEntry + "]");
		
		boolean useCache = wrapper.isCacheResponseInMemory() || wrapper.isCacheResponseOnDisk();
		if (cacheEntry != null && cacheEntry.getData() != null && useCache) {
			if (CachePolicy.ALWAYS_USE_CACHE == cachePolicy) {
				// 处理response
				wrapper.setResultValue(processSyncResponse(wrapper, cacheEntry.getData()));
				return cacheEntry.getData();
			} 
			if (CachePolicy.SERVER_CACHE_CONTROL == cachePolicy && !cacheEntry.refreshNeeded()) {
				wrapper.setResultValue(processSyncResponse(wrapper, cacheEntry.getData()));
				return cacheEntry.getData();
			}
		}
		
		//先休眠一段时间
		if (options.getDelayBeforeRequest() > 0) {
			try {
				TimeUnit.MILLISECONDS.sleep(options.getDelayBeforeRequest());
			} catch (InterruptedException e) {
			}
		}
		
		Response response = sendHttpRequestOrRecovery(wrapper, options.getRecoveryPolicy(), cacheEntry);
		if (response == null) {
			throw new NetworkException("request failed and Recovery fail");
		}
		wrapper.setResultValue(processSyncResponse(wrapper, response));
		return response;
	}
	
	private Object processSyncResponse(SyncRequest wrapper, Response response) throws ConversionException, HttpStatusException {
		Class<?> clazz = TypesHelper.getRawType(wrapper.getResultType());
		try {
			//不需要转换的
			if (Response.class.isAssignableFrom(clazz)) {
				return response;
			} else if (ResponseBody.class.isAssignableFrom(clazz)) {
				return response.getBody();
			}
			
			Converter converter =  wrapper.getConverter();
			if (200 <= response.getStatus() && response.getStatus() < 300) {
				// 2xx请求认为是对的
				Object syncResultValue = converter.convert(response, wrapper.getResultType());
				wrapper.setResultValue(syncResultValue);
				cacheDispatcher.updateSyncRequestConverterCache(wrapper);
				return syncResultValue;
			} else if (300 <= response.getStatus() && response.getStatus() < 400) {
				// 3xx请求是缓存跟跳转相关的，在上面会处理过，所以认为是对的
				Object syncResultValue = converter.convert(response, wrapper.getResultType());
				wrapper.setResultValue(syncResultValue);
				cacheDispatcher.updateSyncRequestConverterCache(wrapper);
				return syncResultValue;
			} else if (400 <= response.getStatus() && response.getStatus() < 500) {
				// 4xx请求是服务端错误
				throw makeHttpStatusException(wrapper, response, wrapper.getErrorType());
			} else if (500 <= response.getStatus() && response.getStatus() < 600) {
				// 5xx请求是服务器内部错误
				throw makeHttpStatusException(wrapper, response, wrapper.getErrorType());
			} else {
				throw makeHttpStatusException(wrapper, response, wrapper.getErrorType());
			}
		} catch (ConversionException e) {
			throw e;
		} finally {
			cacheDispatcher.updateRequestCache(wrapper, response);
		}
		
	}
	
	private HttpStatusException makeHttpStatusException(SyncRequest wrapper, Response response, Type errorType) {
		Object errorValue = null;
		if (errorType != null) {
			try {
				Converter converter = wrapper.getConverter();
				errorValue = converter.convert(response, errorType);
				wrapper.setErrorValue(errorValue);
			} catch (ConversionException e) {
				return new HttpStatusException(response, e);
			}
		}
		int status = response == null ? -1 : response.getStatus();
		HttpStatusException e = new HttpStatusException(response, "bad request  status : " + status);
		e.setErrorValue(wrapper.getErrorValue());
		return e;
	} 
	
	private void appendLogForResponse(StringBuilder log, Response response, IOException e) {
		log.append("-------------------<<Response-------------------\n");
		if (response != null) {
			log.append("Status:").append(response.getStatus()).append("  ").append(response.getMessage()).append("\n");
			
			Map<String, String> headers = response.getHeaders();
			if (headers != null && !headers.isEmpty()) {
				log.append("Headers: \n");
				for (String name : headers.keySet()) {
					log.append(name).append("=").append(headers.get(name)).append("\n");
				}
			} else {
				log.append("Headers: none\n");
			}
		}
		if (e != null) {
			log.append("IOException:").append(e.getMessage()).append("\n");
		}
	}
	
	private void appendLogFOrRequest(StringBuilder log, Request request) {
		log.append("-------------------Request>>-------------------\n");
		log.append("URL:").append(request.getUrl()).append("\n");
		Map<String, String> headers = request.getHeaders();
		if (headers != null && !headers.isEmpty()) {
			log.append("Headers: \n");
			for (String name : headers.keySet()) {
				log.append(name).append("=").append(headers.get(name));
				log.append("\n");
			}
		} else {
			log.append("Headers: none\n");
		}
	}

	@Override
	public void pause() {
		pause.set(true);
	}

	@Override
	public void resume() {
		pause.set(false);
	}

	@Override
	public void destroy() {
		
	}

}
