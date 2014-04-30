package com.yepstudio.legolas;

import java.lang.ref.SoftReference;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.yepstudio.legolas.description.ApiDescription;
import com.yepstudio.legolas.internal.converter.JSONConverter;

/**
 * @author zzljob@gmail.com
 * @create 2014年1月14日
 * @version 2.0，2014年4月23日
 */
public class LegolasConfig {
	private static LegolasLog log = LegolasLog.getClazz(LegolasConfig.class);
	
	private static LegolasConfig instance;
	
	private Map<Type, Converter> converterMap = new ConcurrentHashMap<Type, Converter>();
	private Map<Class<?>, SoftReference<ApiDescription>> apiDescriptionCache = new ConcurrentHashMap<Class<?>, SoftReference<ApiDescription>>();
	
	public synchronized static LegolasConfig getInstance() {
		if (instance == null) {
			instance = new LegolasConfig();
			JSONConverter c = new JSONConverter();
			instance.register(JSONObject.class, c);
			instance.register(JSONArray.class, c);
		}
		return instance;
	}

	public void register(Type type, Converter converter) {
		log.d("register Converter[" + converter + "], type:" + type.toString());
		converterMap.put(type, converter);
	}
	
	public Converter getConverter(Type type) {
		return converterMap.get(type);
	}
	
	protected ApiDescription getApiDescription(Class<?> clazz) {
		SoftReference<ApiDescription> apiRef = apiDescriptionCache.get(clazz);
		ApiDescription api;
		if (apiRef == null || apiRef.get() == null) {
			long birthTime = System.currentTimeMillis();
			api = new ApiDescription(clazz);
			apiDescriptionCache.put(clazz, new SoftReference<ApiDescription>(api));
			long finishTime = System.currentTimeMillis();
			log.d("ApiDescription be init : [" + (finishTime - birthTime) + "ms]");
		} else {
			api = apiRef.get();
			log.v("this api class is be cache, so sikp parse api.");
		}
		return api;
	}
	
}

