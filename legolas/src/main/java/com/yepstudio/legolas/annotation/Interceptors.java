package com.yepstudio.legolas.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.yepstudio.legolas.RequestInterceptor;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年5月14日
 * @version 2.0, 2014年5月14日
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Interceptors {

	/**
	 * 
	 * @return
	 */
	public abstract Class<? extends RequestInterceptor>[] value();

	/**
	 * 是否把上一层的拦截器也扩展过来
	 * 
	 * @return
	 */
	public abstract boolean expansion() default false;

}
