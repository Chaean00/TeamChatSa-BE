package com.chaean.teamchatsa.domain.team.dto.response;

import com.chaean.teamchatsa.domain.team.model.Team;

public record TeamDetailRes(
		Long id,
		Long leaderUserId,
		String name,
		String area,
		String img,
		String description,
		Long memberCount,
		String level
) {
	public static TeamDetailRes fromEntity(Team team, Long memberCount) {
		return new TeamDetailRes(
				team.getId(),
				team.getLeaderUserId(),
				team.getName(),
				team.getArea(),
				team.getImg(),
				team.getDescription(),
				memberCount,
				team.getLevel()
		);
	}
}
