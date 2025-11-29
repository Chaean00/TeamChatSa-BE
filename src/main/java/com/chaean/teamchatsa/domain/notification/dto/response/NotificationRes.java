package com.chaean.teamchatsa.domain.notification.dto.response;

import com.chaean.teamchatsa.domain.notification.model.Notification;
import com.chaean.teamchatsa.domain.notification.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRes {

	private Long id;
	private NotificationType type;
	private String title;
	private String content;
	private String link;
	private Boolean isRead;
	private LocalDateTime createdAt;

	public static NotificationRes fromEntity(Notification notification) {
		return NotificationRes.builder()
				.id(notification.getId())
				.type(notification.getType())
				.title(notification.getTitle())
				.content(notification.getContent())
				.link(notification.getLink())
				.isRead(notification.getIsRead())
				.createdAt(notification.getCreatedAt())
				.build();
	}
}
