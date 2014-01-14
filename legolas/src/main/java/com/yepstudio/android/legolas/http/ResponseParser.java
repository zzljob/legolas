package com.yepstudio.android.legolas.http;

import java.lang.reflect.Type;

import com.yepstudio.android.legolas.error.LegolasError;

public interface ResponseParser {
	public Object doParse(Request request, Response response, Type type) throws LegolasError;
}
