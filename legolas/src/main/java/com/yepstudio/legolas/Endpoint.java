package com.yepstudio.legolas;

/**
 * 对于API可能有不同的环境<br/>
 * 比如 :<br/>
 * 正式环境是: http://rebirth.duapp.com<br/>
 * 测试环境是: http://dev.rebirth.duapp.com<br/>
 * 那么就可以在请求的时候通过设置不同的{@link com.yepstudio.legolas.Endpoint}来改变请求的环境
 * 
 * @author zzljob@gmail.com
 * @create 2014年4月24日
 * @version 2.0, 2014年4月24日
 * 
 */
public interface Endpoint {

	public static String DEFAUL_TNAME = "";

	/**
	 * API 请求的 URL.
	 * 
	 * @return
	 */
	public String getRequestUrl();

	/**
	 * Api 的HOST
	 * 
	 * @return
	 */
	public String getHost();

	/**
	 * A name for differentiating between multiple API URLs.
	 * 
	 * @return
	 */
	public String getName();

}