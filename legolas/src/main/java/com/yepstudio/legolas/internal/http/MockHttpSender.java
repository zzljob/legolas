package com.yepstudio.legolas.internal.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.LegolasLog;
import com.yepstudio.legolas.internal.SimpleProfiler;
import com.yepstudio.legolas.mime.ResponseBody;
import com.yepstudio.legolas.mime.StringBody;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.Response;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年5月6日
 * @version 2.0, 2014年5月6日
 *
 */
public class MockHttpSender implements HttpSender {
	
	private static LegolasLog log = LegolasLog.getClazz(MockHttpSender.class);
	
	public static final String RESPONSE_HEADER = "header";
	public static final String RESPONSE_DESCRIPTION = "description";
	public static final String RESPONSE_URL = "url";
	public static final String RESPONSE_METHOD = "method";
	public static final String RESPONSE_BODY = "body";
	
	public static final String RESPONSE_SUCCESS = "success";
	public static final String RESPONSE_RESULT = "result";
	
	@Override
	public Response execute(Request request) throws IOException {
		log.d(String.format("execute, request:[%s]", request.getUuid()));
		int status = 200;
		String reason = "Ok";
		Map<String, String> headers = new HashMap<String, String>();

		Map<String, Object> response = new HashMap<String, Object>();
		response.put(RESPONSE_HEADER, request.getHeaders());
		response.put(RESPONSE_DESCRIPTION, request.getDescription());
		response.put(RESPONSE_URL, request.getUrl());
		response.put(RESPONSE_METHOD, request.getMethod());
		response.put(RESPONSE_BODY, "");
		
		response.put(RESPONSE_SUCCESS, true);
		response.put(RESPONSE_RESULT, "");
		
		Gson gson = new Gson();
		String text = gson.toJson(response);
		log.d(String.format("execute finished, response:%s", text));
		ResponseBody body = new StringBody(text);
		return new Response(status, reason, headers, body);
	}

}

