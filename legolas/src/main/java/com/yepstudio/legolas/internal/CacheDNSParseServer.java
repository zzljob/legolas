package com.yepstudio.legolas.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class CacheDNSParseServer extends Server implements Serializable, Runnable {

	private static final long serialVersionUID = 1658273096683720668L;

	private final Set<String> ippools = new CopyOnWriteArraySet<String>();
	private final Set<String> dnsIps;
	private final long expireTime;
	private final Executor executor;
	private Date lastUpdateTime;
	private AtomicBoolean enable = new AtomicBoolean(true);

	public static Set<String> getDefaultDNSServers() {
		Set<String> defaultDnsServers = new HashSet<String>();
		defaultDnsServers.add("8.8.8.8");// Google
		defaultDnsServers.add("114.114.114.114");// 114DNS
		defaultDnsServers.add("223.5.5.5");// AliDNS
		defaultDnsServers.add("180.76.76.76");// BaiduDNS
		return defaultDnsServers;
	}

	public CacheDNSParseServer(String name, String url) {
		this(name, url, getDefaultDNSServers());
	}

	public CacheDNSParseServer(String name, String url, Set<String> dnsIps) {
		this(name, url, dnsIps, 2, TimeUnit.HOURS);
	}

	public CacheDNSParseServer(String name, String url, Set<String> dnsIps, long refreshTime, TimeUnit refreshTimeUnit) {
		this(name, url, dnsIps, refreshTime, refreshTimeUnit, Executors.newSingleThreadExecutor());
	}

	public CacheDNSParseServer(String name, String url, Set<String> dnsIps, long refreshTime, TimeUnit refreshTimeUnit, Executor executor) {
		super(name, url);
		this.dnsIps = dnsIps;
		if (refreshTimeUnit == null) {
			this.expireTime = refreshTime;
		} else {
			this.expireTime = refreshTimeUnit.toMillis(refreshTime);
		}
		this.executor = executor;
		
		executor.execute(this);
	}

	@Override
	public void run() {
		// 去获取DNS
		String domain = getHost();
		String[] dnsServers = dnsIps.toArray(new String[] {});
		int timeout = 1000 * 60;
		int retryCount = 3;
		Set<String> ips = getAllIP(domain, dnsServers, timeout, retryCount);
		ippools.clear();
		ippools.addAll(ips);
		lastUpdateTime = new Date();
	}
	
	private boolean isExpire() {
		if (lastUpdateTime == null) {
			return true;
		}
		return new Date().getTime() - lastUpdateTime.getTime() > expireTime;
	} 
	
    /**
    * 获取DNS服务器信息
    *
    * @param domain  要获取DNS信息的域名
    * @param provider      DNS服务器
    * @param types   信息类型 "A"(IP信息)，"MX"(邮件路由记录)，"CNAME"通常称别名指向，"NS"
    * @param timeout 请求超时
    * @param retryCount    重试次数
    *
    * @return 所有信息组成的数组
    *
    * @throws NamingException
    *        
    */
    @SuppressWarnings("rawtypes" )
    public static List<String> getDNSRecs(String domain, String provider, String [] types, int timeout, int retryCount) throws NamingException {
		ArrayList<String> results = new ArrayList<String>(15);
		Hashtable<String, String> env = new Hashtable<String, String>();

		env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
		// 设置域名服务器
		env.put(Context.PROVIDER_URL, "dns://" + provider);
		// 连接时间
		env.put("com.sun.jndi.dns.timeout.initial", String.valueOf(timeout));
		// 连接次数
		env.put("com.sun.jndi.dns.timeout.retries", String.valueOf(retryCount));
		DirContext ictx = new InitialDirContext(env);
		Attributes attrs = ictx.getAttributes(domain, types);
		for (Enumeration e = attrs.getAll(); e.hasMoreElements();) {
			Attribute a = (Attribute) e.nextElement();
			int size = a.size();
			for (int i = 0; i < size; i++) {
				results.add((String) a.get(i));
			}
		}
		return results;
   }

    /**
    * 获取域名所有IP
    * @param domain  域名
    * @param dnsServers    DNS服务器列表
    * @param timeout 请求超时
    * @param retryCount    重试次数
    * @return
    */
	public static Set<String> getAllIP(String domain, String[] dnsServers, int timeout, int retryCount) {
		Set<String> ips = new HashSet<String>();
		String[] types = new String[] { "A" };
		for (String dnsServer : dnsServers) {
			List<String> ipList;
			try {
				ipList = getDNSRecs(domain, dnsServer, types, timeout, retryCount);
			} catch (NamingException e) {
				continue;
			}
			ips.addAll(ipList);
		}
		return ips;
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
