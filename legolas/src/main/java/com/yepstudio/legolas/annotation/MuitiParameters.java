package com.yepstudio.legolas.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 注释在一个类上面的多参数的参数
 * @author zzljob@gmail.com
 * @create 2014年11月6日
 * @version 1.0，2014年11月6日
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MuitiParameters {
	
}
