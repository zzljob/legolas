package com.yepstudio.legolas;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegolasTest {

	private static Logger logger = LoggerFactory.getLogger(LegolasTest.class);
	private static Legolas legolas;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		logger.info("setUpBeforeClass");
		legolas = new Legolas.Build().create();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		logger.info("tearDownAfterClass");
	}
	
	@Before
	public void setUp() throws Exception {
		logger.info("setUp");
	}

	@After
	public void tearDown() throws Exception {
		logger.info("tearDown");
	}

	@Test
	public void test1() {
		logger.info("test1");
		HttpApi api = legolas.newInstance(HttpApi.class);
		api.getUserInfo("list", 1, 2, null, null, null);
	}

	@Test
	public void test2() {
		logger.info("test2");
	}

}
