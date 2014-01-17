package com.yepstudio.android.legolas.log;

/**
 * 使用Sl4fLog去记录Log
 * @author zzljob@gmail.com
 * @createDate 2013年12月28日
 */
public class Sl4fLog implements Log {

	private org.slf4j.Logger logger = null;

	public Sl4fLog(Class<?> clazz) {
		logger = org.slf4j.LoggerFactory.getLogger(clazz);
	}

	@Override
	public void log(int showLevel, int level, String msg) {
		switch (level) {
		case TRACE:
			logger.trace(msg);
			break;
		case DEBUG:
			logger.debug(msg);
			break;
		case INFO:
			logger.info(msg);
			break;
		case WARNING:
			logger.warn(msg);
			break;
		case ERROR:
			logger.error(msg);
			break;
		case FATAL:
			logger.error(msg);
			break;
		default:
			logger.debug(msg);
			break;
		}
	}

	@Override
	public void log(int showLevel, int level, String msg, Throwable t) {
		switch (level) {
		case TRACE:
			logger.trace(msg, t);
			break;
		case DEBUG:
			logger.debug(msg, t);
			break;
		case INFO:
			logger.info(msg, t);
			break;
		case WARNING:
			logger.warn(msg, t);
			break;
		case ERROR:
			logger.error(msg, t);
			break;
		case FATAL:
			logger.error(msg, t);
			break;
		default:
			logger.debug(msg, t);
			break;
		}
	}
}
