package com.yepstudio.legolas.converter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.mime.JsonRequestBody;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.mime.ResponseBody;
import com.yepstudio.legolas.response.Response;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年5月5日
 * @version 2.0, 2014年5月5日
 *
 */
public class GsonConverter extends BasicConverter {
	private static LegolasLog log = LegolasLog.getClazz(GsonConverter.class);

	private final Gson gson;
	private String encoding;

	public GsonConverter() {
		this(new GsonBuilder().create(), "UTF-8");
	}

	public GsonConverter(Gson gson) {
		this(gson, "UTF-8");
	}

	public GsonConverter(Gson gson, String encoding) {
		this.gson = gson;
		this.encoding = encoding;
	}

	@Override
	public Object fromBody(ResponseBody body, Type type) throws Exception {
		log.d("fromBody:[" + type + "]");
		String charset = Response.parseCharset(body.mimeType(), encoding);
		log.d("charset:[" + charset + "]");
		InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(body.read(), charset);
			return gson.fromJson(isr, type);
		} catch (IOException e) {
			throw e;
		} catch (JsonParseException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		} finally {
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	@Override
	public RequestBody toBody(Object object) {
		try {
			return new JsonRequestBody(gson.toJson(object), encoding);
		} catch (UnsupportedEncodingException e) {
			
		}
		return null;
	}

}
