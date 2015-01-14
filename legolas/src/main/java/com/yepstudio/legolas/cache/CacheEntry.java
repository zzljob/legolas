package com.yepstudio.legolas.cache;

import java.util.Map;

public class CacheEntry<T> {
	/** The data returned from cache. */
	private T data;

	/** ETag for cache coherency. */
	private String etag;

	/** Date of this response as reported by the server. */
	private long serverDate;

	/** TTL for this record. */
	private long ttl;

	/** Soft TTL for this record. */
	private long softTtl;
	
	/**
	 * Immutable response headers as received from server; must be non-null.
	 */
	private final Map<String, String> responseHeaders;

	public CacheEntry(T data, Map<String, String> responseHeaders, String etag, long serverDate, long softTtl) {
		this(data, responseHeaders, etag, serverDate, softTtl, softTtl);
	}
	
	public CacheEntry(T data, Map<String, String> responseHeaders, String etag, long serverDate, long softTtl, long ttl) {
		super();
		this.data = data;
		this.responseHeaders = responseHeaders;
		this.etag = etag;
		this.serverDate = serverDate;
		this.ttl = ttl;
		this.softTtl = softTtl;
	}


	/** True if the entry is expired. */
	public boolean isExpired() {
		return this.ttl > 0 && this.ttl < System.currentTimeMillis();
	}

	/** True if a refresh is needed from the original data source. */
	public boolean refreshNeeded() {
		return this.softTtl < System.currentTimeMillis();
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

	@Override
	public String toString() {
		return "CacheEntry [data=" + data + ", etag=" + etag + ", serverDate="
				+ serverDate + ", ttl=" + ttl + ", softTtl=" + softTtl
				+ ", responseHeaders=" + responseHeaders + "]";
	}

	public void setData(T data) {
		this.data = data;
	}

	public void setEtag(String etag) {
		this.etag = etag;
	}

	public void setServerDate(long serverDate) {
		this.serverDate = serverDate;
	}

	public void setTtl(long ttl) {
		this.ttl = ttl;
	}

	public void setSoftTtl(long softTtl) {
		this.softTtl = softTtl;
	}

	public long getTtl() {
		return ttl;
	}

	public long getSoftTtl() {
		return softTtl;
	}
}