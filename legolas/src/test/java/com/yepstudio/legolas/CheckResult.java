package com.yepstudio.legolas;

public class CheckResult {
	private Boolean success;
	private Integer errorCode;
	private String errorMsg;
	private Response response;

	public static class Response {
		private String app;
		private Integer version;
		private Integer build;
		private String name;
		private String title;
		private String description;
		private Long release;
		private String url;
		private Integer level;
		private Boolean debug;
		private Boolean whiteList;
		private String remark;
		private String sha1;
		private String md5;

		public String getApp() {
			return app;
		}

		public void setApp(String app) {
			this.app = app;
		}

		public Integer getVersion() {
			return version;
		}

		public void setVersion(Integer version) {
			this.version = version;
		}

		public Integer getBuild() {
			return build;
		}

		public void setBuild(Integer build) {
			this.build = build;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Long getRelease() {
			return release;
		}

		public void setRelease(Long release) {
			this.release = release;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public Integer getLevel() {
			return level;
		}

		public void setLevel(Integer level) {
			this.level = level;
		}

		public Boolean getDebug() {
			return debug;
		}

		public void setDebug(Boolean debug) {
			this.debug = debug;
		}

		public Boolean getWhiteList() {
			return whiteList;
		}

		public void setWhiteList(Boolean whiteList) {
			this.whiteList = whiteList;
		}

		public String getRemark() {
			return remark;
		}

		public void setRemark(String remark) {
			this.remark = remark;
		}

		public String getSha1() {
			return sha1;
		}

		public void setSha1(String sha1) {
			this.sha1 = sha1;
		}

		public String getMd5() {
			return md5;
		}

		public void setMd5(String md5) {
			this.md5 = md5;
		}
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public Integer getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}
}
