package com.yepstudio.legolas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpApiInterceptor implements RequestInterceptor {
	private static Logger logger = LoggerFactory.getLogger(HttpApiInterceptor.class);

	@Override
	public void interceptor(RequestInterceptorFace face) {
		logger.info(face.getRequestUrl(false));
		logger.info(face.getRequestUrl(true));
		logger.info(face.getQueryParams().toString());
	}

}
