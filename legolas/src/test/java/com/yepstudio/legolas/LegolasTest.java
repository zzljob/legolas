package com.yepstudio.legolas;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		logger.trace("testRequest1");
		
		RequestInterceptor interceptor = new RequestInterceptor() {

			@Override
			public void interceptor(RequestInterceptorFace face) {
				logger.info(face.getRequestUrl());
			}
			
		};
		
		Legolas legolas = new Legolas.Build()
											.setDefaultEndpoint(Endpoints.newFixedEndpoint("http://rebirth.duapp.com"))
											.setRequestInterceptor(interceptor)
											.create();
		
		HttpApi api = legolas.newInstance(HttpApi.class);
		Request request = api.doGet("check", "com.yepstudio.geekpark", "APK");
		TimeUnit.SECONDS.sleep(10);
	}
	
	public void testRequest2() {
		OnRequestListener requestListener = new OnRequestListener() {

			@Override
			public void onRequest(Request request) {
				
			}
			
		};
		OnResponseListener<JSONArray> responseListener = new OnResponseListener<JSONArray>() {

			@Override
			public void onResponse(JSONArray response) {
				
			}
			
		};
		OnErrorListener errorListener = new OnErrorListener() {

			@Override
			public void onError(LegolasError error) {
				
			}
			
		};
	}

}
