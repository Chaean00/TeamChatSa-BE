package com.chaean.teamchatsa.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

	// 400
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),
	INVALID_STATE(HttpStatus.BAD_REQUEST, "잘못된 상태입니다."),
	DUPLICATE_RESOURCE(HttpStatus.BAD_REQUEST, "이미 존재하는 자원입니다."),

	// 401
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
	REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Refresh Token이 만료되었습니다. 재로그인이 필요합니다."),

	// 403
	FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
	NOT_TEAM_LEADER(HttpStatus.FORBIDDEN, "팀장이 아닙니다."),
	INSUFFICIENT_TEAM_ROLE(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다."),
	NOT_TEAM_MEMBER(HttpStatus.FORBIDDEN, "팀 멤버가 아닙니다."),

	// 404
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 자원을 찾을 수 없습니다."),
	MATCH_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "매치 게시글을 찾을 수 없습니다."),
	TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."),
	APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "매치 지원서를 찾을 수 없습니다."),

	// 409
	LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT, "다른 요청이 처리 중입니다. 잠시 후 다시 시도해주세요."),

	// 500
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.");

	private final HttpStatus status;
	private String message;
}