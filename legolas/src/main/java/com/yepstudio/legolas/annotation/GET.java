package com.yepstudio.legolas.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 进行GET请求
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月6日
 * @version 2.0, 2014年4月23日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Http(value = "GET", httpValue = "value", supportBody = false)
public @interface GET {

	/**
	 * 请求地址
	 */
	public abstract String value();

}