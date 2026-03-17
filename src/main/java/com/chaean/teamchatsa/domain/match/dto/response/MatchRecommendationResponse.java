package com.chaean.teamchatsa.domain.match.dto.response;

import com.chaean.teamchatsa.domain.team.model.TeamLevel;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchRecommendationResponse {

	private Long matchId;
	private String matchTitle;
	private String placeName;
	private LocalDate matchDate;
	private LocalTime matchTime;
	private Long teamId;
	private String teamName;
	private String matchAddress;
	private Integer teamLevel;
	private String teamLevelLabel;
	private String reason;

	public static MatchRecommendationResponse of(MatchRecommendationCandidate candidate, String reason) {
		TeamLevel teamLevel = candidate.getTeamLevel();

		return new MatchRecommendationResponse(
				candidate.getMatchId(),
				candidate.getMatchTitle(),
				candidate.getPlaceName(),
				candidate.getMatchDateTime().toLocalDate(),
				candidate.getMatchDateTime().toLocalTime(),
				candidate.getTeamId(),
				candidate.getTeamName(),
				candidate.getMatchAddress(),
				teamLevel.getValue(),
				teamLevel.getDescription(),
				reason
		);
	}
}
