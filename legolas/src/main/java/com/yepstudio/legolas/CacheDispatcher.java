package com.yepstudio.legolas;

import com.yepstudio.legolas.cache.CacheEntry;
import com.yepstudio.legolas.request.BasicRequest;
import com.yepstudio.legolas.request.AsyncRequest;
import com.yepstudio.legolas.request.SyncRequest;
import com.yepstudio.legolas.response.Response;

public interface CacheDispatcher {

	public boolean getSyncRequestConverterCache(SyncRequest wrapper);

	public void updateSyncRequestConverterCache(SyncRequest wrapper);

	public boolean getAsyncRequestConverterCache(AsyncRequest wrapper);

	public void updateAsyncRequestConverterCache(AsyncRequest wrapper);

	public CacheEntry<Response> getRequestCacheEntry(BasicRequest wrapper);

	public void updateRequestCache(BasicRequest wrapper, Response response);

}
