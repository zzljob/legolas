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
	private static String DEFAULT_CHARSET = "UTF-8";

	@Override
	public Object fromBody(ResponseBody body, Type clazz) throws Exception {
		String charset = Response.parseCharset(body.mimeType(), DEFAULT_CHARSET);
		log.v("fromBody, charset:" + charset);
		String jsonText;
		if (clazz == JSONObject.class) {
			jsonText = (String) super.fromBody(body, String.class);
			log.d("jsonText : " + jsonText);
			jsonText = jsonText.substring(jsonText.indexOf("{"), jsonText.lastIndexOf("}")  + 1);
			try {
				return new JSONObject(jsonText);
			} catch (JSONException e) {
				throw e;
			}
		} else if (clazz == JSONArray.class) {
			jsonText = (String) super.fromBody(body, String.class);
			jsonText = jsonText.substring(jsonText.indexOf("["), jsonText.lastIndexOf("]")  + 1);
			try {
				return new JSONArray(jsonText);
			} catch (JSONException e) {
				throw e;
			}
		} else {
			return super.fromBody(body, clazz);
		}
	}

	@Override
	public RequestBody toBody(Object object) {
		String jsonText = "";
		if (object != null) {
			if (object instanceof JSONObject) {
				jsonText = ((JSONObject) object).toString();
			} else if (object instanceof JSONArray) {
				jsonText = ((JSONArray) object).toString();
			} else {
				return super.toBody(object);
			}
		}
		try {
			return new JsonRequestBody(DEFAULT_CHARSET, jsonText);
		} catch (UnsupportedEncodingException e) {
			
		}
		return null;
	}
}
