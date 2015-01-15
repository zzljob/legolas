package com.yepstudio.legolas.mime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
public class FileResponseBody extends StreamResponseBody {

	private static final int DEFAULT_BUFFER_SIZE = 4096;
	private final File file;

	public static FileResponseBody build(ResponseBody body, File file) throws IOException {
		return build(body, file, DEFAULT_BUFFER_SIZE);
	}

	public static FileResponseBody build(ResponseBody body, File file, int bufferSize) throws IOException {
		if (body == null || file == null) {
			return null;
		}
		if (body instanceof FileResponseBody) {
			return (FileResponseBody) body;
		} else {
			File dirFile = file.getParentFile();
			if (!dirFile.exists()) {
				dirFile.mkdirs();
			}
			if (file.exists()) {
				file.delete();
			}
			long length = 0;
			InputStream inputStream = body.read();
			FileOutputStream outputStream = new FileOutputStream(file);
			byte[] buffer = new byte[bufferSize];
			try {
				int read;
				while ((read = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, read);
					length += read;
				}
			} finally {
				try {
					outputStream.flush();
					outputStream.close();
				} catch (IOException e) {
				}
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
			return new FileResponseBody(body.mimeType(), length, file);
		}
	}

	public FileResponseBody(String mimeType, long length, File file) throws IOException {
		super(mimeType, length, new FileInputStream(file));
		this.file = file;
	}

	public File getFile() {
		return file;
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
		if (o instanceof FileResponseBody) {
			FileResponseBody rhs = (FileResponseBody) o;
			return file.equals(rhs.file);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return file.hashCode();
	}

}
