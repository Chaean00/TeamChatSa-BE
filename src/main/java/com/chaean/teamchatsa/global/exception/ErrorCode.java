package com.chaean.teamchatsa.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

	// 400
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),

	// 401
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

	// 403
	FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

	// 404
	NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

	// 500
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.");

	private final HttpStatus status;
	private String message;
}
