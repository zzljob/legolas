package com.yepstudio.legolas.mime;

import java.io.IOException;
import java.io.InputStream;

public interface ResponseBody extends Body {

	InputStream read() throws IOException;

	public interface OnReadListener {
		public void onReadProgress(ResponseBody body, long readSize);
		public void onReadFinish(ResponseBody body);
	}

}
