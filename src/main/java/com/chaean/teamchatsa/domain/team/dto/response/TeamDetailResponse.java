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
	private TeamLevel level;
	private TeamRole userRole;

	/**
	 * Create a TeamDetailResponse DTO from a Team entity, the requesting user's role, and the team's member count.
	 *
	 * @param team        the Team entity to convert
	 * @param userRole    the role of the requesting user within the team
	 * @param memberCount the team's current member count
	 * @return            a TeamDetailResponse populated with the team's data, the provided userRole, and memberCount
	 */
	public static TeamDetailResponse fromEntity(Team team, TeamRole userRole, Long memberCount) {
		return new TeamDetailResponse(
				team.getId(),
				team.getLeaderUserId(),
				team.getName(),
				team.getArea(),
				team.getImg(),
				team.getDescription(),
				memberCount,
				team.getLevel(),
				userRole
		);
	}
}
