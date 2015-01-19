package com.yepstudio.legolas.internal;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.Platform;
import com.yepstudio.legolas.Profiler;
import com.yepstudio.legolas.cache.disk.BasicDiskCache;
import com.yepstudio.legolas.cache.disk.DiskCache;
import com.yepstudio.legolas.cache.memory.MemoryCache;
import com.yepstudio.legolas.cache.memory.WeakMemoryCache;
import com.yepstudio.legolas.converter.BasicConverter;
import com.yepstudio.legolas.converter.GsonConverter;
import com.yepstudio.legolas.httpsender.HttpClientHttpSender;
import com.yepstudio.legolas.httpsender.UrlConnectionHttpSender;

public class BasicPlatform extends Platform {
	
	public LegolasLog defaultLog() {
		if (hasClass("org.slf4j.Logger")) {
			return new Sl4fLog();
		} else {
			return new NoneLog();
		}
	}
	
	@Override
	public Converter defaultConverter() {
		if (hasClass("com.google.gson.Gson")) {
			return new GsonConverter();
		} else {
			return new BasicConverter();
		}
	}
	
	private Executor executor = Executors.newCachedThreadPool();
	
	@Override
	public HttpSender defaultHttpSender() {
		if (hasClass("org.apache.http.client.HttpClient")) {
			return new HttpClientHttpSender();
		} else {
			return new UrlConnectionHttpSender();
		}
	}
	
	@Override
	public Executor defaultTaskExecutorForHttp() {
		return executor;
	}

	@Override
	public DiskCache defaultDiskCache() {
		String dirStr = System.getProperty("java.io.tmpdir");
		StringBuilder builder = new StringBuilder(dirStr);
		builder.append(File.separator).append("legolas").append(File.separator);
		DiskCache cache = new BasicDiskCache(new File(builder.toString()));
		return cache;
	}
	
	@Override
	public MemoryCache defaultMemoryCache() {
		return new WeakMemoryCache();
	}
	
	@Override
	public Executor defaultTaskExecutorForCache() {
		return executor;
	}
	
	public Profiler<?> defaultProfiler() {
		return new SimpleProfiler();
	}
	
	@Override
	public Executor defaultTaskExecutorForProfiler() {
		return executor;
	}

	@Override
	public Executor defaultTaskExecutorForListener() {
		return executor;
	}

}
