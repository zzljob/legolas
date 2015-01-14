package com.yepstudio.legolas.mime;

import java.io.ByteArrayInputStream;

/**
 * 
 * @author zzljob@gmail.com
 * @createDate 2014年1月7日
 */
public class ByteArrayRequestBody extends StreamRequestBody {
	
	private static final int DEFAULT_BUFFER_SIZE = 4096;
	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
	private final byte[] bytes;

	/**
	 * Constructs a new typed byte array. Sets mimeType to
	 * {@code application/unknown} if absent.
	 * 
	 * @throws NullPointerException
	 *             if bytes are null
	 */
	public ByteArrayRequestBody(byte[] bytes) {
		this(DEFAULT_MIME_TYPE, bytes);
	}
	
	public ByteArrayRequestBody(String mimeType, byte[] bytes) {
		this(mimeType, bytes, Math.min(DEFAULT_BUFFER_SIZE, bytes.length));
	}

	public ByteArrayRequestBody(String mimeType, byte[] bytes, int bufferSize) {
		super(mimeType, bytes.length, new ByteArrayInputStream(bytes), bufferSize);
		this.bytes = bytes;
	}

	public byte[] getBytes() {
		return bytes;
	}
}
