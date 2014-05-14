package com.yepstudio.legolas;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import android.os.Build;

import com.yepstudio.legolas.internal.AndroidPlatform;
import com.yepstudio.legolas.internal.BasePlatform;
import com.yepstudio.legolas.internal.SimpleProfiler;

public abstract class Platform {

	private static final Platform PLATFORM = findPlatform();

	private static Platform findPlatform() {
		Platform p = null;
		try {
			Class.forName("android.os.Build");
			if (Build.VERSION.SDK_INT != 0) {
				return null;
			}
			p = new AndroidPlatform();
		} catch (ClassNotFoundException ignored) {
			
		}
		p = new BasePlatform();
		return p;
	}

	public static Platform get() {
		return PLATFORM;
	}

	/**
	 * 转换器
	 * 
	 * @return
	 */
	public abstract Converter defaultConverter();

	/**
	 * 默认的Http请求器
	 * 
	 * @return
	 */
	public abstract HttpSender defaultHttpSender();

	/**
	 * 默认的Http的执行器，也就是Http请求的执行线程
	 * 
	 * @return
	 */
	public abstract ExecutorService defaultHttpExecutor();

	/**
	 * 默认的投递的执行器，也就是Listener的执行线程
	 * 
	 * @return
	 */
	public abstract Executor defaultDeliveryExecutor();

	public abstract Class<? extends LegolasLog> defaultLog();

	public Profiler<?> defaultProfiler() {
		return new SimpleProfiler();
	}

	public boolean isDebug() {
		return false;
	}

	public RequestInterceptor defaultRequestInterceptor() {
		return new RequestInterceptor() {

			@Override
			public void interceptor(RequestInterceptorFace face) {
				
			}

		};
	}

}
