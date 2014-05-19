package com.yepstudio.legolas.internal;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.Endpoint;
import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.LegolasOptions;
import com.yepstudio.legolas.RequestInterceptorFace;
import com.yepstudio.legolas.description.ApiDescription;
import com.yepstudio.legolas.description.ParameterDescription;
import com.yepstudio.legolas.description.ParameterDescription.ParameterType;
import com.yepstudio.legolas.description.RequestDescription;
import com.yepstudio.legolas.description.RequestDescription.RequestType;
import com.yepstudio.legolas.mime.FormUrlEncodedRequestBody;
import com.yepstudio.legolas.mime.MultipartRequestBody;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.mime.StringBody;
import com.yepstudio.legolas.request.OnRequestListener;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.request.RequestWrapper;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.OnResponseListener;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年4月30日
 * @version 2.0, 2014年4月30日
 *
 */
public class RequestBuilder implements RequestInterceptorFace {
	
	private static LegolasLog log = LegolasLog.getClazz(RequestBuilder.class);
	private static String ENCODE= "UTF-8";
	
	private static Map<Class<?>, List<Field>> fieldsCache;
	private static Map<Class<?>, List<Method>> methodsCache;

	private final Endpoint endpoint;
	private final Map<String, Object> legolasHeaders;
	private final ApiDescription api;
	private final RequestDescription request;
	private Converter converter;
	private Object[] arguments;
	
	private Map<String, Object> headerMap = new HashMap<String, Object>();
	private Map<String, Object> pathMap = new HashMap<String, Object>();
	private Map<String, Object> queryMap = new HashMap<String, Object>();
	
	private final FormUrlEncodedRequestBody formBody;
	private final MultipartRequestBody multipartBody;
	private RequestBody body;
	
	private final List<OnRequestListener> onRequestListeners;
	private final Map<Type, OnResponseListener<?>> onResponseListeners;
	private final List<OnErrorListener> onErrorListeners;
	
	public RequestBuilder(Endpoint endpoint, Map<String, Object> headers, ApiDescription api, RequestDescription request, Converter converter) {
		super();
		this.endpoint = endpoint;
		this.legolasHeaders = headers;
		this.api = api;
		this.request = request;
		this.converter = converter;
		
		if (request.getRequestType() == RequestType.FORM_URL_ENCODED) {
			formBody = new FormUrlEncodedRequestBody();
			body = formBody;
			multipartBody = null;
		} else if (request.getRequestType() == RequestType.MULTIPART) {
			formBody = null;
			multipartBody = new MultipartRequestBody();
			body = multipartBody;
		} else {
			formBody = null;
			multipartBody = null;
		}
		
		onRequestListeners = new LinkedList<OnRequestListener>();
		onResponseListeners = new HashMap<Type, OnResponseListener<?>>();
		onErrorListeners = new LinkedList<OnErrorListener>();
	}
	
	public void parseArguments(Object[] arguments) throws UnsupportedEncodingException {
		this.arguments = arguments;
		List<ParameterDescription> list = request.getParameters();
		for (int i = 0; i < list.size(); i++) {
			ParameterDescription p = list.get(i);
			switch (p.getParameterType()) {
			case ParameterType.HEADER:
				addHeader(p.getName(), arguments[i]);
				break;
			case ParameterType.PATH:
				addPathParam(p.getName(), arguments[i]);
				break;
			case ParameterType.QUERY:
				if (p.isMuitiParameter()) {
					addQuerysParam(p.getType(), arguments[i]);
				} else {
					addQueryParam(p.getName(), arguments[i]);
				}
				break;
			case ParameterType.PART:
				if (p.isMuitiParameter()) {
					addPartsParam(p.getType(), arguments[i]);
				} else {
					addPartParam(p.getName(), arguments[i]);
				}
				break;
			case ParameterType.FIELD:
				if (p.isMuitiParameter()) {
					addFieldsParam(p.getType(), arguments[i]);
				} else {
					addFieldParam(p.getName(), arguments[i]);
				}
				break;
			case ParameterType.BODY:
				setBodyParam(p.getName(), arguments[i]);
				break;
			case ParameterType.NONE:
				if (p.isOptions() && arguments[i] != null) {
					parseArgumentsOptions((LegolasOptions) arguments[i]);
				} else if (p.isListener() && arguments[i] != null) {
					parseArgumentsListener(p, arguments[i]);
				} 
				break;
			default:
				
				break;
			}
		}
	}
	
