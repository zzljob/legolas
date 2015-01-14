package com.yepstudio.legolas.mime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2014年12月17日
 * @version 1.0，2014年12月17日
 *
 */
public final class FileRequestBody extends StreamRequestBody {
	public static final int DEFAULT_BUFFER_SIZE = 1024;
	public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	private final File file;

	/**
	 * Constructs a new typed file.
	 * @throws IOException 
	 * 
	 * @throws NullPointerException
	 *             if file or mimeType is null
	 */
	public FileRequestBody(File file) throws IOException {
		this(DEFAULT_MIME_TYPE, file);
	}

	public FileRequestBody(String mimeType, File file) throws IOException {
		this(mimeType, file, DEFAULT_BUFFER_SIZE);
	}

	public FileRequestBody(String mimeType, File file, int bufferSize) throws IOException {
		super(mimeType, file.getUsableSpace(), new FileInputStream(file), bufferSize);
		this.file = file;
		if (!file.exists()) {
			throw new IllegalArgumentException("file is no exists");
		}
	}

	/** Returns the file. */
	public File file() {
		return file;
	}

	public String fileName() {
		return file != null ? file.getName() : null;
	}

	/**
	 * Atomically moves the contents of this file to a new location.
	 * 
	 * @param destination
	 *            file
	 * @throws java.io.IOException
	 *             if the move fails
	 */
	public void moveTo(FileRequestBody destination) throws IOException {
		if (!mimeType().equals(destination.mimeType())) {
			throw new IOException("Type mismatch.");
		}
		if (!file.renameTo(destination.file())) {
			throw new IOException("Rename failed!");
		}
	}

	@Override
	public String toString() {
		return file.getAbsolutePath() + " (" + mimeType() + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof FileRequestBody) {
			FileRequestBody rhs = (FileRequestBody) o;
			return file.equals(rhs.file);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return file.hashCode();
	}

//	@Override
//	public InputStream read() throws IOException {
//		if (fileInited.get()) {
//			return stream;
//		}
//		try {
//			InputStream responseStream = super.read();
//			FileOutputStream outStream = new FileOutputStream(file);
//			long readSize = 0;
//			if (responseStream != null) {
//				byte[] buf = new byte[bufferSize];
//				int r;
//				while ((r = responseStream.read(buf)) != -1) {
//					outStream.write(buf, 0, r);
//					readSize += r;
//					if (onResponseListener != null) {
//						onResponseListener.onReadProgress(this, readSize);
//					}
//				}
//				responseStream.close();
//				outStream.flush();
//				outStream.close();
//				if (onResponseListener != null) {
//					onResponseListener.onReadFinish(this);
//				}
//			}
//			stream = new FileInputStream(file);
//			return stream;
//		} finally {
//			fileInited.set(true);
//		}
//	}
	
	
}
