package com.yepstudio.legolas.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在请求过程中添加的Header，优先级高于用于类注释和方法注释上的@Headers
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月8日
 * @version 2.0，2014年4月29日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD })
public @interface Header {

	/**
	 * 参数名
	 */
	public abstract String value() default "";
}
