package com.yepstudio.legolas.mime;

import java.io.UnsupportedEncodingException;

public class StringResponseBody extends ByteArrayResponseBody {

	private final String charset;
	private final String string;

	public StringResponseBody(String string) {
		this("text/plain", string);
	}

	public StringResponseBody(String mimeType, String string) {
		this(mimeType, string, "UTF-8");
	}

	public StringResponseBody(String mimeType, String string, String charset) {
		super(mimeType, getBytes(string, charset));
		this.string = string;
		this.charset = charset;
	}

	private static byte[] getBytes(String string, String charset) {
		try {
			return string.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public String getCharset() {
		return charset;
	}

	public String getString() {
		return string;
	}

}
