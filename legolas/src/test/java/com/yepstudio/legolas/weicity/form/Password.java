package com.yepstudio.legolas.weicity.form;

import java.io.Serializable;

import android.text.TextUtils;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年7月4日
 * @version 1.0, 2014年7月4日
 * 
 */
public class Password implements Serializable {

	private static final long serialVersionUID = 3959912678952676718L;

	private final String original;

	private String target;

	public Password(String original) {
		super();
		this.original = original;
	}
	
	public String encode(String original) {
		return original;
	}

	@Override
	public String toString() {
		if (TextUtils.isEmpty(target)) {
			target = encode(original);
		}
		return target;
	}
}
