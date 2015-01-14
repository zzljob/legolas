package com.yepstudio.legolas.cache.memory;

import java.lang.ref.Reference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.yepstudio.legolas.cache.CacheEntry;

public abstract class BasicMemoryCache implements MemoryCache {
	
	/** Stores not strong references to objects */
	private final Map<String, Reference<CacheEntry<?>>> softMap = Collections.synchronizedMap(new HashMap<String, Reference<CacheEntry<?>>>());

	@Override
	public CacheEntry<?> get(String key) {
		CacheEntry<?> result = null;
		Reference<CacheEntry<?>> reference = softMap.get(key);
		if (reference != null) {
			result = reference.get();
		}
		return result;
	}

	@Override
	public void put(String key, CacheEntry<?> value) {
		softMap.put(key, createReference(value));
	}

	@Override
	public CacheEntry<?> remove(String key) {
		Reference<CacheEntry<?>> bmpRef = softMap.remove(key);
		return bmpRef == null ? null : bmpRef.get();
	}

	@Override
	public void clear() {
		softMap.clear();
	}
	
	@Override
	public Collection<String> keys() {
		synchronized (softMap) {
			return new HashSet<String>(softMap.keySet());
		}
	}
	
	protected abstract Reference<CacheEntry<?>> createReference(CacheEntry<?> value); 

}
