package com.yepstudio.legolas.cache.memory;

import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yepstudio.legolas.cache.CacheEntry;

/**
 * A cache that doesn't.
 */
public class NoMemoryCache implements MemoryCache {

	private static Logger logger = LoggerFactory.getLogger(NoMemoryCache.class);

	@Override
	public CacheEntry<?> get(String key) {
		logger.debug("get : {}, result : null", key);
		return null;
	}

	@Override
	public void put(String key, CacheEntry<?> value) {
		logger.debug("put : {}, value : {}", key, value);
	}

	@Override
	public void clear() {
		logger.debug("clear");
	}

	@Override
	public CacheEntry<?> remove(String key) {
		logger.debug("remove : {}", key);
		return null;
	}

	@Override
	public Collection<String> keys() {
		return Collections.emptySet();
	}

}