	private void parseArgumentsOptions(LegolasOptions options) {
		if (options.getConverter() != null) {
			converter = options.getConverter();
		}
	}
	
	private void parseArgumentsListener(ParameterDescription p, Object obj) {
		if (p.isErrorListener()) {
			onErrorListeners.add((OnErrorListener) obj);
		} else if (p.isRequestListener()) {
			onRequestListeners.add((OnRequestListener) obj);
		} else if (p.isResponseListener()) {
			onResponseListeners.put(p.getResponseType(), (OnResponseListener<?>) obj);
		}
	}
	
	private LegolasOptions getLegolasOptions() {
		int index = request.getIndexOfOptions();
		if (index < 0 || arguments[index] == null) {
			return null;
		}
		return (LegolasOptions) arguments[index];
	}
	
	protected Endpoint getEndpoint() {
		Endpoint rootPoint;
		LegolasOptions options = getLegolasOptions();
		if (options != null && options.getEndpoint() != null) {
			log.d("find LegolasOptions in param and Endpoint of LegolasOptions is not null, so use it");
			rootPoint = options.getEndpoint();
		} else {
			rootPoint = endpoint;
		}
		return rootPoint;
	}
	
	public String getRequestUrl(boolean original) {
		if(original){
			StringBuilder builder = new StringBuilder();
			builder.append(api.getApiPath());
			if (!api.getApiPath().endsWith("/") && !request.getRequestPath().startsWith("/")) {
				builder.append("/");
			}
			builder.append(request.getRequestPath());
			return builder.toString();
		}else {
			try {
				return buildTargetUrl(false);
			} catch (MalformedURLException e) {
				return null;
			}
		}
	}
	
	public String getRequestDescription() {
		StringBuilder builder = new StringBuilder();
		if (api.getDescription() != null
				&& api.getDescription().trim().length() >0) {
			builder.append(api.getDescription());
			builder.append("-");
		}
		builder.append(request.getDescription());
		Endpoint rootPoint = getEndpoint();
		if (rootPoint != null) {
			builder.append("(");
			builder.append(rootPoint.getName());
			builder.append(")");
		}
		return request.getMethod();
	}
	
	public String getRequestMethod() {
		return request.getMethod();
	}
	
	public int getRequestType() {
		return request.getRequestType();
	}
	
