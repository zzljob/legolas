package com.yepstudio.legolas.converter;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Date;

import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.description.ParameterDescription.ParameterType;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.mime.ResponseBody;

public abstract class AbstractConverter implements Converter {
	
	private final String defaultCharset;

	public AbstractConverter(String defaultCharset) {
		super();
		this.defaultCharset = defaultCharset;
	}

	@Override
	public abstract Object fromBody(ResponseBody body, Type clazz) throws Exception;

	@Override
	public abstract RequestBody toBody(Object object);
	
	@Override
	public String toParam(Object object, int type) {
		String result = "";
		if (object == null) {
			result = "";
		} else {
			if (object instanceof Date) {
				result = String.valueOf(((Date) object).getTime());
			} else {
				result = object.toString();
			}
		}
		if (type == ParameterType.QUERY || type == ParameterType.PATH) {
			return URLEncode(result, defaultCharset);
		} else {
			return result;
		}
	}
	
	protected String URLEncode(String result, String charset) {
		try {
			return URLEncoder.encode(result, charset);
		} catch (UnsupportedEncodingException e) {
			return result;
		}
	}

	public String getDefaultCharset() {
		return defaultCharset;
	}

}
