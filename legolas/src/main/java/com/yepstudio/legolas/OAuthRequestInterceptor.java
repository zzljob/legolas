package com.yepstudio.legolas;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.yepstudio.legolas.mime.FormUrlEncodedRequestBody;
import com.yepstudio.legolas.mime.RequestBody;

/**
 * 
 * 
 * @author zzljob@gmail.com
 * @create 2015年1月20日
 * @version 1.0，2015年1月20日
 *
 */
public class OAuthRequestInterceptor implements RequestInterceptor {
	
	protected static final int DEFAULT_NONCE_LENGTH = 32;
	protected static final String SAFETY_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_.~";
	protected static final String POSSIBLE_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	public static enum Signature {
		HMAC_SHA1("HMAC-SHA1"),
		HMAC_SHA256("HMAC-SHA256"),
		PLAINTEXT("PLAINTEXT");

		private String signature;
		private Signature(String signature) {
			this.signature = signature;
		}
		public String toString() {
			return signature;
		}
	};

	public static enum OAuthType {
		QUERTY_STRING, HTTP_HEADERS
	}

	public static enum OAuthLevel {
		APP, APP_USER
	}
	
	private final SecureRandom random = new SecureRandom();
	private final Signature signature;
	private final OAuthType oauthType;
	private final int nonceLength;
	private final String consumerKey;
	private final String consumerSecret;
	
	private final OAuthLevel oauthLevel;
	private String token;
	private String tokenSecret;
	
	public OAuthRequestInterceptor(String consumerKey, String consumerSecret) {
		this(Signature.HMAC_SHA1, consumerKey, consumerSecret, OAuthType.QUERTY_STRING);
	}
	
	public OAuthRequestInterceptor(String consumerKey, String consumerSecret, String token, String tokenSecret) {
		this(Signature.HMAC_SHA1, consumerKey, consumerSecret, token, tokenSecret, OAuthType.QUERTY_STRING);
	}

	public OAuthRequestInterceptor(Signature signature, String consumerKey, String consumerSecret, String token, String tokenSecret, OAuthType auth) {
		this(signature, DEFAULT_NONCE_LENGTH, consumerKey, consumerSecret, token, tokenSecret, auth, OAuthLevel.APP_USER);
	}

	public OAuthRequestInterceptor(Signature signature, String consumerKey, String consumerSecret, OAuthType auth) {
		this(signature, DEFAULT_NONCE_LENGTH, consumerKey, consumerSecret, null, null, auth, OAuthLevel.APP);
	}
	
	public OAuthRequestInterceptor(Signature signature, int nonceLength, String consumerKey, String consumerSecret, String token, String tokenSecret, OAuthType auth, OAuthLevel level) {
		super();
		this.signature = signature;
		this.oauthType = auth;
		this.nonceLength = nonceLength;
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.token = token;
		this.tokenSecret = tokenSecret;
		this.oauthLevel = level;
	}
	
	public void setTokenAndTokenSecret(String token, String tokenSecret) {
		this.token = token;
		this.tokenSecret = tokenSecret;
	}
	
	protected String getRandomString() {
		StringBuilder sb = new StringBuilder(nonceLength);
		for (int i = 0; i < nonceLength; i++) {
			sb.append(POSSIBLE_CHARS.charAt(random.nextInt(POSSIBLE_CHARS.length())));
		}
		return sb.toString();
	}
	
	protected long getTimestamp() {
		return new Date().getTime() / 1000;
	}
	
	protected Map<String, String> getOAuthParams() {
		String timestamp = String.valueOf(getTimestamp());
		String nonce = getRandomString();
		
		Map<String, String> oauthParams = new HashMap<String, String>();
		// 填上OAuth自身的参数
		oauthParams.put("oauth_version", "1.0");
		oauthParams.put("oauth_signature_method", signature.toString());
		oauthParams.put("oauth_nonce", nonce);
		oauthParams.put("oauth_timestamp", timestamp);
		oauthParams.put("oauth_consumer_key", consumerKey);
		if (OAuthLevel.APP_USER == oauthLevel) {
			oauthParams.put("oauth_token", this.token);
		}
		return oauthParams;
	}
	
	protected String oauthURLEncode(String string, String charset) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		Set<Byte> safetyBytesSet = new TreeSet<Byte>();
		byte[] safetyByte = SAFETY_CHARS.getBytes(charset);
		if (safetyByte != null) {
			for (byte b : safetyByte) {
				safetyBytesSet.add(b);
			}
		}
		
