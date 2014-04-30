package com.yepstudio.legolas;

import java.util.concurrent.Executor;

import android.os.Build;

import com.yepstudio.legolas.internal.SimpleProfiler;

public abstract class Platform {

	private static final Platform PLATFORM = findPlatform();

	private static Platform findPlatform() {
		try {
			Class.forName("android.os.Build");
			if (Build.VERSION.SDK_INT != 0) {
				return null;
			}
		} catch (ClassNotFoundException ignored) {

		}

		return null;
	}

	public static Platform get() {
		return PLATFORM;
	}

	public abstract Converter defaultConverter();

	public abstract HttpSender defaultHttpSender();

	public abstract Executor defaultHttpExecutor();

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
			public boolean interceptor(RequestInterceptorFace face) {
				return false;
			}

		};
	}

}
