package com.yepstudio.legolas;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年4月24日
 * @version 2.0, 2014年4月24日
 *
 */
public interface Endpoint {
	
	static String DEFAUL_TNAME = ""; 

	/** The base API URL. */
	String getUrl();

	/** A name for differentiating between multiple API URLs. */
	String getName();

}