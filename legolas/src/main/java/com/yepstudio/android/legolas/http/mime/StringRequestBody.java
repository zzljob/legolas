package com.yepstudio.android.legolas.http.mime;

import java.io.UnsupportedEncodingException;

public class StringRequestBody extends ByteArrayBody {

	public StringRequestBody(String string) {
		super("text/plain; charset=UTF-8", convertToBytes(string));
	}

	private static byte[] convertToBytes(String string) {
		try {
			return string.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
