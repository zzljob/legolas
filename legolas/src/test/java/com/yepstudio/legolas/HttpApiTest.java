package com.yepstudio.legolas;

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
import com.yepstudio.legolas.OAuthRequestInterceptor.OAuthType;
import com.yepstudio.legolas.OAuthRequestInterceptor.Signature;
import com.yepstudio.legolas.converter.GsonConverter;
import com.yepstudio.legolas.httpsender.HttpClientHttpSender;
import com.yepstudio.legolas.internal.Sl4fLog;
import com.yepstudio.legolas.listener.LegolasListener;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.webapi.HttpApi;
import com.yepstudio.legolas.webapi.OpenApi;
import com.yepstudio.legolas.webapi.HttpApi.NewsTitleEntity;
import com.yepstudio.legolas.webapi.OpenApi.AuthorizeFrom;

public class HttpApiTest {

	private static Logger logger = LoggerFactory.getLogger(HttpApiTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Endpoint endpoint = Endpoints.newFixedEndpoint("http://jrsj1.data.fund123.cn", "金融数据1");
		Endpoint openApiEndpoint = Endpoints.newFixedEndpoint("https://trade.fund123.cn", "OpenAPI");
		LegolasOptions defaultHttpRequestOptions = new LegolasOptions.Builder()
				.cacheConverterResult(false)
				.cacheInMemory(false)
				.cacheOnDisk(true)
				.cachePolicy(CachePolicy.SERVER_CACHE_CONTROL)
				.recoveryPolicy(RecoveryPolicy.RESPONSE_ERROR)
				.build(); 
		
		String ConsumerKey = "SM_SDK_SMB_ANDROID";
		String ConsumerSecret = "09F62474C5B24DA18CD4600E4DF0D1DB";
		OAuthRequestInterceptor appOAuth = new OAuthRequestInterceptor(ConsumerKey, ConsumerSecret);
		LegolasConfiguration config = new LegolasConfiguration.Builder()
				.defaultEndpoints(endpoint)
				.converterResultMaxExpired(10, TimeUnit.MINUTES)
				.defaultOptions(defaultHttpRequestOptions)
				.defaultConverter(new GsonConverter())
				.httpSender(new HttpClientHttpSender())
				.legolasLog(new Sl4fLog())
				.registerRequestInterceptors("AppOAuth", appOAuth)
				.requestApiEndpoints(OpenApi.class, openApiEndpoint)
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
	public void testSyncAuthorize() {
		OpenApi api = Legolas.getInstance().getApi(OpenApi.class);
		AuthorizeFrom from = new AuthorizeFrom();
		from.username = "15657121873";
		from.password = "123123";
		String response = api.authorize(from);
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
