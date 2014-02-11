package com.yepstudio.android.legolas;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.yepstudio.android.legolas.conversion.Converter;
import com.yepstudio.android.legolas.description.ApiDescription;
import com.yepstudio.android.legolas.handler.ProxyHandler;
import com.yepstudio.android.legolas.http.RequestExecutor;
import com.yepstudio.android.legolas.http.ResponseParser;
import com.yepstudio.android.legolas.http.Server;
import com.yepstudio.android.legolas.http.SimpleRequestExecutor;
import com.yepstudio.android.legolas.http.SimpleResponseParser;
import com.yepstudio.android.legolas.http.client.Client;
import com.yepstudio.android.legolas.http.client.UrlConnectionClient;
import com.yepstudio.android.legolas.log.LegolasLog;

/**
 * 莱戈拉斯<br/>
 * 幽暗密林的精灵王瑟兰督伊之子<br/>
 * 魔戒远征队的成员之一<br/>
 * 优秀的精灵弓箭手<br/>
 * 
 * @author zhangzl@fund123.cn
 * @createDate 2013年12月24日
 */
public class Legolas {
	
	private static LegolasLog log = LegolasLog.getClazz(Legolas.class);
	private static Map<Class<?>, ApiDescription> apiDescriptionCache = new ConcurrentHashMap<Class<?>, ApiDescription>();
	
	public static class Build {
		private Server server;
		private Client client;
		private Converter converter;
		private ResponseParser parser;
		private ResponseDelivery delivery;
		private RequestExecutor executor;

		public Build setServer(Server server) {
			this.server = server;
			return this;
		}

		public Build setClient(Client client) {
			this.client = client;
			return this;
		}

		public Build setRequestExecutor(RequestExecutor executor) {
			this.executor = executor;
			return this;
		}
		
		public Build setConverter(Converter converter) {
			this.converter = converter;
			return this;
		}
		
		public Build setParser(ResponseParser parser) {
			this.parser = parser;
			return this;
		}

		public Build setDelivery(ResponseDelivery delivery) {
			this.delivery = delivery;
			return this;
		}

		public Build setExecutor(RequestExecutor executor) {
			this.executor = executor;
			return this;
		}

		public Legolas create() {
			if (client == null) {
				client = new UrlConnectionClient();
				log.v("use Default Client:" + client.toString());
			}
			if (parser == null) {
				parser = new SimpleResponseParser(converter);
			}
			if (delivery == null) {
				delivery = new ExecutorDelivery(new Handler(Looper.getMainLooper()));
			}
			if (executor == null) {
				executor = new SimpleRequestExecutor(client, delivery, parser);
				log.v("use DefaultRequestExecutor:" + executor.toString());
			}
			return new Legolas(server, client, executor);
		}

	}
	
	private final Server server;
	private final RequestExecutor executor;
	
	public Legolas(Server server, Client client, RequestExecutor executor) {
		super();
		this.server = server;
		this.executor = executor;
	}

	/**
	 * 获取Api的实例
	 * @param context
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T newInstance(Context context, Class<T> clazz) {
		log.d("newInstance, Context:" + context + ", Class:" + clazz.getName());
		ApiDescription apiDescription = apiDescriptionCache.get(clazz);
		if (apiDescription == null) {
			long birthTime = SystemClock.elapsedRealtime();
			apiDescription = new ApiDescription(clazz);
			apiDescriptionCache.put(clazz, apiDescription);
			long finishTime = SystemClock.elapsedRealtime();
			log.d("ApiDescription be init : [" + (finishTime - birthTime) + "ms]");
		} else {
			log.v("this api class is be cache, so sikp parse api.");
		}
		ProxyHandler handler = new ProxyHandler(apiDescription, server, executor);
		Object proxy = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, handler);
		return (T) proxy;
	}

}
