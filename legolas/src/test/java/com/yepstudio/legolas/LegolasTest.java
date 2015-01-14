package com.yepstudio.legolas;

import org.junit.Test;

import com.yepstudio.legolas.exception.LegolasConfigureError;
import com.yepstudio.legolas.webapi.ErrorApi1;
import com.yepstudio.legolas.webapi.HttpApi;

public class LegolasTest {

	@Test(expected = IllegalStateException.class)
	public void testLegolasInit() {
		Legolas.getInstance().getApi(HttpApi.class);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testBuildLegolasConfig() {
		new LegolasConfiguration.Builder().build();
	}

	@Test(expected = LegolasConfigureError.class)
	public void testGetApi1() {
		Endpoint endpoint = Endpoints.newFixedEndpoint("http://www.baidu.com", "百度");
		LegolasConfiguration config = new LegolasConfiguration.Builder().defaultEndpoints(endpoint).build();
		Legolas.getInstance().init(config);
		
		Legolas.getInstance().getApi(ErrorApi1.class);
	}

	@Test(expected = LegolasConfigureError.class)
	public void testGetApi2() {
		Endpoint endpoint = Endpoints.newFixedEndpoint("http://www.baidu.com", "百度");
		LegolasConfiguration config = new LegolasConfiguration.Builder().defaultEndpoints(endpoint).build();
		Legolas.getInstance().init(config);
		
		Legolas.getInstance().getApi(ErrorApi1.class);
	}
	
	

}
