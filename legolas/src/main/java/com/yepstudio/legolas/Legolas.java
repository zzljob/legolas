package com.yepstudio.legolas;

import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yepstudio.legolas.cache.disk.DiskCache;
import com.yepstudio.legolas.cache.memory.MemoryCache;
import com.yepstudio.legolas.description.ApiDescription;
import com.yepstudio.legolas.description.RequestDescription;
import com.yepstudio.legolas.internal.NoneLog;
import com.yepstudio.legolas.internal.RequestBuilder;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.request.AsyncRequest;

/**
 * 莱戈拉斯<br/>
 * 幽暗密林的精灵王瑟兰督伊之子<br/>
 * 魔戒远征队的成员之一<br/>
 * 优秀的精灵弓箭手<br/>
 * 身材轻盈，且射箭精准
 * 
 * @author zhangzl@fund123.cn
 * @create 2013年12月24日
 * @version 2.0，2014年4月23日
 */
public class Legolas {
	
	public static final String LOG_TAG = "Legolas";
	private static final String version = "2.0.0";
	
	private static Legolas instance;
	
	private Map<Class<?>, SoftReference<Object>> proxyCache = new ConcurrentHashMap<Class<?>, SoftReference<Object>>();
	
	private boolean inited = false;
	private LegolasConfiguration configuration;
	
	private Legolas() {
		super();
	}
	
	public static Legolas getInstance() {
		if (instance == null) {
			synchronized (Legolas.class) {
				if (instance == null) {
					instance = new Legolas();
				}
			}
		}
		return instance;
	}
	
	public static LegolasLog getLog() {
		Legolas legolas = getInstance();
		if (legolas.configuration == null || legolas.configuration.log == null) {
			return new NoneLog();
		}
		return getInstance().configuration.log;
	}
	
	public static String getVersion() {
		return version;
	}
	
	public synchronized void init(LegolasConfiguration configuration) {
		inited = true;
		this.configuration = configuration;
	}
	
	public boolean isInited() {
		return inited;
	}
	
