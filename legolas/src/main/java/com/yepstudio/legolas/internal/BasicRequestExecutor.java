package com.yepstudio.legolas.internal;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import org.apache.http.HttpStatus;
import org.apache.http.impl.cookie.DateUtils;

import com.yepstudio.legolas.Cache;
import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.LegolasException;
import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.Platform;
import com.yepstudio.legolas.ProfilerDelivery;
import com.yepstudio.legolas.RequestExecutor;
import com.yepstudio.legolas.ResponseDelivery;
import com.yepstudio.legolas.ResponseParser;
import com.yepstudio.legolas.exception.ConversionException;
import com.yepstudio.legolas.exception.HttpException;
import com.yepstudio.legolas.exception.NetworkException;
import com.yepstudio.legolas.exception.ServiceException;
import com.yepstudio.legolas.mime.ByteArrayBody;
import com.yepstudio.legolas.mime.ResponseBody;
import com.yepstudio.legolas.request.OnRequestListener;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.request.RequestWrapper;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.Response;

/**
 * 简单的请求执行器
 * @author zzljob@gmail.com
 * @create 2014年1月8日
 * @version 2.0，2014年4月30日
 */
public class BasicRequestExecutor implements RequestExecutor {
	private static LegolasLog log = LegolasLog.getClazz(BasicRequestExecutor.class);
	
	private final ExecutorService httpSenderExecutor;
	private final HttpSender httpSender;
	private final ResponseParser responseParser;
	private final ResponseDelivery responseDelivery;
	private final ProfilerDelivery profilerDelivery;
	private final Cache cache;
	
	public BasicRequestExecutor(ExecutorService httpSenderExecutor, HttpSender httpSender, ResponseDelivery responseDelivery, ResponseParser responseParser, ProfilerDelivery profiler, Cache cache) {
		this.httpSenderExecutor = httpSenderExecutor;
		this.httpSender = httpSender;
		this.responseParser = responseParser;
		this.responseDelivery = responseDelivery;
		this.profilerDelivery = profiler;
		this.cache = cache;
	}
	
