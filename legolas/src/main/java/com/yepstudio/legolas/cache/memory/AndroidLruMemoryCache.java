package com.yepstudio.legolas.cache.memory;

import java.util.Collection;

import android.util.LruCache;

import com.yepstudio.legolas.cache.CacheEntry;

/**
 * 使用Android的LruCache
 * 
 * @author zzljob@gmail.com
 * @create 2015年1月12日
 * @version 1.0，2015年1月12日
 *
 */
public class AndroidLruMemoryCache implements MemoryCache {
	
	private final LruCache<String, CacheEntry<?>> cache;

	public AndroidLruMemoryCache(int maxSize) {
		cache = new LruCache<String, CacheEntry<?>>(maxSize);
	}

	@Override
	public CacheEntry<?> get(String key) {
		return cache.get(key);
	}

	@Override
	public void put(String key, CacheEntry<?> value) {
		cache.put(key, value);
	}

	@Override
	public CacheEntry<?> remove(String key) {
		return cache.remove(key);
	}

	@Override
	public Collection<String> keys() {
		return cache.snapshot().keySet();
	}

	@Override
	public void clear() {
		cache.evictAll();
	}

}
