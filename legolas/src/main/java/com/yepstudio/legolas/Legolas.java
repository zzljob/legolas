package com.yepstudio.legolas;

import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import com.yepstudio.legolas.description.ApiDescription;
import com.yepstudio.legolas.description.RequestDescription;
import com.yepstudio.legolas.internal.ExecutorResponseDelivery;
import com.yepstudio.legolas.internal.RequestBuilder;
import com.yepstudio.legolas.internal.SimpleProfilerDelivery;
import com.yepstudio.legolas.internal.SimpleRequestExecutor;
import com.yepstudio.legolas.internal.SimpleResponseParser;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.request.RequestWrapper;

/**
 * 莱戈拉斯<br/>
 * 幽暗密林的精灵王瑟兰督伊之子<br/>
 * 魔戒远征队的成员之一<br/>
 * 优秀的精灵弓箭手<br/>
 * 
 * @author zhangzl@fund123.cn
 * @create 2013年12月24日
 * @version 2.0，2014年4月23日
 */
public class Legolas {
	
	private static LegolasLog log = LegolasLog.getClazz(Legolas.class);
	
	private static Map<Class<?>, SoftReference<ApiDescription>> apiDescriptionCache = new ConcurrentHashMap<Class<?>, SoftReference<ApiDescription>>();
	
	private static Map<Object, Legolas> legolasBindMap = new WeakHashMap<Object, Legolas>(5); 
	private static Map<Object, Map<Class<?>, Object>> proxyBindMap = new WeakHashMap<Object, Map<Class<?>, Object>>(5); 
	
	private Map<Class<?>, Endpoint> dynamicEndpoint;
	private Map<Class<?>, Map<String, Object>> dynamicHeaders;
	
	private final Endpoint defaultEndpoint;
	private final Map<String, Object> defaultHeaders;
	private final Converter defaultConverter;
	private final RequestExecutor executor;
	
	public Legolas(Endpoint defaultEndpoint, Map<String, Object> defaultHeaders, RequestExecutor executor, Converter converter) {
		super();
		this.defaultEndpoint = defaultEndpoint;
		this.defaultHeaders = defaultHeaders;
		this.executor = executor;
		this.defaultConverter = converter;
	}

	/**
	 * new一个Api的请求实例
	 * @param clazz 带有@Api注释的接口
	 * @return JDK动态代理类
	 */
	public <T> T newInstance(Class<T> clazz) {
		return newInstance(null, clazz);
	}
	
