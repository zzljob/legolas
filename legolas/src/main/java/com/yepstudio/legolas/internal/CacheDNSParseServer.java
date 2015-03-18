package com.yepstudio.legolas.internal;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CacheDNSParseServer extends Server implements Serializable, Runnable {

	private static final long serialVersionUID = 1658273096683720668L;

	private final Set<String> ippools = new CopyOnWriteArraySet<String>();
	private final long expireTime;
	private final Executor executor;
	private Date lastUpdateTime;
	private AtomicBoolean enable = new AtomicBoolean(true);

	public CacheDNSParseServer(String name, String url) {
		this(name, url, 1, TimeUnit.HOURS);
	}

	public CacheDNSParseServer(String name, String url, long refreshTime, TimeUnit refreshTimeUnit) {
		this(name, url, refreshTime, refreshTimeUnit, Executors.newSingleThreadExecutor());
	}

	public CacheDNSParseServer(String name, String url, long refreshTime, TimeUnit refreshTimeUnit, Executor executor) {
		super(name, url);
		if (refreshTimeUnit == null) {
			this.expireTime = refreshTime;
		} else {
			this.expireTime = refreshTimeUnit.toMillis(refreshTime);
		}
		this.executor = executor;
		
		//executor.execute(this);
	}

	@Override
	public void run() {
		// 去获取DNS
		String domain = getHost();
		try {
			List<String> results = new ArrayList<String>();
			InetAddress[] address = InetAddress.getAllByName(domain);
			if (address != null && address.length > 0) {
				for (InetAddress inetAddress : address) {
					results.add(inetAddress.getHostAddress());
				}
			}
			ippools.clear();
			ippools.addAll(results);
			lastUpdateTime = new Date();
		} catch (RuntimeException e) {
		} catch (UnknownHostException e) {
		}
	}
	
	private boolean isExpire() {
		if (lastUpdateTime == null) {
			return true;
		}
		return new Date().getTime() - lastUpdateTime.getTime() > expireTime;
	} 

	@Override
	public String getIp() {
		if (isExpire()) {
			executor.execute(this);
		}
		if (isEnableCache() && !ippools.isEmpty()) {
			return ippools.iterator().next();
		}
		return super.getIp();
	}
	
	public boolean isEnableCache() {
		return enable.get();
	}

	public void enableCache(boolean enable) {
		this.enable.set(enable);
	}
}
