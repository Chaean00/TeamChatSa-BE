package com.chaean.teamchatsa.domain.notification.service;

import com.chaean.teamchatsa.domain.notification.dto.response.NotificationRes;
import com.chaean.teamchatsa.domain.notification.model.Notification;
import com.chaean.teamchatsa.domain.notification.model.NotificationType;
import com.chaean.teamchatsa.domain.notification.repository.NotificationRepository;
import com.chaean.teamchatsa.global.common.aop.annotation.Loggable;
import com.chaean.teamchatsa.global.exception.BusinessException;
import com.chaean.teamchatsa.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

	private final NotificationRepository notificationRepo;

	/** 알림 생성 및 DB 저장 */
	@Transactional
	@Loggable
	public Notification createNotification(Long recipientId, NotificationType type,
										   String content, String link) {
		Notification notification = Notification.builder()
				.recipientId(recipientId)
				.type(type)
				.title(type.getTitle())
				.content(content)
				.link(link)
				.isRead(false)  // 기본값: 읽지 않음
				.build();

		Notification saved = notificationRepo.save(notification);
		log.info("알림 저장 완료: recipientId={}, type={}, id={}", recipientId, type, saved.getId());

		return saved;
	}

	/** 읽지 않은 알림 목록 조회 */
	@Transactional(readOnly = true)
	@Loggable
	public List<NotificationRes> getUnreadNotifications(Long userId) {
		List<Notification> notifications = notificationRepo
				.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);

		return notifications.stream()
				.map(NotificationRes::fromEntity)
				.collect(Collectors.toList());
	}

	/** 모든 알림 목록 조회 */
	@Transactional(readOnly = true)
	@Loggable
	public List<NotificationRes> getAllNotifications(Long userId) {
		List<Notification> notifications = notificationRepo
				.findByRecipientIdOrderByCreatedAtDesc(userId);

		return notifications.stream()
				.map(NotificationRes::fromEntity)
				.collect(Collectors.toList());
	}

	/** 알림 읽음 처리 */
	@Transactional
	@Loggable
	public void markAsRead(Long notificationId, Long userId) {
		Notification notification = notificationRepo
				.findByIdAndRecipientId(notificationId, userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "알림을 찾을 수 없습니다."));

		if (notification.getIsRead()) {
			log.debug("이미 읽은 알림: notificationId={}", notificationId);
			return;
		}

		notification.markAsRead();
		log.info("알림 읽음 처리: notificationId={}, userId={}", notificationId, userId);
	}

	/** 모든 알림을 읽음 처리 */
	@Transactional
	@Loggable
	public void markAllAsRead(Long userId) {
		List<Notification> unreadNotifications = notificationRepo
				.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);

		unreadNotifications.forEach(Notification::markAsRead);
		log.info("모든 알림 읽음 처리: userId={}, count={}", userId, unreadNotifications.size());
	}

	/** 읽지 않은 알림 개수 조회 */
	@Transactional(readOnly = true)
	public Long getUnreadCount(Long userId) {
		return notificationRepo.countByRecipientIdAndIsReadFalse(userId);
	}

	/** 여러 사용자에게 동일한 알림  생성 */
	@Transactional
	@Loggable
	public List<Notification> createNotifications(List<Long> recipientIds, NotificationType type, String content, String link
	) {
		if (recipientIds == null || recipientIds.isEmpty()) {
			log.warn("배치 알림 생성 요청이 빈 수신자 목록으로 호출됨");
			return List.of();
		}

		// 배치 생성을 위해 Notification 객체 리스트 생성
		List<Notification> notifications = new ArrayList<>();
		for (Long recipientId : recipientIds) {
			Notification notification = Notification.builder()
					.recipientId(recipientId)
					.type(type)
					.title(type.getTitle())
					.content(content)
					.link(link)
					.isRead(false)
					.build();
			notifications.add(notification);
		}

		// 한 번에 저장
		List<Notification> savedNotifications = notificationRepo.saveAll(notifications);
		log.info("배치 알림 저장 완료: type={}, count={}, recipientIds={}",
				type, savedNotifications.size(), recipientIds);

		return savedNotifications;
	}
}
