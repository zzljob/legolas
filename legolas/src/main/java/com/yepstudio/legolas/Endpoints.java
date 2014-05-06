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

	public static Endpoint newFixedEndpoint(String url) {
		return new Server(Endpoint.DEFAUL_TNAME, url);
	}

	public static Endpoint newFixedEndpoint(String url, String name) {
		return new Server(name, url);
	}
}
