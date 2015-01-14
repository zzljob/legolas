package com.yepstudio.legolas.internal;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 服务端，{@link com.yepstudio.legolas.Endpoint} 的一个实现
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月6日
 * @version 2.0，2014年4月23日
 */
public class StaticIpServer extends Server {

	public StaticIpServer(String url, String ip) {
		this("", url, ip, 80);
	}
	
	public StaticIpServer(String url, String ip, int port) {
		this("", url, ip, port);
	}
	
	public StaticIpServer(String name, String url, String ip, int port) {
		super(name, replaceHost(url, ip, port));
		try {
			host = new URL(url).getHost();
		} catch (MalformedURLException e) {
			throw new IllegalStateException("不是一个合法的URL");
		}
	}
	
	private static String replaceHost(String url, String ip, int port) {
		String target = "";
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
			target = temp.substring(0, len);
		} else {
			target = temp;
		}
		return target;
	}

}
