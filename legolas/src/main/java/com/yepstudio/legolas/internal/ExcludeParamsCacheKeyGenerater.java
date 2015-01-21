package com.yepstudio.legolas.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import com.yepstudio.legolas.request.Request;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2015年1月14日
 * @version 1.0，2015年1月14日
 *
 */
public class ExcludeParamsCacheKeyGenerater extends SimpleCacheKeyGenerater {

	private final Set<String> excludeParamNames;
	private final String excludeParamRegex;

	public ExcludeParamsCacheKeyGenerater(String excludeParamRegex) {
		this(null, excludeParamRegex);
	}

	public ExcludeParamsCacheKeyGenerater(String[] params) {
		super();
		excludeParamNames = new HashSet<String>();
		Collections.addAll(excludeParamNames, params);
		excludeParamRegex = null;
	}

	public ExcludeParamsCacheKeyGenerater(Set<String> excludeParamNames, String excludeParamRegex) {
		super();
		this.excludeParamNames = excludeParamNames;
		this.excludeParamRegex = excludeParamRegex;
	}

	@Override
	public String generateKey(Request request) {
		String url = request.getUrl();
		TreeMap<String, String> map = new TreeMap<String, String>();

		if (url == null) {
			return url;
		}
		int index = url.indexOf("?");
		if (index < 0) {
			return url;
		}
		StringBuilder builder = new StringBuilder(request.getMethod());
		builder.append(":");
		builder.append(url.substring(0, index));

		String queryStr = url.substring(index + 1);

		int beforeIndex = queryStr.indexOf("#");
		if (beforeIndex > 0) {
			queryStr = queryStr.substring(0, index);
		}
		fillAllQuerys(map, queryStr);
		for (String key : map.keySet()) {
			if (isSkip(key)) {
				continue;
			}
			builder.append(key).append("=").append(map.get(key));
			builder.append("&");
		}
		return builder.toString();
	}

	private boolean isSkip(String paramName) {
		if (paramName == null) {
			return true;
		}
		if (excludeParamNames != null && excludeParamNames.contains(paramName)) {
			return true;
		}
		if (excludeParamRegex != null && paramName.matches(excludeParamRegex)) {
			return true;
		}
		return false;
	}
	
	private void fillAllQuerys(TreeMap<String, String> map, String queryStr) {
		String[] ss = queryStr.split("&");
		if (ss != null && ss.length > 0) {
			for (String string : ss) {
				String[] temp = string.split("=", 2);
				if (temp != null && temp.length == 2) {
					map.put(temp[0], temp[1]);
				}
			}
		}
	}

}