	public Map<String, Object> getHeaders() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.putAll(api.getHeaders());
		map.putAll(request.getHeaders());
		if (legolasHeaders != null && legolasHeaders.size() > 0) {
			map.putAll(legolasHeaders);
		}
		List<ParameterDescription> list = request.getParameters();
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				if (ParameterType.HEADER == list.get(i).getParameterType()) {
					map.put(list.get(i).getName(), arguments[i]);
				}
			}
		}
		return map;
	}
	
	private Map<String, Object> getParams(int paramType) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<ParameterDescription> list = request.getParameters();
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				if (paramType == list.get(i).getParameterType()) {
					map.put(list.get(i).getName(), arguments[i]);
				}
			}
		}
		return map;
	}
	
	public Map<String, Object> getPathParams() {
		return getParams(ParameterType.PATH);
	}

	public Map<String, Object> getQueryParams() {
		return getParams(ParameterType.QUERY);
	}
	
	public Map<String, Object> getFieldParams() {
		return getParams(ParameterType.FIELD);
	}

	public Map<String, Object> getPartParams() {
		return getParams(ParameterType.PART);
	}

	public Object getBodyParams() {
		List<ParameterDescription> list = request.getParameters();
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				if (ParameterType.BODY == list.get(i).getParameterType()) {
					return arguments[i];
				}
			}
		}
		return null;
	}
	
	public void addHeader(String name, Object value) {
		headerMap.put(name, value);
	}
	
	public void addPathParam(String name, Object value) {
		pathMap.put(name, value);
	}
	
	protected void addQuerysParam(Type type, Object value) {
		try {
			addMuitiParameter(queryMap, value);
		} catch (Throwable th) {
			throw new RuntimeException(th);
		}
	}
	
	private void addMuitiParameter(Map<String, Object> target, Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (value == null) {
			return ;
		}
		log.d("addMuitiParameter:");
		if (value instanceof Map) {
			log.v("is map, add Parameter for map");
			Map map = (Map) value;
			for (Object key : map.keySet()) {
				if (key != null) {
					Object result = map.get(key);
					log.v("Map:" + key.toString() + "=>" + result);
					target.put(key.toString(), result);
				}
			}
		} else {
			Class<?> clazz = value.getClass();
			log.v("is Object, add Parameter for Object");
			cacheAllPublicFieldsAndMethods(clazz);
			List<Field> fieldList = fieldsCache.get(clazz);
			if (fieldList != null && !fieldList.isEmpty()) {
				for (Field field : fieldList) {
					Object result = field.get(value);
					log.v("Field:" + field.getName() + "=>" + result);
					target.put(field.getName(), result);	
				}
			}
			
			List<Method> methodList = methodsCache.get(clazz);
			if (methodList != null && !methodList.isEmpty()) {
				for (Method method : methodList) {
					String name = method.getName();
					String paramName = method2Param(name);
					if (paramName != null) {
						Object result = method.invoke(value);
						log.v("Method: [" + method.getName() + "] " + paramName + "=>" + result);
						target.put(paramName, result);
					}
				}
			}
		}
	}
	
	private String method2Param(String name) {
		String paramName = null;
		if ("getClass".equals(name)) {
			return null;
		}
		if (name.startsWith("get") && name.length() > 3) {
			paramName = "";
			if (name.length() > 4) {
				paramName = name.substring(4);
			}
			paramName = name.substring(3, 4).toLowerCase() + paramName;
		} else if (name.startsWith("is") && name.length() > 2) {
			paramName = "";
			if (name.length() > 3) {
				paramName = name.substring(2);
			}
			paramName = name.substring(2, 3).toLowerCase() + paramName;
		}
		return paramName;
	}
	
	private static void cacheAllPublicFieldsAndMethods(Class<?> clazz) {
		if (fieldsCache == null) {
			fieldsCache = new HashMap<Class<?>, List<Field>>();
		}
		if (methodsCache == null) {
			methodsCache = new HashMap<Class<?>, List<Method>>();
		}
		if (fieldsCache.containsKey(clazz) || methodsCache.containsKey(clazz)) {
			return ;
		}
		Field[] fields = clazz.getFields();
		List<Field> fieldList = null;
		if (fields != null && fields.length > 0) {
			fieldList = new ArrayList<Field>(fields.length);
			for (Field field : fields) {
				fieldList.add(field);
			}
		}
		fieldsCache.put(clazz, fieldList);
		
		Method[] methods = clazz.getMethods();
		List<Method> methodList = null;
		if (methods != null && methods.length > 0) {
			methodList = new ArrayList<Method>(methods.length);
			for (Method method : methods) {
				
				methodList.add(method);
			}
		}
		methodsCache.put(clazz, methodList);
	}
	
	public void addQueryParam(String name, Object value) {
		queryMap.put(name, value);
	}
	
	protected void addFieldsParam(Type type, Object value) {
		try {
			Map<String, Object> fieldsMap = new HashMap<String, Object>();
			addMuitiParameter(fieldsMap, value);
			for (String key : fieldsMap.keySet()) {
				addFieldParam(key, fieldsMap.get(key));
			}
		} catch (Throwable th) {
			throw new RuntimeException(th);
		}
	}
	
	public void addFieldParam(String name, Object target) {
		formBody.addField(name, converter.toParam(target, ParameterType.FIELD));
	}
	
	protected void addPartsParam(Type type, Object value) {
		try {
			Map<String, Object> partsMap = new HashMap<String, Object>();
			addMuitiParameter(partsMap, value);
			for (String key : partsMap.keySet()) {
				addPartParam(key, partsMap.get(key));
			}
		} catch (Throwable th) {
			throw new RuntimeException(th);
		}
	}
	
	public void addPartParam(String name, Object target) {
		if (target != null) { // Skip null values.
			if (target instanceof RequestBody) {
				multipartBody.addPart(name, (RequestBody) target);
			} else if (target instanceof String) {
				multipartBody.addPart(name, new StringBody((String) target));
			} else {
				multipartBody.addPart(name, converter.toBody(target));
			}
		}
	}
	
	public void setBodyParam(String name, Object target) {
		if (target instanceof RequestBody) {
			body = (RequestBody) target;
		} else {
			body = converter.toBody(target);
		}
	}
	
	protected String buildRequestPath() {
		String path = request.getRequestPath();
		String value;
		List<ParameterDescription> parameters = request.getParameters();
		for (String name : request.getRequestPathParamNames()) {
			value = null;
			if (pathMap.containsKey(name)) {
				value = converter.toParam(pathMap.get(name), ParameterType.PATH);
			} else {
				for (int i = 0; i < parameters.size(); i++) {
					ParameterDescription description = parameters.get(i);
					if (description.getName().equals(name)
							&& description.getParameterType() == ParameterType.PATH) {
						value = converter.toParam(arguments[i], ParameterType.PATH);
					}
				}
			}
			if (value == null) {
				throw new IllegalArgumentException("request lost [PATH] params : " + name);
			}
			path = path.replace(String.format("{%s}", name), encodeValue(value));
		}
		log.d("buildRequestPath : " + path);
		return path;
	}
	
	protected String encodeValue(String value) {
		return value;
//		try {
//			String encodeValue = URLEncoder.encode(value, ENCODE);
//			if (encodeValue != null && encodeValue.contains("+")) {
//				log.v("encodeValue , replace [+] : [" + encodeValue + "]");
//				encodeValue = encodeValue.replace("+", "%20");
//			}
//			log.v("encodeValue: [" + value + "]=>[" + encodeValue + "]");
//			return encodeValue;
//		} catch (UnsupportedEncodingException e) {
//			log.e("UnsupportedEncodingException:[" + ENCODE + "]", e);
//			return value;
//		}
	}
	
	protected String buildTargetUrl(boolean appleQuery) throws MalformedURLException {
		log.i("buildTargetUrl:");
		StringBuilder targetUrl = new StringBuilder();
		targetUrl.append(api.getApiPath());
		log.v("API path : " + api.getApiPath());
		String requestPath = buildRequestPath();
		log.v("Request Path : " + requestPath);
		if (!api.getApiPath().endsWith("/")
				&& requestPath != null
				&& requestPath.trim().length() > 0
				&& !requestPath.startsWith("/")) {
			targetUrl.append("/");
		}
		targetUrl.append(requestPath);
		if (!appleQuery) {
			return applyEndpoint(targetUrl.toString());
		}
		boolean hasQuery = false;
		if (request.getRequestQuery() != null && request.getRequestQuery().trim().length() > 0) {
			targetUrl.append("?");
			targetUrl.append(request.getRequestQuery());
			hasQuery = true;
			log.v("hasQuery : " + request.getRequestQuery());
		}
		if (queryMap != null && queryMap.size() > 0) {
			if (hasQuery) {
				targetUrl.append("&");
			} else {
				targetUrl.append("?");
			}
			for (String name : queryMap.keySet()) {
				targetUrl.append(encodeValue(name));
				targetUrl.append("=");
				targetUrl.append(encodeValue(converter.toParam(queryMap.get(name), ParameterType.QUERY)));
				targetUrl.append("&");
			}
			targetUrl.deleteCharAt(targetUrl.length() - 1);
		}
		return applyEndpoint(targetUrl.toString());
	}
	
	protected String applyEndpoint(String targetUrl) throws MalformedURLException {
		URL url = null;
		Endpoint endpoint = getEndpoint();
		if (endpoint == null) {
			log.w("endpoint is null, can switch HOST, targetUrl : " + targetUrl);
			return targetUrl.toString();
		} else {
			log.d("endpoint find, use relative path of [" + endpoint.getUrl() + "]");
			URL endpointURL = new URL(endpoint.getUrl());
			url = new URL(endpointURL, targetUrl);
			log.v("[" + targetUrl.toString() + "]=>[" + url.toString() + "]");
			return url.toString();
		}
	}
	
	public RequestWrapper build() throws Exception {
		if (multipartBody != null && multipartBody.getPartCount() <= 0) {
			throw new IllegalStateException("Multipart requests must contain at least one part.");
		}
		Map<String, Object> header = getHeaders();
		Map<String, String> headers = new HashMap<String, String>();
		for (String key : header.keySet()) {
			headers.put(key, converter.toParam(header.get(key), ParameterType.HEADER));
		}
		Request request = new Request(getRequestDescription(), getRequestMethod(), buildTargetUrl(true), headers, body);
		return new RequestWrapper(request, this.request.getResponseType(), converter, onRequestListeners, onResponseListeners, onErrorListeners);
	}

	public Converter getConverter() {
		return converter;
	}
	
}
