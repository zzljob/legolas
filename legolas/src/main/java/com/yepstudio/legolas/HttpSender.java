package com.yepstudio.legolas;

import java.io.IOException;

import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.Response;

public interface HttpSender {

	/**
	 * 执行网络请求
	 * @param request
	 * @return
	 * @throws IOException
	 */
	Response execute(Request request) throws IOException;
	
	
}
