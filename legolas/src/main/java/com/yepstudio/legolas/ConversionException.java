package com.yepstudio.legolas;

/** Indicate that conversion was unable to complete successfully. */
@SuppressWarnings("serial")
public class ConversionException extends Exception {
	public ConversionException(String message) {
		super(message);
	}

	public ConversionException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public ConversionException(Throwable throwable) {
		super(throwable);
	}
}
