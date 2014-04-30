package com.yepstudio.legolas.internal;

import java.util.concurrent.Executor;

import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.Platform;
import com.yepstudio.legolas.internal.log.Sl4fLog;

public class BasePlatform extends Platform {

	@Override
	public Converter defaultConverter() {
		
		return null;
	}

	@Override
	public HttpSender defaultHttpSender() {
		
		return null;
	}

	@Override
	public Executor defaultHttpExecutor() {
		return null;
	}

	@Override
	public Executor defaultDeliveryExecutor() {
		return null;
	}

	@Override
	public Class<? extends LegolasLog> defaultLog() {
		return Sl4fLog.class;
	}

}
