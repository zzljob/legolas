package com.yepstudio.legolas;

import java.lang.reflect.Type;

import com.yepstudio.legolas.exception.ConversionException;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.mime.ResponseBody;

/**
 * 请求和相应内容的转换接口
 * @author zzljob@gmail.com
 * @create 2014年1月14日
 * @version 2.0，2014年4月23日
 */
public interface Converter {
	
	/**
	   * Convert an HTTP response body to a concrete object of the specified type.
	   *
	   * @param body HTTP response body.
	   * @param type Target object type.
	   * @return Instance of {@code type} which will be cast by the caller.
	   * @throws ConversionException if conversion was unable to complete. This will trigger a call to
	   * {@link retrofit.Callback#failure(retrofit.RetrofitError)} or throw a
	   * {@link retrofit.RetrofitError}. The exception message should report all necessary information
	   * about its cause as the response body will be set to {@code null}.
	   */
	public Object fromBody(ResponseBody body, Type clazz) throws ConversionException;

	  /**
	   * Convert and object to an appropriate representation for HTTP transport.
	   *
	   * @param object Object instance to convert.
	   * @return Representation of the specified object as bytes.
	   */
	  public RequestBody toBody(Object object);
	  
	  public String toParam(Object object, int type);
	  
}
