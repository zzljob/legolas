package com.yepstudio.legolas.converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Date;

import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.mime.FileBody;
import com.yepstudio.legolas.mime.RequestBody;
import com.yepstudio.legolas.mime.ResponseBody;
import com.yepstudio.legolas.mime.StringBody;
import com.yepstudio.legolas.response.Response;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年5月16日
 * @version 2.0, 2014年5月16日
 *
 */
public class BasicConverter extends AbstractConverter {
	
	private static LegolasLog log = LegolasLog.getClazz(BasicConverter.class);
	private final String defaultCharset;

	private static final int BUFFER_SIZE = 0x1000;
	
	public BasicConverter() {
		this("UTF-8");
	}
	
	public BasicConverter(String defaultCharset) {
		super();
		this.defaultCharset = defaultCharset;
	}

	/**
	 * 可转换的类型包括:
	 * <ul>
	 * <li>RequestBody</li>
	 * <li>File</li>
	 * <li>String</li>
	 * <li>StringBuilder</li>
	 * <li>StringBuffer</li>
	 * </ul>
	 */
	@Override
	public Object fromBody(ResponseBody body, Type clazz) throws Exception {
		if (body == null) {
			new NullPointerException("ResponseBody can not be null");
		}
		log.d("fromBody => " + clazz);
		if (clazz == RequestBody.class) {
			return body;
		} else if (clazz == String.class) {
			String charset = Response.parseCharset(body.mimeType(), defaultCharset);
			log.v("fromBody, charset:" + charset);
			return readToString(body, charset);
		} else if (clazz == StringBuilder.class) {
			return readToStringBuilder(body);
		} else if (clazz == StringBuffer.class) {
			return readToStringBuffer(body);
		} else if (clazz == File.class) {
			return writeToFile(body);
		} else {
			log.d("BasicConverter is not supported this type : " + clazz);
			throw new Exception("not supported this type : " + clazz);
		}
	}
	
	protected StringBuffer readToStringBuffer(ResponseBody body) throws Exception {
		if (body == null) {
			return null;
		}
		InputStream inputStream = null;
		StringBuffer buffer = new StringBuffer();
		try {
			inputStream = body.read();
			buffer.append(Response.streamToBytes(inputStream));
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
			}
		}
		return buffer;
	}
	
	protected StringBuilder readToStringBuilder(ResponseBody body) throws Exception {
		if (body == null) {
			return null;
		}
		InputStream inputStream = null;
		StringBuilder builder = new StringBuilder();
		try {
			inputStream = body.read();
			builder.append(Response.streamToBytes(inputStream));
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
			}
		}
		return builder;
	}
	
	protected String readToString(ResponseBody body, String charset) throws Exception {
		if (body == null) {
			return null;
		}
		InputStream inputStream = null;
		try {
			inputStream = body.read();
			return new String(Response.streamToBytes(inputStream), charset);
		} catch (UnsupportedEncodingException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
			}
		}
	}
	
	protected File writeToFile(ResponseBody body) throws Exception {
		if (body == null) {
			return null;
		}
		FileOutputStream outputStream = null;
		InputStream inputStream = null;
		File file = null;
		try {
			file = File.createTempFile("legolas", "body");
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
		return file;
	}

	/**
	 * 可转换的类型包括:
	 * <ul>
	 * <li>RequestBody</li>
	 * <li>File</li>
	 * <li>Date</li>
	 * <li>String</li>
	 * <li>Others call toString()</li>
	 * </ul>
	 */
	@Override
	public RequestBody toBody(Object object) {
		if (object == null) {
			return new StringBody("");
		}
		if (object instanceof RequestBody) {
			return (RequestBody) object;
		} else if (object instanceof File) {
			return new FileBody("application/octet-stream", (File) object);
		} else if (object instanceof Date) {
			return new StringBody(String.valueOf(((Date) object).getTime()));
		} else if (object instanceof String) {
			return new StringBody((String) object);
		} else {
			log.d("BasicConverter is not supported this type toBody : " + object.getClass());
			return null;
		}
	}

}
