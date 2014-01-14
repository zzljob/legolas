package com.yepstudio.android.legolas.http.mime;

public interface Body {
	
	final static String Content_Type = "Content-Type";
	final static String Content_Length = "Content-Length";
	
	String mimeType();

	long length();
}
