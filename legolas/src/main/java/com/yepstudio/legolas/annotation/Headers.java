package com.yepstudio.legolas.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在请求过程中添加的Headers，优先级低于用于参数上的@Header
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月8日
 * @version 2.0, 2014年4月23日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Headers {

	public Item[] value();
}
