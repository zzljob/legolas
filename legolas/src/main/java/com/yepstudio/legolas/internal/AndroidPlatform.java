package com.yepstudio.legolas.internal;

import java.util.concurrent.Executor;

import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.Platform;
import com.yepstudio.legolas.internal.converter.GsonConverter;
import com.yepstudio.legolas.internal.converter.JSONConverter;

public class AndroidPlatform extends Platform {

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
