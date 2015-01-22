package com.yepstudio.legolas.internal;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.Endpoint;
import com.yepstudio.legolas.LegolasOptions;
import com.yepstudio.legolas.ParameterType;
import com.yepstudio.legolas.RequestInterceptorFace;
import com.yepstudio.legolas.RequestType;
import com.yepstudio.legolas.description.ApiDescription;
import com.yepstudio.legolas.description.ParameterDescription;
import com.yepstudio.legolas.description.ParameterItemDescription;
import com.yepstudio.legolas.description.RequestDescription;
import com.yepstudio.legolas.listener.LegolasListener;
import com.yepstudio.legolas.listener.LegolasListenerWrapper;
import com.yepstudio.legolas.mime.FileRequestBody;
import com.yepstudio.legolas.mime.FormUrlEncodedRequestBody;
import com.yepstudio.legolas.mime.MultipartRequestBody;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.mime.StringRequestBody;
import com.yepstudio.legolas.request.OnRequestListener;
import com.yepstudio.legolas.request.AsyncRequest;
import com.yepstudio.legolas.request.SyncRequest;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.OnResponseListener;
import com.yepstudio.legolas.response.ResponseListenerWrapper;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年4月30日
 * @version 2.0, 2014年4月30日
 *
 */
public class RequestBuilder implements RequestInterceptorFace {
	
	private final Endpoint endpoint;
	private final ApiDescription apiDescription;
	private final RequestDescription requestDescription;
	private final Converter converter;
	private final Map<String, String> defaultHeaders;
	
	private final Map<String, String> headerMap = new HashMap<String, String>();
	private final Map<String, String> pathMap = new HashMap<String, String>();
	private final Map<String, String> queryMap = new HashMap<String, String>();
	
	private final List<OnRequestListener> onRequestListeners = new LinkedList<OnRequestListener>();
	private final List<ResponseListenerWrapper> onResponseListeners = new LinkedList<ResponseListenerWrapper>();
	private final List<OnErrorListener> onErrorListeners = new LinkedList<OnErrorListener>();
	private final List<LegolasListenerWrapper> onLegolasListeners = new LinkedList<LegolasListenerWrapper>();
	
	private final FormUrlEncodedRequestBody formBody;
	private final MultipartRequestBody multipartBody;
	
	private final Object[] arguments;
	
	private final StringBuilder requestBuildLog = new StringBuilder();
	
	private String requestPath;
	private RequestBody body;
	private LegolasOptions options;
	
	/**被解析过的URL，也就是请求时候的URL，但是不带Query参数**/
	private String requestFullUrl;
	
	public RequestBuilder(Endpoint endpoint, ApiDescription apiDescription, Method method, Converter converter, LegolasOptions options, Map<String, String> defaultHeaders, Object[] arguments) {
		super();
		
		this.endpoint = endpoint;
		this.apiDescription = apiDescription;
		this.requestDescription = apiDescription.getRequestDescription(method);
		this.defaultHeaders = defaultHeaders;
		this.converter = converter;
		this.options = options;
		this.arguments = arguments;
		
		if (endpoint == null || options == null) {
			throw new NullPointerException("endpoint null, options null");
		}
		
		if (requestDescription.getRequestType() == RequestType.FORM_URL_ENCODED) {
			formBody = new FormUrlEncodedRequestBody();
			body = formBody;
			multipartBody = null;
		} else if (requestDescription.getRequestType() == RequestType.MULTIPART) {
			formBody = null;
			multipartBody = new MultipartRequestBody();
			body = multipartBody;
		} else {
			formBody = null;
			multipartBody = null;
		}
		
		queryMap.putAll(requestDescription.getRequestQueryMap());
		
		appendLogForGlobal(requestBuildLog);
		
		appendLogForRequest(requestBuildLog);
		try {
			parseArguments();
		} catch (Throwable th) {
			throw new IllegalArgumentException("parseArguments fail", th);
		}
		
		appendLogForLegolasOptions(requestBuildLog, options);
		
		parseRequestPath();
		parseRequestUrl();
	}
	
	private boolean isEmpty(String string) {
		return string == null || "".equalsIgnoreCase(string.trim());
	}
	
	private void appendLogForGlobal(StringBuilder builder) {
		builder.append("\n-------------------Global>>-------------------\n");
		if (defaultHeaders == null || defaultHeaders.isEmpty()) {
			builder.append("Headers : none \n");
			return ;
		}
		builder.append("Headers : \n");
		for (String key : defaultHeaders.keySet()) {
			builder.append(key).append("=").append(defaultHeaders.get(key));
			builder.append("\n");
		}
	}
	
