package com.yepstudio.android.legolas;

import android.os.Handler;

import java.util.concurrent.Executor;

import com.yepstudio.android.legolas.error.LegolasError;
import com.yepstudio.android.legolas.http.Request;
import com.yepstudio.android.legolas.http.Response;
import com.yepstudio.android.legolas.http.Request.OnRequestListener;
import com.yepstudio.android.legolas.log.LegolasLog;

/**
 * Delivers responses and errors.
 */
public class ExecutorDelivery implements ResponseDelivery {
	
	private static LegolasLog log = LegolasLog.getClazz(ExecutorDelivery.class);
    /** Used for posting responses, typically to the main thread. */
    private final Executor mResponsePoster;

    /**
     * Creates a new response delivery interface.
     * @param handler {@link Handler} to post responses on
     */
	public ExecutorDelivery(final Handler handler) {
		// Make an Executor that just wraps the handler.
		this(new Executor() {
            @Override
            public void execute(Runnable command) {
            	log.v("execute : " + command);
                handler.post(command);
            }
        });
    }

    /**
     * Creates a new response delivery interface, mockable version
     * for testing.
     * @param executor For running delivery tasks
     */
    public ExecutorDelivery(Executor executor) {
        mResponsePoster = executor;
        log.v("init with : " + executor);
    }
    
	public void submitRequest(Request request) {
		log.v("submitRequest");
		mResponsePoster.execute(new ResponseDeliveryRunnable(request, null, null, null));
	}

    @Override
    public void postResponse(Request request, Object result) {
        postResponse(request, result, null);
    }

    @Override
    public void postResponse(Request request, Object result, Runnable runnable) {
        log.d("post-response");
        mResponsePoster.execute(new ResponseDeliveryRunnable(request, result, null, runnable));
    }

    @Override
	public void postError(Request request, LegolasError error) {
		log.d("post-error");
		mResponsePoster.execute(new ResponseDeliveryRunnable(request, null, error, null));
	}

    /**
     * A Runnable used for delivering network responses to a listener on the
     * main thread.
     */
    @SuppressWarnings("rawtypes")
    private class ResponseDeliveryRunnable implements Runnable {
        private final Request mRequest;
        private final Object mResult;
        private final LegolasError mError;
        private final Runnable mRunnable;

		public ResponseDeliveryRunnable(Request request, Object result, LegolasError error, Runnable runnable) {
			mRequest = request;
			mResult = result;
			mRunnable = runnable;
			mError = error;
		}

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
			if (mResult == null && mError == null) {
				OnRequestListener onRequestListener = mRequest.getRequestListener();
				if (onRequestListener != null) {
					onRequestListener.onRequest();
				}
			} else if (mResult != null && mError == null) {
				Response.OnResponseListener OnResponseListener = mRequest.getResponseListener();
				if (OnResponseListener != null) {
					OnResponseListener.onResponse(mResult);
				}
			} else if (mResult == null && mError != null) {
				Response.OnErrorListener onErrorListener = mRequest.getErrorListener();
				if (onErrorListener != null) {
					onErrorListener.onError(mError);
				}
			}
        	
            // If we have been provided a post-delivery runnable, run it.
            if (mRunnable != null) {
                mRunnable.run();
            }
       }
    }
}
