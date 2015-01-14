package com.yepstudio.legolas.converter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.yepstudio.legolas.exception.ConversionException;
import com.yepstudio.legolas.mime.ByteArrayResponseBody;
import com.yepstudio.legolas.response.Response;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2015年1月4日
 * @version 1.0，2015年1月4日
 *
 */
public class GsonConverter extends BasicConverter {

	protected final Gson gson;
	protected boolean debug;

	public GsonConverter() {
		this(new GsonBuilder().create());
	}

	public GsonConverter(Gson gson) {
		this(gson, "UTF-8");
	}
	
	public GsonConverter(Gson gson, String encoding) {
		super(encoding);
		this.gson = gson;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public boolean isSupport(Type type) {
		return true;
	}
	
	public Object convert(Response response, Type type) throws ConversionException {
		if (super.isSupport(type)) {
			return super.convert(response, type);
		} else {
			String charset = response.parseCharset(getDefaultCharset());
			InputStreamReader isr = null;
			try {
				if (debug) {
					ByteArrayResponseBody body = ByteArrayResponseBody.build(response.getBody());
					String bodyString = new String(body.getBytes(), charset);
					// 输出日志
					return gson.fromJson(bodyString, type);
				} else {
					isr = new InputStreamReader(response.getBody().read(), charset);
					return gson.fromJson(isr, type);
				}
			} catch (IOException e) {
				throw generateException(response, type, "has IOException", e);
			} catch (JsonParseException e) {
				throw generateException(response, type, "convert to Object error", e);
			} finally {
				if (isr != null) {
					try {
						isr.close();
					} catch (IOException ignored) {
					}
				}
			}
		}
	}

}
