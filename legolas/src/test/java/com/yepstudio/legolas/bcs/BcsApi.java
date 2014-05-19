package com.yepstudio.legolas.bcs;

import org.json.JSONObject;

import com.yepstudio.legolas.annotation.Api;
import com.yepstudio.legolas.annotation.GET;
import com.yepstudio.legolas.annotation.Interceptors;
import com.yepstudio.legolas.annotation.Path;
import com.yepstudio.legolas.annotation.Query;
import com.yepstudio.legolas.request.OnRequestListener;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.OnResponseListener;

@Api
@Interceptors(BcsSignInterceptor.class)
public interface BcsApi {

	@GET("/{bucket}")
	public Request getObjectList(@Path("bucket") String bucket, @Query("sign") BcsSign sign, OnRequestListener requestListener, OnResponseListener<JSONObject> responseListener, OnErrorListener errorListener);

	@GET("/{bucket}")
	public JSONObject getObjectList(@Path("bucket") String bucket, @Query("sign") BcsSign sign);

}
