package com.yepstudio.legolas.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 跟在URL的？后面的参数
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月6日
 * @version 2.0，2014年4月23日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD })
public @interface Query {

	/**
	 * 参数名
	 */
	public abstract String value() default "";

}
