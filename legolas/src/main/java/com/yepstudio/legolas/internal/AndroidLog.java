package com.yepstudio.legolas.internal;

import com.yepstudio.legolas.Legolas;
import com.yepstudio.legolas.LegolasLog;

/**
 * 使用Android.Log去记录Log
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月6日
 * @version 3.0，2014年10月30日
 */
public class AndroidLog implements LegolasLog {

	private static final String LOG_TAG = Legolas.LOG_TAG;

	@Override
	public void v(String msg) {
		android.util.Log.v(LOG_TAG, msg);
	}

	@Override
	public void v(String msg, Throwable t) {
		android.util.Log.v(LOG_TAG, msg, t);
	}

	@Override
	public void d(String msg) {
		android.util.Log.d(LOG_TAG, msg);
	}

	@Override
	public void d(String msg, Throwable t) {
		android.util.Log.d(LOG_TAG, msg, t);
	}

	@Override
	public void i(String msg) {
		android.util.Log.i(LOG_TAG, msg);
	}

	@Override
	public void i(String msg, Throwable t) {
		android.util.Log.i(LOG_TAG, msg, t);
	}

	@Override
	public void w(String msg) {
		android.util.Log.w(LOG_TAG, msg);
	}

	@Override
	public void w(String msg, Throwable t) {
		android.util.Log.w(LOG_TAG, msg, t);
	}

	@Override
	public void e(String msg) {
		android.util.Log.e(LOG_TAG, msg);
	}

	@Override
	public void e(String msg, Throwable t) {
		android.util.Log.e(LOG_TAG, msg, t);
	}

}
