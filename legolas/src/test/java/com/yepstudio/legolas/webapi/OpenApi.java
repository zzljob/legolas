package com.yepstudio.legolas.webapi;

import com.yepstudio.legolas.annotation.Api;
import com.yepstudio.legolas.annotation.Description;
import com.yepstudio.legolas.annotation.GET;
import com.yepstudio.legolas.annotation.Interceptors;
import com.yepstudio.legolas.annotation.MuitiParameters;
import com.yepstudio.legolas.annotation.Query;

@Description("数米OpenAPI")
@Api("/openapi/get.json")
public interface OpenApi {

	@Description("登陆")
	@GET(value = "/trade_account.authorize")
	@Interceptors(alias = "AppOAuth")
	public String authorize(AuthorizeFrom from);

	@MuitiParameters
	public static class AuthorizeFrom {
		@Description("版本")
		@Query("username")
		public String username;
		@Query("password")
		public String password;
		@Query("loginmode")
		public String mode = "0";
	}
}
