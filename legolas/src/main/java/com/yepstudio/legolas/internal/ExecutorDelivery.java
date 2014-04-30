package com.yepstudio.legolas.internal;

import java.util.concurrent.Executor;

import com.yepstudio.legolas.LegolasError;
import com.yepstudio.legolas.ResponseDelivery;
import com.yepstudio.legolas.request.OnRequestListener;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.OnResponseListener;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年4月30日
 * @version 2.0, 2014年4月30日
 *
 */
public class ExecutorDelivery implements ResponseDelivery {
	
    private final Executor executor;

    public ExecutorDelivery(Executor executor) {
        this.executor = executor;
    }
    
	public void submitRequest(final OnRequestListener onRequestListener, final Request request) {
		if (onRequestListener == null) {
			return;
		}
		executor.execute(new Runnable() {

			@Override
			public void run() {
				onRequestListener.onRequest(request);
			}
		});
	}

    @Override
    public void postResponse(OnResponseListener onResponseListener, Request request, Object result) {
    	postResponse(onResponseListener, request, result, null);
    }

    @Override
    public void postResponse(final OnResponseListener onResponseListener, final Request request, final Object result, final Runnable runnable) {
    	if (onResponseListener == null) {
			return;
		}
    	executor.execute(new Runnable() {

			@Override
			public void run() {
				onResponseListener.onResponse(result);
				if (runnable != null) {
					runnable.run();
				}
			}
		});
    }

    @Override
	public void postError(final OnErrorListener onErrorListener, final Request request, final LegolasError error) {
    	if(onErrorListener == null){
    		return ;
    	}
    	executor.execute(new Runnable() {

			@Override
			public void run() {
				onErrorListener.onError(error);
			}
		});
	}
}
