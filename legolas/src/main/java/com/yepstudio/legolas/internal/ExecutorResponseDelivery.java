package com.yepstudio.legolas.internal;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.yepstudio.legolas.Legolas;
import com.yepstudio.legolas.LegolasException;
import com.yepstudio.legolas.ResponseDelivery;
import com.yepstudio.legolas.listener.LegolasListenerWrapper;
import com.yepstudio.legolas.request.OnRequestListener;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.request.AsyncRequest;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.OnResponseListener;
import com.yepstudio.legolas.response.ResponseListenerWrapper;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年4月30日
 * @version 2.0, 2014年4月30日
 *
 */
public class ExecutorResponseDelivery implements ResponseDelivery {
	
	private final Executor deliveryExecutor = Executors.newSingleThreadExecutor();
    private final Executor taskExecutor;

	public ExecutorResponseDelivery(Executor executor) {
		super();
		this.taskExecutor = executor;
	}

	@Override
	public void postAsyncResponse(final AsyncRequest wrapper) {
		if (wrapper.isCancel()) {
			return ;
		}
		deliveryExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
				doPostAsyncResponse(wrapper);
			}
		});
	}
	
	private void doPostAsyncResponse(AsyncRequest wrapper) {
		if (wrapper.isCancel()) {
			return ;
		}
		List<ResponseListenerWrapper> listeners = wrapper.getOnResponseListeners();
		if (listeners != null && !listeners.isEmpty()) {
			for (ResponseListenerWrapper listener : listeners) {
				if (listener == null) {
					continue;
				}
				ListenerRunnable run = new ListenerRunnable(wrapper, null, listener, null, null); 
				FutureTask<Void> task = new FutureTask<Void>(run, null);
				taskExecutor.execute(task);
				try {
					task.get();
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
				}
			}
		}
		List<LegolasListenerWrapper> legolasListeners = wrapper.getOnLegolasListeners();
		if (legolasListeners != null && !legolasListeners.isEmpty()) {
			for (LegolasListenerWrapper listenerWrapper : legolasListeners) {
				if (listenerWrapper == null) {
					continue;
				}
				LegolasListenerRunnable run = new LegolasListenerRunnable(wrapper, listenerWrapper, null);
				run.onResponse = true;
				FutureTask<Void> task = new FutureTask<Void>(run, null);
				taskExecutor.execute(task);
				try {
					task.get();
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
				}
			}
		}
	}

	@Override
	public void postAsyncError(final AsyncRequest wrapper, final LegolasException exception) {
		if (wrapper.isCancel()) {
			return ;
		}
		deliveryExecutor.execute(new Runnable() {

			@Override
			public void run() {
				doPostAsyncError(wrapper, exception);
			}
		});
	}
	
	private void doPostAsyncError(AsyncRequest wrapper, LegolasException exception) {
		if (wrapper.isCancel()) {
			return ;
		}
		List<OnErrorListener> listeners = wrapper.getOnErrorListeners();
		if (listeners != null && !listeners.isEmpty()) {
			for (OnErrorListener listener : listeners) {
				if (listener == null) {
					continue;
				}
				ListenerRunnable run = new ListenerRunnable(wrapper, null, null, listener, exception);
				FutureTask<Void> task = new FutureTask<Void>(run, null);
				taskExecutor.execute(task);
				try {
					task.get();
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
				}
			}
		}
		List<LegolasListenerWrapper> legolasListeners = wrapper.getOnLegolasListeners();
		if (legolasListeners != null && !legolasListeners.isEmpty()) {
			for (LegolasListenerWrapper listenerWrapper : legolasListeners) {
				if (listenerWrapper == null) {
					continue;
				}
				LegolasListenerRunnable run = new LegolasListenerRunnable(wrapper, listenerWrapper, exception);
				run.onError = true;
				FutureTask<Void> task = new FutureTask<Void>(run, null);
				taskExecutor.execute(task);
				try {
					task.get();
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
				}
			}
		}
	}

	@Override
	public void postAsyncRequest(final AsyncRequest wrapper) {
		if (wrapper.isCancel()) {
			return ;
		}
		Legolas.getLog().d("postAsyncRequest");
		deliveryExecutor.execute(new Runnable() {

			@Override
			public void run() {
				doPostAsyncRequest(wrapper);
			}
		});
	}
	
	private void doPostAsyncRequest(final AsyncRequest wrapper) {
		if (wrapper.isCancel()) {
			return ;
		}
		Legolas.getLog().d("doPostAsyncRequest");
		List<OnRequestListener> listeners = wrapper.getOnRequestListeners();
		if (listeners != null && !listeners.isEmpty()) {
			for (OnRequestListener listener : listeners) {
				if (listener == null) {
					continue;
				}
				ListenerRunnable run = new ListenerRunnable(wrapper, listener, null, null, null);
				FutureTask<Void> task = new FutureTask<Void>(run, null);
				taskExecutor.execute(task);
				try {
					task.get();
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
				}
			}
		}
		List<LegolasListenerWrapper> legolasListeners = wrapper.getOnLegolasListeners();
		if (legolasListeners != null && !legolasListeners.isEmpty()) {
			for (LegolasListenerWrapper listenerWrapper : legolasListeners) {
				if (listenerWrapper == null) {
					continue;
				}
				LegolasListenerRunnable run = new LegolasListenerRunnable(wrapper, listenerWrapper, null);
				run.onRequest = true;
				FutureTask<Void> task = new FutureTask<Void>(run, null); 
				taskExecutor.execute(task);
				try {
					task.get();
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
				}
			}
		}
	}
	
	private static class ListenerRunnable implements Runnable {
		private final Request request;
		private final OnRequestListener requestListener;
		private final ResponseListenerWrapper responseListenerWrapper;
		private final OnErrorListener errorListener;
		private final LegolasException exception;

		public ListenerRunnable(Request request,
				OnRequestListener requestListener,
				ResponseListenerWrapper responseListenerWrapper,
				OnErrorListener errorListener, 
				LegolasException exception) {
			super();
			this.request = request;
			this.requestListener = requestListener;
			this.responseListenerWrapper = responseListenerWrapper;
			this.errorListener = errorListener;
			this.exception = exception;
		}

		@Override
		public void run() {
			if (requestListener != null) {
				if (!isCancel()) {
					requestListener.onRequest(request);
				}
			}
			if (responseListenerWrapper != null) {
				OnResponseListener listener = responseListenerWrapper.getListener();
				if (listener != null && !isCancel()) {
					listener.onResponse(responseListenerWrapper.getResponseValue());
				}
			}
			if (errorListener != null && !isCancel()) {
				errorListener.onError(exception);
			}
		}
		
		private boolean isCancel() {
			return request != null && request.isCancel();
		}
	}
	
	private static class LegolasListenerRunnable implements Runnable {
		private final Request request;
		private final LegolasListenerWrapper listenerWrapper;
		private boolean onRequest = false;
		private boolean onResponse = false;
		private boolean onError = false;
		private final LegolasException exception;

		public LegolasListenerRunnable(Request request,
				LegolasListenerWrapper listenerWrapper, LegolasException exception) {
			super();
			this.request = request;
			this.listenerWrapper = listenerWrapper;
			this.exception = exception;
		}

		@Override
		public void run() {
			if (listenerWrapper == null
					|| listenerWrapper.getListener() == null) {
				return;
			}
			if (onRequest && !isCancel()) {
				Legolas.getLog().d("listenerWrapper onRequest");
				listenerWrapper.getListener().onRequest(request);
			}
			if (onResponse && !isCancel()) {
				Legolas.getLog().d("listenerWrapper onResponse");
				Object response = listenerWrapper.getResponseValue();
				listenerWrapper.getListener().onResponse(request, response);
			}
			if (onError && !isCancel()) {
				Legolas.getLog().d("listenerWrapper onError");
				Object error = listenerWrapper.getErrorValue();
				listenerWrapper.getListener().onError(request, exception, error);
			}
		}
		
		private boolean isCancel() {
			return request != null && request.isCancel();
		}
	}


}
