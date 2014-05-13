package com.yepstudio.legolas.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多个Query参数，只支持Map，JavaObject，会解析成key-value对象
 * 
 * @author zzljob@gmail.com
 * @create 2014年5月13日
 * @version 2.0, 2014年5月13日
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Querys {
}
