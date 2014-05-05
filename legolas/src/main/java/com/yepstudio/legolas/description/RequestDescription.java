package com.yepstudio.legolas.description;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.annotation.FormUrlEncoded;
import com.yepstudio.legolas.annotation.Headers;
import com.yepstudio.legolas.annotation.Http;
import com.yepstudio.legolas.annotation.Multipart;
import com.yepstudio.legolas.description.ParameterDescription.ParameterType;

/**
 * 每个带有@GET @POST @PUT注释的方法，将一些数据缓存下来，省得每次都要调用反射
 * @author zzljob@gmail.com
 * @create 2014年1月8日
 * @version 2.0, 2014年4月23日
 */
public class RequestDescription {
	
	private static LegolasLog log = LegolasLog.getClazz(RequestDescription.class);
	
	public static interface RequestType {
		/** No content-specific logic required. */
		final int SIMPLE = 0;
		/** Multi-part request body. */
		final int MULTIPART = 1;
		/** Form URL-encoded request body. */
		final int FORM_URL_ENCODED = 2;
	}
	
	private static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
	private static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);
	private static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");
	
	private final Method javaMethod;
	/**是否同步**/
	private final boolean synchronous;
	private boolean httpRequest = false;
	
	private Type responseType;
	
	private int requestType = RequestType.SIMPLE;
	private String description;
	private String method;
	private boolean supportBody;
	/**源请求URL**/
	private String requestUrl;
	private String requestPath;
	
	private final Set<String> requestPathParamNames;
	/**requestUrl上带有的参数**/
	private String requestQuery;
	private final Map<String, String> headers = new ConcurrentHashMap<String, String>();
	
	private final List<ParameterDescription> parameters = new LinkedList<ParameterDescription>();
	
	public RequestDescription(Method method) {
		super();
		javaMethod = method;
		responseType = javaMethod.getGenericReturnType();
		requestPathParamNames = new LinkedHashSet<String>();
		synchronous = hasSynchronousReturnType(responseType);
		log.v("[" + method.getName() + "] synchronous:" + synchronous + ", responseType:" + responseType.toString());
		parseMethodAnnotations();
		parseRequestPathParams(requestUrl);
		parseParameters();
		vaildateParameters();
	}
	
	public static boolean hasSynchronousReturnType(Type responseType) {
		return !(responseType == void.class || responseType == Void.class);
	}
	
	private Http getHttpAnnotation(Annotation methodAnnotation) {
		if (methodAnnotation == null) {
			return null;
		}
		Annotation[] annotations = methodAnnotation.annotationType().getAnnotations();
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (Http.class == annotation.annotationType()) {
					return (Http) annotation;
				}
			}
		}
		return null;
	}
	
	private void parseAnnotationWithHttpAnnotation(Annotation methodAnnotation, Http http) {
		method = http.value();
		supportBody = http.supportBody();
		
		String valueName = http.httpValue();
		String url = null;
		boolean fail = true;
		try {
			Method method = methodAnnotation.annotationType().getMethod(valueName);
			Object result = method.invoke(methodAnnotation);
			if(result == null) {
				url = "";
			} else {
				url = result.toString();
			}
			fail = false;
		} catch (NoSuchMethodException e) {
			log.e("get Request url fail", e);
		} catch (SecurityException e) {
			log.e("get Request url fail", e);
		} catch (IllegalAccessException e) {
			log.e("get Request url fail", e);
		} catch (IllegalArgumentException e) {
			log.e("get Request url fail", e);
		} catch (InvocationTargetException e) {
			log.e("get Request url fail", e);
		}
		if (fail) {
			throw new IllegalArgumentException("can not parse Annotation With @Http Annotation");
		}
		requestUrl = url;
	}
	
	/**
	 * 解析方法上的注释
	 */
	private void parseMethodAnnotations() {
		log.d("start parseMethodAnnotations...");
		Annotation[] methodAnnotations = javaMethod.getAnnotations();
		if (methodAnnotations != null) {
			for (Annotation methodAnnotation : methodAnnotations) {
				if (Headers.class == methodAnnotation.annotationType()) {
					ParseHelper.parseHeaders(headers, (Headers) methodAnnotation);
				} else if (FormUrlEncoded.class == methodAnnotation.annotationType()) {
					requestType = RequestType.FORM_URL_ENCODED;
				} else if (Multipart.class == methodAnnotation.annotationType()) {
					requestType = RequestType.MULTIPART;
				} else {
					Http http = getHttpAnnotation(methodAnnotation);
					if (httpRequest && http != null) {
						throw new IllegalArgumentException("can not have two or more Annotation with @Http Method");
					}
					if (http != null) {
						parseAnnotationWithHttpAnnotation(methodAnnotation, http);
						httpRequest = true;
					}
				}
				log.v(methodAnnotation.toString());
			}
		}
		log.v(String.format("description:%s, requestUrl:%s, method:%s, supportBody:%s", description, requestUrl, method, supportBody));
		if (!httpRequest) {
			log.w("this Method has not Annotation with @Http, is not a Request.");
		}
	}

	/**
	 * 解析RequestPath上带有的参数，形式是：{xxx}，不能带空格<br/>
	 * 只解析requestUrl 的？前面的内容<br/>
	 * requestUrl的？后面的内容是RequestQuery，不允许使用{xxx}参数，要想自定义就去在参数上使用@Query
	 * @param path
	 * @return
	 */
	private void parseRequestPathParams(String fullUrl) {
		log.d("parseRequestPathParams:" + fullUrl);
		if (fullUrl == null || fullUrl == "") {
			log.w("requestUrl is empty");
			return ;
		}
		int index = fullUrl.indexOf("?");
		if (index > -1) {
			requestPath = fullUrl.substring(0, index);
			requestQuery = fullUrl.substring(index + 1);
		} else {
			requestPath = fullUrl;
			requestQuery = "";
		}
		log.v(String.format("requestPath:%s, requestQuery:%s", requestPath, requestQuery));
		
		Matcher m = PARAM_URL_REGEX.matcher(requestPath);
	    String v = "";
		while (m.find()) {
			v = m.group(1);
			log.v("find path param : [" + v + "]");
			requestPathParamNames.add(v);
		}
	}

	/**
	 * 解析参数
	 */
	private void parseParameters() {
		log.d("start parseParameters...");
		Type[] parameterTypes = javaMethod.getGenericParameterTypes();
		//Class<?>[] clazzes = javaMethod.getParameterTypes();
		Annotation[][] parameterAnnotations = javaMethod.getParameterAnnotations();
		
		boolean hasListener = false;
		boolean hasField = false;
		boolean hasPart = false;
		int bodyParams = 0;
		
		for (int i = 0; i < parameterTypes.length; i++) {
			ParameterDescription param = new ParameterDescription(parameterTypes[i], parameterAnnotations[i]);
			if (param.isListener()) {
				hasListener = true;
			}
			if (!param.isIgnore()) {
				parameters.add(param);
			} else {
				switch (param.getParameterType()) {
				case ParameterType.BODY:
					bodyParams++;
					break;
				case ParameterType.FIELD:
					hasField = true;
					break;
				case ParameterType.PART:
					hasPart = true;
					break;

				default:
					break;
				}
			}
		}
		if (hasListener && synchronous) {
			throw new IllegalStateException("request result is [" + responseType + "], synchronous to request, so can not has listener in Parameter");
		}
		if ((bodyParams > 0 || hasField || hasPart) && !supportBody) {
			throw new IllegalStateException("request[" + method + "] is not support has @Body、@Field、@Part in Parameter.");
		}
		if (requestType == RequestType.FORM_URL_ENCODED) {
			if (bodyParams > 0 || hasPart) {
				throw new IllegalStateException("RequestType [FORM_URL_ENCODED] just support @Field in Parameter.");
			}
		} else if (requestType == RequestType.MULTIPART) {
			if (bodyParams > 0 || hasField) {
				throw new IllegalStateException("RequestType [MULTIPART] just support @Part in Parameter.");
			}
		} else if (requestType == RequestType.SIMPLE) {
			if (hasPart || hasField) {
				throw new IllegalStateException("RequestType [SIMPLE] just support @Body in Parameter.");
			} else if (bodyParams > 1) {
				throw new IllegalStateException("RequestType [SIMPLE] just support @Body in Parameter, and just one Parameter whit @Body.");
			} 
		}
	}
	
	private void vaildateParameters() {
		log.d("start vaildateParameters...");
		for (ParameterDescription description : parameters) {
			if (ParameterType.PATH == description.getParameterType()) {
				vaildatePath(description.getName());
			}
		}
	}

	private void vaildatePath(String name) {
		if (!PARAM_NAME_REGEX.matcher(name).matches()) {
			throw new IllegalStateException("parameter name is not valid: " + name + ". Must match " + PARAM_URL_REGEX.pattern());
		}
		if (!requestPathParamNames.contains(name)) {
			throw new IllegalStateException("Method URL \"" + requestUrl + "\" does not contain {" + name + "}.");
		}
		log.v("vaildatePath: [" + name + "] success");
	}

	public boolean isSynchronous() {
		return synchronous;
	}

	public int getRequestType() {
		return requestType;
	}

	public String getDescription() {
		return description;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public String getMethod() {
		return method;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public Type getResponseType() {
		return responseType;
	}

	public boolean isHttpRequest() {
		return httpRequest;
	}
	
	public int getIndexOfOptions() {
		for (int i = 0; i < parameters.size(); i++) {
			if(parameters.get(i).isOptions()){
				return i;
			}
		}
		return -1;
	}

	public String getRequestQuery() {
		return requestQuery;
	}
	
	public String getRequestPath() {
		return requestPath;
	}

	public List<ParameterDescription> getParameters() {
		return parameters;
	}

	public Set<String> getRequestPathParamNames() {
		return requestPathParamNames;
	}
}
