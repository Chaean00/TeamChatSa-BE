package com.chaean.teamchatsa.domain.match.repository.projection;

import java.time.LocalDateTime;

public interface MatchRecommendationProjection {

	Long getMatchId();

	String getMatchTitle();

	String getPlaceName();

	LocalDateTime getMatchDateTime();

	Long getTeamId();

	String getTeamName();

	String getMatchAddress();

	Integer getTeamLevel();
}
