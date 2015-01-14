package com.yepstudio.legolas.internal;

import com.yepstudio.legolas.Legolas;
import com.yepstudio.legolas.LegolasLog;

public class ObjectHelper {

	public static <T> T get(Class<T> clazz) {
		LegolasLog log = Legolas.getLog();
		if (clazz == null) {
			return null;
		}
		T result = null;
		try {
			result = clazz.newInstance();
		} catch (InstantiationException e) {
			log.e("class can not be newInstance", e);
		} catch (IllegalAccessException e) {
			log.e("class can not be newInstance", e);
		}
		return result;
	}
	
	public static Object getObject(String className) {
		LegolasLog log = Legolas.getLog();
		if (className == null || "".equals(className.trim())) {
			return null;
		}
		Object result = null;
		try {
			Class<?> clazz = Class.forName(className);
			result = clazz.newInstance();
		} catch (InstantiationException e) {
			log.e("class can not be newInstance", e);
		} catch (IllegalAccessException e) {
			log.e("class can not be newInstance", e);
		} catch (ClassNotFoundException e) {
			log.e("class can not be newInstance", e);
		}
		return result;
	}

}
