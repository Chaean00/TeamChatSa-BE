package com.chaean.teamchatsa.global.exception;

import com.chaean.teamchatsa.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
		log.info("Exception: {}", ex.getMessage(), ex);
		return ResponseEntity
				.status(ex.getErrorCode().getStatus())
				.body(ApiResponse.fail(ex.getMessage()));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse> handleConstraintViolationException(ConstraintViolationException ex) {
		log.info("Exception: {}", ex.getMessage(), ex);

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
		log.info("Exception: {}", ex.getMessage(), ex);

		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getField() + " " + (err.getDefaultMessage() != null ? err.getDefaultMessage() : "유효하지 않습니다"))
				.distinct()
				.collect(java.util.stream.Collectors.joining(", "));

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.fail(message));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
		log.info("Exception: {}", ex.getMessage(), ex);
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.fail("서버 내부 오류가 발생했습니다."));
	}
}
