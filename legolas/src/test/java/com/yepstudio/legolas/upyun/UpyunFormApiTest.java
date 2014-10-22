package com.yepstudio.legolas.upyun;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yepstudio.legolas.Endpoints;
import com.yepstudio.legolas.Legolas;
import com.yepstudio.legolas.LegolasException;
import com.yepstudio.legolas.request.OnRequestListener;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.OnResponseListener;

public class UpyunFormApiTest {
	
	private Logger logger = LoggerFactory.getLogger(UpyunFormApiTest.class);

	@Test
	public void test() {
		Legolas legolas = new Legolas.Build().setDefaultEndpoint(Endpoints.newFixedEndpoint("http://v0.api.upyun.com", "又拍云存储")).create();

		String bucket = "yehiimg";
		String secret = "QG61cXN9pjOIRwD25tb2R5MScQg=";
		PolicyPart policy = new PolicyPart(bucket);
		SignaturePart signature = new SignaturePart(policy, secret);
		
		File file = new File("D:/xx.jpg");
		
		final CountDownLatch countDown = new CountDownLatch(2);
		
		OnRequestListener requestListener = new OnRequestListener() {

			@Override
			public void onRequest(Request arg0) {
				logger.debug("开始请求");
				countDown.countDown();
			}
		};
		
		OnResponseListener<String> responseListener = new OnResponseListener<String>() {

			@Override
			public void onResponse(String arg0) {
				logger.debug("请求返回了:{}", arg0);
				countDown.countDown();
			}
		};
		
		OnErrorListener errorListener = new OnErrorListener() {

			@Override
			public void onError(LegolasException arg0) {
				logger.debug("出错了");
				countDown.countDown();
			}
			
		};
		
		UpyunFormApi api = legolas.newInstance(UpyunFormApi.class);
		Request request = api.upload(bucket, policy, signature, file, requestListener, responseListener, errorListener);
		
		try {
			countDown.await(2, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			
		}
	}

}
