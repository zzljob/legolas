package com.yepstudio.legolas.mime;

import java.io.IOException;
import java.io.OutputStream;

public interface RequestBody extends Body {
	
	String fileName();

	void writeTo(OutputStream out) throws IOException;

}
