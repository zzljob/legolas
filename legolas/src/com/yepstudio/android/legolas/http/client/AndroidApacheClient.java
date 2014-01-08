package com.yepstudio.android.legolas.http.client;

import android.net.http.AndroidHttpClient;

public final class AndroidApacheClient extends ApacheClient {
	
	public AndroidApacheClient() {
		super(AndroidHttpClient.newInstance("Retrofit"));
	}
	
}