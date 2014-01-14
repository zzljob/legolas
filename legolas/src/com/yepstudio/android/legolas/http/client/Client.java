package com.yepstudio.android.legolas.http.client;

import java.io.IOException;

import com.yepstudio.android.legolas.http.Request;
import com.yepstudio.android.legolas.http.Response;

public interface Client {

	/**
	 * 执行网络请求
	 * @param request
	 * @return
	 * @throws IOException
	 */
	Response execute(Request request) throws IOException;

}