	private void appendLogForLegolasOptions(StringBuilder builder, LegolasOptions options) {
		builder.append("[LegolasOptions] : \n");
		builder.append(this.options.toString());
		if (this.options != options) {
			builder.append("[").append("from Arguments").append("]");
		} else {
			builder.append("[").append("from Configuration").append("]");
		}
		builder.append("\n");
	}
	
	private void appendLogForRequest(StringBuilder builder) {
		builder.append("-------------------Init>>-------------------\n");
		builder.append("Endpoint：").append(endpoint.getRequestUrl());
		if (!isEmpty(endpoint.getName())) {
			builder.append("(").append(endpoint.getName()).append(")");
		}
		
		builder.append("\n");
		
		builder.append("Api：").append(apiDescription.getApiPath());
		if (!isEmpty(apiDescription.getDescription())) {
			builder.append("(").append(apiDescription.getDescription()).append(")");
		}
		
		builder.append("\n");
		
		builder.append("Request：").append(getRequestMethod()).append("==>");
		builder.append(requestDescription.getRequestPath());
		if (!isEmpty(requestDescription.getDescription())) {
			builder.append("(").append(requestDescription.getDescription()).append(")");
		}
		builder.append("\n");
	}
	
	private void parseRequestPath() {
		String fullPath = requestDescription.getRequestUrl();
		Set<String> pathNames = requestDescription.getRequestPathParamNames();
		if (pathNames == null || pathNames.isEmpty()) {
			//do nothing
		} else {
			for (String pathName : pathNames) {
				if (isEmpty(pathName) || !pathMap.containsKey(pathName)) {
					continue;
				}
				String value = object2String(pathMap.get(pathName));
				String encodeValue = encodeValue(value);
				fullPath = fullPath.replaceAll("{" + pathName + "}", encodeValue);
			}
		}
		requestPath = fullPath;
	}
	
	private String object2String(Object obj) {
		if (obj == null) {
			return "";
		}
		return obj.toString();
	}
	
	private String object2String(Type type, Object object) {
		return object2String(object);
	}
	
	private RequestBody object2body(Type type, Object object) throws IOException {
		if (object == null) {
			return new StringRequestBody("", options.getRequestCharset());
		}
		if (type == null) {
			return new StringRequestBody(object2String(object), options.getRequestCharset());
		}
		Class<?> clazz = TypesHelper.getRawType(type);
		if (clazz.isAssignableFrom(RequestBody.class)) {
			return (RequestBody) object;
		} else if (clazz.isAssignableFrom(File.class)) {
			return new FileRequestBody((File) object);
		} else {
			return new StringRequestBody(object2String(type, object), options.getRequestCharset());
		}
	}
	
	private void parseClassParameter(ParameterDescription pd, Object obj) throws IOException {
		List<ParameterItemDescription> itemList = pd.getParameterItems();
		if (itemList != null && !itemList.isEmpty()) {
			for (ParameterItemDescription itemDescription : itemList) {
				if (itemDescription == null || itemDescription.isIgnore()) {
					continue;
				}
				String value = "";
				if (itemDescription.getParameterType() == ParameterType.HEADER) {
					value = object2String(itemDescription.getValueType(), itemDescription.getValue(obj));
					headerMap.put(itemDescription.getName(), object2String(itemDescription.getValueType(), itemDescription.getValue(obj)));
				} else if (itemDescription.getParameterType() == ParameterType.QUERY) {
					value = object2String(itemDescription.getValueType(), itemDescription.getValue(obj));
					queryMap.put(itemDescription.getName(), value);
				} else if (itemDescription.getParameterType() == ParameterType.PATH) {
					value = object2String(itemDescription.getValueType(), itemDescription.getValue(obj));
					pathMap.put(pd.getName(), value);
				} else if (itemDescription.getParameterType() == ParameterType.PART) {
					RequestBody partBody = object2body(itemDescription.getValueType(), itemDescription.getValue(obj));
					if (multipartBody != null) {
						value = getRequestBodyDescription(partBody);
						multipartBody.addPart(pd.getName(), partBody);
					}
				} else if (itemDescription.getParameterType() == ParameterType.FIELD) {
					if (formBody != null) {
						value = object2String(itemDescription.getValueType(), itemDescription.getValue(obj));
						formBody.addOrReplaceField(itemDescription.getName(), value);
					}
				}
				appendLogForParameter(requestBuildLog, pd, itemDescription, value);
			}
		}
	}
	
