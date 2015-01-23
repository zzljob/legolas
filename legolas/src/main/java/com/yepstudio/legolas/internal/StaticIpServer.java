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
		if (url.indexOf("://") < 0) {
			throw new IllegalStateException("url需要带上协议");
		}
		String temp = url.substring(url.indexOf("://") + 3);
		String host = "";
		String other = "";
		
		int len = temp.indexOf("/");
		if (len > 0) {
			host = temp.substring(0, len);
			other = temp.substring(len);//后面那部分
		} else {
			host = temp;
			other = "";
		}

		StringBuilder builder = new StringBuilder();
		builder.append(url.substring(0, url.indexOf("://") + 3));
		if (ip != null && !"".equals(ip.trim())) {
			builder.append(ip);
		} else {
			builder.append(host);
		}
		if (port > -1) {
			builder.append(":").append(port);
		}
		builder.append(other);
		return builder.toString();
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
	
	public static void main(String[] args) {
		StaticIpServer smbApi = new StaticIpServer("数米API(线上)", "http://smb.fund123.cn/api", null, -1);
		smbApi.setRemoteAddress("数米API(测试环境)", "192.168.123.84", 18080);
	}

}
