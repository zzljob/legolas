package com.yepstudio.android.legolas;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.yepstudio.android.legolas.conversion.Converter;
import com.yepstudio.android.legolas.handler.AuthHandler;
import com.yepstudio.android.legolas.handler.ParamFormat;
import com.yepstudio.android.legolas.log.LegolasLog;

/**
 * 用于注册各种处理方式，包括：
 * <ol>
 * <li>@API的全局默认环境，{@linkplain LegolasConfig.API_URL_INDEX}</li>
 * <li>认证方式，要求实现接口{@link com.yepstudio.android.legolas.handler.AuthHandler}</li>
 * <li>参数格式化方式，要求实现接口{@link com.yepstudio.android.legolas.handler.ParamFormat}</li>
 * <li>转换方式，要求实现接口{@link com.yepstudio.android.legolas.conversion.Converter}</li>
 * </ol>
 * @author zhangzl@fund123.cn
 * @createDate 2014年1月14日
 */
public class LegolasConfig {
	private static LegolasLog log = LegolasLog.getClazz(LegolasConfig.class);
	
	/**
	 * 设置Api的类型，有可能有多种运行环境，正式，测试，沙箱
	 */
	public static AtomicInteger API_URL_INDEX = new AtomicInteger(0);

	public static final String DEFAULT_FORMAT_DATE = "yyyy-MM-dd HH:mm:ss.SSS";
	public static String DEFAULT_USERAGENT = "";

	private static Map<String, AuthHandler> authHandlerMap = new ConcurrentHashMap<String, AuthHandler>();
	private static Map<String, ParamFormat<?>> paramHandlerMap = new ConcurrentHashMap<String, ParamFormat<?>>();
	private static Map<Type, Converter> converterMap = new ConcurrentHashMap<Type, Converter>();

	public static String getKeyByClass(Class<?> clazz) {
		return String.format("class:%s", clazz.getName());
	}

	/**
	 * 注册参数类型格式化方式
	 * 
	 * @param clazz
	 * @param handler
	 */
	public static <T> void register(Class<T> clazz, ParamFormat<T> handler) {
		register(getKeyByClass(clazz), handler);
	}

	public static ParamFormat<?> getParamFormat(String name) {
		return paramHandlerMap.get(name);
	}

	public static ParamFormat<?> getParamFormat(Class<?> clazz) {
		return paramHandlerMap.get(getKeyByClass(clazz));
	}

	/**
	 * 注册参数格式化方式
	 * 
	 * @param name
	 * @param handler
	 */
	public static <T> void register(String name, ParamFormat<T> handler) {
		log.d("register ParamHandler[" + handler + "], Name:" + name);
		paramHandlerMap.put(name, handler);
	}

	/**
	 * 注册认证方式
	 * 
	 * @param name
	 * @param handler
	 */
	public static void register(String name, AuthHandler handler) {
		log.d("register AuthHandler[" + handler + "], Name:" + name);
		authHandlerMap.put(name, handler);
	}
	
	public static AuthHandler getAuthHandler(String name) {
		return authHandlerMap.get(name);
	}
	
	public static void register(Type type, Converter handler) {
		log.d("register Converter[" + handler + "], type:" + type.toString());
		converterMap.put(type, handler);
	}
	
	public static Converter getConverter(Type type) {
		return converterMap.get(type);
	}
}
