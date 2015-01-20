package com.yepstudio.legolas;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import com.yepstudio.legolas.cache.disk.DiskCache;
import com.yepstudio.legolas.cache.memory.MemoryCache;
import com.yepstudio.legolas.internal.AndroidPlatform;
import com.yepstudio.legolas.internal.BasicLegolasEngine;
import com.yepstudio.legolas.internal.BasicPlatform;
import com.yepstudio.legolas.internal.ExecutorCacheDispatcher;
import com.yepstudio.legolas.internal.ExecutorProfilerDelivery;
import com.yepstudio.legolas.internal.ExecutorResponseDelivery;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2014年10月30日
 * @version 1.0，2014年10月30日
 *
 */
public class LegolasConfiguration {
	final LegolasLog log;
	
	final LegolasEngine legolasEngine;
	final HttpSender httpSender;

	final ResponseDelivery responseDelivery;
	
	final ProfilerDelivery profilerDelivery;
	
	final CacheDispatcher cacheDispatcher; 
	final MemoryCache memoryCache;
	final DiskCache diskCache;
	
	final LegolasOptions defaultLegolasOption;
	final Map<Class<?>, LegolasOptions> legolasOptionsForApi;

	final Endpoint defaultEndpoint;
	final Map<Class<?>, Endpoint> endpointsForApi;

	final Converter defaultConverter;
	final Map<Class<?>, Converter> defaultConverterForApi;

	final Map<String, String> defaultHeaders;

	final Map<String, RequestInterceptor> requestInterceptors;

	private LegolasConfiguration(Builder builder) {
		super();
		log = builder.legolasLog;
		
		legolasEngine = builder.engine;
		httpSender = builder.httpSender;

		responseDelivery = builder.responseDelivery;
		
		profilerDelivery = builder.profilerDelivery;
		
		cacheDispatcher = builder.cacheDispatcher;
		memoryCache = builder.memoryCache;
		diskCache = builder.diskCache;
		
		defaultLegolasOption = builder.defaultOptions;
		legolasOptionsForApi = builder.optionsForApi;

		defaultEndpoint = builder.defaultEndpoints;
		endpointsForApi = builder.endpointsForApi;

		defaultConverter = builder.defaultConverter;
		defaultConverterForApi = builder.converterForApi;

		defaultHeaders = builder.defaultHeaders;
		requestInterceptors = builder.requestInterceptors;
	}

	public static class Builder {
		private Platform platform;

		private LegolasLog legolasLog;

		private LegolasEngine engine;
		private HttpSender httpSender;
		private Executor taskExecutorForHttp;

		private ResponseDelivery responseDelivery;
		private Executor taskExecutorForListener;
		
		private CacheDispatcher cacheDispatcher;
		private Executor taskExecutorForCache;
		private DiskCache diskCache;
		private MemoryCache memoryCache;
		private long converterResultMaxExpired = TimeUnit.MINUTES.toMillis(10);
		
		private Executor taskExecutorForProfiler;
		private Profiler<?> profiler;
		private ProfilerDelivery profilerDelivery;
		private Boolean enableProfiler;
		
		private LegolasOptions defaultOptions;
		private Map<Class<?>, LegolasOptions> optionsForApi;

		private Endpoint defaultEndpoints;
		private Map<Class<?>, Endpoint> endpointsForApi;

		private Converter defaultConverter;
		private Map<Class<?>, Converter> converterForApi;

		private String UserAgent;
		private Map<String, String> defaultHeaders;

		private Map<String, RequestInterceptor> requestInterceptors;
		
		public Builder platform(Platform platform) {
			this.platform = platform;
			return this;
		}

		public Builder legolasLog(LegolasLog legolasLog) {
			this.legolasLog = legolasLog;
			return this;
		}

		public Builder taskExecutorForHttp(Executor taskExecutorForHttp) {
			this.taskExecutorForHttp = taskExecutorForHttp;
			return this;
		}

		public Builder taskExecutorForListener(Executor taskExecutorForListener) {
			this.taskExecutorForListener = taskExecutorForListener;
			return this;
		}

		public Builder taskExecutorForProfiler(Executor taskExecutorForProfiler) {
			this.taskExecutorForProfiler = taskExecutorForProfiler;
			return this;
		}

