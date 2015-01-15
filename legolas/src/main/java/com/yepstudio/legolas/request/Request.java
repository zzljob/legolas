package com.yepstudio.legolas.request;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.yepstudio.legolas.mime.RequestBody;

/**
 * 
 * @author zzljob@gmail.com
 * @createDate 2014年1月9日
 */
public abstract class Request implements Comparable<Request> {

	private final String url;
	private final String method;
	private final String description;
	private final Map<String, String> headers;
	private final RequestBody body;
	
	protected final Date birthTime = new Date();
	/**** 初始化的时间 ***/
	protected final AtomicBoolean cacheConverterResult = new AtomicBoolean(true);
	protected final AtomicBoolean cacheResponseInMemory = new AtomicBoolean(true);
	protected final AtomicBoolean cacheResponseOnDisk = new AtomicBoolean(true);
	
	protected final StringBuffer log = new StringBuffer();
	protected final AtomicBoolean cancel = new AtomicBoolean(false);
	
	public Request(String url, String method, String description, Map<String, String> headers, RequestBody body) {
		super();
		this.url = url;
		this.method = method;
		this.description = description;
		this.headers = headers;
		this.body = body;
	}

	@Override
	public int compareTo(Request o) {
		if (o == null) {
			return 1;
		}
		Date time1 = getBirthTime();
		Date time2 = getBirthTime();
		if (time1 == null && time2 == null) {
			return 0;
		}
		if (time1 == null || time2 == null) {
			return time1 == null ? -1 : 1;
		}
		return (int) (time1.getTime() - time2.getTime());
	}

	public String getMethod() {
		return method;
	}

	public String getUrl() {
		return url;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public RequestBody getBody() {
		return body;
	}

	public String getDescription() {
		return description;
	}
	
	public String getRequestLog() {
		return log.toString();
	}
	
	public Request appendLog(String logText) {
		log.append(logText);
		return this;
	}
	
	public Date getBirthTime() {
		return birthTime;
	}
	
	public abstract String getCharset();
	
	public abstract String getCacheKey();

	public abstract String getCacheKey(Type type);
	
	public abstract boolean isFinished();
	
	public abstract boolean isRequestFinished();
	
	public abstract boolean isRequestStarted();
	
	public abstract boolean isRequesting();
	
	public abstract Date getRequestStartTime();

	public abstract Date getRequestFinishTime();

	public abstract Date getFinishTime();
	
	public abstract long getSpendTime();
	
	public abstract long getReadyTime();
	
	public boolean isUseCache() {
		return cacheConverterResult.get() || cacheResponseInMemory.get() || cacheResponseOnDisk.get();
	}
	
	public synchronized void denyCache(boolean denyCache) {
		cacheConverterResult.set(denyCache);
		cacheResponseInMemory.set(denyCache);
		cacheResponseOnDisk.set(denyCache);
	}
	
	public synchronized void cancel() {
		cancel.set(true);
	}
	
	public synchronized boolean isCancel() {
		return cancel.get();
	}
	
	public abstract boolean isRetry();
	
	public abstract boolean retry();
	
}
