package com.yepstudio.legolas;

/**
 * 
 * 因为Exception不支持泛型，所以需要通过LegolasOptions参数来告知错误返回时要转换的类型
 * 
 * @author zzljob@gmail.com
 * @create 2014年12月31日
 * @version 1.0，2014年12月31日
 *
 * @param <T>
 */
public final class LegolasSyncOptions<T> extends LegolasOptions {

	protected LegolasSyncOptions(Builder builder) {
		super(builder);
	}

	public static class Builder extends LegolasOptions.Builder {
		
	}
}
