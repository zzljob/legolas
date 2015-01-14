package com.yepstudio.legolas.mime;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2014年12月17日
 * @version 1.0，2014年12月17日
 *
 */
public final class MultipartRequestBody implements RequestBody {

	public static final String DEFAULT_CHARSET = "UTF-8";
	
	private final List<MimePart> mimeParts = new LinkedList<MimePart>();

	private final String charset;
	private final byte[] footer;
	private final String boundary;
	private final AtomicLong length = new AtomicLong(0);

	public MultipartRequestBody() {
		this(DEFAULT_CHARSET);
	}
	
	public MultipartRequestBody(String charset) {
		this(UUID.randomUUID().toString(), charset);
	}

	MultipartRequestBody(String boundary, String charset) {
		this.charset = charset;
		this.boundary = boundary;
		footer = buildBoundary(boundary, charset, false, true);
		length.addAndGet(footer.length);
	}

	public synchronized void addPart(String name, RequestBody body) {
		if (name == null) {
			throw new NullPointerException("Part name must not be null.");
		}
		if (body == null) {
			throw new NullPointerException("Part body must not be null.");
		}

		MimePart part = new MimePart(name, body, charset, boundary, mimeParts.isEmpty());
		mimeParts.add(part);

		length.addAndGet(part.size());
	}

	public int getPartCount() {
		return mimeParts.size();
	}

	@Override
	public String mimeType() {
		return "multipart/form-data; boundary=" + boundary;
	}

	@Override
	public long length() {
		return length.get();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		for (MimePart part : mimeParts) {
			part.writeTo(out);
		}
		out.write(footer);
	}

	private static byte[] buildBoundary(String boundary, String charset, boolean first, boolean last) {
		try {
			StringBuilder sb = new StringBuilder();
			if (!first) {
				sb.append("\r\n");
			}
			sb.append("--");
			sb.append(boundary);
			if (last) {
				sb.append("--");
			} else {
				sb.append("\r\n");
			}
			return sb.toString().getBytes(charset);
		} catch (IOException ex) {
			throw new RuntimeException("Unable to write multipart boundary", ex);
		}
	}
	
	public static final class MimePart {
		private final RequestBody body;
		private final String name;
		private final boolean isFirst;
		private final String boundary;

		private byte[] partBoundary;
		private byte[] partHeader;
		private boolean isBuilt;
		private final String charset;

		private MimePart(String name, RequestBody body, String charset, String boundary, boolean isFirst) {
			this.name = name;
			this.body = body;
			this.charset = charset;
			this.isFirst = isFirst;
			this.boundary = boundary;
		}

		private void writeTo(OutputStream out) throws IOException {
			build();
			out.write(partBoundary);
			out.write(partHeader);
			body.writeTo(out);
		}

		private long size() {
			build();
			if (body.length() > -1) {
				return body.length() + partBoundary.length + partHeader.length;
			} else {
				return -1;
			}
		}

		private void build() {
			if (isBuilt) {
				return;
			}
			partBoundary = buildBoundary(boundary, charset, isFirst, false);
			partHeader = buildHeader(name, body, charset);
			isBuilt = true;
		}

		public RequestBody getBody() {
			return body;
		}

		public String getName() {
			return name;
		}

		public String getBoundary() {
			return boundary;
		}
		
		public boolean isFile() {
			return body instanceof FileRequestBody;
		}
	}

	private static byte[] buildHeader(String name, RequestBody value, String charset) {
		try {
			StringBuilder headers = new StringBuilder();
			headers.append("Content-Disposition: form-data; name=\"").append(name);
			if (value instanceof FileRequestBody) {
				FileRequestBody fileBody = (FileRequestBody) value;
				if (fileBody.fileName() != null && "".equalsIgnoreCase(fileBody.fileName().trim())) {
					headers.append("\"; filename=\"");
					headers.append(fileBody.fileName());
				}
			}
			headers.append("\"\r\nContent-Type: ");
			headers.append(value.mimeType());
			if (value.length() != -1) {
				headers.append("\r\nContent-Length: ").append(value.length());
			}
			headers.append("\r\nContent-Transfer-Encoding: binary\r\n\r\n");
			return headers.toString().getBytes(charset);
		} catch (IOException ex) {
			throw new RuntimeException("Unable to write multipart header", ex);
		}
	}
	
	public int getFilePartCount() {
		int count = 0;
		for (MimePart p : mimeParts) {
			if (p.isFile()) {
				count++;
			}
		}
		return count;
	}

	public List<MimePart> getMimeParts() {
		return mimeParts;
	}
}
