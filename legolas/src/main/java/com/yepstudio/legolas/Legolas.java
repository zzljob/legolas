package com.yepstudio.legolas;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.yepstudio.legolas.description.ApiDescription;
import com.yepstudio.legolas.description.RequestDescription;
import com.yepstudio.legolas.internal.ExecutorDelivery;
import com.yepstudio.legolas.internal.RequestBuilder;
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
	
	private static Map<Object, Legolas> legolasBindMap = new WeakHashMap<Object, Legolas>(5); 
	private static Map<Object, Map<Class<?>, Object>> proxyBindMap = new WeakHashMap<Object, Map<Class<?>, Object>>(5); 
	
	private final Map<Class<?>, Endpoint> dynamicEndpoint = new ConcurrentHashMap<Class<?>, Endpoint>();
	private final Map<Class<?>, Map<String, String>> dynamicHeaders = new ConcurrentHashMap<Class<?>, Map<String, String>>();
	
	private final Endpoint defaultEndpoint;
	private final Map<String, String> defaultHeaders;
	
	private final RequestExecutor executor;
	private final RequestInterceptor interceptor;
	private final Converter converter;
	
	public Legolas(Endpoint defaultEndpoint, Map<String, String> defaultHeaders, RequestExecutor executor, RequestInterceptor interceptor, Converter converter) {
		super();
		this.defaultEndpoint = defaultEndpoint;
		this.defaultHeaders = defaultHeaders;
		this.executor = executor;
		this.interceptor = interceptor;
		this.converter = converter;
	}

	public <T> T newInstance(Class<T> clazz) {
		return newInstance(null, clazz);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T newInstance(Object bind, Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("newInstance fail, bind and class can be null.");
		}
		log.d("newInstance, bind:" + bind + ", Class:" + clazz.getName());
		ApiDescription apiDescription = LegolasConfig.getInstance().getApiDescription(clazz);
		ProxyHandler handler = new ProxyHandler(apiDescription);
		Object proxy = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, handler);
		
		if (bind != null) {
			getApiBindMap(bind).put(clazz, proxy);
		}
		
		return (T) proxy;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getInstanceByBind(Object bind, Class<T> clazz) {
		if (bind == null || clazz == null) {
			throw new IllegalArgumentException("newInstance fail, bind and class can be null.");
		}
		Object proxy = getApiBindMap(bind).get(clazz);
		try {
			if (Proxy.getInvocationHandler(proxy) == null) {
				throw new IllegalArgumentException("getInstance fail, InvocationHandler is null.");
			}
		} catch (Throwable th) {
			throw new IllegalArgumentException("getInstance fail, need newInstance before getInstance.", th);
		}
		return (T) proxy;
	}
	
	public static Legolas getBindLegolas(Object bind) {
		if (bind == null) {
			throw new IllegalArgumentException("the bind can not be null.");
		}
		return legolasBindMap.get(bind);
	}
	
	private Map<Class<?>, Object> getApiBindMap(Object bind) {
		Map<Class<?>, Object> apiMap = proxyBindMap.get(bind);
		if (apiMap == null) {
			apiMap = new ConcurrentHashMap<Class<?>, Object>(5);
			proxyBindMap.put(bind, apiMap);
		}
		return apiMap;
	}
	
	public Endpoint getEndpoint(Class<?> clazz) {
		Endpoint endpoint = dynamicEndpoint.get(clazz);
		if (endpoint == null) {
			return defaultEndpoint;
		}
		return endpoint;
	}
	
	public Map<String, String> getHeaders(Class<?> clazz) {
		Map<String, String> headers = new LinkedHashMap<String, String>();
		headers.putAll(defaultHeaders);
		if (dynamicHeaders.get(clazz) != null) {
			headers.putAll(dynamicHeaders.get(clazz));
		}
		return headers;
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
			RequestBuilder builder = new RequestBuilder(getEndpoint(clazz), getHeaders(clazz), apiDescription, description, converter);
			builder.parseArguments(args);
			interceptor.interceptor(builder);
			RequestWrapper wrapper = builder.build();
			
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

	public static class Build {
		private Endpoint endpoint;
		private Map<String, String> headers;
		private RequestExecutor executor;
		private RequestInterceptor interceptor;
		private Converter converter;
		private HttpSender httpSender;
		private ResponseDelivery delivery;
		private ResponseParser parser;
		private Profiler<?> profiler;
		
		private Object bind;
		
		public Build setBind(Object bind) {
			this.bind = bind;
			return this;
		}
		
		public Build setProfiler(Profiler<?> profiler) {
			this.profiler = profiler;
			return this;
		}
		
		public Build setResponseParser(ResponseParser parser) {
			this.parser = parser;
			return this;
		}
		
		public Build setResponseDelivery(ResponseDelivery delivery) {
			this.delivery = delivery;
			return this;
		}
		
		public Build setHttpSender(HttpSender httpSender) {
			this.httpSender = httpSender;
			return this;
		}
		
		public Build setDefaultConverter(Converter defaultConverter) {
			this.converter = defaultConverter;
			return this;
		}
		
		public Build setRequestInterceptor(RequestInterceptor interceptor) {
			this.interceptor = interceptor;
			return this;
		}
		
		public Build setRequestInterceptor(RequestExecutor executor) {
			this.executor = executor;
			return this;
		}
		
		public Build setDefaultHeaders(Map<String, String> defaultHeaders) {
			this.headers = defaultHeaders;
			return this;
		}
		
		public Build setDefaultEndpoint(Endpoint defaultEndpoint) {
			this.endpoint = defaultEndpoint;
			return this;
		}
		
		public Legolas create() {
			if (httpSender == null) {
				httpSender = Platform.get().defaultHttpSender();
			}
			if(converter == null){
				converter = Platform.get().defaultConverter();
			}
			if (parser == null) {
				parser = new SimpleResponseParser(converter);
			}
			if(interceptor == null){
				interceptor = Platform.get().defaultRequestInterceptor();
			}
			if (delivery == null) {
				delivery = new ExecutorDelivery(Platform.get().defaultDeliveryExecutor());
			}
			if (profiler == null) {
				profiler = Platform.get().defaultProfiler(); 
			}
			if (executor == null) {
				executor = new SimpleRequestExecutor(Platform.get().defaultHttpExecutor(), httpSender, delivery, parser, profiler);
			}
			Legolas legolas = new Legolas(endpoint, headers, executor, interceptor, converter);
			if (bind != null) {
				legolasBindMap.put(bind, legolas);
			}
			return legolas;
		}
		
	}

}
