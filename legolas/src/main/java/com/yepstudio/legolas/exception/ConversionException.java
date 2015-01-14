package com.yepstudio.legolas.exception;

import java.lang.reflect.Type;

/**
 * 转换数据出错
 * 
 * @author zzljob@gmail.com
 * @create 2015年1月4日
 * @version 1.0，2015年1月4日
 *
 */
public class ConversionException extends ResponseException {

	private static final long serialVersionUID = -5439925901304555188L;

	private Type conversionType;

	public ConversionException() {
		super();
	}

	public ConversionException(String message) {
		super(message);
	}

	public ConversionException(Throwable cause) {
		super(cause);
	}

	public ConversionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConversionException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public Type getConversionType() {
		return conversionType;
	}

	public void setConversionType(Type conversionType) {
		this.conversionType = conversionType;
	}

}
