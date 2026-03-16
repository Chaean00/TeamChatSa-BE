package com.chaean.teamchatsa.domain.notification.model;

import com.chaean.teamchatsa.global.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE notification SET deleted_at = NOW() WHERE id = ?")
public class Notification extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

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

	@Column(name = "link", length = 255)
	private String link;

	@NotNull
	@Builder.Default
	@Column(name = "is_read", nullable = false)
	private Boolean isRead = false;

	/**
	 * 알림 생성을 위한 정적 팩토리 메서드
	 */
	public static Notification create(Long recipientId, NotificationType type, String content,
			String link) {
		return Notification.builder()
				.recipientId(recipientId)
				.type(type)
				.title(type.getTitle())
				.content(content)
				.link(link)
				.build();
	}

	public void markAsRead() {
		this.isRead = true;
	}
}
