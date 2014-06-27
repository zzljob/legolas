package com.yepstudio.legolas.bcs;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年5月19日
 * @version 2.0, 2014年5月19日
 *
 */
public class BcsSign {

	private static Logger logger = LoggerFactory.getLogger(BcsSign.class);

	private static final String AccessKey = "AccessKey";
	private static final String SecretKey = "SecretKey";
	private static final String HMAC_SHA1 = "HmacSHA1";

	private String charset = "UTF-8";

	private String method;
	private String bucket;
	private String object;
	private Date time;
	private String ip;
	private Long size;

	public String getMethod() {
		return method;
	}

	public String getBucket() {
		return bucket;
	}

	public String getObject() {
		return object;
	}

	public Date getTime() {
		return time;
	}

	public String getIp() {
		return ip;
	}

	public Long getSize() {
		return size;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	private String signature(String data) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
		byte[] key = SecretKey.getBytes(charset);
		SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA1);
		Mac mac = Mac.getInstance(HMAC_SHA1);
		mac.init(signingKey);
		byte[] rawHmac = mac.doFinal(data.toString().getBytes(charset));
		String signature = new String(Base64.encodeBase64(rawHmac));
		logger.info("Signature:{}", signature);
		String encodeSignature = URLEncoder.encode(signature, charset);
		logger.info("encodeSignature:{}", encodeSignature);
		return encodeSignature;
	}

	@Override
	public String toString() {
		StringBuilder flag = new StringBuilder();
		StringBuilder content = new StringBuilder();
		flag.append("M");
		content.append("Method=").append(method).append("\n");
		flag.append("B");
		content.append("Bucket=").append(bucket).append("\n");
		flag.append("O");
		object = (object == null || object.trim().length() < 1) ? "/" : object;
		content.append("Object=").append(object).append("\n");
		if (time != null) {
			flag.append("T");
			content.append("Time=").append(time.getTime()).append("\n");
		}
		if (ip != null && ip.trim().length() > 0) {
			flag.append("I");
			content.append("Ip=").append(ip).append("\n");
		}
		if (size != null && size > 0) {
			flag.append("S");
			content.append("Size=").append(size).append("\n");
		}
		content.insert(0, "\n");
		content.insert(0, flag.toString());

		logger.info("Content:{}", content.toString());
		logger.info("Flag:{}", flag.toString());
		String signature = null;
		try {
			signature = signature(content.toString());
		} catch (Exception e) {

		}
		logger.info("AccessKey:{}", AccessKey);
		return String.format("%s:%s:%s", flag.toString(), AccessKey, signature);
	}

}
