package com.yepstudio.legolas.cache.memory;

import java.util.Collection;

import com.yepstudio.legolas.cache.CacheEntry;

public interface MemoryCache {

	public CacheEntry<?> get(String key);

	public void put(String key, CacheEntry<?> value);

	public CacheEntry<?> remove(String key);
	
	public Collection<String> keys();

	public void clear();
}
