package com.chaean.teamchatsa.domain.team.model;

import com.chaean.teamchatsa.global.common.model.TimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "team_application", uniqueConstraints = {
		@UniqueConstraint(name = "uc_team_application_user_id", columnNames = {"team_id", "user_id"})
})
public class TeamApplication extends TimeEntity {
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
	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private JoinStatus status = JoinStatus.PENDING;

	@Size(max = 255)
	@Column(name = "message")
	private String message;

	public static TeamApplication of(Long teamId, Long userId, String message) {
		return TeamApplication.builder()
				.teamId(teamId)
				.userId(userId)
				.message(message)
				.build();
	}

	public void updateStatus(JoinStatus status) {
		this.status = status;
	}
}