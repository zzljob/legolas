package com.yepstudio.legolas.bcs;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.Endpoint;
import com.yepstudio.legolas.Endpoints;
import com.yepstudio.legolas.Legolas;
import com.yepstudio.legolas.LegolasException;
import com.yepstudio.legolas.converter.JSONConverter;
import com.yepstudio.legolas.request.OnRequestListener;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.OnResponseListener;

public class BcsApiTest {
	private static Logger logger = LoggerFactory.getLogger(BcsApiTest.class);
	
	private static BcsApi bcsApi;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Endpoint  defaultEndpoint = Endpoints.newFixedEndpoint("http://bcs.duapp.com");
		Converter converter = new JSONConverter();
		Legolas legolas = new Legolas.Build().setDefaultEndpoint(defaultEndpoint).setDefaultConverter(converter).create();
		bcsApi = legolas.newInstance(BcsApi.class);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
	}

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testAsyncGetObjectList() throws InterruptedException {
		final CountDownLatch countDown = new CountDownLatch(2);
		OnRequestListener requestListener = new OnRequestListener() {

			@Override
			public void onRequest(Request request) {
				logger.info("requestListener:{}, url:{}", request.getUuid(), request.getUrl());
				countDown.countDown();
			}

		};
		OnResponseListener<JSONObject> responseListener = new OnResponseListener<JSONObject>() {

			@Override
			public void onResponse(JSONObject response) {
				countDown.countDown();
				logger.info("responseListener:{}", response.toString());
			}

		};
		OnErrorListener errorListener = new OnErrorListener() {

			@Override
			public void onError(LegolasException error) {
				countDown.countDown();
				logger.info("errorListener");
			}

		};
		BcsSign sign = new BcsSign();
		sign.setMethod("GET");
		sign.setBucket("rebirth");
		sign.setObject("/");
		Request request = bcsApi.getObjectList("rebirth", sign, requestListener, responseListener, errorListener);
		countDown.await(20, TimeUnit.SECONDS);
		logger.info("finish");
	}
	
	@Test
	public void testSyncGetObjectList() throws InterruptedException {
		BcsSign sign = new BcsSign();
		sign.setMethod("GET");
		sign.setBucket("rebirth");
		sign.setObject("/");
		JSONObject json = bcsApi.getObjectList("rebirth", sign);
		logger.info("finish:{}", json.toString());
	}

}
