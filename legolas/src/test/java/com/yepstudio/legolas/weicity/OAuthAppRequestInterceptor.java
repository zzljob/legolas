package com.yepstudio.legolas.weicity;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import android.annotation.SuppressLint;
import android.util.Base64;
import com.yepstudio.legolas.Converter;
import com.yepstudio.legolas.RequestInterceptor;
import com.yepstudio.legolas.RequestInterceptorFace;
import com.yepstudio.legolas.description.ParameterDescription.ParameterType;

/**
 * 应用级别的OAuth签名认证
 * @author zzljob@gmail.com
 * @createDate 2014年6月12日
 * @version 1.0, 2014年6月12日
 *
 */
public class OAuthAppRequestInterceptor implements RequestInterceptor {
	
	private static Logger logger = LoggerFactory.getLogger(OAuthAppRequestInterceptor.class);

	protected String charset = "UTF-8";
	protected static int NONCE_LENGTH = 32;
	
	protected String signatureMethod = "HMAC-SHA1";
	protected String consumerKey = "android_smb";
	protected String consumerSecret = "android_smb";
	
	protected String POSSIBLE_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	@SuppressLint("TrulyRandom") protected SecureRandom random = new SecureRandom();
	protected Set<Byte> safetyBytesSet;
	
	public OAuthAppRequestInterceptor() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		super();
		String safety = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_.~";
		safetyBytesSet = new TreeSet<Byte>();
		byte[] safetyByte = safety.getBytes(charset);
		if(safetyByte != null){
			for (byte b : safetyByte) {
				safetyBytesSet.add(b);
			}
		}
	}
	
	@Override
	public void interceptor(RequestInterceptorFace face) {
		String url = face.getRequestUrl(false);
		logger.debug("url:{}", url);
		String method = face.getRequestMethod();
		logger.debug("method:{}", method);
		TreeMap<String, String> parametersMap = getParametersMap(face);
		
		fillOAuthParameters(parametersMap, face);
		logger.trace("parametersMap:{}", parametersMap);
		
		logger.debug("url:{}", url);
		String baseString = jointRequest(url, method, parametersMap);
		logger.debug("baseString:{}", baseString);
		
		String signatureKey = getSignatureKey();
		logger.debug("signatureKey:{}", signatureKey);
		
		String sign = signature(baseString, signatureKey);
		logger.debug("sign:{}", sign);
		face.addQueryParam("oauth_signature", oauthURLEncode(sign));
	}
	
	protected void fillOAuthParameters(TreeMap<String, String> parameters, RequestInterceptorFace face) {
		String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
		logger.debug("timestamp:{}", timestamp);
		String nonce = getRandomString(NONCE_LENGTH);
		logger.debug("nonce:{}", nonce);
		
		//填上OAuth自身的参数
		parameters.put("oauth_version", "1.0");
		parameters.put("oauth_signature_method", signatureMethod);
		parameters.put("oauth_nonce", nonce);
		parameters.put("oauth_timestamp", timestamp);
		parameters.put("oauth_consumer_key", consumerKey);
		
		//添加参数到请求中
		face.addQueryParam("oauth_version", "1.0");
		face.addQueryParam("oauth_signature_method", signatureMethod);
		face.addQueryParam("oauth_nonce", nonce);
		face.addQueryParam("oauth_timestamp", timestamp);
		face.addQueryParam("oauth_consumer_key", consumerKey);
		
		fillTokenForOAuth(parameters, face);
	}
	
	
	/**
	 * 计算签名
	 * @param value
	 * @param key 
	 * @return
	 */
	protected String signature(String value, String key) {
		Mac mac = null;
		byte[] byteHMAC = null;
		try {
			mac = Mac.getInstance(signatureMethod);
			SecretKeySpec spec = new SecretKeySpec(key.getBytes(charset), signatureMethod);
			mac.init(spec);
			byteHMAC = mac.doFinal(value.getBytes(charset));
			return new String(Base64.encode(byteHMAC, Base64.NO_WRAP), charset);
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("", e);
		} catch (InvalidKeyException e) {
			logger.error("", e);
		}
		return "";
	}
	
	protected String jointRequest(String url, String method, TreeMap<String, String> parameters) {
		StringBuffer request = new StringBuffer();
		request.append(method);
		request.append("&");
		request.append(oauthURLEncode(url));
		if(parameters != null && !parameters.isEmpty()){
			request.append("&");
			
			Iterator<Entry<String, String>> iter = parameters.entrySet().iterator();
			StringBuilder params = new StringBuilder();
			while (iter.hasNext()) {
				Entry<String, String> entry = iter.next();
				params.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
			}
			params.deleteCharAt(params.length() - 1);
			request.append(oauthURLEncode(params.toString()));
		}
		return request.toString();
	}
	
	protected TreeMap<String, String> getParametersMap(RequestInterceptorFace face) {
		TreeMap<String, String> map = new TreeMap<String, String>();

		Converter converter = face.getConverter();
		Map<String, Object> queryMap = face.getQueryParams();
		for (String key : queryMap.keySet()) {
			map.put(key, converter.toParam(queryMap.get(key), ParameterType.QUERY));
		}
		Map<String, Object> formMap = face.getFieldParams();
		for (String key : formMap.keySet()) {
			map.put(key, converter.toParam(formMap.get(key), ParameterType.FIELD));
		}
		return map;
	}
	
	protected String getRandomString(int length) {
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(POSSIBLE_CHARS.charAt(random.nextInt(POSSIBLE_CHARS.length())));
		}
		logger.trace("getRandomString:{}", sb.toString());
		return sb.toString();
	}

	protected String oauthURLEncode(String string) {
		StringBuilder sb = new StringBuilder();
		try {
			byte[] utf8bytes = string.getBytes(charset);
			for (byte b : utf8bytes) {
				if (!safetyBytesSet.contains(b)) {
					sb.append(String.format("%%%02X", b));
				} else {
					sb.append((char)b);
				}
			}
		} catch (Exception e) {
			logger.error("oauthURLEncode", e);
		}
		logger.trace("oauthURLEncode, {} => {}", string, sb.toString());
		return sb.toString();
	}
	
	protected void fillTokenForOAuth(TreeMap<String, String> parameters, RequestInterceptorFace face) {
		//do nothing
	}
	
	protected String getSignatureKey() {
		return String.format("%s&%s", consumerSecret, "");
	}

}
