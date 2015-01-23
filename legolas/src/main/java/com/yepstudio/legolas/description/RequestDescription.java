package com.yepstudio.legolas.description;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yepstudio.legolas.LegolasConfiguration;
import com.yepstudio.legolas.ParameterType;
import com.yepstudio.legolas.RequestInterceptor;
import com.yepstudio.legolas.RequestType;
import com.yepstudio.legolas.annotation.Description;
import com.yepstudio.legolas.annotation.FormUrlEncoded;
import com.yepstudio.legolas.annotation.Http;
import com.yepstudio.legolas.annotation.Interceptors;
import com.yepstudio.legolas.annotation.Multipart;
import com.yepstudio.legolas.exception.LegolasConfigureError;
import com.yepstudio.legolas.request.BasicRequest;
import com.yepstudio.legolas.request.Request;

/**
 * 每个带有@POST、@PUT、@TRACE、@GET、@DELETE、@OPTIONS、@HEAD注释的方法都将被缓存下来，省得每次都要调用反射
 * @author zzljob@gmail.com
 * @create 2014年1月8日
 * @version 2.0, 2014年4月23日
 */
public class RequestDescription {
	
	private static final String LOG_VALIDATE_MULTI_HTTP_METHOD = "@POST、@PUT、@TRACE、@GET、@DELETE、@OPTIONS、@HEAD，just only have one.";
	private static final String LOG_VALIDATE_NO_HTTP_METHOD = "@POST、@PUT、@TRACE、@GET、@DELETE、@OPTIONS、@HEAD，must have one.";
	private static final String LOG_VALIDATE_MULTI_REQUEST_TYPE = "@FormUrlEncoded、@Multipart，just only have one.";
	private static final String LOG_VALIDATE_SIMPLE_REQUEST = "@GET、@DELETE、@OPTIONS、@HEAD not support @FormUrlEncoded、@Multipart";
	private static final String LOG_VALIDATE_FORMURLENCODED = "@FormUrlEncoded not support @Parts、@Part";
	private static final String LOG_VALIDATE_MULTIPART = "@Multipart not support @Fields、@Field";
	private static final String LOG_VALIDATE_LOST_PATH = "need @Path params for [%s]";
	
