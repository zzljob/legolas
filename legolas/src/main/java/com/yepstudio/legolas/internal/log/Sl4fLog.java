package com.yepstudio.legolas.internal.log;

import org.slf4j.LoggerFactory;

import com.yepstudio.legolas.LegolasLog;

/**
 * 使用Sl4fLog去记录Log
 * 
 * @author zzljob@gmail.com
 * @create 2013年12月28日
 * @version 2.0 2014年04月24日
 */
public class Sl4fLog extends LegolasLog {

	private org.slf4j.Logger logger = null;

	@Override
	public void log(int level, String msg, Throwable t) {
		switch (level) {
		case TRACE:
			if (t != null) {
				logger.trace(msg);
			}
			logger.trace(msg, t);
			break;
		case DEBUG:
			if (t != null) {
				logger.debug(msg);
			}
			logger.debug(msg, t);
			break;
		case INFO:
			if (t != null) {
				logger.info(msg);
			}
			logger.info(msg, t);
			break;
		case WARNING:
			if (t != null) {
				logger.warn(msg);
			}
			logger.warn(msg, t);
			break;
		case ERROR:
			if (t != null) {
				logger.error(msg);
			}
			logger.error(msg, t);
			break;
		case FATAL:
			if (t != null) {
				logger.error(msg);
			}
			logger.error(msg, t);
			break;
		default:
			if (t != null) {
				logger.debug(msg);
			}
			logger.debug(msg, t);
			break;
		}
	}

	@Override
	public void init(Class<?> clazz) {
		logger = LoggerFactory.getLogger(clazz);
	}

}
