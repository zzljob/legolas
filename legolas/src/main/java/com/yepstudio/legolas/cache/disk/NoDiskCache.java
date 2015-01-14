package com.yepstudio.legolas.cache.disk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yepstudio.legolas.cache.CacheEntry;
import com.yepstudio.legolas.response.Response;

/**
 * A cache that doesn't.
 */
public class NoDiskCache implements DiskCache {

	private static Logger logger = LoggerFactory.getLogger(NoDiskCache.class);

	@Override
	public CacheEntry<Response> get(String key) {
		logger.debug("get : {}, result : null", key);
		return null;
	}

	@Override
	public void put(String key, CacheEntry<Response> entry) {
		logger.debug("put : {}, result : {}", key, entry);
	}

	@Override
	public void initialize() {
		logger.debug("initialize");
	}

	@Override
	public void invalidate(String key, boolean fullExpire) {
		logger.debug("invalidate : {}, fullExpire : {}", key, fullExpire);
	}

	@Override
	public void remove(String key) {
		logger.debug("remove : {}", key);
	}

	@Override
	public void clear() {
		logger.debug("clear");
	}

}
