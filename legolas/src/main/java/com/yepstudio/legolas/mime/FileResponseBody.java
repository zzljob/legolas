package com.yepstudio.legolas.mime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2014年12月31日
 * @version 1.0，2014年12月31日
 *
 */
public class FileResponseBody extends StreamResponseBody {

	private final File file;

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
