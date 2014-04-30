package com.yepstudio.legolas.internal;

import com.yepstudio.legolas.Endpoint;



/**
 * 服务端
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月6日
 * @version 2.0，2014年4月23日
 */
public class Server implements Endpoint {

	private String name;
	private String url;

	public Server(String name, String url) {
		super();
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
