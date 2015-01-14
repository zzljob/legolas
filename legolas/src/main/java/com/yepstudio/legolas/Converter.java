package com.yepstudio.legolas;

import java.lang.reflect.Type;

import com.yepstudio.legolas.exception.ConversionException;
import com.yepstudio.legolas.response.Response;

/**
 * 请求和相应内容的转换接口
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月14日
 * @version 2.0，2014年4月23日
 */
public interface Converter {

	public Object convert(Response response, Type type) throws ConversionException;
	
	public boolean isSupport(Type type);

}
