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
import com.yepstudio.legolas.cache.disk.SafeDiskCache;
import com.yepstudio.legolas.cache.disk.ZipDiskCache;
import com.yepstudio.legolas.converter.GsonConverter;
import com.yepstudio.legolas.httpsender.UrlConnectionHttpSender;
import com.yepstudio.legolas.internal.Sl4fLog;
import com.yepstudio.legolas.listener.LegolasListener;
import com.yepstudio.legolas.mime.ResponseBody;
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
				.cacheInMemory(false)
				.cacheOnDisk(true)
				.cachePolicy(CachePolicy.SERVER_CACHE_CONTROL)
				.recoveryPolicy(RecoveryPolicy.RESPONSE_ERROR)
				.build(); 
		
		BasicDiskCache diskCache = new SafeDiskCache(new File("D:/cache"));
		LegolasConfiguration config = new LegolasConfiguration.Builder()
				.defaultEndpoints(endpoint)
				.converterResultMaxExpired(10, TimeUnit.MINUTES)
				.defaultOptions(defaultHttpRequestOptions)
				.diskCache(diskCache)
				.defaultConverter(new GsonConverter())
				.httpSender(new UrlConnectionHttpSender())
				.legolasLog(new Sl4fLog())
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
			String body = api.getBaseCss();
			body = api.getBaseCss();
			body = api.getBaseCss();
			body = api.getBaseCss();
			body = api.getBaseCss();
			body = api.getBaseCss();
			logger.debug("response:{}", body);
		} catch (Exception e) {
			logger.error("load fail", e.getCause());
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
		LegolasListener<NewsTitleEntity, NewsTitleEntity> listener = new LegolasListener<NewsTitleEntity, NewsTitleEntity>() {

			@Override
			public void onRequest(Request request) {
				logger.debug("onRequest....");
			}

			@Override
			public void onResponse(Request request, NewsTitleEntity response) {
				logger.debug("onResponse:", response);
			}

			@Override
			public void onError(Request request, LegolasException error, NewsTitleEntity response) {
				logger.debug("onError:", response);
				logger.debug("onError:", error);
			}
		};
		Request request = api.asyncNewsTitle(1, "", 1, 0, 100, listener);
		logger.debug("log:{}", request.getRequestLog());
		
		TimeUnit.MINUTES.sleep(10);
	}

}
