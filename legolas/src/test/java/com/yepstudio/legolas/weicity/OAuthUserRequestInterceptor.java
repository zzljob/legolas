package com.yepstudio.legolas.weicity;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yepstudio.legolas.RequestInterceptorFace;

/**
 * 用户级别OAuth签名认证
 * @author zzljob@gmail.com
 * @createDate 2014年6月12日
 * @version 1.0, 2014年6月12日
 *
 */
public class OAuthUserRequestInterceptor extends OAuthAppRequestInterceptor {
	
	private static Logger logger = LoggerFactory.getLogger(OAuthUserRequestInterceptor.class);
	
	protected static String token = "";
	protected static String tokenSecret = "";
	
	public OAuthUserRequestInterceptor() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		super();
	}
	
	public static void setUserLoginToken(String userToken, String userTokenSecret) {
		token = userToken;
		tokenSecret = userTokenSecret;
		logger.info("token:{}, tokenSecret:{}", token, tokenSecret);
	}

	protected void fillTokenForOAuth(TreeMap<String, String> parameters, RequestInterceptorFace face) {
		parameters.put("oauth_token", token);
		face.addQueryParam("oauth_token", token);
	}
	
	protected String getSignatureKey() {
		return String.format("%s&%s", consumerSecret, tokenSecret);
	}
	
}
