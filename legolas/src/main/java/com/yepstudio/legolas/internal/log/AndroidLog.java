package com.yepstudio.legolas.internal.log;

import com.yepstudio.legolas.LegolasLog;


/**
 * 使用Android.Log去记录Log
 * 
 * @author zzljob@gmail.com
 * @create 2014年1月6日
 * @version 2.0，2014年4月24日
 */
public class AndroidLog extends LegolasLog {
	private String tag;
	private static int SHOWLEVEL = findShowLevel();

	private static int findShowLevel() {
		return ERROR;
	}

	@Override
	public void log(int level, String msg, Throwable t) {
		if (SHOWLEVEL < level) {
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

	@Override
	public void init(Class<?> clazz) {
		tag = clazz.getName();
	}

}
