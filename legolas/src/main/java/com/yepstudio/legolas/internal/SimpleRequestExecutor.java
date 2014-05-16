package com.yepstudio.legolas.internal;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.http.HttpStatus;
import org.apache.http.impl.cookie.DateUtils;

import com.yepstudio.legolas.Cache;
import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.LegolasException;
import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.ProfilerDelivery;
import com.yepstudio.legolas.RequestExecutor;
import com.yepstudio.legolas.ResponseDelivery;
import com.yepstudio.legolas.ResponseParser;
import com.yepstudio.legolas.exception.ConversionException;
import com.yepstudio.legolas.exception.NetworkException;
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
public class SimpleRequestExecutor implements RequestExecutor {
	private static LegolasLog log = LegolasLog.getClazz(SimpleRequestExecutor.class);
	
	private final ExecutorService httpSenderExecutor;
	private final HttpSender httpSender;
	private final ResponseParser responseParser;
	private final ResponseDelivery responseDelivery;
	private final ProfilerDelivery profilerDelivery;
	private final Cache cache;
	
	private final CompletionService<Response> service;
	private final Thread watchThread;
	private final Map<Future<Response>, RequestWrapper> futureMap;

	public SimpleRequestExecutor(ExecutorService httpSenderExecutor, HttpSender httpSender, ResponseDelivery responseDelivery, ResponseParser responseParser, ProfilerDelivery profiler, Cache cache) {
		this.httpSenderExecutor = httpSenderExecutor;
		this.httpSender = httpSender;
		this.responseParser = responseParser;
		this.responseDelivery = responseDelivery;
		this.profilerDelivery = profiler;
		this.cache = cache;
		
		service = new ExecutorCompletionService<Response>(this.httpSenderExecutor);
		futureMap = new WeakHashMap<Future<Response>, RequestWrapper>();
		watchThread = new Thread() {

			@Override
			public void run() {
				super.run();
				Future<Response> future;
				while (true) {
					try {
						future = service.take();
					} catch (InterruptedException e) {
						//还没开始就被取消掉了，这个时候Listeners，和分析都还没调用
						future = null;
					}
					if (future == null) {
						continue;
					}
					RequestWrapper wrapper = futureMap.get(future);
					if (wrapper == null) {
						continue;
					}
					boolean isCancel = false;
					LegolasException exception = null;
					Response response = null;
					try {
						response = future.get();
					} catch (InterruptedException e) {
						isCancel = true;
					} catch (ExecutionException e) {
						if (e.getCause() instanceof CancelException) {
							isCancel = true;
						} else if (e.getCause() instanceof LegolasException) {
							exception = (LegolasException) e.getCause();
						} else {
							throw new RuntimeException(e);
						}
					}
					if (isCancel) {
						profilerDelivery.postCancelCall(wrapper);
					} else {
						if (exception != null) {
							executErrorListeners(wrapper, exception);
						}
						profilerDelivery.postAfterCall(wrapper, response);
					}
					futureMap.remove(future);
				}
			}
			
		};
		watchThread.setName("WatchThreadForAsyncRequest");
		watchThread.start();
	}

	@Override
	public void asyncRequest(final RequestWrapper wrapper) {
		log.i("asyncRequest execute ...");
		Future<Response> future = service.submit(new Callable<Response>() {

			@Override
			public Response call() throws Exception {
				executRequestListeners(wrapper);
				profilerDelivery.postBeforeCall(wrapper);

				Request request = wrapper.getRequest();
				Response response = null;
				try {
					if (request.isCancel()) {
						throw new CancelException();
					}
					
					//Read Cache
					Cache.Entry entry = cache.get(wrapper.getRequest().getCacheKey());
					if (entry == null) {
						response = httpSender.execute(request);
						log.d("cache-miss");
					} else{
						//设置缓存
						wrapper.getRequest().setCacheEntry(entry);
						if (entry.etag != null) {//设置Etag
							wrapper.getRequest().getHeaders().put("If-None-Match", entry.etag);
						}
						if (entry.serverDate > 0) {//设置Etag 的时间
							Date refTime = new Date(entry.serverDate);
							wrapper.getRequest().getHeaders().put("If-Modified-Since", DateUtils.formatDate(refTime));
						}
						
						if (entry.isExpired() || entry.refreshNeeded()) {//缓存过期 或者 需要更新
							log.d("cache-hit-expired");
							response = httpSender.execute(request);
						} else {
							log.d("cache-hit-parsed");
							ResponseBody body = new ByteArrayBody(null, entry.data);
							response = new Response(wrapper.getRequest().getUuid(), HttpStatus.SC_OK, "OK", entry.responseHeaders, body);
						}
					}
					
					response = responseParser.doParse(request, response);
					
					if (request.isCancel()) {
						throw new CancelException();
					}
					executResponseListeners(wrapper, response);
					return null;
				} catch (IOException e) {
					throw new NetworkException(wrapper.getRequest().getUuid(), e);
				} catch (LegolasException e) {
					throw e;
				} catch (CancelException e) {
					throw e;
				}
			}

		});
		futureMap.put(future, wrapper);
	}
	
	private class CancelException extends Exception {

		private static final long serialVersionUID = -2595820862553291465L;
		
	}
	
	protected void executResponseListeners(RequestWrapper wrapper, Response response) throws ConversionException {
		if (wrapper.getOnResponseListeners() != null) {
			for (Type type : wrapper.getOnResponseListeners().keySet()) {
				Object result = wrapper.getConverter().fromBody(response.getBody(), type);
				responseDelivery.postResponse(wrapper.getOnResponseListeners().get(type), wrapper.getRequest(), result);
			}
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
			public Response call() throws IOException {
				return httpSender.execute(wrapper.getRequest());
			}
			
		};
		
		FutureTask<Response> task = new FutureTask<Response>(callable);
		profilerDelivery.postBeforeCall(wrapper);
		httpSenderExecutor.execute(task);
		Response response = null;
		try {
			response = responseParser.doParse(wrapper.getRequest(), task.get());
			return wrapper.getConverter().fromBody(response.getBody(), wrapper.getResult());
		} catch (InterruptedException e) {
			log.e("syncRequest interrupted.", e);
		} catch (ExecutionException e) {
			log.e("send syncRequest has a IOException", e);
			Throwable thr = e.getCause();
			if (thr instanceof IOException) {
				throw new NetworkException(wrapper.getRequest().getUuid(), thr);
			} else {
				throw new LegolasException(wrapper.getRequest().getUuid(), thr);
			}
		} catch (ConversionException e) {
			throw new ConversionException(wrapper.getRequest().getUuid(), e);
		} catch (LegolasException e) {
			throw e;
		} finally {
			profilerDelivery.postBeforeCall(wrapper);
		}
		return null;
	}

}