	protected void executeHttp(final RequestWrapper wrapper) {
		httpSenderExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
				Request request = wrapper.getRequest();
				Response response = null;
				LegolasException throwable = null;
				try {
					if (request.isCancel()) {
						throw new CancelException(request.getUuid());
					}
					response = getCacheOrNetworkResponse(request);
					if (request.isCancel()) {
						throw new CancelException(request.getUuid());
					}
					response = responseParser.doParse(request, response);
					executResponseListeners(wrapper, response);
				} catch (IOException e) {
					throwable = new NetworkException(request.getUuid(), e);
				} catch (NetworkException e) {
					throwable = e;
				} catch (HttpException e) {
					throwable = e;
				} catch (ServiceException e) {
					throwable = e;
				} catch (CancelException e) {
					throwable = e;
				} catch (ConversionException e) {
					throwable = e;
				} catch (Throwable e) {
					throwable = new LegolasException(request.getUuid(), e);
				}
				
				if (throwable instanceof CancelException || wrapper.getRequest().isCancel()) {//有异常，并且是取消了
					profilerDelivery.postCancelCall(wrapper);
				} else {
					if (throwable != null) { //有异常
						executErrorListeners(wrapper, throwable);
					}
					profilerDelivery.postAfterCall(wrapper, response, throwable);
				}
			}
		});
	}

	@Override
	public void asyncRequest(final RequestWrapper wrapper) {
		log.i("asyncRequest execute ...");
		executRequestListeners(wrapper);
		profilerDelivery.postBeforeCall(wrapper);
		executeHttp(wrapper);
	}
	
	protected Response getCacheOrNetworkResponse(Request request) throws IOException {
		if (!Platform.get().hasNetwork()) {
			return null;
		}
		Response response = null;
		//Read Cache
		Cache.Entry entry = cache.get(request.getCacheKey());
		if (entry == null) {
			response = httpSender.execute(request);
			log.d("cache-miss");
		} else {
			// 设置缓存
			request.setCacheEntry(entry);
			if (entry.etag != null) {// 设置Etag
				request.getHeaders().put("If-None-Match", entry.etag);
			}
			if (entry.serverDate > 0) {// 设置Etag 的时间
				Date refTime = new Date(entry.serverDate);
				request.getHeaders().put("If-Modified-Since", DateUtils.formatDate(refTime));
			}

			if (entry.isExpired() || entry.refreshNeeded()) {// 缓存过期 或者 需要更新
				log.d("cache-hit-expired");
				response = httpSender.execute(request);
			} else {
				log.d("cache-hit-parsed");
				ResponseBody body = new ByteArrayBody(null, entry.data);
				response = new Response(request.getUuid(), HttpStatus.SC_OK, "OK", entry.responseHeaders, body);
			}
		}
		return response;
	}
	
	private class CancelException extends LegolasException {
		
		private static final long serialVersionUID = -2595820862553291465L;

		public CancelException(String uuid, String message, Throwable cause) {
			super(uuid, message, cause);
		}

		public CancelException(String uuid, String message) {
			super(uuid, message);
		}

		public CancelException(String uuid, Throwable cause) {
			super(uuid, cause);
		}

		public CancelException(String uuid) {
			super(uuid);
		}

	}
	
	protected void executResponseListeners(RequestWrapper wrapper, Response response) throws ConversionException {
		try{
			if (wrapper.getOnResponseListeners() != null) {
				for (Type type : wrapper.getOnResponseListeners().keySet()) {
					Object result = wrapper.getConverter().fromBody(response.getBody(), type);
					responseDelivery.postResponse(wrapper.getOnResponseListeners().get(type), wrapper.getRequest(), result);
				}
			}
		} catch (ConversionException e) {
			//因为Converter是获取不到Request的UUID的，所以在这个地方要故意转一下
			throw new ConversionException(wrapper.getRequest().getUuid(), e.getMessage(), e.getCause());
		}
	}
	
	protected void executRequestListeners(RequestWrapper wrapper) {
		if (wrapper.getOnRequestListeners() != null) {
			for (OnRequestListener listener : wrapper.getOnRequestListeners()) {
				responseDelivery.submitRequest(listener, wrapper.getRequest());
			}
		}
	}
	
	protected void executErrorListeners(RequestWrapper wrapper, LegolasException error) {
		if (wrapper.getOnErrorListeners() != null) {
			for (OnErrorListener listener : wrapper.getOnErrorListeners()) {
				responseDelivery.postError(listener, wrapper.getRequest(), error);
			}
		}
	}

	@Override
	public Object syncRequest(final RequestWrapper wrapper) throws LegolasException {
		log.i("syncRequest execute ...");
		Callable<Response> callable = new Callable<Response>() {

			@Override
			public Response call() throws IOException, NetworkException, HttpException, ServiceException {
				return responseParser.doParse(wrapper.getRequest(), getCacheOrNetworkResponse(wrapper.getRequest()));
			}
			
		};
		
		FutureTask<Response> task = new FutureTask<Response>(callable);
		profilerDelivery.postBeforeCall(wrapper);
		httpSenderExecutor.execute(task);
		Response response = null;
		try {
			response = task.get();
			return wrapper.getConverter().fromBody(response.getBody(), wrapper.getResult());
		} catch (InterruptedException e) {
			log.e("syncRequest interrupted.", e);
		} catch (ExecutionException e) {
			log.e("send syncRequest has a IOException", e);
			Throwable thr = e.getCause();
			if (thr instanceof IOException) {
				throw new NetworkException(wrapper.getRequest().getUuid(), thr);
			} else if (thr instanceof LegolasException) {
				throw (LegolasException) thr;
			} else {
				throw new LegolasException(wrapper.getRequest().getUuid(), thr);
			}
		} catch (ConversionException e) {
			throw new ConversionException(wrapper.getRequest().getUuid(), e);
		} finally {
			profilerDelivery.postBeforeCall(wrapper);
		}
		return null;
	}
	
}
