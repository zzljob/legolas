package com.yepstudio.legolas.response;

import com.yepstudio.legolas.LegolasException;

public interface OnErrorListener {

	public void onError(LegolasException error);

}