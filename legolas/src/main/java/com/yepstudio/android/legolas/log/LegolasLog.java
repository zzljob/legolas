package com.yepstudio.android.legolas.log;

/**
 * 如果有slf4j的话就使用slf4j输出日志</br> 
 * 如果没有则使用Android日志LOG_LEVEL控制日志输出级别
 * 
 * @author zzljob@gmail.com
 * @createDate 2013年12月28日
 */
public class LegolasLog {

	public static int LOG_LEVEL = Log.TRACE;

	private Log logger;

	public static LegolasLog getClazz(Class<?> clazz) {
		return new LegolasLog(clazz);
	}

	public LegolasLog(Class<?> clazz) {
		try {
			Class<?> cf = clazz.getClassLoader().loadClass("org.slf4j.LoggerFactory");
			if (cf != null) {
				logger = new Sl4fLog(clazz);
				logger.log(LOG_LEVEL, Log.DEBUG, "have slf4j support, so use slf4j log.");
			}
		} catch (ClassNotFoundException e1) {
			logger = new AndroidLog(clazz);
		}
	}

	public void v(String msg) {
		logger.log(LOG_LEVEL, Log.TRACE, msg);
	}

	public void v(String msg, Throwable t) {
		logger.log(LOG_LEVEL, Log.TRACE, msg, t);
	}

	public void d(String msg) {
		logger.log(LOG_LEVEL, Log.DEBUG, msg);
	}

	public void d(String msg, Throwable t) {
		logger.log(LOG_LEVEL, Log.DEBUG, msg, t);
	}

	public void i(String msg) {
		logger.log(LOG_LEVEL, Log.INFO, msg);
	}

	public void i(String msg, Throwable t) {
		logger.log(LOG_LEVEL, Log.INFO, msg, t);
	}

	public void w(String msg) {
		logger.log(LOG_LEVEL, Log.WARNING, msg);
	}

	public void w(String msg, Throwable t) {
		logger.log(LOG_LEVEL, Log.WARNING, msg, t);
	}

	public void e(String msg) {
		logger.log(LOG_LEVEL, Log.ERROR, msg);
	}

	public void e(String msg, Throwable t) {
		logger.log(LOG_LEVEL, Log.ERROR, msg, t);
	}

}
