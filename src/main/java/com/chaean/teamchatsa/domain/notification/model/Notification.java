package com.chaean.teamchatsa.domain.notification.model;

import com.chaean.teamchatsa.global.common.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE notification SET deleted_at = NOW() WHERE id = ?")
public class Notification extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 알림 수신자 ID (User ID) */
	@NotNull
	@Column(name = "recipient_id", nullable = false)
	private Long recipientId;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 50)
	private NotificationType type;

	@NotNull
	@Column(name = "title", nullable = false, length = 100)
	private String title;

	@NotNull
	@Column(name = "content", nullable = false, length = 500)
	private String content;

	/** 관련 링크 (클릭 시 이동할 페이지) */
	@Column(name = "link", length = 255)
	private String link;

	@NotNull
	@Column(name = "is_read", nullable = false)
	private Boolean isRead = false;

	/**
	 * 알림을 읽음 처리
	 */
	public void markAsRead() {
		this.isRead = true;
	}
}
