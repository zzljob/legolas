package com.yepstudio.legolas.internal;

/**
 * 服务端，{@link com.yepstudio.legolas.Endpoint} 的一个实现
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月6日
 * @version 2.0，2014年4月23日
 */
public class StaticIpServer extends Server {

	private static final String DEFAULT_NAME = "";

	private String url;
	private String ip;
	private int port;

	public StaticIpServer(String url) {
		this(DEFAULT_NAME, url, null, 80);
	}

	public StaticIpServer(String url, String ip) {
		this(DEFAULT_NAME, url, ip, 80);
	}

	public StaticIpServer(String url, String ip, int port) {
		this(DEFAULT_NAME, url, ip, port);
	}

	public StaticIpServer(String name, String url, String ip, int port) {
		super(name, url);
		this.url = url;
		this.ip = ip;
		this.port = port;
		setRemoteAddress(ip, port);
	}

	static String replaceHost(String url, String ip, int port) {
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

	public void setRemoteAddress(String name, String ip, int port) {
		this.ip = ip;
		this.port = port;
		if (ip == null || "".equals(ip.trim())) {
			ip = getHost();
		}
		super.url = replaceHost(this.url, ip, port);
		super.name = name;
	}
	
	public void setRemoteAddress(String ip, int port) {
		setRemoteAddress("", ip, port);
	}

	public String getNativeUrl() {
		return this.url;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

}
