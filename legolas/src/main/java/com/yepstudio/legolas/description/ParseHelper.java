package com.yepstudio.legolas.description;

import java.util.Map;

import com.yepstudio.legolas.annotation.Headers;

public class ParseHelper {

	/**
	 * 从@Headers 解析出header
	 * @param headerMap
	 * @param headers
	 */
	public static void parseHeaders(Map<String, String> headerMap, Headers headers) {
		if (headerMap == null || headers == null) {
			return;
		}
		String[] headerArray = headers.value();
		if (headerArray != null) {
			for (String headerStr : headerArray) {
				String[] headerArr = headerStr.split(":", 2);
				if (headerArr != null && headerArr.length == 2) {
					headerMap.put(headerArr[0], headerArr[1]);
				}
			}
		}
	}

}
