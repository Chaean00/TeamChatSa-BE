package com.chaean.teamchatsa.domain.team.dto.response;

import com.chaean.teamchatsa.domain.team.model.Position;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberRes {
	private Long userId;
	private String userName;
	private String nickname;
	private String email;
	private Position position;
	private TeamRole role;
	private LocalDate joinedAt;

	public TeamMemberRes(Long userId, String userName, String nickname, String email, Position position, TeamRole role, LocalDateTime joinedAt) {
		this.userId = userId;
		this.userName = userName;
		this.nickname = nickname;
		this.email = email;
		this.position = position;
		this.role = role;
		this.joinedAt = joinedAt.toLocalDate();
	}
}
