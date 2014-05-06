package com.yepstudio.legolas;

import org.json.JSONArray;

import com.yepstudio.legolas.annotation.Api;
import com.yepstudio.legolas.annotation.GET;
import com.yepstudio.legolas.annotation.Headers;
import com.yepstudio.legolas.annotation.Item;
import com.yepstudio.legolas.annotation.Path;
import com.yepstudio.legolas.annotation.Query;
import com.yepstudio.legolas.request.OnRequestListener;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.OnResponseListener;

@Api("/app")
@Headers({ 
	@Item(key = "api", value = "HttpApi"),
	@Item(key = "user-agent", value = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36") 
})
public interface HttpApi {

	@GET(value = "/{check}")
	@Headers({ 
		@Item(key = "request", value = "GET"),
		@Item(key = "user-agent", value = "getHuodong")
	})
	public Request doGet(@Path("check") String check, @Query("app") String app, @Query("appType") String appType);

	@GET(value = "/check")
	@Headers({ 
		@Item(key = "request", value = "GET"),
		@Item(key = "user-agent", value = "getHuodong")
	})
	public Request check(@Query("app") String app, @Query("appType") String appType, OnRequestListener requestListener, OnResponseListener<JSONArray> responseListener, OnErrorListener errorListener);

	@GET(value = "Redirect/Update.ashx")
	public void update(@Query("xx") int intaa, @Query("xxx") int intaaaa, AdddDTO dto, OnResponseListener<JSONArray> listener, OnErrorListener errorListener);

}
