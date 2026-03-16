package com.chaean.teamchatsa.domain.team.model;

import com.chaean.teamchatsa.global.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "team_application", uniqueConstraints = {
		@UniqueConstraint(name = "uc_team_application_user_id", columnNames = {"team_id", "user_id"})
})
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE team_application SET deleted_at = NOW() WHERE id = ?")
public class TeamApplication extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@NotNull
	@Column(name = "team_id", nullable = false)
	private Long teamId;

	@NotNull
	@Column(name = "user_id", nullable = false)
	private Long userId;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private JoinStatus status = JoinStatus.PENDING;

	@Size(max = 255)
	@Column(name = "message")
	private String message;

	public static TeamApplication of(Long teamId, Long userId, String message) {
		TeamApplication application = new TeamApplication();
		application.teamId = teamId;
		application.userId = userId;
		application.message = message;
		return application;
	}

	public void updateStatus(JoinStatus status) {
		this.status = status;
	}
}