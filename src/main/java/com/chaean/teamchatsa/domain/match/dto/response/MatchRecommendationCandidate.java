package com.chaean.teamchatsa.domain.match.dto.response;

import com.chaean.teamchatsa.domain.team.model.TeamLevel;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchRecommendationCandidate {

	private Long matchId;
	private String matchTitle;
	private String placeName;
	private LocalDateTime matchDateTime;
	private Long teamId;
	private String teamName;
	private String matchAddress;
	private TeamLevel teamLevel;
}