	private void checkConfiguration() {
		if (configuration == null) {
			throw new IllegalStateException("Legolas must be init with configuration before using");
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T> T getApi(Class<T> apiClass) {
		checkConfiguration();
		if (apiClass == null) {
			throw new IllegalArgumentException("getApi fail, class can not be null. it is must be a interface.");
		}

		Object proxy;
		if (proxyCache.get(apiClass) != null) {
			proxy = proxyCache.get(apiClass).get();
			try {
				if (Proxy.isProxyClass(proxy.getClass())) {
					return (T) proxy;
				}
			} catch (Throwable th) {
			}
		}

		long birthTime = System.currentTimeMillis();
		ApiDescription apiDescription = new ApiDescription(apiClass, configuration);
		long finishTime = System.currentTimeMillis();
		getLog().d("ApiDescription be init : [" + (finishTime - birthTime) + "ms]");
		ProxyHandler handler = new ProxyHandler(apiClass, apiDescription);

		proxy = Proxy.newProxyInstance(apiClass.getClassLoader(), new Class[] { apiClass }, handler);
		return (T) proxy;
	}
	
	/**
	 * new一个Api的请求实例
	 * @deprecated 请使用 {@link #getApi(Class)}
	 * @param clazz 带有@Api注释的接口
	 * @return JDK动态代理类
	 */
	@Deprecated
	public <T> T newInstance(Class<T> clazz) {
		return newInstance(null, clazz);
	}
	
	/**
	 * new一个Api的请求实例，并且把它绑定到bind 对象上
	 * @deprecated  请使用 {@link #getApi(Class)}
	 * <ul>
	 * <li>对于绑定的对象可以通过{@link com.yepstudio.legolas.Legolas#getInstanceByBind(Object, Class)} 方法获取到</li>
	 * <li>不需要考虑垃圾回收的问题，当绑定的bind对象被回收，那个该次new的对象也会被回收</li>
	 * </ul>
	 * @param bind 绑定的对象
	 * @param clazz 带有@Api注释的接口
	 * @return JDK动态代理类
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public <T> T newInstance(Object bind, Class<T> clazz) {
		return getApi(clazz);
	}
	
	@Deprecated
	public <T> T getInstanceByBindOrNew(Object bind, Class<T> clazz) {
		try {
			return getInstanceByBind(bind, clazz);
		} catch (Throwable e) {
			return newInstance(bind, clazz);
		}
	}
	
	/**
	 * 获取一个已经绑定过的Api对象
	 * @param bind 绑定的对象
	 * @param clazz 带有@Api注释的接口
	 * @return 返回一个JDK动态代理类
	 * @throws IllegalArgumentException
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public <T> T getInstanceByBind(Object bind, Class<T> clazz) {
		return getApi(clazz);
	}
	
	/**
	 * 获取绑定到bind对象上的Legolas对象<br/>
	 * @deprecated 请使用 {@link #getInstance()}
	 * @param bind
	 * @return
	 */
	@Deprecated
	public static Legolas getBindLegolas(Object bind) {
		return getInstance();
	}
	
	public MemoryCache getMemoryCache() {
		checkConfiguration();
		return configuration.memoryCache;
	}

	/**
	 * Clears memory cache
	 *
	 * @throws IllegalStateException if {@link #init(LegolasConfiguration)} method wasn't called before
	 */
	public void clearMemoryCache() {
		checkConfiguration();
		configuration.memoryCache.clear();
	}
	
	public void enableProfiler(boolean enable) {
		checkConfiguration();
		configuration.profilerDelivery.enableProfiler(enable);
	}

	/**
	 * Returns disk cache
	 *
	 * @throws IllegalStateException if {@link #init(LegolasConfiguration)} method wasn't called before
	 */
	public DiskCache getDiskCache() {
		checkConfiguration();
		return configuration.diskCache;
	}

	/**
	 * Clears disk cache.
	 *
	 * @throws IllegalStateException if {@link #init(LegolasConfiguration)} method wasn't called before
	 */
	public void clearDiskCache() {
		checkConfiguration();
		configuration.diskCache.clear();
	}
	
	public void denyCache(boolean denyCache) {
		checkConfiguration();
		configuration.legolasEngine.denyCache(denyCache);
	}
	
	public void pause() {
		//engine.pause();
	}

	/** Resumes waiting "load&display" tasks */
	public void resume() {
		//engine.resume();
	}

	/**
	 * Cancels all running and scheduled display image tasks.<br />
	 * <b>NOTE:</b> This method doesn't shutdown
	 * {@linkplain com.nostra13.universalimageloader.core.ImageLoaderConfiguration.Builder#taskExecutor(java.util.concurrent.Executor)
	 * custom task executors} if you set them.<br />
	 * ImageLoader still can be used after calling this method.
	 */
	public void stop() {
		//engine.stop();
	}
	
	public void destroy() {
		if (configuration != null) {
			configuration.log.i("Destroy Legolas");
		};
		stop();
		//configuration.diskCache.close();
		configuration = null;
	}
	
	private class ProxyHandler implements InvocationHandler {
		
		private final ApiDescription apiDescription;
		private final Class<?> apiClass;
		
		private ProxyHandler(Class<?> apiClass, ApiDescription apiDescription){
			this.apiDescription = apiDescription;
			this.apiClass = apiClass;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method == null) {
				throw new IllegalArgumentException("method can be null");
			}
			RequestDescription requestDescription = apiDescription.getRequestDescription(method);
			//如果没有找到该方法的请求描述RequestDescription 则直接返回类型的默认值
			if(requestDescription == null || requestDescription.isIgnore()) {
				Legolas.getLog().d("this Request is Ignore.");
				return null;
			}
			Endpoint endpoint = configuration.getApiEndpoint(apiClass);
			if (endpoint == null) {
				endpoint = configuration.getDefaultEndpoint();
			}
			LegolasOptions options = configuration.getApiLegolasOptions(apiClass);
			if (options == null) {
				options = configuration.getDefaultLegolasOptions();
			}
			Map<String, String> defaultHeaders = configuration.getDefaultHeaders();
			
			Converter converter = configuration.getApiConverter(apiClass);
			if (converter == null) {
				converter = configuration.getDefaultConverter();
			}
			RequestBuilder builder = null;
			try {
				builder = new RequestBuilder(endpoint, apiDescription, method, converter, options, defaultHeaders, args);
			} catch (Throwable th) {
				throw new IllegalArgumentException("Argument can be parse", th);
			}
			
			try {
				List<RequestInterceptor> interceptors;
				
				//处理Api的拦截器
				if (requestDescription.isExpansionInterceptors()) {
					interceptors = apiDescription.getInterceptors();
					if (interceptors != null && interceptors.size() > 0) {
						for (RequestInterceptor requestInterceptor : interceptors) {
							requestInterceptor.interceptor(builder);
						}
					}
				}
				
				//处理Request的拦截器
				interceptors = requestDescription.getInterceptors();
				if (interceptors != null && interceptors.size() > 0) {
					for (RequestInterceptor requestInterceptor : interceptors) {
						requestInterceptor.interceptor(builder);
					}
				}
				
			} catch (Throwable th) {
				throw new IllegalArgumentException("build request has error before request, Interceptor has Exception", th);
			}
			
			if (requestDescription.isSynchronous()) {
				return configuration.legolasEngine.syncRequest(builder.buildSyncRequest());
			}
			
			AsyncRequest request  = builder.buildAsyncRequest();
			configuration.legolasEngine.asyncRequest(request);
			if (Request.class == requestDescription.getResultType()) {
				return request;
			}
			return null;
		}
	}
}
