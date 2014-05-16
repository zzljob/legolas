package com.yepstudio.legolas;

/**
 * 
 * @author zhangzl@gmail.com
 * @create 2014年4月23日
 * @version 2.0, 2014年4月23日
 * 
 */
public final class LegolasOptions {
	
	private Endpoint endpoint;
	private Converter converter;

	public Endpoint getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	public Converter getConverter() {
		return converter;
	}

	public void setConverter(Converter converter) {
		this.converter = converter;
	}

}