		byte[] utf8bytes = string.getBytes(charset);
		for (byte b : utf8bytes) {
			if (!safetyBytesSet.contains(b)) {
				sb.append(String.format("%%%02X", b));
			} else {
				sb.append((char) b);
			}
		}
		return sb.toString();
	}
	
	protected String jointBaseString(String method, String url, Map<String, String> parameters, String charset) throws UnsupportedEncodingException {
		StringBuffer request = new StringBuffer();
		request.append(method.toUpperCase());
		request.append("&");
		request.append(oauthURLEncode(url.toLowerCase(), charset));
		request.append("&");
		String paramsString = jointParameters(parameters, charset);
		request.append(oauthURLEncode(paramsString, charset));
		return request.toString();
	}
	
	protected String jointParameters(Map<String, String> parameters, String charset) throws UnsupportedEncodingException {
		StringBuilder params = new StringBuilder();
		if (parameters == null || parameters.isEmpty()) {
			return "";
		}
		Iterator<Entry<String, String>> iter = parameters.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			params.append(oauthURLEncode(entry.getKey(), charset));
			params.append("=");
			params.append(oauthURLEncode(entry.getValue(), charset));
			params.append("&");
		}
		params.deleteCharAt(params.length() - 1);
		return params.toString();
	}

	protected String getSignatureKey() {
		if (OAuthLevel.APP_USER == oauthLevel) {
			return String.format("%s&%s", consumerSecret, tokenSecret);
		} else {
			return String.format("%s&%s", consumerSecret, "");
		}
	}
	
	/**
	 * 计算签名
	 * 
	 * @param value
	 * @param key
	 * @return
	 * @throws NoSuchAlgorithmException 
	 * @throws UnsupportedEncodingException 
	 * @throws InvalidKeyException 
	 */
	protected String signature(String value, String key, String charset) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
		Mac mac = null;
		byte[] byteHMAC = null;
		
		String realSignature = signature.toString().replace("-", "");
		mac = Mac.getInstance(realSignature);
		
		SecretKeySpec spec = new SecretKeySpec(key.getBytes(charset), signature.toString());
		mac.init(spec);
		byteHMAC = mac.doFinal(value.getBytes(charset));
		return base64(byteHMAC);
	}
	
	protected String base64(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}
	
	@Override
	public void interceptor(RequestInterceptorFace face) {
		//添加OAuth的参数
		TreeMap<String, String> parametersMap = new TreeMap<String, String>();
		
		Map<String, String> oauths = getOAuthParams();
		
		RequestType type = face.getRequestType();
		if (RequestType.MULTIPART == type) {
			parametersMap.putAll(oauths);
		} else {
			parametersMap.putAll(face.getQuerys());
			parametersMap.putAll(oauths);
			RequestBody body = face.getBody();
			if (body != null) {
				if (body instanceof FormUrlEncodedRequestBody) {
					FormUrlEncodedRequestBody formBody = (FormUrlEncodedRequestBody) body;
					String[] names = formBody.getFieldName();
					if (names != null && names.length > 0) {
						for (String name : names) {
							parametersMap.put(name, formBody.getField(name));
						}
					}
				}
			}
		}
		
		String url = face.getRequestUrl();
		String method = face.getRequestMethod();
		String charset = face.getEncode();
		
		String signature = null;
		try {
			String baseString = jointBaseString(method, url, parametersMap, charset);
			String signatureKey = getSignatureKey();
			String sign = signature(baseString, signatureKey, charset);
			signature = oauthURLEncode(sign, charset);
			oauths.put("oauth_signature", signature);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UnsupportedEncodingException", e); 
		} catch (InvalidKeyException e) {
			throw new IllegalStateException("InvalidKeyException", e); 
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("NoSuchAlgorithmException", e); 
		}
		
		if (OAuthType.QUERTY_STRING == oauthType) {
			face.getQuerys().putAll(oauths);
		} else {
			StringBuilder builder = new StringBuilder("OAuth");
			for (String key : oauths.keySet()) {
				builder.append(" ");
				builder.append(key);
				builder.append("=");
				builder.append("\"");
				builder.append(oauths.get(key));
				builder.append("\"");
				builder.append(",");
			}
			builder.deleteCharAt(builder.length() - 1);
			face.getHeaders().put("Authorization", builder.toString());
		}
	}

	public SecureRandom getRandom() {
		return random;
	}

	public Signature getSignature() {
		return signature;
	}

	public OAuthType getOAuthType() {
		return oauthType;
	}

	public int getNonceLength() {
		return nonceLength;
	}

	public OAuthLevel getOAuthLevel() {
		return oauthLevel;
	}

}
