package com.yepstudio.legolas.mime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * @author zzljob@gmail.com
 * @createDate 2014年1月7日
 */
public class ByteArrayResponseBody extends StreamResponseBody {

	private static final int DEFAULT_BUFFER_SIZE = 4096;
	public static final long MAX_LIMIT_SIZE = 1 * 1024 * 1024; //1Mb

	private volatile byte[] bytes = null;
	private final int bufferSize;
	private AtomicBoolean dataHasRead = new AtomicBoolean(false);
	private OnReadListener onReadListener;
	
	public static ByteArrayResponseBody build(ResponseBody body) throws IOException {
		return build(body, DEFAULT_BUFFER_SIZE);
	}
	
	public static ByteArrayResponseBody build(ResponseBody body, int bufferSize) throws IOException {
		if (body == null) {
			return null;
		}
		if (body instanceof ByteArrayResponseBody) {
			return (ByteArrayResponseBody) body;
		} else {
			String mimeType = body.mimeType();
			long length = body.length();
			InputStream stream = body.read();
			ByteArrayResponseBody byteBody = new ByteArrayResponseBody(mimeType, length, stream, bufferSize);
			byteBody.read();
			return byteBody;
		}
	}
	
	public ByteArrayResponseBody(String mimeType, byte[] bytes) {
		this(mimeType, bytes == null ? 0 : bytes.length, null, DEFAULT_BUFFER_SIZE);
		this.bytes = bytes;
		dataHasRead.set(true);
	}
	
	public ByteArrayResponseBody(String mimeType, long length, InputStream stream) {
		this(mimeType, length, stream, DEFAULT_BUFFER_SIZE);
	}

	public ByteArrayResponseBody(String mimeType, long length, InputStream stream, int bufferSize) {
		super(mimeType, length, stream);
		this.bufferSize = bufferSize;
		this.bytes = null;
	}

	public byte[] getBytes() throws IOException {
		if (dataHasRead.get()) {
			return bytes;
		} else {
			read();
		}
		return bytes;
	}
	
	@Override
	public long length() {
		if (dataHasRead.get()) {
			return bytes == null ? 0 : bytes.length;
		} else {
			return super.length();
		}
	}
	
	@Override
	public synchronized InputStream read() throws IOException {
		if (dataHasRead.get()) {
			return new ByteArrayInputStream(bytes);
		}
		InputStream responseStream = null;
		try {
			responseStream = super.read();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			if (responseStream != null) {
				byte[] buf = new byte[bufferSize];
				long readSize = 0;
				int r;
				while ((r = responseStream.read(buf)) != -1) {
					baos.write(buf, 0, r);
					readSize += r;
					onReadProgress(readSize);
				}
				onReadFinish();
			}
			bytes = baos.toByteArray();
			return new ByteArrayInputStream(bytes);
		} finally {
			if (responseStream != null) {
				responseStream.close();
			}
			dataHasRead.set(true);
		}
	}
	
	protected void onReadProgress(long readSize) {
		if (onReadListener != null) {
			try {
				onReadListener.onReadProgress(this, readSize);
			} catch (Throwable th) {
			}
		}
	}
	
	protected void onReadFinish() {
		if (onReadListener != null) {
			try {
				onReadListener.onReadFinish(this);
			} catch (Throwable th) {
			}
		}
	}

	public OnReadListener getOnReadListener() {
		return onReadListener;
	}

	public void setOnReadListener(OnReadListener onReadListener) {
		this.onReadListener = onReadListener;
	}

}
