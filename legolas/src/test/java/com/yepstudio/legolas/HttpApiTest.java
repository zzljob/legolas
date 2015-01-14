package com.yepstudio.legolas;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yepstudio.legolas.LegolasOptions.CachePolicy;
import com.yepstudio.legolas.LegolasOptions.RecoveryPolicy;
import com.yepstudio.legolas.cache.disk.BasicDiskCache;
import com.yepstudio.legolas.converter.BasicConverter;
import com.yepstudio.legolas.httpsender.UrlConnectionHttpSender;
import com.yepstudio.legolas.internal.NoneLog;
import com.yepstudio.legolas.listener.LegolasListener;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.webapi.HttpApi;
import com.yepstudio.legolas.webapi.HttpApi.NewsTitleEntity;

public class HttpApiTest {

	private static Logger logger = LoggerFactory.getLogger(HttpApiTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Endpoint endpoint = Endpoints.newFixedEndpoint("http://jrsj1.data.fund123.cn", "金融数据1");
		LegolasOptions defaultHttpRequestOptions = new LegolasOptions.Builder()
				.cacheConverterResult(false)
				.cacheInMemory(true)
				.cachePolicy(CachePolicy.SERVER_CACHE_CONTROL)
				.recoveryPolicy(RecoveryPolicy.RESPONSE_NONE)
				.build(); 
		
		BasicDiskCache diskCache = new BasicDiskCache(new File("D:/cache"));
		LegolasConfiguration config = new LegolasConfiguration.Builder()
				.defaultEndpoints(endpoint)
				.defaultOptions(defaultHttpRequestOptions)
				.diskCache(diskCache)
				.defaultConverter(new BasicConverter())
				.httpSender(new UrlConnectionHttpSender())
				.legolasLog(new NoneLog())
				.build();
		Legolas.getInstance().init(config);
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
	public void testSyncGetCss() throws InterruptedException {
		HttpApi api = Legolas.getInstance().getApi(HttpApi.class);
		try {
			String response = api.getBaseCss();
			logger.debug("response:{}", response);
		} catch (Exception e) {
			e.getCause();
			logger.error("", e);
		}
		
		TimeUnit.SECONDS.sleep(10);
	}
	
	@Test
	public void testSyncApi() throws InterruptedException {
		HttpApi api = Legolas.getInstance().getApi(HttpApi.class);
		try {
			String response = api.syncGetNewsTitle(1, "", 1, 0, 100);
			logger.debug("response:{}", response);
		} catch (Exception e) {
			e.getCause();
			logger.error("", e);
		}

		TimeUnit.SECONDS.sleep(10);
	}

	@Test
	public void testSyncApiDto() {
		HttpApi api = Legolas.getInstance().getApi(HttpApi.class);
		NewsTitleEntity response = api.syncGetNewsTitleDto(null);
		logger.debug("response:{}", response);
	}
	
	@Test
	public void testAsyncApi() throws InterruptedException {
		HttpApi api = Legolas.getInstance().getApi(HttpApi.class);
		LegolasListener<String, String> listener = new LegolasListener<String, String>() {

			@Override
			public void onRequest(Request request) {
				logger.debug("onRequest....");
			}

			@Override
			public void onResponse(Request request, String response) {
				logger.debug("onResponse:", response);
			}

			@Override
			public void onError(Request request, LegolasException error, String response) {
				logger.debug("onError:", response);
				logger.debug("onError:", error);
			}
		};
		Request request = api.asyncNewsTitle(1, "", 1, 0, 100, listener);
		logger.debug("log:{}", request.getRequestLog());
		
		TimeUnit.MINUTES.sleep(10);
	}

}
