package com.chaean.teamchatsa.domain.notification.repository;

import com.chaean.teamchatsa.domain.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    /**
     * 특정 사용자의 읽지 않은 알림 조회 (최신순)
     * 알림창에서 사용
     */
    List<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId);

    /**
     * 특정 사용자의 모든 알림 조회 (최신순)
     * 알림 이력 페이지에서 사용
     */
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    /**
     * 특정 알림 조회 (권한 검증용)
     * 다른 사용자의 알림을 읽음 처리하지 못하도록
     */
    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);

    /**
     * 읽지 않은 알림 개수 조회
     * 헤더 뱃지 숫자에 사용
     */
    Long countByRecipientIdAndIsReadFalse(Long recipientId);
}