package com.yepstudio.android.legolas.description;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

import com.yepstudio.android.legolas.annotation.FormUrlEncoded;
import com.yepstudio.android.legolas.annotation.Headers;
import com.yepstudio.android.legolas.annotation.Multipart;
import com.yepstudio.android.legolas.annotation.http.DELETE;
import com.yepstudio.android.legolas.annotation.http.GET;
import com.yepstudio.android.legolas.annotation.http.HEAD;
import com.yepstudio.android.legolas.annotation.http.Http;
import com.yepstudio.android.legolas.annotation.http.OPTIONS;
import com.yepstudio.android.legolas.annotation.http.PATCH;
import com.yepstudio.android.legolas.annotation.http.POST;
import com.yepstudio.android.legolas.annotation.http.PUT;
import com.yepstudio.android.legolas.annotation.parameter.Body;
import com.yepstudio.android.legolas.annotation.parameter.Field;
import com.yepstudio.android.legolas.annotation.parameter.Header;
import com.yepstudio.android.legolas.annotation.parameter.Part;
import com.yepstudio.android.legolas.annotation.parameter.Path;
import com.yepstudio.android.legolas.annotation.parameter.Query;
import com.yepstudio.android.legolas.http.Request;
import com.yepstudio.android.legolas.http.Response;
import com.yepstudio.android.legolas.log.LegolasLog;

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
	
	public static interface ParameterType {
		final int PATH = 0;
		final int QUERY = 1;
		final int FIELD = 2;
		final int PART = 3;
		final int BODY = 4;
		final int HEADER = 5;
		final int NONE = -1;
	}
	
	public static class Parameter {
		public int index;
		public Type type;
		public Class<?> clazz;
		public int parameterType = ParameterType.NONE;
		public String name;
		public boolean encoded = false;
		public String format;
		public boolean isRequestListener;
		public boolean isResponseListener;
		public boolean isErrorListener;
	}
	
	private static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
	private static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);
	private static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");
	
	private final Method javaMethod;
	/**是否同步**/private final boolean isSynchronous;
	private int requestType = RequestType.SIMPLE;
	private String lable;
	private String auth;
	private String requestUrl;
	/**requestUrl上带有的参数**/private Set<String> requestUrlParamNames;
	private String requestQuery;
	private String method;
	private boolean hasBody;
	private final Map<String, String> headers;
	private final List<Parameter> parameters;
	private Type responseType;
	
	private AtomicBoolean cached = new AtomicBoolean(false);
	
	public RequestDescription(Method method) {
		super();
		javaMethod = method;
		headers = new HashMap<String, String>(); 
		parameters = new ArrayList<Parameter>(); 
		responseType = javaMethod.getGenericReturnType();
		if (responseType == void.class || responseType == Void.class) {
			isSynchronous = false;
		} else {
			isSynchronous = true;
		}
		log.v("[" + method.getName() + "] isSynchronous:" + isSynchronous + ", responseType:" + responseType.toString());
	}
	
	public void initCache() {
		if (!cached.getAndSet(true)) {
			log.v("initCache");
			parseMethodAnnotations();
			parseParameters();
		}
	}
	
	/**
	 * 解析方法上的注释
	 */
	private void parseMethodAnnotations() {
		log.v("parseMethodAnnotations:");
		Annotation[] methodAnnotations = javaMethod.getAnnotations();
		Http http = null;
		Annotation httpAnnotation = null;
		if (methodAnnotations != null) {
			for (Annotation methodAnnotation : methodAnnotations) {
				//注释的注释
				Annotation[] annotations = methodAnnotation.annotationType().getAnnotations();
				if (annotations != null) {
					for (Annotation annotation : annotations) {
						if (Http.class.equals(annotation.annotationType())) {
							if (http != null) {
								throw new RuntimeException("can not have two or more Annotation with @HttpMethod");
							}
							http = (Http) annotation;
							httpAnnotation = methodAnnotation;
							break;
						}
					}
				}
				if (Headers.class == methodAnnotation.annotationType()) {
					ParseHelper.parseHeaders(headers, (Headers) methodAnnotation);
				} else if (FormUrlEncoded.class == methodAnnotation.annotationType()) {
					requestType = RequestType.FORM_URL_ENCODED;
				} else if (Multipart.class == methodAnnotation.annotationType()) {
					requestType = RequestType.MULTIPART;
				}
				log.v(methodAnnotation.toString());
			}
		}
		if (http == null || httpAnnotation == null) {
			throw new RuntimeException("have not one Annotation with @HttpMethod");
		}
		method = http.value();
		hasBody = http.hasBody();
		String fullUrl = "";
		if (GET.class == httpAnnotation.annotationType()) {
			GET anno = (GET) httpAnnotation;
			fullUrl = anno.value();
			lable = anno.label();
			auth = anno.auth();
		} else if (POST.class == httpAnnotation.annotationType()) {
			POST anno = (POST) httpAnnotation;
			fullUrl = anno.value();
			lable = anno.label();
			auth = anno.auth();
		} else if (PUT.class == httpAnnotation.annotationType()) {
			PUT anno = (PUT) httpAnnotation;
			fullUrl = anno.value();
			lable = anno.label();
			auth = anno.auth();
		} else if (PATCH.class == httpAnnotation.annotationType()) {
			PATCH anno = (PATCH) httpAnnotation;
			fullUrl = anno.value();
			lable = anno.label();
			auth = anno.auth();
		} else if (DELETE.class == httpAnnotation.annotationType()) {
			DELETE anno = (DELETE) httpAnnotation;
			fullUrl = anno.value();
			lable = anno.label();
			auth = anno.auth();
		} else if (OPTIONS.class == httpAnnotation.annotationType()) {
			OPTIONS anno = (OPTIONS) httpAnnotation;
			fullUrl = anno.value();
			lable = anno.label();
			auth = anno.auth();
		} else if (HEAD.class == httpAnnotation.annotationType()) {
			HEAD anno = (HEAD) httpAnnotation;
			fullUrl = anno.value();
			lable = anno.label();
			auth = anno.auth();
		} else {
			
		}
		log.v(String.format("lable:%s, method:%s, hasBody:%s, auth:%s,", lable, method, hasBody, auth));
		requestUrlParamNames = parsePath(fullUrl);
	}

	/**
	 * 解析RequestUrl上带有的参数<br/>
	 * 只解析requestUrl 的？后面的是requestQuery，不允许使用{xxx}参数
	 * @param path
	 * @return
	 */
	private Set<String> parsePath(String fullUrl) {
		log.v("parsePath:");
		int index = fullUrl.indexOf("?");
		if (index > -1) {
			requestUrl = fullUrl.substring(0, index);
			requestQuery = fullUrl.substring(index + 1);
		} else {
			requestUrl = fullUrl;
			requestQuery = "";
		}
		log.v(String.format("requestUrl:%s, requestQuery:%s", requestUrl, requestQuery));
		Matcher m = PARAM_URL_REGEX.matcher(fullUrl);
	    Set<String> patterns = new LinkedHashSet<String>();
	    String v = "";
		while (m.find()) {
			v = m.group(1);
			log.v("fina path param : " + v);
			patterns.add(v);
		}
	    return patterns;
	}

	/**
	 * 解析参数
	 */
	private void parseParameters() {
		log.v("parseParameters:");
		Type[] parameterTypes = javaMethod.getGenericParameterTypes();
		Class<?>[] clazzes = javaMethod.getParameterTypes();
		Annotation[][] parameterAnnotations = javaMethod.getParameterAnnotations();
		boolean getField = false;
		boolean getPart = false;
		boolean getBody = false;
		for (int i = 0; i < parameterTypes.length; i++) {
			Annotation[] parameterAnnotation = parameterAnnotations[i];
			Parameter p = new Parameter();
			p.index = i;
			p.clazz = clazzes[i];
			p.type = parameterTypes[i];
			if (parameterAnnotation != null && parameterAnnotation.length > 0) {
				parseParameterAnnotation(p, parameterAnnotation);
				switch (p.parameterType) {
				case ParameterType.BODY:
					getBody = true;
					break;
				case ParameterType.FIELD:
					getField = true;
					break;
				case ParameterType.PART:
					getPart = true;
					break;

				default:
					break;
				}
			} else { //没有注释
				if (parseListener(p) && isSynchronous) {
					throw new IllegalStateException("request result is void, so can not has listener in Parameter whitout Annotation");
				}
			}
			if ((getBody || getField || getPart) && !hasBody) {
				throw new IllegalStateException("request[" + method + "] is not support has @Body、@Field、@Part.");
			}
			parameters.add(p);
		}
	}
	
	private boolean parseListener(Parameter p) {
		Class<?> clazz = p.clazz;
		boolean isListener = false;
		if (clazz.equals(Request.OnRequestListener.class)) {
			p.isRequestListener = true;
		}
		if (clazz.equals(Response.OnResponseListener.class)) {
			p.isResponseListener = true;
			// 配置返回的类型
			if (p.type instanceof ParameterizedType) {
				responseType = getParameterUpperBound((ParameterizedType) p.type);
			}
		}
		if (clazz.equals(Response.OnErrorListener.class)) {
			p.isErrorListener = true;
		}
		isListener = p.isRequestListener || p.isResponseListener || p.isErrorListener;
		log.v("parseListener isListener:" + isListener);
		return isListener;
	}
	
	private static Type getParameterUpperBound(ParameterizedType type) {
		Type[] types = type.getActualTypeArguments();
		for (int i = 0; i < types.length; i++) {
			Type paramType = types[i];
			if (paramType instanceof WildcardType) {
				types[i] = ((WildcardType) paramType).getUpperBounds()[0];
			}
		}
		log.v("getParameterUpperBound:" + types[0]);
		return types[0];
	}
	
	/**
	 * 处理参数上的注释
	 * @param p
	 * @param parameterAnnotation
	 */
	private void parseParameterAnnotation(Parameter p, Annotation[] parameterAnnotation) {
		log.v("parseParameterAnnotation:");
		for (Annotation annotation : parameterAnnotation) {
			log.v("annotation:" + annotation);
			if (Path.class == annotation.annotationType()) {
				p.parameterType = ParameterType.PATH;
				Path anno = (Path) annotation;
				p.encoded = anno.encoded();
				p.format = anno.format();
				p.name = anno.value();
				vaildatePath(p.name);
			} else if (Header.class == annotation.annotationType()) {
				p.parameterType = ParameterType.HEADER;
				Header anno = (Header) annotation;
				p.encoded = false;
				p.format = anno.format();
				p.name = anno.value();
			} else if (Query.class == annotation.annotationType()) {
				p.parameterType = ParameterType.QUERY;
				Query anno = (Query) annotation;
				p.encoded = anno.encoded();
				p.format = anno.format();
				p.name = anno.value();
			} else if (Part.class == annotation.annotationType()) {
				if (requestType != RequestType.MULTIPART) {
					throw new IllegalStateException("@Part parameters can only be used with multipart encoding.");
				}
				p.parameterType = ParameterType.PART;
				Part anno = (Part) annotation;
				p.format = anno.format();
				p.name = anno.value();
			} else if (Field.class == annotation.annotationType()) {
				if (requestType != RequestType.FORM_URL_ENCODED) {
					throw new IllegalStateException("@Field parameters can only be used with form encoding.");
				}
				p.parameterType = ParameterType.FIELD;
				Field anno = (Field) annotation;
				p.format = anno.format();
				p.name = anno.value();
			} else if (Body.class == annotation.annotationType()) {
				if (requestType != RequestType.SIMPLE) {
					throw new IllegalStateException("@Body parameters can not be used with form or multipart encoding.");
				}
				p.parameterType = ParameterType.BODY;
				Body anno = (Body) annotation;
				p.format = anno.format();
				p.name = anno.value();
			} else {
				
			}
			log.v(annotation.toString());
			if (TextUtils.isEmpty(p.name)) {
				throw new IllegalStateException("parameter with Annotation, name can not be null.");
			}
		}
	}
	
	private void vaildatePath(String name) {
		if (!PARAM_NAME_REGEX.matcher(name).matches()) {
			throw new IllegalStateException("parameter name is not valid: " + name + ". Must match " + PARAM_URL_REGEX.pattern());
		}
		if (!requestUrlParamNames.contains(name)) {
			throw new IllegalStateException("Method URL \"" + requestUrl + "\" does not contain {" + name + "}.");
		}
		log.v("vaildatePath:" + name + " success");
	}

	public boolean isSynchronous() {
		return isSynchronous;
	}

	public int getRequestType() {
		return requestType;
	}

	public String getLable() {
		return lable;
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

	public List<Parameter> getParameters() {
		return parameters;
	}

	public Type getResponseType() {
		return responseType;
	}

	public String getAuth() {
		return auth;
	}

	public String getRequestQuery() {
		return requestQuery;
	}
}
