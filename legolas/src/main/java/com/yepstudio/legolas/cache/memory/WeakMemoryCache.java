package com.yepstudio.legolas.cache.memory;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import com.yepstudio.legolas.cache.CacheEntry;

public class WeakMemoryCache extends BasicMemoryCache {
	
	@Override
	protected Reference<CacheEntry<?>> createReference(CacheEntry<?> value) {
		return new WeakReference<CacheEntry<?>>(value);
	}
	
}
