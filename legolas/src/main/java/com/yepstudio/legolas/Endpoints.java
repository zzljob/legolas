package com.yepstudio.legolas;

import com.yepstudio.legolas.internal.Server;
import com.yepstudio.legolas.internal.StaticIpServer;

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
	
	public static Endpoint newStaticIpServer(String url, String ip) {
		return new StaticIpServer(Endpoint.DEFAUL_TNAME, url, ip, 80);
	}
	
	public static Endpoint newStaticIpServer(String name, String url, String ip, int port) {
		return new StaticIpServer(name, url, ip, port);
	}
}
