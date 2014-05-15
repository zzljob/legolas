package com.yepstudio.legolas.httpsender;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.yepstudio.legolas.HttpSender;
import com.yepstudio.legolas.LegolasLog;
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
	
	private final int waitSeconds;
	private final HttpSender httpSender;
	private Map<String, String> mockResponseMap = new ConcurrentHashMap<String, String>();

	public MockHttpSender() {
		this(new UrlConnectionHttpSender(), 3);
	}
	
	public MockHttpSender(HttpSender httpSender, int waitSeconds) {
		super();
		this.httpSender = httpSender;
		if (waitSeconds <= 0) {
			throw new IllegalArgumentException("waitSeconds must > 0");
		}
		this.waitSeconds = waitSeconds;
	}

	@Override
	public Response execute(Request request) throws IOException {
		Response response = getMockResponse(request);
		if (response == null) {
			return httpSender.execute(request);
		}
		try {
			TimeUnit.SECONDS.sleep(waitSeconds);
		} catch (InterruptedException e) {
			
		}
		return response;
	}

	protected Response getMockResponse(Request request) {
		String text = mockResponseMap.get(makeKey(request.getMethod(), request.getUrl()));
		if (text == null || text.trim().length() < 1) {
			return null;
		}
		return makeResponse(text);
	}

	protected Response makeResponse(String text) {
		int status = 200;
		String reason = "Ok";
		Map<String, String> headers = new HashMap<String, String>();
		log.d(String.format("execute finished, response:%s", text));
		ResponseBody body = new StringBody(text);
		return new Response(status, reason, headers, body);
	}

	protected String makeKey(String method, String url) {
		return String.format("%s_%s", method, url);
	}

	public void putMockResponse(String method, String url, String response) {
		mockResponseMap.put(makeKey(method, url), response);
	}
}
