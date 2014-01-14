package com.yepstudio.android.legolas.http.mime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author zzljob@gmail.com
 * @createDate 2014年1月7日
 */
public class ByteArrayBody implements ResponseBody, RequestBody {
	private final String mimeType;
	private final byte[] bytes;

	/**
	 * Constructs a new typed byte array. Sets mimeType to
	 * {@code application/unknown} if absent.
	 * 
	 * @throws NullPointerException
	 *             if bytes are null
	 */
	public ByteArrayBody(String mimeType, byte[] bytes) {
		if (mimeType == null) {
			mimeType = "application/unknown";
		}
		if (bytes == null) {
			throw new NullPointerException("bytes");
		}
		this.mimeType = mimeType;
		this.bytes = bytes;
	}

	@Override
	public String mimeType() {
		return mimeType;
	}

	@Override
	public long length() {
		return bytes.length;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(bytes);
	}

	@Override
	public InputStream read() throws IOException {
		return new ByteArrayInputStream(bytes);
	}

	@Override
	public String fileName() {
		return null;
	}

	public byte[] getBytes() {
		return bytes;
	}

}
