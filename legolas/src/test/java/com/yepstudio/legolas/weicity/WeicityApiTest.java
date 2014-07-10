package com.yepstudio.legolas.weicity;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.Endpoint;
import com.yepstudio.legolas.Endpoints;
import com.yepstudio.legolas.Legolas;
import com.yepstudio.legolas.converter.JSONConverter;
import com.yepstudio.legolas.weicity.form.SignupForm;

public class WeicityApiTest {
	
	private static WeicityApi api;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Endpoint  defaultEndpoint = Endpoints.newFixedEndpoint("http://bcs.duapp.com");
		Converter converter = new JSONConverter();
		Legolas legolas = new Legolas.Build().setDefaultEndpoint(defaultEndpoint).setDefaultConverter(converter).create();
		api = legolas.newInstance(WeicityApi.class);
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
	public void test() {
		SignupForm form = new SignupForm();
		api.signup(form, null, null, null);
	}

}
