package com.chaean.teamchatsa.domain.team.dto.response;

import com.chaean.teamchatsa.domain.team.model.Team;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamDetailRes {
	private Long id;
	private Long leaderUserId;
	private String name;
	private String area;
	private String img;
	private String description;
	private Long memberCount;
	private String level;

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
