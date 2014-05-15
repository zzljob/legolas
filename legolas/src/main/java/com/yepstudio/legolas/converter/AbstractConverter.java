package com.yepstudio.legolas.converter;

import java.lang.reflect.Type;

import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.exception.ConversionException;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.mime.ResponseBody;

public abstract class AbstractConverter implements Converter {

	@Override
	public abstract Object fromBody(ResponseBody body, Type clazz) throws ConversionException;

	@Override
	public abstract RequestBody toBody(Object object);

	@Override
	public String toParam(Object object, int type) {
		if (object == null) {
			return "";
		}
		return object.toString();
	}

}
