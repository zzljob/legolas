package com.yepstudio.legolas;

/**
 * 日志输出接口
 * 
 * @author zzljob@gmail.com
 * @create 2013年12月28日
 * @version 3.0 2014年11月5日
 */
public interface LegolasLog {

	public void v(String msg);

	public void v(String msg, Throwable t);

	public void d(String msg);

	public void d(String msg, Throwable t);

	public void i(String msg);

	public void i(String msg, Throwable t);

	public void w(String msg);

	public void w(String msg, Throwable t);

	public void e(String msg);

	public void e(String msg, Throwable t);

}
