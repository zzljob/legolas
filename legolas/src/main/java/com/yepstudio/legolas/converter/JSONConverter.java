package com.yepstudio.legolas.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.exception.ConversionException;
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
public class JSONConverter extends AbstractConverter {
	
	private static LegolasLog log = LegolasLog.getClazz(JSONConverter.class);
	private static String DEFAULT_CHARSET = "UTF-8";

	@Override
	public Object fromBody(ResponseBody body, Type clazz) throws ConversionException {
		String charset = Response.parseCharset(body.mimeType(), DEFAULT_CHARSET);
		log.v("fromBody, charset:" + charset);
		InputStream is = null;
		try {
			is = body.read();
			String str = new String(Response.streamToBytes(is), charset);
			str = str.trim();
			log.v(str);
			if (str.startsWith("{")) {
				return new JSONObject(str);
			} else if (str.startsWith("[")) {
				return new JSONArray(str);
			} else {
				throw new RuntimeException("not supported type");
			}
		} catch (IOException e) {
			log.e("ConversionException", e);
			throw new ConversionException(e);
		} catch (JSONException e) {
			log.e("ConversionException", e);
			throw new ConversionException(e);
		} catch (Throwable e) {
			throw new ConversionException(e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	@Override
	public RequestBody toBody(Object object) {
		String text = "";
		if (object != null) {
			if (object instanceof JSONObject) {
				text = ((JSONObject) object).toString();
			} else if (object instanceof JSONArray) {
				text = ((JSONArray) object).toString();
			} else if (object instanceof String) {
				text = (String) text;
			} else {
				text = object.toString();
			}
		}
		try {
			return new JsonRequestBody(DEFAULT_CHARSET, text);
		} catch (UnsupportedEncodingException e) {
			
		}
		return null;
	}
}