	private void parseArguments() throws IOException {
		//先把LegolasOptions参数处理了，因为这个会涉及到解析参数的配置
		List<ParameterDescription> list = requestDescription.getParameters();
		for (int i = 0; i < list.size(); i++) {
			ParameterDescription pd = list.get(i);
			//如果LegolasOptions被加上了@Header@Path@Query@Part@Field@Body的注释，则会被当做参数处理
			if (pd.getParameterType() == ParameterType.NONE 
					&& pd.isOptions()
					&& arguments[i] != null) {
				this.options = (LegolasOptions) arguments[i];
			}
		}
		
		appendLogForStartParameter(requestBuildLog);
		if (arguments == null || arguments.length < 1) {
			return ;
		}
		for (int i = 0; i < arguments.length; i++) {
			ParameterDescription pd = list.get(i);
			Object objArg = arguments[i];
			
			if (pd.getParameterType() == ParameterType.HEADER) {
				String value = object2String(pd.getResponseType(), objArg);
				headerMap.put(pd.getName(), value);
				appendLogForParameter(requestBuildLog, pd, value);
			} else if (pd.getParameterType() == ParameterType.PATH) {
				String value = object2String(pd.getResponseType(), objArg);
				pathMap.put(pd.getName(), value);
				appendLogForParameter(requestBuildLog, pd, value);
			} else if (pd.getParameterType() == ParameterType.QUERY) {
				String value = object2String(pd.getResponseType(), objArg);
				queryMap.put(pd.getName(), value);
				appendLogForParameter(requestBuildLog, pd, value);
			} else if (pd.getParameterType() == ParameterType.PART) {
				RequestBody partBody = object2body(pd.getResponseType(), objArg);
				if (multipartBody != null && partBody !=null) {
					String value = getRequestBodyDescription(partBody);
					appendLogForParameter(requestBuildLog, pd, value);
					multipartBody.addPart(pd.getName(), partBody);
				}
			} else if (pd.getParameterType() == ParameterType.FIELD) {
				if (formBody != null) {
					String value = object2String(pd.getResponseType(), objArg);
					formBody.addOrReplaceField(pd.getName(), value);
					appendLogForParameter(requestBuildLog, pd, value);
				}
			} else if (pd.getParameterType() == ParameterType.BODY) {
				body = object2body(pd.getResponseType(), objArg);
				String value = getRequestBodyDescription(body);
				appendLogForParameter(requestBuildLog, pd, value);
			} else {
				//处理一些没有注释的参数
				//首先就是一些带有@MuitiParameters的参数
				if (pd.isClassParameter()) {
					parseClassParameter(pd, objArg);
				}
				
				//处理Listener
				if (pd.isLegolasListener()) {
					Type resultType = pd.getResponseType();
					Type errorType = pd.getErrorType();
					LegolasListener<?, ?> listener = (LegolasListener<?, ?>) objArg;
					LegolasListenerWrapper wrapper = new LegolasListenerWrapper(listener, resultType, errorType);
					onLegolasListeners.add(wrapper);
				}
				if (pd.isErrorListener()) {
					onErrorListeners.add((OnErrorListener) objArg);
				}
				if (pd.isRequestListener()) {
					onRequestListeners.add((OnRequestListener) objArg);
				}
				if (pd.isResponseListener()) {
					OnResponseListener<?> listener = (OnResponseListener<?>)objArg;
					Type response = pd.getResponseType();
					ResponseListenerWrapper wrapper = new ResponseListenerWrapper(listener, response); 
					onResponseListeners.add(wrapper);
				}
			}
		}
	}
	
	private void appendLogForStartParameter(StringBuilder builder) {
		builder.append("Params：\n");
	}
	
	private void appendLogForParameter(StringBuilder builder, ParameterDescription pd, ParameterItemDescription itemDescription, String value) {
		builder.append("[").append(itemDescription.getParameterType()).append("]");
		builder.append("[").append(pd.getType()).append("]");
		builder.append(itemDescription.getName());
		if (!isEmpty(itemDescription.getDescription())) {
			builder.append("(").append(itemDescription.getDescription()).append(")");
		}
		builder.append("=").append(value);
		builder.append("\n");
	}
	
	private void appendLogForParameter(StringBuilder builder, ParameterDescription pd, String value) {
		builder.append("[").append(pd.getParameterType()).append("]");
		builder.append(pd.getName());
		if (!isEmpty(pd.getDescription())) {
			builder.append("(").append(pd.getDescription()).append(")");
		}
		builder.append("=").append(value);
		builder.append("\n");
	}
	
