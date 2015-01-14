package com.yepstudio.legolas.mime;

import java.io.IOException;
import java.io.OutputStream;

public interface RequestBody extends Body {
	
	void writeTo(OutputStream out) throws IOException;
	
	public interface OnWriteListener {
		public void onWriteProgress(RequestBody body, long readSize);
		public void onWriteFinish(RequestBody body);
	}

}
