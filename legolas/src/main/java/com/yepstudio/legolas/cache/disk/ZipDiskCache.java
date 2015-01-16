package com.yepstudio.legolas.cache.disk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.attribute.FileTime;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.yepstudio.legolas.mime.FileResponseBody;
import com.yepstudio.legolas.mime.ResponseBody;

public class ZipDiskCache extends BasicDiskCache {
	
	private final Charset charset;

	public ZipDiskCache(File rootDirectory, int maxCacheSizeInBytes, Charset charset) {
		super(rootDirectory, maxCacheSizeInBytes);
		this.charset = charset;
	}

	public ZipDiskCache(File rootDirectory, Charset charset) {
		super(rootDirectory);
		this.charset = charset;
	}
	
	public ZipDiskCache(File rootDirectory) {
		this(rootDirectory, Charset.forName("UTF-8"));
	}
	
	@Override
	protected Checksum writeResponseBodyToFile(ResponseBody body, File file, int bufferSize) throws IOException {
		ZipOutputStream zipFileOut = null;
		FileOutputStream fileOut = null;
		InputStream input = null;
		CheckedInputStream checkedInput = null;
		try {
			fileOut = new FileOutputStream(file, false);
			zipFileOut = new ZipOutputStream(fileOut, charset);
			
			ZipEntry en=new ZipEntry("cachefile");
			en.setComment("cachefile");
			en.setCreationTime(FileTime.fromMillis(System.currentTimeMillis()));
			zipFileOut.putNextEntry(en); 
			
			if (body instanceof FileResponseBody) {
				input = new FileInputStream(((FileResponseBody) body).getFile());
			} else {
				input = body.read();
			}

			checkedInput = new CheckedInputStream(input, createChecksum());
			//只写一个文件就可以了
			byte[] buffer = new byte[bufferSize];
			int read;
			while ((read = checkedInput.read(buffer)) != -1) {
				zipFileOut.write(buffer, 0, read);
			}
			
			zipFileOut.closeEntry();
			return checkedInput.getChecksum();
		} finally {
			closeStream(fileOut);
			closeStream(zipFileOut);
			closeStream(input);
			closeStream(checkedInput);
		}
	}

	@Override
	protected byte[] readFileToBytes(File file, int bufferSize, Checksum checksum) throws IOException {
		FileInputStream input = null;
		ZipInputStream zipInput = null;
		InputStream zipEntryInput = null;
		
		ByteArrayOutputStream output = null;
		CheckedOutputStream checkedOutput = null;
		try {
			input = new FileInputStream(file);
			zipInput = new ZipInputStream(input, charset);

			zipInput.getNextEntry();
			
			output = new ByteArrayOutputStream();
			checkedOutput = new CheckedOutputStream(output, checksum);
			byte[] buf = new byte[bufferSize];
			int r;
			while ((r = zipInput.read(buf, 0, bufferSize)) != -1) {
				checkedOutput.write(buf, 0, r);
			}
			return output.toByteArray();
		} finally {
			closeStream(input);
			closeStream(zipInput);
			closeStream(zipEntryInput);
			closeStream(output);
			closeStream(checkedOutput);
		}
	}
	
	protected File getTempFileByZipFile(File file) {
		return new File(file.getAbsolutePath() + ".temp");
	}

	@Override
	protected InputStream readFileToStream(File file, int bufferSize, Checksum checksum) throws IOException {
		FileInputStream input = null;
		ZipInputStream zipInput = null;
		
		File tempFile = getTempFileByZipFile(file);
		FileOutputStream temFileOut = null;
		CheckedOutputStream checkTempFileOut = null;
		try {
			input = new FileInputStream(file);
			zipInput = new ZipInputStream(input, charset);
			ZipEntry zipEntry = zipInput.getNextEntry();
			if (zipEntry != null) {
				temFileOut = new FileOutputStream(tempFile, false);
				checkTempFileOut = new CheckedOutputStream(temFileOut, checksum);
				writeStream(zipInput, checkTempFileOut, bufferSize);
			}
		} finally {
			closeStream(input);
			closeStream(zipInput);
			closeStream(checkTempFileOut);
			closeStream(temFileOut);
		}
		tempFile.deleteOnExit();
		return new FileInputStream(tempFile);
	}

	@Override
	protected String getConfigFileSuffix() {
		return ".config";
	}
	
	protected String getFileSuffix() {
		return ".zip";
	}
	

}