	private String getRequestBodyDescription(RequestBody body) {
		if(body == null){
			return "";
		}
		StringBuilder value = new StringBuilder();
		if (body instanceof StringRequestBody) {
			StringRequestBody stringBody = (StringRequestBody) body;
			value.append(stringBody.getString());
			value.append("[").append(stringBody.getCharset()).append("]");
		} else if (body instanceof FileRequestBody) {
			FileRequestBody fileBody = (FileRequestBody) body;
			value.append(fileBody.fileName());
		} else {
			value.append(body.toString());
		}
		return value.toString();
	}
	
	private void parseRequestUrl() {
		StringBuilder builder = new StringBuilder();
		if (requestDescription.isAbsolutePath()) {
			builder.append(requestPath);
		} else {
			if(apiDescription.isAbsoluteApiPath()){
				builder.append(apiDescription.getApiPath());
			} else {
				builder.append(endpoint.getRequestUrl());
				if ((endpoint != null && endpoint.getRequestUrl() != null && endpoint.getRequestUrl().endsWith("/"))
						|| (apiDescription.getApiPath() != null && apiDescription.getApiPath().startsWith("/"))) {
					//都不需要加/
				} else {
					builder.append("/");
				}
				builder.append(apiDescription.getApiPath());
			}
			if (!requestPath.startsWith("/") && !builder.toString().endsWith("/")) {
				builder.append("/");
			}
			builder.append(requestPath);
		}
		
		requestFullUrl = builder.toString();
	}
	
	private String applyQuery(String requestUrl) {
		if (queryMap.isEmpty()) {
			return requestUrl;
		}
		// 添加Query参数
		StringBuilder builder = new StringBuilder(requestUrl);
		builder.append("?");
		for (String key : queryMap.keySet()) {
			builder.append(encodeValue(key));
			builder.append("=");
			builder.append(encodeValue(queryMap.get(key)));
			builder.append("&");
		}
		builder.deleteCharAt(builder.length() - 1);
		return builder.toString();
	}
	
	public String getRequestUrl() {
		return requestFullUrl;
	}
	
	public String getRequestMethod() {
		return requestDescription.getMethod();
	}
	
	public RequestType getRequestType() {
		return requestDescription.getRequestType();
	}
	
	protected String encodeValue(String string) {
		if (isEmpty(string)) {
			return "";
		}
		try {
			return URLEncoder.encode(string, getEncode());
		} catch (UnsupportedEncodingException e) {
			
		}
		return string;
	}
	
	private String makeRequestDescription() {
		StringBuilder builder = new StringBuilder();
		builder.append("Api : ");
		builder.append(apiDescription.getApiPath());
		if (!isEmpty(apiDescription.getDescription())) {
			builder.append("(").append(apiDescription.getDescription()).append(")");
		}
		builder.append("->");
		builder.append(requestDescription.getRequestPath());
		if (!isEmpty(requestDescription.getDescription())) {
			builder.append("(").append(requestDescription.getDescription()).append(")");
		}
		return builder.toString();
	}
	
	private Map<String, String> aggregationHeader() {
		Map<String, String> header = new ConcurrentHashMap<String, String>();
		if (defaultHeaders != null) {
			for (String key : defaultHeaders.keySet()) {
				header.put(key, defaultHeaders.get(key));
			}
		}
		if (headerMap != null) {
			for (String key : headerMap.keySet()) {
				header.put(key, headerMap.get(key));
			}
		}
		if (!requestDescription.isAbsolutePath()) {
			header.put("Host", endpoint.getHost());
		}
		return header;
	}
	
	public SyncRequest buildSyncRequest() {
		String url = applyQuery(getRequestUrl());
		String method = getRequestMethod();
		String description = makeRequestDescription();
		Map<String, String> header = aggregationHeader();
		
		SyncRequest request = null;
		if (requestDescription.isSynchronous()) {
			Type result = requestDescription.getResultType();
			Type error = requestDescription.getExceptionType();
			request = new SyncRequest(url, method, description, header, body, this.options, this.converter, result, error);
			request.appendLog(requestBuildLog.toString());
		}
		return request;
	}
	
	public AsyncRequest buildAsyncRequest() {
		if (requestDescription.isSynchronous()) {
			return null;
		}
		
		String url = applyQuery(getRequestUrl());
		String method = getRequestMethod();
		String description = makeRequestDescription();
		Map<String, String> header = aggregationHeader();
		
		return new AsyncRequest(url, method, description, header, body,
				this.options, this.converter, onRequestListeners,
				onResponseListeners, onErrorListeners, onLegolasListeners);
	}

	@Override
	public String getEncode() {
		return options.getRequestCharset();
	}

	@Override
	public RequestBody getBody() {
		return body;
	}

	@Override
	public Map<String, String> getHeaders() {
		return headerMap;
	}

	@Override
	public Map<String, String> getQuerys() {
		return queryMap;
	}
	
}
