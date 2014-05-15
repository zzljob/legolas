package com.yepstudio.legolas.internal.platform;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.yepstudio.legolas.Cache;
import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.Platform;
import com.yepstudio.legolas.internal.DiskBasedCache;
import com.yepstudio.legolas.internal.NoCache;
import com.yepstudio.legolas.internal.converter.GsonConverter;
import com.yepstudio.legolas.internal.converter.JSONConverter;
import com.yepstudio.legolas.internal.http.AndroidHttpClientHttpSender;
import com.yepstudio.legolas.internal.http.UrlConnectionHttpSender;
import com.yepstudio.legolas.internal.log.AndroidLog;
import com.yepstudio.legolas.internal.log.Sl4fLog;

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
		if (android.os.Build.VERSION.SDK_INT >= 9) {
			return new AndroidHttpClientHttpSender();
		} else {
			return new UrlConnectionHttpSender();
		}
	}

	@Override
	public ExecutorService defaultHttpExecutor() {
		return Executors.newCachedThreadPool();
	}

	@Override
	public Executor defaultResponseDeliveryExecutor() {
		return new Executor(){

			@Override
			public void execute(Runnable command) {
				handler.post(command);
			}
			
		};
	}

	@Override
	public Class<? extends LegolasLog> defaultLog() {
		try {
			Class<?> clazz = Class.forName("org.slf4j.Logger");
			if (clazz != null) {
				return Sl4fLog.class;
			}
		} catch (Throwable th) {
		}
		return AndroidLog.class;
	}

	@Override
	public Cache defaultCache() {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				&& !Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())) {
			File file = Environment.getExternalStorageDirectory();
			Cache cache = new DiskBasedCache(new File(file, "legolas"));
			cache.initialize();
			return cache;
		} else {
			return new NoCache();
		}
	}

}
