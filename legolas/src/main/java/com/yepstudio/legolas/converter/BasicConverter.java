package com.yepstudio.legolas.converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.exception.ConversionException;
import com.yepstudio.legolas.internal.TypesHelper;
import com.yepstudio.legolas.mime.ByteArrayResponseBody;
import com.yepstudio.legolas.mime.FileResponseBody;
import com.yepstudio.legolas.mime.ResponseBody;
import com.yepstudio.legolas.response.Response;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2015年1月4日
 * @version 1.0，2015年1月4日
 *
 */
public class BasicConverter implements Converter {
	
	private static final int BUFFER_SIZE = 0x1000;
	private final String defaultCharset;
	
	public BasicConverter() {
		this("UTF-8");
	}
	
	public BasicConverter(String defaultCharset) {
		this.defaultCharset = defaultCharset;
	}
	
	public Object convert(Response response, Type type) throws ConversionException {
		if (response == null || response.getBody() == null || type == null) {
			throw new NullPointerException("Response、ResponseBody 、Type not be null");
		}
		String charset = null;
		try{
			Class<?> clazz = TypesHelper.getRawType(type);
			if (clazz.isAssignableFrom(ResponseBody.class)) {
				return response.getBody();
			} else if (clazz == String.class) {
				charset = response.parseCharset(defaultCharset);
				ByteArrayResponseBody body = ByteArrayResponseBody.build(response.getBody());
				return new String(body.getBytes(), charset);
			} else if (clazz == StringBuilder.class) {
				ByteArrayResponseBody body = ByteArrayResponseBody.build(response.getBody());
				StringBuilder builder = new StringBuilder();
				builder.append(body.getBytes());
				return builder;
			} else if (clazz == StringBuffer.class) {
				ByteArrayResponseBody body = ByteArrayResponseBody.build(response.getBody());
				StringBuffer builder = new StringBuffer();
				builder.append(body.getBytes());
				return builder;
			} else if (clazz == File.class) {
				if (response.getBody() instanceof FileResponseBody) {
					FileResponseBody body = (FileResponseBody) response.getBody();
					return body.getFile();
				} else {
					File file = generateFile();
					writeToFile(response.getBody(), file);
					return file;
				}
			} else {
				throw generateException(response, type, "Unsupported Convert Type : " + type, null);
			}
		} catch (UnsupportedEncodingException e) {
			throw generateException(response, type, "Unsupported Encoding : " + charset, e);
		} catch (IOException e) {
			throw generateException(response, type, "has IOException", e);
		} 
	}
	
	protected ConversionException generateException(Response response, Type type, String message, Exception e) {
		ConversionException ce;
		if (e == null) {
			ce = new ConversionException(message);
		} else {
			ce = new ConversionException(message, e);
		}
		ce.setResponse(response);
		ce.setConversionType(type);
		return ce;
	}
	
	protected File generateFile() throws IOException {
		return File.createTempFile("legolas", "body");
	}
	
	public boolean isSupport(Type type) {
		Class<?> clazz = TypesHelper.getRawType(type);
		if (clazz.isAssignableFrom(ResponseBody.class)) {
			return true;
		} else if (File.class.equals(clazz)) {
			return true;
		} else if (String.class.equals(clazz)) {
			return true;
		} else if (StringBuilder.class.equals(clazz)) {
			return true;
		} else if (StringBuffer.class.equals(clazz)) {
			return true;
		}
		return false;
	}
	
	protected void writeToFile(ResponseBody body, File file) throws IOException {
		if (body == null || file == null) {
			return ;
		}
		FileOutputStream outputStream = null;
		InputStream inputStream = null;
		try {
			file.getParentFile().mkdirs();
			outputStream = new FileOutputStream(file);
			inputStream = body.read();
			if (inputStream != null) {
				byte[] buf = new byte[BUFFER_SIZE];
				int r;
				while ((r = inputStream.read(buf)) != -1) {
					outputStream.write(buf, 0, r);
				}
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (outputStream != null) {
				try {
					outputStream.flush();
					outputStream.close();
				} catch (IOException e) {
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public String getDefaultCharset() {
		return defaultCharset;
	}

}
