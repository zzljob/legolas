package com.yepstudio.legolas.webapi;

import com.yepstudio.legolas.annotation.Api;
import com.yepstudio.legolas.annotation.GET;
import com.yepstudio.legolas.annotation.Path;
import com.yepstudio.legolas.annotation.Query;
import com.yepstudio.legolas.request.Request;

@Api("/app")
public interface ErrorApi1 {

	@GET(value = "/{checkx}?xxx=qw")
	public Request doGet(@Path("check") String check, @Query("app") String app, @Query("appType") String appType);


}
