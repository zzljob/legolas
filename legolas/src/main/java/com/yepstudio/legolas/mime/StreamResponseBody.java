package com.yepstudio.legolas.mime;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2014年12月31日
 * @version 1.0，2014年12月31日
 *
 */
public class StreamResponseBody implements ResponseBody {
	
	private final String mimeType;
	private final long length;
	private final InputStream stream;

	public StreamResponseBody(String mimeType, long length, InputStream stream) {
		this.mimeType = mimeType;
		this.length = length;
		this.stream = stream;
	}

	@Override
	public String mimeType() {
		return mimeType;
	}

	@Override
	public long length() {
		return length;
	}

	@Override
	public InputStream read() throws IOException {
		return stream;
	}

}
