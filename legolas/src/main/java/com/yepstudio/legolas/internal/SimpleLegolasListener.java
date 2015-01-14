package com.yepstudio.legolas.internal;

import com.yepstudio.legolas.LegolasException;
import com.yepstudio.legolas.listener.LegolasListener;
import com.yepstudio.legolas.request.Request;

public class SimpleLegolasListener<R, E> implements LegolasListener<R, E> {

	@Override
	public void onRequest(Request request) {

	}

	@Override
	public void onResponse(Request request, R response) {

	}

	@Override
	public void onError(Request request, LegolasException error, E response) {

	}

}
