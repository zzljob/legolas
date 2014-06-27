package com.yepstudio.legolas;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.yepstudio.legolas.httpsender.MockHttpSender;
import com.yepstudio.legolas.request.OnRequestListener;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.OnResponseListener;

public class LegolasTest {

	private static Logger logger = LoggerFactory.getLogger(LegolasTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		logger.trace("setUpBeforeClass");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		logger.trace("tearDownAfterClass");
	}
	
	@Before
	public void setUp() throws Exception {
		logger.trace("setUp");
	}

	@After
	public void tearDown() throws Exception {
		logger.trace("tearDown");
	}

	@Test
	public void testRequest1() throws InterruptedException, IOException {
		logger.trace("---------------------------------testRequest1-----------------------------------");
		
		RequestInterceptor interceptor = new RequestInterceptor() {

			@Override
			public void interceptor(RequestInterceptorFace face) {
				logger.info(face.getRequestUrl(false));
				logger.info(face.getRequestUrl(true));
			}
			
		};
		
		Legolas legolas = new Legolas.Build()
											.setDefaultEndpoint(Endpoints.newStaticIpServer("http://rebirth.duapp.com", "12312"))
											.create();
		
		HttpApi api = legolas.newInstance(HttpApi.class);
		Request request = api.doGet("check", "com.yepstudio.geekpark", "APK");
		TimeUnit.SECONDS.sleep(10);
	}
	
	public void testRequest2() throws InterruptedException {
		logger.trace("---------------------------------testRequest2-----------------------------------");
		MockHttpSender httpSender = new MockHttpSender();
		String url = "http://tools.fund123.cn/Redirect/Update.ashx?aa_a=0.0&xxx=2&aaa=0.0&zx=11&aAaxxxx_a=false&aa=0&xx=1";
		
		CheckResult result = new CheckResult();
		result.setSuccess(true);
		CheckResult.Response r = new CheckResult.Response();
		r.setApp("xxxxxxxx");
		result.setResponse(r);
		Gson gson = new Gson();
		String text = gson.toJson(result);
		logger.info("json : {}", text);
		httpSender.putMockResponse("GET", url, text);
		
		Legolas legolas = new Legolas.Build().setDefaultEndpoint(Endpoints.newFixedEndpoint("http://tools.fund123.cn"))
				.setHttpSender(httpSender)
				.create();

		Map<String, Object> defaultHeaders = new HashMap<String, Object>();
		defaultHeaders.put("own", "defaultHeaders");
		defaultHeaders.put("defaultHeaders", "defaultHeadersxxxx");
		legolas.setDynamicHeaders(HttpApi.class, defaultHeaders);
		
		ToolHttpApi api = legolas.newInstance(ToolHttpApi.class);
		
		OnRequestListener requestListener = new OnRequestListener() {
			
			@Override
			public void onRequest(Request request) {
				logger.info("requestListener:{}, url:{}", request.getUuid(), request.getUrl());
			}
			
		};
		OnResponseListener<CheckResult> responseListener = new OnResponseListener<CheckResult>() {
			
			@Override
			public void onResponse(CheckResult response) {
				logger.info("responseListener:{}, getApp:{}", response.getSuccess(), response.getResponse().getApp());
			}
			
		};
		OnErrorListener errorListener = new OnErrorListener() {
			
			@Override
			public void onError(LegolasException error) {
				logger.info("errorListener");
			}
			
		};
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("zx", 11);
		Request request = api.update(1, 2, map, new AdddDTO(), responseListener, errorListener);
		TimeUnit.SECONDS.sleep(10);
	}
	
	public void testRequest3() throws InterruptedException {
		logger.trace("---------------------------------testRequest3-----------------------------------");
		Legolas legolas = new Legolas.Build().setDefaultEndpoint(Endpoints.newFixedEndpoint("http://rebirth.duapp.com")).create();
		
		Map<String, Object> defaultHeaders = new HashMap<String, Object>();
		defaultHeaders.put("own", "defaultHeaders");
		defaultHeaders.put("defaultHeaders", "defaultHeadersxxxx");
		legolas.setDynamicHeaders(HttpApi.class, defaultHeaders);
		
		HttpApi api = legolas.newInstance(HttpApi.class);
		
		OnRequestListener requestListener = new OnRequestListener() {
			
			@Override
			public void onRequest(Request request) {
				logger.info("requestListener:{}, url:{}", request.getUuid(), request.getUrl());
			}
			
		};
		OnResponseListener<CheckResult> responseListener = new OnResponseListener<CheckResult>() {
			
			@Override
			public void onResponse(CheckResult response) {
				logger.info("responseListener, getSuccess:{}, Re", response.getSuccess());
			}
			
		};
		OnErrorListener errorListener = new OnErrorListener() {
			
			@Override
			public void onError(LegolasException error) {
				logger.info("errorListener");
			}
			
		};
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("zx", 11);
		Request request = api.update(1, 2, map, new AdddDTO(), responseListener, errorListener);
		TimeUnit.SECONDS.sleep(10);
	}

}
