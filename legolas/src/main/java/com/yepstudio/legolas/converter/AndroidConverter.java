package com.yepstudio.legolas.converter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.yepstudio.legolas.exception.ConversionException;
import com.yepstudio.legolas.mime.ByteArrayResponseBody;
import com.yepstudio.legolas.response.Response;

/**
 * 解析JSONObject或JSONArray
 * @author zzljob@gmail.com
 * @create 2014年1月8日
 * @version 2.0，2014年4月23日
 */
public class AndroidConverter extends BasicConverter {
	
	public AndroidConverter() {
		this("UTF-8");
	}
	
	public AndroidConverter(String defaultCharset) {
		super(defaultCharset);
	}
	
	public boolean isSupport(Type type) {
		if (JSONObject.class.equals(type)) {
			return true;
		} else if (JSONArray.class.equals(type)) {
			return true;
		} else if (Bitmap.class.equals(type)) {
			return true;
		} else {
			return super.isSupport(type);
		}
	}
	
	public Object convert(Response response, Type type) throws ConversionException {
		String charset = null;
		try {
			if (JSONObject.class.equals(type)) {
				String jsonText = getJsonText(response);
				if (jsonText == null || jsonText.length() < 1){
					return null;
				}
				jsonText = subString(jsonText, "{", "}");
				return new JSONObject(jsonText);
			} else if (JSONArray.class.equals(type)) {
				String jsonText = getJsonText(response);
				jsonText = subString(jsonText, "[", "]");
				return new JSONArray(jsonText);
			} else if (Bitmap.class.equals(type)) {
				return BitmapFactory.decodeStream(response.getBody().read());
			} else {
				return super.convert(response, type);
			}
		} catch (UnsupportedEncodingException e) {
			throw generateException(response, type, "Unsupported Encoding : " + charset, e);
		} catch (IOException e) {
			throw generateException(response, type, "has IOException", e);
		} catch (JSONException e) {
			throw generateException(response, type, "convert JSON has Exception", e);
		} 
	}
	
	protected String getJsonText(Response response) throws UnsupportedEncodingException, IOException  {
		String charset = response.parseCharset(getDefaultCharset());
		ByteArrayResponseBody body = ByteArrayResponseBody.build(response.getBody());
		return new String(body.getBytes(), charset);
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
}
