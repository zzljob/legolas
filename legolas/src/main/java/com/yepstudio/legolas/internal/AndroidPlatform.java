package com.yepstudio.legolas.internal;

import java.util.concurrent.Executor;

import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.Platform;

public class AndroidPlatform extends Platform {

	@Override
	public Converter defaultConverter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpSender defaultHttpSender() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Executor defaultHttpExecutor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Executor defaultDeliveryExecutor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends LegolasLog> defaultLog() {
		// TODO Auto-generated method stub
		return null;
	}

}
