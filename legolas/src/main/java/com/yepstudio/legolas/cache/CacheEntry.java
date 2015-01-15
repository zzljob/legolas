package com.yepstudio.legolas.cache;

import java.util.Map;

public class CacheEntry<T> {
	/** The data returned from cache. */
	private T data;

	/** ETag for cache coherency. */
	private String etag;

	/** Date of this response as reported by the server. */
	private long serverDate;

	/** Date of this response Last-Modified as reported by the server. */
	private long serverModified;

	/** TTL for this record. */
	private long ttl;

	/** Soft TTL for this record. */
	private long softTtl;
	
	/**
	 * Immutable response headers as received from server; must be non-null.
	 */
	private final Map<String, String> responseHeaders;

	public CacheEntry(T data, Map<String, String> responseHeaders, String etag, long serverDate, long serverModified, long softTtl) {
		this(data, responseHeaders, etag, serverDate, serverModified, softTtl, softTtl);
	}
	
	public CacheEntry(T data, Map<String, String> responseHeaders, String etag, long serverDate, long serverModified, long softTtl, long ttl) {
		super();
		this.data = data;
		this.responseHeaders = responseHeaders;
		this.etag = etag;
		this.serverDate = serverDate;
		this.serverModified = serverModified;
		this.ttl = ttl;
		this.softTtl = softTtl;
	}

	public void makeExpired(boolean fullExpire) {
		if (fullExpire) {
			this.ttl = 0;
		}
		this.softTtl = 0;
	}

	/** True if the entry is expired. */
	public boolean isExpired() {
		return this.ttl > 0 && this.ttl < System.currentTimeMillis();
	}

	/** True if a refresh is needed from the original data source. */
	public boolean refreshNeeded() {
		return true;
	}

	public T getData() {
		return data;
	}

	public String getEtag() {
		return etag;
	}

	public long getServerDate() {
		return serverDate;
	}

	public Map<String, String> getResponseHeaders() {
		return responseHeaders;
	}

	public long getTtl() {
		return ttl;
	}

	public long getSoftTtl() {
		return softTtl;
	}

	public long getServerModified() {
		return serverModified;
	}

	@Override
	public String toString() {
		return "CacheEntry [data=" + data + ", etag=" + etag + ", serverDate="
				+ serverDate + ", serverModified=" + serverModified + ", ttl="
				+ ttl + ", softTtl=" + softTtl + ", responseHeaders="
				+ responseHeaders + "]";
	}
}