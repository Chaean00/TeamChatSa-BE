package com.chaean.teamchatsa.domain.match.dto.response;

import com.chaean.teamchatsa.domain.match.repository.projection.MatchLocationProjection;
import com.chaean.teamchatsa.domain.team.model.TeamLevel;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchMapResponse {

	private Long postId;
	private String matchTitle;
	private LocalDate matchDate;
	private LocalTime matchTime;
	private String teamName;
	private Integer teamLevel;
	private String teamLevelLabel;
	private Double lat;
	private Double lng;

	public static MatchMapResponse from(MatchLocationProjection projection) {
		Integer teamLevel = projection.getLevel();
		return new MatchMapResponse(
				projection.getId(),
				projection.getTitle(),
				projection.getMatchDate().toLocalDate(),
				projection.getMatchDate().toLocalTime(),
				projection.getTeamName(),
				teamLevel,
				teamLevel != null ? TeamLevel.fromValue(teamLevel).getDescription() : null,
				projection.getLat(),
				projection.getLng()
		);
	}
}
