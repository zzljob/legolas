package com.yepstudio.legolas.internal;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.yepstudio.legolas.CacheDispatcher;
import com.yepstudio.legolas.Legolas;
import com.yepstudio.legolas.LegolasOptions;
import com.yepstudio.legolas.LegolasOptions.RecoveryPolicy;
import com.yepstudio.legolas.cache.CacheEntry;
import com.yepstudio.legolas.cache.disk.DiskCache;
import com.yepstudio.legolas.cache.memory.MemoryCache;
import com.yepstudio.legolas.listener.LegolasListenerWrapper;
import com.yepstudio.legolas.request.AsyncRequest;
import com.yepstudio.legolas.request.BasicRequest;
import com.yepstudio.legolas.request.SyncRequest;
import com.yepstudio.legolas.response.Response;
import com.yepstudio.legolas.response.ResponseListenerWrapper;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2015年1月5日
 * @version 1.0，2015年1月5日
 *
 */
public class ExecutorCacheDispatcher implements CacheDispatcher {

	private final Executor taskExecutorForCache;
	/***毫秒*MILLISECONDS**/
	private final long converterResultMaxExpired;
	private final MemoryCache memoryCache;
	private final DiskCache diskCache;
	private final CompletionService<Boolean> service;
	private AtomicBoolean enableCache = new AtomicBoolean(true);
	
	private static final Date DEFAULT_TWO_DIGIT_YEAR_START;

	public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

	static {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(GMT);
		calendar.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		DEFAULT_TWO_DIGIT_YEAR_START = calendar.getTime();
	}
	
	public ExecutorCacheDispatcher(Executor executor, MemoryCache memoryCache, DiskCache diskCache, long converterResultMaxExpired) {
		super();
		this.taskExecutorForCache = executor;
		this.converterResultMaxExpired = converterResultMaxExpired;
		this.memoryCache = memoryCache;
		this.diskCache = diskCache;
		this.service = new ExecutorCompletionService<Boolean>(taskExecutorForCache);
	}
	
