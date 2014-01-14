package com.yepstudio.android.legolas.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个接口是网络接口
 * 
 * @author zhangzl@fund123.cn
 * @createDate 2014年1月14日
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Api {

	/**
	 * 接口的名称，选填
	 */
	public String label() default "";

	/**
	 * 注册接口的全局地址，可以不填
	 * <p>
	 * 每一个接口都可能有多种运行，例如 正式，测试，沙箱，配置后可由{@linkplain com.yepstudio.android.legolas.LegolasConfig#API_URL_INDEX}全局修改
	 * </p>
	 */
	public String[] value() default { "" };

	/**
	 * 指定已注册的认证方式， 具体的认证方式参考{@link com.yepstudio.android.legolas.LegolasConfig#register(String, com.yepstudio.android.legolas.handler.AuthHandler)}
	 */
	public String auth() default "";
}
