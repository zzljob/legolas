package com.yepstudio.legolas.internal.platform;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.yepstudio.legolas.Cache;
import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.Platform;
import com.yepstudio.legolas.cache.DiskBasedCache;
import com.yepstudio.legolas.converter.GsonConverter;
import com.yepstudio.legolas.httpsender.HttpClientHttpSender;
import com.yepstudio.legolas.internal.log.Sl4fLog;

public class BasePlatform extends Platform {
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	@Override
	public Converter defaultConverter() {
		return new GsonConverter();
	}

	@Override
	public HttpSender defaultHttpSender() {
		return new HttpClientHttpSender();
	}

	@Override
	public ExecutorService defaultHttpExecutor() {
		return executor;
	}

	@Override
	public Executor defaultResponseDeliveryExecutor() {
		return executor;
	}

	@Override
	public Class<? extends LegolasLog> defaultLog() {
		return Sl4fLog.class;
	}

	@Override
	public Cache defaultCache() {
		String dirStr = System.getProperty("java.io.tmpdir");
		StringBuilder builder = new StringBuilder(dirStr);
		builder.append(File.separator).append("legolas").append(File.separator);
		Cache cache = new DiskBasedCache(new File(builder.toString()));
		cache.initialize();
		return cache;
	}

}
