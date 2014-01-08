package com.yepstudio.android.legolas.annotation.parameter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * multipart 和 form 之外的请求参数， 将被设置到body<br/>
 * 不能和@FormUrlEncoded 、@Multipart一块使用
 * 
 * @author zzljob@gmail.com
 * @createDate 2014年1月6日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface Body {
	
	/**
	 * 参数名
	 */
	String value();

	/**
	 * 如果format设置了的话，会通过format去查找注册的ParamFormat
	 * 		<ol>
	 * 			<li>没有找到，直接报错</li>
	 * 			<li>找到，通过ParamFormat转成String，再由{@link #StringRequestBody}去转成ASCII</li>
	 * 		</ol>
	 * 
	 * 如果format没有设置，则会直接去找Class对应的Converter
	 * 		<ol>
	 * 			<li>没有找到，则调用toString()转成String，再由{@link #StringRequestBody}去转成ASCII</li>
	 * 			<li>找到，通过Converter去转成ASCII</li>
	 * 		<ol>
	 */
	String format() default "";
}
