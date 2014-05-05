package com.yepstudio.legolas;

import java.lang.reflect.Type;

import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.Response;

public interface ResponseParser {
	
	public Object doParse(Converter converter, Request request, Response response, Type type) throws LegolasError;
}
