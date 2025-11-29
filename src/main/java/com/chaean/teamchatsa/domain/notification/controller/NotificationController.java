package com.chaean.teamchatsa.domain.notification.controller;

import com.chaean.teamchatsa.domain.notification.dto.response.NotificationRes;
import com.chaean.teamchatsa.domain.notification.service.NotificationService;
import com.chaean.teamchatsa.global.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {

	private final NotificationService notificationService;

	/** 읽지 않은 알림 목록 조회 */
	@GetMapping
	@Operation(summary = "읽지 않은 알림 조회", description = "현재 사용자의 읽지 않은 알림 목록")
	public ResponseEntity<ApiResponse<List<NotificationRes>>> getUnreadNotifications(
			@AuthenticationPrincipal Long userId) {
		List<NotificationRes> notifications = notificationService.getUnreadNotifications(userId);
		return ResponseEntity.ok(ApiResponse.success(notifications));
	}

	/** 모든 알림 목록 조회 */
	@GetMapping("/all")
	@Operation(summary = "모든 알림 조회", description = "읽음 포함 전체 알림 목록")
	public ResponseEntity<ApiResponse<List<NotificationRes>>> getAllNotifications(
			@AuthenticationPrincipal Long userId) {
		List<NotificationRes> notifications = notificationService.getAllNotifications(userId);
		return ResponseEntity.ok(ApiResponse.success(notifications));
	}

	/** 읽지 않은 알림 개수 조회 */
	@GetMapping("/unread-count")
	@Operation(summary = "읽지 않은 알림 개수", description = "헤더 뱃지에 표시할 알림 개수")
	public ResponseEntity<ApiResponse<Long>> getUnreadCount(
			@AuthenticationPrincipal Long userId) {
		Long count = notificationService.getUnreadCount(userId);
		return ResponseEntity.ok(ApiResponse.success(count));
	}

	/** 특정 알림 읽음 처리 */
	@PatchMapping("/{notificationId}/read")
	@Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경")
	public ResponseEntity<ApiResponse<Void>> markAsRead(
			@PathVariable Long notificationId,
			@AuthenticationPrincipal Long userId) {
		notificationService.markAsRead(notificationId, userId);
		return ResponseEntity.ok(ApiResponse.success("알림을 읽음 처리했습니다.", null));
	}

	/** 모든 알림 읽음 처리 */
	@PatchMapping("/read-all")
	@Operation(summary = "모든 알림 읽음 처리", description = "현재 사용자의 모든 알림을 읽음 상태로 변경")
	public ResponseEntity<ApiResponse<Void>> markAllAsRead(
			@AuthenticationPrincipal Long userId) {
		notificationService.markAllAsRead(userId);
		return ResponseEntity.ok(ApiResponse.success("모든 알림을 읽음 처리했습니다.", null));
	}
}
