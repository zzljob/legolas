package com.yepstudio.legolas.description;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.yepstudio.legolas.HttpApi;

public class ApiDescriptionTest {

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void test() {
		ApiDescription api = new ApiDescription(HttpApi.class);
	}

}
