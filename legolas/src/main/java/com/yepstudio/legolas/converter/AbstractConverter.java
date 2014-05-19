package com.yepstudio.legolas.converter;

import java.lang.reflect.Type;
import java.util.Date;

import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.exception.ConversionException;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.mime.ResponseBody;

public abstract class AbstractConverter implements Converter {
	
	//private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	@Override
	public abstract Object fromBody(ResponseBody body, Type clazz) throws ConversionException;

	@Override
	public abstract RequestBody toBody(Object object);

	
	@Override
	public String toParam(Object object, int type) {
		if (object == null) {
			return "";
		}
		if (object instanceof Date) {
			return String.valueOf(((Date) object).getTime());
		}
		return object.toString();
	}

}
