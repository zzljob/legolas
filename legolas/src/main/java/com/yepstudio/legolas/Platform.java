package com.yepstudio.legolas;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import com.yepstudio.legolas.internal.SimpleProfiler;
import com.yepstudio.legolas.internal.platform.BasicPlatform;

public abstract class Platform {

	private static Platform PLATFORM = null;

	public static void initPlatform(Platform platform) {
		PLATFORM = platform;
	}
	
	public static Platform get() {
		if (PLATFORM == null) {
			PLATFORM = new BasicPlatform();
		}
		return PLATFORM;
	}

	/**
	 * 转换器
	 * 
	 * @return
	 */
	public abstract Converter defaultConverter();
	
	public abstract Cache defaultCache();

	/**
	 * 默认的Http请求器
	 * 
	 * @return
	 */
	public abstract HttpSender defaultHttpSender();

	/**
	 * 默认的Http的执行器，也就是Http请求的执行线程
	 * 
	 * @return
	 */
	public abstract ExecutorService defaultHttpExecutor();
	
	/**
	 * 默认的投递的执行器，也就是Listener的执行线程
	 * 
	 * @return
	 */
	public abstract Executor defaultResponseDeliveryExecutor();

	public abstract Class<? extends LegolasLog> defaultLog();

	public Profiler<?> defaultProfiler() {
		return new SimpleProfiler();
	}

	public boolean hasNetwork() {
		return true;
	}

}
