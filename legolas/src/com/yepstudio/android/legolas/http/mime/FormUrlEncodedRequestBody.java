package com.yepstudio.android.legolas.http.mime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

public final class FormUrlEncodedRequestBody implements RequestBody {
	final ByteArrayOutputStream content = new ByteArrayOutputStream();
	final static String CHARSET = "UTF-8";

	public void addField(String name, String value) {
		if (name == null) {
			throw new NullPointerException("name");
		}
		if (value == null) {
			throw new NullPointerException("value");
		}
		if (content.size() > 0) {
			content.write('&');
		}
		try {
			name = URLEncoder.encode(name, CHARSET);
			value = URLEncoder.encode(value, CHARSET);

			content.write(name.getBytes(CHARSET));
			content.write('=');
			content.write(value.getBytes(CHARSET));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String fileName() {
		return null;
	}

	@Override
	public String mimeType() {
		return "application/x-www-form-urlencoded; charset=" + CHARSET;
	}

	@Override
	public long length() {
		return content.size();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(content.toByteArray());
	}
}
