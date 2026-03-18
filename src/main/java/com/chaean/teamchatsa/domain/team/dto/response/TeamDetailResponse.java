package com.chaean.teamchatsa.domain.team.dto.response;

import com.chaean.teamchatsa.domain.team.model.Team;
import com.chaean.teamchatsa.domain.team.model.TeamLevel;
import com.chaean.teamchatsa.domain.team.model.TeamRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamDetailResponse {

	private Long id;
	private Long leaderUserId;
	private String name;
	private String area;
	private String img;
	private String description;
	private Long memberCount;
	private Integer level;
	private String levelLabel;
	private TeamRole userRole;

	public static TeamDetailResponse fromEntity(Team team, TeamRole userRole, Long memberCount) {
		TeamLevel level = team.getLevel();
		return new TeamDetailResponse(
				team.getId(),
				team.getLeaderUserId(),
				team.getName(),
				team.getArea(),
				team.getImg(),
				team.getDescription(),
				memberCount,
				level != null ? level.getValue() : null,
				level != null ? level.getDescription() : null,
				userRole
		);
	}
}
