package com.yepstudio.android.legolas.demo;

import org.json.JSONArray;

import com.yepstudio.android.legolas.annotation.Api;
import com.yepstudio.android.legolas.annotation.http.GET;
import com.yepstudio.android.legolas.annotation.parameter.Path;
import com.yepstudio.android.legolas.annotation.parameter.Query;
import com.yepstudio.android.legolas.http.Response.OnErrorListener;
import com.yepstudio.android.legolas.http.Response.OnResponseListener;

@Api("http://tools.fund123.cn/")
public interface HttpApi {

	@GET(value = "/huodong/{list}?1=2")
	public Void getUserInfo(@Path(value = "list", format = "") String doublea,
			@Query("xx") int intaa, 
			@Query("xxx") int intaaaa, 
			AdddDTO dto,
			OnResponseListener<JSONArray> listener,
			OnErrorListener errorListener);

}