	private static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
	private static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);
	private static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");
	
	private final Type resultType;
	private final Type exceptionType;
	/**未处理过的URL配置**/
	private final String requestPath;
	/**是否是绝对URL路径**/
	private final boolean absolutePath;
	private final String description;
	private final String method;
	private final boolean supportBody;
	private final RequestType requestType;
	private final List<RequestInterceptor> interceptors = new LinkedList<RequestInterceptor>();
	private final boolean expansionInterceptors;
	private final List<ParameterDescription> parameters = new LinkedList<ParameterDescription>();
	
	/**是否同步**/
	private final boolean synchronous;
	/****去掉了Query参数的URL链接***/
	private String requestUrl;
	private final Set<String> requestPathParamNames = new HashSet<String>();
	private final Map<String, String> requestQueryMap = new HashMap<String, String>();
	
	public RequestDescription(Method method, LegolasConfiguration config) {
		super();
		resultType = method.getGenericReturnType();
		exceptionType = null;
		
		Annotation[] annotations = method.getAnnotations();
		Description description = null;
		Interceptors interceptors = null;
		FormUrlEncoded formUrlEncoded = null; 
		Multipart multipart = null; 
		Http http = null;
		Annotation httpMethod = null; 
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (annotation.annotationType() == Description.class) {
					description = (Description) annotation;
				} else if (annotation.annotationType() == Interceptors.class) {
					interceptors = (Interceptors) annotation;
				} else if (FormUrlEncoded.class == annotation.annotationType()) {
					formUrlEncoded =  (FormUrlEncoded) annotation;
				} else if (Multipart.class == annotation.annotationType()) {
					multipart =  (Multipart) annotation;
				} else {
					Http temp = getHttpAnnotation(annotation);
					if (temp == null) {
						continue;
					}
					if (http != null) {
						throw new LegolasConfigureError(LOG_VALIDATE_MULTI_HTTP_METHOD);
					} else {
						http = temp;
						httpMethod = annotation;
					}
				}
			}
		}
		Object absoluteObj = false;
		Object pathObj = null;
		if (http == null || httpMethod == null) {
			this.method = null;
			this.supportBody = false;
			throw new LegolasConfigureError(LOG_VALIDATE_NO_HTTP_METHOD);
		} else {
			this.method = http.value();
			this.supportBody = http.supportBody();
			pathObj = getAnnotationValue(httpMethod, http.pathMethod());
			absoluteObj = getAnnotationValue(httpMethod, http.absoluteMethod());
		}
		this.requestPath = pathObj == null ? "" : pathObj.toString();
		parseRequestPathParams();
		
		boolean isAbsolute = false;
		try {
			isAbsolute = Boolean.parseBoolean(absoluteObj.toString());
		} catch (Throwable th) {
		}
		this.absolutePath = isAbsolute; 
		
		if (description != null) {
			this.description = description.value();
		} else {
			this.description = null;
		}
		
		requestType = getRequestType(formUrlEncoded, multipart);
		validateRequestType();
		
		AnnotationHelper.parseInterceptors(this.interceptors, interceptors, config);
		if (interceptors != null) {
			expansionInterceptors = interceptors.expansion();
		} else {
			expansionInterceptors = false;
		}
		
		Type[] parameters = method.getGenericParameterTypes();
		Annotation[][] annotationArray = method.getParameterAnnotations();
		if (parameters != null) {
			for (int i = 0; i < parameters.length; i++) {
				Type type = parameters[i];
				Annotation[] ann = annotationArray[i];
				this.parameters.add(new ParameterDescription(type, ann));
			}
		}
		
		validateRequestParam();
		
		synchronous = hasSynchronousReturnType(resultType);
	}
	
	private void validateRequestParam() {
		Set<String> paths = new HashSet<String>();
		for (ParameterDescription pd : parameters) {
			if (pd.getParameterType() == ParameterType.PATH) {
				paths.add(pd.getName());
			}
			if (!pd.supportRequestType(requestType)) {
				if (requestType == RequestType.FORM_URL_ENCODED) {
					throw new LegolasConfigureError(LOG_VALIDATE_FORMURLENCODED);
				} else if (requestType == RequestType.MULTIPART) {
					throw new LegolasConfigureError(LOG_VALIDATE_MULTIPART);
				} else {
					throw new LegolasConfigureError("default is Simple, Simple request not support @Body、@Fields、@Field、@Parts、@Part, Please use @FormUrlEncoded or ");
				}
			}
		}

		// 验证Path参数，看看链接里边的参数，是不是在配置里边都有
		for (String name : requestPathParamNames) {
			if (!paths.contains(name)) {
				String text = String.format(LOG_VALIDATE_LOST_PATH, name);
				throw new LegolasConfigureError(text);
			}
		}
	}
	
	private void validateRequestType() {
		if (!supportBody) {
			if (requestType == RequestType.FORM_URL_ENCODED
					|| requestType == RequestType.MULTIPART) {
				throw new LegolasConfigureError(LOG_VALIDATE_SIMPLE_REQUEST);
			}
		}
	}
	
	private Http getHttpAnnotation(Annotation methodAnnotation) {
		if (methodAnnotation == null) {
			return null;
		}
		Annotation[] annotations = methodAnnotation.annotationType().getAnnotations();
		if (annotations == null) {
			return null;
		}
		for (Annotation annotation : annotations) {
			if (Http.class == annotation.annotationType()) {
				return (Http) annotation;
			}
		}
		return null;
	}
	
	private Object getAnnotationValue(Annotation methodAnnotation, String methodName) {
		if (methodAnnotation == null 
				|| methodName == null
				|| "".equals(methodName.trim())) {
			return null;
		}
		try {
			Method method = methodAnnotation.annotationType().getMethod(methodName);
			Object result = method.invoke(methodAnnotation);
			return result;
		} catch (NoSuchMethodException e) {
		} catch (SecurityException e) {
		} catch (IllegalAccessException e) {
		} catch (IllegalArgumentException e) {
		} catch (InvocationTargetException e) {
		}
		return null;
	}
	
	
	private RequestType getRequestType(FormUrlEncoded formUrlEncoded, Multipart multipart) {
		if (formUrlEncoded != null && multipart != null) {
			throw new LegolasConfigureError(LOG_VALIDATE_MULTI_REQUEST_TYPE);
		}
		
		RequestType type = RequestType.SIMPLE;
		if (formUrlEncoded != null) {
			type = RequestType.FORM_URL_ENCODED;
		}
		if (multipart != null) {
			type = RequestType.MULTIPART;
		}
		return type;
	}
	
	public static boolean hasSynchronousReturnType(Type responseType) {
		return !(responseType == void.class || responseType == Void.class
				|| responseType == Request.class || responseType == BasicRequest.class);
	}
	
	/**
	 * 解析RequestPath上带有的参数，形式是：{xxx}，不能带空格<br/>
	 * 只解析requestUrl 的？前面的内容<br/>
	 * requestUrl的？后面的内容是RequestQuery，不允许使用{xxx}参数，要想自定义就去在参数上使用@Query
	 * @param path
	 * @return
	 */
	private void parseRequestPathParams() {
		if (requestPath == null || "".equals(requestPath.trim())) {
			//为空，或者压根就没有
			return ;
		}
		//分隔请求链接和Query的参数字符串
		int index = requestPath.indexOf("?");
		String requestQuery = "";
		if (index > -1) {
			requestUrl = requestPath.substring(0, index);
			requestQuery = requestPath.substring(index + 1);
		} else {
			requestUrl = requestPath;
			requestQuery = "";
		}
		
		//分隔Query的参数字符串
		if (requestQuery != null && !"".equals(requestQuery.trim())) {
			String[] querys = requestQuery.split("&");
			if (querys != null && querys.length > 0) {
				for (String string : querys) {
					String[] param = string.split("=", 2);
					if (param != null && param.length == 2) {
						requestQueryMap.put(param[0], param[1]);
					}
				}
			}
		}
		
		//这里只解析？前面的部分，后面的部分算作Query参数，不支持Path参数
		Matcher m = PARAM_URL_REGEX.matcher(requestUrl);
	    String pathName = "";
		while (m.find()) {
			pathName = m.group(1);
			vaildatePathName(pathName);
			requestPathParamNames.add(pathName);
		}
	}

	private void vaildatePathName(String name) {
		if (!PARAM_NAME_REGEX.matcher(name).matches()) {
			throw new LegolasConfigureError("parameter name is not valid: " + name + ". Must match " + PARAM_URL_REGEX.pattern());
		}
	}

	public boolean isSynchronous() {
		return synchronous;
	}

	public RequestType getRequestType() {
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

	public Type getResultType() {
		return resultType;
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

	public boolean isExpansionInterceptors() {
		if (interceptors == null || interceptors.isEmpty()) {
			return true;
		}
		return expansionInterceptors;
	}

	public List<RequestInterceptor> getInterceptors() {
		return interceptors;
	}

	public boolean isAbsolutePath() {
		return absolutePath;
	}

	public boolean isSupportBody() {
		return supportBody;
	}

	public Map<String, String> getRequestQueryMap() {
		return requestQueryMap;
	}
	
	public boolean isIgnore() {
		return method == null || "".equals(method.trim());
	}

	public Type getExceptionType() {
		return exceptionType;
	}
}
