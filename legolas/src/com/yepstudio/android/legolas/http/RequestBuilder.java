package com.yepstudio.android.legolas.http;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;

import com.yepstudio.android.legolas.LegolasConfig;
import com.yepstudio.android.legolas.description.ApiDescription;
import com.yepstudio.android.legolas.description.RequestDescription;
import com.yepstudio.android.legolas.description.RequestDescription.Parameter;
import com.yepstudio.android.legolas.description.RequestDescription.ParameterType;
import com.yepstudio.android.legolas.description.RequestDescription.RequestType;
import com.yepstudio.android.legolas.handler.ParamFormat;
import com.yepstudio.android.legolas.http.mime.FormUrlEncodedRequestBody;
import com.yepstudio.android.legolas.http.mime.MultipartRequestBody;
import com.yepstudio.android.legolas.http.mime.RequestBody;
import com.yepstudio.android.legolas.http.mime.StringRequestBody;
import com.yepstudio.android.legolas.log.LegolasLog;

public class RequestBuilder {
	
	private static LegolasLog log = LegolasLog.getClazz(RequestBuilder.class);

	private final ApiDescription api;
	private final RequestDescription request;
	
	private final FormUrlEncodedRequestBody formBody;
	private final MultipartRequestBody multipartBody;
	private RequestBody body;
	
	private Request.OnRequestListener onRequestListener; 
	private Response.OnResponseListener<?> responseListener;
	private Response.OnErrorListener onErrorListener;
	
	/**apiURL**/
	private String apiUrl;
	/**相对路径**/
	private String relativeUrl;
	/**?后面的参数**/
	private final StringBuilder query;
	private final Map<String, String> headers;

	public RequestBuilder(ApiDescription api, Method method) {
		super();
		log.v("init");
		this.api = api;
		request = api.getRequestMap().get(method);
		if (request == null) {
			throw new RuntimeException("");
		}
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
		parseApiUrl();
		headers = parseHeaders();
		relativeUrl = request.getRequestUrl();
		query = new StringBuilder(request.getRequestQuery());
	}
	
	private void parseApiUrl() {
		List<String> list = api.getServers();
		int index = LegolasConfig.API_URL_INDEX.get();
		if (index < 0 || index >= list.size()) {
			throw new RuntimeException("API_URL_INDEX is out of apiServers");
		}
		apiUrl =  list.get(index);
		log.v("apiUrl:" + apiUrl);
	}
	
	private Map<String, String> parseHeaders() {
		Map<String, String> map = new HashMap<String, String>();
		map.putAll(api.getHeaders());
		map.putAll(request.getHeaders());
		log.v("parseHeaders:" + map.toString());
		return map;
	}
	
	/**
	 * 将参数都转成String
	 * @param Parameter
	 * @param Object
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static String getParameterValue(Parameter p, Object obj) {
		String name = "";
		ParamFormat pf = null;
		if (TextUtils.isEmpty(p.format)) {
			name = LegolasConfig.getKeyByClass(p.type.getClass());
			pf = LegolasConfig.getParamFormat(name);
		} else {
			name = p.format;
			pf = LegolasConfig.getParamFormat(name);
			if (pf == null) {
				throw new RuntimeException("can not find Register ParamFormat");
			}
		}
		if (pf != null) {
			return pf.format(obj);
		} else {
			return obj == null ? "" : obj.toString();
		}
	}
	
	private void addHeader(String name, String value) {
		headers.put(name, value);
	}
	
	private void addPath(String name, String value) {
		relativeUrl = relativeUrl.replace("{" + name + "}", String.valueOf(value));
	}
	
	private void addQueryParam(String name, String value) {
		int length = query.length();
		if (length > 0 && !"&".equalsIgnoreCase(query.substring(length - 1))) {
			query.append("&");
		}
		query.append(name).append("=").append(value);
	}
	
	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}
	
	public void setArguments(Object[] args) {
		List<Parameter> list = request.getParameters();
		if (list != null) {
			for (Parameter p : list) {
				Object target = args[p.index];
				String name = p.name;
				String value = "";
				switch (p.parameterType) {
				case ParameterType.HEADER:
					addHeader(name, getParameterValue(p, target));
					break;
				case ParameterType.PATH:
					value = getParameterValue(p, target);
					try {
						if (p.encoded) {
							value = URLEncoder.encode(String.valueOf(value), "UTF-8");
							value = value.replace("+", "%20");
						}
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException("Unable to encode path parameter \"" + name + "\" value to UTF-8:" + value, e);
					}
					addPath(name, value);
					break;
				case ParameterType.QUERY:
					value = getParameterValue(p, target);
					try {
						if (p.encoded) {
							value = URLEncoder.encode(String.valueOf(value), "UTF-8");
						}
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException("Unable to encode path parameter \"" + name + "\" value to UTF-8:" + value, e);
					}
					addQueryParam(name, value);
					break;
				case ParameterType.PART:
					if (target != null) { // Skip null values.
						if (target instanceof RequestBody) {
							multipartBody.addPart(name, (RequestBody) target);
						} else if (value instanceof String) {
							multipartBody.addPart(name, new StringRequestBody((String) value));
						} else {
							//multipartBody.addPart(name, converter.toBody(value));
						}
					}
					break;
				case ParameterType.FIELD:
					formBody.addField(name, getParameterValue(p, target));
					break;
				case ParameterType.BODY:
					if (target instanceof RequestBody) {
						body = (RequestBody) target;
					} else {
						// body = converter.toBody(value);
					}
					break;
				case ParameterType.NONE:
					if (setListener(p, args[p.index])) {
						break;
					}
					//处理没有注释的对象(处理无限极对象注释链)
					break;
					
				default:
					
					break;
				}
			}
		}
	}
	
	private boolean setListener(Parameter p, Object obj) {
		if (p.isRequestListener) {
			onRequestListener = (Request.OnRequestListener) obj;
		}
		if (p.isResponseListener) {
			responseListener = (Response.OnResponseListener<?>) obj;
		}
		if (p.isErrorListener) {
			onErrorListener = (Response.OnErrorListener) obj;
		}
		return p.isRequestListener || p.isResponseListener || p.isErrorListener;
	}
	
	public Request build() throws Exception {
		log.v("build");
		String method = request.getMethod();

		URL apiObj = new URL(this.apiUrl);
		URL urlObj = new URL(apiObj, relativeUrl);

		StringBuilder requestUrl = new StringBuilder(urlObj.toString());
		StringBuilder queryParams = this.query;
		if (queryParams.length() > 0) {
			requestUrl.append("?");
			requestUrl.append(queryParams);
		}

		if (multipartBody != null && multipartBody.getPartCount() == 0) {
			throw new IllegalStateException("Multipart requests must contain at least one part.");
		}
		//进行认证
		Request r = new Request(method, requestUrl.toString(), headers, body);
		r.setRequestListener(onRequestListener);
		r.setResponseListener(responseListener);
		r.setErrorListener(onErrorListener);
		r.setResult(request.getResponseType());
		log.v(String.format("RequestListener:%s, ResponseListener:%s, ErrorListener:%s", onRequestListener, responseListener, onErrorListener));
		log.v(String.format("ResponseType:%s", request.getResponseType()));
		return r;
	}

}
