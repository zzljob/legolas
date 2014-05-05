package com.yepstudio.legolas.internal.converter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.yepstudio.legolas.ConversionException;
import com.yepstudio.legolas.Converter;
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
public class GsonConverter implements Converter {

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
	public Object fromBody(ResponseBody body, Type type) throws ConversionException {
		String charset = Response.parseCharset(body.mimeType(), encoding);
		InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(body.read(), charset);
			return gson.fromJson(isr, type);
		} catch (IOException e) {
			throw new ConversionException(e);
		} catch (JsonParseException e) {
			throw new ConversionException(e);
		} catch (Exception e) {
			throw new ConversionException(e);
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
			return new JsonRequestBody(gson.toJson(object).getBytes(encoding), encoding);
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
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

	@Override
	public String toParam(Object object, int type) {
		if (object == null) {
			return "";
		}
		return object.toString();
	}

}
