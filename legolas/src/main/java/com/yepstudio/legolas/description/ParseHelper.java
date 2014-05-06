package com.yepstudio.legolas.description;

import java.util.Map;

import com.yepstudio.legolas.annotation.Headers;
import com.yepstudio.legolas.annotation.Item;

public class ParseHelper {

	/**
	 * 从@Headers 解析出header
	 * 
	 * @param headerMap
	 * @param headers
	 */
	public static void parseHeaders(Map<String, String> headerMap, Headers headers) {
		if (headerMap == null || headers == null) {
			return;
		}
		Item[] headerArray = headers.value();
		if (headerArray != null) {
			for (Item headerStr : headerArray) {
				headerMap.put(headerStr.key(), headerStr.value());
			}
		}
	}

}
