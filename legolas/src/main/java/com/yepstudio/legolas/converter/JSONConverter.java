package com.yepstudio.legolas.converter;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.mime.JsonRequestBody;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.mime.ResponseBody;
import com.yepstudio.legolas.response.Response;

/**
 * 解析JSONObject或JSONArray
 * @author zzljob@gmail.com
 * @create 2014年1月8日
 * @version 2.0，2014年4月23日
 */
public class JSONConverter extends BasicConverter {
	
	private static LegolasLog log = LegolasLog.getClazz(JSONConverter.class);
	
	public JSONConverter() {
		this("UTF-8");
	}
	
	public JSONConverter(String defaultCharset) {
		super(defaultCharset);
	}

	@Override
	public Object fromBody(ResponseBody body, Type clazz) throws Exception {
		Object result = null;
		try {
			result = super.fromBody(body, clazz);
			return result;
		} catch (Exception e) {
			
		}
		
		String jsonText;
		if (clazz == JSONObject.class) {
			
			String charset = Response.parseCharset(body.mimeType(), getDefaultCharset());
			log.v("fromBody, charset:" + charset);
			
			jsonText = readToString(body, charset);
			log.d("jsonText : " + jsonText);
			if (jsonText == null || jsonText.length() < 1){
				return null;
			}
			jsonText = subString(jsonText, "{", "}");
			try {
				return new JSONObject(jsonText);
			} catch (JSONException e) {
				throw e;
			}
			
		} else if (clazz == JSONArray.class) {
			
			String charset = Response.parseCharset(body.mimeType(), getDefaultCharset());
			log.v("fromBody, charset:" + charset);
			
			jsonText = readToString(body, charset);
			log.d("jsonText : " + jsonText);
			if (jsonText == null || jsonText.length() < 1){
				return null;
			}
			jsonText = subString(jsonText, "[", "]");
			try {
				return new JSONArray(jsonText);
			} catch (JSONException e) {
				throw e;
			}
			
		} else {
			log.d("JSONConverter is not supported this type : " + clazz);
			throw new Exception("not supported this type : " + clazz);
		}
	}
	
	protected String subString(String string, String start, String end) {
		if (string == null || string.length() < 1) {
			return null;
		}
		int startIndex = string.indexOf(start);
		int endIndex = string.lastIndexOf(end) + 1;
		if (startIndex >= 0 && endIndex > startIndex && endIndex <= string.length()) {
			return string.substring(startIndex, endIndex);
		} else {
			return string;
		}
	}

	@Override
	public RequestBody toBody(Object object) {
		RequestBody body = super.toBody(object);
		if (body != null) {
			return body;
		}
		String jsonText = "";
		if (object instanceof JSONObject) {
			try {
				jsonText = ((JSONObject) object).toString();
				return new JsonRequestBody(getDefaultCharset(), jsonText);
			} catch (UnsupportedEncodingException e) {
			}
		} else if (object instanceof JSONArray) {
			try {
				jsonText = ((JSONArray) object).toString();
				return new JsonRequestBody(getDefaultCharset(), jsonText);
			} catch (UnsupportedEncodingException e) {
			}
		}
		return null;
	}
}
