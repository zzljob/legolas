package com.yepstudio.legolas.mime;

import java.io.UnsupportedEncodingException;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2014年12月17日
 * @version 1.0，2014年12月17日
 *
 */
public class StringRequestBody extends ByteArrayRequestBody {
	
	private final String charset;
	private final String string;

	public StringRequestBody(String string) {
		this(string, "UTF-8");
	}

	public StringRequestBody(String string, String charset) {
		this("text/plain", string, charset);
	}

	public StringRequestBody(String mimeType, String string, String charset) {
		this(mimeType, string, charset, 2048);
	}

	public StringRequestBody(String mimeType, String string, String charset, int bufferSize) {
		super(String.format("%s; charset=%s", mimeType, charset), convertToBytes(charset, string), bufferSize);
		this.charset = charset;
		this.string = string;
	}
	
	private static byte[] convertToBytes(String charset, String string) {
		try {
			return string.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("UnsupportedEncoding : " + charset);
		}
	}

	public String getCharset() {
		return charset;
	}

	public String getString() {
		return string;
	}
}
