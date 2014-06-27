package com.yepstudio.legolas;


/**
 * 如果有slf4j的话就使用slf4j输出日志</br> 如果没有则使用Android日志LOG_LEVEL控制日志输出级别
 * 
 * @author zzljob@gmail.com
 * @create 2013年12月28日
 * @version 2.0 2014年04月24日
 */
public abstract class LegolasLog {

	public static LegolasLog getClazz(Class<?> clazz) {
		Class<? extends LegolasLog> logClass = Platform.get().defaultLog();
		try {
			LegolasLog log = logClass.newInstance();
			log.init(clazz);
			return log;
		} catch (Throwable e) {
			throw new IllegalArgumentException("log class is error, can not be newInstance ");
		}
	}

	public static final int TRACE = 1;
	public static final int DEBUG = 2;
	public static final int INFO = 3;
	public static final int WARNING = 4;
	public static final int ERROR = 5;
	public static final int FATAL = 6;

	public abstract void init(Class<?> clazz);

	public void log(int level, String msg) {
		log(level, msg, null);
	}

	public abstract void log(int level, String msg, Throwable t);

	public void v(String msg) {
		log(TRACE, msg);
	}

	public void v(String msg, Throwable t) {
		log(TRACE, msg, t);
	}

	public void d(String msg) {
		log(DEBUG, msg);
	}

	public void d(String msg, Throwable t) {
		log(DEBUG, msg, t);
	}

	public void i(String msg) {
		log(INFO, msg);
	}

	public void i(String msg, Throwable t) {
		log(INFO, msg, t);
	}

	public void w(String msg) {
		log(WARNING, msg);
	}

	public void w(String msg, Throwable t) {
		log(WARNING, msg, t);
	}

	public void e(String msg) {
		log(ERROR, msg);
	}

	public void e(String msg, Throwable t) {
		log(ERROR, msg, t);
	}

}
