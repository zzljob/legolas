package com.yepstudio.android.legolas.description;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yepstudio.android.legolas.annotation.Api;
import com.yepstudio.android.legolas.annotation.Headers;
import com.yepstudio.android.legolas.log.LegolasLog;

/**
 * 每个带有@Api注释的类的描述类，将一些数据缓存下来，省得每次都要调用反射
 * @author zzljob@gmail.com
 * @createDate 2013年12月27日
 */
public class ApiDescription {
	
	private static LegolasLog log = LegolasLog.getClazz(ApiDescription.class);

	private final Class<?> apiClazz;
	
	private String label;
	private String auth;
	private final List<String> servers;
	private final Map<String, String> headers;
	private final Map<Method, RequestDescription> requestMap;

	public ApiDescription(Class<?> clazz) {
		super();
		this.apiClazz = clazz;
		servers = new ArrayList<String>();
		headers = new ConcurrentHashMap<String, String>();
		requestMap = new ConcurrentHashMap<Method, RequestDescription>();
		
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
		log.v("validateServiceClass:" + service.getName() + " success.");
	}

	private void parseApi(Class<?> service) {
		log.v("parseApi:" + service.getName());
		validateServiceClass(service);
		
		Annotation[] annotations = service.getAnnotations();
		boolean hasApi = false;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (annotation.annotationType() == Api.class) {
					hasApi = true;
					parseAnnotationOfApi((Api) annotation);
				} else if (annotation.annotationType() == Headers.class) {
					ParseHelper.parseHeaders(headers, (Headers) annotation);
				}
			}
		}
		if (!hasApi) {
			throw new RuntimeException("class have not Api annotation.");
		}
		
		Method[] methods = service.getMethods();
		if (methods != null) {
			for (Method method : methods) {
				requestMap.put(method, new RequestDescription(method));
			}
		}
	}
	
	private void parseAnnotationOfApi(Api api) {
		log.v(api.toString());
		label = api.label();
		auth = api.auth();
		String[] value = api.value();
		if (value != null) {
			log.d("register api HOST [" + value.length + "]:");
			for (int i = 0; i < value.length; i++) {
				servers.add(value[i]);
				log.d(i + "=>" + value[i] + ",");
			}
		}
	}
	
	public Map<Method, RequestDescription> getRequestMap() {
		return requestMap;
	}

	public String getLabel() {
		return label;
	}

	public String getAuth() {
		return auth;
	}

	public List<String> getServers() {
		return servers;
	}

	public Class<?> getApiClazz() {
		return apiClazz;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}
	
}
