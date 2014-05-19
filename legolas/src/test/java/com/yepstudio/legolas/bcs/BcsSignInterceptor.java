package com.yepstudio.legolas.bcs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yepstudio.legolas.RequestInterceptor;
import com.yepstudio.legolas.RequestInterceptorFace;

public class BcsSignInterceptor implements RequestInterceptor {
	
	private static Logger logger = LoggerFactory.getLogger(BcsSignInterceptor.class);
	
	@Override
	public void interceptor(RequestInterceptorFace face) {
		logger.info("Method : {}", face.getRequestMethod());
		logger.info("RequestUrl: {}", face.getRequestUrl(false));
		logger.info("RequestUrl: {}", face.getRequestUrl(true));
		logger.info("PathParams: {}", face.getPathParams());
		logger.info("QueryParams: {}", face.getQueryParams());
	}

}
