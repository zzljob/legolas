package com.yepstudio.legolas.internal;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
		List<ParameterDescription> list = request.getParameters();
		for (int i = 0; i < list.size(); i++) {
			ParameterDescription p = list.get(i);
			switch (p.getParameterType()) {
			case ParameterType.HEADER:
				addHeader(p.getName(), argumentToString(arguments[i]));
				break;
			case ParameterType.PATH:
				addPathParam(p.getName(), arguments[i]);
				break;
			case ParameterType.QUERY:
				addQueryParam(p.getName(), arguments[i]);
				break;
			case ParameterType.PART:
				addPartParam(p.getName(), arguments[i]);
				break;
			case ParameterType.FIELD:
				addFieldParam(p.getName(), arguments[i]);
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
			rootPoint = options.getEndpoint();
		} else {
			rootPoint = endpoint;
		}
		return rootPoint;
	}
	
	public String getRequestUrl() {
		StringBuilder builder = new StringBuilder();
		builder.append(api.getApiPath());
		if (!api.getApiPath().endsWith("/") && !request.getRequestPath().startsWith("/")) {
			builder.append("/");
		}
		builder.append(request.getRequestUrl());
		
		Endpoint rootPoint = getEndpoint();
		if (rootPoint == null) {
			throw new IllegalArgumentException("endpoint is null, it must be set, you can set by Legolas or LegolasOptions");
		}
		
		URL url = null;
		try {
			URL endpointURL = new URL(rootPoint.getUrl());
			url = new URL(endpointURL, builder.toString());
		} catch (MalformedURLException e) {
			log.e("endpoint is not a URL", e);
		}
		return url.toString();
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
		if(legolasHeaders != null){
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
	
	private String argumentToString(Object arg) {
		if (arg == null) {
			return "";
		} else {
			return arg.toString();
		}
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
	
	public void addQueryParam(String name, Object value) {
		queryMap.put(name, value);
	}
	
	public void addFieldParam(String name, Object target) {
		formBody.addField(name, argumentToString(target));
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
	
	protected String buildTargetPath() throws UnsupportedEncodingException {
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
				throw new IllegalArgumentException("request lost params : " + name);
			}
			String encodeValue = URLEncoder.encode(value, ENCODE);
			encodeValue = encodeValue.replace("+", "%20");
			path = path.replace(String.format("{%s}", name), encodeValue);
		}
		log.d("path : " + path);
		
		return path;
	}
	
	protected String buildTargetUrl() throws UnsupportedEncodingException, MalformedURLException {
		StringBuilder targetUrl = new StringBuilder();
		targetUrl.append(api.getApiPath());
		if (!api.getApiPath().endsWith("/") && !request.getRequestPath().startsWith("/")) {
			targetUrl.append("/");
		}
		targetUrl.append(buildTargetPath());
		boolean hasQuery = false;
		if (request.getRequestQuery() != null && request.getRequestQuery().trim().length() > 0) {
			targetUrl.append("?");
			targetUrl.append(request.getRequestQuery());
			hasQuery = true;
		}
		if(queryMap != null && queryMap.size() > 0){
			if (hasQuery) {
				targetUrl.append("&");
			} else {
				targetUrl.append("?");
			}
			for (String name : queryMap.keySet()) {
				targetUrl.append(URLEncoder.encode(name, ENCODE));
				targetUrl.append("=");
				targetUrl.append(URLEncoder.encode(converter.toParam(queryMap.get(name), ParameterType.QUERY), ENCODE));
				targetUrl.append("&");
			}
			targetUrl.deleteCharAt(targetUrl.length() - 1);
		}
		
		URL url = null;
		Endpoint endpoint = getEndpoint();
		if (endpoint == null) {
			return targetUrl.toString();
		} else {
			URL endpointURL = new URL(endpoint.getUrl());
			url = new URL(endpointURL, targetUrl.toString());
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
		//进行认证
		Request request = new Request(getRequestDescription(), getRequestMethod(), buildTargetUrl(), headers, body);
		return new RequestWrapper(request, this.request.getResponseType(), converter, onRequestListeners, onResponseListeners, onErrorListeners);
	}
	
}
