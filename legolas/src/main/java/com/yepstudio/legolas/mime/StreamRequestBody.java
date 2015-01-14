package com.yepstudio.legolas.mime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamRequestBody implements RequestBody {
	
	private static final int DEFAULT_BUFFER_SIZE = 1024;
	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	private final String mimeType;
	private final long length;
	private final InputStream stream;
	protected final int bufferSize;
	
	private OnWriteListener onWriteListener;

	/**
	 * Constructs a new typed file.
	 * 
	 * @throws NullPointerException
	 *             if file or mimeType is null
	 */
	public StreamRequestBody(long length, InputStream stream) {
		this(DEFAULT_MIME_TYPE, length, stream, DEFAULT_BUFFER_SIZE);
	}
	
	public StreamRequestBody(String mimeType, long length, InputStream stream) {
		this(mimeType, length, stream, DEFAULT_BUFFER_SIZE);
	}

	public StreamRequestBody(String mimeType, long length, InputStream stream, int bufferSize) {
		if (mimeType == null) {
			throw new NullPointerException("mimeType");
		}
		if (stream == null) {
			throw new NullPointerException("stream");
		}
		if (bufferSize <= 0) {
			throw new IllegalArgumentException("bufferSize need > 0");
		}
		this.mimeType = mimeType;
		this.length = length;
		this.stream = stream;
		this.bufferSize = bufferSize;
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
	public void writeTo(OutputStream out) throws IOException {
		byte[] buffer = new byte[bufferSize];
		long writeSize = 0;
		try {
			int read;
			while ((read = stream.read(buffer)) != -1) {
				out.write(buffer, 0, read);
				writeSize += read;
				onWriteProgress(writeSize);
			}
		} finally {
			stream.close();
			onWriteFinish();
		}
	}
	
	protected void onWriteProgress(long writeSize) {
		if (onWriteListener != null) {
			try {
				onWriteListener.onWriteProgress(this, writeSize);
			} catch (Throwable th) {
			}
		}
	}
	
	protected void onWriteFinish() {
		if (onWriteListener != null) {
			try {
				onWriteListener.onWriteFinish(this);
			} catch (Throwable th) {
			}
		}
	}

	@Override
	public String toString() {
		return stream.toString() + " (" + mimeType() + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof StreamRequestBody) {
			StreamRequestBody rhs = (StreamRequestBody) o;
			return stream.equals(rhs.stream);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return stream.hashCode();
	}

	public InputStream getInputStream() {
		return stream;
	}

	public OnWriteListener getOnWriteListener() {
		return onWriteListener;
	}

	public void setOnWriteListener(OnWriteListener onWriteListener) {
		this.onWriteListener = onWriteListener;
	}
}
