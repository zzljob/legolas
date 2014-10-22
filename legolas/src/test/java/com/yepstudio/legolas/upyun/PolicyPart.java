package com.yepstudio.legolas.upyun;

import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 表单文件上传到 UPYUN 时，需要告知该文件需要怎么处理，以及最终的保存路径等
 * 
 * @author zzljob@gmail.com
 * @create 2014年10月22日
 * @version 1.0，2014年10月22日
 *
 */
public class PolicyPart {

	private static Logger logger = LoggerFactory.getLogger(PolicyPart.class);
	private static final long TIMEOUT = 3600 * 24 * 30;

	/*** 空间名，必选 **/
	private final String bucket;
	/*** 最终的保存路径。必选，如: /dir/filename，可以使用占位符 **/
	private final String saveKey;
	/*** 请求的过期时间。必选，当前上传请求授权的过期时间，单位为“秒” **/
	private final long expiration;
	/** 文件校验码。可选 **/
	private String contentMd5;
	/** 文件类型。可选，上传文件时，允许用户强行指定该文件的文件类型；默认情况下，系统会通过文件扩展名自动识别 **/
	private String contentType;
	/** 文件类型限制。可选，根据文件扩展名限制上传文件，如 jpg,gif,png含义：仅允许上传扩展名为 jpg,gif,png 三种类型的文件 **/
	private String allowFileType;
	/** 异步回调 url。可选，表单上传完成后，云存储服务端主动把上传结果 POST 到该 URL需要确保公网可以正常访问该 URL **/
	private String notifyUrl;
	/** 同步跳转 url。可选，表单上传完成后，使用 http 302 的方式跳转到该 URL **/
	private String returnUrl;

	public static long defaultExpiration() {
		return (new Date().getTime() / 1000L + TIMEOUT);
	}

	public static String randomSaveKey(String prefix, String suffix) {
		Date time = new Date();
		String name = UUID.randomUUID().toString();
		String format = "/nightclub/upload/%1$s/%2$tY/%2$tm/%2$td/%3$s.%4$s";
		return String.format(format, prefix, time, name, suffix);
	}

	public PolicyPart(String bucket) {
		this(bucket, defaultExpiration());
	}

	public PolicyPart(String bucket, long expiration) {
		this(bucket, randomSaveKey("images", "jpg"), expiration);
	}

	public PolicyPart(String bucket, String saveKey, long expiration) {
		super();
		this.bucket = bucket;
		this.saveKey = saveKey;
		this.expiration = expiration;
		logger.debug("bucket={}, saveKey={}, expiration={}", bucket, saveKey,
				expiration);
	}
	
	private boolean isEmpty(String text) {
		return text == null || "".equals(text.trim());
	}

	@Override
	public String toString() {
		if (isEmpty(saveKey)) {
			logger.warn("miss param saveKey");
		}
		if (expiration == 0) {
			logger.warn("miss param expiration");
		}
		if (isEmpty(bucket)) {
			logger.warn("miss param bucket");
		}

		JSONObject obj = new JSONObject();
		try {
			obj.put("save-key", saveKey);
			obj.put("expiration", expiration);
			obj.put("bucket", bucket);
			if (!isEmpty(contentMd5)) {
				obj.put("content-md5", contentMd5);
			}
			if (!isEmpty(contentType)) {
				obj.put("content-type", contentType);
			}
			if (!isEmpty(allowFileType)) {
				obj.put("allow-file-type", allowFileType);
			}
			if (!isEmpty(notifyUrl)) {
				obj.put("notify-url", notifyUrl);
			}
			if (!isEmpty(returnUrl)) {
				obj.put("return-url", returnUrl);
			}
		} catch (JSONException e) {
			logger.warn("", e);
		}
		String result = Base64Coder.encodeString(obj.toString());
		logger.debug(result);
		return result;
	}

	public String getContentMd5() {
		return contentMd5;
	}

	public void setContentMd5(String contentMd5) {
		this.contentMd5 = contentMd5;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getAllowFileType() {
		return allowFileType;
	}

	public void setAllowFileType(String allowFileType) {
		this.allowFileType = allowFileType;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	public String getBucket() {
		return bucket;
	}

	public String getSaveKey() {
		return saveKey;
	}

	public long getExpiration() {
		return expiration;
	}
}
