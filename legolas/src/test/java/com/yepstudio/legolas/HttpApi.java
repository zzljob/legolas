package com.yepstudio.legolas;

import org.json.JSONArray;

import com.yepstudio.legolas.annotation.Api;
import com.yepstudio.legolas.annotation.GET;
import com.yepstudio.legolas.annotation.Path;
import com.yepstudio.legolas.annotation.Query;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.OnResponseListener;

@Api("http://tools.fund123.cn/")
public interface HttpApi {

	@GET(value = "/huodong/{list}?1=2")
	public void getUserInfo(@Path(value = "list") String doublea,
			@Query("xx") int intaa, @Query("xxx") int intaaaa, AdddDTO dto,
			OnResponseListener<JSONArray> listener,
			OnErrorListener errorListener);

}
