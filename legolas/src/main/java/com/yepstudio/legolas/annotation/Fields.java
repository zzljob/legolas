package com.yepstudio.legolas.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * form请求的参数，只能配合{@link @FormUrlEncoded }使用
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月6日
 * @version 2.0，2014年4月23日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface Fields {
}
