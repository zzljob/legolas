package com.yepstudio.legolas.weicity;

import com.yepstudio.legolas.annotation.Api;
import com.yepstudio.legolas.annotation.GET;
import com.yepstudio.legolas.annotation.Interceptors;
import com.yepstudio.legolas.annotation.Querys;
import com.yepstudio.legolas.request.OnRequestListener;
import com.yepstudio.legolas.request.Request;
import com.yepstudio.legolas.response.OnErrorListener;
import com.yepstudio.legolas.response.OnResponseListener;
import com.yepstudio.legolas.weicity.form.SignupForm;

@Api("/v1")
@Interceptors(OAuthAppRequestInterceptor.class)
public interface WeicityApi {
	
	@GET("/register")
	public Request signup(@Querys SignupForm form, OnRequestListener requestListener, OnResponseListener<Long> responseListener, OnErrorListener errorListener);
	
}
