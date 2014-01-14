package com.yepstudio.android.legolas.error;

import java.io.IOException;
import java.lang.reflect.Type;

import com.yepstudio.android.legolas.conversion.ConversionException;
import com.yepstudio.android.legolas.conversion.Converter;
import com.yepstudio.android.legolas.http.Response;
import com.yepstudio.android.legolas.http.mime.ResponseBody;

@SuppressWarnings("serial")
public class LegolasError extends RuntimeException {
	public static LegolasError networkError(String url, IOException exception) {
		return new LegolasError(url, null, null, null, true, exception);
	}

	public static LegolasError conversionError(String url, Response response, Converter converter, Type successType, ConversionException exception) {
		return new LegolasError(url, response, converter, successType, false, exception);
	}

	public static LegolasError httpError(String url, Response response, Converter converter, Type successType) {
		return new LegolasError(url, response, converter, successType, false, null);
	}

	public static LegolasError unexpectedError(String url, Throwable exception) {
		return new LegolasError(url, null, null, null, false, exception);
	}

	private final String url;
	private final Response response;
	private final Converter converter;
	private final Type successType;
	private final boolean networkError;

	LegolasError(String url, Response response, Converter converter, Type successType, boolean networkError, Throwable exception) {
		super(exception);
		this.url = url;
		this.response = response;
		this.converter = converter;
		this.successType = successType;
		this.networkError = networkError;
	}

	/** The request URL which produced the error. */
	public String getUrl() {
		return url;
	}

	/** Response object containing status code, headers, body, etc. */
	public Response getResponse() {
		return response;
	}

	/** Whether or not this error was the result of a network error. */
	public boolean isNetworkError() {
		return networkError;
	}

	/**
	 * HTTP response body converted to the type declared by either the interface
	 * method return type or the generic type of the supplied {@link Callback}
	 * parameter.
	 */
	public Object getBody() {
		ResponseBody body = response.getBody();
		if (body == null) {
			return null;
		}
		try {
			return converter.fromBody(body, successType);
		} catch (ConversionException e) {
			throw new RuntimeException(e);
		}
	}

	/** HTTP response body converted to specified {@code type}. */
	public Object getBodyAs(Type type) {
		ResponseBody body = response.getBody();
		if (body == null) {
			return null;
		}
		try {
			return converter.fromBody(body, type);
		} catch (ConversionException e) {
			throw new RuntimeException(e);
		}
	}
}