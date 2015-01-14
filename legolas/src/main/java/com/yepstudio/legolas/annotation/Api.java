package com.yepstudio.legolas.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个接口是网络接口
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月14日
 * @version 2.0，2014年4月23日
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Api {

	/**
	 * 注册整个API的根路径Path
	 * 
	 * @return
	 */
	public abstract String value() default "";

	/**
	 * 是否是绝对路径
	 * 
	 * @return
	 */
	public abstract boolean absolute() default false;

}
