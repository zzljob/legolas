package com.yepstudio.legolas.internal;

import java.io.File;
import java.util.concurrent.Executor;

import android.os.Build;
import android.os.Environment;

import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.cache.disk.BasicDiskCache;
import com.yepstudio.legolas.cache.disk.DiskCache;
import com.yepstudio.legolas.cache.disk.NoDiskCache;
import com.yepstudio.legolas.cache.memory.AndroidLruMemoryCache;
import com.yepstudio.legolas.cache.memory.MemoryCache;
import com.yepstudio.legolas.converter.AndroidConverter;
import com.yepstudio.legolas.converter.GsonConverter;
import com.yepstudio.legolas.httpsender.AndroidHttpClientHttpSender;
import com.yepstudio.legolas.httpsender.UrlConnectionHttpSender;

public class AndroidPlatform extends BasicPlatform {
	
	@Override
	public LegolasLog defaultLog() {
		if (hasClass("org.slf4j.Logger")) {
			return new Sl4fLog();
		} else {
			return new AndroidLog();
		}
	}
	
	@Override
	public Converter defaultConverter() {
		if (hasClass("com.google.gson.Gson")) {
			return new GsonConverter();
		} else {
			return new AndroidConverter();
		}
	}
	
	@Override
	public HttpSender defaultHttpSender() {
		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			return new AndroidHttpClientHttpSender();
		} else {
			return new UrlConnectionHttpSender();
		}
	}
	
	@Override
	public Executor defaultTaskExecutorForListener() {
		return new AndroidUiThreadExecutor();
	}

	@Override
	public DiskCache defaultDiskCache() {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				&& !Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())) {
			File file = Environment.getExternalStorageDirectory();
			DiskCache cache = new BasicDiskCache(new File(file, "legolas"));
			return cache;
		} else {
			return new NoDiskCache();
		}
	}

	@Override
	public MemoryCache defaultMemoryCache() {
		return new AndroidLruMemoryCache();
	}

}
