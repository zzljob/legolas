package com.yepstudio.android.legolas.annotation.parameter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * multipart请求的参数，只能配合{@link @Multipart }使用
 * 
 * @author zzljob@gmail.com
 * @createDate 2014年1月6日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Part {
	
	/**
	 * 参数名
	 * @return
	 */
	String value();

	/**
	 * 如果format设置了的话，会通过format去查找注册的ParamFormat
	 * 		<ol>
	 * 			<li>没有找到，直接报错</li>
	 * 			<li>找到，通过ParamFormat转成String，再由处理String的Converter去转成ASCII</li>
	 * 		</ol>
	 * 
	 * 如果format没有设置，则会直接去找Class对应的Converter
	 * 		<ol>
	 * 			<li>没有找到，普通对象则调用toString()转成String，再由StringRequestBody去转成ASCII</li>
	 * 			<li>没有找到，文件对象则由FileBody去转成ASCII</li>
	 * 			<li>找到，通过Converter去转成ASCII</li>
	 * 		<ol>
	 */
	String format() default "";
}
