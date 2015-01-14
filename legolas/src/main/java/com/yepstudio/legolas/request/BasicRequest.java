package com.yepstudio.legolas.request;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.yepstudio.legolas.CacheKeyGenerater;
import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.LegolasOptions;
import com.yepstudio.legolas.mime.RequestBody;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2015年1月14日
 * @version 1.0，2015年1月14日
 *
 */
public abstract class BasicRequest extends Request {

	private final String uuid = UUID.randomUUID().toString();
	private final LegolasOptions options;
	private final String charset;
	private final Converter converter;
	private final CacheKeyGenerater cacheKeyGenerater;
	
	private Object lock = new Object();
	private final Date birthTime = new Date();
	/*** 开始请求的时候，如果缓存命中则为空 ***/
	private Date startRequestTime;
	/*** 完成时间，一定不为空 ***/
	private Date finishRequestTime;
	/***所有都完成的时间**/
	private Date finishTime;
	private AtomicBoolean allFinished = new AtomicBoolean(false);

	// 统计分析的扩展
	private Object profilerExpansion;

	public BasicRequest(String url, String method, String description,
			Map<String, String> headers, RequestBody body,
			LegolasOptions options, Converter converter) {
		super(url, method, description, headers, body);
		this.options = options;
		this.converter = converter;
		this.charset = options.getRequestCharset();
		this.cacheKeyGenerater = options.getCacheKeyGenerater();
	}

	public LegolasOptions getOptions() {
		return options;
	}

	public Converter getConverter() {
		return converter;
	}

	public String getCharset() {
		return charset;
	}

	public String getCacheKey() {
		if (cacheKeyGenerater != null) {
			return cacheKeyGenerater.generateKey(this);
		}
		return null;
	}

	public String getCacheKey(Type type) {
		if (cacheKeyGenerater != null) {
			return cacheKeyGenerater.generateKey(this, type);
		}
		return null;
	}

	public Object getProfilerExpansion() {
		return profilerExpansion;
	}

	public void setProfilerExpansion(Object profilerExpansion) {
		this.profilerExpansion = profilerExpansion;
	}

	public String getUuid() {
		return uuid;
	}
	
	public Date getRequestStartTime() {
		return startRequestTime;
	}

	public Date getRequestFinishTime() {
		return finishRequestTime;
	}

	public Date getFinishTime() {
		return finishTime;
	}

	public Date getBirthTime() {
		return birthTime;
	}

	public void startRequest() {
		synchronized (lock) {
			startRequestTime = new Date();
		}
	}

	public void finishRequest() {
		synchronized (lock) {
			finishRequestTime = new Date();
		}
	}

	public boolean isRequesting() {
		synchronized (lock) {
			return startRequestTime != null && finishRequestTime == null;
		}
	}
	
	public boolean isRequestStarted() {
		synchronized (lock) {
			return startRequestTime != null;
		}
	}
	
	public boolean isRequestFinished() {
		synchronized (lock) {
			return finishRequestTime != null;
		}
	}

	public synchronized boolean isFinished() {
		return allFinished.get();
	}
	
	public synchronized void finish() {
		synchronized (lock) {
			finishTime = new Date();
		}
		allFinished.set(true);
	}
	
	/**
	 * 毫秒数
	 * @return
	 */
	public long getReadyTime() {
		synchronized (lock) {
			if (allFinished.get()) {
				return finishTime.getTime() - birthTime.getTime() - getSpendTime();
			} 
			if (startRequestTime == null) {
				return -1;
			} else {
				return startRequestTime.getTime() - birthTime.getTime();
			}
		}
	}
	
	/**
	 * 毫秒数
	 * @return
	 */
	public long getSpendTime() {
		synchronized (lock) {
			if (finishRequestTime == null || startRequestTime == null) {
				return allFinished.get() ? 0 : -1;
			} else {
				return finishRequestTime.getTime() - startRequestTime.getTime();
			}
		}
	}

}
