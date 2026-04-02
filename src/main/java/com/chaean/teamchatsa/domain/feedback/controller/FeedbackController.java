package com.chaean.teamchatsa.domain.feedback.controller;

import com.chaean.teamchatsa.domain.feedback.dto.request.FeedbackCreateRequest;
import com.chaean.teamchatsa.domain.feedback.service.FeedbackService;
import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
@Tag(name = "개발자 의견 API", description = "개발자에게 의견을 전달하는 API")
public class FeedbackController {

	private final FeedbackService feedbackService;

	@PostMapping
	@Operation(summary = "의견 보내기 API", description = "로그인한 사용자의 의견을 저장합니다.")
	public ResponseEntity<ApiResponse<Void>> createFeedback(
			@AuthenticationPrincipal Long userId,
			@RequestBody @Valid FeedbackCreateRequest request
	) {
		feedbackService.createFeedback(userId, request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("의견이 저장되었습니다. 감사합니다!", null));
	}
}
