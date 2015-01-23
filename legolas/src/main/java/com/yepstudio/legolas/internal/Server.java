package com.yepstudio.legolas.internal;

import java.net.MalformedURLException;
import java.net.URL;

import com.yepstudio.legolas.Endpoint;

/**
 * 服务端，{@link com.yepstudio.legolas.Endpoint} 的一个实现
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月6日
 * @version 2.0，2014年4月23日
 */
public class Server implements Endpoint {

	protected String url;
	protected String name;
	protected final String host;

	public Server(String url) {
		this("", url);
	}

	public Server(String name, String url) {
		super();
		this.name = name;
		this.url = url;
		URL obj = null;
		try {
			obj = new URL(url);
			host = obj.getHost();
		} catch (MalformedURLException e) {
			throw new IllegalStateException("不是一个合法的URL");
		}
	}

	public String getName() {
		return name;
	}

	public String getRequestUrl() {
		return url;
	}

	@Override
	public String getHost() {
		return host;
	}
	
	public String getIp() {
		return null;
	}

	public int getPort() {
		return -1;
	}

}
