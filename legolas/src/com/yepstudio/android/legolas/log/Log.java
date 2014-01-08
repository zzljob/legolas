package com.yepstudio.android.legolas.log;

public interface Log {
	
	public static final int TRACE = 1;
	public static final int DEBUG = 2;
	public static final int INFO = 3;
	public static final int WARNING = 4;
	public static final int ERROR = 5;
	public static final int FATAL = 6;

	public void log(int showLevel, int level, String msg);

	public void log(int showLevel, int level, String msg, Throwable t);

}
