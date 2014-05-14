package com.yepstudio.legolas.internal;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Looper;

import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.Platform;
import com.yepstudio.legolas.internal.converter.GsonConverter;
import com.yepstudio.legolas.internal.converter.JSONConverter;
import com.yepstudio.legolas.internal.http.AndroidHttpClientHttpSender;
import com.yepstudio.legolas.internal.log.AndroidLog;

public class AndroidPlatform extends Platform {
	private Handler handler = new Handler(Looper.getMainLooper());

	@Override
	public Converter defaultConverter() {
		Converter converter = null;
		try {
			Class<?> clazz = Class.forName("com.google.gson.Gson");
			if (clazz != null) {
				converter = new GsonConverter();
			}
		} catch (Throwable th) {
			
		}
		if (converter == null) {
			converter = new JSONConverter();
		}
		return converter;
	}

	@Override
	public HttpSender defaultHttpSender() {
		return new AndroidHttpClientHttpSender();
	}

	@Override
	public ExecutorService defaultHttpExecutor() {
		return Executors.newCachedThreadPool();
	}

	@Override
	public Executor defaultDeliveryExecutor() {
		return new Executor(){

			@Override
			public void execute(Runnable command) {
				handler.post(command);
			}
			
		};
	}

	@Override
	public Class<? extends LegolasLog> defaultLog() {
		return AndroidLog.class;
	}

}
