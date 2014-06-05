package com.yepstudio.legolas.internal;

import com.yepstudio.legolas.RemoteEndpoint;

/**
 * 服务端，{@link com.yepstudio.legolas.Endpoint} 的一个实现
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月6日
 * @version 2.0，2014年4月23日
 */
public class StaticIpServer extends Server implements RemoteEndpoint {

	private final String host;
	private final String targetUrl;

	public StaticIpServer(String url, String ip) {
		this("", url, ip);
	}
	
	public StaticIpServer(String name, String url, String ip) {
		this(name, url, ip, 80);
	}
	
	public StaticIpServer(String name, String url, String ip, int port) {
		super(name, url);
		if (url.indexOf("://") < 0) {
			throw new IllegalStateException("url需要带上协议");
		}
		String temp = url.substring(url.indexOf("://") + 3);

		StringBuilder builder = new StringBuilder();
		builder.append(url.substring(0, url.indexOf("://") + 3));
		builder.append(ip).append(":").append(port);
		int len = temp.indexOf("/");
		if (len > 0) {
			builder.append(temp.substring(len));
			host = temp.substring(0, len);
		} else {
			host = temp;
		}
		targetUrl = builder.toString();
	}

	@Override
	public String getRemoteUrl() {
		return targetUrl;
	}

	@Override
	public String getHost() {
		return host;
	}
	
}
