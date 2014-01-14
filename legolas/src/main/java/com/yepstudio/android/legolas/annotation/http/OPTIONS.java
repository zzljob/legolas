package com.yepstudio.android.legolas.annotation.http;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 进行OPTIONS请求，允许客户端查看服务器的性能
 * 
 * @author zzljob@gmail.com
 * @createDate 2014年1月6日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Http("OPTIONS")
public @interface OPTIONS {
	/**
	 * 接口的名字
	 */
	public String label() default "";

	/**
	 * 请求地址
	 */
	public String value();

	/**
	 * 认证方式
	 */
	public String auth() default "";
}
