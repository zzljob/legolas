package com.yepstudio.legolas.internal;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.Platform;
import com.yepstudio.legolas.internal.converter.GsonConverter;
import com.yepstudio.legolas.internal.http.HttpClientHttpSender;
import com.yepstudio.legolas.internal.log.Sl4fLog;

public class BasePlatform extends Platform {
	private ExecutorService executor = Executors.newCachedThreadPool();;
	
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
	public Executor defaultDeliveryExecutor() {
		return executor;
	}

	@Override
	public Class<? extends LegolasLog> defaultLog() {
		return Sl4fLog.class;
	}

}
