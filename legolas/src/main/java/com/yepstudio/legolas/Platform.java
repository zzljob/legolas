package com.yepstudio.legolas;

import java.util.concurrent.Executor;

import com.yepstudio.legolas.cache.disk.DiskCache;
import com.yepstudio.legolas.cache.memory.MemoryCache;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年5月19日
 * @version 2.0, 2014年5月19日
 *
 */
public abstract class Platform {

	public String defaultCharset() {
		return "UTF-8";
	}

	public abstract LegolasLog defaultLog();

	public abstract Converter defaultConverter();

	public abstract HttpSender defaultHttpSender();

	public abstract Executor defaultTaskExecutorForHttp();

	public abstract Executor defaultTaskExecutorForListener();

	public abstract DiskCache defaultDiskCache();

	public abstract MemoryCache defaultMemoryCache();

	public abstract Executor defaultTaskExecutorForCache();

	public abstract Profiler<?> defaultProfiler();

	public abstract Executor defaultTaskExecutorForProfiler();

	protected boolean hasClass(String className) {
		try {
			Class<?> clazz = Class.forName(className);
			if (clazz != null) {
				return true;
			}
		} catch (Throwable th) {
		}
		return false;
	}

}
