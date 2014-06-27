package com.yepstudio.legolas;

import java.util.Map;

import com.yepstudio.legolas.annotation.Api;
import com.yepstudio.legolas.annotation.GET;
import com.yepstudio.legolas.annotation.Headers;
import com.yepstudio.legolas.annotation.Interceptors;
import com.yepstudio.legolas.annotation.Item;
import com.yepstudio.legolas.annotation.Path;
import com.yepstudio.legolas.annotation.Query;
import com.yepstudio.legolas.annotation.Querys;
import com.yepstudio.legolas.request.OnRequestListener;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.OnResponseListener;

@Api
@Headers({ 
	@Item(key = "api", value = "HttpApi"),
})
@Interceptors(HttpApiInterceptor.class)
public interface ToolHttpApi {

	@GET(value = "/Redirect/Update.ashx")
	@Interceptors(value = UpdateRequestInterceptor.class, expansion = true)
	public Request update(@Query("xx") int intaa, @Query("xxx") int intaaaa, @Querys Map<String, Object> map, @Querys AdddDTO dto, OnResponseListener<CheckResult> listener, OnErrorListener errorListener);

}
