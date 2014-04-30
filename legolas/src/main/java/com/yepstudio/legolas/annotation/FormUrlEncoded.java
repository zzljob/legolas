package com.yepstudio.legolas.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 有该注释的请求，支持的参数：@Header，@Path，@Query，@Field
 * @author zzljob@gmail.com
 * @create 2014年1月8日
 * @version 2.0，2014年4月23日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FormUrlEncoded {
	
}