	/**
	 * new一个Api的请求实例，并且把它绑定到bind 对象上
	 * <ul>
	 * <li>对于绑定的对象可以通过{@link com.yepstudio.legolas.Legolas#getInstanceByBind(Object, Class)} 方法获取到</li>
	 * <li>不需要考虑垃圾回收的问题，当绑定的bind对象被回收，那个该次new的对象也会被回收</li>
	 * </ul>
	 * @param bind 绑定的对象
	 * @param clazz 带有@Api注释的接口
	 * @return JDK动态代理类
	 */
	@SuppressWarnings("unchecked")
	public <T> T newInstance(Object bind, Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("newInstance fail, bind and class can be null.");
		}
		log.d("newInstance, bind:" + bind + ", Class:" + clazz.getName());
		ApiDescription apiDescription = getApiDescription(clazz);
		ProxyHandler handler = new ProxyHandler(apiDescription);
		Object proxy = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, handler);
		
		if (bind != null) {
			getApiBindMap(bind).put(clazz, proxy);
		}
		
		return (T) proxy;
	}
	
	/**
	 * 获取一个已经绑定过的Api对象
	 * @param bind 绑定的对象
	 * @param clazz 带有@Api注释的接口
	 * @return 返回一个JDK动态代理类
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	public <T> T getInstanceByBind(Object bind, Class<T> clazz) {
		if (bind == null || clazz == null) {
			throw new IllegalArgumentException("getInstanceByBind fail, bind and class can be null.");
		}
		Object proxy = getApiBindMap(bind).get(clazz);
		try {
			if (Proxy.getInvocationHandler(proxy) == null) {
				throw new IllegalArgumentException("getInstanceByBind fail, InvocationHandler is null.");
			}
		} catch (Throwable th) {
			throw new IllegalArgumentException("getInstanceByBind fail, need newInstance before getInstanceByBind.", th);
		}
		return (T) proxy;
	}
	
	private Map<Class<?>, Endpoint> getOrNewDynamicEndpointMap() {
		if (dynamicEndpoint == null) {
			dynamicEndpoint = new ConcurrentHashMap<Class<?>, Endpoint>(5);
		}
		return dynamicEndpoint;
	}
	
	/**
	 * 针对某个具体的API设置默认的{@link com.yepstudio.legolas.Endpoint}<br/>
	 * 对于不同的API可能域名本身就不一样
	 * @param clazz
	 * @param endpoint
	 */
	public void setEndpoint(Class<?> clazz, Endpoint defaultEndpoint) {
		getOrNewDynamicEndpointMap().put(clazz, defaultEndpoint);
	}
	
	/**
	 * 获取一个Api接口的{@link com.yepstudio.legolas.Endpoint}
	 * @param clazz
	 * @return
	 */
	public Endpoint getEndpoint(Class<?> clazz) {
		log.d("getEndpoint for API : [" + clazz + "]");
		if (dynamicEndpoint == null) {
			return defaultEndpoint;
		}
		Endpoint endpoint = dynamicEndpoint.get(clazz);
		if (endpoint == null) {
			return defaultEndpoint;
		}
		log.v("has set dynamicEndpoint for " + clazz + ", use it");
		return endpoint;
	}
	
	private Map<Class<?>, Map<String, Object>> getOrNewDynamicHeadersMap() {
		if (dynamicHeaders == null) {
			dynamicHeaders = new ConcurrentHashMap<Class<?>, Map<String, Object>>(5);
		}
		return dynamicHeaders;
	}
	
	/**
	 * 针对某个具体的API设置默认的defaultHeaders<br/>
	 * @param clazz
	 * @param defaultHeaders
	 */
	public void setHeaders(Class<?> clazz, Map<String, Object> defaultHeaders) {
		getOrNewDynamicHeadersMap().put(clazz, defaultHeaders);
	}
	
	/**
	 * 获取一个Api接口的默认的Header<br/>
	 * 该Header的是根据Key来设置的，如果key多次设置，前面设置的将会被后面的覆盖<br/>
	 * 该Header的覆盖优先级：<br/>
	 * <ol>
	 * <li> API类的{@link com.yepstudio.legolas.annotation.Headers }标签</li>
	 * <li> API的方法的{@link com.yepstudio.legolas.annotation.Headers }标签</li>
	 * <li> Legolas初始化是设置的defaultHeaders {@link com.yepstudio.legolas.Legolas.Build#setDefaultHeaders(Map) } </li>
	 * <li> Legolas针对API设置的dynamicHeaders  {@link com.yepstudio.legolas.Legolas#setHeaders(Class, Map) } </li>
	 * <li> API的方法的{@link com.yepstudio.legolas.annotation.Header }标签 </li>
	 * <li> 通过{@link com.yepstudio.legolas.RequestInterceptor#interceptor(RequestInterceptorFace) }设置的Header </li>
	 * </ul>
	 *  
	 * @param clazz
	 * @return
	 */
	public Map<String, Object> getHeaders(Class<?> clazz) {
		Map<String, Object> headers = new LinkedHashMap<String, Object>();
		if (defaultHeaders != null) {
			headers.putAll(defaultHeaders);
		}
		if (dynamicHeaders != null && dynamicHeaders.get(clazz) != null) {
			headers.putAll(dynamicHeaders.get(clazz));
		}
		return headers;
	}
	
	/**
	 * 获取绑定到bind对象上的Legolas对象<br/>
	 * {@link com.yepstudio.legolas.Legolas.Build#setBind(Object)}
	 * @param bind
	 * @return
	 */
	public static Legolas getBindLegolas(Object bind) {
		if (bind == null) {
			throw new IllegalArgumentException("the bind can not be null.");
		}
		return legolasBindMap.get(bind);
	}
	
	protected static ApiDescription getApiDescription(Class<?> clazz) {
		SoftReference<ApiDescription> apiRef = apiDescriptionCache.get(clazz);
		ApiDescription api;
		if (apiRef == null || apiRef.get() == null) {
			long birthTime = System.currentTimeMillis();
			api = new ApiDescription(clazz);
			apiDescriptionCache.put(clazz, new SoftReference<ApiDescription>(api));
			long finishTime = System.currentTimeMillis();
			log.d("ApiDescription be init : [" + (finishTime - birthTime) + "ms]");
		} else {
			api = apiRef.get();
			log.v("this api class is be cache, so sikp parse api.");
		}
		return api;
	}
	
	private static Map<Class<?>, Object> getApiBindMap(Object bind) {
		Map<Class<?>, Object> apiMap = proxyBindMap.get(bind);
		if (apiMap == null) {
			apiMap = new ConcurrentHashMap<Class<?>, Object>(5);
			proxyBindMap.put(bind, apiMap);
		}
		return apiMap;
	}
	
	private class ProxyHandler implements InvocationHandler {
		
		private final ApiDescription apiDescription;
		
		private ProxyHandler(ApiDescription apiDescription){
			this.apiDescription = apiDescription;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			RequestDescription description = apiDescription.getRequestDescription(method);
			//如果没有找到该方法的请求描述RequestDescription 则直接返回类型的默认值
			if(description == null || !description.isHttpRequest()) {
				log.e("have not annotation whit @Http, is not http Request.");
				if (Platform.get().isDebug()) {
					throw new IllegalArgumentException("this method have not @GET @POST and so on, is not http Request.");
				} else {
					return null;
				}
			}
			
			Class<?> clazz = apiDescription.getApiClazz();
			RequestBuilder builder = new RequestBuilder(getEndpoint(clazz), getHeaders(clazz), apiDescription, description, defaultConverter);
			RequestWrapper wrapper = null;
			try {
				builder.parseArguments(args);
				
				List<RequestInterceptor> interceptors;
				if (description.isExpansionInterceptors()) {
					interceptors = apiDescription.getInterceptors();
					if (interceptors != null && interceptors.size() > 0) {
						for (RequestInterceptor requestInterceptor : interceptors) {
							requestInterceptor.interceptor(builder);
						}
					}
				}
				
				interceptors = description.getInterceptors();
				if (interceptors != null && interceptors.size() > 0) {
					for (RequestInterceptor requestInterceptor : interceptors) {
						requestInterceptor.interceptor(builder);
					}
				}
				
				wrapper = builder.build();
			} catch (Throwable th) {
				throw new IllegalArgumentException("build request has error before request", th);
			}
			if (description.isSynchronous()) {
				return executor.syncRequest(wrapper);
			}
			
			executor.asyncRequest(wrapper);
			if (Request.class == description.getResponseType()) {
				return wrapper.getRequest();
			}
			return null;
		}
	}

	/**
	 * 建构Legolas对象
	 * @author zzljob@gmail.com
	 * @create 2014年5月6日
	 * @version 2.0, 2014年5月6日
	 *
	 */
	public static class Build {
		private Endpoint endpoint;
		private Map<String, Object> headers;
		
		private RequestExecutor requestExecutor;
		private Cache cache;
		
		private Converter converter;
		
		private ExecutorService httpSenderExecutor;
		private HttpSender httpSender;
		private ResponseDelivery delivery;
		private ResponseParser parser;
		
		private ProfilerDelivery profilerDelivery;
		private Profiler<?> profiler;
		
		private Object bind;
		
		/**
		 * 设置Legolas的绑定对象，如果绑定对象被回收，Legolas对象也会在不使用后被回收
		 * @param bind
		 * @return
		 */
		public Build setBind(Object bind) {
			this.bind = bind;
			return this;
		}
		
		/**
		 * 设置分析器，如果不设置，将由{@link com.yepstudio.legolas.Platform#defaultProfiler()}提供默认分析器
		 * @param profiler
		 * @return
		 */
		public Build setProfiler(Profiler<?> profiler) {
			this.profiler = profiler;
			return this;
		}
		
		public Build setProfilerDelivery(ProfilerDelivery profilerDelivery) {
			this.profilerDelivery = profilerDelivery;
			return this;
		}
		
		/**
		 * 设置
		 * @param parser
		 * @return
		 */
		public Build setResponseParser(ResponseParser parser) {
			this.parser = parser;
			return this;
		}
		
		public Build setResponseDelivery(ResponseDelivery delivery) {
			this.delivery = delivery;
			return this;
		}
		
		public Build setHttpSenderExecutor(ExecutorService httpSenderExecutor){
			this.httpSenderExecutor = httpSenderExecutor;
			return this;
		}
		
		public Build setCache(Cache cache) {
			this.cache = cache;
			return this;
		}
		
		public Build setHttpSender(HttpSender httpSender) {
			this.httpSender = httpSender;
			return this;
		}
		
		public Build setRequestExecutor(RequestExecutor executor) {
			this.requestExecutor = executor;
			return this;
		}
		
		public Build setDefaultHeaders(Map<String, Object> defaultHeaders) {
			this.headers = defaultHeaders;
			return this;
		}
		
		/**
		 * 针对所有的Api的默认的服务端<br/>
		 * 当然你也可以通过
		 * @param defaultEndpoint
		 * @return
		 */
		public Build setDefaultEndpoint(Endpoint defaultEndpoint) {
			this.endpoint = defaultEndpoint;
			return this;
		}
		
		public Build setDefaultConverter(Converter defaultConverter) {
			this.converter = defaultConverter;
			return this;
		}
		
		public Legolas create() {
			if (httpSender == null) {
				httpSender = Platform.get().defaultHttpSender();
			}
			if (httpSenderExecutor == null) {
				httpSenderExecutor = Platform.get().defaultHttpExecutor();
			}
			if (converter == null) {
				converter = Platform.get().defaultConverter();
			}
			if (parser == null) {
				parser = new SimpleResponseParser();
			}
			if (delivery == null) {
				delivery = new ExecutorResponseDelivery(Platform.get().defaultResponseDeliveryExecutor());
			}
			if (profiler == null) {
				profiler = Platform.get().defaultProfiler();
			}
			if (profilerDelivery == null) {
				profilerDelivery = new SimpleProfilerDelivery(profiler);
			}
			if (cache == null) {
				cache = Platform.get().defaultCache();
			}
			if (requestExecutor == null) {
				requestExecutor = new SimpleRequestExecutor(httpSenderExecutor, httpSender, delivery, parser, profilerDelivery, cache);
			}
			Legolas legolas = new Legolas(endpoint, headers, requestExecutor, converter);
			if (bind != null) {
				legolasBindMap.put(bind, legolas);
			}
			return legolas;
		}
	}
}
