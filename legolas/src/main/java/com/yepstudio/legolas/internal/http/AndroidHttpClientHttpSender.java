package com.yepstudio.legolas.internal.http;

import android.net.http.AndroidHttpClient;

public final class AndroidHttpClientHttpSender extends HttpClientHttpSender {
	
	public AndroidHttpClientHttpSender() {
		super(AndroidHttpClient.newInstance("Legolas"));
	}
	
}