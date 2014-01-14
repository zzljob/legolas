package com.yepstudio.android.legolas.conversion;

import java.lang.reflect.Type;

import com.yepstudio.android.legolas.http.mime.RequestBody;
import com.yepstudio.android.legolas.http.mime.ResponseBody;

public interface Converter {
	/**
	   * Convert an HTTP response body to a concrete object of the specified type.
	   *
	   * @param body HTTP response body.
	   * @param type Target object type.
	   * @return Instance of {@code type} which will be cast by the caller.
	   * @throws ConversionException if conversion was unable to complete. This will trigger a call to
	   * {@link retrofit.Callback#failure(retrofit.RetrofitError)} or throw a
	   * {@link retrofit.RetrofitError}. The exception message should report all necessary information
	   * about its cause as the response body will be set to {@code null}.
	   */
	  Object fromBody(ResponseBody body, Type clazz) throws ConversionException;

	  /**
	   * Convert and object to an appropriate representation for HTTP transport.
	   *
	   * @param object Object instance to convert.
	   * @return Representation of the specified object as bytes.
	   */
	  RequestBody toBody(Object object);
}
