package com.yepstudio.legolas.description;

import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.annotation.Api;
import com.yepstudio.legolas.annotation.Description;
import com.yepstudio.legolas.annotation.Headers;

/**
 * 每个带有@Api注释的类的描述类，将一些数据缓存下来，省得每次都要调用反射
 * @author zzljob@gmail.com
 * @create 2013年12月27日
 * @version 2.0，2014年4月23日
 */
public class ApiDescription {
	
	private static LegolasLog log = LegolasLog.getClazz(ApiDescription.class);

	private final Class<?> apiClazz;
	
	private String description;
	private String apiPath;
	private final Map<String, String> headers;
	private final Map<Method, SoftReference<RequestDescription>> requestDescriptionCache;

	public ApiDescription(Class<?> clazz) {
		super();
		this.apiClazz = clazz;
		headers = new ConcurrentHashMap<String, String>();
		requestDescriptionCache = new ConcurrentHashMap<Method, SoftReference<RequestDescription>>();
		parseApi(clazz);
	}
	
	private static <T> void validateServiceClass(Class<T> service) {
		if (!service.isInterface()) {
			throw new IllegalArgumentException("Only interface endpoint definitions are supported.");
		}
		// Prevent API interfaces from extending other interfaces. This not only avoids a bug in
		// Android (http://b.android.com/58753) but it forces composition of API
		// declarations which is the recommended pattern.
		if (service.getInterfaces().length > 0) {
			throw new IllegalArgumentException("Interface definitions must not extend other interfaces.");
		}
		log.d("validateServiceClass:[" + service.getName() + "] success.");
	}

	private synchronized void parseApi(Class<?> service) {
		log.v("parseApi:" + service.getName());
		validateServiceClass(service);
		
		Annotation[] annotations = service.getAnnotations();
		boolean hasApi = false;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (annotation.annotationType() == Api.class) {
					hasApi = true;
					apiPath = ((Api) annotation).value();
				} else if (annotation.annotationType() == Headers.class) {
					ParseHelper.parseHeaders(headers, (Headers) annotation);
				} else if (annotation.annotationType() == Description.class) {
					description = ((Description) annotation).value();
				}
			}
		}
		if (!hasApi) {
			throw new IllegalArgumentException("interface have not @Api annotation.");
		}
		
		Method[] methods = service.getMethods();
		if (methods != null) {
			for (Method method : methods) {
				parseRequest(method);
			}
		}
	}
	
	private synchronized RequestDescription parseRequest(Method method) {
		if (method == null) {
			return null;
		}
		SoftReference<RequestDescription> requestRef = requestDescriptionCache.get(method);
		RequestDescription result = null;
		if (requestRef == null || requestRef.get() == null) {
			long birthTime = System.currentTimeMillis();
			RequestDescription request = new RequestDescription(method);
			if (request.isHttpRequest()) {
				requestDescriptionCache.put(method, new SoftReference<RequestDescription>(request));
				result = request;
			}
			long finishTime = System.currentTimeMillis();
			log.d("RequestDescription be init : [" + (finishTime - birthTime) + "ms]");
		} else {
			result = requestRef.get();
			log.v("this Request Method is be cache, so sikp parse Request.");
		}
		return result;
	}
	
	public RequestDescription getRequestDescription(Method method) {
		return parseRequest(method);
	}

	public Class<?> getApiClazz() {
		return apiClazz;
	}

	public String getDescription() {
		return description;
	}

	public String getApiPath() {
		return apiPath;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}
	
}