	public void updateRequestCache(final BasicRequest wrapper, Response response) {
		//开始存储Response
		FutureTask<Void> memoryTask = null;
		if (needUpdateMemoryCacheForResponse(wrapper, response)) {
			wrapper.appendLog("Memory Cache Response update. \n");
			UpdateRequestCacheRunnable runnable = new UpdateRequestCacheRunnable(wrapper, response, memoryCache, diskCache);
			runnable.updateMemoryResponse = true;
			memoryTask = new FutureTask<Void>(runnable, null);
			taskExecutorForCache.execute(memoryTask); 
		} else {
			wrapper.appendLog("Memory Cache Response Ignore. \n");
		}
		
		FutureTask<Void> diskTask = null;
		//存入磁盘缓存
		//开始存储Response
		if (needUpdateDiskCacheForResponse(wrapper, response)) {
			wrapper.appendLog("Disk Cache Response update. \n");
			UpdateRequestCacheRunnable runnable = new UpdateRequestCacheRunnable(wrapper, response, memoryCache, diskCache);
			runnable.updateDiskResponse = true;
			diskTask = new FutureTask<Void>(runnable, null);
			taskExecutorForCache.execute(diskTask); 
		} else {
			wrapper.appendLog("Disk Cache Response Ignore. \n");
		}
		
		try {
			if (memoryTask != null) {
				memoryTask.get(1, TimeUnit.MINUTES);
			}
			if (diskTask != null) {
				diskTask.get(1, TimeUnit.MINUTES);
			}
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (TimeoutException e) {
		}
	}

	public void updateSyncRequestConverterCache(final SyncRequest wrapper) {
		Legolas.getLog().d("updateSyncRequestConverterCache");
		appendLogForUpdateCache(wrapper);
		LegolasOptions options = wrapper.getOptions();
		wrapper.appendLog("updateSyncRequestConverterCache: ");
		if (options.isCacheConverterResult()) {
			wrapper.appendLog("Sync Result ignore. \n");
			return ;
		}
		if (wrapper.getResultValue() == null) {
			wrapper.appendLog("SyncRequest ResultValue is null, update quit. \n");
			return ;
		}
		
		UpdateSyncRequestConverterCacheRunnable runnable = new UpdateSyncRequestConverterCacheRunnable(wrapper, memoryCache, converterResultMaxExpired);
		FutureTask<Void> task = new FutureTask<Void>(runnable, null);
		taskExecutorForCache.execute(task); 
		try {
			task.get(1, TimeUnit.MINUTES);
			wrapper.appendLog("Sync Result update. \n");
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (TimeoutException e) {
		}
		taskExecutorForCache.execute(task); 
	}
	
	private boolean needUpdateMemoryCacheForResponse(BasicRequest wrapper, Response response) {
		if (response.isFromMemoryCache()) {
			return false;
		}
		return true;
	}
	
	private boolean needLoadMemoryCacheForResponse(BasicRequest wrapper) {
		if (!enableCache.get()) {
			return false;
		}
		RecoveryPolicy policy = wrapper.getRecoveryPolicy();
		if (RecoveryPolicy.NONE != policy) {
			return true;
		}
		return wrapper.isCacheResponseInMemory();
	}
	
	private boolean needUpdateDiskCacheForResponse(BasicRequest wrapper, Response response) {
		if (response.isFromDiskCache()) {
			return false;
		}
		return true;
	}
	
	private boolean needLoadDiskCacheForResponse(BasicRequest wrapper) {
		if (!enableCache.get()) {
			return false;
		}
		RecoveryPolicy policy = wrapper.getRecoveryPolicy();
		if (RecoveryPolicy.NONE != policy) {
			return true;
		}
		return wrapper.isCacheResponseOnDisk();
	}
	
	public void updateAsyncRequestConverterCache(final AsyncRequest wrapper) {
		appendLogForUpdateCache(wrapper);
		wrapper.appendLog("updateSyncRequestConverterCache: ");
		if (!wrapper.getOptions().isCacheConverterResult()) {
			wrapper.appendLog("Async Result ignore. \n");
			return ;
		}
		//存入内存缓存
		UpdateAsyncRequestConverterCacheRunnable runnable = new UpdateAsyncRequestConverterCacheRunnable(wrapper, memoryCache, converterResultMaxExpired);
		FutureTask<Void> task = new FutureTask<Void>(runnable, null);
		taskExecutorForCache.execute(task); 
		try {
			task.get(1, TimeUnit.MINUTES);
			wrapper.appendLog("Async ListenerResultValue update. \n");
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (TimeoutException e) {
		}
	}
	
	private static class UpdateSyncRequestConverterCacheRunnable implements Runnable {
		private final SyncRequest wrapper;
		private final MemoryCache memoryCache;
		private final long converterResultMaxExpired;
		
		public UpdateSyncRequestConverterCacheRunnable(SyncRequest wrapper,
				MemoryCache memoryCache, long converterResultMaxExpired) {
			super();
			this.wrapper = wrapper;
			this.memoryCache = memoryCache;
			this.converterResultMaxExpired = converterResultMaxExpired;
		}

		@Override
		public void run() {
			updateMemoryCacheForResult();
		}
		
		private void updateMemoryCacheForResult() {
			if (wrapper.getResultValue() == null) {
				return ;
			}
			String key = wrapper.getCacheKey(wrapper.getResultType());
			CacheEntry<?> data = makeCacheEntryForConverterResult(wrapper.getResultValue(), converterResultMaxExpired);
			memoryCache.put(key, data);
		}
		
	}
	
	private static class UpdateAsyncRequestConverterCacheRunnable implements Runnable {
		private final AsyncRequest wrapper;
		private final MemoryCache memoryCache;
		private final long converterResultMaxExpired;
		
		public UpdateAsyncRequestConverterCacheRunnable(AsyncRequest wrapper,
				MemoryCache memoryCache, long converterResultMaxExpired) {
			super();
			this.wrapper = wrapper;
			this.memoryCache = memoryCache;
			this.converterResultMaxExpired = converterResultMaxExpired;
		}

		@Override
		public void run() {
			updateMemoryCacheForListener();
		}
		
		private void updateMemoryCacheForListener() {
			List<ResponseListenerWrapper> rlList = wrapper.getOnResponseListeners();
			if (rlList != null && !rlList.isEmpty()) {
				for (ResponseListenerWrapper w : rlList) {
					if (w == null || !w.isShouldCacheInMemory()) {
						continue;
					}
					//根本不过期的
					String key = wrapper.getCacheKey(w.getResponseType());
					CacheEntry<?> data = makeCacheEntryForConverterResult(w.getResponseValue(), converterResultMaxExpired);
					memoryCache.put(key, data);
				}
			}
			List<LegolasListenerWrapper> llwList = wrapper.getOnLegolasListeners();
			if (llwList != null && !llwList.isEmpty()) {
				for (LegolasListenerWrapper w : llwList) {
					if (w == null || !w.isShouldCacheResponseInMemory()) {
						continue;
					}
					//根本不过期的
					String key = wrapper.getCacheKey(w.getResponseType());
					CacheEntry<?> data = makeCacheEntryForConverterResult(w.getResponseValue(), converterResultMaxExpired);
					memoryCache.put(key, data);
				}
			}
		}
	}
	
	private static class UpdateRequestCacheRunnable implements Runnable {

		private final BasicRequest wrapper;
		private final Response response;
		private final DiskCache diskCache;
		private final MemoryCache memoryCache;
		private boolean updateMemoryResponse = false;
		private boolean updateDiskResponse = false;

		public UpdateRequestCacheRunnable(BasicRequest wrapper,
				Response response, MemoryCache memoryCache, DiskCache diskCache) {
			super();
			this.wrapper = wrapper;
			this.response = response;
			this.memoryCache = memoryCache;
			this.diskCache = diskCache;
		}

		@Override
		public void run() {
			if (updateMemoryResponse) {
				updateMemoryCacheForResponse();
			}
			if (updateDiskResponse) {
				updateDiskCacheForResponse();
			}
		}

		private void updateMemoryCacheForResponse() {
			Legolas.getLog().d("updateMemoryCacheForResponse");
			if (response == null || response.isFromMemoryCache()) {
				return;
			}
			String key = wrapper.getCacheKey();
			CacheEntry<Response> entry = makeCacheEntryForResponse(response);
			if (entry != null) {
				Legolas.getLog().d("CacheEntry:" + entry.toString());
				memoryCache.put(key, entry);
			}
		}

		private void updateDiskCacheForResponse() {
			Legolas.getLog().d("updateDiskCacheForResponse");
			if (response == null || response.isFromDiskCache()) {
				return;
			}
			String key = wrapper.getCacheKey();
			CacheEntry<Response> entry = makeCacheEntryForResponse(response);
			if (entry != null) {
				Legolas.getLog().d("CacheEntry:" + entry.toString());
				diskCache.put(key, entry);
			}
		}
	}

	private static <T> CacheEntry<T> makeCacheEntryForConverterResult(T value, long maxExpired) {
		Map<String, String> responseHeaders = Collections.emptyMap();
		long softTtl = System.currentTimeMillis() + TimeUnit.MILLISECONDS.toMillis(maxExpired);
		return new CacheEntry<T>(value, responseHeaders, null, 0, 0, softTtl);
	}
	
	
	private static CacheEntry<Response> makeCacheEntryForResponse(Response response) {
		if (response == null) {
			return null;
		}
		// 只缓存2xx请求
		CacheEntry<Response> entry = null;
		if (200 <= response.getStatus() && response.getStatus() < 300) {
			
			long now = System.currentTimeMillis();

	        Map<String, String> headers = response.getHeaders();

	        long serverDate = 0;
	        long serverExpires = 0;
	        long serverModified = 0;//Last-Modified
	        long softExpire = 0;
	        long maxAge = 0;
	        boolean hasCacheControl = false;

	        String serverEtag = null;
	        String headerValue;

	        //服务器时间
	        headerValue = headers.get("Date");
	        if (headerValue != null) {
	            serverDate = parseDateAsEpoch(headerValue);
	        }

	        //服务器改动时间
	        headerValue = headers.get("Last-Modified");
	        if (headerValue != null) {
	        	serverModified = parseDateAsEpoch(headerValue);
	        }

	        //服务器的缓存控制
	        headerValue = headers.get("Cache-Control");
	        if (headerValue != null) {
	            hasCacheControl = true;
	            String[] tokens = headerValue.split(",");
	            for (int i = 0; i < tokens.length; i++) {
	                String token = tokens[i].trim();
	                if (token.equals("no-cache") || token.equals("no-store")) {
	                    return null;
	                } else if (token.startsWith("max-age=")) {
	                    try {
	                        maxAge = Long.parseLong(token.substring(8));
	                    } catch (Exception e) {
	                    }
	                } else if (token.equals("must-revalidate") || token.equals("proxy-revalidate")) {
	                    maxAge = 0;
	                }
	            }
	        }

	        headerValue = headers.get("Expires");
	        if (headerValue != null) {
	            serverExpires = parseDateAsEpoch(headerValue);
	        }

	        //ETag
	        serverEtag = headers.get("ETag");

	        // Cache-Control takes precedence over an Expires header, even if both exist and Expires
	        // is more restrictive.
	        if (hasCacheControl) {
	            softExpire = now + maxAge * 1000;
	        } else if (serverDate > 0 && serverExpires >= serverDate) {
	            // Default semantic for Expire header in HTTP specification is softExpire.
	            softExpire = now + (serverExpires - serverDate);//本地的过期的时间
	        }
	        
	        entry = new CacheEntry<Response>(response, headers, serverEtag, serverDate, serverModified, softExpire);
		}
		return entry;
	}
	
	protected static long parseDateAsEpoch(String dateValue) {
		//Legolas.getLog().d("parseDateAsEpoch:" + dateStr);
		
		if (dateValue != null 
				&&dateValue.length() > 1 
				&& dateValue.startsWith("'")
				&& dateValue.endsWith("'")) {
			dateValue = dateValue.substring(1, dateValue.length() - 1);
		}

		String RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";
		String RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
		String ASCTIME = "EEE MMM d HH:mm:ss yyyy";
		
		//Thu, 15 Jan 2015 05:52:38 GMT
		String[] patterns = new String[] { RFC1036, RFC1123, ASCTIME };
		for (String string : patterns) {
			SimpleDateFormat dateParser = new SimpleDateFormat(string, Locale.US);
			dateParser.set2DigitYearStart(DEFAULT_TWO_DIGIT_YEAR_START);
			try {
				// Parse date in RFC1123 format if this header contains one
				return dateParser.parse(dateValue).getTime();
			} catch (ParseException e) {
				// Date in invalid format, fallback to 0
			}
		}
		return 0;
	}
	
	public CacheEntry<Response> getRequestCacheEntry(BasicRequest wrapper) {
		CacheEntry<Response> cacheEntry = null;
		boolean loadMemoryCache = needLoadMemoryCacheForResponse(wrapper);
		Legolas.getLog().d("needLoadMemoryCacheForResponse [" + loadMemoryCache + "]");
		if (loadMemoryCache) {
			//如果有Response没有在缓存里边那就从内存缓存拿出Response对象
			wrapper.appendLog("MemoryCache load Response : ");
			cacheEntry = loadResponseFromMemoryCache(wrapper);
		}
		
		//内存缓存命中，并且可用
		if (cacheEntry != null) {
			wrapper.appendLog(" Hited \n");
			return cacheEntry;
		}
		
		if (loadMemoryCache) {
			wrapper.appendLog(" Losted \n");
		} else {
			wrapper.appendLog("MemoryCache no load \n");
		}
		
		boolean loadDiskCache = needLoadDiskCacheForResponse(wrapper);
		Legolas.getLog().d("needLoadDiskCacheForResponse [" + loadDiskCache + "]");
		//内存缓存没命中
		if (loadDiskCache) {
			wrapper.appendLog("DiskCache load Response : ");
			cacheEntry = loadResponseFromDiskCache(wrapper);
		}
		
		//磁盘缓存命中，并且可用
		if (cacheEntry != null) {
			wrapper.appendLog(" Hited \n");
			return cacheEntry;
		}
		if (loadDiskCache) {
			wrapper.appendLog(" Losted \n");
		} else {
			wrapper.appendLog("DiskCache no load \n");
		}
		return null;
	}
	
	public boolean getSyncRequestConverterCache(SyncRequest wrapper) {
		if (!enableCache.get()) {
			Legolas.getLog().d("Global enableCache : [false] getSyncRequestConverterCache fail");
			return false;
		}
		if (!wrapper.isCacheConverterResult()) {
			Legolas.getLog().d("CacheConverterResult : [false] getSyncRequestConverterCache fail");
			return false;
		}
		LegolasOptions options = wrapper.getOptions();
		appendLogForLoadCache(wrapper);
		
		if (options.isCacheConverterResult()) {
			wrapper.appendLog("MemoryCache load ConverterResult : ");
			if (loadResultFromMemoryCacheForSync(wrapper)) {
				//如果所有的Response都从缓存加载出来了，就直接结束
				wrapper.appendLog("sync Result Object be Cached in MemoryCache(no Converter) \n");
				return true;
			} else {
				wrapper.appendLog(" Losted \n");
			}
		}
		return false;
	}
	
	private static class GetMemoryCacheListenerCallable implements Callable<Boolean> {
		
		private final MemoryCache memoryCache;
		private final String key;
		private final ResponseListenerWrapper rlw;
		private final LegolasListenerWrapper llw;

		public GetMemoryCacheListenerCallable(MemoryCache memoryCache, String key, ResponseListenerWrapper rlw,
				LegolasListenerWrapper llw) {
			super();
			this.memoryCache = memoryCache;
			this.key = key;
			this.rlw = rlw;
			this.llw = llw;
		}

		@Override
		public Boolean call() throws Exception {
			CacheEntry<?> entry = memoryCache.get(key);
			if (entry == null || entry.getData() == null) {
				return false;
			} 
			if (entry.isExpired()) {
				return false;
			}
			setData(entry);
			return true;
		}
		
		private void setData(CacheEntry<?> entry) {
			if (rlw != null) {
				rlw.setResponseValue(entry.getData());
			}
			if (llw != null) {
				llw.setResponseValue(entry.getData());
			}
		}
	}
	
	private static class GetMemoryCacheCallable implements Callable<CacheEntry<?>> {
		private final MemoryCache memoryCache;
		private final String key;

		public GetMemoryCacheCallable(MemoryCache memoryCache, String key) {
			super();
			this.memoryCache = memoryCache;
			this.key = key;
		}

		@Override
		public CacheEntry<?> call() throws Exception {
			return memoryCache.get(key);
		}
	}
	
	private FutureTask<CacheEntry<?>> submitMemoryCacheTask(final String key) {
		GetMemoryCacheCallable memoryCacheCallable = new GetMemoryCacheCallable(memoryCache, key);
		FutureTask<CacheEntry<?>> memoryCacheTask = new FutureTask<CacheEntry<?>>(memoryCacheCallable);
		taskExecutorForCache.execute(memoryCacheTask);
		return memoryCacheTask;
	}
	
	private static class GetDiskCacheCallable implements Callable<CacheEntry<Response>> {

		private final DiskCache diskCache;
		private final String key;
		
		public GetDiskCacheCallable(DiskCache diskCache, String key) {
			super();
			this.diskCache = diskCache;
			this.key = key;
		}

		@Override
		public CacheEntry<Response> call() throws Exception {
			return diskCache.get(key);
		}
	}
	
	private FutureTask<CacheEntry<Response>> submitDiskCacheTask(final String key) {
		FutureTask<CacheEntry<Response>> diskCacheTask = null;
		GetDiskCacheCallable diskCacheCallable = new GetDiskCacheCallable(diskCache, key);
		diskCacheTask = new FutureTask<CacheEntry<Response>>(diskCacheCallable);
		taskExecutorForCache.execute(diskCacheTask);
		return diskCacheTask;
	}
	
	private boolean loadResultFromMemoryCacheForSync(SyncRequest wrapper) {
		//如果有Response没有在缓存里边那就从内存缓存拿出Response对象
		Type responseType = wrapper.getResultType();
		String key = wrapper.getCacheKey(responseType);
		FutureTask<CacheEntry<?>> task = submitMemoryCacheTask(key);
		try {
			CacheEntry<?> entry = task.get();
			if (entry == null || entry.getData() == null || entry.isExpired()) {
				return false;
			}
			wrapper.setResultValue(entry.getData());
			return true;
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		}
		return false;
	}
	
	private boolean loadResponseListenerFromMemoryCacheForAsync(AsyncRequest wrapper) {
		List<ResponseListenerWrapper> list = wrapper.getOnResponseListeners();
		AtomicInteger count = new AtomicInteger(0);
		if (list != null && !list.isEmpty()) {
			for (ResponseListenerWrapper rlw : list) {
				if (rlw == null || rlw.isShouldIgnore()) {
					continue;
				}
				String key = wrapper.getCacheKey(rlw.getResponseType());
				service.submit(new GetMemoryCacheListenerCallable(memoryCache,  key, rlw, null));
				count.incrementAndGet();
			}
		}
		
		List<LegolasListenerWrapper> llList = wrapper.getOnLegolasListeners();
		if (llList != null && !llList.isEmpty()) {
			for (LegolasListenerWrapper llw : llList) {
				if (llw == null || llw.isShouldIgnoreResponse()) {
					continue;
				}
				String key = wrapper.getCacheKey(llw.getResponseType());
				service.submit(new GetMemoryCacheListenerCallable(memoryCache,  key, null, llw));
				count.incrementAndGet();
			}
		}
		
		boolean loadAllCached = true;
		for (int i = 0; i < count.get(); i++) {
			try{
				if (!Boolean.TRUE.equals(service.take().get())) {
					return false;
				}
			} catch (InterruptedException e) {
				return false;
			} catch (ExecutionException e) {
				return false;
			}
		}
		return loadAllCached;
	}
	
	/**
	 * 加载一个可以直接使用的Response<br/>
	 * 1、加载Response不为空，就存到CacheEntryResponse
	 * 2、如果Response不为空并且策略觉得这个Responsekey 直接使用就返回，如果策略不能直接使用就返回空
	 * @param wrapper
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private CacheEntry<Response> loadResponseFromMemoryCache(BasicRequest wrapper) {
		//如果有Response没有在缓存里边那就从内存缓存拿出Response对象
		String key = wrapper.getCacheKey();
		FutureTask<CacheEntry<?>> task = submitMemoryCacheTask(key);
		try {
			CacheEntry<?> entry = task.get();
			if (entry == null || entry.getData() == null || !(entry.getData() instanceof Response)) {
				return null;
			}
			return (CacheEntry<Response>) entry;
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		}
		return null;
	}
	
	private CacheEntry<Response> loadResponseFromDiskCache(BasicRequest wrapper) {
		//如果有Response没有在缓存里边那就从内存缓存拿出Response对象
		String key = wrapper.getCacheKey();
		FutureTask<CacheEntry<Response>> task = submitDiskCacheTask(key);
		try {
			CacheEntry<Response> entry = task.get();
			if (entry == null || entry.getData() == null) {
				return null;
			}
			return entry;
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		}
		return null;
	}
	
	/**
	 * 记录使用Cache的日志
	 * @param wrapper
	 */
	private void appendLogForLoadCache(BasicRequest wrapper) {
		Legolas.getLog().d("appendLogForLoadCache");
		StringBuilder builder = new StringBuilder();
		builder.append("-------------------LoadCache>>-------------------\n");
		builder.append("CacheConverterResult:").append(wrapper.getOptions().isCacheConverterResult()).append("\n");
		builder.append("ConverterResultMaxExpired:").append(converterResultMaxExpired).append("ms\n");
		builder.append("CacheInMemory:").append(wrapper.getOptions().isCacheInMemory()).append("\n");
		builder.append("CacheInDisk:").append(wrapper.getOptions().isCacheOnDisk()).append("\n");
		builder.append("CachePolicy:").append(wrapper.getOptions().getCachePolicy()).append("\n");
		builder.append("RecoveryPolicy:").append(wrapper.getOptions().getRecoveryPolicy()).append("\n");
		wrapper.appendLog(builder.toString());
	}
	
	private void appendLogForUpdateCache(BasicRequest wrapper) {
		StringBuilder builder = new StringBuilder();
		builder.append("-------------------<<UpdateCache-------------------\n");
		builder.append("CacheConverterResult:").append(wrapper.getOptions().isCacheConverterResult()).append("\n");
		builder.append("ConverterResultMaxExpired:").append(converterResultMaxExpired).append("ms\n");
		builder.append("CacheInMemory:").append(wrapper.getOptions().isCacheInMemory()).append("\n");
		builder.append("CacheInDisk:").append(wrapper.getOptions().isCacheOnDisk()).append("\n");
		builder.append("CachePolicy:").append(wrapper.getOptions().getCachePolicy()).append("\n");
		builder.append("RecoveryPolicy:").append(wrapper.getOptions().getRecoveryPolicy()).append("\n");
		wrapper.appendLog(builder.toString());
	}

	/**
	 * 
	 * @param wrapper
	 * @return true全部从缓存读取，不需要请求网络，false没有从缓存读取到，需要请求(但是不代表没有Etag)
	 */
	public boolean getAsyncRequestConverterCache(AsyncRequest wrapper) {
		if (!enableCache.get()) {
			Legolas.getLog().d("Global enableCache : [false] getAsyncRequestConverterCache fail");
			return false;
		}
		
		appendLogForLoadCache(wrapper);
		if (wrapper.isCacheConverterResult()) {
			wrapper.appendLog("MemoryCache load All Listener Result : ");
			if (loadResponseListenerFromMemoryCacheForAsync(wrapper)) {
				//如果所有的Response都从缓存加载出来了，就直接结束
				wrapper.appendLog("async Listener Result Object be Cached in MemoryCache(no Converter) \n");
				return true;
			} else {
				wrapper.appendLog(" Losted \n");
				Legolas.getLog().d("AsyncRequest CacheConverterResult : [true] getAsyncRequestConverterCache fail");
			}
		} else {
			Legolas.getLog().d("AsyncRequest CacheConverterResult : [false] getAsyncRequestConverterCache fail");
		}
		return false;
	}

	@Override
	public void enableCache(boolean enable) {
		enableCache.set(enable);
	}
}
