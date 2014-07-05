package com.yepstudio.legolas.weicity.form;

import java.io.Serializable;

/**
 * 
 * @author zzljob@gmail.com
 * @create 2014年7月4日
 * @version 1.0, 2014年7月4日
 *
 */
public class SignupForm implements Serializable {

	private static final long serialVersionUID = 1750273308027464377L;

	private String telephone;
	private String username;
	private String nickname;
	private Password password;
	private String sex;

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Password getPassword() {
		return password;
	}

	public void setPassword(Password password) {
		this.password = password;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
