package com.yepstudio.android.legolas.http;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import android.os.Handler;
import android.os.Looper;

import com.yepstudio.android.legolas.ExecutorDelivery;
import com.yepstudio.android.legolas.ResponseDelivery;
import com.yepstudio.android.legolas.error.LegolasError;
import com.yepstudio.android.legolas.http.client.Client;
import com.yepstudio.android.legolas.log.LegolasLog;

/**
 * 简单的网络执行器
 * @author zzljob@gmail.com
 * @createDate 2014年1月8日
 */
public class SimpleRequestExecutor implements RequestExecutor {
	private static LegolasLog log = LegolasLog.getClazz(SimpleRequestExecutor.class);
	private static final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool(); 
	private final Client client;
	private final ResponseDelivery delivery;
	private final ResponseParser parser;

	public SimpleRequestExecutor(Client client) {
		this.client = client;
		parser = new SimpleResponseParser();
		delivery = new ExecutorDelivery(new Handler(Looper.getMainLooper()));
		log.v("init");
	}

	@Override
	public void doRequest(final Request request) {
		log.v("doRequest execute ...");
		threadPool.submit(new Runnable() {

			@Override
			public void run() {
				delivery.submitRequest(request);
				try {
					Response response = client.execute(request);
					delivery.postResponse(request, parser.doParse(request, response, request.getResult()));
				} catch (IOException e) {
					log.e("IOException", e);
					delivery.postError(request, LegolasError.networkError(request.getUrl(), e));
				} catch (LegolasError e) {
					log.e("LegolasError", e);
					delivery.postError(request, e);
				}
			}
		});

	}

	@Override
	public Object syncRequest(final Request request, Type type) throws LegolasError {
		log.v("syncRequest execute ...");
		Callable<Response> callable = new Callable<Response>() {

			@Override
			public Response call() throws IOException {
				return client.execute(request);
			}
		};

		Future<Response> task = threadPool.submit(callable);

		try {
			Response response = task.get();
			return parser.doParse(request, response, type);
		} catch (InterruptedException e) {
			log.e("syncRequest interrupted.", e);
		} catch (ExecutionException e) {
			log.e("send syncRequest has a IOException", e);
			Throwable thr = e.getCause();
			if (thr instanceof IOException) {
				throw LegolasError.networkError(request.getUrl(), (IOException) thr);
			} else {
				throw LegolasError.unexpectedError(request.getUrl(), thr);
			}
		} catch (LegolasError e) {
			throw e;
		}
		return null;
	}

}
