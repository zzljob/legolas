package com.yepstudio.android.legolas.handler;

/**
 * 参数解析接口
 * @author zzljob@gmail.com
 * @createDate 2013年12月27日
 * @param <T>
 */
public interface ParamFormat<T> {

	public String format(T obj);

}
