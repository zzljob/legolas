package com.yepstudio.legolas.description;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.yepstudio.legolas.Legolas;
import com.yepstudio.legolas.LegolasConfiguration;
import com.yepstudio.legolas.RequestInterceptor;
import com.yepstudio.legolas.annotation.Api;
import com.yepstudio.legolas.annotation.Description;
import com.yepstudio.legolas.annotation.Interceptors;
import com.yepstudio.legolas.exception.LegolasConfigureError;

/**
 * 每个带有@Api注释的类的描述类，将一些数据缓存下来，省得每次都要调用反射
 * 
 * @author zzljob@gmail.com
 * @create 2013年12月27日
 * @version 3.0，2014年11月4日
 */
public class ApiDescription {

	private static final String LOG_VALIDATE_ISINTERFACE = "Only interface endpoint definitions are supported.";
	private static final String LOG_VALIDATE_NO_EXTENDS = "Interface definitions must not extend other interfaces.";
	private static final String LOG_VALIDATE_NO_API_ANNOTATION = "Interface definitions must has annotation @Api.";

	private final String description;
	private final String apiPath;
	private final boolean absoluteApiPath;

	private final List<RequestInterceptor> interceptors = new LinkedList<RequestInterceptor>();
	private final boolean expansionInterceptors;
	
	private final Map<Method, RequestDescription> requestDescriptionCache;

	public ApiDescription(Class<?> clazz, LegolasConfiguration config) {
		super();
		requestDescriptionCache = new HashMap<Method, RequestDescription>();
		
		validateServiceClass(clazz);

		Api api = clazz.getAnnotation(Api.class);
		
		if (api == null) {
			throw new LegolasConfigureError(LOG_VALIDATE_NO_API_ANNOTATION);
		}
		this.apiPath = api.value();
		this.absoluteApiPath = api.absolute();
		
		Description description = clazz.getAnnotation(Description.class);
		if (description != null) {
			this.description = description.value();
		} else {
			this.description = null;
		}
		
		Interceptors interceptors = clazz.getAnnotation(Interceptors.class);
		if (interceptors != null) {
			expansionInterceptors = interceptors.expansion();
			AnnotationHelper.parseInterceptors(this.interceptors, interceptors, config);
		} else {
			expansionInterceptors = false;
		}

		Method[] methods = clazz.getMethods();
		if (methods != null) {
			for (Method method : methods) {
				long birthTime = System.currentTimeMillis();
				requestDescriptionCache.put(method, new RequestDescription(method, config));
				long finishTime = System.currentTimeMillis();
				Legolas.getLog().d("RequestDescription [" + method.getName() + "] be init : [" + (finishTime - birthTime) + "ms]");
			}
		}
	}

	private static <T> void validateServiceClass(Class<T> service) {
		if (!service.isInterface()) {
			throw new LegolasConfigureError(LOG_VALIDATE_ISINTERFACE);
		}
		// Prevent API interfaces from extending other interfaces. This not only  avoids a bug in
		// Android (http://b.android.com/58753) but it forces composition of API
		// declarations which is the recommended pattern.
		if (service.getInterfaces().length > 0) {
			throw new LegolasConfigureError(LOG_VALIDATE_NO_EXTENDS);
		}
	}

	public String getDescription() {
		return description;
	}

	public String getApiPath() {
		return apiPath;
	}

	public boolean isAbsoluteApiPath() {
		return absoluteApiPath;
	}

	public List<RequestInterceptor> getInterceptors() {
		return interceptors;
	}

	public boolean isExpansionInterceptors() {
		return expansionInterceptors;
	}

	public RequestDescription getRequestDescription(Method method) {
		return requestDescriptionCache.get(method);
	}

}
