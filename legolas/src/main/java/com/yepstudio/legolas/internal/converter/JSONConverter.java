package com.yepstudio.legolas.internal.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.yepstudio.legolas.ConversionException;
import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.mime.ResponseBody;
import com.yepstudio.legolas.response.Response;

/**
 * 解析JSONObject或JSONArray
 * @author zzljob@gmail.com
 * @create 2014年1月8日
 * @version 2.0，2014年4月23日
 */
public class JSONConverter implements Converter {
	private static LegolasLog log = LegolasLog.getClazz(JSONConverter.class);
	private static String default_charset = "UTF-8";

	@Override
	public Object fromBody(ResponseBody body, Type clazz) throws ConversionException {
		String charset = Response.parseCharset(body.mimeType(), default_charset);
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
			}
		} catch (IOException e) {
			log.e("ConversionException", e);
			throw new ConversionException(e);
		} catch (JSONException e) {
			log.e("ConversionException", e);
			throw new ConversionException(e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ignored) {
				}
			}
		}
		return null;
	}

	@Override
	public RequestBody toBody(Object object) {
		String text = "";
		if (object != null) {
			Class<?> clazz = object.getClass();
			if (clazz.equals(JSONObject.class)) {
				text = ((JSONObject) object).toString();
			} else if (clazz.equals(JSONArray.class)) {
				text = ((JSONArray) object).toString();
			}
		}
		try {
			return new JsonRequestBody(text.getBytes(default_charset), default_charset);
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}
	
	private static class JsonRequestBody implements RequestBody {
		private final byte[] jsonBytes;
		private final String mimeType;

		JsonRequestBody(byte[] jsonBytes, String encode) {
			this.jsonBytes = jsonBytes;
			this.mimeType = "application/json; charset=" + encode;
		}

		@Override
		public String fileName() {
			return null;
		}

		@Override
		public String mimeType() {
			return mimeType;
		}

		@Override
		public long length() {
			return jsonBytes.length;
		}

		@Override
		public void writeTo(OutputStream out) throws IOException {
			out.write(jsonBytes);
		}

	}

}
