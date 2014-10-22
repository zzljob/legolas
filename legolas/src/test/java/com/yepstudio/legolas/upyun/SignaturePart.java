package com.yepstudio.legolas.upyun;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 给又拍云存储计算签名
 * 
 * @author zzljob@gmail.com
 * @create 2014年10月22日
 * @version 1.0，2014年10月22日
 *
 */
public class SignaturePart {
	private static Logger logger = LoggerFactory.getLogger(SignaturePart.class);
	private static final String charset = "UTF-8";

	private final PolicyPart policy;
	private final String formApiSecret;

	public SignaturePart(PolicyPart policy, String formApiSecret) {
		super();
		this.policy = policy;
		this.formApiSecret = formApiSecret;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(policy == null ? "" : policy.toString());
		builder.append("&");
		builder.append(formApiSecret);
		StringBuilder hashtext = new StringBuilder();
		try {
			MessageDigest md5Digest = MessageDigest.getInstance("MD5");
			md5Digest.update(builder.toString().getBytes(charset));
			byte[] md5byte = md5Digest.digest();

			BigInteger bigInt = new BigInteger(1, md5byte);
			hashtext.append(bigInt.toString(16));
			while (hashtext.length() < 32) {
				hashtext.insert(0, "");
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("", e);
		}
		logger.debug(hashtext.toString());
		return hashtext.toString();
	}
}
