package com.yepstudio.legolas.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 进行PATCH请求，实体中包含一个表，表中说明与该URI所表示的原内容的区别。
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月6日
 * @version 2.0, 2014年4月23日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Http(value = "TRACE", supportBody = true)
public @interface TRACE {
	/**
	 * 请求地址
	 */
	public String value();
}
