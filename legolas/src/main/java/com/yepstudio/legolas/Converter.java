package com.yepstudio.legolas;

import java.lang.reflect.Type;

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

	public Object fromBody(ResponseBody body, Type clazz) throws Exception;

	/**
	 * <ul>
	 * <li>ParameterDescription.ParameterType.PART</li>
	 * <li>ParameterDescription.ParameterType.BODY</li>
	 * </ul>
	 */
	public RequestBody toBody(Object object);

	/**
	 * <ul>
	 * <li>ParameterDescription.ParameterType.HEADER</li>
	 * <li>ParameterDescription.ParameterType.PATH</li>
	 * <li>ParameterDescription.ParameterType.QUERY</li>
	 * <li>ParameterDescription.ParameterType.FIELD</li>
	 * </ul>
	 */
	public String toParam(Object object, int type);

}
