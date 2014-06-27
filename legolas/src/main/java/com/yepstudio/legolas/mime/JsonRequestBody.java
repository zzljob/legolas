package com.yepstudio.legolas.mime;

import java.io.UnsupportedEncodingException;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年5月15日
 * @version 2.0, 2014年5月15日
 *
 */
public class JsonRequestBody extends ByteArrayBody {

	private static String defaultCharset = "UTF-8";
	
	public JsonRequestBody(String jsonText) throws UnsupportedEncodingException {
		this(defaultCharset, jsonText);
	}
	
	public JsonRequestBody(String encode, String jsonText) throws UnsupportedEncodingException {
		super("application/json; charset=" + encode, jsonText.getBytes(encode));
	}
	
}
