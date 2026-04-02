package com.chaean.teamchatsa.domain.feedback.model;

import com.chaean.teamchatsa.global.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "feedback")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE app.feedback SET deleted_at = NOW() WHERE id = ?")
public class Feedback extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(name = "sender_user_id", nullable = false)
	private Long senderUserId;

	@Size(max = 50)
	@NotNull
	@Column(name = "sender_name", nullable = false, length = 50)
	private String senderName;

	@Size(max = 1000)
	@NotNull
	@Column(name = "content", nullable = false, length = 1000)
	private String content;

	public static Feedback create(Long senderUserId, String senderName, String content) {
		return Feedback.builder()
				.senderUserId(senderUserId)
				.senderName(senderName)
				.content(content)
				.build();
	}
}
