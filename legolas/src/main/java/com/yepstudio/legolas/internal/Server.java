package com.yepstudio.legolas.internal;

import com.yepstudio.legolas.Endpoint;

/**
 * 服务端，{@link com.yepstudio.legolas.Endpoint} 的一个实现
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月6日
 * @version 2.0，2014年4月23日
 */
public class Server implements Endpoint {

	private final String url;
	private final String name;

	public Server(String url) {
		this("", url);
	}

	public Server(String name, String url) {
		super();
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

}
