package com.yepstudio.legolas;

import com.yepstudio.legolas.internal.ExcludeParamsCacheKeyGenerater;

/**
 * 
 * @author zhangzl@gmail.com
 * @create 2014年4月23日
 * @version 2.0, 2014年4月23日
 * 
 */
public class LegolasOptions {

	/** 数组类参数转换到String的间隔 **/
	private final String converterArrayParamSplit;
	/** 日期格式参数转换到String的格式 **/
	private final String converterDateParamFormat;
	/** 编码格式 **/
	private final String requestCharset;

	/** 请求前等待几毫秒 **/
	private final long delayBeforeRequest;
	/** 缓存策略 **/
	private final CachePolicy cachePolicy;
	/** 恢复策略 **/
	private final RecoveryPolicy recoveryPolicy;
	/** 是否使用内存缓存 **/
	private final boolean cacheInMemory;
	/** 是否使用文件缓存 **/
	private final boolean cacheOnDisk;
	/** 是否缓存转换结果 **/
	private final boolean cacheConverterResult;
	private final CacheKeyGenerater cacheKeyGenerater; 

	protected LegolasOptions(Builder builder) {
		converterArrayParamSplit = builder.converterArrayParamSplit;
		converterDateParamFormat = builder.converterDateParamFormat;
		requestCharset = builder.requestDefaultCharset;

		delayBeforeRequest = builder.delayBeforeRequest;
		cachePolicy = builder.cachePolicy;
		recoveryPolicy = builder.recoveryPolicy;
		cacheInMemory = builder.cacheInMemory;
		cacheOnDisk = builder.cacheOnDisk;
		cacheConverterResult = builder.cacheConverterResult;
		cacheKeyGenerater = builder.cacheKeyGenerater;
	}
	
	public Builder cloneBuilder() {
		Builder builder = new Builder();
		builder.converterArrayParamSplit = converterArrayParamSplit;
		builder.converterDateParamFormat = converterDateParamFormat;
		builder.requestDefaultCharset = requestCharset;
		builder.delayBeforeRequest = delayBeforeRequest;
		builder.cachePolicy = cachePolicy;
		builder.recoveryPolicy = recoveryPolicy;
		builder.cacheInMemory = cacheInMemory;
		builder.cacheOnDisk = cacheOnDisk;
		builder.cacheConverterResult = cacheConverterResult;
		builder.cacheKeyGenerater = cacheKeyGenerater;
		return builder;
	}

	/** 缓存策略 **/
	public static enum CachePolicy {
		/** 没有修改的时候使用缓存，跟浏览器的缓存一样 **/
		SERVER_CACHE_CONTROL,
		/** 发现缓存就直接使用，不进行过期判断，实际上这个时候就是交给Cache的实现去判断缓存的过期与否 **/
		ALWAYS_USE_CACHE
	}

	/*** 恢复策略 **/
	public static enum RecoveryPolicy {
		/** 不进行任何恢复策略 **/
		NONE,
		/** 服务器没有任何相应的时候，有三种状况：1、服务器宕机了 2、客户端网络连不上服务器 3、连接超时了。这几种状况都将被使用缓存恢复**/
		RESPONSE_NONE,
		/** httpStatus不是2xx也不是3xx的时候**/
		HTTPSTATUS_ERROR,
		/**
		 *  服务器没有给出期望的相应，相当于RESPONSE_NONE或者HTTPSTATUS_ERROR<br/>
		 *  如果是转换错误这里是不会恢复的
		 **/
		RESPONSE_NOT_EXPECTED,
	}

	public static class Builder {
		private String converterArrayParamSplit = ",";
		private String converterDateParamFormat = "yyyy-MM-dd";
		private String requestDefaultCharset = "UTF-8";

		private long delayBeforeRequest = 0;// 请求前等待几毫秒
		private boolean cacheInMemory = true;
		private boolean cacheOnDisk = true;
		private boolean cacheConverterResult = false;
		private CacheKeyGenerater cacheKeyGenerater;
		private CachePolicy cachePolicy = CachePolicy.SERVER_CACHE_CONTROL;
		private RecoveryPolicy recoveryPolicy = RecoveryPolicy.RESPONSE_NOT_EXPECTED;

		public Builder delayBeforeRequest(long delay) {
			delayBeforeRequest = delay;
			return this;
		}
		
		public Builder cacheInMemory(boolean cache) {
			cacheInMemory = cache;
			return this;
		}

		public Builder cacheKeyGenerater(CacheKeyGenerater cacheKeyGenerater) {
			this.cacheKeyGenerater = cacheKeyGenerater;
			return this;
		}
		
		public Builder cacheConverterResult(boolean cache) {
			cacheConverterResult = cache;
			return this;
		}
		
		public Builder cacheOnDisk(boolean cache) {
			cacheOnDisk = cache;
			return this;
		}

		public Builder cachePolicy(CachePolicy cachePolicy) {
			this.cachePolicy = cachePolicy;
			return this;
		}
		
		public Builder recoveryPolicy(RecoveryPolicy recoveryPolicy) {
			this.recoveryPolicy = recoveryPolicy;
			return this;
		}

		public Builder requestDefaultCharset(String charset) {
			this.requestDefaultCharset = charset;
			return this;
		}

		public LegolasOptions build() {
			if (cacheKeyGenerater == null) {
				String[] excludeParams = new String[] { "oauth_nonce", "oauth_timestamp", "oauth_signature" };
				cacheKeyGenerater = new ExcludeParamsCacheKeyGenerater(excludeParams);
			}
			return new LegolasOptions(this);
		}
	}

	public String getRequestCharset() {
		return requestCharset;
	}

	public String getConverterArrayParamSplit() {
		return converterArrayParamSplit;
	}

	public String getConverterDateParamFormat() {
		return converterDateParamFormat;
	}

	public long getDelayBeforeRequest() {
		return delayBeforeRequest;
	}

	public CachePolicy getCachePolicy() {
		return cachePolicy;
	}

	public RecoveryPolicy getRecoveryPolicy() {
		return recoveryPolicy;
	}

	public boolean isCacheInMemory() {
		return cacheInMemory;
	}

	public boolean isCacheOnDisk() {
		return cacheOnDisk;
	}

	public boolean isCacheConverterResult() {
		return cacheConverterResult;
	}

	public CacheKeyGenerater getCacheKeyGenerater() {
		return cacheKeyGenerater;
	}

}
