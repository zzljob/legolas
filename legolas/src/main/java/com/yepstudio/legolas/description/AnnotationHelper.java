package com.yepstudio.legolas.description;

import java.util.List;

import com.yepstudio.legolas.LegolasConfiguration;
import com.yepstudio.legolas.RequestInterceptor;
import com.yepstudio.legolas.annotation.Interceptors;
import com.yepstudio.legolas.exception.LegolasConfigureError;
import com.yepstudio.legolas.internal.ObjectHelper;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2014年11月4日
 * @version 1.0，2014年11月4日
 *
 */
public class AnnotationHelper {
	
	private static final String LOG_VALIDATE_INTERCEPTORS_CANNOT_INIT = "class must has construction method of no-params and can be newInstance use @Interceptors";
	private static final String LOG_VALIDATE_INTERCEPTORS_CANNOT_FIND_OR_INIT = "className can not be find Or can not be newInstance use @Interceptors";
	private static final String LOG_VALIDATE_INTERCEPTORS_NO_REGISTER = "Interceptors must be register to LegolasConfiguration, name [%s] ";

	public static void parseInterceptors(List<RequestInterceptor> interceptors, Interceptors annotation, LegolasConfiguration config) {
		if (interceptors == null || annotation == null || config == null) {
			return ;
		}
		Class<? extends RequestInterceptor>[] clazzs = annotation.value();
		for (Class<? extends RequestInterceptor> clazz : clazzs) {
			RequestInterceptor obj = ObjectHelper.get(clazz);
			if (obj == null) {
				throw new LegolasConfigureError(LOG_VALIDATE_INTERCEPTORS_CANNOT_INIT);
			}
			interceptors.add(obj);
		}
		
		String[] className = annotation.className();
		for (String clazz : className) {
			Object obj = ObjectHelper.getObject(clazz);
			if (obj != null && obj instanceof RequestInterceptor) {
				interceptors.add((RequestInterceptor) obj);
			} else {
				throw new LegolasConfigureError(LOG_VALIDATE_INTERCEPTORS_CANNOT_FIND_OR_INIT);
			}
		}
		
		String[] names = annotation.alias();
		for (String name : names) {
			RequestInterceptor obj = config.getRequestInterceptor(name);
			if (obj == null) {
				String message = String.format(LOG_VALIDATE_INTERCEPTORS_NO_REGISTER, name);
				throw new LegolasConfigureError(message);
			}
			interceptors.add(obj);
		}
	}
}
