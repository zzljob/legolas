package com.yepstudio.android.legolas.annotation.parameter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * form请求的参数，只能配合{@link @FormUrlEncoded }使用
 * @author zzljob@gmail.com
 * @createDate 2014年1月6日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface Field {
	
	/**
	 * 参数名
	 */
	String value();

	/**
	 * 如果format设置了的话，会通过format去查找注册的ParamFormat
	 * 		<ol>
	 * 			<li>没有找到，直接报错</li>
	 * 			<li>找到，通过ParamFormat转成String</li>
	 * 		</ol>
	 * 如果format没有设置 则调用toString()转成String
	 */
	String format() default "";
}