		public Builder profilerEnable(boolean enable) {
			this.enableProfiler = enable;
			return this;
		}
		
		public Builder profiler(Profiler<?> profiler) {
			this.profiler = profiler;
			return this;
		}

		public Builder memoryCache(MemoryCache memoryCache) {
			this.memoryCache = memoryCache;
			return this;
		}

		public Builder diskCache(DiskCache diskCache) {
			this.diskCache = diskCache;
			return this;
		}
		public Builder converterResultMaxExpired(int maxExpired, TimeUnit timeunit) {
			this.converterResultMaxExpired = timeunit.toMillis(maxExpired);
			return this;
		}

		public Builder httpSender(HttpSender httpSender) {
			this.httpSender = httpSender;
			return this;
		}

		public Builder defaultConverter(Converter converter) {
			this.defaultConverter = converter;
			return this;
		}
		
		public Builder defaultOptions(LegolasOptions defaultOptions) {
			this.defaultOptions = defaultOptions;
			return this;
		}

		public Builder defaultEndpoints(Endpoint endpoints) {
			this.defaultEndpoints = endpoints;
			return this;
		}
		
		public Builder requestApiConverter(Class<?> apiClass, Converter converter) {
			if (converterForApi == null) {
				converterForApi = new HashMap<Class<?>, Converter>();
			}
			this.converterForApi.put(apiClass, converter);
			return this;
		}
		
		public Builder requestApiConverter(Converter converter, Set<Class<?>> apiClassSets) {
			if (converterForApi == null) {
				converterForApi = new HashMap<Class<?>, Converter>();
			}
			if (apiClassSets != null) {
				for (Class<?> apiClass : apiClassSets) {
					converterForApi.put(apiClass, converter);
				}
			}
			return this;
		}
		
		public Builder requestApiOptions(Class<?> apiClass, LegolasOptions options) {
			if (optionsForApi == null) {
				optionsForApi = new HashMap<Class<?>, LegolasOptions>();
			}
			optionsForApi.put(apiClass, options);
			return this;
		}
		
		public Builder requestApiOptions(LegolasOptions options, Set<Class<?>> apiClassSets) {
			if (optionsForApi == null) {
				optionsForApi = new HashMap<Class<?>, LegolasOptions>();
			}
			if (apiClassSets != null) {
				for (Class<?> apiClass : apiClassSets) {
					optionsForApi.put(apiClass, options);
				}
			}
			return this;
		}

		public Builder requestApiEndpoints(Class<?> apiClass, Endpoint endpoints) {
			if (endpointsForApi == null) {
				endpointsForApi = new HashMap<Class<?>, Endpoint>();
			}
			endpointsForApi.put(apiClass, endpoints);
			return this;
		}

		public Builder requestApiEndpoints(Endpoint endpoints, Set<Class<?>> apiClassSets) {
			if (endpointsForApi == null) {
				endpointsForApi = new HashMap<Class<?>, Endpoint>();
			}
			if (apiClassSets != null) {
				for (Class<?> apiClass : apiClassSets) {
					endpointsForApi.put(apiClass, endpoints);
				}
			}
			return this;
		}

		public Builder requestHeader(String name, String value) {
			if (defaultHeaders == null) {
				defaultHeaders = defaultHeaders();
			}
			defaultHeaders.put(name, value);
			return this;
		}
		
		public Builder registerRequestInterceptors(String alias, RequestInterceptor interceptor) {
			if (requestInterceptors == null) {
				requestInterceptors = new HashMap<String, RequestInterceptor>();
			}
			requestInterceptors.put(alias, interceptor);
			return this;
		}

