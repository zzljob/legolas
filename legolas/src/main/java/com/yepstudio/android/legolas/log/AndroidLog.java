package com.yepstudio.android.legolas.log;

/**
 * 使用Android.Log去记录Log
 * @author zzljob@gmail.com
 * @createDate 2014年1月6日
 */
public class AndroidLog implements Log {
	private String tag;

	public AndroidLog(Class<?> clazz) {
		tag = clazz.getSimpleName();
	}

	@Override
	public void log(int showLevel, int level, String msg) {
		if (showLevel > level) {
			return;
		}
		switch (level) {
		case TRACE:
			android.util.Log.v(tag, msg);
			break;
		case DEBUG:
			android.util.Log.d(tag, msg);
			break;
		case INFO:
			android.util.Log.i(tag, msg);
			break;
		case WARNING:
			android.util.Log.w(tag, msg);
			break;
		case ERROR:
			android.util.Log.e(tag, msg);
			break;
		case FATAL:
			android.util.Log.e(tag, msg);
			break;
		default:
			android.util.Log.d(tag, msg);
			break;
		}
	}

	@Override
	public void log(int showLevel, int level, String msg, Throwable t) {
		if (showLevel < level) {
			return;
		}
		switch (level) {
		case TRACE:
			android.util.Log.v(tag, msg, t);
			break;
		case DEBUG:
			android.util.Log.d(tag, msg, t);
			break;
		case INFO:
			android.util.Log.i(tag, msg, t);
			break;
		case WARNING:
			android.util.Log.w(tag, msg, t);
			break;
		case ERROR:
			android.util.Log.e(tag, msg, t);
			break;
		case FATAL:
			android.util.Log.e(tag, msg, t);
			break;
		default:
			android.util.Log.d(tag, msg, t);
			break;
		}
	}

}
