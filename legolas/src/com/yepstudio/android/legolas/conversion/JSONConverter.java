package com.yepstudio.android.legolas.conversion;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.yepstudio.android.legolas.http.Response;
import com.yepstudio.android.legolas.http.mime.RequestBody;
import com.yepstudio.android.legolas.http.mime.ResponseBody;
import com.yepstudio.android.legolas.log.LegolasLog;

/**
 * 解析JSONObject或JSONArray
 * @author zzljob@gmail.com
 * @createDate 2014年1月8日
 */
public class JSONConverter implements Converter {
	private static LegolasLog log = LegolasLog.getClazz(JSONConverter.class);
	private static final Pattern CHARSET = Pattern.compile("\\Wcharset=([^\\s;]+)", CASE_INSENSITIVE);
	private static String default_charset = "UTF-8";

	public static String parseCharset(String mimeType) {
		Matcher match = CHARSET.matcher(mimeType);
		if (match.find()) {
			return match.group(1).replaceAll("[\"\\\\]", "");
		}
		return default_charset;
	}

	@Override
	public Object fromBody(ResponseBody body, Type clazz) throws ConversionException {
		String charset = default_charset;
		if (body.mimeType() != null) {
			charset = parseCharset(body.mimeType());
		}
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
