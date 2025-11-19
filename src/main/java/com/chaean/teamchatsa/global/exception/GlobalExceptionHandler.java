package com.chaean.teamchatsa.global.exception;

import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse> handleBusinessException(BusinessException ex, HttpServletRequest req) {
		log.info("[BusinessException]: {}", ex.getMessage(), ex);
		return ResponseEntity
				.status(ex.getErrorCode().getStatus())
				.body(ApiResponse.fail(ex.getMessage()));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse> handleConstraintViolationException(ConstraintViolationException ex) {
		log.info("[ConstraintViolationException]: {}", ex.getMessage(), ex);

		String message = ex.getConstraintViolations().stream()
				.map(v -> {
					String path = v.getPropertyPath().toString();
					String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
					return field + "는 필수입니다.";
				})
				.distinct()
				.collect(java.util.stream.Collectors.joining(", "));
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.fail(message));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
		log.info("[MethodArgumentNotValidException]: {}", ex.getMessage(), ex);

		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getField() + " " + (err.getDefaultMessage() != null ? err.getDefaultMessage() : "유효하지 않습니다"))
				.distinct()
				.collect(java.util.stream.Collectors.joining(", "));

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.fail(message));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
		log.info("[DataIntegrityViolationException]: {}", ex.getMessage(), ex);
		String message = resolveMessage(ex);
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.fail(message));
	}

	private String resolveMessage(DataIntegrityViolationException ex) {
		String msg = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();

		if (msg.contains("Duplicate entry") || msg.contains("unique constraint") || msg.contains("Unique index")) {
			if (msg.contains("nickname")) return "이미 사용 중인 닉네임입니다.";
			if (msg.contains("email")) return "이미 등록된 이메일입니다.";
			if (msg.contains("team_member_unique")) return "이미 해당 팀에 가입되어 있습니다.";
			return "중복된 데이터가 존재합니다.";
		}

		if (msg.contains("cannot be null") || msg.contains("null value in column")) {
			return "필수 값이 누락되었습니다.";
		}

		if (msg.contains("check constraint")) {
			return "데이터 형식 또는 값이 잘못되었습니다.";
		}

		// 그 외 예외는 일반 무결성 위반으로 처리
		return "데이터 무결성 제약 조건을 위반했습니다.";
	}

	@ExceptionHandler(RedisConnectionFailureException.class)
	public ResponseEntity<ApiResponse<Void>> handleRedisConnectionFailureException(RedisConnectionFailureException ex) {
		log.info("[RedisConnectionFailureException]: {}", ex.getMessage(), ex);
		return ResponseEntity
				.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(ApiResponse.fail("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
		log.info("[Exception]: {}", ex.getMessage(), ex);
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.fail("서버 내부 오류가 발생했습니다."));
	}
}
