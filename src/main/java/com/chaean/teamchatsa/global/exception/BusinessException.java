package com.chaean.teamchatsa.global.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
	private final ErrorCode errorCode;
	private final String overrideMessage;

	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.overrideMessage = null;
	}

	public BusinessException(ErrorCode errorCode, String overrideMessage) {
		super(overrideMessage != null ? overrideMessage : errorCode.getMessage());
		this.errorCode = errorCode;
		this.overrideMessage = overrideMessage;
	}
}
