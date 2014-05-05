package com.yepstudio.legolas.internal;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.LegolasError;
import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.Profiler;
import com.yepstudio.legolas.RequestExecutor;
import com.yepstudio.legolas.ResponseDelivery;
import com.yepstudio.legolas.ResponseParser;
import com.yepstudio.legolas.request.OnRequestListener;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.request.RequestWrapper;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.Response;

/**
 * 简单的网络执行器
 * @author zzljob@gmail.com
 * @create 2014年1月8日
 * @version 2.0，2014年4月30日
 */
public class SimpleRequestExecutor implements RequestExecutor {
	private static LegolasLog log = LegolasLog.getClazz(SimpleRequestExecutor.class);
	
	private final Executor executor;
	private final HttpSender httpSender;
	private final ResponseParser parser;
	private final ResponseDelivery delivery;
	private final Profiler profiler;

	public SimpleRequestExecutor(Executor executor, HttpSender httpSender, ResponseDelivery delivery, ResponseParser parser, Profiler<?> profiler) {
		this.executor = executor;
		this.httpSender = httpSender;
		this.parser = parser;
		this.delivery = delivery;
		this.profiler = profiler;
	}

	@Override
	public void asyncRequest(final RequestWrapper wrapper) {
		log.v("asyncRequest execute ...");
		executor.execute(new Runnable() {

			@Override
			public void run() {
				LegolasError error = null;
				Request request = wrapper.getRequest();
				Object beforeCallData = profiler.beforeCall(request);
				long startTime = System.currentTimeMillis();
				Response response = null;
				try {
					for (OnRequestListener listener : wrapper.getOnRequestListeners()) {
						delivery.submitRequest(listener, request);
					}
					response = httpSender.execute(request);
					if (wrapper.getOnResponseListeners() != null) {
						for (Type type : wrapper.getOnResponseListeners().keySet()) {
							Object result = parser.doParse(wrapper.getConverter(), request, response, type);
							delivery.postResponse(wrapper.getOnResponseListeners().get(type), request, result);
						}
					}
				} catch (IOException e) {
					error = LegolasError.networkError(request.getUrl(), e);
				} catch (LegolasError e) {
					error = e;
				} catch (Throwable e) {
					error = LegolasError.unexpectedError(request.getUrl(), e);
				}
				if (error != null) {
					for (OnErrorListener listener : wrapper.getOnErrorListeners()) {
						delivery.postError(listener, request, error);
					}
				}
				profiler.afterCall(response, startTime, System.currentTimeMillis(), beforeCallData);
			}
		});

	}

	@Override
	public Object syncRequest(final RequestWrapper wrapper) throws LegolasError {
		log.v("syncRequest execute ...");
		Callable<Response> callable = new Callable<Response>() {

			@Override
			public Response call() throws IOException {
				return httpSender.execute(wrapper.getRequest());
			}
		};

		FutureTask<Response> task = new FutureTask<Response>(callable);
		long startTime = System.currentTimeMillis();
		Object beforeCallData = profiler.beforeCall(wrapper.getRequest());
		executor.execute(task);
		Response response = null;
		try {
			response = task.get();
			return parser.doParse(wrapper.getConverter(), wrapper.getRequest(), response, wrapper.getResult());
		} catch (InterruptedException e) {
			log.e("syncRequest interrupted.", e);
		} catch (ExecutionException e) {
			log.e("send syncRequest has a IOException", e);
			Throwable thr = e.getCause();
			if (thr instanceof IOException) {
				throw LegolasError.networkError(wrapper.getRequest().getUrl(), (IOException) thr);
			} else {
				throw LegolasError.unexpectedError(wrapper.getRequest().getUrl(), thr);
			}
		} catch (LegolasError e) {
			throw e;
		} finally {
			profiler.afterCall(response, startTime, System.currentTimeMillis(), beforeCallData);
		}
		return null;
	}

}
