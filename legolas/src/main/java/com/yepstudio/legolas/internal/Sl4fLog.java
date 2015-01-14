package com.yepstudio.legolas.internal;

import org.slf4j.LoggerFactory;

import com.yepstudio.legolas.Legolas;
import com.yepstudio.legolas.LegolasLog;

/**
 * 使用Sl4fLog去记录Log
 * 
 * @author zzljob@gmail.com
 * @create 2013年12月28日
 * @version 3.0 2014年10月30日
 */
public class Sl4fLog implements LegolasLog {

	private org.slf4j.Logger logger = LoggerFactory.getLogger(Legolas.LOG_TAG);

	@Override
	public void v(String msg) {
		logger.trace(msg);
	}

	@Override
	public void v(String msg, Throwable t) {
		logger.trace(msg, t);
	}

	@Override
	public void d(String msg) {
		logger.debug(msg);
	}

	@Override
	public void d(String msg, Throwable t) {
		logger.debug(msg, t);
	}

	@Override
	public void i(String msg) {
		logger.info(msg);
	}

	@Override
	public void i(String msg, Throwable t) {
		logger.info(msg, t);
	}

	@Override
	public void w(String msg) {
		logger.warn(msg);
	}

	@Override
	public void w(String msg, Throwable t) {
		logger.warn(msg, t);
	}

	@Override
	public void e(String msg) {
		logger.error(msg);
	}

	@Override
	public void e(String msg, Throwable t) {
		logger.error(msg, t);
	}

}
