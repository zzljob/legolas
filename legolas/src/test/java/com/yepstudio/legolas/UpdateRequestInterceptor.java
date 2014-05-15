package com.yepstudio.legolas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateRequestInterceptor implements RequestInterceptor {
	private static Logger logger = LoggerFactory.getLogger(UpdateRequestInterceptor.class);

	@Override
	public void interceptor(RequestInterceptorFace face) {
		logger.info("interceptor");
	}

}
