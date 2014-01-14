package com.yepstudio.android.legolas.http.mime;

import java.io.IOException;
import java.io.InputStream;

public interface ResponseBody extends Body {

	InputStream read() throws IOException;

}
