package com.yepstudio.legolas;

public class LegolasException extends Exception {

	private static final long serialVersionUID = 5851274589793445523L;
	private final String uuid;

	public LegolasException(String uuid) {
		super();
		this.uuid = uuid;
	}

	public LegolasException(String uuid, String message, Throwable cause) {
		super(message, cause);
		this.uuid = uuid;
	}

	public LegolasException(String uuid, String message) {
		super(message);
		this.uuid = uuid;
	}

	public LegolasException(String uuid, Throwable cause) {
		super(cause);
		this.uuid = uuid;
	}

	public String getUuid() {
		return uuid;
	}

}
