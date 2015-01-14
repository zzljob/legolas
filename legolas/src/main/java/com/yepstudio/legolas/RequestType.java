package com.yepstudio.legolas;

public enum RequestType {
	/** No content-specific logic required. */
	SIMPLE,
	/** Multi-part request body. */
	MULTIPART,
	/** Form URL-encoded request body. */
	FORM_URL_ENCODED
}
