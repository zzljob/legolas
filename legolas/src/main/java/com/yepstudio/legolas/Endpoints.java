package com.yepstudio.legolas;

import com.yepstudio.legolas.internal.Server;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年4月24日
 * @version 2.0, 2014年4月24日
 * 
 */
public class Endpoints {
	private Endpoints() {
	}

	/** Create a server with the provided URL. */
	public static Endpoint newFixedEndpoint(String url) {
		return new Server(url, Endpoint.DEFAUL_TNAME);
	}

	/** Create an endpoint with the provided URL and name. */
	public static Endpoint newFixedEndpoint(String url, String name) {
		return new Server(url, name);
	}
}
