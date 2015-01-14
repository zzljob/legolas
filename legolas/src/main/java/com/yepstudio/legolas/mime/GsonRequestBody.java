package com.yepstudio.legolas.mime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年5月15日
 * @version 2.0, 2014年5月15日
 *
 */
public final class GsonRequestBody extends StringRequestBody {

	private final Gson gson;
	private final Object object;

	public GsonRequestBody(Object object) {
		this(object, "UTF-8");
	}
	
	public GsonRequestBody(Object object,String charset) {
		this("text/plain", new GsonBuilder().create(), object, charset, 2048);
	}

	public GsonRequestBody(String mimeType, Gson gson, Object object, String charset, int bufferSize) {
		super(mimeType, parseJson(object, gson), charset, bufferSize);
		this.object = object;
		this.gson = gson;
	}

	private static String parseJson(Object object, Gson gson) {
		if (gson == null) {
			throw new IllegalArgumentException("gson can not be null");
		}
		if (object == null) {
			return "";
		}
		return gson.toJson(object);
	}

	public Gson getGson() {
		return gson;
	}

	public Object getObject() {
		return object;
	}
}
