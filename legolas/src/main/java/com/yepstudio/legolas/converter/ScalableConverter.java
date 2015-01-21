package com.yepstudio.legolas.converter;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.Legolas;
import com.yepstudio.legolas.exception.ConversionException;
import com.yepstudio.legolas.response.Response;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2015年1月20日
 * @version 1.0，2015年1月20日
 *
 */
public class ScalableConverter implements Converter {

	private List<Converter> converters = new LinkedList<Converter>();
	
	public void expand(Converter converter) {
		converters.add(converter);
	}

	@Override
	public Object convert(Response response, Type type) throws ConversionException {
		ConversionException throwE = null;
		for (Converter converter : converters) {
			if (converter == null || !converter.isSupport(type)) {
				continue;
			}
			try {
				return converter.convert(response, type);
			} catch (ConversionException e) {
				throwE = e;
				Legolas.getLog().w("convert failed ", e);
			} 
		}
		throw generateException(response, type, "convert failed", throwE);
	}
	
	protected ConversionException generateException(Response response, Type type, String message, Exception e) {
		ConversionException ce;
		if (e == null) {
			ce = new ConversionException(message);
		} else {
			ce = new ConversionException(message, e);
		}
		ce.setResponse(response);
		ce.setConversionType(type);
		return ce;
	}

	@Override
	public boolean isSupport(Type type) {
		for (Converter converter : converters) {
			if (converter != null && converter.isSupport(type)) {
				return true;
			}
		}
		return false;
	}

}