		public LegolasConfiguration build() {
			if (platform == null) {
				platform = defaultPlatform();
			}
			
			if (legolasLog == null) {
				legolasLog = platform.defaultLog();
			}
			
			if (UserAgent == null) {
				//默认UserAgent
				StringBuilder ua = new StringBuilder();
				ua.append(Legolas.LOG_TAG).append("_");
				ua.append(Legolas.getVersion());
				this.UserAgent = ua.toString();
			}
			if (defaultHeaders == null) {
				defaultHeaders = defaultHeaders();
			}
			
			// Cache
			if (taskExecutorForCache == null) {
				taskExecutorForCache = platform.defaultTaskExecutorForCache();
			}
			if (memoryCache == null) {
				memoryCache = platform.defaultMemoryCache();
			}
			if (diskCache == null) {
				diskCache = platform.defaultDiskCache();
			}
			diskCache.initialize();
			cacheDispatcher = new ExecutorCacheDispatcher(taskExecutorForCache, memoryCache, diskCache, converterResultMaxExpired);

			// Listener
			if (taskExecutorForListener == null) {
				taskExecutorForListener = platform.defaultTaskExecutorForListener();
			}
			responseDelivery = new ExecutorResponseDelivery(taskExecutorForListener);
			
			//Profiler
			if (enableProfiler == null) {
				enableProfiler = true;
			}
			if (profiler == null) {
				profiler = platform.defaultProfiler();
			}
			if (taskExecutorForProfiler == null) {
				taskExecutorForProfiler = platform.defaultTaskExecutorForProfiler();
			}
			if (profilerDelivery == null) {
				profilerDelivery = new ExecutorProfilerDelivery(taskExecutorForProfiler, profiler);
				profilerDelivery.enableProfiler(enableProfiler);
			}
			
			//Converter
			if (defaultConverter == null) {
				defaultConverter = platform.defaultConverter();
			}
			
			if (converterForApi == null) {
				converterForApi = null;
			}
			
			if (requestInterceptors == null) {
				requestInterceptors = null;
			}
			
			//设置一个默认的LegolasOptions
			if (defaultOptions == null) {
				defaultOptions = new LegolasOptions.Builder().build();
			}
			
			//一个默认的服务端入口是必须的
			if (defaultEndpoints == null && endpointsForApi == null) {
				throw new IllegalArgumentException("must set defaultEndpoints or defaultEndpointsForApi when build LegolasConfiguration");
			}
			
			//Http
			if (taskExecutorForHttp == null) {
				taskExecutorForHttp = platform.defaultTaskExecutorForHttp();
			}
			if (httpSender == null) {
				httpSender = platform.defaultHttpSender();
			}
			if(engine == null) {
				engine = new BasicLegolasEngine(taskExecutorForHttp, httpSender, cacheDispatcher, responseDelivery, profilerDelivery);
			}
			return new LegolasConfiguration(this);
		}
		
		private Map<String, String> defaultHeaders() {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Accept-Encoding", "gzip,deflate");
			headers.put("Cache-Control", "max-age=0");
			headers.put("User-Agent", UserAgent);
			return headers;
		}
		
		private Platform defaultPlatform() {
			try {
				Class.forName("android.os.Build");
				if (android.os.Build.VERSION.SDK_INT != 0) {
					return new AndroidPlatform();
				}
			} catch (Throwable th) {
				
			}
			return new BasicPlatform();
		}

	}

	public RequestInterceptor getRequestInterceptor(String alias) {
		if (alias == null || "".equals(alias.trim()) || requestInterceptors == null) {
			return null;
		}
		return requestInterceptors.get(alias);
	}

	public Endpoint getApiEndpoint(Class<?> apiClass) {
		if (endpointsForApi == null || apiClass == null) {
			return null;
		}
		return endpointsForApi.get(apiClass);
	}

	public Endpoint getDefaultEndpoint() {
		return defaultEndpoint;
	}

	public LegolasOptions getApiLegolasOptions(Class<?> apiClass) {
		if (legolasOptionsForApi == null || apiClass == null) {
			return null;
		}
		return legolasOptionsForApi.get(apiClass);
	}

	public LegolasOptions getDefaultLegolasOptions() {
		return defaultLegolasOption;
	}

	public MemoryCache getMemoryCache() {
		return memoryCache;
	}

	public DiskCache getDiskCache() {
		return diskCache;
	}

	public Map<String, String> getDefaultHeaders() {
		return defaultHeaders;
	}

	public LegolasLog getLog() {
		return log;
	}

	public Converter getDefaultConverter() {
		return defaultConverter;
	}

	public Converter getApiConverter(Class<?> clazz) {
		if (defaultConverterForApi == null || clazz == null) {
			return null;
		}
		return defaultConverterForApi.get(clazz);
	}

}
