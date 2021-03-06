package com.yepstudio.legolas.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 进行OPTIONS请求，请求查询服务器的性能，或查询与资源相关的选项和需求
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月6日
 * @version 2.0, 2014年10月27日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Http("OPTIONS")
public @interface OPTIONS {

	/**
	 * 请求地址
	 */
	public String value();

}
