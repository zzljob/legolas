package com.yepstudio.legolas.mime;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2014年12月18日
 * @version 1.0，2014年12月18日
 *
 */
public final class FormUrlEncodedRequestBody implements RequestBody {
	
	private final static String DEFAULT_CHARSET = "UTF-8";
	
	private final String charset;
	private boolean urlEncode = true;
	private Map<String, String> fieldMap = new ConcurrentHashMap<String, String>();
	private StringBuffer content = new StringBuffer();
	
	public FormUrlEncodedRequestBody() {
		this(DEFAULT_CHARSET);
	}
	
	public FormUrlEncodedRequestBody(String charset) {
		super();
		this.charset = charset;
	}

	private boolean isEmpty(String string) {
		return string == null || "".equalsIgnoreCase(string.trim());
	}
	
	public void addOrReplaceField(String name, String value) {
		if (isEmpty(name)) {
			throw new IllegalArgumentException("name isEmpty ");
		}
		fieldMap.put(name, value);
		
		build();
	}
	
	public boolean existsField(String name) {
		return fieldMap.containsKey(name);
	}
	
	public String[] getFieldName() {
		return fieldMap.keySet().toArray(new String[0]);
	}
	
	public String getField(String name) {
		return fieldMap.get(name);
	}
	
	public void removeField(String name) {
		if (isEmpty(name)) {
			throw new IllegalArgumentException("name isEmpty ");
		}
		fieldMap.remove(name);
		
		build();
	}
	
	private void build() {
		if (content.length() > 0) {
			content.delete(0, content.length());
		}
		if (fieldMap.isEmpty()) {
			return;
		}
		for (String key : fieldMap.keySet()) {
			try {
				content.append(urlEncodeOrNot(key));
				content.append("=");
				content.append(urlEncodeOrNot(fieldMap.get(key)));
				content.append("&");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		content.deleteCharAt(content.length() - 1);
	}
	
	private String urlEncodeOrNot(String value) throws UnsupportedEncodingException {
		if (value == null) {
			return "";
		}
		if (urlEncode) {
			value = URLEncoder.encode(value, charset);
		}
		return value == null ? "" : value;
	}
	
	@Override
	public String mimeType() {
		return "application/x-www-form-urlencoded; charset=" + DEFAULT_CHARSET;
	}

	@Override
	public long length() {
		return content.length();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(content.toString().getBytes(charset));
	}

	public boolean isUrlEncode() {
		return urlEncode;
	}

	public void setUrlEncode(boolean urlEncode) {
		this.urlEncode = urlEncode;
	}
}
