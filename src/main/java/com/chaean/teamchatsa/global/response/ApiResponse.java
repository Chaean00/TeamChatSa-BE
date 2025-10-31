package com.chaean.teamchatsa.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

	private final boolean success;
	private final String message;
	private final T data;

	public static <T> ApiResponse<T> success(T data) {
		return ApiResponse.<T>builder()
				.success(true)
				.message("요청이 성공적으로 처리되었습니다.")
				.data(data)
				.build();
	}

	public static <T> ApiResponse<T> success(String message, T data) {
		return ApiResponse.<T>builder()
				.success(true)
				.message(message)
				.data(data)
				.build();
	}

	public static ApiResponse<Void> fail(String message) {
		return ApiResponse.<Void>builder()
				.success(false)
				.message(message)
				.build();
	}
}
