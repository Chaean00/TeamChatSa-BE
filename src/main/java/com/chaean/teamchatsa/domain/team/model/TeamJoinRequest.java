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
@Table(name = "team_join_request")
public class TeamJoinRequest extends TimeEntity {
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

	@Size(max = 30)
	@NotNull
	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private JoinStatus status = JoinStatus.PENDING;

	@Size(max = 255)
	@Column(name = "message")
	private String message;
}