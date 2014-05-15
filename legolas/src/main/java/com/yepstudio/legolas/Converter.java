package com.yepstudio.legolas;

import java.lang.reflect.Type;

import com.yepstudio.legolas.exception.ConversionException;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.mime.ResponseBody;

/**
 * 请求和相应内容的转换接口
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月14日
 * @version 2.0，2014年4月23日
 */
public interface Converter {

	public Object fromBody(ResponseBody body, Type clazz) throws ConversionException;

	public RequestBody toBody(Object object);

	public String toParam(Object object, int type);

}
